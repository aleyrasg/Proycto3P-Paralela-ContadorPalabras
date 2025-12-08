public class ResultadoProcesamiento {
    private final String servidor;
    private final int palabras;
    private final long tiempoMs;
    private final boolean exitoso;
    private final String error;

    public ResultadoProcesamiento(String servidor, int palabras, long tiempoMs) {
        this.servidor = servidor;
        this.palabras = palabras;
        this.tiempoMs = tiempoMs;
        this.exitoso = true;
        this.error = null;
    }

    public ResultadoProcesamiento(String servidor, String error) {
        this.servidor = servidor;
        this.palabras = 0;
        this.tiempoMs = 0;
        this.exitoso = false;
        this.error = error;
    }

    public String getServidor() { return servidor; }
    public int getPalabras() { return palabras; }
    public long getTiempoMs() { return tiempoMs; }
    public boolean isExitoso() { return exitoso; }
    public String getError() { return error; }
}
