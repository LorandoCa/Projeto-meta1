public class PageInfo {
    private String url;
    private String titulo;
    private String citacao;

    public PageInfo(String url, String titulo, String citacao){
        this.url = url;
        this.titulo = titulo;
        this.citacao = citacao;
    }

     public String getUrl() {
        return url;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCitacao() {
        return citacao;
    }
}
