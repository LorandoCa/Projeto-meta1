import java.util.List;
import java.util.Set;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BarrelInterface extends Remote { 

    public void addWordToStructure(String word, String url) throws RemoteException;
    public List<String> returnSearchResult(List<String> queryWords) throws RemoteException;

}
