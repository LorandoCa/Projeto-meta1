import java.util.List;
import java.util.ArrayList;

public class Gateway implements Runnable {

    List<String> URL_queue= new ArrayList<>();

    Gateway(){
        new Thread(this,"Gateway").start();
    }

    private boolean addURL(String URL){
        try {
            URL_queue.add((URL));
            return true;
        
        } catch (Exception e) {
            System.out.println("The URL couldn't be added in the list");
            return false;
        }
    }

    public void run() {
        
    }
}
