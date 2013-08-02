/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.clever.HostManager.HyperVisorPlugins.OCCI.HvOCCI;
import org.clever.HostManager.HyperVisorPlugins.OCCI.auth.MySSLSocketFactory;

/**
 *
 * @author maurizio
 */
public class HttpClientFactory {

   
    private final PoolingClientConnectionManager tm;
    Logger logger = Logger.getLogger(HttpClientFactory.class);
    HttpParams params;
    
    public HttpClientFactory()
    {
        DefaultHttpClient client = new DefaultHttpClient();

        //ClientConnectionManager mgr = client.getConnectionManager();

         params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000); //TODO: from plugin params
        HttpConnectionParams.setSoTimeout(params, 10000); //TODO: from plugin params
        
        //qui vanno messi i parametri come autenticazione x509

        logger.debug("Http params: " + params);


        //Per accettare tutti i certificati x509 (certificati unverified)
        KeyStore trustStore = null;
        try {
            logger.debug("trustStore creating ...");
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException ex) {
            logger.error(ex);
        }
        try {
            trustStore.load(null, null);
        } catch (IOException ex) {
            logger.error(ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.error(ex);
        } catch (CertificateException ex) {
            logger.error(ex);
        }
        logger.debug("SSLSocketFactory creating ...");
        SSLSocketFactory sf = null;
        try {
            
            sf = new MySSLSocketFactory().getSSLSocketFactory();
        } catch (NoSuchAlgorithmException ex) {
            logger.error(ex);
        } catch (KeyManagementException ex) {
            logger.error(ex);
        } catch (KeyStoreException ex) {
           logger.error(ex);
        } catch (UnrecoverableKeyException ex) {
            logger.error(ex);
        }
        
 logger.debug("Registry creating ...");
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        registry.register(new Scheme("https", 3200, sf)); //adhoc per opennebula spagnolo ciemat
       //registry.register(new Scheme("https", sf, 3000)); //adhoc per opennebula infnmat
        ///////////////////////////////////


        //ThreadSafeClientConnManager tm = new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry());
         logger.debug("PoolingClientConnectionManager creating ...");
        tm = new PoolingClientConnectionManager(registry);
       // tm = new ThreadSafeClientConnManager();
           logger.debug("PoolingClientConnectionManager created");
        tm.setDefaultMaxPerRoute(20);

        tm.setMaxTotal(200);

         logger.debug("HTTPCLientFactory created");

    }
    
    
    
    public DefaultHttpClient getThreadSafeClient() {

        return new DefaultHttpClient(tm,params);
    }
}

