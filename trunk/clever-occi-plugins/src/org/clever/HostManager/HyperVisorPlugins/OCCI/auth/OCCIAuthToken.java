package org.clever.HostManager.HyperVisorPlugins.OCCI.auth;


import org.apache.http.HttpRequest;
import org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils.HttpClientFactory;
import org.jdom.Element;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author maurizio
 */
public class OCCIAuthToken implements OCCIAuthImpl {
 
   private final String token;

   
   
  public OCCIAuthToken(Element auth) {
      this.token = auth.getChildText("token");
  }
   
   
  @Override
  public boolean doAuth(HttpRequest request) {
   
      request.addHeader("Auth-Token" , this.token);
      return true;
  }

    @Override
    public void initClient(HttpClientFactory client) {
        
    }
  
}
