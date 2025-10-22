import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.rmi.RemoteException;

public class StorageBarrelImp extends UnicastRemoteObject implements StorageBarrelInterface{
    // Índice invertido: palavra -> conjunto de URLs
    private Map<String, Set<String>> index;

    private Map<String, Integer> urlPopularity;

    public StorageBarrelImp() throws RemoteException {
        index = new HashMap<>();
        urlPopularity = new HashMap<>();
    }

    @Override
    public void addWordToStructure(String word, String url) {
        index.computeIfAbsent(word,  k -> new HashSet<>()).add(url);

        // Sempre que adicionar uma URL nova para determinada palavra incrementa-se o valor no Map da popularidade
        urlPopularity.put(url, urlPopularity.getOrDefault(url, 0) + 1);
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
}
