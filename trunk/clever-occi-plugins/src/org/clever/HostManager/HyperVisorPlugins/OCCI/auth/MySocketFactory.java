/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI.auth;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.Certificate;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;


/**
 *
 * @author maurizio
 */
public class MySocketFactory {
    SchemeSocketFactory socketFactory;
    SSLContext sslContext = SSLContext.getInstance("TLS");
    Logger log = Logger.getLogger(MySocketFactory.class);
    private final String protocol;
    private final boolean acceptAllCertificates;

    public MySocketFactory(String protocol, boolean aac) throws 
                                            NoSuchAlgorithmException, 
                                            KeyManagementException,
                                            KeyStoreException,
                                            UnrecoverableKeyException 
    {
        this.protocol = protocol;
        this.acceptAllCertificates = aac;
        if(protocol.equalsIgnoreCase("http"))
        {
            socketFactory = PlainSocketFactory.getSocketFactory();
        }
        else if(!aac)
        {
            socketFactory = SSLSocketFactory.getSystemSocketFactory();
        }
        else
        {
            log.debug("Acceptallcerttrue ");
               socketFactory = this.createAcceptAllCertificatesSocketFactory(null);
        }
    }

    
    private SSLSocketFactory createAcceptAllCertificatesSocketFactory(KeyManager[] keystore) throws KeyManagementException
    {
        
         log.debug("Creation allcertificatessocketfactory with trustmanager: " + (keystore==null?"null":keystore[0]));
         TrustManager tm = new X509TrustManager() {
                    Set<X509Certificate> acceptedissuers = new HashSet<X509Certificate>();
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            log.debug("checking cert trusted " + chain);
                            for (X509Certificate c : chain)
                            {
                                log.debug("check client trusted : " + c);
                            }
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            for (X509Certificate c : chain)
                            {
                                log.debug("check server trusted : " + c);
                                acceptedissuers.add(c);
                            }
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                           log.debug("returning accepted issuers "+ acceptedissuers.toArray(new X509Certificate[0]).length );
                           
                            return acceptedissuers.toArray(new X509Certificate[0]);
                        }
                    
                };

                this.sslContext.init(keystore, new TrustManager[] { tm }, null);

                return new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }
    
    
    
    public void addClientCertificate(InputStream icert) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, IOException
    {
        if(protocol.equalsIgnoreCase("http"))
        {
            log.error("Add a x509 certificate is not possible with plain http protocol");
            return;
        }
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        
        java.security.cert.Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(icert);
        keystore.load(null, null);
       
        keystore.setCertificateEntry("client_cert", cert);
        
        if(this.acceptAllCertificates)
        {
            log.debug("KeyManager creation with alghoritm: " + KeyManagerFactory.getDefaultAlgorithm());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keystore, null);
            socketFactory = this.createAcceptAllCertificatesSocketFactory(kmf.getKeyManagers());
        }
        else
        {
            socketFactory = new SSLSocketFactory(keystore);
        }
        
        
    }
    
    
    
    public SchemeSocketFactory getSocketFactory()
    {
        return socketFactory;
    }
    
    
    
    /*
    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        log.debug("Creato socket per " + host);
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        log.debug("creato socket");
        return sslContext.getSocketFactory().createSocket();
    }
    * */
}
