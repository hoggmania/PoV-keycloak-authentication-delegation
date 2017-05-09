package com.example.frontend;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.KeycloakCredential;
import com.example.KeycloakCredentialRepository;

@Controller
public class AuthenticationController {
	
	@Autowired
	KeycloakCredentialRepository keycloakCredentialRepository;
	
	@RequestMapping(path = "/alphabank/authentication", method = RequestMethod.GET)
	public String httpRedirectBinding (
			@RequestParam("code") String code,
			@RequestParam("execution") String execution,
			@RequestParam("client_id") String clientId,
			@RequestHeader("User-Agent") String userAgent,
			Model model, Locale locale) {
		KeycloakCredential kc = new KeycloakCredential();
		kc.setCode(code);
		kc.setExecution(execution);
		keycloakCredentialRepository.save(kc);
		
		KeycloakCredential newkc = keycloakCredentialRepository.findAll().get(0);
		
		model.addAttribute("title", "Delegated Authentication");
		model.addAttribute("method", "In HTTP Redirect Binding");
		model.addAttribute("code", "code = " + newkc.getCode());
		model.addAttribute("execution", "execution = " + newkc.getExecution());
		model.addAttribute("client_id", "client_id = " + clientId);
		model.addAttribute("user_agent", "User-Agent = " + userAgent);
		
		//keycloakCredentialRepository.deleteAll();
		
		String modelName = "alphabank-customerportal";
		
		return modelName;
	}
	
	@RequestMapping(path = "/alphabank/authentication", method = RequestMethod.POST)
	public String httpPostBinding (
			@RequestParam("code") String code,
			@RequestParam("execution") String execution,
			@RequestParam("client_id") String clientId,
			@RequestHeader("User-Agent") String userAgent,
			Model model, Locale locale) {
		KeycloakCredential kc = new KeycloakCredential();
		kc.setCode(code);
		kc.setExecution(execution);
		keycloakCredentialRepository.save(kc);
		
		KeycloakCredential newkc = keycloakCredentialRepository.findAll().get(0);
		
		model.addAttribute("title", "Delegated Authentication");
		model.addAttribute("method", "In HTTP POST Binding");
		model.addAttribute("code", "code = " + newkc.getCode());
		model.addAttribute("execution", "execution = " + newkc.getExecution());
		model.addAttribute("client_id", "client_id = " + clientId);
		model.addAttribute("user_agent", "User-Agent = " + userAgent);
		
		//keycloakCredentialRepository.deleteAll();
		
		String modelName = "alphabank-customerportal";
		
		return modelName;
	}
	
	/*
	@RequestMapping(path = "/alphabank/authentication/redirect", method = RequestMethod.POST)
	public String redirect (
			@RequestParam("client_id") String clientId,
			@RequestParam("username") String userName,
			@RequestParam("password") String passWord, Model model, Locale locale) {
		String artifact = "983wn4r498hfsadugv90r3woi";
	
		String realm = "alphabank";
		
		KeycloakCredential newkc = keycloakCredentialRepository.findAll().get(0);
		String code = newkc.getCode();
		String execution = newkc.getExecution();

		keycloakCredentialRepository.deleteAll();
		String redirectUri = "http://localhost:8180/auth/realms/" + realm + "/login-actions/authenticate?code=" + code + "&execution=" + execution + "&artifact=" + artifact;
		return "redirect:" + redirectUri;
	}
	*/

	@RequestMapping(path = "/alphabank/authentication/redirect", method = RequestMethod.POST)
	public String redirect (
			@RequestParam("client_id") String clientId,
			@RequestParam("username") String userName,
			@RequestParam("password") String passWord, Model model, Locale locale) {
		String artifact = "983wn4r498hfsadugv90r3woi";
		String realm = "alphabank";
		
		KeycloakCredential newkc = keycloakCredentialRepository.findAll().get(0);
		String code = newkc.getCode();
		String execution = newkc.getExecution();
		keycloakCredentialRepository.deleteAll();
		
		String action = "http://localhost:8180/auth/realms/" + realm + "/login-actions/authenticate?code=" + code + "&execution=" + execution;
		model.addAttribute("action", action);
		//model.addAttribute("code", code);
		//model.addAttribute("execution", execution);
		model.addAttribute("artifact", artifact);
		
		String modelName = "formpost";
		
		return modelName;
	}
	
}
