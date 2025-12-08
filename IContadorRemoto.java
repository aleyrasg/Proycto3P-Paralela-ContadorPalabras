import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IContadorRemoto extends Remote {
    int contarPalabras(List<String> lineas) throws RemoteException;
    int contarPalabrasTexto(String texto) throws RemoteException;
}
