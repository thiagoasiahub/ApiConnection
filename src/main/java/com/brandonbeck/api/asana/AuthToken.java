package com.brandonbeck.api.asana;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Properties;

import com.brandonbeck.api.util.ApiRequestClient;
import com.brandonbeck.api.util.ApiUtilities;
import com.brandonbeck.api.util.UrlBuilder;
import com.brandonbeck.api.util.ApiRequestClient.HttpMethod;
import com.brandonbeck.api.util.KeyValuePair;
import com.brandonbeck.api.util.UrlBuilder.Protocol;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AuthToken {

	String token;
	String refreshToken;
	Date expires;
	
	public AuthToken() throws Exception {
		token = null;
		refreshToken = null;
		expires = new Date();
		getTokenFromProperties();
	}

	public void getAuthTokenFromCode(String authCode) throws Exception {
		UrlBuilder builder = new UrlBuilder();
		builder = builder.setProtocol(Protocol.HTTPS)
				.setDomain("app.asana.com")
				.setPath("-/oauth_token");
		
		String postBody = "grant_type=authorization_code&" +
				"client_id=964079093504457&" + 
				"client_secret=92b8023bdeba76846e3e4915463bfe76&" +
				"code=" + authCode + "&" +
				"redirect_uri=" + URLEncoder.encode("https://webhook.site/85f90d30-d070-454b-b1c6-d2f740f96a75", "UTF-8");
		
		ApiRequestClient request = new ApiRequestClient();
		String response = request.establishURLConnection(builder.build(), HttpMethod.POST, postBody.toString(), 
				new KeyValuePair("Content-Type", "application/x-www-form-urlencoded"));
		setTokenFromJson(response);
		writeTokenToProperties();
		
	}
		
	private void refreshToken() throws Exception {
		UrlBuilder builder = new UrlBuilder();
		
		builder = builder.setProtocol(Protocol.HTTPS)
				.setDomain("app.asana.com")
				.setPath("-/oauth_token");
				
		String postBody = "grant_type=refresh_token&" +
				"client_id=964079093504457&" + 
				"client_secret=92b8023bdeba76846e3e4915463bfe76&" +
				"code=" + refreshToken + "&" +
				"redirect_uri=" + URLEncoder.encode("https://webhook.site/85f90d30-d070-454b-b1c6-d2f740f96a75", "UTF-8");
		
		ApiRequestClient request = new ApiRequestClient();
		
		String response = request.establishURLConnection(builder.build(), HttpMethod.POST, postBody.toString(), 
				new KeyValuePair("Content-Type", "application/x-www-form-urlencoded"));
		setTokenFromJson(response);
	}
	
	
	private void setTokenFromJson(String response) {
		JsonParser parser = new JsonParser();
		JsonObject authResponse = parser.parse(response).getAsJsonObject();
		this.token = authResponse.get("access_token").getAsString();
		this.refreshToken = authResponse.get("refresh_token").getAsString();
		this.expires = new Date(new Date().getTime() + (authResponse.get("expires_in").getAsInt() * 1000)); 
	}

	private void getTokenFromProperties() throws Exception {
		File file = new File("C:/Users/Brandon/oauth.properties");
		if(file.exists()) {
			InputStream in = new FileInputStream(file);
			Properties props = new Properties();
			props.load(in);
			in.close();
			token = props.getProperty("token");
			refreshToken = props.getProperty("refreshToken");
			expires = new Date(Long.parseLong(props.getProperty("expires")));
		}
	}
	
	private void writeTokenToProperties() throws Exception {
		File file = new File("C:/Users/Brandon/oauth.properties");
		FileOutputStream out = new FileOutputStream(file);
		Properties props = new Properties();
		props.setProperty("token", token);
		props.setProperty("refreshToken", refreshToken);
		props.setProperty("expires", String.valueOf(expires.getTime()));
		props.store(out, null);
		out.close();
	}

	public String getToken() throws Exception {
		if(new Date().after(expires)) {
			refreshToken();
		}
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public Date getExpires() {
		return expires;
	}
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	
	
}
