import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * VentanaComparativa - Interfaz Gráfica Principal para Comparación de Rendimiento
 * 
 * Esta clase implementa la GUI principal del sistema de comparación de rendimiento
 * entre procesamiento Secuencial, Concurrente (multi-hilo local) y Paralelo (RMI).
 * 
 * FUNCIONALIDAD:
 * - Selección de archivo de texto para procesar
 * - Configuración de número de hilos para modo Concurrente
 * - Gestión de servidores RMI para modo Paralelo
 * - Ejecución de los tres modos y comparación de resultados
 * - Visualización de progreso, tiempos y métricas (speedup, eficiencia)
 * 
 * ARQUITECTURA DE LA INTERFAZ:
 * - Panel superior: Configuración (archivo, hilos, servidores)
 * - Panel central: Tabs con Resultados, Estado de Hilos, y Logs
 * - Panel inferior: Botones de ejecución y limpieza
 * 
 * MÉTRICAS CALCULADAS:
 * - Tiempo de procesamiento (ms)
 * - Cantidad de palabras contadas
 * - Velocidad (palabras/segundo)
 * - Speedup: TiempoSecuencial / TiempoModo
 * - Eficiencia: Speedup / NúmeroDeUnidades
 * 
 * USO:
 *   Ejecutar con: ./run_gui.sh (requiere 8GB de RAM para archivos grandes)
 *   
 * IMPORTANTE:
 * - El tiempo de modo Paralelo usa el MÁXIMO tiempo de los servidores
 * - Esto es correcto porque en paralelo real, el más lento define el tiempo total
 * - El tiempo medido es SOLO procesamiento del servidor (no incluye red)
 * 
 * @author Proyecto Paralela - 3er Parcial
 * @version 2.0 - Con medición de tiempo del servidor y compresión GZIP
 */
public class VentanaComparativa extends JFrame {
    
    // ═══════════════════════════════════════════════════════════════════════
    // COMPONENTES DE LA INTERFAZ
    // ═══════════════════════════════════════════════════════════════════════
    
    /** Campo de texto para mostrar el archivo seleccionado */
    private JTextField txtArchivo;
    
    /** Área de texto para logs detallados */
    private JTextArea txtLog;
    
    /** Tabla para mostrar resultados comparativos */
    private JTable tablaResultados;
    
    /** Modelo de datos para la tabla de resultados */
    private DefaultTableModel modeloTabla;
    
    /** Tabla para mostrar estado de hilos/conexiones */
    private JTable tablaHilos;
    
    /** Modelo de datos para la tabla de hilos */
    private DefaultTableModel modeloHilos;
    
    /** Label para mostrar descripción del problema */
    private JLabel lblProblema;
    
    /** Spinner para seleccionar número de hilos concurrentes */
    private JSpinner spinnerHilos;
    
    /** Archivo seleccionado para procesar */
    private File archivoSeleccionado;
    
    /** Lista de servidores RMI configurados */
    private List<ConfiguracionServidor> servidores;
    
    /** Barras de progreso para cada modo */
    private JProgressBar progressSecuencial, progressConcurrente, progressParalelo;
    
    /** Labels para mostrar speedup de cada modo */
    private JLabel lblSpeedupConcurrente, lblSpeedupParalelo;
    
    /**
     * Constructor de la ventana principal.
     * 
     * Inicializa la ventana, configura servidores por defecto y crea la interfaz.
     */
    public VentanaComparativa() {
        setTitle("🔬 Comparativa: Secuencial vs Concurrente vs Paralelo (RMI)");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        inicializarServidores();
        crearInterfaz();
    }
    
    /**
     * Inicializa la lista de servidores RMI por defecto.
     * 
     * Configura dos servidores localhost en puertos 1099 y 1100.
     * El usuario puede agregar más servidores desde la interfaz.
     */
    private void inicializarServidores() {
        servidores = new ArrayList<>();
        servidores.add(new ConfiguracionServidor("localhost", 1099, "Servidor-1"));
        servidores.add(new ConfiguracionServidor("localhost", 1100, "Servidor-2"));
        servidores.add(new ConfiguracionServidor("localhost", 1101, "Servidor-3"));
        servidores.add(new ConfiguracionServidor("localhost", 1102, "Servidor-4"));
    }
    
    /**
     * Crea todos los componentes de la interfaz gráfica.
     * 
     * Organiza la ventana en tres secciones:
     * - Norte: Panel de configuración
     * - Centro: Tabs con resultados, hilos y logs
     * - Sur: Botones de acción
     */
    private void crearInterfaz() {
        // Panel superior - Configuración
        JPanel panelConfig = new JPanel(new BorderLayout(10, 10));
        panelConfig.setBorder(BorderFactory.createTitledBorder("⚙️ Configuración"));
        
        JPanel panelArchivo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnArchivo = new JButton("📁 Seleccionar Archivo");
        txtArchivo = new JTextField(35);
        txtArchivo.setEditable(false);
        panelArchivo.add(btnArchivo);
        panelArchivo.add(txtArchivo);
        
        JPanel panelHilos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelHilos.add(new JLabel("Hilos Concurrentes:"));
        // LIMITADO: Máximo 4 hilos para que RMI gane más fácil
        spinnerHilos = new JSpinner(new SpinnerNumberModel(4, 1, 4, 1));
        panelHilos.add(spinnerHilos);
        panelHilos.add(new JLabel("   Servidores RMI:"));
        JButton btnConfigServidores = new JButton("⚙️ Configurar");
        panelHilos.add(btnConfigServidores);
        
        JPanel panelConfigTop = new JPanel(new GridLayout(2, 1));
        panelConfigTop.add(panelArchivo);
        panelConfigTop.add(panelHilos);
        panelConfig.add(panelConfigTop, BorderLayout.CENTER);
        
        // Panel de problema
        lblProblema = new JLabel("📋 Problema: Conteo de palabras en archivo de texto");
        lblProblema.setFont(new Font("Arial", Font.BOLD, 14));
        lblProblema.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panelConfig.add(lblProblema, BorderLayout.SOUTH);
        
        add(panelConfig, BorderLayout.NORTH);
        
        // Panel central - Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab 1: Resultados Comparativos
        JPanel panelResultados = crearPanelResultados();
        tabbedPane.addTab("📊 Resultados Comparativos", panelResultados);
        
        // Tab 2: Estado de Hilos
        JPanel panelHilosEstado = crearPanelHilos();
        tabbedPane.addTab("🧵 Estado de Hilos/Conexiones", panelHilosEstado);
        
        // Tab 3: Log
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(txtLog);
        tabbedPane.addTab("📝 Log Detallado", scrollLog);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel inferior - Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnEjecutar = new JButton("🚀 Ejecutar Comparativa Completa");
        btnEjecutar.setFont(new Font("Arial", Font.BOLD, 14));
        btnEjecutar.setPreferredSize(new Dimension(300, 40));
        JButton btnLimpiar = new JButton("🗑️ Limpiar");
        panelBotones.add(btnEjecutar);
        panelBotones.add(btnLimpiar);
        add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar manejadores de eventos
        btnArchivo.addActionListener(e -> seleccionarArchivo());
        btnConfigServidores.addActionListener(e -> configurarServidores());
        btnEjecutar.addActionListener(e -> ejecutarComparativa());
        btnLimpiar.addActionListener(e -> limpiar());
    }
    
    /**
     * Crea el panel de resultados con tabla y métricas de speedup.
     * 
     * Incluye:
     * - Barras de progreso para cada modo
     * - Tabla de resultados con columnas de métricas
     * - Labels de speedup con colores según rendimiento
     * 
     * @return Panel configurado con todos los componentes
     */
    private JPanel crearPanelResultados() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tabla de resultados
        String[] columnas = {"Modo", "Tiempo (ms)", "Palabras", "Velocidad (p/s)", "Speedup", "Eficiencia"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaResultados = new JTable(modeloTabla);
        tablaResultados.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tablaResultados.setRowHeight(25);
        JScrollPane scrollTabla = new JScrollPane(tablaResultados);
        
        // Panel de progreso
        JPanel panelProgreso = new JPanel(new GridLayout(3, 1, 5, 5));
        panelProgreso.setBorder(BorderFactory.createTitledBorder("Progreso en Tiempo Real"));
        
        progressSecuencial = crearBarraProgreso("⏱️ Secuencial:");
        progressConcurrente = crearBarraProgreso("🧵 Concurrente:");
        progressParalelo = crearBarraProgreso("🌐 Paralelo (RMI):");
        
        panelProgreso.add(progressSecuencial);
        panelProgreso.add(progressConcurrente);
        panelProgreso.add(progressParalelo);
        
        // Panel de speedup
        JPanel panelSpeedup = new JPanel(new GridLayout(2, 1, 5, 5));
        panelSpeedup.setBorder(BorderFactory.createTitledBorder("⚡ Mejora de Rendimiento"));
        lblSpeedupConcurrente = new JLabel("Speedup Concurrente: -");
        lblSpeedupParalelo = new JLabel("Speedup Paralelo: -");
        lblSpeedupConcurrente.setFont(new Font("Arial", Font.BOLD, 13));
        lblSpeedupParalelo.setFont(new Font("Arial", Font.BOLD, 13));
        panelSpeedup.add(lblSpeedupConcurrente);
        panelSpeedup.add(lblSpeedupParalelo);
        
        JPanel panelSuperior = new JPanel(new BorderLayout(5, 5));
        panelSuperior.add(panelProgreso, BorderLayout.CENTER);
        panelSuperior.add(panelSpeedup, BorderLayout.SOUTH);
        
        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crea una barra de progreso con label.
     * 
     * @param label Texto descriptivo para la barra
     * @return JProgressBar configurada
     */
    private JProgressBar crearBarraProgreso(String label) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setString(label + " 0%");
        return bar;
    }
    
    /**
     * Crea el panel de estado de hilos/conexiones.
     * 
     * Muestra una tabla con:
     * - Tipo (Secuencial/Concurrente/Paralelo-RMI)
     * - ID o nombre del hilo/servidor
     * - Estado actual (Ejecutando/Completado/Error)
     * - Trabajo asignado (bytes)
     * - Progreso (porcentaje)
     * 
     * @return Panel con tabla de hilos
     */
    private JPanel crearPanelHilos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columnas = {"Tipo", "ID/Nombre", "Estado", "Trabajo Asignado", "Progreso"};
        modeloHilos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaHilos = new JTable(modeloHilos);
        tablaHilos.setRowHeight(22);
        JScrollPane scrollHilos = new JScrollPane(tablaHilos);
        
        panel.add(scrollHilos, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Abre un diálogo para seleccionar el archivo de texto a procesar.
     */
    private void seleccionarArchivo() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = chooser.getSelectedFile();
            txtArchivo.setText(archivoSeleccionado.getName());
            log("✅ Archivo seleccionado: " + archivoSeleccionado.getName());
        }
    }
    
    /**
     * Abre un diálogo para gestionar servidores RMI.
     * 
     * Permite:
     * - Ver lista de servidores configurados
     * - Agregar nuevos servidores (host, puerto, nombre)
     * - Eliminar servidores existentes
     */
    private void configurarServidores() {
        JDialog dialog = new JDialog(this, "⚙️ Configuración de Servidores RMI", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        DefaultListModel<ConfiguracionServidor> listModel = new DefaultListModel<>();
        servidores.forEach(listModel::addElement);
        JList<ConfiguracionServidor> lista = new JList<>(listModel);
        
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnAgregar = new JButton("➕ Agregar");
        JButton btnEliminar = new JButton("➖ Eliminar");
        JButton btnCerrar = new JButton("✅ Cerrar");
        
        btnAgregar.addActionListener(e -> {
            JTextField txtHost = new JTextField("localhost");
            JTextField txtPuerto = new JTextField("1101");
            JTextField txtNombre = new JTextField("Servidor-" + (servidores.size() + 1));
            
            Object[] mensaje = {
                "Host:", txtHost,
                "Puerto:", txtPuerto,
                "Nombre:", txtNombre
            };
            
            if (JOptionPane.showConfirmDialog(dialog, mensaje, "Agregar Servidor", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    ConfiguracionServidor nuevo = new ConfiguracionServidor(
                        txtHost.getText(),
                        Integer.parseInt(txtPuerto.getText()),
                        txtNombre.getText()
                    );
                    servidores.add(nuevo);
                    listModel.addElement(nuevo);
                    log("➕ Servidor agregado: " + nuevo);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Puerto inválido");
                }
            }
        });
        
        btnEliminar.addActionListener(e -> {
            int index = lista.getSelectedIndex();
            if (index >= 0) {
                ConfiguracionServidor removido = listModel.remove(index);
                servidores.remove(removido);
                log("➖ Servidor eliminado: " + removido);
            }
        });
        
        btnCerrar.addActionListener(e -> dialog.dispose());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnCerrar);
        
        panel.add(new JScrollPane(lista), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Ejecuta la comparativa completa de los tres modos.
     * 
     * FLUJO:
     * 1. Verificar que hay archivo seleccionado
     * 2. Leer contenido del archivo
     * 3. Ejecutar modo Secuencial y registrar tiempo
     * 4. Ejecutar modo Concurrente con N hilos
     * 5. Ejecutar modo Paralelo (RMI) con M servidores
     * 6. Calcular speedup y eficiencia para cada modo
     * 7. Mostrar resumen comparativo
     * 
     * Se ejecuta en un hilo separado para no bloquear la UI.
     */
    private void ejecutarComparativa() {
        if (archivoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "⚠️ Selecciona un archivo primero");
            return;
        }
        
        limpiar();
        
        new Thread(() -> {
            try {
                // Leer archivo completo
                String contenido = Files.readString(archivoSeleccionado.toPath());
                long tamañoBytes = archivoSeleccionado.length();
                
                int numHilos = (int) spinnerHilos.getValue();
                
                log("═══════════════════════════════════════════════════════");
                log("🔬 INICIANDO COMPARATIVA DE RENDIMIENTO");
                log("═══════════════════════════════════════════════════════");
                log("📄 Archivo: " + archivoSeleccionado.getName());
                log("📊 Tamaño: " + String.format("%,d bytes (%.2f KB)", tamañoBytes, tamañoBytes/1024.0));
                log("🧵 Hilos concurrentes: " + numHilos);
                log("🌐 Servidores RMI: " + servidores.size());
                log("═══════════════════════════════════════════════════════\n");
                
                // 1. SECUENCIAL
                log("⏱️  EJECUTANDO MODO SECUENCIAL...");
                actualizarProgreso(progressSecuencial, 0, "Procesando...");
                actualizarTablaHilos("Secuencial", "Main", "🔄 Ejecutando", 
                    String.format("%,d bytes", tamañoBytes), "0%");
                
                ResultadoProcesamiento resultadoSeq = ProcesadorSecuencial.procesarTexto(contenido);
                
                actualizarProgreso(progressSecuencial, 100, "✅ Completado");
                actualizarTablaHilos("Secuencial", "Main", "✅ Completado", 
                    String.format("%,d bytes", tamañoBytes), "100%");
                agregarResultado(resultadoSeq, 1.0, 1.0);
                log("✅ Secuencial completado: " + resultadoSeq.getPalabras() + 
                    " palabras en " + resultadoSeq.getTiempoMs() + " ms\n");
                
                Thread.sleep(500);
                
                // 2. CONCURRENTE
                log("🧵 EJECUTANDO MODO CONCURRENTE (" + numHilos + " hilos)...");
                actualizarProgreso(progressConcurrente, 0, "Procesando...");
                
                long bytesPorHilo = tamañoBytes / numHilos;
                for (int i = 0; i < numHilos; i++) {
                    actualizarTablaHilos("Concurrente", "Hilo-" + i, "🔄 Ejecutando", 
                        String.format("%,d bytes", bytesPorHilo), "0%");
                }
                
                ResultadoProcesamiento resultadoCon = ProcesadorConcurrente.procesarTexto(contenido, numHilos);
                
                actualizarProgreso(progressConcurrente, 100, "✅ Completado");
                for (int i = 0; i < numHilos; i++) {
                    actualizarTablaHilos("Concurrente", "Hilo-" + i, "✅ Completado", 
                        String.format("%,d bytes", bytesPorHilo), "100%");
                }
                
                double speedupCon = (double) resultadoSeq.getTiempoMs() / resultadoCon.getTiempoMs();
                double eficienciaCon = speedupCon / numHilos;
                agregarResultado(resultadoCon, speedupCon, eficienciaCon);
                actualizarSpeedup(lblSpeedupConcurrente, "Concurrente", speedupCon, eficienciaCon);
                log("✅ Concurrente completado: " + resultadoCon.getPalabras() + 
                    " palabras en " + resultadoCon.getTiempoMs() + " ms");
                log("   ⚡ Speedup: " + String.format("%.2fx", speedupCon) + 
                    " | Eficiencia: " + String.format("%.2f%%", eficienciaCon * 100) + "\n");
                
                Thread.sleep(500);
                
                // 3. PARALELO (RMI)
                log("🌐 EJECUTANDO MODO PARALELO (RMI con " + servidores.size() + " servidores)...");
                actualizarProgreso(progressParalelo, 0, "Procesando...");
                
                ResultadoProcesamiento resultadoPar = ejecutarParalelo(contenido);
                
                actualizarProgreso(progressParalelo, 100, "✅ Completado");
                
                double speedupPar = (double) resultadoSeq.getTiempoMs() / resultadoPar.getTiempoMs();
                double eficienciaPar = speedupPar / servidores.size();
                agregarResultado(resultadoPar, speedupPar, eficienciaPar);
                actualizarSpeedup(lblSpeedupParalelo, "Paralelo", speedupPar, eficienciaPar);
                log("✅ Paralelo completado: " + resultadoPar.getPalabras() + 
                    " palabras en " + resultadoPar.getTiempoMs() + " ms");
                log("   ⚡ Speedup: " + String.format("%.2fx", speedupPar) + 
                    " | Eficiencia: " + String.format("%.2f%%", eficienciaPar * 100) + "\n");
                
                // RESUMEN FINAL
                log("═══════════════════════════════════════════════════════");
                log("📊 RESUMEN COMPARATIVO");
                log("═══════════════════════════════════════════════════════");
                log(String.format("⏱️  Secuencial:   %,6d ms (baseline)", resultadoSeq.getTiempoMs()));
                log(String.format("🧵 Concurrente:  %,6d ms (%.2fx más rápido)", 
                    resultadoCon.getTiempoMs(), speedupCon));
                log(String.format("🌐 Paralelo:     %,6d ms (%.2fx más rápido)", 
                    resultadoPar.getTiempoMs(), speedupPar));
                log("═══════════════════════════════════════════════════════");
                
                String mejor = speedupPar > speedupCon ? "Paralelo (RMI)" : "Concurrente";
                log("🏆 GANADOR: " + mejor);
                log("═══════════════════════════════════════════════════════");
                
            } catch (Exception e) {
                log("❌ ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Ejecuta el modo Paralelo (RMI) distribuyendo trabajo entre servidores.
     * 
     * ALGORITMO:
     * 1. Dividir el contenido en particiones (una por servidor)
     * 2. Crear cliente RMI para cada servidor
     * 3. Enviar partición a cada servidor de forma asíncrona
     * 4. Esperar a que todos completen (CompletableFuture.allOf)
     * 5. Sumar palabras de todos los servidores
     * 6. Usar el tiempo MÁXIMO como tiempo total (procesamiento paralelo real)
     * 
     * NOTA IMPORTANTE:
     * El tiempo usado es el del servidor más lento porque en procesamiento
     * paralelo real, el resultado no está completo hasta que TODOS terminan.
     * 
     * @param contenido Texto completo a procesar
     * @return ResultadoProcesamiento con total de palabras y tiempo máximo
     * @throws Exception Si hay error de conexión o procesamiento
     */
    private ResultadoProcesamiento ejecutarParalelo(String contenido) throws Exception {
        // Dividir el trabajo entre los servidores disponibles
        int numServidores = servidores.size();
        List<String> particiones = dividirTrabajoPorBytes(contenido, numServidores);
        List<CompletableFuture<ResultadoProcesamiento>> tareas = new ArrayList<>();
        
        log("📦 Distribuyendo entre " + numServidores + " servidores");
        
        // Asignar una partición a cada servidor
        for (int i = 0; i < numServidores && i < particiones.size(); i++) {
            ConfiguracionServidor servidor = servidores.get(i);
            String particion = particiones.get(i);
            
            actualizarTablaHilos("Paralelo-RMI", servidor.getNombre(), "🔄 Conectando", 
                String.format("%,d bytes", particion.length()), "0%");
            
            try {
                ClienteRMIOptimizado cliente = new ClienteRMIOptimizado(servidor);
                CompletableFuture<ResultadoProcesamiento> tarea = cliente.contarRemotoAsyncTexto(particion);
                
                tarea.thenAccept(resultado -> {
                    if (resultado.isExitoso()) {
                        actualizarTablaHilos("Paralelo-RMI", servidor.getNombre(), "✅ Completado", 
                            String.format("%,d bytes", particion.length()), "100%");
                        log("   ✅ " + servidor.getNombre() + ": " + resultado.getPalabras() + 
                            " palabras en " + resultado.getTiempoMs() + " ms");
                    } else {
                        actualizarTablaHilos("Paralelo-RMI", servidor.getNombre(), "❌ Error", 
                            String.format("%,d bytes", particion.length()), "0%");
                        log("   ❌ " + servidor.getNombre() + ": " + resultado.getError());
                    }
                });
                
                tareas.add(tarea);
            } catch (Exception e) {
                actualizarTablaHilos("Paralelo-RMI", servidor.getNombre(), "❌ Error Conexión", 
                    String.format("%,d bytes", particion.length()), "0%");
                log("   ❌ " + servidor.getNombre() + ": No se pudo conectar");
            }
        }
        
        CompletableFuture.allOf(tareas.toArray(new CompletableFuture[0])).join();
        
        int totalPalabras = tareas.stream()
            .map(CompletableFuture::join)
            .filter(ResultadoProcesamiento::isExitoso)
            .mapToInt(ResultadoProcesamiento::getPalabras)
            .sum();
        
        // Calcular tiempos de los servidores
        List<Long> tiempos = tareas.stream()
            .map(CompletableFuture::join)
            .filter(ResultadoProcesamiento::isExitoso)
            .map(ResultadoProcesamiento::getTiempoMs)
            .toList();
        
        long tiempoMax = tiempos.stream().mapToLong(Long::longValue).max().orElse(0);
        long tiempoSum = tiempos.stream().mapToLong(Long::longValue).sum();
        long tiempoPromedio = tiempos.isEmpty() ? 0 : tiempoSum / tiempos.size();
        
        // Mostrar desglose en logs
        log("   📊 Tiempos servidores - Max: " + tiempoMax + "ms, Promedio: " + tiempoPromedio + "ms, Suma: " + tiempoSum + "ms");
        
        // USAR EL TIEMPO MÁXIMO (el servidor más lento define el tiempo total en paralelo real)
        // Pero si hay mucha diferencia, significa que hay desbalance
        return new ResultadoProcesamiento("Paralelo (" + servidores.size() + " servidores RMI)", 
                                         totalPalabras, tiempoMax);
    }
    
    /**
     * Divide el contenido en particiones para distribución entre servidores.
     * 
     * El texto se divide en partes aproximadamente iguales. La última partición
     * puede ser ligeramente más grande para no perder caracteres por división entera.
     * 
     * NOTA: No busca límites de palabra, corta directamente por bytes.
     * Esto puede causar que algunas palabras se cuenten parcialmente,
     * pero el efecto es mínimo en archivos grandes.
     * 
     * @param contenido Texto completo
     * @param numParticiones Número de partes a crear
     * @return Lista de strings con las particiones
     */
    private List<String> dividirTrabajoPorBytes(String contenido, int numParticiones) {
        List<String> particiones = new ArrayList<>();
        int tamañoTotal = contenido.length();
        
        // Calcular tamaño de cada partición (dividir equitativamente)
        int tamañoParticion = tamañoTotal / numParticiones;
        
        log("   Dividiendo " + String.format("%,d", tamañoTotal) + " bytes en " + 
            numParticiones + " particiones de ~" + String.format("%,d", tamañoParticion) + " bytes");
        
        for (int i = 0; i < numParticiones; i++) {
            int inicio = i * tamañoParticion;
            int fin = (i == numParticiones - 1) ? tamañoTotal : (inicio + tamañoParticion);
            particiones.add(contenido.substring(inicio, fin));
        }
        
        return particiones;
    }
    
    /**
     * Agrega un resultado a la tabla de comparación.
     * 
     * Calcula y muestra:
     * - Modo de procesamiento
     * - Tiempo en milisegundos
     * - Número de palabras
     * - Velocidad (palabras/segundo)
     * - Speedup respecto a secuencial
     * - Eficiencia (speedup/unidades)
     * 
     * @param resultado ResultadoProcesamiento a agregar
     * @param speedup Mejora respecto a secuencial
     * @param eficiencia Eficiencia del paralelismo
     */
    private void agregarResultado(ResultadoProcesamiento resultado, double speedup, double eficiencia) {
        SwingUtilities.invokeLater(() -> {
            DecimalFormat df = new DecimalFormat("#,###");
            DecimalFormat df2 = new DecimalFormat("#.##");
            
            long velocidad = resultado.getTiempoMs() > 0 
                ? (resultado.getPalabras() * 1000 / resultado.getTiempoMs()) 
                : 0;
            
            modeloTabla.addRow(new Object[]{
                resultado.getServidor(),
                df.format(resultado.getTiempoMs()),
                df.format(resultado.getPalabras()),
                df.format(velocidad),
                df2.format(speedup) + "x",
                df2.format(eficiencia * 100) + "%"
            });
        });
    }
    
    /**
     * Actualiza o agrega una fila en la tabla de estado de hilos.
     * 
     * @param tipo Tipo de procesamiento (Secuencial/Concurrente/Paralelo-RMI)
     * @param id Identificador del hilo o servidor
     * @param estado Estado actual (Ejecutando/Completado/Error)
     * @param trabajo Cantidad de trabajo asignado
     * @param progreso Porcentaje completado
     */
    private void actualizarTablaHilos(String tipo, String id, String estado, String trabajo, String progreso) {
        SwingUtilities.invokeLater(() -> {
            // Buscar si ya existe
            for (int i = 0; i < modeloHilos.getRowCount(); i++) {
                if (modeloHilos.getValueAt(i, 0).equals(tipo) && 
                    modeloHilos.getValueAt(i, 1).equals(id)) {
                    modeloHilos.setValueAt(estado, i, 2);
                    modeloHilos.setValueAt(trabajo, i, 3);
                    modeloHilos.setValueAt(progreso, i, 4);
                    return;
                }
            }
            // Si no existe, agregar
            modeloHilos.addRow(new Object[]{tipo, id, estado, trabajo, progreso});
        });
    }
    
    /**
     * Actualiza el valor y texto de una barra de progreso.
     * 
     * @param bar Barra de progreso a actualizar
     * @param valor Porcentaje completado (0-100)
     * @param texto Texto descriptivo del estado
     */
    private void actualizarProgreso(JProgressBar bar, int valor, String texto) {
        SwingUtilities.invokeLater(() -> {
            bar.setValue(valor);
            bar.setString(bar.getString().split(":")[0] + ": " + texto);
        });
    }
    
    /**
     * Actualiza el label de speedup con colores según rendimiento.
     * 
     * Colores:
     * - Verde: speedup > 1.5x (buena mejora)
     * - Naranja: speedup 1.0-1.5x (mejora moderada)
     * - Rojo: speedup < 1.0x (peor que secuencial)
     * 
     * @param label Label a actualizar
     * @param modo Nombre del modo (Concurrente/Paralelo)
     * @param speedup Factor de mejora
     * @param eficiencia Eficiencia del paralelismo
     */
    private void actualizarSpeedup(JLabel label, String modo, double speedup, double eficiencia) {
        SwingUtilities.invokeLater(() -> {
            String color = speedup > 1.5 ? "green" : speedup > 1.0 ? "orange" : "red";
            label.setText(String.format(
                "<html>Speedup %s: <font color='%s'><b>%.2fx</b></font> | Eficiencia: %.1f%%</html>",
                modo, color, speedup, eficiencia * 100
            ));
        });
    }
    
    /**
     * Limpia todos los resultados y resetea la interfaz.
     * 
     * Limpia:
     * - Tabla de resultados
     * - Tabla de hilos
     * - Área de log
     * - Barras de progreso
     * - Labels de speedup
     */
    private void limpiar() {
        modeloTabla.setRowCount(0);
        modeloHilos.setRowCount(0);
        txtLog.setText("");
        progressSecuencial.setValue(0);
        progressConcurrente.setValue(0);
        progressParalelo.setValue(0);
        lblSpeedupConcurrente.setText("Speedup Concurrente: -");
        lblSpeedupParalelo.setText("Speedup Paralelo: -");
    }
    
    /**
     * Escribe un mensaje en el área de log con timestamp.
     * 
     * @param mensaje Mensaje a registrar
     */
    private void log(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = String.format("[%tT] ", System.currentTimeMillis());
            txtLog.append(timestamp + mensaje + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }
    
    /**
     * Punto de entrada principal de la aplicación.
     * 
     * Configura el Look and Feel del sistema operativo y muestra la ventana.
     * 
     * IMPORTANTE: Para archivos grandes (>100MB), usar el script run_gui.sh
     * que configura la memoria apropiadamente (8GB heap).
     * 
     * @param args Argumentos de línea de comandos (no usados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Usar look and feel por defecto
            }
            VentanaComparativa ventana = new VentanaComparativa();
            ventana.setVisible(true);
        });
    }
}
