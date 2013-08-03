/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI.auth;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;


/**
 *
 * @author maurizio
 */
public class MySocketFactory {
    final SchemeSocketFactory socketFactory;
    SSLContext sslContext = SSLContext.getInstance("TLS");
    Logger log = Logger.getLogger(MySocketFactory.class);

    public MySocketFactory(String protocol, boolean aac) throws 
                                            NoSuchAlgorithmException, 
                                            KeyManagementException,
                                            KeyStoreException,
                                            UnrecoverableKeyException 
    {
       
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

                TrustManager tm = new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                };

                this.sslContext.init(null, new TrustManager[] { tm }, null);

                socketFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
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
