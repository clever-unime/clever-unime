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
import java.util.HashMap;
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
        this.sessionKey = new HashMap<String, byte[]>();
        try {
            this.ldapClient = new LDAPClient();
        } catch (Exception ex) {
            logger.error("Unable to configure LDAP", ex);
            System.exit(1);
        }
    }

    CleverChatListener(CleverMessageHandler msgHandler, LDAPClient ldapClient) {
        logger = Logger.getLogger("XMPPCommunicator");
        this.msgHandler = msgHandler;
        this.ldapClient = ldapClient;
        this.sessionKey = new HashMap<String, byte[]>();
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
        try {
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
                logger.debug("Saving session key: " + X509Utils.bytesToHex(sessionKeyBytes));
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
                logger.debug("Raw input: " + X509Utils.bytesToHex(rawInput));
                logger.debug("Session key: " + X509Utils.bytesToHex(sessionKeyBytes));

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
                cleverMessage = new CleverMessage(message.getBody());
                X509Certificate cert;
                logger.debug("\nSearch certificate for public key uid=" + src);
                cert = ldapClient.searchCert("", "uid=" + src);

                if (cert == null) {
                    logger.debug(" ERROR - There is no Certificate in LDAP server ");
                } else {
                    pubKey = cert.getPublicKey();
                }
                logger.debug("\n\nCheck signature");
                isVerified = X509Utils.verify(cleverMessage.toXML(), signedExtension.getData(), pubKey);
                if (isVerified) {
                    logger.debug("[This message is signed]\n");
                } else {
                    logger.debug("[This message is signed but the signature in not verified]\n");
                }
                msgHandler.handleCleverMessage(cleverMessage);
                return;
            }

            if (isEncrypted) {
                cleverMessage = new CleverMessage(decrypted);
            } else {
                cleverMessage = new CleverMessage(message.getBody());
            }
            msgHandler.handleCleverMessage(cleverMessage);
        } catch (Exception ex) {
            logger.error("Message Listener fails:\n", ex);
        }
    }
}
