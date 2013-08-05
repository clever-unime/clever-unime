package org.clever.HostManager.HyperVisorPlugins.OCCI.auth;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpRequest;
import org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils.HttpClientFactory;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author salvullo
 */
public class OCCIAuthX509Impl implements OCCIAuthImpl {
 private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OCCIAuthX509Impl.class);  
    private final String certpath;
   
   

   public OCCIAuthX509Impl(String cert)  {
    super();
    certpath = cert;
}
   
   
  @Override
  public boolean doAuth(HttpRequest request) {
   
    return !isExpired();
  }
  private boolean isExpired() {
   
   
    
    return false;
    
    
  }

    @Override
    public void initClient(HttpClientFactory client) {
        File f = new File(certpath);
     try {
         client.addClientCertificate(new FileInputStream(f));
         log.debug("cetificato aggiunto: " + certpath);
     } catch (KeyStoreException ex) {
         log.error(ex);
     } catch (CertificateException ex) {
         log.error(ex);
     } catch (NoSuchAlgorithmException ex) {
         log.error(ex);
     } catch (KeyManagementException ex) {
         log.error(ex);
     } catch (UnrecoverableKeyException ex) {
         log.error(ex);
     } catch (FileNotFoundException ex) {
         log.error(ex);
     } catch (IOException ex) {
         log.error(ex);
     }
    }
}
