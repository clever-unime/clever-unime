/*
 * Copyright 2014 Università di Messina
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
 * The MIT License
 *
 * Copyright 2011 Alessio Di Pietro.
 * Copyright 2012 Marco Carbone
 * Copyright 2013 Nicola Peditto
 * Copyright 2013 Carmelo Romeo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.clever.HostManager.Dispatcher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ErrorResult;
import org.clever.Common.XMPPCommunicator.MethodConfiguration;
import org.clever.Common.XMPPCommunicator.NotificationOperation;
import org.clever.Common.XMPPCommunicator.OperationResult;
import org.clever.Common.XMPPCommunicator.Result;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;


import org.clever.Common.Measure.*;
/**
 *
 * @author alessiodipietro
 */
class NotificationThread extends Thread implements PacketListener 
{

    private Queue<CleverMessage> queue = new LinkedList();
    private ConnectionXMPP connectionXMPP;
    boolean queueNotEmpty = false;
    boolean CMisPresent = false;
    private int notificationThreshold;
    private Logger logger=null;

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
        logger.info("Start NotificationThread run!");
        String target = null;
        while (true) {
            //no messages
            logger.info("Start NotificationThread run!1");
            if (queue.isEmpty()) {
                try {
                    logger.info("Start NotificationThread run!2");
                    queueNotEmpty = false;
                    logger.info("Start NotificationThread run!3");
                    while (!queueNotEmpty) {
                        logger.info("Start NotificationThread run!4");
                        this.wait();
                    }
                } catch (InterruptedException ex) {
                    logger.error("InterruptedException: "+ex);
                }
            }
            target = connectionXMPP.getActiveCC(ConnectionXMPP.ROOM.CLEVER_MAIN);
            logger.debug("dispatcherHM  "+target);
            //no active cc
            if (target == null) {
                try {
                    CMisPresent = false;
                    while (!CMisPresent) {
                        wait(5000);
                        this.processPacket(null);
                    }
                    target = connectionXMPP.getActiveCC(ConnectionXMPP.ROOM.CLEVER_MAIN);
                    logger.debug("dispatcherHM  "+target);
                } catch (InterruptedException ex) {
                    logger.error("InterruptedException: "+ex);
                }
            }
            CleverMessage msg = queue.poll();
            msg.setDst(target);
            logger.debug("Send notification:"+msg.getNotificationFromMessage());
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
   

    public DispatcherAgent(ConnectionXMPP connectionXMPP, int notificationsThreshold) throws CleverException
    {   super();
        
        this.connectionXMPP = connectionXMPP;
        this.notificationsThreshold = notificationsThreshold;
    }
    public DispatcherAgent() throws CleverException{
        super();
       
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
    notification.setId("PRESENCE");
    notification.setType("HM");
    logger.debug("?=)** hostId= "+hostid+" "+notification.getType());
    notification.setHostId(hostid);
    this.sendNotification(notification);
}


    @Override
    public void sendNotification(Notification notification) {

        //TODO: send notification (via CleverMsg) to active CC
        logger.debug("preparing notify clever message");

        CleverMessage cleverMsg = new CleverMessage();
        List attachments=new ArrayList();
        attachments.add(notification.getBody());
        cleverMsg.fillMessageFields(CleverMessage.MessageType.NOTIFY, this.connectionXMPP.getUsername(), attachments, new NotificationOperation(connectionXMPP.getUsername(), notification.getAgentId(), notification.getId(),notification.getType()));
        
        
        
       
        
        
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
//******************************************************************************    
//NEWMONITOR
//******************************************************************************
    /**
    * Send a CleverMessage of MEASURE type
    * @param measure returned by Sigar methods
    */      
    public void sendMeasure(String measure) {
         
        CleverMessage cleverMsg = new CleverMessage();
        String cc=this.connectionXMPP.getActiveCC(ConnectionXMPP.ROOM.CLEVER_MAIN);
        logger.debug("clustercoordinator for sendMeasure is "+cc);
        cleverMsg.setDst(cc);
        cleverMsg.setSrc(this.connectionXMPP.getUsername());
        cleverMsg.setHasReply(false);
        cleverMsg.setTypeSrc("Physical");
        cleverMsg.setType( CleverMessage.MessageType.MEASURE );
        
        cleverMsg.setBody(
                            "    <measure useAttachementId=\"true\">\n" +
                            "      <HM>"+this.connectionXMPP.getUsername()+"</HM>\n" +
                            "      <type>PhisicHost</type>\n"+
                            "      <agentId>CloudMonitorAgent</agentId>\n" +
                            "      <measureId></measureId>\n" +
                            "      <timestamp></timestamp>\n" +
                            "    </measure>"
                            );

        cleverMsg.addAttachment( measure );

        logger.debug("ZZZ " + cleverMsg.getDst() +" "+cleverMsg.getSrc()+"\n");

        //cleverMsg.setReplyToMsg(message.getId());
        notificationThread.sendCleverMsg(cleverMsg);
        //connectionXMPP.sendMessage(connectionXMPP.getActiveCC(ConnectionXMPP.ROOM.CLEVER_MAIN), cleverMsg);
                

    }
    
    
    
    
    
    
    
    
    
    
}
