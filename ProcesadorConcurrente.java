import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ProcesadorConcurrente - Procesador local multi-hilo para conteo de palabras
 * 
 * Esta clase implementa el modo "Concurrente" del sistema de comparación.
 * Procesa el texto localmente usando múltiples hilos con ExecutorService,
 * sin necesidad de comunicación por red.
 * 
 * PROPÓSITO:
 * - Servir como punto de comparación para el modo Paralelo (RMI)
 * - Demostrar procesamiento multi-hilo local con Java Concurrency API
 * - Usar el mismo algoritmo optimizado que el servidor para comparación justa
 * 
 * ARQUITECTURA:
 * - Usa ExecutorService con pool fijo de hilos
 * - Divide el texto en particiones iguales (una por hilo)
 * - AtomicInteger para acumulación thread-safe del total
 * - Future<?> para esperar completación de todas las tareas
 * 
 * DIFERENCIA CON PARALELO (RMI):
 * - Concurrente: Todo el procesamiento ocurre en UNA máquina
 * - Paralelo: El trabajo se distribuye entre MÚLTIPLES máquinas por red
 * 
 * @author Proyecto Paralela - 3er Parcial
 * @version 2.0 - Optimizado con mismo algoritmo que servidor RMI
 */
public class ProcesadorConcurrente {
    
    /**
     * Procesa texto usando múltiples hilos locales.
     * 
     * Método principal que divide el texto y lo procesa en paralelo.
     * Cada hilo procesa una partición del texto y contribuye al total.
     * 
     * ALGORITMO:
     * 1. Crear ExecutorService con el número de hilos especificado
     * 2. Dividir el texto en particiones iguales (texto.length / numHilos)
     * 3. Enviar cada partición como tarea al executor
     * 4. Cada tarea cuenta palabras y suma a AtomicInteger
     * 5. Esperar a que todas las tareas terminen (Future.get())
     * 6. Cerrar el executor y retornar resultado
     * 
     * NOTA: El tiempo medido incluye TODO el procesamiento local,
     * equivalente al tiempo de procesamiento del servidor en modo RMI.
     * 
     * @param texto Texto completo a procesar
     * @param numHilos Número de hilos a usar (típicamente = núcleos de CPU)
     * @return ResultadoProcesamiento con conteo y tiempo
     * @throws Exception Si hay error en el procesamiento
     */
    public static ResultadoProcesamiento procesarTexto(String texto, int numHilos) throws Exception {
        long inicio = System.currentTimeMillis();
        
        // Crear pool de hilos fijo con la cantidad especificada
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);
        AtomicInteger totalPalabras = new AtomicInteger(0);
        
        // Dividir texto por bytes entre hilos
        int tamañoTotal = texto.length();
        int tamañoParticion = tamañoTotal / numHilos;
        List<Future<?>> futuros = new ArrayList<>();
        
        // Crear y enviar tareas para cada partición
        for (int i = 0; i < numHilos; i++) {
            int inicio_idx = i * tamañoParticion;
            // Último hilo toma el resto (evita perder caracteres por división entera)
            int fin_idx = (i == numHilos - 1) ? tamañoTotal : (inicio_idx + tamañoParticion);
            
            String particion = texto.substring(inicio_idx, fin_idx);
            
            // Enviar tarea al executor
            Future<?> futuro = executor.submit(() -> {
                // Procesar la partición sin overhead artificial
                int palabrasLocales = contarPalabras(particion);
                totalPalabras.addAndGet(palabrasLocales);
            });
            
            futuros.add(futuro);
        }
        
        // Esperar a que todos terminen (bloquea hasta completar)
        for (Future<?> futuro : futuros) {
            futuro.get();
        }
        
        // Limpiar recursos del executor
        executor.shutdown();
        
        long tiempo = System.currentTimeMillis() - inicio;
        return new ResultadoProcesamiento("Concurrente (" + numHilos + " hilos)", 
                                         totalPalabras.get(), tiempo);
    }
    
    /**
     * Cuenta palabras usando algoritmo optimizado (igual que servidor RMI).
     * 
     * Este método usa EXACTAMENTE el mismo algoritmo que contarPalabrasRapido()
     * en ContadorRemotoImpl, garantizando una comparación justa de rendimiento.
     * 
     * ALGORITMO:
     * - Recorre cada carácter una sola vez (O(n))
     * - Detecta transiciones de espacio a no-espacio
     * - Usa comparación directa de caracteres (sin regex ni split)
     * 
     * OPTIMIZACIONES:
     * - No usa split("\\s+") que es costoso
     * - No usa Character.isWhitespace() que tiene overhead
     * - Comparación directa de chars: ' ', '\t', '\n', '\r'
     * 
     * @param texto Partición de texto a procesar
     * @return Número de palabras encontradas
     */
    private static int contarPalabras(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        
        int palabras = 0;
        boolean enPalabra = false;
        int length = texto.length();
        
        // Recorrer cada carácter una sola vez
        for (int i = 0; i < length; i++) {
            char c = texto.charAt(i);
            
            // Comparación directa (más rápido que Character.isWhitespace)
            boolean esEspacio = (c == ' ' || c == '\t' || c == '\n' || c == '\r');
            
            if (esEspacio) {
                // Saliendo de una palabra
                enPalabra = false;
            } else {
                // Si no estábamos en palabra, encontramos una nueva
                if (!enPalabra) {
                    palabras++;
                    enPalabra = true;
                }
            }
        }
        
        return palabras;
    }
}
