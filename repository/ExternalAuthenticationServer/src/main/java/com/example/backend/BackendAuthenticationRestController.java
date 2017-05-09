package com.example.backend;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.UserResource;

@RestController
public class BackendAuthenticationRestController {

	@RequestMapping(path = "/alphabank/userid", method = RequestMethod.GET)
	public String getAuthentication(@RequestParam("artifact") String artifact) {
		return "userid=ciekan9882ivndc";
	}
	
	@RequestMapping(path = "/alphabank/userinfo", method = RequestMethod.GET)
	public UserResource getUserInfo(@RequestParam("userid") String userid) {
	    Math.random();
        Random rnd = new Random();
        int ran = rnd.nextInt(10);
        String firstName = "Jane";
        if (ran < 3) firstName = "Alice";
        else if (ran < 7) firstName = "Emma";
		
        Map<String, List<String>> attributes = new HashMap<String, List<String>>();
        attributes.put("firstName", Arrays.asList(firstName));
        attributes.put("lastName", Arrays.asList("Doe"));        
        attributes.put("email", Arrays.asList("janedoe@example.com"));
        attributes.put("roles", Arrays.asList("user", "read", "write"));  
        attributes.put("sex", Arrays.asList("Female"));
        attributes.put("age", Arrays.asList("26"));
        attributes.put("customerRank", Arrays.asList("Gold"));
		UserResource res = new UserResource();
		res.setUsername("janedoe@example.com");
		res.setAttributes(attributes);
		return res;
	}
}
