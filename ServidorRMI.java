import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public class ServidorRMI {
    public static void main(String[] args) {
        try {
            int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
            
            // Obtener IP local (intentar varias formas)
            String hostIP = obtenerIPLocal();
            
            // IMPORTANTE: Configurar hostname ANTES de crear objetos remotos
            System.setProperty("java.rmi.server.hostname", hostIP);
            System.setProperty("java.rmi.server.useLocalHostname", "false");
            
            // Ahora crear el registro y el objeto remoto
            Registry registry = LocateRegistry.createRegistry(puerto);
            IContadorRemoto contador = new ContadorRemotoImpl();
            registry.rebind("ContadorRemoto", contador);
            
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
    
    private static String obtenerIPLocal() {
        try {
            // Buscar la mejor IP disponible (priorizar redes 192.168.x.x)
            java.util.Enumeration<java.net.NetworkInterface> interfaces = 
                java.net.NetworkInterface.getNetworkInterfaces();
            
            String fallbackIP = null;
            
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                
                // Ignorar interfaces inactivas o loopback
                if (iface.isLoopback() || !iface.isUp()) continue;
                
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
