import java.util.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StorageBarrel{

    public static void main(String[] args) {
        try{
            StorageBarrelImp barrel = new StorageBarrelImp();
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            registry.rebind("Barrel", barrel);

            
        }
        catch (Exception e) {
            System.out.println("null");
            e.printStackTrace();
        }
    }
}
