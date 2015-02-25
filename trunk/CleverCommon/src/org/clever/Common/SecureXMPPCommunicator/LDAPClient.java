package org.clever.Common.SecureXMPPCommunicator;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.logging.Level;
import org.apache.log4j.*;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Alessandro La Bella
 */
public class LDAPClient {
    
    private DirContext dirContext = null;
    private String server;
    private int port;
    private String rootContext;
    private String rootdn;
    private String password;
    
    private String cfgTemplatePath;
    private InputStream inxml;
    private ParserXML pXML;
    private Logger logger;
    
    public LDAPClient() throws FileNotFoundException {
        logger = Logger.getLogger(this.getClass());
        logger.debug("Creazione LDAPClient");
        init();
        this.connect();
    }
    
    public LDAPClient(String server, int port, String rootContext, String rootdn, String password){
        this.server = server;
        this.port = port;
        this.rootContext = rootContext;
        this.rootdn = rootdn;
        this.password = password;
        logger = Logger.getLogger(this.getClass());
        logger.debug("Creazione LDAPClient");
        this.connect();
    }

    private void init() throws FileNotFoundException{
        
        try {
            cfgTemplatePath = "./cfg/configuration_ldapClient.xml";
            logger.debug("Reading configuration from file " + cfgTemplatePath);
            inxml = new FileInputStream(cfgTemplatePath);
        } catch (FileNotFoundException ex) {
            logger.error("Configuration file not found ", ex);
            throw ex;
        }
        
        FileStreamer fs = new FileStreamer();        	
        try {       
            pXML = new ParserXML( fs.xmlToString( inxml ) );
        } catch (IOException ex) {
            logger.error("Parser error", ex);
        }
        server = pXML.getElementContent("server");
        port = Integer.parseInt(pXML.getElementContent("port"));
        rootContext = pXML.getElementContent("rootContext");
        rootdn = pXML.getElementContent("rootdn");
        password = pXML.getElementContent("password");
        
        try {
            inxml.close();
        } catch (IOException ex) {
            logger.error("IO Error", ex);
        }
    }

    private void connect(){
        logger.debug("connecting LDAP");
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL,"ldap://"+this.server +":"+this.port+"/"+this.rootContext);
        env.put(Context.REFERRAL,"throw");
        env.put(Context.SECURITY_CREDENTIALS,this.password);
        env.put(Context.SECURITY_PRINCIPAL,this.rootdn);
        try {
            dirContext = new InitialDirContext(env);
        } catch (NamingException ex) {
            logger.error(ex);
        }
        logger.debug("Connected to LDAP");
    }
    
    public void disconnect(){
        try{
            if (dirContext != null){
                dirContext.close();
		dirContext=null;
            }
        }
        catch (NamingException e){
            System.out.println(e);
        }
    }
    
    public void insertEntry(String CN, String SN,String UID, String PWD, String OU, byte[] cert) {
        //input = new FileInputStream(pathCert);
        //X509Certificate cert = X509Certificate.getInstance(input);

        //byte[] userCert = null;
        //byte[] userPubKey = cert.getPublicKey().getEncoded();
        //userCert = cert.getEncoded();

        BasicAttributes matchAttrs = new BasicAttributes(true);
        matchAttrs.put(new BasicAttribute("ou",OU));
        matchAttrs.put(new BasicAttribute("cn",CN));
        matchAttrs.put(new BasicAttribute("sn",SN));
        matchAttrs.put(new BasicAttribute("uid",UID));
        matchAttrs.put(new BasicAttribute("userpassword",PWD));
        matchAttrs.put(new BasicAttribute("usercertificate;binary",cert));

        matchAttrs.put(new BasicAttribute("objectclass", "top"));
        matchAttrs.put(new BasicAttribute("objectclass", "person"));
        matchAttrs.put(new BasicAttribute("objectclass", "organizationalPerson"));
        matchAttrs.put(new BasicAttribute("objectclass","inetorgperson"));

        InitialDirContext iniDirContext = (InitialDirContext)dirContext;
        try{
            iniDirContext.bind("uid="+UID, dirContext, matchAttrs);
        }
        catch(NamingException ne){
            System.out.println(ne.toString());
        }
            
    }
    
    public synchronized X509Certificate searchCert(String base, String filter){
        Security.addProvider(new BouncyCastleProvider());
        X509Certificate cert = null;
        boolean found = false;
        
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
        NamingEnumeration ne = null;
        try {
            ne = dirContext.search(base, filter, sc);
        } catch (NamingException ex) {
            logger.error(ex);
        }
        byte[] certBytes = null;
        try {
            while(ne.hasMore()){
                found = true;
                SearchResult sr = (SearchResult)ne.next();
                certBytes = (byte[]) sr.getAttributes().get("usercertificate;binary").get();
            }
        } catch (NamingException ex) {
            logger.error(ex);
        }
        if(!found){
            System.out.println(filter+" not found");
        }
        else{
            if(certBytes == null){
                System.out.println("Certificate not found");
            }else{
                logger.debug("Certificate founded");
                CertificateFactory certFactory = null;
                try {
                    certFactory = CertificateFactory.getInstance("X.509", "BC");
                } catch (CertificateException ex) {
                   logger.error("Certificate error", ex);
                } catch (NoSuchProviderException ex) {
                    logger.error("Provider not found", ex);
                }
                InputStream in = new ByteArrayInputStream(certBytes);
                try {
                    cert = (X509Certificate)certFactory.generateCertificate(in);
                } catch (CertificateException ex) {
                    logger.error("Certificate exception", ex);
                }
                try {
                    cert.checkValidity();
                } catch (CertificateExpiredException ex) {
                    logger.error("Certificate expired", ex);
                } catch (CertificateNotYetValidException ex) {
                    logger.error("Certificate not valid", ex);
                }
            }
        }
        return cert;
    }
    
    /*
    public void searchTest(String base, String filter)
        throws NamingException, javax.security.cert.CertificateException{
        
        boolean found = false;
        
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
        NamingEnumeration ne = null;
        ne = dirContext.search(base, filter, sc);
        byte[] certBytes = null;
        while(ne.hasMore()){
            found = true;
            SearchResult sr = (SearchResult)ne.next();
            certBytes = (byte[]) sr.getAttributes().get("usercertificate;binary").get();
        }
        if(!found){
            System.out.println(filter+" not found");
        }
        else{
            if(certBytes == null){
                System.out.println("Certificate not found");
            }else{
                X509Certificate cert = X509Certificate.getInstance(certBytes);
                cert.checkValidity();
                System.out.println("Public Key: "+cert.getPublicKey().toString());
            }
        }
    }
    */
}
