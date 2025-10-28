import java.util.List;
import java.util.Set;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StorageBarrelInterface extends Remote { 

    public void addWordToStructure(Set<String> word, String url) throws RemoteException;
    public List<String> returnSearchResult(List<String> queryWords) throws RemoteException;
    public void addLinks(String fromUrl, Set<String> toUrls) throws RemoteException;
    public Set<String> searchUrl(String url) throws RemoteException;
}
