import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawler {
public static void main(String args[]) {
    
    //Setup
    try {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        System.out.println("Aquiiiiiii");
        GatewayInterface stub = (GatewayInterface) registry.lookup("Gateway");
        
        //Setup end

        String url = args[0];
        try {
            while(url!=null){
                Document doc = Jsoup.connect(url).get();
                StringTokenizer tokens = new StringTokenizer(doc.text());
                int countTokens = 0;

                Set<String> words_indexed= new HashSet<>();

                while (tokens.hasMoreElements() && countTokens++ < 100){
                    words_indexed.add(tokens.nextToken().toLowerCase());  
                }

                //Mandar ao Barrel a lista de palavras e o URL atual
                

                Elements links = doc.select("a[href]");
                Set<String> Refs = new HashSet<>();

                for (Element link : links){
                    String href = link.attr("abs:href");
                    Refs.add(href);
                    System.out.println(href);
                    //Inserir elementos na URLqueue
                }
                stub.addURLs(new ArrayList<>(Refs)); //Adicionar 
                url=stub.getURL();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    } catch (Exception e) {
        System.out.println("null");
        e.printStackTrace();
    }
}
}