import java.io.Serializable;

public class ConfiguracionServidor implements Serializable {
    private final String host;
    private final int puerto;
    private final String nombre;

    public ConfiguracionServidor(String host, int puerto, String nombre) {
        this.host = host;
        this.puerto = puerto;
        this.nombre = nombre;
    }

    public String getHost() { return host; }
    public int getPuerto() { return puerto; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return nombre + " (" + host + ":" + puerto + ")";
    }
}
