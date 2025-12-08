import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorRMI {
    public static void main(String[] args) {
        try {
            int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
            IContadorRemoto contador = new ContadorRemotoImpl();
            Registry registry = LocateRegistry.createRegistry(puerto);
            registry.rebind("ContadorRemoto", contador);
            System.out.println("Servidor RMI listo en puerto " + puerto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
