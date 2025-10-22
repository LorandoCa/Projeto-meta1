import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;


public class GatewayImp extends UnicastRemoteObject implements Gateway_interface{

    Queue<String> URL_queue= new LinkedList<>();
    Set <String> visited= new HashSet<>();
    List<Client_interface> clients= new ArrayList<>();//do callback to all the stored references

    // <nome da implementacao da interface de barrel> stub_barrel;


    Map<String, Integer> searchFreq= new HashMap<>();

    public GatewayImp() throws RemoteException {super();
        //Inicializar registry e stub de barrel apos criar o "servidor" Barrel
    }

    @Override
    public synchronized String getURL(){
        if(URL_queue.isEmpty()) return null;
        visited.add(URL_queue.peek());
        return URL_queue.poll();
    }

    @Override
    public synchronized Void addURLs(List<String> new_URLs) {

        for (int i=0; i< new_URLs.size(); i++){
            this.addURL(new_URLs.get(i));
            
        }
        return null;
    }

    @Override
    public synchronized Void addURL(String new_URL) {
        if(URL_queue.contains(new_URL) || visited.contains(new_URL)) return null;

        URL_queue.add(new_URL);
        return null;
    }

    @Override
    public List<String> pesquisa_word(String word){
        
        //stub_barrel.search(word);



        searchFreq.put(word, searchFreq.getOrDefault(word, 0) + 1);

        
        //Atualizacao apos alteracao
        //Ordenar pelo valor (de menor para maior) ChatGPT
        searchFreq = searchFreq.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // .reversed() para maior→menor
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,  // merge function
                LinkedHashMap::new // mantém a ordem do stream
            ));
        
            this.collback();
        
            return null; //vai retornar a lista de palavras
    }


    @Override
    public void collback() {
        List<String> listaPesq = new ArrayList<>(searchFreq.keySet());
  
        for(int i = 0; i < this.clients.size(); ++i) {
           try {
              ((Client_interface)this.clients.get(i)).updateStatistics(listaPesq.subList(0, 10));
           }
            catch (NoSuchObjectException a){ //unsubscribe
                clients.remove(i);
                
            }catch (Exception e) {
              e.printStackTrace();
           }
        }
  
    }




    @Override
    public String statistics(){
        List<String> chaves = new ArrayList<>(searchFreq.keySet());
        
        List<String> pesquisasComuns= new ArrayList<>();
        for(int i=0; i<chaves.size();i++){
            pesquisasComuns.add(chaves.get(i));
        }
    


        return null;
    }
    
    @Override
    public List<String> pesquisa_URL(String url){
        //stub do barrel
        return null;
    }

    @Override
    public void subscribe(Client_interface c){
        clients.add(c); 
    }













    public static void main(String[] args) {

        try {

            LocateRegistry.createRegistry(1099); // cria o registry na porta 1099
            System.out.println("RMI registry iniciado na porta 1099");

            
            GatewayImp obj = new GatewayImp();
            Naming.rebind("Gateway", obj);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
