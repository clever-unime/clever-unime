/*
 * The MIT License
 *
 * Copyright 2011 Alessio Di Pietro.
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
package org.clever.ClusterManager.Brain;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.Dispatcher.DispatcherAgent;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.MessageFormatter;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.Format.TextMode;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author alessiodipietro
 */
public class SensorBrain implements BrainInterface {

    Logger logger;
    DispatcherAgent dispatcherAgent;

    public SensorBrain(DispatcherAgent dispatcherAgent) {
        logger = Logger.getLogger("SensorBrain");
        this.dispatcherAgent = dispatcherAgent;
    }

    @Override
    public void handleNotification(Notification notification) {
        logger.debug("Received notification " + notification.getId());
        try {

            //insert notification into tags <notification id= timestamp=> </notification>
            Element xmlNotification = new Element("notification");
            xmlNotification.setAttribute("id", notification.getId());
            xmlNotification.setAttribute("timestamp", new Date().toString());

            //StringReader stringReader = new StringReader((String) notification.getBody());
            StringReader stringReader = new StringReader(MessageFormatter.messageFromObject(notification.getBody()));
            SAXBuilder builder = new SAXBuilder();
            Document doc = null;
            try {
                doc = builder.build(stringReader);
            } catch (JDOMException ex) {
                logger.error("JDOM exception: "+ex);
            } catch (IOException ex) {
                logger.error("IOException: "+ex);
            }

            Element xmlNotificationBody = doc.getRootElement();
            xmlNotification.addContent(xmlNotificationBody.detach());
            XMLOutputter xout = new XMLOutputter();
            Format f = Format.getPrettyFormat();
            xout.setFormat(f);
            String xmlNotificationString = xout.outputString(xmlNotification);


            //write on db using DatabaseManagerAgent
            List<String> params = new ArrayList();
            if (notification.getHostId() != null) {
                params.add(notification.getHostId());
            }

            params.add(notification.getAgentId());
            params.add(xmlNotificationString);
            params.add("into");
            params.add("");


            MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent",
                    "insertNode",
                    true,
                    params);
            Integer n = (Integer) dispatcherAgent.getDispatcherPlugin().dispatchToIntern(mi);
        } catch (CleverException ex) {
            logger.error("Error invoking database agent method: " + ex);
        }




    }
}
