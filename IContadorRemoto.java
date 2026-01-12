import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * IContadorRemoto - Interfaz Remota RMI
 * 
 * Define los métodos que pueden ser invocados remotamente por los clientes.
 * Esta interfaz extiende Remote para indicar que sus métodos pueden ser
 * llamados desde otra JVM (Java Virtual Machine) a través de la red.
 * 
 * Todos los métodos deben lanzar RemoteException para manejar errores de red.
 * 
 * @author Proyecto Paralela 3P
 */
public interface IContadorRemoto extends Remote {
    
    /**
     * Cuenta palabras en una lista de líneas de texto.
     * @param lineas Lista de strings, cada uno representa una línea del archivo
     * @return Número total de palabras encontradas
     * @throws RemoteException Si hay error de comunicación con el servidor
     */
    int contarPalabras(List<String> lineas) throws RemoteException;
    
    /**
     * Cuenta palabras en un texto plano (sin comprimir).
     * @param texto String con el contenido completo del texto
     * @return Número total de palabras encontradas
     * @throws RemoteException Si hay error de comunicación con el servidor
     */
    int contarPalabrasTexto(String texto) throws RemoteException;
    
    /**
     * Cuenta palabras en un texto comprimido con GZIP.
     * Útil para reducir el tráfico de red en archivos grandes.
     * @param textoComprimido Bytes del texto comprimido con GZIP
     * @return Número total de palabras encontradas
     * @throws RemoteException Si hay error de comunicación con el servidor
     */
    int contarPalabrasComprimido(byte[] textoComprimido) throws RemoteException;
    
    /**
     * Cuenta palabras y devuelve también el tiempo de procesamiento del SERVIDOR.
     * Esto permite medir solo el tiempo de procesamiento, excluyendo el tiempo
     * de conexión y transferencia de red.
     * @param texto String con el contenido completo del texto
     * @return Array de 2 elementos: [0]=palabras contadas, [1]=tiempo en milisegundos
     * @throws RemoteException Si hay error de comunicación con el servidor
     */
    long[] contarPalabrasConTiempo(String texto) throws RemoteException;
    
    /**
     * Versión con compresión que devuelve tiempo de procesamiento del servidor.
     * El servidor descomprime, procesa y mide solo ese tiempo (no la transferencia).
     * @param textoComprimido Bytes del texto comprimido con GZIP
     * @return Array de 2 elementos: [0]=palabras contadas, [1]=tiempo en milisegundos
     * @throws RemoteException Si hay error de comunicación con el servidor
     */
    long[] contarPalabrasComprimidoConTiempo(byte[] textoComprimido) throws RemoteException;
}
