/*
 * The MIT License
 *
 * Copyright 2011 alessiodipietro.
 * Copyright 2012 Francesco Manera.
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
package org.clever.HostManager.SAS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.Shared.Support;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.custommonkey.xmlunit.Diff;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.safehaus.uuid.UUIDGenerator;
import org.xml.sax.SAXException;
/**
 *
 * @author alessiodipietro
 */
public class SASAgent extends Agent {

    
    private UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
    private Map<String, String> pendingRequests = new ConcurrentHashMap<String, String>();
    protected Map<String, AdvertiseRequestEntry> completedRequests = new ConcurrentHashMap<String, AdvertiseRequestEntry>();
    protected XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    protected SimpleDateFormat sdfSos = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private FileStreamer fs = new FileStreamer();
    private Long alertExpirationMinutes;
    String cfgPath="./cfg/configuration_sasagent.xml";
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/SAS/log_conf/";
    private String pathDirOut="/LOGS/HostManager/SASAgentHm";
    //########

    
    public SASAgent() throws CleverException {
            super();
            
      //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("SASAgentHm");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################    
            
            
        try {
            super.setAgentName("SASAgentHm");
           
            //logger = Logger.getLogger("SASAgentHm");
            logger.debug("SASAgentHm start");
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream( "/org/clever/Common/Shared/logger.properties" );
            prop.load( in );
            PropertyConfigurator.configure( prop );
            File cfgFile = new File( cfgPath );
            InputStream inxml=null;
            if( !cfgFile.exists() )
            {
                inxml = getClass().getResourceAsStream( "/org/clever/HostManager/SAS/configuration_sasagent.xml" );
                try
                {
                    Support.copy( inxml, cfgFile );
                }
                catch( IOException ex )
                {
                    logger.error( "Copy file failed" + ex );
                    System.exit( 1 );
                }
            }
            try
      {
          inxml = new FileInputStream( cfgPath );
      }
      catch( FileNotFoundException ex )
      {
          logger.error( "File not found: " + ex );
      }
      
          FileStreamer fs = new FileStreamer();
          ParserXML pXML = new ParserXML( fs.xmlToString( inxml ) );
            
            
            alertExpirationMinutes=Long.parseLong(pXML.getRootElement().getChildText("alertExpirationMinutes"));
            
            ParameterContainer parameterContainer=ParameterContainer.getInstance();
            Element dbParams=pXML.getRootElement().getChild("test").getChild("dbParams");
            parameterContainer.setDbDriver(dbParams.getChildText("driver"));
            parameterContainer.setDbServer(dbParams.getChildText("server"));
            parameterContainer.setDbPassword(dbParams.getChildText("password"));
            parameterContainer.setDbUsername(dbParams.getChildText("username"));
            parameterContainer.setDbName(dbParams.getChildText("name"));
            
            //this.agentName = agentName;
            
            mc = new ModuleCommunicator(this.getAgentName(),"HM");
            mc.setMethodInvokerHandler(this);
           Notification presenceNotification = new Notification();
            presenceNotification.setId("SAS/Presence");
            presenceNotification.setAgentId("SASAgentHm");
            this.sendNotification(presenceNotification);
            
            
            
        } catch (InstantiationException ex) {
            throw new CleverException(ex, "Missing logger.properties or configuration not found");
        } catch (IllegalAccessException ex) {
            throw new CleverException(ex, "Access Error");
        } catch (ClassNotFoundException ex) {
            throw new CleverException(ex, "Plugin Class not found");
        } catch (IOException ex) {
            throw new CleverException(ex, "Error on reading logger.properties");
        }

    }

    public Document sendExpirationAdvertiseRequest(Element expirationAdvertiseRequestElement){
        ArrayList<String> advertisements=new ArrayList<String>();
        String phenomenonAdvertise="";
        try {
            ArrayList params=new ArrayList();
            params.add(outputter.outputString(expirationAdvertiseRequestElement));
            advertisements=(ArrayList<String>)this.invoke("SOSAgent", "sosService", true, params);
            phenomenonAdvertise=advertisements.get(0);
            
        } catch (CleverException ex) {
            
            logger.error("Error sending expiration advertise request to SOSAgent: "+ex);
        }
        return this.stringToDom(phenomenonAdvertise);
    }
    
    public Document getObservation(String getObservationRequest){
        Document getObservationResponseDoc=new Document();
        ArrayList<String> getObservationResponse=new ArrayList<String>();
        ArrayList params=new ArrayList();
        params.add(getObservationRequest);
        try {
            getObservationResponse=(ArrayList<String>)this.invoke("SOSAgent", "sosService", true, params);
            getObservationResponseDoc=this.stringToDom((String)getObservationResponse.get(0));
        } catch (CleverException ex) {
            logger.error("Error getting observation from SOSAgent: "+ex);
        }
        return getObservationResponseDoc;    
    }
    
    protected void advertise(String advertiseRequest, String requestId) {
       // logger.debug("SASAgentHm_advertiseRequest= "+advertiseRequest);
        Document advertiseRequestDoc = this.stringToDom(advertiseRequest);

        Element advertiseRequestElement = advertiseRequestDoc.detachRootElement();
        advertiseRequestElement.setName("Advertise");

        //send notification
        Notification notification = new Notification();
        notification.setId("SAS/Advertise");
        notification.setBody(outputter.outputString(advertiseRequestElement));
        this.sendNotification(notification);

        //add advertise to pendingRequests
        this.pendingRequests.put(requestId, outputter.outputString(advertiseRequestElement));

    }
    
 /*   public void provaNotifica(String IdNot){
        logger.info("*********** funzionaaaaaaa: "+IdNot);
//        Notification notification = new Notification();
//        notification.setId(IdNot);
//        notification.setBody("prova");
//        this.sendNotification(notification);
    }
    
    public String provadvertise(String advertiseRequest){
        advertise(advertiseRequest);
        return "OK";
    }*/
    
    public void advertise(String advertiseRequest) {
       // logger.debug("SASAgentHm_advertiseRequest= "+advertiseRequest);
        Document advertiseRequestDoc = this.stringToDom(advertiseRequest);
        Element advertiseRequestElement = advertiseRequestDoc.detachRootElement();
        advertiseRequestElement.setName("Advertise");

        //set desiredPublicationExpiration
        Date desiredPublicationExpiration = new Date();
        Long alertExpirationMinutesMillisecond = this.alertExpirationMinutes * 60 * 1000;
        Long desiredPublicationExpirationMillisecond = desiredPublicationExpiration.getTime() + alertExpirationMinutesMillisecond;
        desiredPublicationExpiration.setTime(desiredPublicationExpirationMillisecond);
        advertiseRequestElement.getChild("DesiredPublicationExpiration").setText(sdf.format(desiredPublicationExpiration));
        
        //get requestID
        String requestId = ((Element) advertiseRequestElement.getChild("AlertMessageStructure").getChildren().get(0)).getChild("Content").getAttributeValue("definition");
        requestId = requestId.replaceAll(Pattern.quote(":"), "_");

        //check requestId in completed requests 
        AdvertiseRequestEntry advertiseRequestEntry = this.completedRequests.get(requestId);

        Boolean equals = false;
       
        //if advertise doesn't exist
        if (advertiseRequestEntry == null) {
            //send notification
            Notification notification = new Notification();
            notification.setId("SAS/Advertise");
            notification.setAgentId("SASAgentHm");
            notification.setBody(outputter.outputString(advertiseRequestElement));
            this.sendNotification(notification);

            //add advertise to pendingRequests
            this.pendingRequests.put(requestId, outputter.outputString(advertiseRequestElement));


        } else {
            //execute diff (if the area changed)
            Diff advertiseDiff = null;
            Element advertiseRequestElementCopy=(Element)advertiseRequestElement.clone();
            advertiseRequestElementCopy.getChild("DesiredPublicationExpiration").setText("");
            Element advertiseRequestEntryElement=this.stringToDom(advertiseRequestEntry.advertiseRequest).detachRootElement();
            advertiseRequestEntryElement.getChild("DesiredPublicationExpiration").setText("");
            try {
                advertiseDiff = new Diff("<root>"+outputter.outputString(advertiseRequestElementCopy.getContent()) +"</root>",
                        "<root>"+outputter.outputString(advertiseRequestEntryElement.getContent()) +"</root>");
            } catch (SAXException ex) {
                logger.error("SASException during advertisement diff: " + ex);
            } catch (IOException ex) {
                logger.error("IOException during advertisement diff: " + ex);
            }
            equals = advertiseDiff.similar();
            if (!equals) {
                Notification notification = new Notification();
                notification.setId("SAS/Advertise");
                notification.setAgentId("SASAgentHm");
                notification.setBody(outputter.outputString(advertiseRequestElement));
                this.sendNotification(notification);

                //add advertise to pendingRequests
                this.pendingRequests.put(requestId, outputter.outputString(advertiseRequestElement));
            }
        }
        /*if (advertiseRequestEntry != null) {
        //execute diff
        Diff advertiseDiff = null;
        System.out.println(outputter.outputString(advertiseRequestElement));
        System.out.println("-------------------------------");
        System.out.println(advertiseRequestEntry.advertiseRequest);
        try {
        advertiseDiff = new Diff(outputter.outputString(advertiseRequestElement),
        advertiseRequestEntry.advertiseRequest);
        } catch (SAXException ex) {
        logger.error("SASException during advertisement diff: "+ex);
        } catch (IOException ex) {
        logger.error("IOException during advertisement diff: "+ex);
        }
        equals = advertiseDiff.similar();
        
        }
        //if advertisement doesn't exists or exists and are equals
        if (!equals) {
        //send notification
        Notification notification = new Notification();
        notification.setId("SAS/Advertise");
        notification.setBody(outputter.outputString(advertiseRequestElement));
        this.sendNotification(notification);
        
        //add advertise to pendingRequests
        this.pendingRequests.put(requestId, outputter.outputString(advertiseRequestElement));
        
        }*/
        //if advertisements are equals -> SOSAgent recovered after problem
    }
    
    public void renewAdvertisement(String requestId,String publicationId, String desiredPublicationExpiration){
        Element renewAdvertisement=new Element("RenewAdvertisement");
        Element publicationIdElement=new Element("PublicationID");
        publicationIdElement.setText(publicationId);
        Element requestIdElement=new Element("RequestID");
        requestIdElement.setText(requestId);
        
        Element desiredPublicationExpirationElement=new Element("DesiredPublicationExpiration");
        desiredPublicationExpirationElement.setText(desiredPublicationExpiration);
        
        renewAdvertisement.addContent(publicationIdElement);
        renewAdvertisement.addContent(desiredPublicationExpirationElement);
        renewAdvertisement.addContent(requestIdElement);
        Notification renewAdvertisementNotification=new Notification();
        renewAdvertisementNotification.setId("SAS/RenewAdvertisement");
        renewAdvertisementNotification.setAgentId("SASAgentHm");
        renewAdvertisementNotification.setBody(outputter.outputString(renewAdvertisement));
        
        this.sendNotification(renewAdvertisementNotification);
    }
    
    public void handleRenewAdvertisementResponse(String requestId, String renewAdvertisementResponse){
        //update timer and expiration date in hashtable
        AdvertiseRequestEntry advertiseRequestEntry=this.completedRequests.get(requestId);
      //  logger.debug("Received renew advertise response:\n" + renewAdvertisementResponse);
        Document renewAdvertisementResponseDocument=this.stringToDom(renewAdvertisementResponse);
        Element renewAdvertisementResponseElement=renewAdvertisementResponseDocument.detachRootElement();
        
        //get expiration date
        String expiration=renewAdvertisementResponseElement.getAttributeValue("expires");
        try {
            //update xml
            Document oldAdvertiseRequestDoc=this.stringToDom(advertiseRequestEntry.advertiseRequest);
            Element oldAdvertiseRequestElement=oldAdvertiseRequestDoc.detachRootElement();
            oldAdvertiseRequestElement.getChild("DesiredPublicationExpiration").setText(expiration);
            advertiseRequestEntry.advertiseRequest=outputter.outputString(oldAdvertiseRequestElement);
            
            //update timer
            Date expirationDate=sdf.parse(expiration);
            AdvertisementExpirationTask advertisementExpirationTask=new AdvertisementExpirationTask(requestId,advertiseRequestEntry,this,this.alertExpirationMinutes);
            advertiseRequestEntry.timer.cancel();
            
            advertiseRequestEntry.timer.purge();
            advertiseRequestEntry.timer=new Timer();
            advertiseRequestEntry.timer.schedule(advertisementExpirationTask, expirationDate);
            
            //put in hashtable
            this.completedRequests.put(requestId, advertiseRequestEntry);
        } catch (ParseException ex) {
            logger.error("Parse Exception: "+ex);
        }
        
    }
    
    /*protected Document sendExpirationAdvertiseRequest(Element expirationAdvertiseRequest){
        Document phenomenonAdvertiseDocument=null;
        try {
            
            BufferedInputStream f = null;
            String filePath="/Users/alessiodipietro/Desktop/phenomenonAdvertise.xml";
            byte[] buffer = new byte[(int) new File(filePath).length()];
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
            String phenomenonAdvertise=new String(buffer);
            
            phenomenonAdvertiseDocument=this.stringToDom(phenomenonAdvertise);
            
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SASAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return phenomenonAdvertiseDocument;
        
    }*/

    /*public void cancelAdvertisement(String requestId) {
        //get pubId from hashtable
        AdvertiseRequestEntry advertiseRequestEntry = this.completedRequests.get(requestId);
        String publicationId = advertiseRequestEntry.publicationId;

        //build CancelAdvertise Request
        Element requestIdElement = new Element("RequestID");
        requestIdElement.setText(requestId);
        Element cancelAdvertisement = new Element("CancelAdvertisement");
        Element publicationIdElement = new Element("PublicationID");
        publicationIdElement.setText(publicationId);
        cancelAdvertisement.addContent(publicationIdElement);
        cancelAdvertisement.addContent(requestIdElement);
        Document cancelAdvertisementDocument = new Document(cancelAdvertisement);
        String cancelAdvertisementRequest = outputter.outputString(cancelAdvertisementDocument);


        //build notification
        Notification cancelAdvertisementNotification = new Notification();
        cancelAdvertisementNotification.setId("SAS/CancelAdvertisement");
        cancelAdvertisementNotification.setBody(cancelAdvertisementRequest);
        //send notification
        this.sendNotification(cancelAdvertisementNotification);
        

    }*/

    /*public void handleCancelAdvertisementResponse(String requestId, String cancelAdvertisementResponse) {
        //delete requestId from completedRequests hashtable
        AdvertiseRequestEntry advertiseRequestEntry=this.completedRequests.get(requestId);
        advertiseRequestEntry.timer.cancel();
        advertiseRequestEntry.timer.purge();
        this.completedRequests.remove(requestId);
        logger.debug("Received cancel advertise response:\n" + cancelAdvertisementResponse);
    }*/

    public void handleAdvertiseResponse(String requestId, String advertiseResponse) {
        try {
            logger.debug("Received advertise response:\n" + advertiseResponse);
            Document doc = stringToDom(advertiseResponse);
            Element advertiseResponseElement = doc.detachRootElement();
            String publicationId = advertiseResponseElement.getChild("PublicationID").getText();
            String advertiseRequest = this.pendingRequests.get(requestId);
            this.pendingRequests.remove(requestId);
            
            AdvertiseRequestEntry advertiseRequestEntry = new AdvertiseRequestEntry();
            advertiseRequestEntry.publicationId = publicationId;
            advertiseRequestEntry.advertiseRequest = advertiseRequest;
            //set timer
            Date now=new Date();
            Date expirationDate=sdf.parse(advertiseResponseElement.getAttributeValue("expires"));
            
            AdvertiseRequestEntry advReqEntry=completedRequests.get(requestId);
            if(advReqEntry!=null){
                //the advertisement was present in the hashmap (changed advertisement)
                
                //reset timer
                advReqEntry.timer.cancel();
                advReqEntry.timer.purge();
                
                //stop getObservationThread
                advReqEntry.getObservationThread.interrupt();
            }
            
            advertiseRequestEntry.timer=new Timer();
            AdvertisementExpirationTask advertisementExpirationTask=new AdvertisementExpirationTask(requestId,advertiseRequestEntry,this,this.alertExpirationMinutes);
            advertiseRequestEntry.timer.schedule(advertisementExpirationTask, expirationDate);
            
            //start getObservation thread
            GetObservationThread getObservationThread=new GetObservationThread(advertiseRequest,this);
            advertiseRequestEntry.getObservationThread=getObservationThread;
            advertiseRequestEntry.getObservationThread.start();
            logger.info("GetObservationThread start....sincronization done!");
            logger.debug("advertise request document:\n!"+advertiseRequest.toString());
            this.completedRequests.put(requestId, advertiseRequestEntry);
            
            
            
        } catch (ParseException ex) {
            logger.equals("Error starting expiration timer: "+ex);
        }
    }

    public void publicationsRecovery(String queryResult) throws CleverException{
        logger.debug(" publication recovery start");
        Document queryResultDocument=this.stringToDom("<queryResult>"+queryResult+"</queryResult>");
        Element queryResultElement=queryResultDocument.detachRootElement();
        
        List subscriptionOfferingList=queryResultElement.getChildren("SubscriptionOffering");
        List advertiseList=queryResultElement.getChildren("advertise");
        
        Iterator iteratorSubscriptionOffering=subscriptionOfferingList.iterator();
        Iterator iteratorAdvertise=advertiseList.iterator();
        
        String publicationId="";
        Element desiredPublicationExpiration = null;
        
        Document advertiseRequestDocument=new Document();
        String requestId="";
        //build completedRequest hashTable
        while(iteratorSubscriptionOffering.hasNext()){
            try {
                Element subscriptionOffering=(Element)iteratorSubscriptionOffering.next();
                Element advertise=(Element)iteratorAdvertise.next();
                Element advertiseElement=new Element("Advertise");
                publicationId=advertise.getAttributeValue("pubId");
                desiredPublicationExpiration = advertise.getChild("DesiredPublicationExpiration");
                requestId=advertise.getChildText("RequestID");
                advertiseElement.addContent(subscriptionOffering.getChild("AlertMessageStructure").detach());
                advertiseElement.addContent(subscriptionOffering.getChild("FeatureOfInterest").detach());
                advertiseElement.addContent(subscriptionOffering.getChild("OperationArea").detach());
                advertiseElement.addContent(subscriptionOffering.getChild("AlertFrequency").detach());
                advertiseElement.addContent(desiredPublicationExpiration.detach());
                
                advertiseRequestDocument.setRootElement(advertiseElement);
                
                //build advertiseRequestEntry
                AdvertiseRequestEntry advertiseRequestEntry = new AdvertiseRequestEntry();
                advertiseRequestEntry.publicationId=publicationId;
                advertiseRequestEntry.advertiseRequest=outputter.outputString(advertiseRequestDocument);
                //System.out.println(advertiseRequestEntry.advertiseRequest);
                advertiseRequestEntry.timer=new Timer();
                Date expirationDate=sdf.parse(desiredPublicationExpiration.getText());
                
                AdvertisementExpirationTask advertisementExpirationTask=new AdvertisementExpirationTask(requestId,advertiseRequestEntry,this,this.alertExpirationMinutes);
                advertiseRequestEntry.timer.schedule(advertisementExpirationTask, expirationDate);
                
                //start getObservation thread
                GetObservationThread getObservationThread=new GetObservationThread(outputter.outputString(advertiseRequestDocument),this);
                advertiseRequestEntry.getObservationThread=getObservationThread;
                advertiseRequestEntry.getObservationThread.start();
                logger.debug("GetObservationThread start....sincronization done!");
                //logger.info("advertise request document:\n!"+advertiseRequestDocument.toString());
                this.completedRequests.put(requestId, advertiseRequestEntry);
            }
            catch (ParseException ex) {
                logger.error("Error recovering completedRequests hashtable: "+ex);
                
            }
        }
        
        //start reader on SOSAgent
        ArrayList params=new ArrayList();
        this.invoke("SOSAgent", "startReader", true, params);
        
        //get advertisements from SOS
        ArrayList paramsGet=new ArrayList();
        ArrayList<String> advertisements=new ArrayList<String>();
        try {
            advertisements=(ArrayList<String>)this.invoke("SOSAgent", "getAdvertisements", true, params);
        } catch (CleverException ex) {
            logger.error("Error getting advertisements from SOSAgent: "+ex);
            throw new CleverException("Error getting advertisements from SOSAgent: "+ex);
        }
        //send advertisement request to SASAgent on cluster Manager
        Iterator iterator=advertisements.iterator();
        String advertiseRequest="";
        while(iterator.hasNext()){
            
            advertiseRequest=(String) iterator.next();
            logger.debug(" advertiseRequest");
            this.advertise(advertiseRequest);  
        }
        //System.out.println("SASAgent:get advertisement");
        
        
        
        
        
        
        
        

    }

    protected Document stringToDom(String xmlSource) {
        StringReader stringReader = new StringReader(xmlSource);

        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(stringReader);
        } catch (JDOMException ex) {
            logger.error("JDOM exception: " + ex);
        } catch (IOException ex) {
            logger.error("IOException: " + ex);
        }
        return doc;

    }

    /*private SensorAlertMessage getObservation() {
        SensorAlertMessage sensorAlert = new SensorAlertMessage();
        sensorAlert.setLatitude(57.3);
        sensorAlert.setLongitude(8.2);
        sensorAlert.setAltitude(232);

        Date now = new Date();
        sensorAlert.setTimeOfAlert(now);
        Random generator = new Random();
        sensorAlert.setValue(Double.toString(generator.nextDouble() * 200));
        Element alertMessageStructure = new Element("AlertMessageStructure");
        Element alertContent = new Element("Content");
        alertContent.setAttribute("definition", "urn:ogc:humidity");
        alertContent.setAttribute("uom", "percent");
        Element alertType = new Element("QuantityProperty");
        alertType.addContent(alertContent);
        alertMessageStructure.addContent(alertType);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        String xmlString = outputter.outputString(alertMessageStructure);
        sensorAlert.setAlertMessageStructure(xmlString);
        sensorAlert.setRequestId(1894678515);
        return sensorAlert;
    }*/

    public void sendSensorAlertMessage(String timeOfAlert, Double latitude, Double altitude, Double longitude,String value,
                                       String alertMessageStructure) {
        try {
            logger.debug("?=) send sensor alert message");
            SensorAlertMessage sensorAlert=new SensorAlertMessage();
            //sensorAlert.setRequestId(requestId);
            sensorAlert.setLatitude(latitude);
            sensorAlert.setLongitude(altitude);
            sensorAlert.setAltitude(longitude);
            Date now = sdf.parse(timeOfAlert);
            sensorAlert.setTimeOfAlert(now);
            sensorAlert.setAlertExpires(0);
            sensorAlert.setValue(value);
            sensorAlert.setAlertMessageStructure(alertMessageStructure);
            Element alertMessageStructureElement=this.stringToDom(alertMessageStructure).detachRootElement();
            String requestId=((Element)alertMessageStructureElement.getChildren().get(0)).getChild("Content").getAttributeValue("definition");
            requestId=requestId.replaceAll(Pattern.quote(":"),"_");
            AdvertiseRequestEntry advertiseRequestEntry = this.completedRequests.get(requestId);
            sensorAlert.setPublicationId(advertiseRequestEntry.publicationId);
            logger.debug("send sensor alert message");
            Notification notification = new Notification();
            notification.setId("SAS/Publish");
            notification.setAgentId("SASAgentHm");
            notification.setBody(sensorAlert);
            logger.debug(" notification agentId: "+notification.getAgentId());
            this.sendNotification(notification);
            
        } catch (ParseException ex) {
            logger.error("Error parsing timeOfAlert: "+ex);
        }catch (Exception exgen){
            logger.error("Error generic in SendSensor AlertMessage: "+exgen);
        }
    }

    @Override
    public Class getPluginClass() {
        return SASAgent.class;
    }

    @Override
    public Object getPlugin() {
        return this;
    }

    @Override
    public void initialization() throws Exception {
       
    }

    @Override
    public void shutDown() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
