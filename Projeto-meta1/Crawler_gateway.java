import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.rmi.RemoteException;


public class Crawler_gateway extends UnicastRemoteObject implements Crawler_gateway_interface{

    HashMap<String, Boolean> URL_queue= new HashMap<>();

    public String getURL(){
        return URL_queue.getFirst();
    }

    public String addURLs(List<String> new_URLs){
        for (int i=0; i< new_URLs.size(); i++){
            if(URL_queue.containsKey(new_URLs.get(i))) continue;
            
            
        }
    }


}
