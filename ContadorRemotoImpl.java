import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ContadorRemotoImpl extends UnicastRemoteObject implements IContadorRemoto {

    // Pool de hilos para procesamiento paralelo en el servidor
    private final ExecutorService executor;
    private final int numHilos;

    public ContadorRemotoImpl() throws RemoteException {
        super();
        // OPTIMIZACIÓN AGRESIVA: Usar 2x cores para saturar CPU
        this.numHilos = Runtime.getRuntime().availableProcessors() * 2;
        this.executor = Executors.newFixedThreadPool(numHilos);
        System.out.println("🚀 Servidor ULTRA-optimizado con " + numHilos + " hilos paralelos");
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
        int total = contarPalabrasParaleloAgresivo(texto);
        System.out.println("⚡ Procesados " + texto.length() + " bytes → " + total + " palabras (ultra-paralelo)");
        return total;
    }
    
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
    
    // OPTIMIZACIÓN AGRESIVA: Procesamiento ultra-paralelo sin límites
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
    
    // OPTIMIZACIÓN: Algoritmo más rápido sin split()
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
    
    // NUEVO: Devuelve [palabras, tiempoMs] - SOLO mide tiempo de procesamiento
    @Override
    public long[] contarPalabrasConTiempo(String texto) throws RemoteException {
        long inicio = System.currentTimeMillis();
        int palabras = contarPalabrasParaleloAgresivo(texto);
        long tiempo = System.currentTimeMillis() - inicio;
        System.out.println("⏱️ Procesamiento: " + texto.length() + " bytes → " + palabras + " palabras en " + tiempo + " ms");
        return new long[]{palabras, tiempo};
    }
    
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
