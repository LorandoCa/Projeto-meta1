
import java.io.*;
import java.rmi.Naming;
import java.util.*;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawler {
public static void main(String args[]) {
    
    //Setup
    try {
        Gateway_interface stub = (Gateway_interface) Naming.lookup("Gateway");
        StorageBarrelInterface stub_barrel= (StorageBarrelInterface) Naming.lookup("Barrel");
        //Setup end

        String url = args[0];
        try {
            while(url!=null){
                System.err.printf("%s\n", url);
                Document doc = Jsoup.connect(url).get();
                StringTokenizer tokens = new StringTokenizer(doc.text());
                int countTokens = 0;

                Set< String> words_indexed= new HashSet<>();

                while (tokens.hasMoreElements() && countTokens++ < 100){
                    words_indexed.add(tokens.nextToken().toLowerCase());  
                }

                //Mandar ao Barrel a lista de palavras e o URL atual
                stub_barrel.addWordToStructure(words_indexed, url);
                Elements links = doc.select("a[href]");
                Set<String> Refs = new HashSet<>();

                for (Element link : links){
                    Refs.add(link.attr("abs:href"));
                    
                }
                stub.addURLs(new ArrayList<>(Refs)); //Inserir elementos na url queue
                //mandar esses links ao barrel tmb. O barrel vai receber uma lista um lista de links e o link aonde eles sairam 
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