package org.clever.HostManager.HyperVisorPlugins.OCCI.auth;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils.HttpClientFactory;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;




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
  final JsonParser jsonParser;
  
  JsonObject token = null;
  
  
  final String tokenJsonBodyQuery;
    private DateTime expire;
  
  
  
  public OCCIAuthKeystoneImpl(URL oURL, String tenant, Element auth) throws MalformedURLException
  {
                                                                                    
                                                                       
      
     
       this(oURL,
            new URL(auth.getChildText("protocol"), auth.getChildText("host"),
                            Integer.parseInt(auth.getChildText("port")), 
                            "/"),
             auth.getChildText("version"),
             new UsernamePasswordCredentials(auth.getChildText("username"),
                                                          auth.getChildText("password")),
             tenant);
       
       
                                                                                    
  }
  
  
  
  public OCCIAuthKeystoneImpl(URL occiURL, URL keystoneURL, String ver, UsernamePasswordCredentials credentials, String tenant) {
    this.occiURL = occiURL;
    this.keystoneURL = keystoneURL;
    this.credentials = credentials;
    this.tenant = tenant;
    this.version = ver;
    jsonParser = new JsonParser();
    Authorization a = new Authorization(tenant, credentials.getUserName(), credentials.getPassword());
    Gson gson = new Gson();
    this.tokenJsonBodyQuery = gson.toJson(a); 
  }

  
  
  /******************* Private methods **********************/
   
  
  private String createToken() {
      
    String result = null;

    HttpClient httpClient = new DefaultHttpClient();
    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
    HttpPost httpPost = new HttpPost(keystoneURL.toString() + "/" + this.version + "/tokens"); 

    httpPost.addHeader("Accept", "application/json");
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
      synchronized(this)
      {
        this.token = responseobj.get("access").getAsJsonObject().get("token").getAsJsonObject();
      
        expire = DateTime.parse(token.get("expires").getAsString(),ISODateTimeFormat.dateTimeNoMillis());
        
        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"); 
        //2013-07-10T11:00:11Z
        //expire.setTime(df.parse(token.get("expires").getAsString()));


        log.debug("Token will expire on: " + expire);

        result = this.token.get("id").getAsString();
      }

    } catch (IOException ex) {
      log.error("Error in token creation : " + ex.getMessage());
      
    }  
    
    return tokenID = result;
  }


  private boolean isExpired() {
    
    synchronized(this)
    {
        if (tokenID == null) {
          return true;
        }
        return expire.isBeforeNow();
    }
   
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
    synchronized(this)
    {
        return tokenID != null;
    }
  }

    @Override
    public void initClient(HttpClientFactory client) {
        
    }

   

   
}
