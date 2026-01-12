/**
 * ProcesadorSecuencial - Procesador de un solo hilo para conteo de palabras
 * 
 * Esta clase implementa el modo "Secuencial" del sistema de comparación.
 * Procesa todo el texto en un solo hilo, sin paralelismo ni distribución.
 * 
 * PROPÓSITO:
 * - Servir como línea base para comparar con modos Concurrente y Paralelo
 * - Demostrar el rendimiento sin optimizaciones de paralelismo
 * - Mostrar cómo el paralelismo mejora el rendimiento en tareas CPU-bound
 * 
 * CARACTERÍSTICAS:
 * - Un solo hilo de ejecución
 * - Sin overhead de sincronización ni coordinación
 * - El método más simple pero más lento para archivos grandes
 * 
 * CUÁNDO ES COMPETITIVO:
 * - Archivos muy pequeños (overhead de paralelismo supera beneficio)
 * - CPUs con pocos núcleos
 * - Cuando el costo de crear hilos supera el beneficio
 * 
 * ESPERADO EN PRUEBAS:
 * - Debe ser el MÁS LENTO para archivos grandes
 * - Debería perder contra Concurrente (N hilos)
 * - Debería perder contra Paralelo (N servidores)
 * 
 * @author Proyecto Paralela - 3er Parcial
 * @version 1.0 - Implementación base sin optimizaciones
 */
public class ProcesadorSecuencial {
    
    /**
     * Procesa texto usando un solo hilo.
     * 
     * Método principal que cuenta todas las palabras secuencialmente.
     * No divide el trabajo ni usa múltiples hilos.
     * 
     * @param texto Texto completo a procesar
     * @return ResultadoProcesamiento con conteo y tiempo
     */
    public static ResultadoProcesamiento procesarTexto(String texto) {
        long inicio = System.currentTimeMillis();
        
        int totalPalabras = contarPalabras(texto);
        
        long tiempo = System.currentTimeMillis() - inicio;
        return new ResultadoProcesamiento("Secuencial", totalPalabras, tiempo);
    }
    
    /**
     * Cuenta palabras recorriendo el texto carácter por carácter.
     * 
     * ALGORITMO:
     * - Recorre cada carácter secuencialmente
     * - Detecta transiciones de espacio a no-espacio
     * - Cuenta cada inicio de palabra
     * 
     * DIFERENCIA CON Concurrente/RMI:
     * - Usa Character.isWhitespace() en lugar de comparación directa
     * - Es ligeramente más lento pero más legible
     * - Detecta más tipos de espacios (Unicode)
     * 
     * COMPLEJIDAD: O(n) donde n = longitud del texto
     * 
     * @param texto Texto a procesar
     * @return Número total de palabras
     */
    private static int contarPalabras(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        
        int contador = 0;
        boolean enPalabra = false;
        
        // Recorrer cada carácter secuencialmente
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            
            // Character.isWhitespace detecta espacios Unicode
            if (Character.isWhitespace(c)) {
                enPalabra = false;
            } else {
                // Si no estábamos en palabra, encontramos una nueva
                if (!enPalabra) {
                    contador++;
                    enPalabra = true;
                }
            }
        }
        
        return contador;
    }
}
