package org.keycloak.examples.authenticator;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Details;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.EnvUtil;

import org.jboss.resteasy.spi.HttpRequest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.net.URI;

import java.util.Properties;

public class AuthnDelegationAuthenticator implements Authenticator  {

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		// TODO Auto-generated method stub
		
		// WIP
		// TODO: <important> need to verify value of each items of configuration
		
		// extract code and execution for this browser login session
		// those binds all HTTP connections by keycloak's login session and external authentication server's login session
		String code = context.generateAccessCode();
		String execution = context.getExecution().getId();
		
		// extract header fields, query parameters, and client_id from HTTP request
		// for picking up forwarding parameters toward external authentication server
		HttpRequest httpRequest = context.getHttpRequest();
		MultivaluedMap<String, String> headerFields = httpRequest.getHttpHeaders().getRequestHeaders();
		MultivaluedMap<String, String> queryParameters = httpRequest.getUri().getQueryParameters();		
		System.out.println("headerFields = " + headerFields);
		System.out.println("queryParameters = " + queryParameters);
		
		// Only supporting either HTTP Redirect Binding or POST Binding for accessing external authentication server
		boolean isRedirect = false; 
		URI redirect = null;
		
		// Load authenticator configuration
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		String baseUri = null;
        if (config != null) {
            baseUri = config.getConfig().get(AuthnDelegationAuthenticatorFactory.AS_AUTHN_URI);
        }
        // TODO: if config is null, do what?
        
        // Determine whether conducting HTTP FORM POST or HTTP redirect.
        if (Boolean.valueOf(config.getConfig().get(AuthnDelegationAuthenticatorFactory.IS_HTTP_FORM_POST)) == true) {
        	isRedirect = false;
        	System.out.println("Conduct HTTP FORM POST");
        } else {
        	isRedirect = true;
        	System.out.println("Conduct HTTP Redirect");
        }
        		
		// Read forwarding query parameters
        String path = config.getConfig().get(AuthnDelegationAuthenticatorFactory.FW_QUERY_PARAMS);
        // TODO: if path is null, do what?
        path = EnvUtil.replace(path);
        Properties propsQueryParams = new Properties();
        try {
            InputStream is = new FileInputStream(path);
            propsQueryParams.load(is);
            is.close();
        } catch (IOException e) {
        	// TODO: if exception rises (ex. file not exist), do what?
            throw new RuntimeException(e);
        }
		// Read forwarding http header fields
        path = config.getConfig().get(AuthnDelegationAuthenticatorFactory.FW_HTTP_HEADERS);
        // TODO: if path is null, do what?
        path = EnvUtil.replace(path);
        Properties propsHeaders = new Properties();
        try {
            InputStream is = new FileInputStream(path);
            propsHeaders.load(is);
            is.close();
        } catch (IOException e) {
        	// TODO: if exception rises (ex. file not exist), do what?
            throw new RuntimeException(e);
        }
		
		Response challengeResponse;
		
		if(isRedirect) {
			// HTTP Redirect Binding
			try {
				redirect = new URI(baseUri + getRedirectBindQueryString(code, execution, propsQueryParams, queryParameters, propsHeaders, headerFields));
			} catch (Exception e) {
				// TODO: if exception rises (ex. file not exist), do what?
				return;
			}
			challengeResponse = Response.status(302).location(redirect).build();
		} else {
			// HTTP POST Binding
			try {
				redirect = new URI(baseUri);
			} catch (Exception e) {
				// TODO: if exception rises (ex. file not exist), do what?
				return;
			}
			String postBindBody = getPostBindBody(redirect.toString(), code, execution, propsQueryParams, queryParameters, propsHeaders, headerFields);
			challengeResponse = Response.status(200).type(MediaType.TEXT_HTML_TYPE).entity(postBindBody).build();
		}
		context.getClientSession().setNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION, context.getExecution().getId());
		context.challenge(challengeResponse);
	}
	
	private String getRedirectBindQueryString(String code, String execution, Properties propsQueryParams, MultivaluedMap<String, String> queryParameters,
			Properties propsHeaders, MultivaluedMap<String, String> headerFields) {
		
		// WIP
		
		StringBuilder builder = new StringBuilder();
		
        // needed for binding external authentication server login session and keycloak browser login session
        builder.append("?code=" + code + "&execution=" + execution);
        
        String clientId = queryParameters.getFirst(OAuth2Constants.CLIENT_ID);
        if (clientId != null) builder.append("&client_id=" + clientId);
        
        // forwarding http header fields
        for (String propertyName : propsHeaders.stringPropertyNames()) {
            String propertyValue = propsHeaders.getProperty(propertyName);
            System.out.println(propertyName + " = " + propertyValue);
            if (headerFields.containsKey(propertyName)) {
            	builder.append("&" + propertyValue + "=" + Base64Url.encode(headerFields.getFirst(propertyName).getBytes()));
            	System.out.println("forwarding header " + propertyValue + " = " + headerFields.getFirst(propertyName) + " base64url enc -> " + Base64Url.encode(headerFields.getFirst(propertyName).getBytes()));
            }
        }
        // TODO: how about checking whether each query parameter name is URL safe or not, and if not, do what?
        
        // forwarding query parameters
        for (String propertyName : propsQueryParams.stringPropertyNames()) {
            String propertyValue = propsQueryParams.getProperty(propertyName);
            System.out.println(propertyName + " = " + propertyValue);
            if (queryParameters.containsKey(propertyName)) {
            	builder.append("&" + propertyValue + "=" + Base64Url.encode(queryParameters.getFirst(propertyName).getBytes()));
            	System.out.println("forwarding parameter " + propertyValue + " = " + queryParameters.getFirst(propertyName) + " base64url enc -> " + Base64Url.encode(queryParameters.getFirst(propertyName).getBytes()));
            }
        }
        // TODO: how about checking whether each query parameter name is URL safe or not, and if not, do what?

        
		return builder.toString();
	}

	private String getPostBindBody(String redirect, String code, String execution, 
			Properties propsQueryParams, MultivaluedMap<String, String> queryParameters,
			Properties propsHeaders, MultivaluedMap<String, String> headerFields) {
		
		// WIP
		
		StringBuilder builder = new StringBuilder();
		
        builder.append("<HTML>");
        builder.append("<HEAD>");
        builder.append("<TITLE>Submit This Form</TITLE>");
        builder.append("</HEAD>");
        builder.append("<BODY Onload=\"javascript:document.forms[0].submit()\">");
        builder.append("<FORM METHOD=\"POST\" ACTION=\"" + redirect + "\">");
        
        // needed for binding external authenticator and keycloak browser login authenticator
        builder.append("<INPUT name=\"code\" TYPE=\"HIDDEN\" VALUE=\"" + code + "\" />");
        builder.append("<INPUT name=\"execution\" TYPE=\"HIDDEN\" VALUE=\"" + execution + "\" />");
        
        String clientId = queryParameters.getFirst(OAuth2Constants.CLIENT_ID);
        builder.append("<INPUT name=\"client_id\" TYPE=\"HIDDEN\" VALUE=\"" + clientId + "\" />");
        
        // forwarding http headers
        for (String propertyName : propsHeaders.stringPropertyNames()) {
            String propertyValue = propsHeaders.getProperty(propertyName);
            System.out.println(propertyName + " = " + propertyValue);
            if (headerFields.containsKey(propertyName)) {
            	System.out.println("forwarding header " + propertyValue + " = " + headerFields.getFirst(propertyName));
            	builder.append("<INPUT name=\"" + propertyValue + "\" TYPE=\"HIDDEN\" VALUE=\"" + headerFields.getFirst(propertyName) + "\" />");
            }
        }
        // forwarding query parameters
        for (String propertyName : propsQueryParams.stringPropertyNames()) {
            String propertyValue = propsQueryParams.getProperty(propertyName);
            System.out.println(propertyName + " = " + propertyValue);
            if (queryParameters.containsKey(propertyName)) {
            	System.out.println("forwarding parameter " + propertyValue + " = " + queryParameters.getFirst(propertyName));
            	builder.append("<INPUT name=\"" + propertyValue + "\" TYPE=\"HIDDEN\" VALUE=\"" + queryParameters.getFirst(propertyName) + "\" />");
            }
        }
        
        builder.append("<NOSCRIPT>")
        .append("<P>JavaScript is disabled. We strongly recommend to enable it. Click the button below to continue.</P>")
        .append("<INPUT TYPE=\"SUBMIT\" VALUE=\"CONTINUE\" />")
        .append("</NOSCRIPT>");
        builder.append("</FORM></BODY></HTML>");
		return builder.toString();
	}
	
	@Override
	public void action(AuthenticationFlowContext context) {
		// TODO Auto-generated method stub
		
		// WIP
		// TODO: <important> need to verify value of each items of configuration
		
		// receive HTTP POST request from external authentication server
		// carrying artifact for querying user id in back end channel of external authentication server
		HttpRequest httpRequest = context.getHttpRequest();
		
		// get URI for obtaining authenticated user id from external authentication server
		String baseUri = null;
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        if (config != null) {
            baseUri = config.getConfig().get(AuthnDelegationAuthenticatorFactory.AS_USERID_URI);
        }
        // TODO: if config is null, do what?

        // TODO: get artifact from body due to making external authentication server execute http form post
		String artifactValue = null;
		MultivaluedMap<String, String> queryParameters = httpRequest.getUri().getQueryParameters();
		if (queryParameters.containsKey("artifact")) artifactValue = queryParameters.getFirst("artifact");
		
		// TODO: if artifactValue is null, do what?
		String postText = "artifact=" + artifactValue;
		System.out.println(postText);
		//int status = 400;
		//boolean success = status == 204 || status == 200;
		
		// TODO: need to check status code, but how to do that?
		InputStream is;
		try {
			is = context.getSession().getProvider(HttpClientProvider.class).get(baseUri + "?" + postText);
		} catch (Exception e) {
			context.cancelLogin();
			return;
		}
		if (is == null) {
			System.out.println("InputStream is null.");
			context.cancelLogin();
			return;
		}
		
		// get authenticated user ID from external authentication server
		// Conventionally, user the term "username" for user ID which both keycloak and external authentication server use
		// to refer unique user in this context.
		// TODO: these codes seem to be quite low level so messy...
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
        	while ((line = br.readLine()) != null) {
        		sb.append(line);
        	}
        	System.out.println("read from inputstream : " + sb.toString());
        	br.close();
        } catch (IOException ioe) {
			// TODO: confirm whether InputStream is automatically closed if IOException happens.
        	System.out.println("Eror. ioe = " + ioe.toString());
        }
		String username = sb.toString().split("=", 0)[1];
		
        context.getEvent().detail(Details.USERNAME, username);
        context.getClientSession().setNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        UserModel user = null;
        try {
        	// here, it relies on DelegatedUserStorageProvider as User Storage SPI's provider to get user info
        	// from external authentication server and convert it onto userModel by using adapter class.
            user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
        } catch (Exception e) {
        	System.out.println("Eror. user storage spi lookup error = " + e.toString());
            context.cancelLogin();
            return;
        }
        System.out.println("usermodel username=" + user.getUsername());
		context.setUser(user);
		context.success();
	}

	@Override
	public boolean requiresUser() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		// TODO Auto-generated method stub
	}
	
}
