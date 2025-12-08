public class ProcesadorSecuencial {
    
    public static ResultadoProcesamiento procesarTexto(String texto) {
        long inicio = System.currentTimeMillis();
        
        int totalPalabras = contarPalabras(texto);
        
        long tiempo = System.currentTimeMillis() - inicio;
        return new ResultadoProcesamiento("Secuencial", totalPalabras, tiempo);
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
