/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api;

import java.security.MessageDigest;
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
import org.clever.Common.XMPPCommunicator.CleverMessage.MessageType;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.CleverMessageHandler;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ExecOperation;
import org.clever.administration.ClusterManagerAdministrationTools;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.packet.DelayInformation;

/**
 * Usata per mandare comandi e smistare le risposte ai vari client Normalmente
 * viene instanziato e gestito da CleverCommandClientProvider
 *
 * @author maurizio
 */
public class CleverCommandClient implements CleverMessageHandler, CleverMessagesDispatcher {

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
            String nickname) {
        try {

            adminHostName = username;
            this.XMPPServer = XMPPServer;
            this.room = room;
            ldapClient = new LDAPClient("localhost", 389, "dc=clever,dc=unime,dc=it", "cn=admin,dc=clever,dc=unime,dc=it", "clever");
            //ldapClient = new LDAPClient();
            String usr;
            if (this.adminHostName.indexOf("-") != -1) {
                usr = this.adminHostName.substring(0, this.adminHostName.indexOf("-"));
            } else {
                usr = this.adminHostName;
            }

            utils = new X509Utils("./keystore/" + usr + ".p12", usr, usr.toCharArray());

            connectionXMPP.connect(XMPPServer, port);
            connectionXMPP.authenticate(username, passwd);

            connectionXMPP.joinInRoom(room, ConnectionXMPP.ROOM.SHELL, nickname);
            connectionXMPP.addChatManagerListener(this);
            return true;
        } catch (CleverException e) {
            log.error("Error on connect : " + e);
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
            } else {
                firstMsg = true;
                key = utils.generateSecretKey();
                connectionXMPP.getSessionKey().put(dst, key);
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
            log.debug("CleverMessage to XML: " + msg.toXML());
            log.debug("Message to XML: " + endMessage);
            try {
                connectionXMPP.getMultiUserChat(ConnectionXMPP.ROOM.SHELL).sendMessage(message);
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
            connectionXMPP.getMultiUserChat(ConnectionXMPP.ROOM.SHELL).sendMessage(msg.toXML());
        } catch (XMPPException ex) {
            log.error("Error in sending Clever Message. " + ex);
        }
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
        if (request == null) {
            //no request for this reply or error
            log.error("No request for message : " + message.getReplyToMsg());
            return;
        }
        try {
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
        log.warn("Dispatch not implemented : received an unexpected REQUEST message ?");
    }

    @Override
    public Object dispatchToIntern(MethodInvoker mi) throws CleverException {
        throw new CleverException("Unsupported Operation");
    }

    @Override
    public void handleMeasure(final CleverMessage message) {

    }

}
