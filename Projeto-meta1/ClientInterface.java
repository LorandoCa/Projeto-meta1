import java.util.List;

public interface ClientInterface {
    public void indexNewURL(String url);
    public List<String> pesquisa(String words); //or URL if client wants to execute the 5th functionality
    public String statistic();
    
}
