import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.rmi.RemoteException;


public class GatewayImp extends UnicastRemoteObject implements GatewayInterface{

    Queue<String> URL_queue= new LinkedList<>();
    Set <String> visited= new HashSet<>();

    public GatewayImp() throws RemoteException {super();}

    @Override
    public String getURL(){
        if(URL_queue.isEmpty()) return null;
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

    @Override
    public Void addURL(String new_URL) {
        if(URL_queue.contains(new_URL) || visited.contains(new_URL)) return null;

        URL_queue.add(new_URL);
        return null;
    }

}
