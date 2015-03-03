/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.clever.Common.Communicator.Request;
import org.clever.Common.Communicator.CleverMessagesDispatcher;
import org.clever.Common.Communicator.InvocationCallback;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Communicator.RequestsManager;
import org.clever.Common.Communicator.ThreadMessageDispatcher;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.SecureXMPPCommunicator.LDAPClient;
import org.clever.Common.SecureXMPPCommunicator.SecureExtension;
import org.clever.Common.SecureXMPPCommunicator.X509Utils;
import org.clever.Common.XMPPCommunicator.CleverChatListener;
import org.clever.Common.XMPPCommunicator.CleverMessage.MessageType;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.CleverMessageHandler;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ExecOperation;
import org.clever.administration.ClusterManagerAdministrationTools;
import org.clever.administration.openam.OpenAmSessionClient;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.DelayInformation;

/**
 * Usata per mandare comandi e smistare le risposte ai vari client Normalmente
 * viene instanziato e gestito da CleverCommandClientProvider
 *
 * @author maurizio
 */
public class CleverCommandClient implements MessageListener, CleverMessageHandler, CleverMessagesDispatcher {

    private static final Logger log = Logger.getLogger(CleverCommandClient.class);

    private String XMPPServer;
    private String room;
    private LDAPClient ldapClient;
    private X509Utils utils;
    private String adminHostName;
    private ConnectionXMPP connectionXMPP;

    private HashMap<Integer, InvocationCallback> commandsSent = null;

    private RequestsManager requestManager;

    private ThreadMessageDispatcher dispatcher;

    public CleverCommandClient(Integer maxMessages, Integer maxThreadHandlers) {

        commandsSent = new HashMap<Integer, InvocationCallback>();
        requestManager = new RequestsManager();
        connectionXMPP = new ConnectionXMPP();
        dispatcher = new ThreadMessageDispatcher(this, maxMessages, maxThreadHandlers);
        dispatcher.start();
    }

    /**
     * Il client e' attivo (connessone XMPP e autenticato)
     *
     * @return
     */
    public boolean isActive() {
        XMPPConnection c = connectionXMPP.getXMPP();
        return (c == null ? false : c.isAuthenticated());
    }

    public ConnectionXMPP getConnectionXMPP() {
        return connectionXMPP;
    }

    public void setConnectionXMPP(ConnectionXMPP connectionXMPP) {
        this.connectionXMPP = connectionXMPP;
    }

    public synchronized boolean connect(String XMPPServer,
            String username,
            String passwd,
            int port,
            String room,
            String nickname,
            ConnectionXMPP.TransmissionModes mode) {
        try {

            adminHostName = username;
            this.XMPPServer = XMPPServer;
            this.room = room;
            //ldapClient = new LDAPClient("localhost", 389, "dc=clever,dc=unime,dc=it", "cn=admin,dc=clever,dc=unime,dc=it", "clever");
            ldapClient = new LDAPClient();
            String usr;
            if (this.adminHostName.indexOf("-") != -1) {
                usr = this.adminHostName.substring(0, this.adminHostName.indexOf("-"));
            } else {
                usr = this.adminHostName;
            }

            utils = new X509Utils("./keystore/" + usr + ".p12", usr, usr.toCharArray());

            connectionXMPP.connect(XMPPServer, port, mode);
            connectionXMPP.authenticate(username, passwd);

            MultiUserChat chat = connectionXMPP.joinInRoom(room, ConnectionXMPP.ROOM.SHELL, nickname);
            connectionXMPP.addChatManagerListener(this);
            return true;
        } catch (CleverException e) {
            log.error("Error on connect : " + e);
            return false;

        } catch (Exception ex) {
            log.error("Error on connect : " + ex);
            return false;

        }
    }

    private void sendEncryptedRequest(final CleverMessage msg) {
        try {
            Message message = new Message();
            message.setSubject("Messaggio incapsulato");
            message.setType(Message.Type.groupchat);
            message.setTo(room);
            message.setBody("[This message is encrypted]");

            X509Certificate dstCert;
            PublicKey pubKey;

            String target = connectionXMPP.getActiveCC(ConnectionXMPP.ROOM.SHELL);

            String dst = msg.getDst();
            /**
             * * OpenAM add authentication token **
             */
            addToken(msg);
            if (!target.equals(dst)) {
                dst = target;
            }

            dstCert = ldapClient.searchCert("", "uid=" + dst);
            if (dstCert == null) {
                System.out.print(" Error - No Certificate in LDAP Server ");
                return;
            } else {
                pubKey = dstCert.getPublicKey();
            }

            byte[] key;
            byte[] encryptedBytes;
            String encryptedSessionKey;

            boolean firstMsg = false;

            if (connectionXMPP.getSessionKey().get(dst) != null) {
                key = connectionXMPP.getSessionKey().get(dst);
                log.debug("Session key exists:\nDst: " + dst + " key: " + X509Utils.bytesToHex(key));
            } else {
                firstMsg = true;
                key = utils.generateSecretKey();
                connectionXMPP.getSessionKey().put(dst, key);
                log.debug("Session key generated:\nDst: " + dst + " key: " + X509Utils.bytesToHex(key));
            }

            if (firstMsg) {
                String stringKey = new String(Hex.encode(key));
                SecureExtension sessionKeyExtension = new SecureExtension("sessionkey");
                encryptedSessionKey = utils.encryptToString(stringKey, (RSAPublicKey) pubKey);

                if (encryptedSessionKey != null) {
                    sessionKeyExtension.setData(encryptedSessionKey);
                    message.addExtension(sessionKeyExtension);
                }
            }

            String encryptedMsg;

            Date date = new Date();
            DelayInformation delayInformation = new DelayInformation(date);
            SecureExtension encryptedExtension = new SecureExtension();
            encryptedBytes = utils.sEncrypt(true, msg.toXML().getBytes(), key);
            encryptedMsg = new String(Hex.encode(encryptedBytes));

            if (encryptedMsg != null) {
                encryptedExtension.setData(encryptedMsg);
                message.addExtension(encryptedExtension);
            }
            message.addExtension(delayInformation);
            log.debug("CleverMessage to XML:\n " + msg.toXML());
            log.debug("Message to XML:\n " + message.toXML());
            try {
                connectionXMPP.getMultiUserChat(ConnectionXMPP.ROOM.SHELL).sendMessage(message);
            } catch (XMPPException ex) {
                System.out.println("Error in sending Clever Message. " + ex);
            }
        } catch (CleverException ex) {
            log.error(ex);
        }
    }

    public void sendSignedRequest(final CleverMessage msg) {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        try {
            Message message = new Message();
            message.setType(Message.Type.groupchat);
            message.setTo(room);
            /**
             * * OpenAM add authentication token **
             */
            addToken(msg);
            message.setBody(msg.toXML());

            SecureExtension signedExtension = new SecureExtension("signed");
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA");
            } catch (java.security.NoSuchAlgorithmException ex) {
                java.util.logging.Logger.getLogger(ClusterManagerAdministrationTools.class.getName()).log(Level.SEVERE, null, ex);
            }

            md.reset();
            try {
                md.update(msg.toXML().getBytes("UTF-8"), 0, msg.toXML().length());
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(ClusterManagerAdministrationTools.class.getName()).log(Level.SEVERE, null, ex);
            }
            String digest = new String(Base64.encode(md.digest()));
            signedExtension.setData(digest);
            message.addExtension(signedExtension);
            Date date = new Date();
            DelayInformation delayInformation = new DelayInformation(date);
            message.addExtension(delayInformation);
            
            String endMessage = message.toXML();
            //String endMessage = utils.signXML(msg.toXML());
            log.debug("CleverMessage to XML:\n " + msg.toXML());
            log.debug("Message to XML:\n " + endMessage);
            try {
                MultiUserChat chat = connectionXMPP.getMultiUserChat(ConnectionXMPP.ROOM.SHELL);
                chat.sendMessage(message);
            } catch (XMPPException ex) {
                System.out.println("Error in sending Clever Message. " + ex);
            }
        } catch (CleverException ex) {
            log.error(ex);
        }
    }

    /**
     * Send messag to MUC . Destination active ClusterManager
     *
     * @param msg
     */
    private synchronized void sendRequest(final CleverMessage msg) throws CleverException {
        try {
            /**
             * * OpenAM send authentication token **
             */
            msg.setAuthToken(OpenAmSessionClient.getInstance().getToken());
            log.debug("CleverMessage to XML: " + msg.toXML());

            connectionXMPP.getMultiUserChat(ConnectionXMPP.ROOM.SHELL).sendMessage(msg.toXML());
        } catch (XMPPException ex) {
            log.error("Error in sending Clever Message. " + ex);
        }
    }

    private void addToken(CleverMessage message) {
        message.setAuthToken(OpenAmSessionClient.getInstance().getToken());
    }

    /**
     *
     * @param agent: the entity which executes the command (e.g. ClusterManager)
     * @param command: the target command
     * @param params:the params of the command
     * @param showXML:It sets if show the XML request/response messages.
     * @throws CleverException
     */
    public void execAdminCommand(final InvocationCallback cleverCommand,
            final String target,
            final String agent,
            final String command,
            final ArrayList params,
            final boolean showXML) throws CleverException {

        CleverMessage requestMsg = new CleverMessage();

        requestMsg.fillMessageFields(MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation(command, params, agent), 0);

        int id = requestManager.addRequestPending(requestMsg, Request.Type.INTERNAL);
        Request request = requestManager.getRequest(id);
        //request.setAsync(true);
        request.setCallback(cleverCommand); //store invoker to async reply
        requestMsg.setId(id);
        sendRequest(requestMsg);
        log.debug("Clever Request Message (aSync): \n" + requestMsg.toXML());

    }

    /**
     *
     * @param agent: the entity which executes the command (e.g. ClusterManager)
     * @param command: the target command
     * @param params:the params of the command
     * @param showXML:It sets if show the XML request/response messages.
     * @throws CleverException
     */
    public void execAdminCommand(final InvocationCallback cleverCommand,
            final String target,
            final String agent,
            final String command,
            final ArrayList params,
            final boolean showXML,
            final String mode) throws CleverException {

        CleverMessage requestMsg = new CleverMessage();

        requestMsg.fillMessageFields(MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation(command, params, agent), 0);

        int id = requestManager.addRequestPending(requestMsg, Request.Type.INTERNAL);
        Request request = requestManager.getRequest(id);
        //request.setAsync(true);
        request.setCallback(cleverCommand); //store invoker to async reply
        requestMsg.setId(id);
        if (mode.equalsIgnoreCase(Environment.MSG_MODE_ENCRYPTED)) {
            log.debug("Send encrypted message");
            sendEncryptedRequest(requestMsg);
        } else if (mode.equalsIgnoreCase(Environment.MSG_MODE_SIGNED)) {
            //signed
            log.debug("Send signed message");
            sendSignedRequest(requestMsg);
        } else {
            log.debug("Send plain message");
            sendRequest(requestMsg);
        }
        log.debug("Clever Request Message (aSync): \n" + requestMsg.toXML());

    }

    /**
     *
     * @param agent: the entity which executes the command (e.g. ClusterManager)
     * @param command: the target command
     * @param params:the params of the command
     * @param showXML:It sets if show the XML request/response messages.
     * @throws CleverException
     */
    public Object execSyncAdminCommand(
            final String target,
            final String agent,
            final String command,
            final ArrayList params,
            final boolean showXML) throws CleverException {

        CleverMessage requestMsg = new CleverMessage();

        requestMsg.fillMessageFields(MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation(command, params, agent), 0);

        int id = requestManager.addSyncRequestPending(requestMsg, Request.Type.INTERNAL, 1800000); //TODO: retrieve from configuration
        Request request = requestManager.getRequest(id);

        requestMsg.setId(id);
        sendRequest(requestMsg);
        log.debug("Clever Request Message: \n" + requestMsg.toXML());
        //wait for response (sync invocation)
        return request.getReturnValue();

    }

    /**
     *
     * @param agent: the entity which executes the command (e.g. ClusterManager)
     * @param command: the target command
     * @param params:the params of the command
     * @param showXML:It sets if show the XML request/response messages.
     * @throws CleverException
     */
    public Object execSyncAdminCommand(
            final String target,
            final String agent,
            final String command,
            final ArrayList params,
            final boolean showXML,
            final String mode) throws CleverException {

        CleverMessage requestMsg = new CleverMessage();

        requestMsg.fillMessageFields(MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation(command, params, agent), 0);

        int id = requestManager.addSyncRequestPending(requestMsg, Request.Type.INTERNAL, 1800000); //TODO: retrieve from configuration
        Request request = requestManager.getRequest(id);

        requestMsg.setId(id);

        if (mode.equalsIgnoreCase(Environment.MSG_MODE_ENCRYPTED)) {
            log.debug("Sending encrypted message");
            sendEncryptedRequest(requestMsg);
        } else if (mode.equalsIgnoreCase(Environment.MSG_MODE_SIGNED)) {
            //signed
            log.debug("Sending signed message");
            sendSignedRequest(requestMsg);
        } else {
            log.debug("Sending plain message");
            sendRequest(requestMsg);
        }
        log.debug("Clever Request Message: \n" + requestMsg.toXML());
        //wait for response (sync invocation)
        return request.getReturnValue();

    }

    /**
     * Close client command and release all resources
     */
    public void close() {
        //TODO: call ClientCommandProvider to choose the correct action
        this.connectionXMPP.closeConnection();
        this.dispatcher.close();

    }

    //invocato direttamente dalla coda XMPP
    @Override
    public synchronized void handleCleverMessage(final CleverMessage cleverMessage) {
        try {
            log.debug("Received:\n" + cleverMessage.toXML());
        } catch (CleverException ex) {
            java.util.logging.Logger.getLogger(CleverCommandClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        dispatcher.pushMessage(cleverMessage); //insert in message queue    

    }

    //invocati dai thread gestori dei messaggi
    @Override
    public void handleNotification(Notification notification) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleMessage(CleverMessage message) {
        Request request = requestManager.getRequest(message.getReplyToMsg()); // retrieve request using replytomsg  field
        try {
            if (request == null) {
                //no request for this reply or error
                log.error("No request for message : " + message.getReplyToMsg());
                log.error("Message : " + message.toXML());
                return;
            }

            if (request.isAsync()) {
                log.debug("Async request serving ... ");
                request.getCallback().handleMessage(message.getObjectFromMessage()); //TODO: handle error message
            } else {
                //sync invocation
                //TODO : do an overloaded method setReturnValue(CleverMessage c)
                log.debug("Sync request serving ... ");
                request.setReturnValue(message.getObjectFromMessage());

            }
        } catch (CleverException ex) {
            if (ex.getInternalException() != null) {
                log.error("from CleverCommandClient: " + ex);
            } else {
                log.error("from CleverCommandClient: " + ex.getInternalException());
            }
            if (!request.isAsync()) {
                request.setReturnValue(ex); //the ex CleverException will be raised by getReturnValue method
            } else {
                request.getCallback().handleMessageError(ex);
            }
        }
    }

    @Override
    public void dispatch(CleverMessage message) {
        //log.warn("Dispatch not implemented : received an unexpected REQUEST message ?");
        log.debug("Dispatch message to handler");
        handleMessage(message);
    }

    @Override
    public Object dispatchToIntern(MethodInvoker mi) throws CleverException {
        throw new CleverException("Unsupported Operation");
    }

    @Override
    public void handleMeasure(final CleverMessage message) {

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
                connectionXMPP.getSessionKey().put(src, sessionKeyBytes);
                log.debug("Saving session key: " + X509Utils.bytesToHex(sessionKeyBytes));
                //writeKey(sessionKeyBytes);
            }

            byte[] decryptedBytes = null;
            String decrypted = null;
            if (encryptedExtension != null) {
                isEncrypted = true;
                if (!isSessionKey) //sessionKeyBytes = readKey();
                {
                    sessionKeyBytes = connectionXMPP.getSessionKey().get(src);
                }
                byte[] rawInput = Hex.decode(encryptedExtension.getData());
                log.debug("Raw input: " + X509Utils.bytesToHex(rawInput));
                log.debug("Session key: " + X509Utils.bytesToHex(sessionKeyBytes));

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
                cert = ldapClient.searchCert("", "uid=" + src);

                if (cert == null) {
                    log.debug(" ERROR - There is no Certificate in LDAP server ");
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
                cleverMessage = new CleverMessage(message.getBody());
                try {
                    md.update(cleverMessage.toXML().getBytes("UTF-8"), 0, cleverMessage.toXML().length());
                } catch (UnsupportedEncodingException ex) {
                    java.util.logging.Logger.getLogger(CleverChatListener.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CleverException ex) {
                    log.error("Outputting message:\n", ex);
                }

                String digest = new String(Base64.encode(md.digest()));

                if (digest.equals(signedExtension.getData())) {
                    isVerified = true;
                } else {
                    isVerified = false;
                }

                if (isVerified) {
                    log.debug("[This message is signed]");
                } else {
                    log.debug("[The signature in not verified]");
                }
                handleCleverMessage(cleverMessage);
                return;
            }

            if (isEncrypted) {
                cleverMessage = new CleverMessage(decrypted);
            } else {
                cleverMessage = new CleverMessage(message.getBody());
            }
            handleCleverMessage(cleverMessage);
        } catch (Exception ex) {
            log.error("Message Listener fails:\n", ex);
        }
    }
}
