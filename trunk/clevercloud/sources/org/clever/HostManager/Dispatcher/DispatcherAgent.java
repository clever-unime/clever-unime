/*
 * The MIT License
 *
 * Copyright 2011 Alessio Di Pietro.
 * Copyright 2012 Marco Carbone
 *
 */
package org.clever.HostManager.Dispatcher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.NotificationOperation;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/**
 *
 * @author alessiodipietro
 */
class NotificationThread extends Thread implements PacketListener 
{
    Logger logger =null;
    private Queue<CleverMessage> queue = new LinkedList();
    private ConnectionXMPP connectionXMPP;
    boolean queueNotEmpty = false;
    boolean CMisPresent = false;
    private int notificationThreshold;
            
    

    public NotificationThread(ConnectionXMPP connectionXMPP, int notificationThreshold) {
        logger=Logger.getLogger("NotificationThread");
        this.connectionXMPP = connectionXMPP;
        this.connectionXMPP.addPresenceListener(ConnectionXMPP.ROOM.CLEVER_MAIN, this);
        this.notificationThreshold = notificationThreshold;
        this.setName("NotificationThread");
    }

    public synchronized void sendCleverMsg(CleverMessage msg) {
        if (queue.size() > this.notificationThreshold) {
            queue.poll();
        }
        queue.add(msg);
        queueNotEmpty = true;
        this.notifyAll();
    }

    @Override
    public synchronized void run() {
        logger.debug("Start NotificationThread run!");
        String target = null;
        while (true) {
            //no messages
            if (queue.isEmpty()) {
                try {
                    queueNotEmpty = false;
                    while (!queueNotEmpty) {
                        this.wait();
                    }
                } catch (InterruptedException ex) {
                    logger.error("InterruptedException: "+ex);
                }
            }
            target = connectionXMPP.getActiveCC(ConnectionXMPP.ROOM.CLEVER_MAIN);
            //no active cc
            while(target.isEmpty()) {
                try {
                    CMisPresent = false;
                    while (!CMisPresent) {
                        wait();
                    }
                    target = connectionXMPP.getActiveCC(ConnectionXMPP.ROOM.CLEVER_MAIN);
                } catch (InterruptedException ex) {
                    logger.error("InterruptedException: "+ex);
                }
            }
            CleverMessage msg = queue.poll();
            msg.setDst(target);
            logger.debug("X?X target="+target);
            connectionXMPP.sendMessage(target, msg);

        }
    }

    @Override
    public synchronized void processPacket(Packet packet) {
        if (connectionXMPP.getActiveCC(ConnectionXMPP.ROOM.CLEVER_MAIN) != null) {
            CMisPresent = true;
            this.notifyAll();
        }
    }
    
   
}

public class DispatcherAgent extends Agent 
{    
    //private Class cl = null;
    private ConnectionXMPP connectionXMPP = null;
    private NotificationThread notificationThread;
    private int notificationsThreshold;    
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/Dispatcher/log_conf/";
    private String pathDirOut="/LOGS/HostManager/DispatcherAgentHm";
    //########
    

    public DispatcherAgent(ConnectionXMPP connectionXMPP, int notificationsThreshold) throws CleverException
    {   super();
        
        this.connectionXMPP = connectionXMPP;
        this.notificationsThreshold = notificationsThreshold;
        
        //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("DispatcherAgentHm");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
    }
    
    
    public DispatcherAgent() throws CleverException{
        super();
        //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("DispatcherAgentHm");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
       
    }
    
@Override
public void initialization() throws CleverException
{
    
    super.setAgentName("DispatcherAgentHm");    
    super.start();
    
    notificationThread = new NotificationThread(connectionXMPP, notificationsThreshold);
    notificationThread.start();    
    
    String hostid=this.connectionXMPP.getHostName();
    Notification notification=new Notification();
    notification.setId("PRESENCE/HM");
    logger.debug("?=)** hostId= "+hostid);
    notification.setHostId(hostid);
    this.sendNotification(notification);
    
    
    //logger.debug("Debug Message! su DispatcherAgentHm.java");
    //logger.info("Info Message! su DispatcherAgentHm.java");
    //logger.warn("Warn Message! su DispatcherAgentHm.java");
    //logger.error("Error Message! su DispatcherAgentHm.java");
    //logger.fatal("Fatal Message! su DispatcherAgentHm.java");
    
    
    
    
    
}

    @Override
    public void sendNotification(Notification notification) {

        //TODO: send notification (via CleverMsg) to active CC
        logger.debug("preparing notify clever message");

        CleverMessage cleverMsg = new CleverMessage();
        List attachments=new ArrayList();
        attachments.add(notification.getBody());
        cleverMsg.fillMessageFields(CleverMessage.MessageType.NOTIFY, this.connectionXMPP.getUsername(), attachments, new NotificationOperation(connectionXMPP.getUsername(), notification.getAgentId(), notification.getId()));
        
        
        
       
        
        
        //cleverMsg.setBody(MessageFormatter.messageFromObject(notification));
        notificationThread.sendCleverMsg(cleverMsg);
        logger.debug("notification sent");


    }

    @Override
    public Class getPluginClass() {
        return this.getClass();
    }

    @Override
    public Object getPlugin() {
        return this.pluginInstantiation;
    }

    /**
     * @param notificationThreshold the notificationThreshold to set
     */
    public void setNotificationThreshold(int notificationsThreshold) {
        this.notificationsThreshold = notificationsThreshold;
    }
    
    @Override
   public void shutDown()
    {
        
    }
   
   
   
}
