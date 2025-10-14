//import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

//Mede o tempo medio de pesquisa -> IMPLEMENTAR

public class Gateway {
    public static void main(String[] args) {

        try {
            Crawler_gateway_interface obj = new Crawler_gateway();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("Gateway", obj);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        

    }
}
