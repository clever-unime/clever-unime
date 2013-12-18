/*
 *  Copyright (c) 2011 Sergio Marino
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */
package org.clever.Common.XMPPCommunicator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509Principal;
import org.jscep.CertificateVerificationCallback;
import org.jscep.client.Client;
import org.jscep.transaction.EnrolmentTransaction;
import org.jscep.transaction.FailInfo;
import org.jscep.transaction.Transaction.State;
import org.jscep.x509.X509Util;



public class ScepRequest implements CallbackHandler
{

  private java.security.PrivateKey key = null;
  private java.security.cert.X509Certificate cert = null;
  private String password = "secret";
  private KeyPair keyPair = null;
  private String CN = null;
  private String C = "EU";
  private String O = "Clever";
  private String OU = "Applications";
  private String domain = "clever.unime.it";
  private String requestPassword = null;
  private String hostname = null;
  private String keystorePath = null;



  public ScepRequest( String CApath, String CApassword, String CAserver, String hostname, String requestPassword, String keystorePath ) throws MalformedURLException, GeneralSecurityException, IOException, InterruptedException
  {

    this.requestPassword = requestPassword;
    this.hostname = hostname;
    this.keystorePath = keystorePath;

    System.setProperty( "javax.net.ssl.trustStore", CApath );
    System.setProperty( "javax.net.ssl.trustStorePassword", CApassword );

    URL server = new URL( "https://" + CAserver + "/cgi-bin/pki/scep/pkiclient.exe" );
    generateKeys( hostname );

    String profile = "PublicCA";
    Client client = new Client( server, cert, this.keyPair.getPrivate(), ( CallbackHandler ) this, profile );

    CertificationRequest csr = this.createCsr( cert.getIssuerX500Principal(), this.keyPair.getPublic(), this.keyPair.getPrivate(), this.requestPassword.toCharArray() );

    EnrolmentTransaction trs = client.enrol( csr );
    EnrolmentTransaction.State state = trs.send();



    switch( state )
    {
      case CERT_ISSUED:
        break;
      case CERT_REQ_PENDING:
        System.out.println( state );
        while( state != State.CERT_ISSUED )
        {
          Thread.sleep( 2000 );
          state = trs.poll();
        }
        CertStore store = trs.getCertStore();
        X509Certificate xcert = null;
        X509Certificate[] certificate_chain = null;
        try
        {
          Collection<X509Certificate> certs = ( Collection<X509Certificate> ) store.getCertificates( null );
          ArrayList chain = new ArrayList<X509Certificate>( certs );
          int numCerts = chain.size();
          if( numCerts == 2 )
          {
            final X509Certificate ca = selectCA( chain );
            int caIndex = chain.indexOf( ca );
            int certIndex = 1 - caIndex;
            xcert = ( X509Certificate ) chain.get( certIndex );
            certificate_chain = new X509Certificate[ 2 ];
            certificate_chain[0] = ca;
            certificate_chain[1] = xcert;
            this.makeKeyStore( keyPair, certificate_chain, password.toCharArray(), keystorePath );

          }
          else if( numCerts == 1 )
          {
            certificate_chain = new X509Certificate[ 1 ];
            xcert = ( X509Certificate ) chain.get( 0 );
            certificate_chain[0] = xcert;
            this.makeKeyStore( keyPair, certificate_chain, password.toCharArray(), keystorePath );

          }
          else
          {

            throw new IllegalStateException();
          }

        }
        catch( CertStoreException ex )
        {
          Logger.getLogger( ScepRequest.class.getName() ).log( Level.SEVERE, null, ex );
        }
        System.out.println( "Stato REQ_PENDING" );
        break;
      case CERT_NON_EXISTANT:
        FailInfo fail = trs.getFailInfo();
        System.err.println( fail );
        break;
    }
    System.out.println( state );

  }



  public final void generateKeys( String hostname )
  {
    try
    {
      KeyPairGenerator kg = null;
      try
      {
        kg = KeyPairGenerator.getInstance( "RSA" );
      }
      catch( NoSuchAlgorithmException ex )
      {
        Logger.getLogger( ScepRequest.class.getName() ).log( Level.SEVERE, null, ex );
      }
      kg.initialize( 2048 );
      this.keyPair = kg.generateKeyPair();
      this.cert = X509Util.createEphemeralCertificate( new X500Principal( "CN=" + hostname ), keyPair );
    }
    catch( GeneralSecurityException ex )
    {
      Logger.getLogger( ScepRequest.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }



  private char[] readPassword( InputStream in ) throws IOException
  {
    return "secret".toCharArray();
  }



  @Override
  public void handle( Callback[] callbacks ) throws IOException, UnsupportedCallbackException
  {
    for( int i = 0; i < callbacks.length; i++ )
    {
      if( callbacks[i] instanceof TextOutputCallback )
      {

        // display the message according to the specified type
        TextOutputCallback toc = ( TextOutputCallback ) callbacks[i];
        switch( toc.getMessageType() )
        {
          case TextOutputCallback.INFORMATION:
            System.out.println( toc.getMessage() );
            break;
          case TextOutputCallback.ERROR:
            System.out.println( "ERROR: " + toc.getMessage() );
            break;
          case TextOutputCallback.WARNING:
            System.out.println( "WARNING: " + toc.getMessage() );
            break;
          default:
            throw new IOException( "Unsupported message type: "
                                   + toc.getMessageType() );
        }

      }
      else if( callbacks[i] instanceof NameCallback )
      {

        NameCallback nc = ( NameCallback ) callbacks[i];

        System.err.print( nc.getPrompt() );
        System.err.flush();
        nc.setName( ( new BufferedReader( new InputStreamReader( System.in ) ) ).readLine() );

      }
      else if( callbacks[i] instanceof PasswordCallback )
      {

        PasswordCallback pc = ( PasswordCallback ) callbacks[i];
        System.err.print( pc.getPrompt() );
        System.err.flush();
        pc.setPassword( readPassword( System.in ) );

      }
      else if( callbacks[i] instanceof CertificateVerificationCallback )
      {
        CertificateVerificationCallback callback = ( CertificateVerificationCallback ) callbacks[i];
        callback.setVerified( true );
      }
      else
      {
        throw new UnsupportedCallbackException( callbacks[i], "Unrecognized Callback " + callbacks[i].toString() );
      }
    }
  }



  public CertificationRequest createCsr( X500Principal subject, PublicKey pubKey, PrivateKey priKey, char[] password ) throws GeneralSecurityException, IOException
  {
    AlgorithmIdentifier sha1withRsa = new AlgorithmIdentifier( PKCSObjectIdentifiers.sha1WithRSAEncryption );

    ASN1Set cpSet = new DERSet( new DERPrintableString( new String( password ) ) );
    Attribute challengePassword = new Attribute( PKCSObjectIdentifiers.pkcs_9_at_challengePassword, cpSet );
    ASN1Set attrs = new DERSet( challengePassword );


    SubjectPublicKeyInfo pkInfo = new SubjectPublicKeyInfo( ( ASN1Sequence ) ASN1Object.fromByteArray( pubKey.getEncoded() ) );

    Properties ht = new Properties();
    ht.put( X509Principal.CN, this.hostname );
    ht.put( X509Principal.C, this.C );
    ht.put( X509Principal.O, this.O );
    ht.put( X509Principal.OU, this.OU );
    ht.put( X509Principal.EmailAddress, this.hostname + "@" + this.domain );
    X509Name nn = new X509Name( ht );


    X509Name name = new X509Name( subject.toString() );

    CertificationRequestInfo requestInfo = new CertificationRequestInfo( nn, pkInfo, attrs );

    Signature signer = Signature.getInstance( "SHA1withRSA" );
    signer.initSign( priKey );
    signer.update( requestInfo.getEncoded() );
    byte[] signatureBytes = signer.sign();
    DERBitString signature = new DERBitString( signatureBytes );

    return new CertificationRequest( requestInfo, sha1withRsa, signature );
  }



  private KeyStore makeKeyStore( KeyPair kp, X509Certificate[] certificate_chain, char[] passwd, String keystorePath ) throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException
  {

    KeyStore ks = KeyStore.getInstance( "pkcs12" );
    ks.load( null, null );
    System.out.println( "certificate_chain.length " + certificate_chain.length );
    ks.setKeyEntry( this.hostname, kp.getPrivate(), passwd, certificate_chain );

    FileOutputStream fos = null;
    try
    {

      fos = new FileOutputStream( keystorePath + this.hostname + ".p12" );
      ks.store( fos, passwd );
    }
    finally
    {
      if( fos != null )
      {
        fos.close();
      }
    }

    return ks;
  }



  private X509Certificate selectCA( List<X509Certificate> certs )
  {
    if( certs.size() == 1 )
    {
      return certs.get( 0 );
    }

    final X509Certificate first = certs.get( 0 );
    final X509Certificate second = certs.get( 1 );
    try
    {

      first.verify( second.getPublicKey() );

      return second;
    }
    catch( InvalidKeyException e )
    {
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
    try
    {
      second.verify( first.getPublicKey() );

      return first;
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }
}
