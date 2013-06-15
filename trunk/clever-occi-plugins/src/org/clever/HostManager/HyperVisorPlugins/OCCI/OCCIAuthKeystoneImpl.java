package org.clever.HostManager.HyperVisorPlugins.OCCI;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.jdom.Element;




class Authorization {
    
    class PasswordCredentials {
        public PasswordCredentials(String user, String pass)
        {
            username = user;
            password = pass;
        }
        public String username;
        public String password;
    }
    
    class Auth {
        public Auth(String tenant, String user, String pass)
        {
            tenantName = tenant;
            passwordCredentials = new PasswordCredentials(user, pass);
        }
        public String tenantName;
        public PasswordCredentials passwordCredentials;
    }
    
    
    
    public Authorization(String tenant, String user,String pass)
    {
        auth = new Auth(tenant,user,pass);
    }
    
    public Auth auth;
}







/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author salvullo
 */
public class OCCIAuthKeystoneImpl implements OCCIAuthImpl {

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OCCIAuthKeystoneImpl.class);  
    
    
  private UsernamePasswordCredentials credentials = null;
  private URL occiURL = null;
  private URL keystoneURL = null;
  private String tenant = null;
  private String tokenID = null;
  private String version = null;
  JsonParser jsonParser = null;
  
  JsonObject token = null;
  
  
  String tokenJsonBodyQuery;
  
  
  
  public OCCIAuthKeystoneImpl(URL oURL, String tenant, Element auth) throws MalformedURLException
  {
                                                                                    
                                                                       
       this.occiURL = oURL;
       this.keystoneURL = 
                    new URL(auth.getChildText("protocol"), auth.getChildText("host"),
                            Integer.parseInt(auth.getChildText("port")), 
                            "/");
       this.tenant = tenant;
       this.credentials = new UsernamePasswordCredentials(auth.getChildText("username"),
                                                          auth.getChildText("password"));
       this.version = auth.getChildText("version");
       
       jsonParser = new JsonParser();
       
       Authorization a = new Authorization(tenant, credentials.getUserName(), credentials.getPassword());
       Gson gson = new Gson();
       this.tokenJsonBodyQuery = gson.toJson(a); 
                                                                                    
  }
  
  
  
  public OCCIAuthKeystoneImpl(URL occiURL, URL keystoneURL, String ver, UsernamePasswordCredentials credentials, String tenant) {
    this.occiURL = occiURL;
    this.keystoneURL = keystoneURL;
    this.credentials = credentials;
    this.tenant = tenant;
    this.version = ver;
  }

  
  
  /******************* Private methods **********************/
   
  
  private String createToken() {
      
    String result = null;

    HttpClient httpClient = new DefaultHttpClient();
    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
    HttpPost httpPost = new HttpPost(keystoneURL.toString() + "/" + this.version + "/tokens"); 

    httpPost.addHeader("Accept", "application/json");
//    httpPost.addHeader("Content-Type", "application/xml");
//
//    String bodyxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//        + "<auth xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
//        + "xmlns=\"http://docs.openstack.org/identity/api/"+ this.version + "\""
//        + "tenantName=\"" + tenant + "\">"
//        + "<passwordCredentials username=\"" + credentials.getUserName() + "\" password=\"" + credentials.getPassword() + "\"/>"
//        + "</auth>";

    httpPost.addHeader("Content-Type", "application/json");
    
    
    
    
    StringEntity entity = null;

    try {
      entity = new StringEntity(this.tokenJsonBodyQuery, "UTF-8");
      entity.setContentType("application/json");
      httpPost.setEntity(entity);

      HttpResponse response = httpClient.execute(httpPost);

      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
      {
          //error
          tokenID = null;
          token=null;
          return null;
      }
      
  
      JsonObject responseobj = this.jsonParser.parse(new InputStreamReader(response.getEntity().getContent())).getAsJsonObject();
      this.token = responseobj.get("access").getAsJsonObject().get("token").getAsJsonObject();
      result = this.token.get("id").getAsString();
      

    } catch (IOException ex) {
      log.error("Error in token creation : " + ex.getMessage());
      
    } 
    
    return tokenID = result;
  }

  private boolean isExpired() {

    if (tokenID == null) {
      return true;
    }

    HttpClient httpClient = new DefaultHttpClient();
    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
    HttpGet httpget = new HttpGet(occiURL.toString() + "/-/"); 
    httpget.addHeader("X-Auth-Token", tokenID);
 
    HttpResponse response;
    boolean result = true; //if error token is expired
    try {
      log.debug("Testing token ...");
      response = httpClient.execute(httpget);
      result = (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK);
    } catch (IOException ex) {
      log.error("Error  : " + ex.getMessage());
    }
    
    return result;
  }
  
  
  
  
  
  
  /*************** Public methods **********************/
  
  
  
  
  @Override
  public boolean doAuth(HttpRequest request) {

    if (isExpired()) {
      log.debug("Token expired !!! Creating it ...");
      createToken();
    }

    if (tokenID != null) {
        log.debug("Token valid !!! I'll use it ...");
      request.addHeader("X-Auth-Token", tokenID);

    }
    return tokenID != null;
  }

   
}
