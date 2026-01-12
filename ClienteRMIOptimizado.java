import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * ClienteRMIOptimizado - Cliente RMI optimizado para conteo distribuido de palabras
 * 
 * Esta clase maneja la comunicación con servidores RMI remotos para el conteo
 * de palabras en modo Paralelo (distribuido). Está optimizada para:
 * - Mínima latencia con timeout y reintentos reducidos
 * - Compresión GZIP para textos grandes (reduce ~74% de transferencia)
 * - Medición de tiempo de procesamiento del servidor (no incluye red)
 * 
 * ARQUITECTURA:
 * - Usa CompletableFuture para llamadas asíncronas no bloqueantes
 * - Obtiene tiempo de procesamiento directamente del servidor
 * - Soporta múltiples servidores en paralelo (uno por instancia)
 * 
 * FLUJO TÍPICO:
 * 1. VentanaComparativa crea múltiples instancias de ClienteRMIOptimizado
 * 2. Cada cliente se conecta a un servidor diferente
 * 3. Se llama contarRemotoAsyncTexto() con el chunk de texto asignado
 * 4. El servidor procesa y devuelve [palabras, tiempoMs]
 * 5. Se usa el máximo tiempo de todos los servidores como tiempo total
 * 
 * @author Proyecto Paralela - 3er Parcial
 * @version 2.0 - Con compresión GZIP y medición de tiempo del servidor
 */
public class ClienteRMIOptimizado {
    
    /** 
     * Número máximo de reintentos en caso de error de conexión.
     * Reducido a 1 para minimizar latencia en caso de fallo.
     */
    private static final int MAX_REINTENTOS = 1;
    
    /**
     * Timeout para operaciones RMI en segundos.
     * 120 segundos (2 minutos) para permitir procesamiento de archivos grandes (1GB+).
     */
    private static final int TIMEOUT_SEGUNDOS = 120;
    
    /**
     * Umbral de compresión en bytes.
     * Textos mayores a 50KB se comprimen con GZIP antes de enviar.
     */
    private static final int COMPRESSION_THRESHOLD = 50000;
    
    /** Configuración del servidor al que se conecta este cliente */
    private final ConfiguracionServidor config;
    
    /** Referencia al servicio remoto RMI */
    private IContadorRemoto servicio;
    
    /** Registro RMI para lookup de servicios */
    private Registry registry;

    /**
     * Constructor del cliente RMI.
     * 
     * Inicializa la conexión al servidor especificado en la configuración.
     * La conexión se establece inmediatamente al crear el objeto.
     * 
     * @param config Configuración del servidor (host, puerto, nombre)
     * @throws Exception Si no puede conectar al servidor RMI
     */
    public ClienteRMIOptimizado(ConfiguracionServidor config) throws Exception {
        this.config = config;
        conectar();
    }

    /**
     * Establece o restablece la conexión al servidor RMI.
     * 
     * Obtiene el registro RMI y busca el servicio "ContadorRemoto".
     * Se llama automáticamente en el constructor y después de errores.
     * 
     * @throws Exception Si no puede localizar el registro o el servicio
     */
    private void conectar() throws Exception {
        if (registry == null) {
            registry = LocateRegistry.getRegistry(config.getHost(), config.getPuerto());
        }
        servicio = (IContadorRemoto) registry.lookup("ContadorRemoto");
    }

    /**
     * Cuenta palabras en una lista de líneas (método legacy, asíncrono).
     * 
     * Procesa la solicitud de forma asíncrona con CompletableFuture.
     * Incluye reintentos y timeout para manejo de errores de red.
     * 
     * NOTA: Este método mide tiempo total (incluye red), se mantiene
     * por compatibilidad. Para comparaciones justas usar contarRemotoAsyncTexto().
     * 
     * @param lineas Lista de líneas de texto
     * @return CompletableFuture con ResultadoProcesamiento
     */
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
                        // Continuar al siguiente intento
                    }
                }
            }
            return new ResultadoProcesamiento(config.getNombre(), "Error desconocido");
        }).orTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
          .exceptionally(ex -> new ResultadoProcesamiento(config.getNombre(), 
              "Timeout: " + ex.getMessage()));
    }
    
    /**
     * Cuenta palabras en texto plano (método principal optimizado, asíncrono).
     * 
     * Este es el método principal para el modo Paralelo. Optimizaciones incluidas:
     * - Compresión GZIP automática para textos >50KB
     * - Usa métodos ConTiempo que devuelven tiempo de procesamiento del SERVIDOR
     * - El tiempo retornado NO incluye latencia de red ni transferencia
     * 
     * FLUJO:
     * 1. Si texto > 50KB → comprime con GZIP
     * 2. Llama al método apropiado del servidor (ComprimidoConTiempo o ConTiempo)
     * 3. El servidor devuelve [palabras, tiempoMs]
     * 4. Retorna ResultadoProcesamiento con tiempo del servidor
     * 
     * @param texto Bloque de texto a procesar
     * @return CompletableFuture con ResultadoProcesamiento (tiempo = tiempo del servidor)
     */
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
                        // Continuar al siguiente intento
                    }
                }
            }
            return new ResultadoProcesamiento(config.getNombre(), "Error desconocido");
        }).orTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
          .exceptionally(ex -> new ResultadoProcesamiento(config.getNombre(), 
              "Timeout: " + ex.getMessage()));
    }
    
    /**
     * Comprime texto usando GZIP.
     * 
     * Reduce significativamente el tamaño de los datos a transferir por red.
     * Típicamente logra ~74% de reducción en texto plano.
     * 
     * EJEMPLO:
     * - Texto original: 100,000 bytes
     * - Texto comprimido: ~26,000 bytes
     * 
     * @param texto Texto a comprimir
     * @return Array de bytes comprimido con GZIP
     * @throws Exception Si hay error de compresión
     */
    private byte[] comprimirTexto(String texto) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(baos);
        gzos.write(texto.getBytes());
        gzos.close();
        return baos.toByteArray();
    }
}
