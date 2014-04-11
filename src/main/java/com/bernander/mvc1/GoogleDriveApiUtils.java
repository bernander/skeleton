package com.bernander.mvc1;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

// ...

class GoogleDriveApiUtils {

  private static final String REDIRECT_URI      = "http://crammage.com:8080/mvc1/authorization"; // Upon successful grant of user-data access; must match the one registered
  private static final String APP_CLIENT_ID     = "312818940423-96v1d79bgvemhbk8idgn6t6tmhudlol5.apps.googleusercontent.com";
  private static final String APP_CLIENT_SECRET =  "pZZ0c4OKFX_eCr7JC7--LA1s";
  private static final String APP_CLIENT_ID_AND_SECRET_JSON =
		  "{"+
		  "  \"web\": {"+
		  "    \"client_id\": \""     + APP_CLIENT_ID     + "\","+
		  "    \"client_secret\": \"" + APP_CLIENT_SECRET + "\","+
		  "    \"auth_uri\":  \"https://accounts.google.com/o/oauth2/auth\","+
		  "    \"token_uri\": \"https://accounts.google.com/o/oauth2/token\""+
		  "    }"+
		  "}";
  private static final List<String> SCOPES = Arrays.asList(
      "https://www.googleapis.com/auth/drive",
      "https://www.googleapis.com/auth/userinfo.email",
      "https://www.googleapis.com/auth/userinfo.profile");

  private static GoogleAuthorizationCodeFlow flow = null;

  // Error occurred while retrieving credentials.
  public static class GetCredentialsException extends Exception {
    protected String authorizationUrl; // The authorization URL to redirect the user to
    public GetCredentialsException(String authorizationUrl) { this.authorizationUrl = authorizationUrl; }
    public void setAuthorizationUrl(String authorizationUrl)   { this.authorizationUrl = authorizationUrl; }
    public String getAuthorizationUrl()   { return authorizationUrl; }
  }
  // A code exchange has failed.
  public static class CodeExchangeException extends GetCredentialsException {
    public CodeExchangeException(String authorizationUrl)   { super(authorizationUrl); }
  }
  // No refresh token has been found.
  public static class NoRefreshTokenException extends GetCredentialsException {
    public NoRefreshTokenException(String authorizationUrl)   { super(authorizationUrl); }
  }
  // No user ID could be retrieved.
  private static class NoUserIdException extends Exception {}
  
  

  // Retrieve OAuth 2.0 credentials from webapp's database, or null if not found.
  static Credential getStoredCredentials(String userId) {
    throw new UnsupportedOperationException(); // IMPLEMENT
  }
//  // Store user's OAuth 2.0 credentials in the webapp's database.
//  static void storeCredentials(String userId, Credential credentials) {
//    throw new UnsupportedOperationException(); // IMPLEMENT
//  }

  /**
   * Build an authorization flow; cached statically.
   */
    static GoogleAuthorizationCodeFlow getFlow() {
    	if (flow == null) {
    		HttpTransport httpTransport = new NetHttpTransport();
	        JacksonFactory jsonFactory = new JacksonFactory();
	        GoogleClientSecrets clientSecrets = null;
	      	try {
	    		clientSecrets = GoogleClientSecrets.load(jsonFactory, new StringReader(APP_CLIENT_ID_AND_SECRET_JSON));
	    	} catch (IOException e) { /* Doesn't happen with a StringReader. */ }
	    	flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, SCOPES).setAccessType("offline").setApprovalPrompt("force").build(); 
	    }
	    return flow;
    }

  /**
   * Exchange a user's authorization code for OAuth 2.0 credentials.
   */
  static Credential exchangeCode(String authorizationCode) throws CodeExchangeException {
    try {
      GoogleAuthorizationCodeFlow flow = getFlow();
      GoogleTokenResponse response = flow.newTokenRequest(authorizationCode).setRedirectUri(REDIRECT_URI).execute();
      return flow.createAndStoreCredential(response, null);
    } catch (IOException e) {
      System.err.println("An error occurred: " + e);
      throw new CodeExchangeException(null);
    }
  }

  /**
   * Send a request to the UserInfo API to retrieve the user's information.
   */
  static Userinfo getUserInfo(Credential credentials) throws NoUserIdException {
    Oauth2 userInfoService = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credentials).build();
    Userinfo userInfo = null;
    try {
      userInfo = userInfoService.userinfo().get().execute();
    } catch (IOException e) {
      System.err.println("An error occurred: " + e);
    }
    if (userInfo != null && userInfo.getId() != null) {
      return userInfo;
    } else {
      throw new NoUserIdException();
    }
  }

  /**
   * Figure out the Authorization URL to redirect the user to.
   * After user select "Accept", user lands on REDIRECT_URL with request parameters state and code.
   * After user select "Cancel", user lands on REDIRECT_URL with request parameters state and error=access_denied.
   */
  public static String getAuthorizationUrl(String usersEmailAddress, String state) {
    GoogleAuthorizationCodeRequestUrl urlBuilder = getFlow().newAuthorizationUrl().setRedirectUri(REDIRECT_URI).setState(state);
    urlBuilder.set("user_id", usersEmailAddress);
    return urlBuilder.build();
  }

  /**
   * Retrieve credentials using the provided authorization code.
   *
   * This function exchanges the authorization code for an access token and
   * queries the UserInfo API to retrieve the user's e-mail address. If a
   * refresh token has been retrieved along with an access token, it is stored
   * in the application database using the user's e-mail address as key. If no
   * refresh token has been retrieved, the function checks in the application
   * database for one and returns it if found or throws a NoRefreshTokenException
   * with the authorization URL to redirect the user to.
   *
   * @param authorizationCode Authorization code to use to retrieve an access token.
   * @return OAuth 2.0 credentials instance containing an access and refresh
   *         token.
   * @throws NoRefreshTokenException No refresh token could be retrieved from
   *         the available sources.
   * @throws IOException Unable to load client_secrets.json.
   */
  public static Credential getCredentialsFromGoogle(String authorizationCode, String state) throws CodeExchangeException, NoRefreshTokenException, IOException {
    String emailAddress = "";
    try {
      Credential credential = exchangeCode(authorizationCode);
      Userinfo userInfo = getUserInfo(credential);
      String userId = userInfo.getId();
      emailAddress = userInfo.getEmail();
      if (credential.getRefreshToken() != null) {
//        storeCredentials(userId, credentials);
        return credential;
      } else {
        credential = getStoredCredentials(userId);
        if (credential != null && credential.getRefreshToken() != null) {
          return credential;
        }
      }
    } catch (CodeExchangeException e) {
      e.printStackTrace();
      // Drive apps should try to retrieve the user and credentials for the current session.
      // If none is available, redirect the user to the authorization URL.
      e.setAuthorizationUrl(getAuthorizationUrl(emailAddress, state));
      throw e;
    } catch (NoUserIdException e) {
      e.printStackTrace();
    }
    // No refresh token has been retrieved.
    String authorizationUrl = getAuthorizationUrl(emailAddress, state);
    throw new NoRefreshTokenException(authorizationUrl);
  }
  
  private static final String SESSION_CREDENTIALS = "oauth2Credentials"; 
  public static Credential getCredentials(HttpSession session) {
	  return (Credential) session.getAttribute(SESSION_CREDENTIALS);
  }
  public static void setCredentials(HttpSession session, Credential credential) {
	  session.setAttribute(SESSION_CREDENTIALS, credential);
  }
  
  public static Drive getDrive(Credential credential) {
	  return new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), credential).build();
  }
  
  

}