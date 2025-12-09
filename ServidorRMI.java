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
            // Método 1: Obtener IP local
            String ip = InetAddress.getLocalHost().getHostAddress();
            
            // Si es localhost, intentar obtener la IP real
            if (ip.equals("127.0.0.1") || ip.startsWith("127.")) {
                // Método 2: Buscar interfaces de red
                java.util.Enumeration<java.net.NetworkInterface> interfaces = 
                    java.net.NetworkInterface.getNetworkInterfaces();
                
                while (interfaces.hasMoreElements()) {
                    java.net.NetworkInterface iface = interfaces.nextElement();
                    if (iface.isLoopback() || !iface.isUp()) continue;
                    
                    java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        java.net.InetAddress addr = addresses.nextElement();
                        // Buscar IPv4 que no sea localhost
                        if (addr instanceof java.net.Inet4Address && 
                            !addr.isLoopbackAddress()) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
            
            return ip;
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo obtener IP, usando localhost");
            return "localhost";
        }
    }
}
