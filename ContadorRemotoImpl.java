import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ContadorRemotoImpl extends UnicastRemoteObject implements IContadorRemoto {

    // Pool de hilos para procesamiento paralelo en el servidor
    private final ExecutorService executor;
    private final int numHilos;

    public ContadorRemotoImpl() throws RemoteException {
        super();
        // Usar todos los cores disponibles en el servidor
        this.numHilos = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(numHilos);
        System.out.println("🚀 Servidor optimizado con " + numHilos + " hilos paralelos");
    }

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
    
    @Override
    public int contarPalabrasTexto(String texto) throws RemoteException {
        int total = contarPalabrasParalelo(texto);
        System.out.println("⚡ Procesados " + texto.length() + " bytes → " + total + " palabras (paralelo)");
        return total;
    }
    
    // OPTIMIZACIÓN: Procesamiento paralelo en el servidor
    private int contarPalabrasParalelo(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        
        // Si el texto es pequeño, procesarlo secuencialmente
        if (texto.length() < 10000) {
            return contarPalabras(texto);
        }
        
        // Dividir en chunks para procesamiento paralelo
        int tamañoChunk = texto.length() / numHilos;
        AtomicInteger totalPalabras = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(numHilos);
        
        for (int i = 0; i < numHilos; i++) {
            int inicio = i * tamañoChunk;
            int fin = (i == numHilos - 1) ? texto.length() : (inicio + tamañoChunk);
            
            final String chunk = texto.substring(inicio, fin);
            
            executor.submit(() -> {
                try {
                    int palabras = contarPalabras(chunk);
                    totalPalabras.addAndGet(palabras);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return totalPalabras.get();
    }
    
    private int contarPalabras(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        
        int contador = 0;
        boolean enPalabra = false;
        
        // Optimización: proceso más eficiente
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
