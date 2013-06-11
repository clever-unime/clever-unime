package org.clever.HostManager.HyperVisorPlugins.OCCI;


import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.jdom.Element;

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
  private String token = null;
  private String version = null;
  
  
  
  
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
                                                                                    
  }
  
  
  
  public OCCIAuthKeystoneImpl(URL occiURL, URL keystoneURL, String ver, UsernamePasswordCredentials credentials, String tenant) {
    this.occiURL = occiURL;
    this.keystoneURL = keystoneURL;
    this.credentials = credentials;
    this.tenant = tenant;
    this.version = ver;
  }

  @Override
  public boolean doAuth(HttpRequest request) {

    if (isExpired()) {
      log.debug("Token expired !!! Creating it ...");
      createToken();
    }

    if (token != null) {
        log.debug("Token valid !!! I'm using it ...");
      request.addHeader("X-Auth-Token", token);

    }
    return token != null;
  }

  private String createToken() {

    String result = null;

    HttpClient httpClient = new DefaultHttpClient();
    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
    HttpPost httpPost = new HttpPost(keystoneURL.toString() + "/" + this.version + "/tokens"); 

    httpPost.addHeader("Accept", "application/xml");
    httpPost.addHeader("Content-Type", "application/xml");

    String bodyxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<auth xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
        + "xmlns=\"http://docs.openstack.org/identity/api/"+ this.version + "\""
        + "tenantName=\"" + tenant + "\">"
        + "<passwordCredentials username=\"" + credentials.getUserName() + "\" password=\"" + credentials.getPassword() + "\"/>"
        + "</auth>";

    StringEntity entity = null;

    try {
      entity = new StringEntity(bodyxml, "UTF-8");
      entity.setContentType("application/xml");
      httpPost.setEntity(entity);

      HttpResponse response = httpClient.execute(httpPost);
      //TODO: controllare
      StringWriter writer = new StringWriter();
      IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
      result = writer.toString();

    } catch (IOException ex) {
      log.error("Error in token creation : " + ex.getMessage());
      
    }
    return token = result;
  }

  private boolean isExpired() {

    if (token == null) {
      return true;
    }

    HttpClient httpClient = new DefaultHttpClient();
    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
    HttpGet httpget = new HttpGet(occiURL.toString() + "/-/"); 
    httpget.addHeader("X-Auth-Token", token);

    HttpResponse response;
    boolean result = false;
    try {
      response = httpClient.execute(httpget);
      result = response.getStatusLine().getStatusCode() == 200;
    } catch (IOException ex) {
      log.error("Error  : " + ex.getMessage());
    }
    return result;
  }
}
