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
                // MENOS EFICIENTE: Añadir pequeño delay para simular overhead
                try {
                    Thread.sleep(5); // Pequeño overhead por hilo
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
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
    
    private static int contarPalabras(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        
        int contador = 0;
        boolean enPalabra = false;
        
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (Character.isWhitespace(c)) {
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
}
