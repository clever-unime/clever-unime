/*
 *  Copyright (c) 2010 Marco Sturiale
 *  Copyright (c) 2012 Maurizio Paone
 *  Copyright (c) 2012 Marco Carbone
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

//New VERSION! Now this class work as expeted.

package org.clever.Common.CommunicatorPlugins.Dbus;

import java.util.logging.Level;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CommunicationPlugin;
import org.clever.Common.Communicator.MessageHandler;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.MessageFormatter;
import org.freedesktop.dbus.DBusAsyncReply;

public class CommunicatorDbus implements CommunicationPlugin, CleverDbusInterface {

    private MessageHandler messageHandler;
    private DBusConnection connection;
    private DBusConnection senderConnection;
    private String baseObjectPath = "/org/clever/Common/CommunicatorPlugins/Dbus/CleverDbusInterface/";
    private String objectPath;
    private String serviceBusName = "org.clever.Common.CommunicatorPlugins";
    private String topic;
    private String group;
    private Logger logger;

    @Override
    public void init(String topic, String group){
        try {
            this.topic = topic;
            this.group = group;
            objectPath = baseObjectPath + this.topic;

            logger = Logger.getLogger("DbusCommunicationPlugin");
            logger.info("Initializing the Dbus Communication plugin ");
            
            connection= DBusConnection.getConnection(DBusConnection.SESSION);
            logger.info("Creating the connection to the SessionBus ");
            
            
            
            connection.requestBusName(this.serviceBusName+"."+topic+group);
            
            logger.debug("Creating the Service on Dbus " + serviceBusName+"."+topic+group);

            connection.exportObject(objectPath, this);
            logger.debug("Creating the Object on Dbus " + objectPath);


        } catch (DBusException ex) {
            String ex_msg="Error while creating the Object on Dbus.\n " +
                    "Object "+objectPath+".\n" +
                    "Check if this object allready exists.";
            ex.printStackTrace();
            logger.error(ex_msg, ex);
        }
    }

    @Override
    public String sendRecv(String to, String msg) throws CleverException {
        try {

            senderConnection = DBusConnection.getConnection(DBusConnection.SESSION);
            logger.debug("Creating connection to remote object " + baseObjectPath + to + " on bus " + serviceBusName+"."+to+this.group);
            CleverDbusInterface c = (CleverDbusInterface) senderConnection.getRemoteObject(serviceBusName+"."+to+this.group, baseObjectPath + to);
            String reply=null;
            DBusAsyncReply<String> rep=senderConnection.callMethodAsync(c,"OnMessage", msg);
            while(!rep.hasReply())
                try {
                Thread.currentThread().sleep(50)    ;
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(CommunicatorDbus.class.getName()).log(Level.SEVERE, null, ex);
            }
            reply=rep.getReply();
            //reply = c.OnMessage(msg);
            logger.debug("Sending message to : " + to + " : " + msg);
            senderConnection.disconnect();
            logger.debug("Sender Connection Closed");

            return reply;

        } 
         catch (org.freedesktop.DBus.Error.UnknownObject ex) {
            logger.error("Agent not found: "+to);
            throw new CleverException("Agent not found: "+to);
        }
        
        catch (DBusException ex) {
            String ex_msg="Error while sending message with sendRecv.\n " +
                    "Remote object "+baseObjectPath + to + " on bus " + serviceBusName+"." +to+this.group+"\n"+
                    "Check if this object exists.";
            logger.error(ex_msg, ex);
            throw new CleverException(ex_msg);
        }
    }

    @Override
    public void asyncSend(String to, String msg) throws CleverException {

        try {
            senderConnection = DBusConnection.getConnection(DBusConnection.SESSION);
            CleverDbusInterface c = (CleverDbusInterface) senderConnection.getRemoteObject(serviceBusName+"."+to+group, baseObjectPath + to);
            logger.debug("Creating connection to remote object " + baseObjectPath + to + " on bus " + serviceBusName+"."+to+group);
            c.OnMessage(msg);
            logger.debug("Sending message to : " + to + " : " + msg);
            senderConnection.disconnect();
            logger.debug("Sender Connection Closed");

        } 
        catch (org.freedesktop.DBus.Error.UnknownObject ex) {
            logger.error("Agent not found: "+to);
            throw new CleverException("Agent not found: "+to);
        }
        
        catch (DBusException ex) {
            String ex_msg="Error while sending message with asyncSend.\n " +
                    "Remote object "+baseObjectPath + to + " on bus " + serviceBusName+".\n" +
                    "Check if this object exists.";
            logger.error(ex_msg, ex);
            throw new CleverException(ex, ex_msg);
        }

    }

    @Override
    public String getName() {
        return ("DbusCommunicationPlugin");
    }

    @Override
    public String getVersion() {
        return ("1.0");
    }

    @Override
    public String getDescription() {
        return ("Low-level communication plugin based on Dbus");
    }

    private void stopConnection() throws DBusException {
        connection.disconnect();
    }

    @Override
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    @Override
    public String OnMessage(String msg) {

        String st = null;
        try {
            logger.debug("Invoking handleMessage of : " + messageHandler);
            st = messageHandler.handleMessage(msg);
        } catch (CleverException ex) {

            st = MessageFormatter.messageFromObject(ex);
            logger.error("Error CleverException , sending as Msg: " + ex + " \nINVIERO: " + st);
        }



        if (st != null) {

            logger.debug("Sending reply: " + st);

        } else {
            logger.error("ERROR: reply null" + st);

        }

        return st;
    }

    @Override
    public boolean isRemote() {
        return false;
    }
}