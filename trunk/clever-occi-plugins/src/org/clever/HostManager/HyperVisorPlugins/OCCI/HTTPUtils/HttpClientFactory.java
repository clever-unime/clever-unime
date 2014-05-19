/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

/**
 *
 * @author maurizio
 */
public class HttpClientFactory {

    private PoolingClientConnectionManager poolingManager;
    Logger logger = Logger.getLogger(HttpClientFactory.class);
    HttpParams params;
    private boolean acceptAllCertificate;
    private final Integer[] ports;
    private final String protocol;
    private final MySocketFactory sf;
    
    /**
     *
     * @param aac Accept All Certificates: the http client accepts all server
     * certificates (also self-signed cert)
     * @param ports ()
     */
    public HttpClientFactory(String protocol, boolean aac, Integer[] ports, int conTimeout,int soTimeout) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {

        
        
        
        this.acceptAllCertificate = aac;
        this.ports = ports;
        this.protocol = protocol;
        DefaultHttpClient client = new DefaultHttpClient();

        //ClientConnectionManager mgr = client.getConnectionManager();

        params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, conTimeout); 
        HttpConnectionParams.setSoTimeout(params, soTimeout); 


        logger.debug("Http params: " + params);



        sf = new MySocketFactory(protocol, aac);



        poolingManager = new PoolingClientConnectionManager(this.createRegistry(sf.getSocketFactory()));
        poolingManager.setDefaultMaxPerRoute(20);
        poolingManager.setMaxTotal(200);

        logger.debug("HTTPCLientFactory created");

    }

    
    
    
    //TODO: gestire meglio la concorrenza tra questo metodo e getThreadSafeClient
    public synchronized void addClientCertificate(InputStream icert) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, IOException, InvalidKeySpecException {
        synchronized( poolingManager )
        {
            sf.addClientCertificate(icert);
            poolingManager.closeExpiredConnections();
            poolingManager = new PoolingClientConnectionManager(this.createRegistry(sf.getSocketFactory()));
            poolingManager.setDefaultMaxPerRoute(20);
            poolingManager.setMaxTotal(200);
        }
    }

    private SchemeRegistry createRegistry(SchemeSocketFactory sf) {
        SchemeRegistry registry = new SchemeRegistry();
        for (Integer port : ports) {
            registry.register(new Scheme(protocol, port, sf));
        }
        return registry;

    }

    public HttpClient getThreadSafeClient() {
        synchronized( poolingManager )
        {
            return new DefaultHttpClient(poolingManager, params);
        }
       //return new AutoRetryHttpClient(new DefaultHttpClient(poolingManager, params)); //TODO: usare autoreplyhttp client (funziona) per gestire i casi di http error
    }
    
    
    public Date getClientCertExpirationDate()
    {
        return sf.getCertExpirationDate();
    }
    
    
}
