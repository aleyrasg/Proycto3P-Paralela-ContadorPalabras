import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

public class ServidorRMI {
    public static void main(String[] args) {
        try {
            int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
            
            // Obtener IP local
            String hostIP = InetAddress.getLocalHost().getHostAddress();
            
            // Configurar hostname para RMI (IMPORTANTE para conexiones remotas)
            System.setProperty("java.rmi.server.hostname", hostIP);
            
            IContadorRemoto contador = new ContadorRemotoImpl();
            Registry registry = LocateRegistry.createRegistry(puerto);
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
}
