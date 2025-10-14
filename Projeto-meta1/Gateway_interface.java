import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Gateway_interface extends Remote {

    public String getURL() throws RemoteException;
    
    public Void addURLs(List<String> new_URLs) throws RemoteException;
    
    public Void addURL(String new_URL) throws RemoteException;
    
    public List<String> pesquisa_word(String word) throws RemoteException;
}
