import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;


public class GatewayImp extends UnicastRemoteObject implements Gateway_interface{

    Queue<String> URL_queue= new LinkedList<>();
    Set <String> visited= new HashSet<>();
    List<Client_interface> clients= new ArrayList<>();//do callback to all the stored references

    List<StorageBarrelInterface> barrels= new ArrayList<>();
    List<String> barrelsNames= new ArrayList<>();

    long somaTempoExecucao=0;
    int countPesquisas=0;

    int client_counter=1;
    int barrel_counter=1;
    int prev_barrel=0; 
    String client_name= new String();

    Map<String, Integer> searchFreq= new HashMap<>();

    
    public GatewayImp() throws RemoteException {super();
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
    public List<PageInfo> pesquisa_word(String word){

        long inicio = System.currentTimeMillis();

        List<PageInfo> result=null;
        String[] words= word.split(" ");
        List <String> wordss= new ArrayList<>(Arrays.asList(words));

        StorageBarrelInterface barrel;

        //Load balancing
        prev_barrel= (prev_barrel+1) % barrels.size();
        barrel= barrels.get(prev_barrel);
       //end

        while(true){
            try {
                result= barrel.returnSearchResult(wordss);
                System.out.println(result);

            } catch (java.rmi.ConnectException e) {

                System.out.println("Barrel desconectado. Tentando outro...");
                barrels.remove(prev_barrel);
                barrel= barrels.get(0);// se ha menos um barrel, so um existe
                prev_barrel=0;
                continue;

            } catch (java.rmi.RemoteException e) {
                System.out.println("Erro remoto ao contactar Barrel.");
                e.printStackTrace();
                continue;
            }catch (Exception e) {
                e.printStackTrace();
            }
            break;
        }

        long fim = System.currentTimeMillis();
        somaTempoExecucao+= fim-inicio;
        countPesquisas++;

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
        System.out.println("Informações de callback adicionados");
        return result; //vai retornar a lista de palavras
    }


    @Override
    public void collback() {
        List<String> listaPesq = new ArrayList<>(searchFreq.keySet());



        Iterator<Client_interface> it = clients.iterator(); //Para q a alteracao da lista nao afete a iteracao sobre ela

        while (it.hasNext()) {
            Client_interface client = it.next();
            try {
                ((Client_interface)client).updateStatistics(new ArrayList<>(listaPesq.subList(0, Math.min(10, listaPesq.size()))), 
                                                            getBarrelsNames(), somaTempoExecucao/countPesquisas);
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
             //Load balancing
            prev_barrel= (prev_barrel+1) % barrels.size();
            StorageBarrelInterface barrel= barrels.get(prev_barrel);
            Set<String> res = barrel.searchUrl(url);
            return new ArrayList<>(res);
       //end
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        
    }

    @Override
    public String subscribe(Client_interface c){ //altere: retornar nome de cliente
        clients.add(c);
        return String.format("Client%d", client_counter++);    
    }

    @Override
    public String subscribe(StorageBarrelInterface b){
        barrels.add(b);
        System.out.println("Adicionado com sucesso");
        barrelsNames.add(String.format("Barrel%d", barrel_counter));
        return String.format("Barrel%d", barrel_counter++);
    }

    @Override
    public  Integer getBarrelNum(){
        return barrels.size();
    }

    @Override
    public StorageBarrelInterface getBarrel(){ //fixando o numero de barrels a 2
        Random r= new Random();
        if(barrels.size()>1) return barrels.get(r.nextInt(2));
        if(barrels.size()==0) return null;
        return barrels.get(0);
    }

//End o interface implementation
//=======================================================================================================

    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "192.168.1.163");
            LocateRegistry.createRegistry(1099); // cria o registry na porta 1099
            GatewayImp server = new GatewayImp();
            Naming.rebind("rmi://192.168.1.163:1099/Gateway", server);
            //java -Djava.rmi.server.hostname=192.168.176.1 MeuServidor: definir um ip para um server

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getBarrelsNames() throws RemoteException {
        return barrelsNames;
    }


}


