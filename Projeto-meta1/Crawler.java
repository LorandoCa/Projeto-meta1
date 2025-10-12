import java.io.*;
import java.util.*;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawler {
public static void main(String args[]) {
    
    //Setup

    //Setup end

    String url = args[0];
    try {
        Document doc = Jsoup.connect(url).get();
        StringTokenizer tokens = new StringTokenizer(doc.text());
        int countTokens = 0;

        List< String> words_indexed= new ArrayList<>();

        while (tokens.hasMoreElements() && countTokens++ < 100){
            words_indexed.add(tokens.nextToken().toLowerCase());  
        }

        //Mandar ao Barrel a lista de palavras e o URL atual

        Elements links = doc.select("a[href]");
        List<String> Refs = new ArrayList<>();

        for (Element link : links){
            Refs.add(link.attr("abs:href"));
            //Inserir elementos na URLqueue
        }

        //writer.write(Refs); serialize Refs
    } catch (IOException e) {
        e.printStackTrace();
    }
    }
}