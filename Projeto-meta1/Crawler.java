
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
        Gateway_interface stub = (Gateway_interface)Naming.lookup("rmi://192.168.1.163:1099/Gateway");
        //Setup end

        String url = args[0];
        String crawler_name= args[1];
        System.out.printf("Eu sou %s\n", crawler_name);
        int ref=0;
        try {
            while(url!=null){
                StorageBarrelInterface stub_barrel= stub.getBarrel(); //Todos os crawlers comunicam com esse barrel: ERRADO
                //Aqui tem que ser um storage aleatorio ou o contrario do ultimo utilizado

                System.out.printf("%s\n", url);
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (compatible; MeuCrawler/1.0; +http://meusite.com)")
                        .header("From", "seuemail@dominio.com") // opcional, indica contato
                        .timeout(10_000) // timeout em ms
                        .get();
                StringTokenizer tokens = new StringTokenizer(doc.text());
                int countTokens = 0;

                Set< String> words_indexed= new HashSet<>();

                while (tokens.hasMoreElements() && countTokens++ < 100){
                    words_indexed.add(tokens.nextToken().toLowerCase());  
                }

                //Mandar ao Barrel a lista de palavras e o URL atual

                //Fazer uma thread para essa adicionar isso 
                System.out.printf("First\n");
                ref=stub_barrel.addWordToStructure(words_indexed,url,crawler_name,ref);
                ref++;
                System.out.printf("Second\n");
                Elements links = doc.select("a[href]");
                Set<String> Refs = new HashSet<>();

                for (Element link : links){
                    Refs.add(link.attr("abs:href"));
                    
                }

                ref=stub_barrel.addLinks(url, Refs,crawler_name, ref);
                ref++;
                //uma thread para esta funcao tambem
                
                stub.addURLs(new ArrayList<>(Refs)); //Inserir elementos na url queue
                //mandar esses links ao barrel tmb. O barrel vai receber uma lista um lista de links e o link aonde eles sairam 
                url=stub.getURL();
                System.out.println("reach out");
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