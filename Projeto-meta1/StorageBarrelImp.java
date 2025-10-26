import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;


public class StorageBarrelImp extends UnicastRemoteObject implements StorageBarrelInterface{
    // Índice invertido: palavra -> conjunto de URLs
    private Map<String, Set<String>> index;

    private Map<String, Set<String>> linkPages;

    private Map<String, Integer> urlPopularity;

    public StorageBarrelImp() throws RemoteException {
        index = new HashMap<>();
        linkPages = new HashMap<>();
        urlPopularity = new HashMap<>();

    }

    @Override
    public synchronized void addWordToStructure(Set<String> word, String url) {
        System.out.println(word);
        for (String words : word){

            index.computeIfAbsent(words,  k -> new HashSet<>()).add(url);
        }
        urlPopularity.putIfAbsent(url, 0); // garante que a URL existe no mapa
    }

    @Override
    public synchronized void addLinks(String fromUrl, Set<String> toUrls){
        linkPages.put(fromUrl, toUrls);
        for (String to : toUrls) {
            urlPopularity.put(to, urlPopularity.getOrDefault(to, 0) + 1);
        }
    }

    @Override
    public List<String> returnSearchResult(List<String> words) throws RemoteException {
       if (words == null || words.isEmpty()) {
            return new ArrayList<>();
       }

       Set<String> resultURLs = new HashSet<>(index.getOrDefault(words.get(0), new HashSet<>()));

       // Faz interseção com URLs das outras palavras
        for (int i = 1; i < words.size(); i++) {
            resultURLs.retainAll(index.getOrDefault(words.get(i), new HashSet<>()));
        }

        // Ordena pelos links recebidos (popularidade)
        List<String> sortedURLs = new ArrayList<>(resultURLs);
        sortedURLs.sort((a, b) -> {
            int popA = urlPopularity.getOrDefault(a, 0);
            int popB = urlPopularity.getOrDefault(b, 0);
            return popB - popA; // ordem decrescente
        });
        
        return sortedURLs;
    }

    // --- Teste simples ---

    public static void main(String[] args) {

        try{
            //LocateRegistry.createRegistry(1099); // cria o registry na porta 1099
            System.out.println("RMI registry iniciado na porta 1099");
            
            StorageBarrelImp barrel = new StorageBarrelImp();
            Naming.rebind("Barrel2", barrel);

            
        }
        catch (Exception e) {
            System.out.println("null");
            e.printStackTrace();
        }
    }
}

