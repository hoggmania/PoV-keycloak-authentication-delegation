package org.keycloak.examples.userstorage.readonly;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.util.JsonSerialization;
import org.keycloak.connections.httpclient.HttpClientProvider;

public class DelegatedUserStorageProvider implements UserStorageProvider, UserLookupProvider {
    protected KeycloakSession session;
    protected ComponentModel model;
    // map of loaded users in this transaction
    protected Map<String, UserModel> loadedUsers = new HashMap<>();
    
    public DelegatedUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }
    
    // UserLookupProvider methods

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
    	// here Adapter for UserModel is constructed and returned.
        UserModel adapter = loadedUsers.get(username);
        if (adapter == null) {
                adapter = createAdapter(realm, username);
                loadedUsers.put(username, adapter);
        }
        return adapter;
    }
    
    protected UserModel createAdapter(RealmModel realm, String username) {
    	
    	// WIP
    	
    	// here ask external authentication server for user info by "username" in context of keycloak.
		// Conventionally, user the term "username" for user ID which both keycloak and external authentication server use
		// to refer unique user in this context.
    	
    	// load external server's user info endpoint from configuration
    	// TODO: <important> need to verify value of each items of configuration
    	String userInfoUriString;
    	if (model.getConfig().containsKey(DelegatedUserStorageProviderFactory.AS_USERINFO_URI)) {
    		userInfoUriString = model.getConfig().getFirst(DelegatedUserStorageProviderFactory.AS_USERINFO_URI);
    	} else {
			System.out.println("Error. external server's user info endpoint not specified.");
			return null;	
    	}
    	
		//int status = 400;
		//boolean success = status == 204 || status == 200;
		// TODO: need to check status code, but how to do that?
    	System.out.println("loaded config : userinfo URI = " + userInfoUriString);
		InputStream is;
		try {
			is = session.getProvider(HttpClientProvider.class).get(userInfoUriString + "?userid=" + username);
		} catch (IOException ioe) {
			System.out.println("Error. http get. ioe = " + ioe.toString());
			return null;
		}
		if (is == null) {
			System.out.println("Error. http get. input stream is null");
			return null;	
		}
		
		AuthorizedUserInfo authorizedUserInfo;
		try {
			authorizedUserInfo = JsonSerialization.readValue(is, AuthorizedUserInfo.class);
			is.close();
		} catch (IOException ioe) {
			// TODO: confirm whether InputStream is automatically closed if IOException happens.
			System.out.println("JSON Serialization Error. summary=" + ioe.toString() + " detail=" + ioe.getMessage());
			return null;
		}	
		System.out.println("username as user ID =" + authorizedUserInfo.getUserName());
		System.out.println("attributes =" + authorizedUserInfo.getAttributes());
    	
        return new AbstractUserAdapter(session, realm, model) {
            @Override
            public String getUsername() {
                return authorizedUserInfo.getUserName();
            }
            @Override
            public Map<String, List<String>> getAttributes() {
                return authorizedUserInfo.getAttributes();
            }
        };
    }
    
    @Override
    public UserModel getUserById(String id, RealmModel realm) {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(username, realm);
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        return null;
    }
    
    @Override
    public void close() {
    }
}
