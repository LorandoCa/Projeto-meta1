import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class client implements ClientInterface {

    Registry registry;
    GatewayImp stub;

    public void indexNewURL(String url){
        stub.addURL(url);
    }

    public List<String> pesquisa(String words){//or URL if client wants to execute the 5th functionality
        
        return null;
    } 

    public String statistic(){

        //Deve ser resolvido pela gateway

        return null;
    }

    public static void main(String[] args) {
        client cliente= new client();
        try {
                cliente.registry = LocateRegistry.getRegistry("localhost", 1099);
                cliente.stub = (GatewayImp) cliente.registry.lookup("Gateway");
                // stub.<metodo>() para chamar um metodo
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
} 
