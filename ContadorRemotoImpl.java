import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.List;

public class ContadorRemotoImpl extends UnicastRemoteObject implements IContadorRemoto {

    public ContadorRemotoImpl() throws RemoteException {
        super();
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
        int total = contarPalabras(texto);
        System.out.println("Procesados " + texto.length() + " bytes → " + total + " palabras");
        return total;
    }
    
    private int contarPalabras(String texto) {
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
