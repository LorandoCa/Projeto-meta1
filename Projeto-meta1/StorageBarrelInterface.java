import java.util.List;
import java.util.Map;
import java.util.Set;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StorageBarrelInterface extends Remote { 

    public int addWordToStructure(Set<String> word, String url, String Crawler, int ref) throws RemoteException;
    public List<String> returnSearchResult(List<String> queryWords) throws RemoteException;
    public int addLinks(String fromUrl, Set<String> toUrls, String Crawler, int ref) throws RemoteException;
    public Set<String> searchUrl(String url) throws RemoteException;
    public Map<String, Set<String>> reboot() throws RemoteException;

}
