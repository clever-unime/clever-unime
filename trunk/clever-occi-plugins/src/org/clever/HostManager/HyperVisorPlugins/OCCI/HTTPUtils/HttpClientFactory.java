/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.clever.HostManager.HyperVisorPlugins.OCCI.auth.MySocketFactory;

/**
 *
 * @author maurizio
 */
public class HttpClientFactory {

   
    private final PoolingClientConnectionManager poolingManager;
    Logger logger = Logger.getLogger(HttpClientFactory.class);
    HttpParams params;
    private boolean acceptAllCertificate; 
    private final Integer[] ports;
    private final String protocol;
    
    
    
    /**
     * 
     * @param aac Accept All Certificates: the http client accepts all server certificates (also self-signed cert)
     * @param ports ()
     */
    public HttpClientFactory(String protocol , boolean aac, Integer [] ports)
    {
        
        this.acceptAllCertificate = aac;
        this.ports = ports;
        this.protocol = protocol;
        DefaultHttpClient client = new DefaultHttpClient();

        //ClientConnectionManager mgr = client.getConnectionManager();

        params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000); //TODO: from plugin params
        HttpConnectionParams.setSoTimeout(params, 10000); //TODO: from plugin params
       
        
        logger.debug("Http params: " + params);
        SchemeSocketFactory sf = null;
        try {
           
            sf = new MySocketFactory(protocol, aac).getSocketFactory();
            
            
        } catch (NoSuchAlgorithmException ex) {
            logger.error(ex);
        } catch (KeyManagementException ex) {
            logger.error(ex);
        } catch (KeyStoreException ex) {
           logger.error(ex);
        } catch (UnrecoverableKeyException ex) {
            logger.error(ex);
        }
         poolingManager = new PoolingClientConnectionManager(this.createRegistry(sf));
         poolingManager.setDefaultMaxPerRoute(20);
         poolingManager.setMaxTotal(200);

        logger.debug("HTTPCLientFactory created");

    }
    
    private SchemeRegistry createRegistry(SchemeSocketFactory sf)
    {
         SchemeRegistry registry = new SchemeRegistry();
         
        
         for(Integer port : ports)
                {
                   registry.register(new Scheme(protocol, port, sf)); 
                }
         /*if(this.acceptAllCertificate)
         {
                for(Integer port : ports)
                {
                   registry.register(new Scheme("https", port, sf)); 
                }
         }*/
         
         
         return registry;
        
    }
    
    public DefaultHttpClient getThreadSafeClient() {

        return new DefaultHttpClient(poolingManager,params);
    }
}

