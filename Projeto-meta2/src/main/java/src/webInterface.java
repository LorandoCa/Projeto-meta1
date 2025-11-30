package src;


import java.rmi.Remote;
import java.util.List;
import java.util.Map;

public interface webInterface extends Remote {
    public void update(Map<String,List<String>> info);
    
}
