import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.rmi.RemoteException;


public class Crawler_gateway extends UnicastRemoteObject implements Crawler_gateway_interface{

    Queue<String> URL_queue= new LinkedList<>();
    Set <String> visited= new HashSet<>();

    public Crawler_gateway() throws RemoteException {super();}

    @Override
    public String getURL(){
        visited.add(URL_queue.peek());
        return URL_queue.poll();
    }

    @Override
    public Void addURLs(List<String> new_URLs) {

        for (int i=0; i< new_URLs.size(); i++){
            if(URL_queue.contains(new_URLs.get(i)) || visited.contains(new_URLs.get(i))) continue;

            URL_queue.add(new_URLs.get(i));
            
        }
        return null;
    }


}
