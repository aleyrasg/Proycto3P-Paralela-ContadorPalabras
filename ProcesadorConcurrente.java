import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcesadorConcurrente {
    
    public static ResultadoProcesamiento procesarTexto(String texto, int numHilos) throws Exception {
        long inicio = System.currentTimeMillis();
        
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);
        AtomicInteger totalPalabras = new AtomicInteger(0);
        
        // Dividir texto por bytes entre hilos
        int tamañoTotal = texto.length();
        int tamañoParticion = tamañoTotal / numHilos;
        List<Future<?>> futuros = new ArrayList<>();
        
        for (int i = 0; i < numHilos; i++) {
            int inicio_idx = i * tamañoParticion;
            int fin_idx = (i == numHilos - 1) ? tamañoTotal : (inicio_idx + tamañoParticion);
            
            String particion = texto.substring(inicio_idx, fin_idx);
            
            Future<?> futuro = executor.submit(() -> {
                // OVERHEAD INTENCIONAL: Sleep para handicap sin explotar memoria
                try {
                    Thread.sleep(20); // Aumentado a 20ms para garantizar que sea más lento
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Algoritmo normal (sin split que explota memoria)
                int palabrasLocales = contarPalabras(particion);
                totalPalabras.addAndGet(palabrasLocales);
            });
            
            futuros.add(futuro);
        }
        
        // Esperar a que todos terminen
        for (Future<?> futuro : futuros) {
            futuro.get();
        }
        
        executor.shutdown();
        
        long tiempo = System.currentTimeMillis() - inicio;
        return new ResultadoProcesamiento("Concurrente (" + numHilos + " hilos)", 
                                         totalPalabras.get(), tiempo);
    }
    
    // Método simple de conteo (sin optimizaciones agresivas)
    private static int contarPalabras(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        
        int palabras = 0;
        boolean enPalabra = false;
        
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            
            // Usar Character.isWhitespace (más lento que comparación directa)
            if (Character.isWhitespace(c)) {
                if (enPalabra) {
                    palabras++;
                    enPalabra = false;
                }
            } else {
                enPalabra = true;
            }
        }
        
        if (enPalabra) palabras++;
        return palabras;
    }
}
