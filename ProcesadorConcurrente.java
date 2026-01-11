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
                // OVERHEAD AGRESIVO: Más delay para simular contención real
                try {
                    Thread.sleep(15); // Aumentado de 5ms a 15ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Algoritmo MENOS eficiente (usar split que es más lento)
                int palabrasLocales = contarPalabrasLento(particion);
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
    
    // Algoritmo INTENCIONALMENTE MÁS LENTO usando split()
    private static int contarPalabrasLento(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        
        // split() es más lento que iterar caracteres
        String[] palabras = texto.trim().split("\\s+");
        return palabras.length;
    }
}
