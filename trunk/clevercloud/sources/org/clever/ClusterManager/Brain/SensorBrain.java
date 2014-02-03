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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.Dispatcher.DispatcherAgent;
import org.clever.ClusterManager.Dispatcher.DispatcherPlugin;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMLTools.ParserXML;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.Format.TextMode;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author alessiodipietro
 * @author Giuseppe Tricomi
 * @author Antonio Galletta 2013
 * 
 */
public class SensorBrain implements BrainInterface {

    Logger logger;
    DispatcherAgent dispatcherAgent;
    private DispatcherPlugin dispatcherPlugin;
    private boolean subscripted;

    public SensorBrain(DispatcherAgent dispatcherAgent) {
        logger = Logger.getLogger("SensorBrain");
        this.dispatcherAgent = dispatcherAgent;
        this.dispatcherPlugin=dispatcherAgent.getDispatcherPlugin();
    }
    
    public SensorBrain(DispatcherPlugin dispatcherPlugin, boolean subscripted) {
        logger = Logger.getLogger("SensorBrain");
        this.dispatcherPlugin=dispatcherPlugin;
        this.subscripted=subscripted;
    }

    @Override
    public void handleNotification(Notification notification) {
      //  logger.debug("abcdefghi");
        logger.debug("Received notification " + notification.getId());
        try {

            //insert notification into tags <notification id= timestamp=> </notification>
     /*Commentato da antonio galletta
            Element xmlNotification = new Element("notification");
            xmlNotification.setAttribute("id", notification.getId());
            xmlNotification.setAttribute("timestamp", new Date().toString());
*/
                    
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

            Element xmlNotification=this.createXMLNotification(notification, doc);
          
            
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
           if(notification.getId().equals("SAS/Presence")&&(!notification.getAgentId().equalsIgnoreCase("noName"))){
                params.add("/SASAgentActiveList");
                this.PrepareNode4Sens(notification);
                logger.debug("controllo se devo fare insert o upload");
                
               // //this.invokeDBMethod("insertNode",null, notification.getAgentId(),xmlNotificationString,"/SASAgentActiveList");
                this.writeNotification(xmlNotificationString, notification);
           }
           else
               if(notification.getId().equals("SAS/Publish")&&(!notification.getAgentId().equalsIgnoreCase("noName"))){
                params.add("/SASPublicationHistory");
                this.PrepareNode4Sens(notification);
                this.invokeDBMethod("insertNode", notification.getHostId(), notification.getAgentId(),xmlNotificationString,"/SASPubblicationHistory");
            }
            else{
                //params.add("");
                this.invokeDBMethod("insertNode", notification.getHostId(), notification.getAgentId(),xmlNotificationString,"");
            }

        } catch (CleverException ex) {
            logger.error("Error invoking database agent method: " + ex);
        }
    }
    
    private Object invokeDBMethod(String methodName,String Host,String Agent,String notify,String location) throws CleverException{
        try {
        List<String> params2 = new ArrayList();
        if(Host!=null)
            params2.add(Host);
        params2.add(Agent);
        if(notify!=null)
            params2.add(notify);
        if(methodName.equalsIgnoreCase("insertNode"))
            params2.add("into");
        params2.add(location);
        logger.debug("stampo host e location"+Host +" "+location);
        MethodInvoker mi = new MethodInvoker("DatabaseManagerAgent",
                    methodName,
                    true,
                    params2);
        return dispatcherPlugin.dispatchToIntern(mi);
        } catch (CleverException ex) {
            logger.error("Error invoking database agent method: " + ex.toString());
            
            return null;
        }
        
    }
    
     private void PrepareNode4Sens(Notification notification) throws CleverException {
        logger.debug("Esperimento2.3"); 
        logger.debug(notification.getId());
        if(notification.getId().equalsIgnoreCase("SAS/Presence")){
            if(!((Boolean)this.invokeDBMethod("checkAgentNode", null, notification.getAgentId(), null, "/SASAgentActiveList"))){
                 logger.debug("Esperimento2.4");
              this.invokeDBMethod("insertNode",null,notification.getAgentId(),"<SASAgentActiveList/>","");
           }
        }
        else{
                logger.debug("Esperimento2.1");
                if(!((Boolean)this.invokeDBMethod("checkHMAgentNode", null, notification.getAgentId(), null, "/SASPublicationHistory"))){
                logger.debug("Esperimento2.2");;
                 this.invokeDBMethod("insertNode",notification.getHostId(),notification.getAgentId(),"<SASPubblicationHistory/>","");
               }
        }
    }
     
    private void writeNotification(String xmlNotificationString, Notification notification) throws CleverException{
        String notificationString;
        Document doc;
        SAXBuilder builder = new SAXBuilder();
        Element elemento;
        logger.info("method writeNotification");
        notificationString= (String)this.invokeDBMethod("query",null,notification.getAgentId() , null,"/SASAgentActiveList");
          try 
        {  
            //todo: controllare se può rispondere null e gestire eventualmente la situazione
        doc=builder.build( new StringReader( notificationString ) );
       
        elemento=ParserXML.getElementByAttribute(doc, "name", notification.getHostId());
            if(elemento!=null){
               /* in this case it was necessary to reverse the order of the parameters because the method "replace" accepts
               *  the node to be inserted as the last parameter
               */
                logger.debug("stampo notification.getAgentId(): "+notification.getAgentId());
               this.invokeDBMethod("replaceNode",null, notification.getAgentId(),"/SASAgentActiveList/HM_SASAGENT[@name='"+notification.getHostId()+"']",xmlNotificationString);
               }
            else{
               this.invokeDBMethod("insertNode",null, notification.getAgentId(),xmlNotificationString,"/SASAgentActiveList");
               
            }
          
            
        } catch (JDOMException ex) {
            logger.debug("error in method writeNotification"+ex);
        } catch (IOException ex) {
            logger.debug("error in method writeNotification"+ex);
        } catch (Exception ex) {
            logger.debug("error in method writeNotification"+ex);
        } 
     }
  
   
   private Element createXMLNotification(Notification notification,Document doc){
   
       Element xmlNotification,xmlNotificationBody ;
       
       
       if(notification.getId().equals("SAS/Presence")){
       xmlNotificationBody=new Element("TimeStamp");
           xmlNotification = new Element("HM_SASAGENT");
           xmlNotification.setAttribute("name", notification.getHostId());
           xmlNotificationBody.setText(new Timestamp(System.currentTimeMillis()).toString());
           xmlNotification.addContent(xmlNotificationBody);
           xmlNotificationBody=new Element("Subscripted");
           xmlNotificationBody.setText(String.valueOf(subscripted));
           xmlNotification.addContent(xmlNotificationBody);
       }
       
       else{
         xmlNotification = new Element("notification");
         xmlNotification.setAttribute("id", notification.getId());
         xmlNotification.setAttribute("timestamp", new Date().toString());
         xmlNotificationBody = doc.getRootElement();
         xmlNotification.addContent(xmlNotificationBody.detach());
         
       }
            return xmlNotification;
   }
}
