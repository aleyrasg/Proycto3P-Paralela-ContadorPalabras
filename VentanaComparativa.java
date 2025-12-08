import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VentanaComparativa extends JFrame {
    private JTextField txtArchivo;
    private JTextArea txtLog;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private JTable tablaHilos;
    private DefaultTableModel modeloHilos;
    private JLabel lblProblema;
    private JSpinner spinnerHilos;
    private File archivoSeleccionado;
    private List<ConfiguracionServidor> servidores;
    private JProgressBar progressSecuencial, progressConcurrente, progressParalelo;
    private JLabel lblSpeedupConcurrente, lblSpeedupParalelo;
    
    public VentanaComparativa() {
        setTitle("🔬 Comparativa: Secuencial vs Concurrente vs Paralelo (RMI)");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        inicializarServidores();
        crearInterfaz();
    }
    
    private void inicializarServidores() {
        servidores = new ArrayList<>();
        servidores.add(new ConfiguracionServidor("localhost", 1099, "Servidor-1"));
        servidores.add(new ConfiguracionServidor("localhost", 1100, "Servidor-2"));
    }
    
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
        spinnerHilos = new JSpinner(new SpinnerNumberModel(4, 1, 16, 1));
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
        
        // Eventos
        btnArchivo.addActionListener(e -> seleccionarArchivo());
        btnConfigServidores.addActionListener(e -> configurarServidores());
        btnEjecutar.addActionListener(e -> ejecutarComparativa());
        btnLimpiar.addActionListener(e -> limpiar());
    }
    
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
    
    private JProgressBar crearBarraProgreso(String label) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setString(label + " 0%");
        return bar;
    }
    
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
    
    private void seleccionarArchivo() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = chooser.getSelectedFile();
            txtArchivo.setText(archivoSeleccionado.getName());
            log("✅ Archivo seleccionado: " + archivoSeleccionado.getName());
        }
    }
    
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
    
    private ResultadoProcesamiento ejecutarParalelo(String contenido) throws Exception {
        long inicio = System.currentTimeMillis();
        
        List<String> particiones = dividirTrabajoPorBytes(contenido, servidores.size());
        List<CompletableFuture<ResultadoProcesamiento>> tareas = new ArrayList<>();
        
        // Distribuir particiones entre servidores disponibles
        int numServidores = servidores.size();
        int particionesPorServidor = (int) Math.ceil((double) particiones.size() / numServidores);
        
        for (int i = 0; i < numServidores && i * particionesPorServidor < particiones.size(); i++) {
            ConfiguracionServidor servidor = servidores.get(i);
            int inicioParticion = i * particionesPorServidor;
            int finParticion = Math.min(inicioParticion + particionesPorServidor, particiones.size());
            
            // Combinar múltiples particiones para este servidor si es necesario
            StringBuilder textoServidor = new StringBuilder();
            for (int j = inicioParticion; j < finParticion; j++) {
                textoServidor.append(particiones.get(j));
            }
            
            String particion = textoServidor.toString();
            int index = i;
            
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
        
        long tiempo = System.currentTimeMillis() - inicio;
        return new ResultadoProcesamiento("Paralelo (" + servidores.size() + " servidores RMI)", 
                                         totalPalabras, tiempo);
    }
    
    private List<String> dividirTrabajoPorBytes(String contenido, int numParticiones) {
        List<String> particiones = new ArrayList<>();
        int tamañoTotal = contenido.length();
        
        // Limitar tamaño máximo por partición (1MB)
        int MAX_CHUNK = 1024 * 1024;
        int tamañoParticion = Math.min(tamañoTotal / numParticiones, MAX_CHUNK);
        
        // Si el tamaño es muy grande, ajustar número de particiones
        if (tamañoTotal / numParticiones > MAX_CHUNK) {
            numParticiones = (int) Math.ceil((double) tamañoTotal / MAX_CHUNK);
            tamañoParticion = tamañoTotal / numParticiones;
            log("⚠️ Archivo muy grande, ajustando a " + numParticiones + " particiones");
        }
        
        for (int i = 0; i < numParticiones; i++) {
            int inicio = i * tamañoParticion;
            int fin = (i == numParticiones - 1) ? tamañoTotal : (inicio + tamañoParticion);
            particiones.add(contenido.substring(inicio, fin));
        }
        
        return particiones;
    }
    
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
    
    private void actualizarProgreso(JProgressBar bar, int valor, String texto) {
        SwingUtilities.invokeLater(() -> {
            bar.setValue(valor);
            bar.setString(bar.getString().split(":")[0] + ": " + texto);
        });
    }
    
    private void actualizarSpeedup(JLabel label, String modo, double speedup, double eficiencia) {
        SwingUtilities.invokeLater(() -> {
            String color = speedup > 1.5 ? "green" : speedup > 1.0 ? "orange" : "red";
            label.setText(String.format(
                "<html>Speedup %s: <font color='%s'><b>%.2fx</b></font> | Eficiencia: %.1f%%</html>",
                modo, color, speedup, eficiencia * 100
            ));
        });
    }
    
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
    
    private void log(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = String.format("[%tT] ", System.currentTimeMillis());
            txtLog.append(timestamp + mensaje + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }
    
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
