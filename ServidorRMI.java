import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

/**
 * ServidorRMI - Punto de entrada para iniciar un servidor de conteo de palabras
 * 
 * Esta clase inicia un servidor RMI que expone el servicio ContadorRemoto
 * para que los clientes puedan enviar texto y recibir el conteo de palabras.
 * 
 * FUNCIONALIDAD:
 * - Detecta automáticamente la IP de red local
 * - Crea el registro RMI en el puerto especificado (default: 1099)
 * - Registra el servicio "ContadorRemoto" para lookup por clientes
 * - Muestra información de conexión para configurar clientes
 * 
 * USO:
 *   java ServidorRMI [puerto]
 *   
 *   Ejemplos:
 *   java ServidorRMI         # Usa puerto 1099 (default)
 *   java ServidorRMI 1100    # Usa puerto 1100
 * 
 * CONFIGURACIÓN DE RED:
 * - Configura java.rmi.server.hostname para respuestas correctas
 * - Prioriza IPs de redes locales (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
 * - Útil cuando la máquina tiene múltiples interfaces de red
 * 
 * PARA PROCESAMIENTO DISTRIBUIDO:
 * - Ejecutar esta clase en cada máquina que participará
 * - Usar puertos diferentes si están en la misma máquina (testing)
 * - Agregar la IP:puerto de cada servidor en la GUI del cliente
 * 
 * @author Proyecto Paralela - 3er Parcial
 * @version 2.0 - Con detección automática de IP de red local
 */
public class ServidorRMI {
    
    /**
     * Punto de entrada principal del servidor.
     * 
     * FLUJO:
     * 1. Obtener puerto de argumentos (o usar 1099)
     * 2. Detectar IP de red local
     * 3. Configurar propiedades RMI para comunicación correcta
     * 4. Crear registro RMI
     * 5. Crear e instanciar ContadorRemotoImpl
     * 6. Registrar servicio como "ContadorRemoto"
     * 7. Mostrar información de conexión
     * 
     * @param args Argumentos: [puerto] (opcional, default 1099)
     */
    public static void main(String[] args) {
        try {
            // Obtener puerto de argumentos o usar default
            int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
            
            // Obtener IP local (intentar varias formas)
            String hostIP = obtenerIPLocal();
            
            // IMPORTANTE: Configurar hostname ANTES de crear objetos remotos
            // Esto asegura que las respuestas RMI usen la IP correcta
            System.setProperty("java.rmi.server.hostname", hostIP);
            System.setProperty("java.rmi.server.useLocalHostname", "false");
            
            // Ahora crear el registro y el objeto remoto
            Registry registry = LocateRegistry.createRegistry(puerto);
            IContadorRemoto contador = new ContadorRemotoImpl();
            registry.rebind("ContadorRemoto", contador);
            
            // Mostrar información de conexión
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("✅ Servidor RMI iniciado correctamente");
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("🌐 IP del servidor: " + hostIP);
            System.out.println("🔌 Puerto: " + puerto);
            System.out.println("📡 Esperando conexiones...");
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("\n💡 Para conectar desde otro dispositivo:");
            System.out.println("   Host: " + hostIP);
            System.out.println("   Puerto: " + puerto);
            System.out.println("═══════════════════════════════════════════════════════\n");
            
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar servidor RMI:");
            e.printStackTrace();
        }
    }
    
    /**
     * Detecta la IP de red local más apropiada.
     * 
     * Recorre todas las interfaces de red buscando una IP que sea:
     * - IPv4 (no IPv6)
     * - No loopback (no 127.0.0.1)
     * - Preferiblemente de red local (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
     * 
     * PRIORIDAD:
     * 1. IPs 192.168.x.x (redes domésticas típicas)
     * 2. IPs 10.x.x.x (redes corporativas)
     * 3. IPs 172.16-31.x.x (redes privadas clase B)
     * 4. Cualquier otra IP no-loopback
     * 5. localhost como último recurso
     * 
     * @return IP detectada o "localhost" si no se encuentra ninguna
     */
    private static String obtenerIPLocal() {
        try {
            // Buscar la mejor IP disponible (priorizar redes 192.168.x.x)
            java.util.Enumeration<java.net.NetworkInterface> interfaces = 
                java.net.NetworkInterface.getNetworkInterfaces();
            
            String fallbackIP = null;
            
            // Iterar sobre todas las interfaces de red
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                
                // Ignorar interfaces inactivas o loopback
                if (iface.isLoopback() || !iface.isUp()) continue;
                
                // Iterar sobre las direcciones de cada interface
                java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    
                    // Solo IPv4 y no loopback
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        
                        // PRIORIDAD 1: Redes locales comunes (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            return ip;
                        }
                        // Verificar rangos 172.16.0.0 - 172.31.255.255
                        if (ip.startsWith("172.")) {
                            int second = Integer.parseInt(ip.split("\\.")[1]);
                            if (second >= 16 && second <= 31) {
                                return ip;
                            }
                        }
                        
                        // Guardar como fallback cualquier IP no-loopback
                        if (fallbackIP == null) {
                            fallbackIP = ip;
                        }
                    }
                }
            }
            
            // Si no encontramos IP prioritaria, usar fallback o localhost
            return fallbackIP != null ? fallbackIP : InetAddress.getLocalHost().getHostAddress();
            
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo obtener IP, usando localhost");
            return "localhost";
        }
    }
}
