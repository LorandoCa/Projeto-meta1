import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawler {
public static void main(String args[]) {
    
    //Setup
    PipedWriter writer = new PipedWriter();
    try {
        PipedReader reader = new PipedReader(writer);
    } catch (Exception IOException) {
        System.out.println("Couldn't create the reader pipe");
    }

    new Gateway();
    
    //Setup end

    String url = args[0];
    try {
        Document doc = Jsoup.connect(url).get();
        StringTokenizer tokens = new StringTokenizer(doc.text());
        int countTokens = 0;

        HashMap< String , Set<String> > words_indexed= new HashMap<>();

        while (tokens.hasMoreElements() && countTokens++ < 100){
            words_indexed.get(tokens.nextToken().toLowerCase()).add(url);     
        }

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