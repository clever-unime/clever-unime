/*
 *  The MIT License
 * 
 *  Copyright 2011 sabayonuser.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.clever.Common.CommunicatorPlugins.Local;

import java.util.HashMap;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CommunicationPlugin;
import org.clever.Common.Communicator.MessageHandler;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.MessageFormatter;

/**
 *
 * @author Maurizio Paone
 */
public class CommunicatorLocal implements CommunicationPlugin {

    private static HashMap<String, MessageHandler> modules;
    private MessageHandler mh;
    private Logger logger;
    private String topic = null, group;

    public CommunicatorLocal() {
        logger = Logger.getLogger("LocalCommunicationPlugin");
        //PropertyConfigurator.configure("logger.properties");

        logger.info("Plugin created");
        if (modules == null) {
            modules = new HashMap();
            logger.info("Modules HasMap instatiated");
        }

    }

    @Override
    public void init(String topic, String group) {

        this.topic = topic;
        this.group = group;
        logger.info("Module Attaching: " + topic + " on group:" + group + " attached total (on each group) modules : " + modules.size());

    }

    @Override
    public String sendRecv(String to, String msg) throws CleverException {
        String st;
        try {
            MessageHandler mhTarget;
            logger.debug("sync message to " + to);

            mhTarget = (MessageHandler) modules.get(to + this.group);
            if (mhTarget == null) //destination module not found
            {
                throw new CleverException("module " + to + "not found");
            }
            st = mhTarget.handleMessage(msg);
            logger.debug("message sent ; response: " + st);

        } catch (CleverException ex) {
            st = MessageFormatter.messageFromObject(ex);
            logger.error("Error CleverException , sending as Msg: " + ex);

        } catch (Exception ex) {
            logger.error("Error in the SendRecv: " + ex);
            throw new CleverException(ex);
        }
        return st;


    }

    @Override
    public void asyncSend(String to, String msg) {
        try {
            //TODO: implement asyncSend
            logger.info("messaggio async per " + to + " : " + msg);
        } catch (Exception ex) {
            logger.error("Errore nella asyncSend: " + ex);
        }
    }

    @Override
    public String getName() {
        return ("LocalCommunicationPlugin");
    }

    @Override
    public String getVersion() {
        return ("0");
    }

    @Override
    public String getDescription() {
        return ("Low-level communication plugin");
    }

    @Override
    public void setMessageHandler(MessageHandler handler) {
        this.mh = handler;
        modules.put(topic + group, handler); //insert myself in modules HashMap
    }
}
