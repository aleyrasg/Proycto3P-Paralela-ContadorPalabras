/**
 * ResultadoProcesamiento - Modelo de datos para resultados de conteo de palabras
 * 
 * Esta clase encapsula el resultado de una operación de conteo de palabras,
 * ya sea exitosa o con error. Se usa uniformemente en los tres modos:
 * Secuencial, Concurrente y Paralelo (RMI).
 * 
 * ATRIBUTOS:
 * - servidor: Identificador del procesador (ej: "Secuencial", "Servidor 1")
 * - palabras: Cantidad de palabras contadas
 * - tiempoMs: Tiempo de procesamiento en milisegundos
 * - exitoso: Indica si el procesamiento fue exitoso
 * - error: Mensaje de error si no fue exitoso
 * 
 * USO:
 * - Los procesadores (Secuencial, Concurrente, RMI) retornan esta clase
 * - La GUI usa esta clase para mostrar resultados en la tabla
 * - Permite manejo uniforme de éxitos y errores
 * 
 * CONSTRUCTORES:
 * - (servidor, palabras, tiempoMs): Para resultados exitosos
 * - (servidor, error): Para resultados con error
 * 
 * @author Proyecto Paralela - 3er Parcial
 * @version 1.0
 */
public class ResultadoProcesamiento {
    
    /** Nombre del servidor o modo de procesamiento */
    private final String servidor;
    
    /** Cantidad de palabras contadas (0 si hubo error) */
    private final int palabras;
    
    /** Tiempo de procesamiento en milisegundos (0 si hubo error) */
    private final long tiempoMs;
    
    /** Indica si el procesamiento fue exitoso */
    private final boolean exitoso;
    
    /** Mensaje de error (null si fue exitoso) */
    private final String error;

    /**
     * Constructor para resultado exitoso.
     * 
     * @param servidor Nombre del servidor o modo (ej: "Secuencial", "Servidor 1")
     * @param palabras Cantidad de palabras contadas
     * @param tiempoMs Tiempo de procesamiento en milisegundos
     */
    public ResultadoProcesamiento(String servidor, int palabras, long tiempoMs) {
        this.servidor = servidor;
        this.palabras = palabras;
        this.tiempoMs = tiempoMs;
        this.exitoso = true;
        this.error = null;
    }

    /**
     * Constructor para resultado con error.
     * 
     * @param servidor Nombre del servidor o modo que falló
     * @param error Mensaje descriptivo del error
     */
    public ResultadoProcesamiento(String servidor, String error) {
        this.servidor = servidor;
        this.palabras = 0;
        this.tiempoMs = 0;
        this.exitoso = false;
        this.error = error;
    }

    /**
     * @return Nombre del servidor o modo de procesamiento
     */
    public String getServidor() { return servidor; }
    
    /**
     * @return Cantidad de palabras contadas
     */
    public int getPalabras() { return palabras; }
    
    /**
     * @return Tiempo de procesamiento en milisegundos
     */
    public long getTiempoMs() { return tiempoMs; }
    
    /**
     * @return true si el procesamiento fue exitoso, false si hubo error
     */
    public boolean isExitoso() { return exitoso; }
    
    /**
     * @return Mensaje de error o null si fue exitoso
     */
    public String getError() { return error; }
}
