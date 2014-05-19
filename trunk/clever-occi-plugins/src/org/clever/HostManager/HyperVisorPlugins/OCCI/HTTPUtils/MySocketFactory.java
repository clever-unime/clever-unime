/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils;

import com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyPair;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.clever.HostManager.HyperVisorPlugins.OCCI.fileutils.InputStreamSplitter;


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
    private List<X509Certificate> certs;

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

    
    private SSLSocketFactory createAcceptAllCertificatesSocketFactory(KeyManager[] keyManagers) throws KeyManagementException, NoSuchAlgorithmException
    {
        
         
         TrustManager tm = new X509ExtendedTrustManager() {
             Set<X509Certificate> acceptedissuers = new HashSet<X509Certificate>();
             @Override
             public void checkClientTrusted(X509Certificate[] xcs, String string, String string1, String string2) throws CertificateException {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public void checkServerTrusted(X509Certificate[] xcs, String string, String string1, String string2) throws CertificateException {
                for (X509Certificate c : xcs)
                            {
                                //log.debug("check server trusted : " + c);
                                 acceptedissuers.add(c);
                            }
             }

             @Override
             public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                 throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }

             @Override
             public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                  for (X509Certificate c : xcs)
                            {
                                //log.debug("check server trusted : " + c);
                                 acceptedissuers.add(c);
                            }
             }

             @Override
             public X509Certificate[] getAcceptedIssuers() {
                log.debug("returning accepted issuers "+ acceptedissuers.toArray(new X509Certificate[0]).length );
                           
                            return acceptedissuers.toArray(new X509Certificate[0]);
             }
         };
         
         
         
         
                sslContext = SSLContext.getInstance("TLS");
         
                this.sslContext.init(keyManagers, new TrustManager[] { tm }, null);

                return new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }
    
    private KeyStore loadKeystore(InputStream icerts) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, InvalidKeySpecException
    {
        certs = new ArrayList<X509Certificate>();
        Key key = null;
        final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null, null);
        int i=0;
        log.debug("Certs parsing ...");
        for (String cert : InputStreamSplitter.split(icerts, ".*BEGIN.*", ".*END.*",true))
        {
           
            String certtrim = cert.replaceAll("[\r\n]", "");
             log.debug(certtrim);
            if(certtrim.matches(".*CERTIFICATE.*"))
           
            {
                log.debug("Storing certificate");
                certs.add(
                        ((X509Certificate)CertificateFactory.
                                                getInstance("X.509").
                                                generateCertificate(new ByteArrayInputStream(cert.getBytes()))
                        )
                     );
               
                
            }
            else 
            if(certtrim.matches(".*KEY.*"))
            {
                log.debug("Storing key");
                
                if (Security.getProvider("BC") == null) {
                    Security.addProvider(new BouncyCastleProvider());
                }
                //PEMReader pemReader = new PEMReader(new StringReader(cert));
                PEMParser pemParser = new PEMParser(new StringReader(cert));
                
                
                
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                KeyPair keyPair = converter.getKeyPair((PEMKeyPair) pemParser.readObject());
                key = keyPair.getPrivate();

                
            }
            
        }
        
        keystore.setKeyEntry("proxycert", key, "".toCharArray(), certs.toArray(new Certificate[1]));
       // InputStream i = new FileInputStream(new File("keystore.jks"));
       // keystore.load(i, "123456".toCharArray());
        return keystore;
    }
    
    public void addClientCertificate(InputStream icert) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, IOException, InvalidKeySpecException
    {
        if(protocol.equalsIgnoreCase("http"))
        {
            log.error("Add a x509 certificate is not possible with plain http protocol");
            return;
        }
        
        
        final KeyStore keystore = this.loadKeystore(icert);
    
        if(this.acceptAllCertificates)
        {
            log.debug("KeyManager creation with alghoritm: " + KeyManagerFactory.getDefaultAlgorithm());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keystore, "".toCharArray());
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
    
    public Date getCertExpirationDate()
    {
        return certs.get(0).getNotAfter();
    }
    
    
    
   
}
