import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;
import java.rmi.Naming;
import java.rmi.RemoteException;


public class GatewayImp extends UnicastRemoteObject implements Gateway_interface{

    Queue<String> URL_queue= new LinkedList<>();
    Set <String> visited= new HashSet<>();
    List<Client_interface> clients= new ArrayList<>();//do callback to all the stored references

    StorageBarrelInterface stub_barrel1,stub_barrel2;
    Map<StorageBarrelInterface, Integer> barrelCoresp;

    int client_counter=1;
    String client_name= new String();

    Map<String, Integer> searchFreq= new HashMap<>();

    
    public GatewayImp() throws RemoteException {super();
        try {
            stub_barrel1= (StorageBarrelInterface) Naming.lookup("Barrel1");
            stub_barrel2= (StorageBarrelInterface) Naming.lookup("Barrel2");
            barrelCoresp= new HashMap<>(); 
            barrelCoresp.put(stub_barrel1, 0);
            barrelCoresp.put(stub_barrel2, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        List<String> result=null;
        String[] words= word.split(" ");
        List <String> wordss= new ArrayList<>(Arrays.asList(words));

        //Load balancing
        StorageBarrelInterface barrel;
       if(barrelCoresp.get(stub_barrel1)>barrelCoresp.get(stub_barrel2)){
            barrel= stub_barrel2;          
       }else{
            barrel= stub_barrel1;
       }
       //end
       boolean flag= true;
       while(flag){
            try {
                flag= false;
                result= barrel.returnSearchResult(wordss);
                System.out.println(result);

            } catch (java.rmi.ConnectException e) {

                System.out.println("Barrel desconectado. Tentando outro...");
                if(barrel== stub_barrel1) barrel=stub_barrel2;
                else barrel= stub_barrel1;
                flag= true;

            } catch (java.rmi.RemoteException e) {

                System.out.println("Erro remoto ao contactar Barrel.");
                e.printStackTrace();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        barrelCoresp.put(barrel, barrelCoresp.get(barrel)+1);
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
        
        return result; //vai retornar a lista de palavras
    }


    @Override
    public void collback() {
        List<String> listaPesq = new ArrayList<>(searchFreq.keySet());



        Iterator<Client_interface> it = clients.iterator(); //Para q a alteracao da lista nao afete a iteracao sobre ela

        while (it.hasNext()) {
            Client_interface client = it.next();
            try {
                ((Client_interface)client).updateStatistics(new ArrayList<>(listaPesq.subList(0, Math.min(10, listaPesq.size()))));
            } catch (java.rmi.ConnectException e) {
                System.out.println("Cliente desconectado. Removendo da lista...");
                it.remove(); // o cliente caiu
            } catch (java.rmi.RemoteException e) {
                System.out.println("Erro remoto ao contactar cliente. Removendo...");
                e.printStackTrace();
                it.remove();
            } catch (Exception e) {
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
        try {
        
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    @Override
    public String subscribe(Client_interface c){ //altere: retornar nome de cliente
        clients.add(c);
        return String.format("Client%d", client_counter++);    
    }

//End o interface implementation
//=======================================================================================================

    public static void main(String[] args) {
        try {
            GatewayImp server = new GatewayImp();
            Naming.rebind("Gateway", server);
            //java -Djava.rmi.server.hostname=192.168.176.1 MeuServidor: definir um ip para um server
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}


