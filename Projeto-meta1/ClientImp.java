
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ClientImp extends UnicastRemoteObject implements Client_interface {

    List<String> topTen;
    



    ClientImp() throws RemoteException {super();}
       

    @Override
    public void updateStatistics(List<String> topTenUpdate){//falta verificar barrels ativos e o tempo medio de pesquisa
        this.topTen= topTenUpdate;
    }

//Interface implemetnation end
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//METHODS


    public void subscribe(Gateway_interface gateway_stub){
        try{
            ClientImp client= new ClientImp();
            gateway_stub.subscribe(client);
        }catch(Exception e){
            System.out.println("Exception in main: " + e); 
        }
    } //Criar uma referencia e enviar a gateway, para fazer callback de estatisticas periodicamente
    //Uma thread por cliente a subscrever


    public void indexNewURL(String url, Gateway_interface gateway_stub){
        try {

            gateway_stub.addURL(url);
        } catch (Exception e) {

            e.printStackTrace();
        }
        
    }

    public List<String> pesquisa(String words){//or URL if client wants to execute the 5th functionality
        
        return null;
    } 

    

    public String statistic(){

        //Deve ser resolvido pela gateway

        return null;
    }

    public static void main(String[] args) {
        Gateway_interface gateway_stub;
        Scanner scanner = new Scanner(System.in);
        //gateway interface setup
        try {
            gateway_stub = (Gateway_interface)Naming.lookup("Gateway");
            //subscribe() here 
        



            while(true){
                System.out.print("1. Indexar um URL\n2. Pesquisar uma palavra\n3.Consultar lista de páginas com ligação para uma página específica\n"+
                    "4.Estatisticas\n");
                
                int option=0;
                String line = scanner.nextLine(); // lê toda a linha
                try {
                    option = Integer.parseInt(line.trim()); // remove espaços e converte
                } catch (NumberFormatException e) {
                    System.out.println("Opção inválida. Digite um número entre 1 e 4.");
                }
                


                switch (option) {
                case 1:
                    System.out.println("Escreva a sua URL\n");
                    String url = scanner.nextLine(); //Possivel verificacao do formato para confirmar que é uma URL
                    try {
                        gateway_stub.addURL(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    break;
                
                case 2:
                    System.out.println("Escreva uma palavra");
                    String wrd = scanner.nextLine(); //Possivel verificacao do formato para confirmar que é uma URL
                    try {
                        List<String> result= gateway_stub.pesquisa_word(wrd);
                        System.out.printf("%d\n\n", result.size());
                        for( String i : result){
                            System.out.printf("%s\n", i);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    break;

                    case 3:
                        System.out.println("Escreva a sua URL de referência\n");
                        url = scanner.nextLine(); //Possivel verificacao do formato para confirmar que é uma URL
                        try {
                            //Usar um metodo da interface gateway. esse metodo vai chamar um metodo da interfce do barrel
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    
                        break;

                    case 4:
                        try {
                            String estatisticas= gateway_stub.statistics();
                            System.out.println("--------------------STATISTICS-----------------------------");
                            System.out.printf("%s\n", estatisticas);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                default:
                    break;
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //end 
    }
}
    

