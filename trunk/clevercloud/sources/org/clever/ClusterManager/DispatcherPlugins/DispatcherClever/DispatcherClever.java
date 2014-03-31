 /*
 *  Copyright (c) 2010 Antonio Nastasi
 *  Copyright (c) 2011 Marco Sturiale
 *  Copyright (c) 2013 Giuseppe Tricomi
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
package org.clever.ClusterManager.DispatcherPlugins.DispatcherClever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.Dispatcher.DispatcherPlugin;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.CleverMessage.MessageType;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP.ROOM;
import org.clever.Common.XMPPCommunicator.ErrorResult;
import org.clever.Common.XMPPCommunicator.ExecOperation;
import org.clever.Common.XMPPCommunicator.MethodConfiguration;
import org.clever.Common.XMPPCommunicator.OperationResult;
import org.clever.Common.XMPPCommunicator.Result;
import org.jdom.Element;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.safehaus.uuid.UUIDGenerator;


class RequestThread implements Runnable {
    private CleverMessage message;
    private DispatcherPlugin dispatcher;
    public RequestThread(DispatcherPlugin dispatcher, CleverMessage message)
    {
        this.message=message;
       this.dispatcher = dispatcher;
    }
    @Override
    public void run() {
         switch( this.message.getType() )

        {

                  case NOTIFY:
                      Notification notification=this.message.getNotificationFromMessage();
                      //Pass notification to dispatcher
                      dispatcher.handleNotification(notification);

                    break;
                  case ERROR:
                  case REPLY:
                    dispatcher.handleMessage( this.message );
                    break;
                  case REQUEST:

                    dispatcher.dispatch( this.message );
                    break;
                      

                   
        }
       }

}

public class DispatcherClever implements DispatcherPlugin,PacketListener {
    private Agent owner;
    private String version = "0.0.1";
    private String description = "Clever Dispatcher";
    private String name = "DispatcherClever";
    private ConnectionXMPP connectionXMPP = null;
    private ModuleCommunicator mc = null;
    private RequestsManager requestsManager = null;
    private Map<String, List<String>> notificationDelivery = new HashMap<String, List<String>>();
    private Map<String, MultiUserChat> agentMucs=new HashMap<String, MultiUserChat>();
    private UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
    private org.clever.ClusterManager.Brain.BrainInterface brain;
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger=null;
    private String pathLogConf="/sources/org/clever/ClusterManager/Dispatcher/log_conf/";
    private String pathDirOut="/LOGS/ClusterManager/Dispatcher";
    //########
    
    
    public DispatcherClever() { // !!!!
        super();
        
        //#############################################
        //Inizializzazione meccanismo di logging
        logger=Logger.getLogger("Dispatcher");    
        Log4J log =new Log4J();
       log.setLog4J(logger, pathLogConf, pathDirOut);
       //############################################# 
        
    }
    
    
    /**
     * This method manage a received clevermessage launching a separate thread
     *
     * @param message
     */

    //@Override
    public void scheduleMsg(CleverMessage msg)
    {
        //TODO: Check the number of active requestThreads;
          new Thread(new RequestThread(this,msg),"requestThread").start();
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void init(Element params,Agent owner) throws CleverException {
        requestsManager = new RequestsManager();
        logger = Logger.getLogger("DispatcherClever");
        this.connectionXMPP.addPresenceListener(ConnectionXMPP.ROOM.CLEVER_MAIN, this);
        this.brain= new org.clever.ClusterManager.Brain.SensorBrain(this);
    }

    @Override
    /**
     * This method will handle CleverMessage whose body is of 'exec' type
     * For other types of CleverMessage's body we plain to use other methods
     * such as RequestInformationDispatcher, etc...
     * MethodDispatcher must be called ONLY with 'exec' body
     *
     * @param message
     */
    public void dispatch(final CleverMessage message) {
        // Check if the message is for the current coordinator
        if (message.getDst().equals(connectionXMPP.getUsername())) {
            // Ok is for me. invoke locally and return
             MethodConfiguration methodConf = new MethodConfiguration(message.getBody(), message.getAttachments());

             MethodInvoker mi = new MethodInvoker(methodConf.getModuleName(),
                    methodConf.getMethodName(),
                    message.needsForReply(),
                    methodConf.getParams());
                CleverMessage cleverMsg = new CleverMessage();
                cleverMsg.setDst(message.getSrc());
                cleverMsg.setSrc(message.getDst());
                cleverMsg.setHasReply(false);
                cleverMsg.setReplyToMsg(message.getId());
                try {
                    Object obj = mc.invoke(mi);
                    if (message.needsForReply()) {
                        cleverMsg.setType( CleverMessage.MessageType.REPLY );
                        cleverMsg.setBody( new OperationResult( Result.ResultType.OBJECT,
                                                    obj,
                                                    methodConf.getModuleName(),
                                                    methodConf.getMethodName() ) );
                        cleverMsg.addAttachment( MessageFormatter.messageFromObject( obj ) );

                    } else {

                        return;
                    }

                } catch (CleverException ex) {
                    //TODO: use specialized CleverException for non such method

                    cleverMsg.setType(CleverMessage.MessageType.ERROR);
                    cleverMsg.setBody(new ErrorResult(Result.ResultType.ERROR,
                            (new CleverException(ex)).toString(),
                            methodConf.getModuleName(),
                            methodConf.getMethodName()));
                    cleverMsg.addAttachment(MessageFormatter.messageFromObject(ex));


                } finally {
                    connectionXMPP.sendMessage(message.getSrc(), cleverMsg);
                }
            

        } else {
            if (message.needsForReply()) {
                int idPendingRequest = requestsManager.addRequestPending(message, Request.Type.EXTERNAL);
                message.setId(idPendingRequest);
            }

            message.setSrc(connectionXMPP.getUsername());
            message.setDst(message.getDst());
            connectionXMPP.sendMessage(message.getDst(), message);
        }
    }

    @Override
    /**
     * Manage the reply message
     *
     * @param msg
     */
    public void handleMessage(final CleverMessage msg) {
        logger.debug("Dispatcher handle message: " + msg.toXML());
        logger.debug("Module name: " + msg.getBodyModule() + " and operation: " + msg.getBodyOperation());

       


        int idToReply = msg.getReplyToMsg();
        Request result = requestsManager.getRequest(idToReply);
        logger.debug("searching for: " + idToReply + " with result: " + result);
        switch (result.getType()) {
            case INTERNAL:
                try {
                    // Set result and unblock the thread
                    result.setReturnValue(msg.getObjectFromMessage());
                } catch (CleverException ex) {
                    result.setReturnValue(ex);
                    logger.error(ex);
                }
                break;
            case EXTERNAL:
                CleverMessage otherMsg = new CleverMessage();
                otherMsg.setType(msg.getType());
                otherMsg.setDst(requestsManager.getRequestPendingSrc(idToReply));
                otherMsg.setHasReply(false);
                otherMsg.setBody(msg.getBody());
                otherMsg.setAttachments(msg.getAttachments());
                otherMsg.setReplyToMsg(requestsManager.getRequestPendingId(idToReply));
                otherMsg.setSrc(connectionXMPP.getMultiUserChat(ROOM.SHELL).getNickname());
                connectionXMPP.sendMessage(otherMsg.getDst(), otherMsg);
                break;
         }



        requestsManager.deleteRequestPending(idToReply);
    }

    @Override
    public void setConnectionXMMP(ConnectionXMPP connectionXMPP) {
        this.connectionXMPP = connectionXMPP;
    }

    @Override
    public void setCommunicator(ModuleCommunicator mc) {
        this.mc = mc;
    }

    @Override
    public Object dispatchToExtern(MethodInvoker method, String to) throws CleverException{
        CleverMessage cleverMessage = new CleverMessage();
        cleverMessage.fillMessageFields(MessageType.REQUEST, connectionXMPP.getUsername(),
                to, true, method.getParams(), new ExecOperation(method.getMethodName(),
                method.getParams(), method.getModule()), 0);


        int id = requestsManager.addRequestPending(cleverMessage, Request.Type.INTERNAL);
        cleverMessage.setId(id);
        connectionXMPP.sendMessage(cleverMessage.getDst(), cleverMessage);
        return requestsManager.getRequest(id).getReturnValue();
    }

    /*
     * Test method for Cluster Manager
     */
    public String testMethod(String value){
        return "This is the value: " + value;
    }

    @Override
    public void subscribeNotification(String agentName, String notificationId) {
        //check for existent notification id
        List<String> agents = notificationDelivery.get(notificationId);
        if (agents == null) {
            agents = new ArrayList();
        }
        agents.add(agentName);
        this.notificationDelivery.put(notificationId, agents);
    }



    /*@Override
    public void handleNotification(CleverMessage msg) {
         //Send notification to corresponding agents using notificationId

      Notification notification=msg.getNotificationFromMessage();
      logger.debug("Received notification from "+msg.getSrc()+ "type "+notification.getId());
      List<String> agentsNameList=notificationDelivery.get(notification.getId());

        if (agentsNameList == null) {
            logger.info("No agents associated to notificationId " + notification.getId());
        } else {
            for (Object agent : agentsNameList) {
                try {
                    List params = new ArrayList();
                    params.add(notification);
                    MethodInvoker mi = new MethodInvoker((String) agent,
                            "handleNotification",
                            true,
                            params);

                    mc.invoke(mi);
                } catch (CleverException ex) {
                    logger.error("Error invoking agent handleNotification method " + ex);
                }
            }
        }


    }*/
    
    
    //NEWMONITOR
    /**
    * Manage the arrive of the new CleverMessage type: MEASURE
    * @param message CleverMessage type MEASURE
    */  
    @Override
    public void handleMeasure(final CleverMessage message) {
        
        String result = message.getAttachment(0);
        String src=message.getSrc();
        result= "<sourceHM name=\""+src+"\" >\n"+result+"\n</sourceHM>";

        
        logger.debug("Measure Received: "+ result);
        
        List params1 = new ArrayList();
        params1.add(result);
        
        try {
            owner.invoke("DatabaseManagerAgent", "insertMeasure", true, params1);
        } catch (CleverException ex) {
            logger.error("Send Measure to DatabaseManagerAgent failed: "+ ex);
        }
        
        
    }
    
    
    
    

    @Override
    public void handleNotification(Notification notification) {
        //Send notification to corresponding agents using notificationId
        logger.debug("Start handle notification");
        List<String> agentsNameList = notificationDelivery.get(notification.getId());

        if (agentsNameList == null) {
            logger.info("No agents associated to notificationId " + notification.getId());
        } else {
            for (Object agent : agentsNameList) {
                try {
                    List params = new ArrayList();
                    params.add(notification);
                    MethodInvoker mi = new MethodInvoker((String) agent,
                            "handleNotification",
                            true,
                            params);
                    logger.debug("dispatcher clever, agent:"+(String) agent);
                    mc.invoke(mi);
                } catch (CleverException ex) {
                    logger.error("Error invoking agent handleNotification method " + ex);
                }
            }
        }
        this.brain.handleNotification(notification);

    }

    //Possibile soluzione? aggiungere una inner class che genera un thread che prova a reinviare le notifiche che si non avevano trovato agenti 
    //registrati a quella notifica... mettere in coda e riprovarli almeno una volta dopo 30 secondi 


    @Override
    public Object dispatchToIntern(MethodInvoker method) throws CleverException {
        return mc.invoke(method);
    }

    @Override
    public void processPacket(Packet packet) {
    }
    
    public String receiveFile(String path){
        return connectionXMPP.receiveFile(path);
    }
    public void setOwner(Agent owner){
        this.owner=owner;
    }
 // METODI AGGIUNTI PER SOS E SAS///////////////////////////////////////////////
    
    public String joinAgentRoom(String agentName,String roomName,String roomPassword){
       // String nickName="SAS"+Math.abs(uuidGenerator.generateTimeBasedUUID().hashCode());
        MultiUserChat muc=connectionXMPP.joinInRoom(roomName, roomPassword, agentName);
        this.agentMucs.put(roomName, muc);
        logger.debug("JoinAgentRoom  roomName= "+roomName);
        return roomName;
    }
    
    //@Override
    public String joinAgentRoom(String agentName,String roomPassword){
        String roomName=agentName+"-"+Math.abs(uuidGenerator.generateTimeBasedUUID().hashCode())+"@conference."+connectionXMPP.getServer();
        //String nickName="SAS"+Math.abs(uuidGenerator.generateTimeBasedUUID().hashCode());
        MultiUserChat muc=connectionXMPP.joinInRoom(roomName, roomPassword, agentName);// agentName);
        this.agentMucs.put(roomName, muc);
        logger.debug("JoinAgentRoom  roomName= "+roomName);
        return roomName;
    }

    //@Override
    public void sendMessageAgentRoom(String roomName, String message) {
        MultiUserChat muc=this.agentMucs.get(roomName);
        try {
            muc.sendMessage(message);
        } catch (XMPPException ex) {
            logger.error("Error sending message "+ex);
        }
    }

    //@Override
    public void leaveAgentRoom(String roomName) {
        MultiUserChat muc=this.agentMucs.get(roomName);
        muc.leave();
        this.agentMucs.remove(roomName);
    }
    
    
}
