import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

public class MulticastHandler extends Thread {

    String groupAddress ;
    int port ;
    MulticastSocket socket ;
    InetAddress group ;
    NetworkInterface netIf ;
    SocketAddress groupSockAddr ;
    StorageBarrelImp barrel;


    int last_ref_num;

    MulticastHandler(StorageBarrelImp barrel){
        this.barrel= barrel;
        last_ref_num= -1;
        // TODO Auto-generated method stub
        groupAddress = "230.0.0.0";
        port = 4446;

        try{
            this.socket = new MulticastSocket(port);
            group = InetAddress.getByName(groupAddress);
            netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            groupSockAddr = new InetSocketAddress(group, port);

            // Entrar no grupo multicast (novo método)
            socket.joinGroup(groupSockAddr, netIf);
            System.out.println("Aguardando mensagens no grupo " + groupAddress + " ...");
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    //Mostly chatGPT
    @Override
    public void run() {
        while(true){
            try {

                byte[] buffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                //Enviar ACK 
                //Guardar a ultima ref recebida para manter a caracteristica at most once
                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData()))) {
    
                    Object obj = ois.readObject();
                    if (!(obj instanceof Map<?, ?> rawMap)) {
                        System.err.println("Objeto recebido não é um Map!");
                        continue;
                    }
    
                    // Fazemos a conversão genérica
                    Map<?, ?> map = rawMap;
    
                    // Caso 1: contém "words" e "url"
                    if (map.containsKey("words") && map.containsKey("url")) {
                        Object a = map.get("ref_num");

                        if((int)a <= last_ref_num ){
                            byte[] buf= new byte[256];
                            buf= "ACK".getBytes();
                            DatagramPacket ackPacket = new DatagramPacket(
                                buf,
                                "ACK".length(),
                                packet.getAddress(),
                                packet.getPort()
                            );
                            socket.send(ackPacket);
                        }
                        last_ref_num= (int)a;

                        Object w = map.get("words");
                        Object u = map.get("url");
    
                        if (w instanceof Set<?> && u instanceof String) {
                            // Converte com segurança
                            @SuppressWarnings("unchecked")
                            Set<String> words = (Set<String>) w;
                            String url = (String) u;

                            barrel.addWordToStructure(words, url);
                        } else {
                            System.err.println("Tipos incompatíveis para 'words' ou 'url'");
                        }
                    }
                    // Caso 2: contém "fromUrl" e "toUrls"
                    else if (map.containsKey("fromUrl") && map.containsKey("toUrls")) {
                        Object f = map.get("fromUrl");
                        Object t = map.get("toUrls");
    
                        if (f instanceof String && t instanceof Set<?>) {
                            @SuppressWarnings("unchecked")
                            Set<String> toUrls = (Set<String>) t;
                            String fromUrl = (String) f;

                            barrel.addLinks(fromUrl, toUrls); //Atualizacao de barrel
                        } else {
                            System.err.println("Tipos incompatíveis para 'fromUrl' ou 'toUrls'");
                        }
                    }else {
                    System.err.println("Estrutura de dados desconhecida recebida: " + map.keySet());
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
                //mandar ACk X
                //Fazer codigo da parte do outro barrel q vai fazer envios multicast X
                //Fazer um envio completo quando um barrel se inscreve
                //Fazer filtragem de duplicados, usando uma ref para cada operacao da parte do barrel q recebe X
                //
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
            
        
    }
}
