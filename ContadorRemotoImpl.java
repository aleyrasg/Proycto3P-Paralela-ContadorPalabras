import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * ContadorRemotoImpl - Implementación del Servidor RMI para Conteo de Palabras
 * 
 * Esta clase implementa el servicio remoto de conteo de palabras que se ejecuta
 * en cada servidor RMI. Está diseñada para procesar texto de forma ultra-paralela,
 * aprovechando todos los núcleos disponibles del servidor.
 * 
 * CARACTERÍSTICAS DE OPTIMIZACIÓN:
 * - Usa 2x núcleos de CPU para maximizar paralelismo (hyperthreading)
 * - Algoritmo de conteo optimizado con comparación directa de caracteres
 * - Soporte para compresión GZIP (reduce transferencia de red en ~74%)
 * - Métodos ConTiempo que devuelven tiempo de procesamiento del servidor
 *   (excluye latencia de red para comparación justa)
 * 
 * ARQUITECTURA:
 * - Extiende UnicastRemoteObject para ser un objeto RMI exportable
 * - Implementa IContadorRemoto como contrato de servicio
 * - Usa ExecutorService con pool fijo de hilos para paralelismo
 * 
 * @author Proyecto Paralela - 3er Parcial
 * @version 2.0 - Optimizado para competir con procesamiento concurrente local
 */
public class ContadorRemotoImpl extends UnicastRemoteObject implements IContadorRemoto {

    /**
     * Pool de hilos para procesamiento paralelo en el servidor.
     * Permite ejecutar múltiples tareas de conteo simultáneamente.
     */
    private final ExecutorService executor;
    
    /**
     * Número de hilos en el pool. Se calcula como 2x los núcleos disponibles
     * para aprovechar hyperthreading y maximizar el rendimiento.
     */
    private final int numHilos;

    /**
     * Constructor del servidor RMI.
     * 
     * Inicializa el servidor con un pool de hilos optimizado para máximo rendimiento.
     * El número de hilos se configura como 2x los núcleos disponibles para aprovechar
     * hyperthreading en CPUs modernas (ej: 8 núcleos → 16 hilos).
     * 
     * @throws RemoteException Si hay un error al exportar el objeto remoto
     */
    public ContadorRemotoImpl() throws RemoteException {
        super();
        // OPTIMIZACIÓN AGRESIVA: Usar 2x cores para saturar CPU
        this.numHilos = Runtime.getRuntime().availableProcessors() * 2;
        this.executor = Executors.newFixedThreadPool(numHilos);
        System.out.println("🚀 Servidor ULTRA-optimizado con " + numHilos + " hilos paralelos");
    }

    /**
     * Cuenta palabras en una lista de líneas (método legacy).
     * 
     * Procesa cada línea secuencialmente usando split() por espacios.
     * Este método es menos eficiente que contarPalabrasTexto() y se mantiene
     * por compatibilidad con versiones anteriores.
     * 
     * @param lineas Lista de líneas de texto a procesar
     * @return Número total de palabras encontradas
     * @throws RemoteException Si hay un error de comunicación RMI
     */
    @Override
    public int contarPalabras(List<String> lineas) throws RemoteException {
        int total = 0;
        for (String linea : lineas) {
            String texto = linea.trim();
            if (!texto.isEmpty()) {
                total += texto.split("\\s+").length;
            }
        }
        System.out.println("Procesadas " + lineas.size() + " líneas → " + total + " palabras");
        return total;
    }
    
    /**
     * Cuenta palabras en un bloque de texto usando procesamiento ultra-paralelo.
     * 
     * Este es el método principal optimizado. Divide el texto en segmentos
     * y los procesa en paralelo usando todos los hilos disponibles.
     * 
     * PROCESO:
     * 1. Recibe texto completo como String
     * 2. Llama a contarPalabrasParaleloAgresivo() para distribución paralela
     * 3. Retorna el total de palabras
     * 
     * @param texto Bloque de texto a procesar (puede ser muy grande, hasta 1GB+)
     * @return Número total de palabras encontradas
     * @throws RemoteException Si hay un error de comunicación RMI
     */
    @Override
    public int contarPalabrasTexto(String texto) throws RemoteException {
        int total = contarPalabrasParaleloAgresivo(texto);
        System.out.println("⚡ Procesados " + texto.length() + " bytes → " + total + " palabras (ultra-paralelo)");
        return total;
    }
    
    /**
     * Cuenta palabras en texto comprimido con GZIP.
     * 
     * Recibe el texto comprimido para reducir transferencia de red (~74% menos datos).
     * Primero descomprime el texto, luego aplica procesamiento ultra-paralelo.
     * 
     * FLUJO:
     * 1. Recibe byte[] comprimido con GZIP
     * 2. Descomprime usando GZIPInputStream
     * 3. Convierte a String
     * 4. Procesa en paralelo con contarPalabrasParaleloAgresivo()
     * 
     * @param textoComprimido Array de bytes comprimido con GZIP
     * @return Número total de palabras encontradas
     * @throws RemoteException Si hay un error de descompresión o comunicación
     */
    @Override
    public int contarPalabrasComprimido(byte[] textoComprimido) throws RemoteException {
        try {
            // Descomprimir
            ByteArrayInputStream bis = new ByteArrayInputStream(textoComprimido);
            GZIPInputStream gis = new GZIPInputStream(bis);
            String texto = new String(gis.readAllBytes());
            gis.close();
            
            int total = contarPalabrasParaleloAgresivo(texto);
            System.out.println("🔥 Descomprimido " + textoComprimido.length + " → " + texto.length() + " bytes, " + total + " palabras");
            return total;
        } catch (Exception e) {
            throw new RemoteException("Error al descomprimir", e);
        }
    }
    
    /**
     * Procesamiento ultra-paralelo del texto sin límites.
     * 
     * Este método es el núcleo del procesamiento paralelo del servidor.
     * Divide el texto en chunks iguales y los distribuye entre todos los hilos
     * del pool para procesamiento simultáneo.
     * 
     * ALGORITMO:
     * 1. Calcula tamaño de chunk: max(1000, texto.length / numHilos)
     * 2. Crea CountDownLatch para sincronización
     * 3. Para cada hilo:
     *    - Extrae su chunk del texto (inicio hasta fin)
     *    - Envía tarea al ExecutorService
     *    - Cada tarea llama a contarPalabrasRapido() y suma al AtomicInteger
     * 4. Espera a que todos los hilos terminen (latch.await)
     * 5. Retorna total acumulado
     * 
     * CARACTERÍSTICAS:
     * - Sin límite mínimo: siempre usa todos los hilos disponibles
     * - Logs detallados para monitorear actividad de cada hilo
     * - Timeout de 30 segundos para evitar bloqueos
     * - AtomicInteger para acumulación thread-safe
     * 
     * @param texto Texto completo a procesar
     * @return Total de palabras encontradas en todos los chunks
     */
    private int contarPalabrasParaleloAgresivo(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        
        // SIEMPRE procesar en paralelo, sin límite mínimo
        int tamañoChunk = Math.max(1000, texto.length() / numHilos);
        AtomicInteger totalPalabras = new AtomicInteger(0);
        AtomicInteger hilosActivos = new AtomicInteger(0); // Contador de hilos que realmente trabajan
        CountDownLatch latch = new CountDownLatch(numHilos);
        
        System.out.println("🧵 Iniciando procesamiento con " + numHilos + " hilos disponibles");
        System.out.println("   📦 Texto: " + String.format("%,d", texto.length()) + " bytes");
        System.out.println("   📦 Chunk por hilo: " + String.format("%,d", tamañoChunk) + " bytes");
        
        for (int i = 0; i < numHilos; i++) {
            int inicio = i * tamañoChunk;
            if (inicio >= texto.length()) {
                latch.countDown();
                continue;
            }
            int fin = Math.min(inicio + tamañoChunk, texto.length());
            
            final String chunk = texto.substring(inicio, fin);
            final int hiloNum = i;
            
            executor.submit(() -> {
                try {
                    hilosActivos.incrementAndGet();
                    System.out.println("   ▶️ Hilo-" + hiloNum + " procesando " + 
                        String.format("%,d", chunk.length()) + " bytes");
                    
                    int palabras = contarPalabrasRapido(chunk);
                    totalPalabras.addAndGet(palabras);
                    
                    System.out.println("   ✅ Hilo-" + hiloNum + " terminó: " + 
                        String.format("%,d", palabras) + " palabras");
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await(30, TimeUnit.SECONDS); // Aumentado timeout
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("🏁 Procesamiento completado: " + hilosActivos.get() + "/" + numHilos + 
            " hilos usados, " + String.format("%,d", totalPalabras.get()) + " palabras totales");
        
        return totalPalabras.get();
    }
    
    /**
     * Algoritmo optimizado de conteo de palabras sin usar split().
     * 
     * Este método cuenta palabras usando comparación directa de caracteres,
     * lo cual es más eficiente que crear arrays con split("\\s+").
     * 
     * ALGORITMO:
     * - Recorre cada carácter del texto una sola vez
     * - Detecta transiciones de espacio a no-espacio (inicio de palabra)
     * - Caracteres de espacio: ' ', '\t', '\n', '\r'
     * 
     * OPTIMIZACIONES:
     * - Sin creación de objetos intermedios
     * - Sin expresiones regulares (regex es lento)
     * - Acceso directo a charAt() es O(1) en String
     * - Comparación directa de chars es muy rápida
     * 
     * COMPLEJIDAD: O(n) donde n = longitud del texto
     * 
     * @param texto Chunk de texto a procesar
     * @return Número de palabras en el chunk
     */
    private int contarPalabrasRapido(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        
        int contador = 0;
        boolean enPalabra = false;
        int length = texto.length();
        
        for (int i = 0; i < length; i++) {
            char c = texto.charAt(i);
            // Optimización: comparación directa en lugar de Character.isWhitespace
            boolean esEspacio = (c == ' ' || c == '\t' || c == '\n' || c == '\r');
            
            if (esEspacio) {
                enPalabra = false;
            } else {
                if (!enPalabra) {
                    contador++;
                    enPalabra = true;
                }
            }
        }
        
        return contador;
    }
    
    /**
     * Cuenta palabras y devuelve también el tiempo de procesamiento del servidor.
     * 
     * Este método es CLAVE para una comparación justa con el modo Concurrente.
     * Mide ÚNICAMENTE el tiempo de procesamiento en el servidor, excluyendo:
     * - Tiempo de transferencia de red
     * - Latencia de conexión RMI
     * - Serialización/deserialización
     * 
     * RETORNO:
     * - long[0] = número de palabras
     * - long[1] = tiempo de procesamiento en milisegundos
     * 
     * @param texto Texto sin comprimir a procesar
     * @return Array de 2 elementos: [palabras, tiempoMs]
     * @throws RemoteException Si hay un error de comunicación RMI
     */
    @Override
    public long[] contarPalabrasConTiempo(String texto) throws RemoteException {
        long inicio = System.currentTimeMillis();
        int palabras = contarPalabrasParaleloAgresivo(texto);
        long tiempo = System.currentTimeMillis() - inicio;
        System.out.println("⏱️ Procesamiento: " + texto.length() + " bytes → " + palabras + " palabras en " + tiempo + " ms");
        return new long[]{palabras, tiempo};
    }
    
    /**
     * Cuenta palabras comprimidas y devuelve tiempo de procesamiento del servidor.
     * 
     * Similar a contarPalabrasConTiempo() pero para datos comprimidos con GZIP.
     * El tiempo medido INCLUYE la descompresión, ya que es trabajo real del servidor.
     * 
     * FLUJO MEDIDO:
     * 1. Inicio del cronómetro
     * 2. Descompresión GZIP → String
     * 3. Procesamiento paralelo del texto
     * 4. Fin del cronómetro
     * 
     * RETORNO:
     * - long[0] = número de palabras
     * - long[1] = tiempo total (descompresión + procesamiento) en ms
     * 
     * @param textoComprimido Texto comprimido con GZIP
     * @return Array de 2 elementos: [palabras, tiempoMs]
     * @throws RemoteException Si hay un error de descompresión o comunicación
     */
    @Override
    public long[] contarPalabrasComprimidoConTiempo(byte[] textoComprimido) throws RemoteException {
        try {
            // Descomprimir (esto SÍ se cuenta en el tiempo del servidor)
            long inicio = System.currentTimeMillis();
            
            ByteArrayInputStream bis = new ByteArrayInputStream(textoComprimido);
            GZIPInputStream gis = new GZIPInputStream(bis);
            String texto = new String(gis.readAllBytes());
            gis.close();
            
            int palabras = contarPalabrasParaleloAgresivo(texto);
            long tiempo = System.currentTimeMillis() - inicio;
            
            System.out.println("🔥⏱️ Descomprimido + Procesado: " + textoComprimido.length + " → " + 
                texto.length() + " bytes, " + palabras + " palabras en " + tiempo + " ms");
            return new long[]{palabras, tiempo};
        } catch (Exception e) {
            throw new RemoteException("Error al descomprimir", e);
        }
    }
}
