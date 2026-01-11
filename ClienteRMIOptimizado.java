import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

public class ClienteRMIOptimizado {
    private static final int MAX_REINTENTOS = 1; // Solo 1 reintento
    private static final int TIMEOUT_SEGUNDOS = 120; // 2 minutos para archivos grandes
    
    private final ConfiguracionServidor config;
    private IContadorRemoto servicio;
    private Registry registry;
    private static final int COMPRESSION_THRESHOLD = 50000; // Comprimir si >50KB

    public ClienteRMIOptimizado(ConfiguracionServidor config) throws Exception {
        this.config = config;
        conectar();
    }

    private void conectar() throws Exception {
        if (registry == null) {
            registry = LocateRegistry.getRegistry(config.getHost(), config.getPuerto());
        }
        servicio = (IContadorRemoto) registry.lookup("ContadorRemoto");
    }

    public CompletableFuture<ResultadoProcesamiento> contarRemotoAsync(List<String> lineas) {
        return CompletableFuture.supplyAsync(() -> {
            for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
                try {
                    long inicio = System.nanoTime();
                    int resultado = servicio.contarPalabras(lineas);
                    long tiempo = (System.nanoTime() - inicio) / 1_000_000;
                    return new ResultadoProcesamiento(config.getNombre(), resultado, tiempo);
                } catch (Exception e) {
                    if (intento == MAX_REINTENTOS) {
                        return new ResultadoProcesamiento(config.getNombre(), 
                            "Error: " + e.getMessage());
                    }
                    try {
                        Thread.sleep(200);
                        conectar();
                    } catch (Exception ex) {
                        // Continuar
                    }
                }
            }
            return new ResultadoProcesamiento(config.getNombre(), "Error desconocido");
        }).orTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
          .exceptionally(ex -> new ResultadoProcesamiento(config.getNombre(), 
              "Timeout: " + ex.getMessage()));
    }
    
    public CompletableFuture<ResultadoProcesamiento> contarRemotoAsyncTexto(String texto) {
        return CompletableFuture.supplyAsync(() -> {
            for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
                try {
                    long[] resultado; // [palabras, tiempoMs] - tiempo medido en el SERVIDOR
                    
                    // OPTIMIZACIÓN: Comprimir si el texto es grande
                    if (texto.length() > COMPRESSION_THRESHOLD) {
                        byte[] comprimido = comprimirTexto(texto);
                        // Usar método que devuelve tiempo del SERVIDOR (no incluye conexión)
                        resultado = servicio.contarPalabrasComprimidoConTiempo(comprimido);
                        System.out.println("🔥 Compresión: " + texto.length() + " → " + 
                            comprimido.length + " bytes (" + 
                            String.format("%.1f%%", 100.0 * comprimido.length / texto.length()) + ")");
                    } else {
                        // Usar método que devuelve tiempo del SERVIDOR (no incluye conexión)
                        resultado = servicio.contarPalabrasConTiempo(texto);
                    }
                    
                    // resultado[0] = palabras, resultado[1] = tiempo de PROCESAMIENTO (ms)
                    return new ResultadoProcesamiento(config.getNombre(), (int)resultado[0], resultado[1]);
                } catch (Exception e) {
                    if (intento == MAX_REINTENTOS) {
                        return new ResultadoProcesamiento(config.getNombre(), 
                            "Error: " + e.getMessage());
                    }
                    try {
                        Thread.sleep(200);
                        conectar();
                    } catch (Exception ex) {
                        // Continuar
                    }
                }
            }
            return new ResultadoProcesamiento(config.getNombre(), "Error desconocido");
        }).orTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
          .exceptionally(ex -> new ResultadoProcesamiento(config.getNombre(), 
              "Timeout: " + ex.getMessage()));
    }
    
    private byte[] comprimirTexto(String texto) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(baos);
        gzos.write(texto.getBytes());
        gzos.close();
        return baos.toByteArray();
    }
}
