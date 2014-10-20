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
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
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
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.PublicKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.Base64;
import org.clever.Common.SecureXMPPCommunicator.LDAPClient;
import org.clever.Common.SecureXMPPCommunicator.SecureExtension;
import org.clever.Common.SecureXMPPCommunicator.X509Utils;
import org.jivesoftware.smackx.packet.DelayInformation;

public class RoomListener implements PacketListener, Runnable {

    private CleverMessageHandler receiver;
    private CleverMessage cleverMessage;
    // private ClusterCoordinator.ROOMS roomId;
    private ConnectionXMPP conn;
    private Logger logger;
    private LDAPClient ldapClient;

    public RoomListener(/*ClusterCoordinator.ROOMS id,*/CleverMessageHandler cmh, ConnectionXMPP conn) throws CleverException {
        receiver = cmh;
        //  roomId=id;
        logger = Logger.getLogger("RoomListener");
        logger.debug("roomlistener added: ");
        this.conn = conn;
        try {
            //ldapClient = new LDAPClient("localhost",389,"dc=clever,dc=unime,dc=it","cn=root,dc=clever,dc=unime,dc=it","secret");
            ldapClient = this.conn.getLDAPClient();
            logger.debug("ldapClient instance " + ldapClient);
        } catch (Exception ex) {
            logger.error("RoomListener constructor", ex);
        }
    }

    @Override
    public void run() {
        logger.debug("nel run");
        receiver.handleCleverMessage(this.cleverMessage);

    }

    @Override
    public void processPacket(Packet arg0) {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        Message message = (Message) arg0;
        logger.debug("messaggio xmpp: " + message);

        String to = message.getTo();
        logger.debug("to " + to);
        String dst = to.substring(0, to.indexOf("@"));
        logger.debug("dst " + dst);
        String from = message.getFrom();
        logger.debug("from " + from);
        String src = from.substring(from.indexOf("/") + 1);
        logger.debug("src " + src);

        //this.conn.writeTimeToFile(src, dst,"Received", timeStart);
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

        try {
            final SecureExtension encryptedExtension = (SecureExtension) message.getExtension("x", "jabber:x:encrypted");
            final SecureExtension sessionKeyExtension = (SecureExtension) message.getExtension("x", "jabber:x:sessionkey");
            final SecureExtension signedExtension = (SecureExtension) message.getExtension("x", "jabber:x:signed");
            final DelayInformation information = (DelayInformation) message.getExtension("x", "jabber:x:delay");

            String sessionKey = null;
            byte[] sessionKeyBytes = null;

            if (sessionKeyExtension != null) {
                isSessionKey = true;
                logger.debug("There is a session key");
                sessionKey = utils.decryptToString(sessionKeyExtension.getData());
                sessionKeyBytes = Hex.decode(sessionKey);
                this.conn.getSessionKey().put(src, sessionKeyBytes);
                //writeKey(sessionKeyBytes);
            }

            byte[] decryptedBytes = null;
            String decrypted = null;
            if (encryptedExtension != null) {
                isEncrypted = true;
                if (!isSessionKey) //sessionKeyBytes = readKey();
                {
                    sessionKeyBytes = this.conn.getSessionKey().get(src);
                }
                byte[] rawInput = Hex.decode(encryptedExtension.getData());
                decryptedBytes = utils.sEncrypt(false, rawInput, sessionKeyBytes);
                decrypted = new String(decryptedBytes);
            }
            logger.debug("signed Ext: " + signedExtension);
            if (signedExtension != null) {
                /*
                 * Procedendo con questo metodo i message digest, quello apposto al messaggio
                 * e quello calcolato in ricezione, possono risultare differenti
                 */
                logger.debug("Message is signed");
                boolean isVerified = false;
                PublicKey pubKey = null;
                CleverMessage cleverMessage = new CleverMessage(message.getBody());

                X509Certificate cert;
                String srcBaseName = "";
                if (cleverMessage.getSrc().indexOf("-") != -1) {
                    srcBaseName = cleverMessage.getSrc().substring(0, cleverMessage.getSrc().indexOf("-"));
                } else {
                    srcBaseName = cleverMessage.getSrc();
                }

                cert = ldapClient.searchCert("", "uid=" + srcBaseName);

                if (cert == null) {
                    logger.debug(" ERROR - There is no Certificate in LDAP server ");
                } else {
                    pubKey = cert.getPublicKey();
                }

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
                    java.util.logging.Logger.getLogger(RoomListener.class.getName()).log(Level.SEVERE, null, ex);
                }

                //String digest = new String(Hex.encode(md.digest()));
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

            String username = from.substring(from.indexOf("/") + 1);
            String srcBaseName = "";
            if (username.indexOf("-") != -1) {
                srcBaseName = username.substring(0, username.indexOf("-"));
            } else {
                srcBaseName = username;
            }

            if (isEncrypted) {
                this.cleverMessage = new CleverMessage(decrypted);
            } else {
                this.cleverMessage = new CleverMessage(message.getBody());
            }

            logger.debug(cleverMessage.toXML());
            receiver.handleCleverMessage(this.cleverMessage);

        } catch (Exception e) {
            logger.error("Exception decoding message ", e);
        }
    }
}
