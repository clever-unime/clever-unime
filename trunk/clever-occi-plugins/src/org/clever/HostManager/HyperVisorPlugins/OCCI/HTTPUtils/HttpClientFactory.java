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
import org.clever.HostManager.HyperVisorPlugins.OCCI.auth.MySSLSocketFactory;

/**
 *
 * @author maurizio
 */
public class HttpClientFactory {

   
    private final PoolingClientConnectionManager tm;
    
    
    
    public HttpClientFactory()
    {
        DefaultHttpClient client = new DefaultHttpClient();

        //ClientConnectionManager mgr = client.getConnectionManager();

        HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000); //TODO: from plugin params
        HttpConnectionParams.setSoTimeout(params, 10000); //TODO: from plugin params
        
        //qui vanno messi i parametri come autenticazione x509




        //Per accettare tutti i certificati x509 (certificati unverified)
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException ex) {
            java.util.logging.Logger.getLogger(HttpClientFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            trustStore.load(null, null);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(HttpClientFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            java.util.logging.Logger.getLogger(HttpClientFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            java.util.logging.Logger.getLogger(HttpClientFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        SSLSocketFactory sf = null;
        try {
            
            sf = new MySSLSocketFactory().getSSLSocketFactory();
        } catch (NoSuchAlgorithmException ex) {
            java.util.logging.Logger.getLogger(HttpClientFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            java.util.logging.Logger.getLogger(HttpClientFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            java.util.logging.Logger.getLogger(HttpClientFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            java.util.logging.Logger.getLogger(HttpClientFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        registry.register(new Scheme("https", 3200, sf)); //adhoc per opennebula spagnolo ciemat
       //registry.register(new Scheme("https", sf, 3000)); //adhoc per opennebula infnmat
        ///////////////////////////////////


        //ThreadSafeClientConnManager tm = new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry());
        tm = new PoolingClientConnectionManager(registry);
        tm.setDefaultMaxPerRoute(20);

        tm.setMaxTotal(200);

        

    }
    
    
    
    public DefaultHttpClient getThreadSafeClient() {

        return new DefaultHttpClient(tm);
    }
}

