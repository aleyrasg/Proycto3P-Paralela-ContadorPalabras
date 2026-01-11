import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IContadorRemoto extends Remote {
    int contarPalabras(List<String> lineas) throws RemoteException;
    int contarPalabrasTexto(String texto) throws RemoteException;
    int contarPalabrasComprimido(byte[] textoComprimido) throws RemoteException;
    
    // NUEVO: Devuelve [palabras, tiempoMs] para medir solo procesamiento
    long[] contarPalabrasConTiempo(String texto) throws RemoteException;
    long[] contarPalabrasComprimidoConTiempo(byte[] textoComprimido) throws RemoteException;
}
