package com.example;

import java.io.Serializable;
//import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserResource implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String username;

	private Map<String, List<String>> attributes;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }
    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }
}
