package org.clever.HostManager.HyperVisorPlugins.OCCI.auth;


import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.jdom.Element;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author salvullo
 */
public class OCCINoAuth implements OCCIAuthImpl {
 private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OCCINoAuth.class);  
    private final URL occiURL;
   

   public OCCINoAuth(URL occi)  {
    super();
    occiURL = occi;
}
   
   
  @Override
  public boolean doAuth(HttpRequest request) {
   
    return !isExpired();
  }
  private boolean isExpired() {
   
    HttpClient httpClient = new DefaultHttpClient();
    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
    HttpGet httpget = new HttpGet(occiURL.toString() + "/-/"); 
   
 
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

    @Override
    public void initClient(HttpClient client) {
        
    }
}
