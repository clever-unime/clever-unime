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
 * Copyright 2013-14 Giuseppe Tricomi
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
//import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.Dispatcher.DispatcherAgent;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.MessageFormatter;
//import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.Format.TextMode;
import org.jdom2.output.XMLOutputter;
import org.clever.ClusterManager.Dispatcher.CLusterManagerDispatcherPlugin;
/**
 *
 * @author alessiodipietro
 * @author Giuseppe Tricomi
 */
public class SensorBrain implements BrainInterface {
   
    Logger logger;
    DispatcherAgent dispatcherAgent;
    CLusterManagerDispatcherPlugin dispatcherPlugin;
    
    public SensorBrain(CLusterManagerDispatcherPlugin dispatcherPlugin) {
        logger = Logger.getLogger("SensorBrain");
        this.dispatcherPlugin = dispatcherPlugin;
    }
    public SensorBrain(DispatcherAgent dispatcheragent) {
        logger = Logger.getLogger("SensorBrain");
        this.dispatcherAgent= dispatcheragent;
        this.dispatcherPlugin = dispatcherAgent.getDispatcherPlugin();
    }
  /**
     * This method is used to invoke the correct method of DB.
     * @param methodName
     * @param Host
     * @param Agent
     * @param notify
     * @param location
     * @return
     * @throws CleverException 
     */
    private boolean invokeDBMethod(String methodName,String Host,String Agent,String notify,String location) throws CleverException{
        try {
        List<String> params2 = new ArrayList();
        if(Host!=null)
            params2.add(Host);
        params2.add(Agent);
        if(notify!=null)
            params2.add(notify);
        if(methodName=="insertNode")
            params2.add("into");
        params2.add(location);
        MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent",
                    methodName,
                    true,
                    params2);
        Integer n = (Integer) dispatcherPlugin.dispatchToIntern(mi);
        } catch (CleverException ex) {
            logger.error("Error invoking database agent method: " + ex);
            return false;
        }
        return true;
    }
    
    /**
     * This method handle the notification received from cluster manager.It write the notification on 
     * DB in HM element.
     * @param notification 
     */
    @Override
    public void handleNotification(Notification notification) {
        logger.debug("Received notification " + notification.getId());
        try {

            //insert notification into tags <notification id= timestamp=> </notification>
            Element xmlNotification = new Element("notification");
            xmlNotification.setAttribute("id", notification.getId());
            xmlNotification.setAttribute("timestamp", new Date().toString());
            xmlNotification.addContent(new Element("type").addContent(notification.getType()));
            Calendar calendar = Calendar.getInstance();
            calendar.getTimeInMillis();
            xmlNotification.addContent(new Element("date").addContent(new Long(calendar.getTimeInMillis()).toString()));
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
            if(notification.getId().equals("SAS/Publish")){
                params.add("/SASPublicationHistory");
                this.invokeDBMethod("PrepareNode4Sens", notification.getHostId(), notification.getAgentId(),null,"/SASPubblicationHistory");
                this.invokeDBMethod("insertNode", notification.getHostId(), notification.getAgentId(),xmlNotificationString,"/SASPubblicationHistory");
            }
            else{
                this.invokeDBMethod("insertNode", notification.getHostId(), notification.getAgentId(),xmlNotificationString,"");
            }
           } catch (CleverException ex) {
            logger.error("Error invoking database agent method: " + ex);
        }




    }
}
