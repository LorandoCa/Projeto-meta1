
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Client_interface extends Remote  {
    public void updateStatistics(List<String> topTenUpdate) throws RemoteException;
}
