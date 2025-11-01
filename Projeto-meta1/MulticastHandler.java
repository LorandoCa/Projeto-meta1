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
    MainStorageBarrel barrel;

    MulticastHandler(MainStorageBarrel barrel){
        this.barrel= barrel;
        // TODO Auto-generated method stub
        groupAddress = "230.0.0.0";
        port = 4446;

        try{
            this.socket = new MulticastSocket(port);
            group = InetAddress.getByName(groupAddress);
            netIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            System.out.println(InetAddress.getLocalHost());
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
                byte[] data = new byte[8192];

                DatagramPacket packet = new DatagramPacket(data, data.length);

                socket.receive(packet);
                System.out.println("passei1");
                /* 
                if((InetAddress.getLocalHost()).equals(packet.getAddress())){
                    continue;
                }*/
                System.out.println("passei");
                byte[] dados = packet.getData();
                int length = packet.getLength();
                //Enviar ACK 
                //Guardar a ultima ref recebida para manter a caracteristica at most once
                try (ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(dados, 0, length))) {
    
                    Object obj = ois.readObject();
                    if (!(obj instanceof Map<?, ?> rawMap)) {
                        System.out.println("Objeto recebido não é um Map!");
                        continue;
                    }
                    
                    // Fazemos a conversão genérica
                    Map<?, ?> map = rawMap;
    
                    // Caso 1: contém "words" e "url"
                    if (map.containsKey("words") && map.containsKey("url")) {
                        Object a = map.get("ref_num");
                        Object c= map.get("Crawler");
                        
                        
                        Object w = map.get("words");
                        Object u = map.get("url");
    
                        if (w instanceof Set<?> && u instanceof String) {
                           
                            // Converte com segurança
                            @SuppressWarnings("unchecked")
                            Set<String> words = (Set<String>) w;
                            String url = (String) u;
                            
                            byte[] buffer= "ACK".getBytes();
                            // Cria o pacote e envia
                            DatagramPacket ackpack = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
                            socket.send(ackpack);

                            barrel.addWordToStructure(words, url,(String)c, (int)a);
                            System.out.println("1 done\n\n");
                        } else {
                            System.err.println("Tipos incompatíveis para 'words' ou 'url'");
                        }
                    }
                    // Caso 2: contém "fromUrl" e "toUrls"
                    else if (map.containsKey("fromUrl") && map.containsKey("toUrls")) {

                        Object a = map.get("ref_num");
                        Object c= map.get("Crawler");

                        Object f = map.get("fromUrl");
                        Object t = map.get("toUrls");
    
                        if (f instanceof String && t instanceof Set<?>) {
                            @SuppressWarnings("unchecked")
                            Set<String> toUrls = (Set<String>) t;
                            String fromUrl = (String) f;

                            byte[] buffer= "ACK".getBytes();
                            // Cria o pacote e envia
                            DatagramPacket ackpack = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
                            socket.send(ackpack);

                            barrel.addLinks(fromUrl, toUrls,(String)c, (int)a); //Atualizacao de barrel
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
