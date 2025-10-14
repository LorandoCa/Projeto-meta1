//import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

//Mede o tempo medio de pesquisa -> IMPLEMENTAR

public class Gateway {
    public static void main(String[] args) {

        try {
            Gateway_interface obj = new GatewayImp();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("Gateway", obj);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        

    }
}
