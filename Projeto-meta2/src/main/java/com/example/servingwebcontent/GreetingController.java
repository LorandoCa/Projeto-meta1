package com.example.servingwebcontent;

import com.example.servingwebcontent.interfaces.Gateway_interface;
import com.example.servingwebcontent.interfaces.PageInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.util.List;
import java.util.Properties;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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





    @GetMapping("/")
    public String redirect() {
        return "redirect:/index";
    }

	@GetMapping("/Search")
	public String Search(@RequestParam(name="word", required=true) String word, Model model) {

		try {
		List<PageInfo> result = gateway_stub.pesquisa_word(word);
		model.addAttribute("name", result);
		
		} catch (Exception e) {
			System.out.println("Erro a comunicar com a gateway");
		}

		return "SearchResults";
	}

}