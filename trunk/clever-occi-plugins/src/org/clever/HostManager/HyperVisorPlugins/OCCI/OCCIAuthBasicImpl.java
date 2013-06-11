package org.clever.HostManager.HyperVisorPlugins.OCCI;


import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.jdom.Element;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author salvullo
 */
public class OCCIAuthBasicImpl implements OCCIAuthImpl {
 
   private final UsernamePasswordCredentials credentials;

   public OCCIAuthBasicImpl(UsernamePasswordCredentials credentials) {
    
    this.credentials = credentials;

  }
   
  public OCCIAuthBasicImpl(Element auth) {
      this.credentials = new UsernamePasswordCredentials(auth.getChildText("username"),
                                                          auth.getChildText("password"));
  }
   
   
  @Override
  public boolean doAuth(HttpRequest request) {
    try {
      request.addHeader(new BasicScheme().authenticate(credentials, request));
    } catch (AuthenticationException ex) {
      Logger.getLogger(OCCIAuthBasicImpl.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    return true;
  }
  
}
