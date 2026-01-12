import java.io.Serializable;

/**
 * ConfiguracionServidor - Modelo de datos para configuración de servidor RMI
 * 
 * Esta clase encapsula los datos necesarios para conectar a un servidor RMI.
 * Se utiliza para almacenar y transferir configuraciones de servidores
 * en el sistema distribuido de conteo de palabras.
 * 
 * ATRIBUTOS:
 * - host: IP o hostname del servidor
 * - puerto: Puerto del registro RMI (default 1099)
 * - nombre: Nombre descriptivo para identificar el servidor en la GUI
 * 
 * USO:
 * - La GUI permite agregar servidores usando esta configuración
 * - ClienteRMIOptimizado usa esta clase para conectar
 * - Serializable para poder ser transmitida si fuera necesario
 * 
 * EJEMPLOS:
 *   new ConfiguracionServidor("192.168.1.100", 1099, "Servidor 1")
 *   new ConfiguracionServidor("localhost", 1100, "Local")
 * 
 * @author Proyecto Paralela - 3er Parcial
 * @version 1.0
 */
public class ConfiguracionServidor implements Serializable {
    
    /** Host o IP del servidor RMI */
    private final String host;
    
    /** Puerto del registro RMI */
    private final int puerto;
    
    /** Nombre descriptivo del servidor para mostrar en UI */
    private final String nombre;

    /**
     * Constructor de configuración de servidor.
     * 
     * @param host IP o hostname del servidor (ej: "192.168.1.100" o "localhost")
     * @param puerto Puerto del registro RMI (típicamente 1099)
     * @param nombre Nombre descriptivo para identificar el servidor
     */
    public ConfiguracionServidor(String host, int puerto, String nombre) {
        this.host = host;
        this.puerto = puerto;
        this.nombre = nombre;
    }

    /**
     * @return Host o IP del servidor
     */
    public String getHost() { return host; }
    
    /**
     * @return Puerto del registro RMI
     */
    public int getPuerto() { return puerto; }
    
    /**
     * @return Nombre descriptivo del servidor
     */
    public String getNombre() { return nombre; }

    /**
     * Representación en String para mostrar en listas y logs.
     * Formato: "Nombre (host:puerto)"
     * 
     * @return String con información completa del servidor
     */
    @Override
    public String toString() {
        return nombre + " (" + host + ":" + puerto + ")";
    }
}
