/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.Common.SecureXMPPCommunicator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class implements encryption/decryption/signing/verifying of String data
 * using X509 format key. Funziona con la libreria bcprov-jkd14-147.jar
 *
 * @author Alessandro La Bella
 */
public class X509Utils {

    private String keystorePath;
    private String alias;
    private char[] keystorePassword;
    private org.apache.log4j.Logger logger;
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private String cfgTemplatePath;
    private InputStream inxml;
    private ParserXML pXML;

    private void init(boolean isCm) {
        try {
            cfgTemplatePath = "cfg/configuration_keystore.xml";
            inxml = new FileInputStream(cfgTemplatePath);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LDAPClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        FileStreamer fs = new FileStreamer();
        try {
            pXML = new ParserXML(fs.xmlToString(inxml));
        } catch (IOException ex) {
            Logger.getLogger(LDAPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        String root = isCm ? "cm" : "user";
        
        keystorePath = pXML.getElementInStructure(root).getChildText("keystorePath");
        alias = pXML.getElementInStructure(root).getChildText("alias");
        keystorePassword = pXML.getElementInStructure(root).getChildText("keystorePassword").toCharArray();
        
        try {
            inxml.close();
        } catch (IOException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public X509Utils(boolean isCm) {
        init(isCm);
    }

    public X509Utils(String keystorePath, String alias, char[] keystorePassword) {

        this.keystorePath = keystorePath;

        this.alias = alias;

        this.keystorePassword = keystorePassword;

        logger = org.apache.log4j.Logger.getLogger("X509Utils");
    }

    public String getKeystorePath() {
        return this.keystorePath;
    }

    private static PrivateKey getPrivateKey(String KSPath, char[] KSPassword, String alias, char[] password) throws KeyStoreException, NoSuchProviderException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        InputStream inputStream = null;

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        KeyStore ks = KeyStore.getInstance("pkcs12", "BC");
        inputStream = new FileInputStream(KSPath);
        ks.load(inputStream, KSPassword);

        Key key = ks.getKey(alias, password);
        return (PrivateKey) key;

    }

    private static PublicKey getPublicKey(String KSPath, char[] KSPassword, String alias) throws KeyStoreException, NoSuchProviderException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        InputStream inputStream = null;

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        KeyStore ks = KeyStore.getInstance("pkcs12", "BC");
        inputStream = new FileInputStream(KSPath);
        ks.load(inputStream, KSPassword);

        Certificate cert = ks.getCertificate(alias);
        PublicKey pubKey = cert.getPublicKey();

        return pubKey;

    }

    public PublicKey extractPublicKey() throws KeyStoreException, NoSuchProviderException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        InputStream inputStream = null;

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        KeyStore ks = KeyStore.getInstance("pkcs12", "BC");
        inputStream = new FileInputStream(this.keystorePath);
        ks.load(inputStream, this.keystorePassword);

        Certificate cert = ks.getCertificate(this.alias);
        PublicKey pubKey = cert.getPublicKey();

        return pubKey;

    }

    public String encryptToString(String inpstr) {
        try {

            PrivateKey privKey = getPrivateKey(keystorePath, keystorePassword, alias, this.keystorePassword);
            return encryptToString(inpstr, privKey);
        } catch (KeyStoreException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public String encryptToString(String inpstr, Key key) {

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        byte[] encodedBytes = null;
        String encoded = null;

        try {

            Cipher encryptCipher = Cipher.getInstance("RSA", provider);

            encryptCipher.init(Cipher.ENCRYPT_MODE, key);

            encodedBytes = encryptCipher.doFinal(inpstr.getBytes());

            encoded = new String(Base64.encode(encodedBytes));

        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return encoded;
    }

    public String encryptToString(String inpstr, RSAPublicKey pubbKey) {

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        byte[] encodedBytes = null;
        String encoded = null;

        try {

            Cipher encryptCipher = Cipher.getInstance("RSA", provider);

            encryptCipher.init(Cipher.ENCRYPT_MODE, pubbKey);

            encodedBytes = encryptCipher.doFinal(inpstr.getBytes());

            encoded = new String(Hex.encode(encodedBytes));

        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return encoded;
    }

    public String simmetricEncrypt(String inpstr, String cipherTrasformation, Key key) {
        String encoded = null;
        byte[] encodedBytes = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(cipherTrasformation, "BC");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            encodedBytes = cipher.doFinal(inpstr.getBytes());
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        encoded = new String(Hex.encode(encodedBytes));
        return encoded;
    }

    public byte[] sEncrypt(boolean encrypt, byte[] inpstr, byte[] keyBytes) {
        SecretKeySpec sskey = new SecretKeySpec(keyBytes, "AES");
        byte[] outputBytes = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if (encrypt) {
                cipher.init(Cipher.ENCRYPT_MODE, sskey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, sskey);
            }
        } catch (InvalidKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            outputBytes = cipher.doFinal(inpstr);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return outputBytes;
    }

    public byte[] generateSecretKey() {
        byte[] rawKey = null;
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        kg.init(128);
        SecretKey key = kg.generateKey();
        rawKey = key.getEncoded();

        return rawKey;
    }

    public static String bytesToHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public String simmetricDecrypt(String encryptedMessage, String cipherTrasformation, Key key) {
        byte[] encryptedBytes = Hex.decode(encryptedMessage);

        String decrypted = null;
        byte[] decryptedBytes = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(cipherTrasformation, "BC");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            decryptedBytes = cipher.doFinal(encryptedBytes);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        decrypted = new String(decryptedBytes);
        return decrypted;
    }

    public String decryptToString(String encryptedMessage, char[] password) {
        byte[] decryptedMessage = null;
        String decrypted = null;
        RSAPrivateKey privKey = null;

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        byte[] encryptedBytes = Hex.decode(encryptedMessage);

        try {

            privKey = (RSAPrivateKey) getPrivateKey(keystorePath, keystorePassword, alias, password);

            Cipher decryptCipher = Cipher.getInstance("RSA", provider);
            decryptCipher.init(Cipher.DECRYPT_MODE, privKey);

            decryptedMessage = decryptCipher.doFinal(encryptedBytes);

        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        decrypted = new String(decryptedMessage);

        return decrypted;
    }

    public String decryptToString(String encryptedMessage, Key pubKey) {
        byte[] decryptedMessage = null;
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        byte[] encryptedBytes = Base64.decode(encryptedMessage);

        try {
            Cipher decryptCipher = Cipher.getInstance("RSA", provider);
            decryptCipher.init(Cipher.DECRYPT_MODE, pubKey);

            decryptedMessage = decryptCipher.doFinal(encryptedBytes);

        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (decryptedMessage != null) {
            return new String(decryptedMessage);
        } else {
            return new String();
        }
    }

    public String decryptToString(String encryptedMessage) {
        byte[] decryptedMessage = null;
        RSAPrivateKey privKey = null;

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        byte[] encryptedBytes = Hex.decode(encryptedMessage);

        try {

            privKey = (RSAPrivateKey) getPrivateKey(keystorePath, keystorePassword, alias, this.keystorePassword);

            Cipher decryptCipher = Cipher.getInstance("RSA", provider);
            decryptCipher.init(Cipher.DECRYPT_MODE, privKey);

            decryptedMessage = decryptCipher.doFinal(encryptedBytes);

        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (decryptedMessage != null) {
            return new String(decryptedMessage);
        } else {
            return new String();
        }
    }

    /**
     * TODO: introdurre la password per le chiavi private
     *
     * @param inpstr
     * @param passwordKey
     * @return
     */
    public String signToString(String inpstr) {
        String signed = null;
        byte[] signatureBytes = null;
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        try {

            PrivateKey privKey = getPrivateKey(keystorePath, keystorePassword, alias, this.keystorePassword);

            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(privKey);
            signature.update(inpstr.getBytes());
            signatureBytes = signature.sign();

        } catch (SignatureException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        signed = new String(Base64.encode(signatureBytes));
        return signed;
    }

    public static boolean verify(String inpstr, String signature, PublicKey pubKey) {
        boolean verified = false;
        byte[] signedBytes = Base64.decode(signature);
        byte[] inpstrBytes = Base64.decode(inpstr);
        try {

            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);

            Signature verifier = Signature.getInstance("SHA1withRSA");
            verifier.initVerify(pubKey);
            verifier.update(inpstr.getBytes());
            if (verifier.verify(signedBytes)) {
                verified = true;
            } else {
                verified = false;
            }

        } catch (InvalidKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return verified;
    }

    public String signXML(String toXML) {

        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
        XMLSignatureFactory fac = null;
        try {
            fac = XMLSignatureFactory.getInstance("DOM",
                    (Provider) Class.forName(providerName).newInstance());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        Reference ref = null;
        try {
            ref = fac.newReference("#message", fac.newDigestMethod(DigestMethod.SHA1, null));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document document = null;
        try {
            document = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(toXML.getBytes("UTF-8")));
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        Node message = document.getDocumentElement();
        XMLStructure content = new DOMStructure(message);
        XMLObject obj = fac.newXMLObject(Collections.singletonList(content), "message", null, null);
        SignedInfo si = null;
        try {
            si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        PrivateKey privKey = null;
        try {
            privKey = getPrivateKey(keystorePath, keystorePassword, alias, this.keystorePassword);
        } catch (KeyStoreException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        XMLSignature signature = fac.newXMLSignature(si, null, Collections.singletonList(obj), null, null);
        Document doc = null;
        try {
            doc = dbf.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        DOMSignContext dsc = new DOMSignContext(privKey, doc);
        try {
            signature.sign(dsc);
        } catch (MarshalException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLSignatureException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = null;
        try {
            trans = tf.newTransformer();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamResult output = new StreamResult(os);
        try {
            trans.transform(new DOMSource(doc), output);
        } catch (TransformerException ex) {
            Logger.getLogger(X509Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new String(os.toByteArray());

    }

    public String validateXML(String toXML, PublicKey pubKey) {
        String message = null;
        boolean isVerified = false;
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

        String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");

        XMLSignatureFactory fac = null;
        try {
            fac = XMLSignatureFactory.getInstance("DOM",
                    (Provider) Class.forName(providerName).newInstance());
        } catch (ClassNotFoundException ex) {
            logger.debug(ex);
        } catch (InstantiationException ex) {
            logger.debug(ex);
        } catch (IllegalAccessException ex) {
            logger.debug(ex);
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document document = null;
        try {
            document = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(toXML.getBytes("UTF-8")));
        } catch (ParserConfigurationException ex) {
            logger.debug(ex);
        } catch (SAXException ex) {
            logger.debug(ex);
        } catch (IOException ex) {
            logger.debug(ex);
        }

        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            logger.debug("Cannot find Signature Element");
            return "";
        }

        DOMValidateContext valContext = new DOMValidateContext(pubKey, nl.item(0));
        XMLSignature signature = null;
        try {
            signature = fac.unmarshalXMLSignature(valContext);
        } catch (MarshalException ex) {
            logger.debug(ex);
        }
        try {
            isVerified = signature.validate(valContext);
        } catch (XMLSignatureException ex) {
            logger.debug(ex);
        }
        if (isVerified == false) {
            logger.debug("Signature failed validation");
        } else {
            logger.debug("Signature validation passed!");
        }

        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);

            if (node instanceof Element) {
                Node object = node.getLastChild();
                Node messageNode = object.getFirstChild();

                String type = null;
                Element e = (Element) messageNode;
                if (e.getAttribute("type") != null) {
                    type = e.getAttribute("type");
                }

                if (type.equals("REPLY") || type.equals("REQUEST") || type.equals("ERROR")) {
                    StringWriter sw = new StringWriter();
                    Transformer t = null;
                    try {
                        t = TransformerFactory.newInstance().newTransformer();
                        //t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

                        try {
                            t.transform(new DOMSource(messageNode), new StreamResult(sw));
                        } catch (TransformerException ex) {
                            logger.debug(ex);
                        }

                        message = sw.toString();
                    } catch (TransformerConfigurationException ex) {
                        logger.debug(ex);
                    }

                } else {
                    Node bodyNode = messageNode.getFirstChild();
                    message = bodyNode.getTextContent();
                }

            }
        }

        return message;
    }

}
