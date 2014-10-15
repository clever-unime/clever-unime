/*
 * Copyright [2014] [Università di Messina]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
/*
 *  Copyright (c) 2010 Antonio Nastasi
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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.SecureXMPPCommunicator.LDAPClient;
import org.clever.Common.SecureXMPPCommunicator.SecureExtension;
import org.clever.Common.SecureXMPPCommunicator.X509Utils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.packet.DelayInformation;

public class CleverChatListener implements MessageListener {

    private CleverMessageHandler msgHandler = null;
    private Logger logger = null;
    private LDAPClient ldapClient = null;
    private Map<String, byte[]> sessionKey;

    CleverChatListener(CleverMessageHandler msgHandler) {
        logger = Logger.getLogger("XMPPCommunicator");
        this.msgHandler = msgHandler;
    }

    CleverChatListener(CleverMessageHandler msgHandler, LDAPClient ldapClient) {
        logger = Logger.getLogger("XMPPCommunicator");
        this.msgHandler = msgHandler;
        this.ldapClient = ldapClient;
        this.sessionKey = null;
    }
        
    CleverChatListener(CleverMessageHandler msgHandler, LDAPClient ldapClient, Map<String, byte[]> sessionkey) {
        logger = Logger.getLogger("XMPPCommunicator");
        this.msgHandler = msgHandler;
        this.ldapClient = ldapClient;
        this.sessionKey = sessionkey;
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        //Recupero source
        CleverMessage cleverMessage = null;
        String to = message.getTo();
        String dst = to.substring(0, to.indexOf("@"));
        String from = message.getFrom();
        String src = from.substring(0, from.indexOf("@"));

        boolean isEncrypted = false;
        boolean isSigned = false;
        String msg = null;
        boolean isSessionKey = false;

        String dstBaseName = "";
        if (dst.indexOf("-") != -1) {
            dstBaseName = dst.substring(0, dst.indexOf("-"));
        } else {
            dstBaseName = dst;
        }

        X509Utils utils = new X509Utils("./keystore/" + dstBaseName + ".p12", dstBaseName, dstBaseName.toCharArray());

        final SecureExtension encryptedExtension = (SecureExtension) message.getExtension("x", "jabber:x:encrypted");
        final SecureExtension sessionKeyExtension = (SecureExtension) message.getExtension("x", "jabber:x:sessionkey");
        final SecureExtension signedExtension = (SecureExtension) message.getExtension("x", "jabber:x:signed");
        final DelayInformation information = (DelayInformation) message.getExtension("x", "jabber:x:delay");

        String sessionKey = null;
        byte[] sessionKeyBytes = null;

        if (sessionKeyExtension != null) {
            isSessionKey = true;
            sessionKey = utils.decryptToString(sessionKeyExtension.getData());
            sessionKeyBytes = Hex.decode(sessionKey);
            this.sessionKey.put(src, sessionKeyBytes);
            //writeKey(sessionKeyBytes);
        }

        byte[] decryptedBytes = null;
        String decrypted = null;
        if (encryptedExtension != null) {
            isEncrypted = true;
            if (!isSessionKey) //sessionKeyBytes = readKey();
            {
                sessionKeyBytes = this.sessionKey.get(src);
            }
            byte[] rawInput = Hex.decode(encryptedExtension.getData());
            decryptedBytes = utils.sEncrypt(false, rawInput, sessionKeyBytes);
            decrypted = new String(decryptedBytes);
        }

        if (signedExtension != null) {
            /*
             * Al momento non funzionante: i digest, quello apposto al messaggio e quello 
             * calcolato in ricezione, possono differire
             */
            boolean isVerified = false;
            isSigned = true;
            PublicKey pubKey = null;

            X509Certificate cert;
            cert = ldapClient.searchCert("", "uid=" + cleverMessage.getSrc());

            if (cert == null) {
                logger.debug(" ERROR - There is no Certificate in LDAP server ");
            } else {
                pubKey = cert.getPublicKey();
            }
            //Calcolo il digest del corpo del messaggio ricevuto
            MessageDigest md = null;
            try {

                md = MessageDigest.getInstance("SHA");

            } catch (NoSuchAlgorithmException ex) {
                java.util.logging.Logger.getLogger(CleverChatListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            md.reset();
            try {
                md.update(cleverMessage.toXML().getBytes("UTF-8"), 0, cleverMessage.toXML().length());
            } catch (UnsupportedEncodingException ex) {
                java.util.logging.Logger.getLogger(CleverChatListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CleverException ex) {
                java.util.logging.Logger.getLogger(CleverChatListener.class.getName()).log(Level.SEVERE, null, ex);
            }

            // String digest = new String(Hex.encode(md.digest()));
            String digest = new String(Base64.encode(md.digest()));

            if (digest.equals(signedExtension.getData())) {
                isVerified = true;
            } else {
                isVerified = false;
            }

            if (isVerified) {
                logger.debug("[This message is signed]");
            } else {
                logger.debug("[The signature in not verified]");
            }

        }

        Date date = null;
        String timestamp = null;

        if (information != null) {

            date = information.getStamp();
            timestamp = date.toString();

        }
        long timeStop = System.currentTimeMillis();
        if (isEncrypted) {
            cleverMessage = new CleverMessage(decrypted);
        } /*else if( isSigned ){
         this.cleverMessage = new CleverMessage( msg );       
         }*/ else {
            cleverMessage = new CleverMessage(message.getBody());
        }
        msgHandler.handleCleverMessage(cleverMessage);
    }
}
