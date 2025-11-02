import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;


public class MainStorageBarrel extends UnicastRemoteObject implements StorageBarrelInterface{
    // Índice invertido: palavra -> conjunto de URLs
    private Map<String, Set<String>> index;

    private Map<String, Set<String>> linkPages;

    private Map<String, Integer> urlPopularity;

    static Gateway_interface gateway;

    static String nome;

    private Map<String,Integer> last_sender;

    //@SuppressWarnings("unchecked")
    public MainStorageBarrel() throws RemoteException {
        index = new HashMap<>();
        linkPages = new HashMap<>();
        urlPopularity = new HashMap<>();
        last_sender= new HashMap<>();

        /*try{
            File file = new File("MainStorageIndex.bin");
            if (file.exists()){
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream("MainStorageIndex.bin"));
                index = (Map<String, Set<String>>) ois.readObject();
                ois.close();
            }
            file = new File("MainStorageLinks.bin");
            if (file.exists()){
                ObjectInputStream ois1 = new ObjectInputStream(new FileInputStream("MainStorageLinks.bin"));
                linkPages= (Map<String, Set<String>>) ois1.readObject();
                ois1.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }*/

        try {
            
            gateway= (Gateway_interface)Naming.lookup("rmi://192.168.1.197:1099/Gateway");
            
            StorageBarrelInterface S= gateway.getBarrel();
            if(S!= null) index= S.reboot();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public synchronized int addWordToStructure(Set<String> words, String url, String Crawler, int ref) {
        //System.out.println(words.size());
        //Se eu guardar o valor da ultima referencia dos dois crawlers, posso fazer filtragem corretamente
        if(last_sender.containsKey(Crawler) && last_sender.get(Crawler)<=ref) return ref;

        try {
            if( gateway.getBarrelNum() > 1){
                List<String> lista_aux= new ArrayList<>(words);
                int tam= words.size();
                int chunkSize = 50;
                int pos=0;
                while (pos < tam) {
                    int end = Math.min(pos + chunkSize, tam);
                    Set<String> chunk = new HashSet<>(lista_aux.subList(pos, end));

                    last_sender.put(Crawler,ref);

                    multicast(chunk,url,Crawler,ref);
                    ref++; //retornar esse valor na funcao para o crawler atualizar a sua prox ref disponivel
                    pos = end;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        

        for (String word : words){

            index.computeIfAbsent(word,  k -> new HashSet<>()).add(url);
        }
        System.out.println("Index updated");
        urlPopularity.putIfAbsent(url, 0); // garante que a URL existe no mapa

       
        return ref;
    }

    @Override
    public synchronized int addLinks(String fromUrl, Set<String> toUrls, String Crawler, int ref){
        if(last_sender.containsKey(Crawler) && last_sender.get(Crawler)<=ref) return ref;
        try {
            if( gateway.getBarrelNum() > 1){
                List<String> lista_aux= new ArrayList<>(toUrls);
                int tam= toUrls.size();
                int chunkSize = 50;
                int pos=0;
                while (pos < tam) {
                    int end = Math.min(pos + chunkSize, tam);
                    Set<String> chunk = new HashSet<>(lista_aux.subList(pos, end));

                    last_sender.put(Crawler,ref);

                    multicast(fromUrl,chunk,Crawler,ref);
                    ref++; //retornar esse valor na funcao para o crawler atualizar a sua prox ref disponivel
                    pos = end;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        linkPages.put(fromUrl, toUrls);
        System.out.println("Links adicionados");
        for (String to : toUrls) {
            urlPopularity.put(to, urlPopularity.getOrDefault(to, 0) + 1);
        }

        return ref;
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
    
    //Atualizar index
    public void multicast(Set<String> words, String url,String Crawler, int ref){
        String groupAddress = "230.0.0.0"; // endereço multicast (faixa 224.0.0.0–239.255.255.255)
        int port = 4446;

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(groupAddress);

            Map<String, Object> data = new HashMap<>();
            data.put("words", words);
            data.put("url", url);
            data.put("ref_num", ref); //num de ref para filtragem de duplicados
            data.put("Crawler", Crawler); //Crawler para filtragem de duplicados

            // Serializa o objeto para bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.flush();
            byte[] buffer = baos.toByteArray();
            // Cria o pacote e envia

            
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            envio(packet, socket);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void envio(DatagramPacket packet, DatagramSocket socket){ //metodo auxiliar
        System.out.println("SENT\n\n");
        while(true){
            try {
                if(gateway.getBarrelNum()==1) break;
                socket.send(packet);
                //Esperar ACK. Define um limite de espera para voltar a enviar XXXXXX
                socket.setSoTimeout(3000);
                byte[] ackBuffer = new byte[256];
                DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
                socket.receive(ackPacket);
                if (ackPacket.getLength() < 10) {
                    String msg = new String(ackPacket.getData(), 0, ackPacket.getLength(), StandardCharsets.UTF_8);
                
                    if (msg.equals("ACK")) {
                        System.out.println("Recebido ACK");
                        break;
                    }
                }
            

            } catch (SocketTimeoutException e) {
                e.printStackTrace();

            }catch(Exception e){
                e.printStackTrace();
            }
            
        }

    }


    //Atualizar relacoes de Urls
    public void multicast(String fromUrl, Set<String> toUrls, String Crawler, int ref){
        String groupAddress = "230.0.0.0"; // endereço multicast (faixa 224.0.0.0–239.255.255.255)
        int port = 4446;

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(groupAddress);

            Map<String, Object> data = new HashMap<>();
            data.put("fromUrl", fromUrl);
            data.put("toUrls", toUrls);
            data.put("ref_num",ref );
            data.put("Crawler", Crawler); //Crawler para filtragem de duplicados


            // Serializa o objeto para bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.flush();
            byte[] buffer = baos.toByteArray();
            // Cria o pacote e envia

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            envio(packet, socket);
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Set<String>> reboot() throws RemoteException {
        return index;
    }

    public static void main(String[] args) {

        try{
            LocateRegistry.createRegistry(1099);
            System.setProperty("java.rmi.server.hostname", "192.168.1.163");
            System.out.println("RMI registry iniciado na porta 1099");

            MainStorageBarrel barrel = new MainStorageBarrel();


            nome= gateway.subscribe(barrel);
            
            System.out.printf("Eu sou %s\n", nome);
            Naming.rebind(nome, barrel);
           


            MulticastHandler t= new MulticastHandler(barrel);
            t.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
    }

   

   

}

