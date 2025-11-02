
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ClientImp extends UnicastRemoteObject implements Client_interface {

    static List<String> topTen;
    static List<String> BarrelsNames;
    static String nome;
    static long searchDur= 0;
    
    
    
        ClientImp() throws RemoteException {super();}
           
    
        @Override
        public void updateStatistics(List<String> topTenUpdate, List<String> BarrelsNamesUpdate, long searchDurUpdate){//falta verificar barrels ativos e o tempo medio de pesquisa
            topTen= topTenUpdate;
            BarrelsNames= BarrelsNamesUpdate;
            searchDur=searchDurUpdate;
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
            System.setProperty("java.rmi.server.hostname", "192.168.1.163");
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
                        List<PageInfo> result = gateway_stub.pesquisa_word(wrd);
                        System.out.printf("%d\n\n", result.size());
                        System.out.printf("=== Resultados da pesquisa ===\n\n");
                        int counter = 1;
                        for(PageInfo i : result){
                            System.out.printf("Título: %s\n", i.getTitulo());
                            System.out.printf("URL: %s\n", i.getUrl());
                            System.out.printf("Citação: %s\n", i.getCitacao());

                            counter++;
                            if(counter%10==0){
                                System.out.println("Quer continuar vendo resultados da pesquisa? [S/N]");
                                String confirmacao = scanner.nextLine(); // Confirmação apra proceder com os resultados da pesquisa
                                if(confirmacao.equals("S")){
                                    continue;
                                }
                                else{
                                    break;
                                }
                            }
                        }   
                            

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    break;

                    case 3:
                        System.out.println("Escreva a sua URL de referência\n");
                        url = scanner.nextLine(); //Possivel verificacao do formato para confirmar que é uma URL
                        try {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    
                        break;

                    case 4:
                        try {
                            System.out.println("--------------------STATISTICS-----------------------------");
                            System.out.println(topTen);
                            System.out.println("\n");
                            System.out.println(BarrelsNames);
                            System.out.println("\n");
                            System.out.println(searchDur);
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
    

