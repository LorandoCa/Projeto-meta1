import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;


public class MainStorageBarrel extends UnicastRemoteObject implements StorageBarrelInterface{
    // Índice invertido: palavra -> conjunto de URLs
    private Map<String, Set<String>> index;

    private Map<String, Set<String>> linkPages;

    private Map<String, Integer> urlPopularity;

    static Gateway_interface gateway;

    static String nome;

    public MainStorageBarrel() throws RemoteException {
        index = new HashMap<>();
        linkPages = new HashMap<>();
        urlPopularity = new HashMap<>();
        try {
            gateway= (Gateway_interface)Naming.lookup("Gateway");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public synchronized void addWordToStructure(Set<String> word, String url) {
        
        multicast(word, url);

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
        System.out.println(sortedURLs);
        
        return sortedURLs;
    }
    
    //Atualizar index
    public void multicast(Set<String> words, String url){
        String groupAddress = "230.0.0.0"; // endereço multicast (faixa 224.0.0.0–239.255.255.255)
        int port = 4446;

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(groupAddress);

            Map<String, Object> data = new HashMap<>();
            data.put("words", words);
            data.put("url", url);

            // Serializa o objeto para bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.flush();
            byte[] buffer = baos.toByteArray();
            // Cria o pacote e envia

            //Adicionar um numero de referencia para fazer filtragem de duplicados XXXX
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);

            //Esperar ACK. Define um limite de espera para voltar a enviar XXXXXX
            socket.receive(packet);
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //Atualizar relacoes de Urls
    public void multicast(String fromUrl, Set<String> toUrls){
        String groupAddress = "230.0.0.0"; // endereço multicast (faixa 224.0.0.0–239.255.255.255)
        int port = 4446;

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(groupAddress);

            Map<String, Object> data = new HashMap<>();
            data.put("fromUrl", fromUrl);
            data.put("toUrls", toUrls);

            // Serializa o objeto para bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.flush();
            byte[] buffer = baos.toByteArray();
            // Cria o pacote e envia

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try{
           
            System.out.println("RMI registry iniciado na porta 1099");

            StorageBarrelImp barrel = new StorageBarrelImp();
            Naming.rebind("Barrel1", barrel);
            nome= gateway.subscribe(barrel);

            }
            catch (Exception e) {
                System.out.println("null");
                e.printStackTrace();
            }
    }
}

