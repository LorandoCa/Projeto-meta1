import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Crawler_gateway_interface extends Remote {

    public String getURL() throws RemoteException;
    public String addURLs(List<String> new_URLs) throws RemoteException;

}
