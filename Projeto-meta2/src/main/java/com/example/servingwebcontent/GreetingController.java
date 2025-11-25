package com.example.servingwebcontent;

import com.example.servingwebcontent.interfaces.Gateway_interface;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.response.OllamaResult;

import com.example.servingwebcontent.forms.PageInfo;
import com.example.servingwebcontent.forms.SearchForm;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.util.List;
import java.util.Properties;

import java.util.concurrent.*;

import org.springframework.ui.Model;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;




@Controller
public class GreetingController {

	/////////////////////////////////////SETUP///////////////////////////////////////////////////////////////
	String endereço_gateway=null;
	String porta=null;
	Properties config = new Properties();
	Gateway_interface gateway_stub;

	GreetingController(){

		try (FileInputStream input = new FileInputStream("config.properties")) {
			// Carrega o arquivo .properties
			config.load(input);
			// Lê as propriedades
			endereço_gateway= config.getProperty("rmi.host2");
			porta = config.getProperty("rmi.port2");
		}catch(IOException e) {
			System.out.println("Erro ao carregar arquivo de configuração: " + e.getMessage());
		}

		
		//gateway interface setup
		try {
			gateway_stub = (Gateway_interface)Naming.lookup( String.format("rmi://%s:%s/Gateway",endereço_gateway,porta));
		}catch(Exception exception){}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public String Completion(String wordToLook){
		try{
			Ollama ollama = new Ollama("http://localhost:11434/");
			// We're just using our quick-setup utility here to instantiate Ollama. Use the following
			// to set it up with your Ollama configuration.
			// Ollama ollama = new Ollama("http://your-ollama-host:11434/");
			String model = "mistral:7b";
			ollama.pullModel(model);

			OllamaResult result =
					ollama.generate(
							OllamaGenerateRequest.builder()
									.withModel(model)
									.withPrompt(wordToLook)
									.build(),
								null);

								
			return result.getResponse();

		}catch(Exception e){
			System.out.println("Erro ao comunicar com o Ollama server");
		}
        return null;
    }

	public boolean isValidURL(String wordToIndex) {
		try {
			URI uri = new URI(wordToIndex);
			// Verifica se tem esquema (http/https)
			if (uri.getScheme() == null) {
				return false;
			}
			return true;
		} catch (URISyntaxException e) {
			return false;
		}
	}


    @GetMapping("/")
    public String redirect() {
        return "index";
    }

	@GetMapping("/goToSearch")
	public String goToSearch(Model model, @RequestParam(defaultValue = "false") boolean wichOne) {
		model.addAttribute("searchForm", new SearchForm());
		if (wichOne) return "indexURL";
		else return "Search";
	}

	//Se for introduzida uma URL, faz-se a pesquisa das URLs ligadas a essa
	@GetMapping("/Search")
	public String Search(@ModelAttribute SearchForm searchForm, Model model) {
		
		String wordToLook= searchForm.getWord();
		
		if(isValidURL(wordToLook)){

			try{

				List<String> result = gateway_stub.pesquisa_URL(wordToLook);
				model.addAttribute("resultado", result);
				

			}catch(Exception e){
				System.out.println("erro ao comunicar com a gateway. URl nao pesquisado");
			}

		}
		else{
			try {

				GreetingController obj = new GreetingController();
				// Supondo que Completion retorna String
				Callable<String> tarefa = () -> obj.Completion(wordToLook);

				// Cria um executor para gerenciar threads
				ExecutorService executor = Executors.newSingleThreadExecutor();

				// Submete a tarefa e obtém um Future
				Future<String> futureResult = executor.submit(tarefa);

				// Executa outras coisas enquanto a thread trabalha
				List<PageInfo> result = gateway_stub.pesquisa_word(wordToLook);
				model.addAttribute("resultado", result);
				// Para pegar o resultado da thread (bloqueia até terminar)
				
				try {
					String completionResult = futureResult.get(); 
					model.addAttribute("resultadoCompletion", completionResult);
				} catch (InterruptedException e) {
					System.out.println("Problema com a thread de execução do Ollama");
				}catch (ExecutionException e) {
					System.out.println("Problema com a thread de execução do Ollama");
				}

				// Encerra o executor
				executor.shutdown();
								
				String confirmation= searchForm.getIndexHAckerNews();

				if(confirmation.equals("yes")){



					//Codigo para index URLs de top Stories de HackerNews que contenham os termos da variavel "wordToLook"
				
				
				}


			} catch (Exception e) {
				System.out.println("Erro a comunicar com a gateway. Frase nao pesquisada");
			}
		}

		return "SearchResults";
	}

	@GetMapping("/indexUrl")
	public String indexUrl(@ModelAttribute SearchForm searchForm, Model model) {

		String wordToIndex= searchForm.getWord();

		if(isValidURL(wordToIndex)){
			try {
			gateway_stub.addURL(wordToIndex);
			
			} catch (Exception e) {
				System.out.println("Erro a comunicar com a gateway");
			}
			return "index";
		}
		else{
			searchForm.setVarTypeError(true); //passa-se o ultimo parametro para gerar
																												//um allert na view
			return "indexURL";
		}

	}


}