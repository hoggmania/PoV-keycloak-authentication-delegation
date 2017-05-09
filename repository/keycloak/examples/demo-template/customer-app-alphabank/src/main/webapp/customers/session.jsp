<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="org.keycloak.common.util.HostUtils" %>
<%@ page import="org.keycloak.example.CustomerDatabaseClient" %>
<%@ page import="org.keycloak.common.util.KeycloakUriBuilder" %>
<%@ page import="org.keycloak.constants.ServiceUrlConstants" %>
<%@ page import="org.keycloak.representations.IDToken" %>
<html>
<head>
  <title>Customer Session Page for Alpha Bank</title>
</head>
<body bgcolor="#FFCC33">
<%
    String logoutUri = KeycloakUriBuilder.fromUri("http://localhost:8180/auth").path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH)
            .queryParam("redirect_uri", "http://localhost:8380/customer-portal-alphabank").build("alphabank").toString();
    IDToken idToken = CustomerDatabaseClient.getIDToken(request);
%>
<p>Goto: <a href="<%=logoutUri%>">logout</a></p>
<p>Your hostname: <b><%= HostUtils.getHostName() %></b></p>
    <p>Your session ID: <b><%= request.getSession().getId() %></b></p>
    <p>You visited this page <b><%= CustomerDatabaseClient.increaseAndGetCounter(request) %></b> times.</p>
    <br><br>
  </body>
</html>