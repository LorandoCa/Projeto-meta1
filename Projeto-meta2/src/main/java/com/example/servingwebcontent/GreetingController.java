package com.example.servingwebcontent;

import com.example.servingwebcontent.interfaces.Gateway_interface;
import com.example.servingwebcontent.interfaces.PageInfo;
import com.example.servingwebcontent.forms.SearchForm;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.util.List;
import java.util.Properties;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.URISyntaxException;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

//import org.springframework.web.bind.annotation.RequestParam;

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
			List<PageInfo> result = gateway_stub.pesquisa_word(wordToLook);
			model.addAttribute("resultado", result);
			
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