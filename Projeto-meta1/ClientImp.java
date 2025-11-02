
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ClientImp extends UnicastRemoteObject implements Client_interface {

    static List<String> topTen;
    static String nome;
    
    
    
        ClientImp() throws RemoteException {super();}
           
    
        @Override
        public void updateStatistics(List<String> topTenUpdate){//falta verificar barrels ativos e o tempo medio de pesquisa
            topTen= topTenUpdate;
        }
    
    //Interface implemetnation end
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //METHODS
    
    
        public static String subscribe(Gateway_interface gateway_stub){
            String res= null;
            try{
                ClientImp client= new ClientImp();
                res= gateway_stub.subscribe(client);
            }catch(Exception e){
                System.out.println("Exception in main: " + e); 
            }
    
            return res;
        } //Criar uma referencia e enviar a gateway, para fazer callback de estatisticas periodicamente
        //Uma thread por cliente a subscrever
    
    
        public static void indexNewURL(String url, Gateway_interface gateway_stub){
            try {
    
                gateway_stub.addURL(url);
            } catch (Exception e) {
    
                e.printStackTrace();
            }
            
        }
    
    
        //Pode se fazer caching de pesquisas de cada cliente
        public static void main(String[] args) {
            Gateway_interface gateway_stub;
            Scanner scanner = new Scanner(System.in);
            //gateway interface setup
            try {
                gateway_stub = (Gateway_interface)Naming.lookup("rmi://192.168.1.197:1099/Gateway");
                //(Gateway_interface) Naming.lookup("rmi://192.168.176.1:1099/Gateway"); achar um server num ip especifico 
                nome=subscribe(gateway_stub);


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
                    System.out.println("Escreva a sua URL");
                    String url = scanner.nextLine(); //Possivel verificacao do formato para confirmar que é uma URL
                    indexNewURL(url,gateway_stub);
                    
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
                            System.out.println("--------------------STATISTICS-----------------------------");
                            System.out.println(topTen);
                            System.out.println("\n");
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
    

