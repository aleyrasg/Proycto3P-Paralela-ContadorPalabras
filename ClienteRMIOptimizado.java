import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ClienteRMIOptimizado {
    private static final int MAX_REINTENTOS = 3;
    private static final int TIMEOUT_SEGUNDOS = 30;
    
    private final ConfiguracionServidor config;
    private IContadorRemoto servicio;

    public ClienteRMIOptimizado(ConfiguracionServidor config) throws Exception {
        this.config = config;
        conectar();
    }

    private void conectar() throws Exception {
        Registry registry = LocateRegistry.getRegistry(config.getHost(), config.getPuerto());
        servicio = (IContadorRemoto) registry.lookup("ContadorRemoto");
    }

    public CompletableFuture<ResultadoProcesamiento> contarRemotoAsync(List<String> lineas) {
        return CompletableFuture.supplyAsync(() -> {
            for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
                try {
                    long inicio = System.currentTimeMillis();
                    int resultado = servicio.contarPalabras(lineas);
                    long tiempo = System.currentTimeMillis() - inicio;
                    return new ResultadoProcesamiento(config.getNombre(), resultado, tiempo);
                } catch (Exception e) {
                    if (intento == MAX_REINTENTOS) {
                        return new ResultadoProcesamiento(config.getNombre(), 
                            "Error después de " + MAX_REINTENTOS + " intentos: " + e.getMessage());
                    }
                    try {
                        Thread.sleep(1000 * intento);
                        conectar();
                    } catch (Exception ex) {
                        // Continuar con siguiente intento
                    }
                }
            }
            return new ResultadoProcesamiento(config.getNombre(), "Error desconocido");
        }).orTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
          .exceptionally(ex -> new ResultadoProcesamiento(config.getNombre(), 
              "Timeout o error: " + ex.getMessage()));
    }
    
    public CompletableFuture<ResultadoProcesamiento> contarRemotoAsyncTexto(String texto) {
        return CompletableFuture.supplyAsync(() -> {
            for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
                try {
                    long inicio = System.currentTimeMillis();
                    int resultado = servicio.contarPalabrasTexto(texto);
                    long tiempo = System.currentTimeMillis() - inicio;
                    return new ResultadoProcesamiento(config.getNombre(), resultado, tiempo);
                } catch (Exception e) {
                    if (intento == MAX_REINTENTOS) {
                        return new ResultadoProcesamiento(config.getNombre(), 
                            "Error después de " + MAX_REINTENTOS + " intentos: " + e.getMessage());
                    }
                    try {
                        Thread.sleep(1000 * intento);
                        conectar();
                    } catch (Exception ex) {
                        // Continuar con siguiente intento
                    }
                }
            }
            return new ResultadoProcesamiento(config.getNombre(), "Error desconocido");
        }).orTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
          .exceptionally(ex -> new ResultadoProcesamiento(config.getNombre(), 
              "Timeout o error: " + ex.getMessage()));
    }
}
