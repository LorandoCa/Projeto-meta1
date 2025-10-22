/*import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class client
    Registry registry;
    Crawler_gateway stub;

    public void indexNewURL(String url){
        stub.addURL(url);
    }

    public List<String> pesquisa(String words){//or URL if client wants to execute the 5th functionality
        //É preciso o Barrel

    } 

    public String statistic(){





    }
    public static void main(String[] args) {
        client cliente= new client();
        try {
                cliente.registry = LocateRegistry.getRegistry("localhost", 1099);
                cliente.stub = (Crawler_gateway) cliente.registry.lookup("Gateway");
                // stub.<metodo>() para chamar um metodo
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
} */
