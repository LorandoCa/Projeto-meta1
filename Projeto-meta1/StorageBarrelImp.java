import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.rmi.RemoteException;

public class StorageBarrelImp extends UnicastRemoteObject implements StorageBarrelInterface{
    // Índice invertido: palavra -> conjunto de URLs
    private Map<String, Set<String>> index;

    // Map para armazenar todas as referências (Set<String>) de uma determianada URL (String)
    private Map<String, Set<String>> linkPages;

    private Map<String, Integer> urlPopularity;

    public StorageBarrelImp() throws RemoteException {
        index = new HashMap<>();
        linkPages = new HashMap<>();
        urlPopularity = new HashMap<>();
    }

    @Override
    public synchronized void addWordToStructure(Set<String> word, String url) {
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

@Override
public Set<String> searchUrl(String url) throws RemoteException {
    Set<String> links = new HashSet<>();
    
    linkPages.forEach((fromUrl, toUrls) -> {
        if (toUrls.contains(url)) {
            links.add(fromUrl);
        }
    });
    return links;
}

    // --- Teste simples ---
    public static void main(String[] args) throws RemoteException {
    StorageBarrelImp barrel = new StorageBarrelImp();

    // 1️⃣ Indexar palavras das páginas
    barrel.addWordToStructure(Set.of("universidade", "coimbra", "portugal"), "http://siteA.com");
    barrel.addWordToStructure(Set.of("universidade", "lisboa"), "http://siteB.com");
    barrel.addWordToStructure(Set.of("portugal", "turismo"), "http://siteC.com");

    // 2️⃣ Adicionar links entre páginas
    barrel.addLinks("http://siteA.com", Set.of("http://siteB.com", "http://siteC.com"));
    barrel.addLinks("http://siteB.com", Set.of("http://siteC.com"));

    // 3️⃣ Exibir mapa de popularidade
    System.out.println("📊 Popularidade das páginas:");
    barrel.urlPopularity.forEach((url, pop) -> System.out.println(url + " → " + pop));

    // 4️⃣ Testar busca por palavra única
    System.out.println("\n🔍 Busca por 'universidade':");
    List<String> result1 = barrel.returnSearchResult(List.of("universidade"));
    result1.forEach(url -> System.out.println(url + " (popularidade: " + barrel.urlPopularity.get(url) + ")"));

    // 5️⃣ Testar busca por duas palavras (interseção)
    System.out.println("\n🔍 Busca por 'portugal' E 'turismo':");
    List<String> result2 = barrel.returnSearchResult(List.of("portugal", "turismo"));
    result2.forEach(url -> System.out.println(url + " (popularidade: " + barrel.urlPopularity.get(url) + ")"));

    // 6️⃣ Testar busca por palavra inexistente
    System.out.println("\n🔍 Busca por 'computador':");
    List<String> result3 = barrel.returnSearchResult(List.of("computador"));
    System.out.println("Resultados: " + result3);

    // 7️⃣ Testar quem aponta para cada página
    System.out.println("\n🔗 Páginas que apontam para cada URL:");
    for (String url : List.of("http://siteA.com", "http://siteB.com", "http://siteC.com")) {
        Set<String> links = barrel.searchUrl(url);
        System.out.println(url + " é referenciado por: " + links);
    }

    // 8️⃣ Testar agrupamento manual (exemplo de 10 em 10)
    System.out.println("\n📑 Exemplo de agrupamento de resultados (paginado 10 em 10):");
    List<String> longResults = new ArrayList<>();
    for (int i = 1; i <= 25; i++) {
        longResults.add("http://site" + i + ".com");
    }
    int pageSize = 10;
    for (int i = 0; i < longResults.size(); i += pageSize) {
        int end = Math.min(i + pageSize, longResults.size());
        System.out.println("Página " + ((i / pageSize) + 1) + ": " + longResults.subList(i, end));
    }
}

}
