/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration;

import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Request;
import org.clever.Common.Exceptions.CleverException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.Base64;
import org.clever.Common.XMPPCommunicator.CleverMessage.MessageType;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.CleverMessageHandler;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ExecOperation;
import org.clever.Common.SecureXMPPCommunicator.LDAPClient;
import org.clever.Common.SecureXMPPCommunicator.SecureExtension;
import org.clever.Common.SecureXMPPCommunicator.X509Utils;
import org.clever.administration.commands.CleverCommand;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.packet.DelayInformation;

public class ClusterManagerAdministrationTools implements CleverMessageHandler {

    private String adminHostName;
    private ConnectionXMPP conn;
    private Request request;
    static private ClusterManagerAdministrationTools managerAdministration = null;
    private HashMap<Integer, CleverCommand> commandsSent = null;
    private Logger logger = null;

    private String XMPPServer;
    private LDAPClient ldapClient;
    private X509Utils utils;
    //private Map<String, byte[]> sessionKey ;
    private String room;
    private long timeStart;
    private long timeStop;

    public ClusterManagerAdministrationTools() {
        commandsSent = new HashMap<Integer, CleverCommand>();
        logger = Logger.getLogger("ClusterManagerAdministrationTools");

    }

    static public ClusterManagerAdministrationTools instance() {
        if (managerAdministration == null) {
            managerAdministration = new ClusterManagerAdministrationTools();
        }

        return managerAdministration;
    }

    public ConnectionXMPP getConnectionXMPP() {
        return conn;
    }

    public boolean connect(String XMPPServer,
            String username,
            String passwd,
            int port,
            String room,
            String nickname, 
            ConnectionXMPP.TransmissionModes mode) {

        adminHostName = username;
        this.XMPPServer = XMPPServer;
        this.room = room;
        try {
            //ldapClient = new LDAPClient("localhost", 389, "dc=clever,dc=unime,dc=it", "cn=admin,dc=clever,dc=unime,dc=it", "clever");
            ldapClient = new LDAPClient();
            String usr = null;
            if (this.adminHostName.indexOf("-") != -1) {
                usr = this.adminHostName.substring(0, this.adminHostName.indexOf("-"));
            } else {
                usr = this.adminHostName;
            }

            utils = new X509Utils("./keystore/" + usr + ".p12", usr, usr.toCharArray());

            conn = new ConnectionXMPP();
            conn.connect(XMPPServer, port, mode);
            conn.authenticate(username, passwd);

            conn.joinInRoom(room, ConnectionXMPP.ROOM.SHELL, nickname);
            conn.addChatManagerListener(this);
            return true;
        } catch (CleverException e) {
            logger.debug("Error in connection : " + e.getMessage());
            return false;
        }
        catch (Exception ex){
            logger.debug("Error in connection : " + ex.getMessage());
            return false;
        }
    }

    private void sendRequest(final CleverMessage msg) throws CleverException {
        try {
            /*** OpenAM send authentication token ***/
            Message message = new Message();
            message.setType(Message.Type.groupchat);
            message.setTo(room);
            message.setBody(msg.toXML());
            logger.debug("CleverMessage to XML: " + msg.toXML());
            logger.debug("Message to XML: " + message.toXML());
            conn.getMultiUserChat(ConnectionXMPP.ROOM.SHELL).sendMessage(message);
            
        } catch (XMPPException ex) {
            System.out.println("Error in sending Clever Message. " + ex);
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

            String target = this.conn.getActiveCC(ConnectionXMPP.ROOM.SHELL);

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

            if (this.conn.getSessionKey().get(dst) != null) {
                key = this.conn.getSessionKey().get(dst);
            } else {
                firstMsg = true;
                key = utils.generateSecretKey();
                this.conn.getSessionKey().put(dst, key);
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
                conn.getMultiUserChat(ConnectionXMPP.ROOM.SHELL).sendMessage(message);
            } catch (XMPPException ex) {
                System.out.println("Error in sending Clever Message. " + ex);
            }
        } catch (CleverException ex) {
            logger.error(ex);
        }
    }

    public void sendSignedRequest(final CleverMessage msg) {
        this.timeStart = System.currentTimeMillis();
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        try {
            Message message = new Message();
            message.setType(Message.Type.groupchat);
            message.setTo(room);
            message.setBody(msg.toXML());

            SecureExtension signedExtension = new SecureExtension("signed");
            X509Utils x = new X509Utils(false);
            String signature = x.signToString(msg.toXML());
            signedExtension.setData(signature);
            message.addExtension(signedExtension);
            Date date = new Date();
            DelayInformation delayInformation = new DelayInformation(date);
            message.addExtension(delayInformation);

            String endMessage = message.toXML();
            //String endMessage = utils.signXML(msg.toXML());
            logger.debug("CleverMessage to XML: " + msg.toXML());
            logger.debug("Message to XML: " + endMessage);
            try {
                conn.getMultiUserChat(ConnectionXMPP.ROOM.SHELL).sendMessage(message);
            } catch (XMPPException ex) {
                System.out.println("Error in sending Clever Message. " + ex);
            }
        } catch (CleverException ex) {
            logger.error(ex);
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
    public void execAdminCommand(final CleverCommand cleverCommand,
            final String target,
            final String agent,
            final String command,
            final ArrayList params,
            final boolean showXML) throws CleverException {

        request = null; //this set the command as async command (see handleCleverMessage)
        CleverMessage requestMsg = new CleverMessage();

        requestMsg.fillMessageFields(MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation(command, params, agent), 0);

        commandsSent.put(Integer.valueOf(requestMsg.getId()), cleverCommand);
        sendRequest(requestMsg);
        if (showXML) {
            System.out.print("Clever Request Message: \n" + requestMsg.toXML());
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
    public void execAdminCommand(final CleverCommand cleverCommand,
            final String target,
            final String agent,
            final String command,
            final ArrayList params,
            final boolean showXML,
            final int mode) throws CleverException {

        request = null; //this set the command as async command (see handleCleverMessage)
        CleverMessage requestMsg = new CleverMessage();

        requestMsg.fillMessageFields(MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation(command, params, agent), 0);

        commandsSent.put(Integer.valueOf(requestMsg.getId()), cleverCommand);

        switch (mode) {
            case 1:
                //simple
                sendRequest(requestMsg);
                break;
            case 2:
                //encrypted
                sendEncryptedRequest(requestMsg);
                break;
            case 3:
                //signed
                sendSignedRequest(requestMsg);
                break;
            default:
                sendRequest(requestMsg);
                break;
        }

        //sendRequest( requestMsg );
        if (showXML) {
            System.out.print("Clever Request Message: \n" + requestMsg.toXML());
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
    public Object execSyncAdminCommand(final CleverCommand cleverCommand,
            final String target,
            final String agent,
            final String command,
            final ArrayList params,
            final boolean showXML) throws CleverException {
        CleverMessage requestMsg = new CleverMessage();

        requestMsg.fillMessageFields(MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation(command, params, agent), 0);

        request = new Request(requestMsg.getId(), 0); //this set the command as sync command (see handleCleverMessage)

        sendRequest(requestMsg);
        if (showXML) {
            System.out.print("Clever Request Message: \n" + requestMsg.toXML());

        }
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
    public Object execSyncAdminCommand(final CleverCommand cleverCommand,
            final String target,
            final String agent,
            final String command,
            final ArrayList params,
            final boolean showXML,
            final int mode) throws CleverException {
        CleverMessage requestMsg = new CleverMessage();

        requestMsg.fillMessageFields(MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation(command, params, agent), 0);

        request = new Request(requestMsg.getId(), 0); //this set the command as sync command (see handleCleverMessage)

        switch (mode) {
            case 1:
                //simple
                sendRequest(requestMsg);
                break;
            case 2:
                //encrypted
                sendEncryptedRequest(requestMsg);
                break;
            case 3:
                //signed
                sendSignedRequest(requestMsg);
                break;
            default:
                sendRequest(requestMsg);
                break;
        }
        if (showXML) {
            System.out.print("Clever Request Message: \n" + requestMsg.toXML());

        }
        return request.getReturnValue();

    }
    
    @Override
    public void handleCleverMessage(final CleverMessage cleverMessage) {
        try {
            logger.debug("Received:\n" + cleverMessage.toXML());

        } catch (CleverException ex) {
            java.util.logging.Logger.getLogger(ClusterManagerAdministrationTools.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        CleverCommand cleverCommand = null;
        try {
            if (request != null) //this is a reply to a sync command
            {

                request.setReturnValue(cleverMessage.getObjectFromMessage()); //TODO : do an overloaded method setReturnValue(CleverMessage c)

            } else //this is a reply to an async command
            {
                cleverCommand = commandsSent.get(Integer.valueOf(cleverMessage.getReplyToMsg()));
                if (cleverMessage.getType() == MessageType.REPLY || cleverMessage.getType() == MessageType.ERROR) {
                    cleverCommand.handleMessage(cleverMessage.getObjectFromMessage());
                }

            }
        } catch (CleverException ex) {

            if (ex.getInternalException() != null) {
                logger.error("from ClusterManagerAdministrationTools: " + ex);
            } else {
                logger.error("from ClusterManagerAdministrationTools: " + ex.getInternalException());
            }
            if (request != null) {
                request.setReturnValue(ex); //the ex CleverException will be raised by getReturnValue method
            } else {
                cleverCommand.handleMessageError(ex);
            }
        }
    }
}
