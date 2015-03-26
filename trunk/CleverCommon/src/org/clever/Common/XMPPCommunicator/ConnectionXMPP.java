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
 *  Copyright (c) 2013 Nicola Peditto
 *  Copyright (c) 2013 Carmelo Romeo
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Date;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.apache.log4j.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.clever.Common.SecureXMPPCommunicator.LDAPClient;
import org.clever.Common.SecureXMPPCommunicator.SecureExtension;
import org.clever.Common.SecureXMPPCommunicator.X509Utils;
import org.clever.Common.SecureXMPPCommunicator.EncryptedProvider;
import org.clever.Common.SecureXMPPCommunicator.SessionKeyProvider;
import org.clever.Common.SecureXMPPCommunicator.SignedProvider;
import java.util.Map;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import java.security.interfaces.RSAPublicKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
//import static org.clever.Common.Communicator.Agent.logger;
import org.clever.Common.Exceptions.CleverException;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.Occupant;

class myTransferListener implements FileTransferListener {

    private Logger logger;
    private String path;

    public myTransferListener(Logger l, String p) {

        logger = l;
        logger.debug("Copying the logger");

        path = p;
        logger.debug("Copying the path " + path);
    }

    @Override
    public void fileTransferRequest(FileTransferRequest request) {
        // Check to see if the request should be accepted
        IncomingFileTransfer transfer = request.accept();
        try {
            logger.debug("------Receiving file in " + path + "------");
            transfer.recieveFile(new File(path));
            logger.debug("------File received------");
        } catch (XMPPException ex) {
            logger.error("------Error while receiving file.------");
            logger.error(ex.toString());
        }
    }
}

public class ConnectionXMPP implements javax.security.auth.callback.CallbackHandler {

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public enum ROOM {

        CLEVER_MAIN,
        SHELL
    };

    public enum TransmissionModes {

        Plain,
        Signed,
        Encrypted
    };

    /**
     * Resource name for XMPP login (unique)
     */
    private String resource;

    private XMPPConnection connection = null;
    private String servername = "";
    private String username = "";
    private String password = "";
    private HashMap<ROOM, MultiUserChat> mucs = new HashMap<ROOM, MultiUserChat>(0);
    private Integer port;
    private Logger logger;
    private TransmissionModes mMode;
    private CleverChatManagerListener cleverChatManagerListener = null;
    private CleverMessageHandler msgHandler = null;

    private boolean isTLS = false;

    //dicembre 2012
    private LDAPClient ldapClient = null;
    private X509Utils utils = null;
    private Map<String, byte[]> sessionKey;
    private long timeStart;
    private long timeStop;

    public ConnectionXMPP() {
        logger = Logger.getLogger("XMPPCommunicator");
        sessionKey = new HashMap<String, byte[]>();
        resource = new Integer(new Random().nextInt()).toString();
    }

    public void connect(final String servername, final Integer port, TransmissionModes mode)
            throws CleverException, FileNotFoundException {
        this.servername = servername;
        this.port = port;
        mMode = mode;

        try {
            ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(servername, port);
            connection = new XMPPConnection(connectionConfiguration);
            //connection.DEBUG_ENABLED=true;
            connection.connect();
            logger.info("XMPP connected plain mode");
        } catch (XMPPException ex) {
            logger.error("Error during XMPP connection: " + ex.toString());
            throw new CleverException(ex, "Error during XMPP connection");
        }

        ProviderManager.getInstance().addExtensionProvider("x", "jabber:x:encrypted",
                new EncryptedProvider());
        ProviderManager.getInstance().addExtensionProvider("x", "jabber:x:signed",
                new SignedProvider());
        ProviderManager.getInstance().addExtensionProvider("x", "jabber:x:sessionkey",
                new SessionKeyProvider());

        //ldapClient = new LDAPClient("localhost", 389, "dc=clever,dc=unime,dc=it", "cn=admin,dc=clever,dc=unime,dc=it", "clever");
        ldapClient = new LDAPClient();
        logger.info("ldapClient created");
        //utils = new X509Utils("./keystore/"this.+".p12","cmgaia","cmgaia".toCharArray());
    }

    public void connectTLS(final String servername, final Integer port,
            final String keystorePath, final String keystorePassword,
            final String truststorePath, final String truststorePassword, TransmissionModes mode) {
        this.servername = servername;
        this.port = port;
        this.isTLS = true;
        mMode = mode;
        try {
            ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(servername, port);
            connectionConfiguration.setSecurityMode(SecurityMode.required);
            connectionConfiguration.setVerifyRootCAEnabled(true);
            connectionConfiguration.setSASLAuthenticationEnabled(true);
            connectionConfiguration.setKeystoreType("pkcs12");
            connectionConfiguration.setKeystorePath(keystorePath);
            connectionConfiguration.setTruststoreType("jks");
            connectionConfiguration.setTruststorePath(truststorePath);
            connectionConfiguration.setTruststorePassword(truststorePassword);
            connection = new XMPPConnection(connectionConfiguration, (CallbackHandler) this);
            SASLAuthentication.supportSASLMechanism("PLAIN", 0);
            connection.connect();
            logger.info("XMPP connected in TLS mode");
        } catch (XMPPException ex) {
            logger.error("Error during the XMPP connection: " + ex.toString());
            System.exit(1);
        }
    }

    /**
     * Join in a specific room
     *
     * @param room
     * @param nickName
     */
    /*
     public void joinInRoom( final String roomName, final ROOM roomType, final String nickName , Boolean createIt)
     {
     if(createIt)
     {
     this.joinInRoom( roomName, roomType, nickName);
     }
     else
     {


     DiscussionHistory history = new DiscussionHistory();
     history.setMaxStanzas( 0 );

     MultiUserChat mucTemp ;
                
     if(mucTemp = MultiUserChat.getRoomInfo(connection, roomName))
     {

     }
                 
     logger.info( "Creating room: " + roomName + " with nickname: " + nickName );
     try
     {
     mucTemp.join( nickName, "", history, 5000 );
     logger.info( "Created room: " + roomName + " with nickname: " + nickName );
     mucs.put( roomType, mucTemp );
     }
     catch( XMPPException ex )
     {
     logger.error( "Error while joing room: " + roomName + " " + ex );
     }
     }
     }
     */
    /**
     * Join in a specific room with a empty status creating it
     *
     * @param room
     * @param nickName
     */
    public MultiUserChat joinInRoom(final String roomName, final ROOM roomType, final String nickName) {
        return this.joinInRoom(roomName, roomType, nickName, "");
    }

    /**
     * Join in a specific room with a specific status creating it
     *
     * @param room
     * @param nickName
     */
    public MultiUserChat joinInRoom(final String roomName, final ROOM roomType, final String nickName, final String status) //provo a fare sta funzione come sincronizzata!
    {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);
        MultiUserChat mucTemp = new MultiUserChat(connection, roomName);

        logger.info("Creating room: " + roomName + " with nickname: " + nickName);

        try {
            mucTemp.join(nickName, "", history, 5000);
            mucTemp.changeAvailabilityStatus(status, Presence.Mode.chat);
            logger.info("Created room: " + roomName + " with nickname: " + nickName);
            mucs.put(roomType, mucTemp);
        } catch (XMPPException ex) {
            logger.error("Error while joing room: " + roomName + " " + ex);
        }
        return mucTemp;
    }

    public MultiUserChat joinInRoom(final String roomName, final String password, final String nickName) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);
        MultiUserChat mucTemp = new MultiUserChat(connection, roomName);

        logger.info("Creating room: " + roomName + " with nickname: " + nickName);
        try {

            logger.debug("?=) mucRoom: " + mucTemp.getRoom());
            mucTemp.join(nickName, password, history, 5000);
            logger.info("Created room: " + roomName + " with nickname: " + nickName);

        } catch (XMPPException ex) {
            logger.error("[MultiUserChat] joinInRoom Error while joing room: " + roomName + " " + ex);
        }

        return mucTemp;

    }

    /**
     * Add Presence listener to connection. Method for testing and debugging
     * purposes
     */
    public void addPresenceListener(ROOM roomType, PacketListener listener) {
        getMultiUserChat(roomType).addParticipantListener(listener);
    }

    /**
     * Delete Presence listener to connection. Method for testing and debugging
     * purposes
     */
    public void removePresenceListener(ROOM roomType, PacketListener listener) {
        getMultiUserChat(roomType).removeParticipantListener(listener);
    }

    /**
     * Add Chat Manager Listener to connection
     *
     * @param msgHandler
     */
    public void addChatManagerListener(final CleverMessageHandler msgHandler) {
        this.msgHandler = msgHandler;
        cleverChatManagerListener = new CleverChatManagerListener(msgHandler, this.ldapClient, this.sessionKey);
        connection.getChatManager().addChatListener(cleverChatManagerListener);
    }

    public void authenticate(final String username, final String password) {
        this.username = username;
        this.password = password;
        //TODO: providing number of retries as parameter
        for (int i = 0; i < 4; i++) {
            try {

                if (isTLS) {
                    logger.debug("trying TLS authentication");
                    connection.getSASLAuthentication().authenticate(this.username, this.servername,
                            new SecurityCallback(this.username, this.password));

                } else {
                    //Append resource name to obtain a unique connection: random number
                    //TODO: choose a better mechanism to specify resource
                    connection.login(username, password, resource);
                }

                logger.debug("XMPP connection established with username: "
                        + this.username
                        + " password: " + this.password
                        + " server: " + this.servername
                        + " port: " + this.port
                        + "resource" + this.resource);

                utils = new X509Utils("./keystore/" + username + ".p12", username, username.toCharArray());

            } catch (XMPPException e) {
                logger.error("XMPP login failed with username (try # " + i + ": "
                        + this.username
                        + " password: " + this.password
                        + " server: " + this.servername
                        + " port: " + this.port
                        + "resource" + this.resource
                        + "Exception:" + e);
            }

            if (connection.isAuthenticated()) {
                return;
            } else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    logger.error("Interrupted Thread error: " + ex.toString());
                    //System.exit( 1 );
                }
            }
        }
        //System.exit( 1 );
    }

    public String getServer() {
        return servername;
    }

    public String getUsername() {
        return (username);
    }

    /**
     * *
     * Aggiunto per ricavare i parametri di accesso per OpenAM.
     *
     * @return
     */
    public String getPassword() {
        return (password);
    }

    //alessandro dicembre 2012
    public String getUser() {
        String userId = this.connection.getUser();
        userId = userId.replaceAll("/.*", "");
        return userId;
    }

    public Map<String, byte[]> getSessionKey() {
        return this.sessionKey;
    }

    public Iterator getMembers(ROOM roomType) {
        return getMultiUserChat(roomType).getOccupants(); //restituisce tutti gli occupanti della stanza specificata
    }

    public MultiUserChat getMultiUserChat(ROOM roomType) {
        return mucs.get(roomType);
    }

    public MultiUserChat getMultiUserChat() {
        return getMultiUserChat(ROOM.CLEVER_MAIN);
    }

    public void closeConnection() {
        Iterator it = mucs.values().iterator();
        MultiUserChat mucTemp = null;
        while (it.hasNext()) {
            mucTemp = (MultiUserChat) it.next();
            mucTemp.leave();
        }

        connection.disconnect();
    }

    public LDAPClient getLDAPClient() {
        return this.ldapClient;
    }

    public static TransmissionModes parseMode(String mode) {
        Logger.getLogger("XMPPCommunicator").debug("Parsing mode: " + mode);
        if (mode != null) {
            if (mode.equalsIgnoreCase("encrypted")) {
                return ConnectionXMPP.TransmissionModes.Encrypted;
            } else if (mode.equalsIgnoreCase("signed")) {
                return ConnectionXMPP.TransmissionModes.Signed;
            }
        }
        return ConnectionXMPP.TransmissionModes.Plain;
    }

    /*
     public boolean hostIsConnected( String jid )
     {
     return (
     this.getMultiUserChat(ROOM.CLEVER_MAIN).getOccupantPresence( jid) != null
     ||
     this.getMultiUserChat(ROOM.SHELL).getOccupantPresence( jid) != null);
     }
     */
    public void sendMessage(String jid, final CleverMessage message) {
        switch (mMode) {
            case Plain:
                sendPlainMessage(jid, message);
                break;
            case Signed:
                sendSignedMessage(jid, message);
                break;
            case Encrypted:
                sendEncryptedMessage(jid, message);
                break;
            default:
                sendPlainMessage(jid, message);
                break;
        }
    }

    /**
     * Send private message to an user
     *
     * @param jid
     * @param message
     */
    public void sendPlainMessage(String jid, final CleverMessage message) {

        try {
            //TODO: check if host is connected and throw a exception on sending failure 

            logger.debug("Sending message: " + message.toXML());
        } catch (CleverException ex) {
            logger.error("error" + ex);
        }

        jid += "@" + this.getServer();
        // See if there is already a chat open
        //TODO: fix problema con jid con risorsa o senza
        Chat chat = cleverChatManagerListener.getChat(jid.toLowerCase());
        if (chat == null) {
            logger.debug("Chat toward " + jid + " not found");
            chat = connection.getChatManager().createChat(jid, new CleverChatListener(msgHandler, ldapClient, sessionKey));
        }

        // Send a message
        try {
            logger.debug("sending plain message: " + message.toXML());
            chat.sendMessage(message.toXML());
            logger.debug("message sent");
        } catch (XMPPException ex) {
            logger.error("Error while sending message: " + message.getId() + " " + ex.getMessage());
        } catch (CleverException ex) {
            java.util.logging.Logger.getLogger(ConnectionXMPP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    /**
     * *
     * Encrypted message
     *
     * @param jid
     * @param message
     */
    public void sendEncryptedMessage(String jid, final CleverMessage message) {
        boolean detailDst = false;
        String src = message.getSrc();
        String dst = message.getDst();

        this.timeStart = System.currentTimeMillis();

        Message packet = new Message();
        packet.setType(Message.Type.normal);
        packet.setTo(message.getDst() + "@" + this.servername);
        packet.setSubject("Messaggio incapsulato");
        packet.setBody("[This message is encrypted]");

        X509Certificate dstCert;
        PublicKey pubKey;

        String srcBaseName = "";
        if (message.getSrc().indexOf("-") != -1) {
            srcBaseName = message.getSrc().substring(0, message.getSrc().indexOf("-"));
        } else {
            srcBaseName = message.getSrc();
        }

        String dstBaseName = "";
        if (message.getDst().indexOf("-") != -1) {
            dstBaseName = message.getDst().substring(0, message.getDst().indexOf("-"));
            detailDst = true;
        } else {
            dstBaseName = message.getDst();
        }

        dstCert = ldapClient.searchCert("", "uid=" + dstBaseName);
        if (dstCert == null) {
            System.out.print(" Error - No Certificate in LDAP Server ");
            return;
        } else {
            pubKey = dstCert.getPublicKey();
        }

        byte[] key = null;
        byte[] encryptedBytes = null;
        String encryptedSessionKey = null;

        boolean firstMsg = false;

        if (this.sessionKey.get(message.getDst()) != null) {
            key = this.sessionKey.get(message.getDst());
            logger.debug("Session key exists:\nDst: " + message.getDst() + " key: " + X509Utils.bytesToHex(key));
        } else {
            firstMsg = true;
            key = utils.generateSecretKey();
            this.sessionKey.put(message.getDst(), key);
            logger.debug("Session key generated:\nDst: " + message.getDst() + " key: " + X509Utils.bytesToHex(key));
        }

        if (firstMsg) {
            String stringKey = new String(Hex.encode(key));
            SecureExtension sessionKeyExtension = new SecureExtension("sessionkey");
            encryptedSessionKey = utils.encryptToString(stringKey, (RSAPublicKey) pubKey);

            if (encryptedSessionKey != null) {
                sessionKeyExtension.setData(encryptedSessionKey);
                packet.addExtension(sessionKeyExtension);
            }
        }

        //key = readKey();
        //key = this.sessionKey.get("sessionkey");
        String encryptedMsg = null;
        try {
            Date date = new Date();
            DelayInformation delayInformation = new DelayInformation(date);
            SecureExtension encryptedExtension = new SecureExtension();
            encryptedBytes = utils.sEncrypt(true, message.toXML().getBytes(), key);
            encryptedMsg = new String(Hex.encode(encryptedBytes));
            if (encryptedMsg != null) {
                encryptedExtension.setData(encryptedMsg);
                packet.addExtension(encryptedExtension);
            }
            packet.addExtension(delayInformation);

            jid += "@" + this.getServer();
            // See if there is already a chat open
            Chat chat = cleverChatManagerListener.getChat(jid.toLowerCase());
            if (chat == null) {
                logger.debug("Chat toward " + jid + " not found");
                chat = connection.getChatManager().createChat(jid, new CleverChatListener(msgHandler, ldapClient, sessionKey));
            }

            try {
                logger.debug("sending encrypted message: " + packet.toXML());
                chat.sendMessage(packet);
                logger.debug("encrypted message sent");
            } catch (XMPPException ex) {
                logger.error("Error while sending message: " + message.toXML() + " " + ex.getMessage());
            }
        } catch (CleverException ex) {
            java.util.logging.Logger.getLogger(ConnectionXMPP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    /**
     * *
     * Calculate clever message signauture.
     *
     * @return The signature.
     */
    private String signCleverMessage(CleverMessage msg) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException ex) {
            java.util.logging.Logger.getLogger(ConnectionXMPP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        md.reset();
        try {
            md.update(msg.toXML().getBytes("UTF-8"), 0, msg.toXML().length());
        } catch (CleverException ex) {
            logger.error("An exception has trown: " + ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex);
        }

        String digest = new String(Base64.encode(md.digest()));
        return digest;
    }

    public void sendSignedMessage(String jid, final CleverMessage message) {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        try {
            Message packet = new Message();
            packet.setType(Message.Type.normal);
            packet.setTo(message.getDst() + "@" + servername);
            message.setSignature(signCleverMessage(message));
            packet.setBody(message.toXML());

            byte[] digestBytes = null;
            String signedDigest = null;
            SecureExtension signedExtension = new SecureExtension("signed");
            X509Utils x = new X509Utils();
            try {
                String signature = x.signToString(message.toXML());
                signedExtension.setData(signature);
                packet.addExtension(signedExtension);
            } catch (CleverException ex) {
                logger.error("Error while signing message: " + ex.getMessage());

            }
            Date date = new Date();
            DelayInformation delayInformation = new DelayInformation(date);
            packet.addExtension(delayInformation);

            jid += "@" + this.getServer();
            // See if there is already a chat open
            Chat chat = cleverChatManagerListener.getChat(jid.toLowerCase());
            if (chat == null) {
                logger.debug("Chat toward " + jid + " not found");
                chat = connection.getChatManager().createChat(jid, new CleverChatListener(msgHandler, ldapClient));
            }
            // Send a message
            try {
                logger.debug("sending signed message: " + packet.toXML());
                chat.sendMessage(packet);
                logger.debug("signed message sent");
            } catch (XMPPException ex) {
                logger.error("Error while sending message: " + message.toXML() + " " + ex.getMessage());
            }

        } catch (CleverException ex) {
            java.util.logging.Logger.getLogger(ConnectionXMPP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public void addListener(ROOM roomType, ParticipantStatusListener list) {
        getMultiUserChat(roomType).addParticipantStatusListener(list);
    }

    public String getHostName() {
        String hostname = "";

        try {
            InetAddress localmachine = InetAddress.getLocalHost();
            hostname = localmachine.getHostName();
            logger.debug("Host name: " + hostname);
        } catch (Exception ex) {
            logger.error("Error while getting the hostname: " + ex.getMessage());
        } finally {
            return hostname;
        }
    }

    /**
     * Procedure for In-Band Registration ( XEP - 0077 )
     *
     * @param server server to connect for registration
     * @param port numeric value for port where is binded the server
     * @param username the name used for the client
     * @param password the password used for the client
     */
    public void inBandRegistration(final String username, final String password) {
        if ((connection == null || !connection.isConnected())) {
            logger.debug("Not connected");
            System.exit(1);
        }
        if (username.isEmpty() || password.isEmpty()) {
            logger.debug("Invalid username or password");
            System.exit(1);

        }
        try {
            // Connect and try to register the new account
            logger.debug("Trying in-band registration with username: " + username + " and password: " + password);
            AccountManager accountManager = new AccountManager(connection);
            accountManager.createAccount(username, password);
        } catch (XMPPException ex) {
            logger.error("Error while using In-Band Registration: " + ex);
            System.exit(1);
        }
    }

    /**
     * Procedure for retrieving the number of CMs in a specific room
     *
     * @param room
     * @return
     */
    public Collection<Occupant> getCCsInRoom(final ROOM roomType) {
        MultiUserChat mucTemp = getMultiUserChat(roomType);

        Collection<Occupant> collection = new LinkedList<Occupant>(); //collezione di uscita!
        Iterator<String> it = mucTemp.getOccupants();
        Occupant occupant = null;
        Presence presence = null;
        String occupantJid = "";
        String tmp = "";

        /*devo effettuare ora una ricerca x status*/
        while (it.hasNext()) {
            occupantJid = it.next();
            presence = mucTemp.getOccupantPresence(occupantJid);
            occupant = mucTemp.getOccupant(occupantJid);
            tmp = presence.getStatus();

            if ((tmp != null && (tmp.equals("CM_MONITOR") || (tmp.equals("CM_ACTIVE"))))) //controlla sempre tmp a null! che è importante
            {
                collection.add(occupant);
            }
        }
        return collection;
    }

    /*autore: Marco Carbone*/
    /* QUESTA FUNZIONE CERCA UN PARTICOLARE CM (per nickname) DENTRO LA STANZA specificata da roomtype E TORNA TRUE SE LO TROVA!*/
    public boolean SearchCM_InRoom(String nickname, ROOM roomtype) {//return true if exist
        Collection<Occupant> cm_inRoom = getCCsInRoom(roomtype); //prendo tutta la lista dei cluster manager presenti nella stanza roomtype
        Iterator it = cm_inRoom.iterator();
        Occupant cm = null;
        String nick = "";

        while (it.hasNext()) {
            cm = (Occupant) it.next(); //cerco quello che ha come nick quello desiderato!
            nick = cm.getNick();

            if (nick.equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    /*autore: Marco Carbone*/
    /*Questa funzione effettua una ricerca per status, fra tutti gli occupanti della stanza
     * specificata (roomType), restituendo il numero di ClusterManager presenti (sia nello stato
     * active che monitor)
     */
    public int getNum_CCsInRoom(final ROOM roomType) {

        MultiUserChat mucTemp = getMultiUserChat(roomType);

        Collection<Occupant> collection = new LinkedList<Occupant>(); //creo una lista concatenata di occupant
        Iterator<String> it = mucTemp.getOccupants(); //prendo tutti gli occupanti della room, devo invece selezionare solo quelli che osno moderatori della room!
        Occupant occupant = null;
        Presence presence = null; //mercoledì 16 Novembre 2011
        String occupantJid = ""; //qui memorizzo ad ogni iterazione un valore della collection Occupants

        while (it.hasNext()) {
            occupantJid = it.next();
            presence = mucTemp.getOccupantPresence(occupantJid);
            occupant = mucTemp.getOccupant(occupantJid);

            if (presence.getStatus() != null) {
                if (presence.getStatus().equals("CM_MONITOR")) {
                    collection.add(occupant);
                }
                if (presence.getStatus().equals("CM_ACTIVE")) {
                    collection.add(occupant);
                }
            }
        }

        return collection.size();
    }

    /**
     * Procedure for retrieving the name of CM ACTIVE in a specific room
     *
     * @param room
     * @return
     */
    public String getActiveCC(final ROOM roomType) //questa funzione da problemi nel restituire il CC attivo cercandolo dentro la room SHELL!!!
    {
        /*Ho modificato quesat funzione di ricerca del CC attivo perché adesso la 
         * modalità di ricerca si deve basare sullo status, lo status ci viene dato dalla funzione getOccupants di MultiUserChat
         * la vecchia funzione getActiveCC, invece ricercava il CC attivo o tra la lista dei
         * CC di CleverMain (che restituisce una lista di Occupant) o tra tutti gli utenti di
         * SHELL, che rioestituisce cmq una lista di Occupant e da Occupant non vediamo lo status!
         * 
         */

        MultiUserChat mucTemp = getMultiUserChat(roomType);
        Iterator<String> it = mucTemp.getOccupants(); //ho tutti i Jid degli utenti di roomType

        Occupant occupant = null;
        Presence presence = null;
        String occupantJid = "";
        String tmp = "";
        String nick = null;

        while (it.hasNext()) {
            occupantJid = it.next();
            presence = mucTemp.getOccupantPresence(occupantJid);
            occupant = mucTemp.getOccupant(occupantJid);
            tmp = presence.getStatus();

            if (tmp == null) {
                continue;
            }
            if ((!tmp.isEmpty()) && (tmp.equals("CM_ACTIVE"))) //in qualsiasi stanza il Cm ha come status CM_ACTIVE!
            {
                nick = occupant.getNick();
            }
        }
        return nick;
    }

    /*questa funzione restituisce la lista degli HM presenti sulla stanza
     * specificata da roomType, attualmente questi HostCoordinator sono dei
     * moderatori per quella stanza (credo SHELL), forse questo controllo va
     * modificato e fatto sulla presence.MODe!
     */
    /*public Collection<Occupant> getHCsInRoom( final ROOM roomType )
     {
     MultiUserChat mucTemp = getMultiUserChat( roomType );

     Collection<Occupant> collection = new LinkedList<Occupant>();
     Iterator<String> it = mucTemp.getOccupants();
     Occupant occupant = null;
     Presence presence = null;

     while( it.hasNext() )
     {
     String occupantJid = it.next();
     presence = mucTemp.getOccupantPresence(occupantJid);
     occupant = mucTemp.getOccupant(occupantJid);

     String tmp = presence.getStatus();
      
     if(tmp == null)
     continue;
      
     if( (!tmp.isEmpty()) && (tmp.equals("HM")) )
     {
     collection.add(occupant);
     }
     }
     return collection;
     }*/
    public Collection<Occupant> getHCsInRoom(final ROOM roomType) {
        MultiUserChat mucTemp = getMultiUserChat(roomType);

        Collection<Occupant> collection = new LinkedList<Occupant>(); //collezione di uscita!
        Iterator<String> it = mucTemp.getOccupants();
        Occupant occupant = null;
        Presence presence = null;
        String occupantJid = "";
        String tmp = "";

        /*devo effettuare ora una ricerca x status*/
        while (it.hasNext()) {
            occupantJid = it.next();
            presence = mucTemp.getOccupantPresence(occupantJid);
            occupant = mucTemp.getOccupant(occupantJid);
            tmp = presence.getStatus();

            if (tmp == null) {
                continue;
            }

            if ((!tmp.isEmpty()) && (tmp.equals("HM"))) //if( (tmp!=null) && (tmp.equals("HM")) ) //controlla sempre tmp a null! che è importante
            {
                collection.add(occupant);
            }
        }
        return collection;
    }

    //NEWMONITOR
    /**
     * Return the Collection of the active VM probe
     *
     * @return the Collection of probes
     */
    public Collection<Occupant> getProbesInRoom(final ROOM roomType) {
        MultiUserChat mucTemp = getMultiUserChat(roomType);

        Collection<Occupant> collection = new LinkedList<Occupant>(); //collezione di uscita!
        Iterator<String> it = mucTemp.getOccupants();
        Occupant occupant = null;
        Presence presence = null;
        String occupantJid = "";
        String tmp = "";

        /*devo effettuare ora una ricerca x status*/
        while (it.hasNext()) {
            occupantJid = it.next();
            presence = mucTemp.getOccupantPresence(occupantJid);
            occupant = mucTemp.getOccupant(occupantJid);
            tmp = presence.getStatus();

            if (tmp == null) {
                continue;
            }

            if ((!tmp.isEmpty()) && (tmp.equals("HM/Probe"))) //if( (tmp!=null) && (tmp.equals("HM")) ) //controlla sempre tmp a null! che è importante
            {
                collection.add(occupant);
            }
        }
        return collection;
    }

    /*QUESTA FUNZIONE CERCA UN PARTICOLARE HOSTNAME (PER NICK) ALL'INTERNO DELLA STANZA roomtype
     per il momento nessuno la usa!*/
    public boolean SearchHM_InRoom(String nickname, ROOM roomtype)//questa funzione cerca l'HM istanziato da questo initiator nella stanza CLEVER_MAIN
    {//return true if exist

        Collection<Occupant> hm_inRoom = getHCsInRoom(roomtype);
        Iterator it = hm_inRoom.iterator();
        Occupant hm = null;
        String nick = "";

        while (it.hasNext()) {
            hm = (Occupant) it.next();
            nick = hm.getNick();

            if (nick.equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public Collection<Occupant> getUsersInRoom(final ROOM roomType) {
        MultiUserChat mucTemp = getMultiUserChat(roomType);

        Collection<Occupant> collection = new LinkedList<Occupant>();
        Iterator<String> it = mucTemp.getOccupants();
        Occupant occupant = null;

        while (it.hasNext()) {

            occupant = mucTemp.getOccupant(it.next());
            collection.add(occupant);

        }
        return collection;
    }

    public XMPPConnection getXMPP() {
        return this.connection;
    }

    /**
     * Send file to an user
     *
     * @param jid
     * @param message
     */
    public void sendFile(String jid, String file_path) {
        logger.debug("Sending file: " + file_path + " to " + jid);
        // See if there is already a chat open
        Chat chat = cleverChatManagerListener.getChat(jid.toLowerCase());
        if (chat == null) {
            logger.debug("Chat toward " + jid + " not found... creating it");
            chat = connection.getChatManager().createChat(jid, new CleverChatListener(msgHandler, ldapClient, sessionKey));
            logger.debug("Chat toward " + jid + " created");
        }

        // Create the file transfer manager
        logger.debug("Creating file transfer manager");
        FileTransferManager manager = new FileTransferManager(connection);

        // Create the outgoing file transfer
        logger.debug("Creating outgoing file transfer");
        OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(jid);
        try {
            // Send the file
            logger.debug("Sending file " + file_path + " to " + jid);
            File file = new File(file_path);

            if (file.exists()) {
                transfer.sendFile(file, "You won't believe this!");
                int progress_int = 0;
                int now = 0;
                System.out.print("|");
                while (!transfer.isDone()) {
                    if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                        logger.debug("Error starting transfering " + transfer.getError());
                    } else if (transfer.getStatus().equals(FileTransfer.Status.in_progress)) {
                        double progress = transfer.getProgress();
                        progress_int = (int) (progress * 10);
                        //System.out.println("Progress_int = " + progress_int);
                        int how_many = progress_int - now;
                        //System.out.println("How_many = " + how_many);
                        for (int i = 1; i <= how_many; i++) {
                            System.out.print("==");
                        }
                        now = progress_int;
                        //System.out.println("Now = " + now);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        logger.error(ex.toString());
                    }
                }
                if (now < 10) {
                    for (int i = now + 1; i <= 10; i++) {
                        System.out.print("==");
                    }
                }
                System.out.println(">|");
                logger.debug("File sent");
            } else {
                logger.debug("File does not exist");
            }

        } catch (XMPPException ex) {
            logger.error("Error while sending file: " + file_path + " to " + jid);

        }
    }

    /**
     * Receive file from an user
     *
     * @param path
     */
    public String receiveFile(String path) {
        // Create the file transfer manager
        logger.debug("Creating file transfer manager");
        final FileTransferManager manager = new FileTransferManager(connection);

        // Create the listener
        logger.debug("Creating transfer listener - I will save file in " + path);
        myTransferListener m = new myTransferListener(logger, path);

        // Adding listener to manager
        logger.debug("Adding listener to manager");
        manager.addFileTransferListener(m);

        logger.debug("Getting my user");
        String myUser = this.connection.getUser();

        logger.debug("My user is: " + myUser);

        return myUser;
    }

}
