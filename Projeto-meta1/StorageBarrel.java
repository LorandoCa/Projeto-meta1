import java.util.*;
import java.rmi.RemoteException;

public class StorageBarrel implements BarrelInterface {
    // Índice invertido: palavra -> conjunto de URLs
    private Map<String, Set<String>> index;

    public StorageBarrel() {
        index = new HashMap<>();
    }

    @Override
    public void addWordToStructure(String word, String url) {
        index.computeIfAbsent(word,  k -> new HashSet<>()).add(url);
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

        List<String> finalUrls = new ArrayList<>(resultURLs);
        
        return finalUrls;
    }

    public static void main(String[] args) {
        StorageBarrel barrel = new StorageBarrel();
    }
}
