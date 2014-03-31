/*
 * The MIT License
 *
 * Copyright 2011 Alessio Di Pietro.
 * Copyright 2012 Francesco Antonino Manera.
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
package org.clever.ClusterManager.SAS;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.Shared.Support;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.HostManager.SAS.SensorAlertMessage;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.safehaus.uuid.UUIDGenerator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

class PublishDeliveryEntry {

    public String mucName = "";
    public String mucPassword = "";
    public Map<FilterKey, Element> resultFilter = null;
}

class SubscriptionEntry {

    public HashMap<String, Integer> soiIndex;
    public Timer timer;
}

class AssignedSoi {

    public Element soiList;
    public String minExpirationDate;
}

/**
 *
 * @author alessiodipietro
 */
public class SASAgent extends CmAgent {

    private Map<String, List<PublishDeliveryEntry>> publishDelivery = new ConcurrentHashMap<String, List<PublishDeliveryEntry>>();
    private Map<String, String> pubSoi = new ConcurrentHashMap<String, String>();
    private Map<String, SubscriptionEntry> subscriptions = new ConcurrentHashMap<String, SubscriptionEntry>();
    private UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
    private XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    private SAXParserFactory spf = null;
    private SAXParser sp = null;
    private XMLReader xr = null;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private String agentPrefix;
    private ThreadMessageDispatcher threadMessageDispatcher;
    private String agentName;
    private FileStreamer fs = new FileStreamer();
    private Database db;
    private ParameterDbContainer parameterContainer;
    private org.w3c.dom.Document doc;
     
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger=null;
    private String pathLogConf="/sources/org/clever/ClusterManager/SAS/log_conf/";
    private String pathDirOut="/LOGS/ClusterManager/SASAgent";
    //########
    
     
    public SASAgent(/*String agentName*/) throws CleverException {
        
        //#############################################
       //Inizializzazione meccanismo di logging
       logger=Logger.getLogger("SASAgent");    
       Log4J log =new Log4J();
       log.setLog4J(logger, pathLogConf, pathDirOut);
       //#############################################  
        
        
        
        try {
            super.setAgentName("SASAgent");
            agentName="SASAgent";
            
            logger = Logger.getLogger(agentName);
            logger.info("SASAgent CM !!!!");
            mc = new ModuleCommunicator(agentName,"CM");
            mc.setMethodInvokerHandler(this);

            spf = SAXParserFactory.newInstance();
            sp = spf.newSAXParser();
            xr = sp.getXMLReader();

            init();
            initCleverResouces();
            this.threadMessageDispatcher = new ThreadMessageDispatcher(this, 100, 10); //TODO: retrieve parameters from configuration file
            this.threadMessageDispatcher.start();
        } catch (ParserConfigurationException ex) {
            throw new CleverException(ex, "Parser Configuration Exception: " + ex);
        } catch (SAXException ex) {
            throw new CleverException(ex, "SAX Exception: " + ex);
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

   
    
    
    private void init() throws CleverException {
        initDb();
        initSoiPublications();
        initPublishDelivery();
        List params = new ArrayList();
            params.add(agentName);
            params.add("SAS/Presence");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);

            params.clear();
            params.add(agentName);
            params.add("SAS/Advertise");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);

            params.clear();
            params.add(agentName);
            params.add("SAS/Publish");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);
            

            params.clear();
            params.add(agentName);
            params.add("SAS/CancelAdvertisement");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);

            params.clear();
            params.add(agentName);
            params.add("SAS/RenewAdvertisement");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);
     }
    
    public String testMethod(String value){
        logger.debug("testMethod value is: "+value);
        return value;
    }

    private void initSoiPublications() {
        try {
            String advertisements = "";
            advertisements = query("/advertisements");
            Document doc = this.stringToDom(advertisements);
            Element xmlAdvertisements = doc.getRootElement();

            List children = xmlAdvertisements.getChildren();
            Iterator iterator = children.iterator();
            String pubId = "";
            String soi = "";
            Element element = null;
            Element subscriptionOfferingId = null;

            while (iterator.hasNext()) {
                element = (Element) iterator.next();
                pubId = element.getAttributeValue("pubId");
                subscriptionOfferingId = element.getChild("SubscriptionOfferingID");
                soi = subscriptionOfferingId.getText();
                this.pubSoi.put(pubId, soi);
            }
        } catch (CleverException ex) {
            logger.error("Init publication-soi hashtable error: " + ex);
        }
    }

    private void initPublishDelivery() {
        try {
            String subscriptions = "";
            subscriptions = query("/subscriptions");
            Document doc = this.stringToDom(subscriptions);
            Element subscriptionsElement = doc.detachRootElement();

            List children = subscriptionsElement.getChildren();
            Iterator iterator = children.iterator();
            List resultFilter = null;
            Element element = null;
            Element xmppUri = null;
            PublishDeliveryEntry publishDeliveryEntry = null;
            List assignedSoiList = null;
            String subscriptionId = "";
            while (iterator.hasNext()) {
                try {
                    element = (Element) iterator.next();

                    //get actual expiration date
                    String actualExpiration = element.getAttributeValue("expires");
                    Date actualExpirationDate = sdf.parse(actualExpiration);
                    //get current date
                    Date currentDate = new Date();

                    //check actual expiration date
                    if (actualExpirationDate.after(currentDate)) {

                        subscriptionId = element.getAttributeValue("subId");
                        xmppUri = element.getChild("XMPPURI");
                        publishDeliveryEntry = new PublishDeliveryEntry();
                        publishDeliveryEntry.mucName = xmppUri.getText();
                        publishDeliveryEntry.mucPassword = xmppUri.getAttributeValue("password");
                        publishDeliveryEntry.resultFilter = this.createFilterHashMap(element.getChildren("ResultFilter"));
                        assignedSoiList = element.getChild("AssignedSoiList").getChildren();




                        //join the room
                        try {
                            List<String> joinParameters = new ArrayList();
                            joinParameters.add(agentName);
                            joinParameters.add(publishDeliveryEntry.mucName);
                            joinParameters.add(publishDeliveryEntry.mucPassword);
                            this.invoke("DispatcherAgent", "joinAgentRoom", true, joinParameters);
                        } catch (CleverException ex) {
                            logger.error("Error joining rooms: " + ex);
                        }
                        String expirationDate = getSubscriptionExpiration(subscriptionId);
                        this.updatePublishDelivery(assignedSoiList, publishDeliveryEntry, subscriptionId, expirationDate);
                    }
                } catch (ParseException ex) {
                    logger.error("Parse exception: " + ex);
                }

            }
        } catch (CleverException ex) {
            logger.error("Init subscription hashtables error: " + ex);
        }
    }

    private String getSubscriptionExpiration(String subId) {
        String expirationDate = "";
        try {
            String soiListQuery = "/subscriptions/subscription[@subId=\"" + subId + "\"]"
                    + "/AssignedSoiList/AssignedSoi/text()";

            String query = "min("
                    + this.agentPrefix + "/advertisements/advertise[SubscriptionOfferingID="
                    + "../.." + soiListQuery + "]/DesiredPublicationExpiration/xs:dateTime(.))";

            expirationDate = this.rawQuery(query);
            expirationDate = expirationDate.replaceAll("\n", "");
        } catch (CleverException ex) {
            logger.error("Error getting subscription expiration date: " + ex);
        }
        return expirationDate;
    }

    private String query(String location) throws CleverException {
        String result = "";
        try {
            //init hashtable
            List<String> params = new ArrayList();
            logger.debug("***Agent Name= "+this.agentName);
            params.add(this.agentName);
            params.add(location);
            result = (String) this.invoke("DatabaseManagerAgent", "query", true, params);
        } catch (CleverException ex) {
            logger.error("Query error: " + ex);
            throw new CleverException("Query error " + ex);
        }
        return result;
    }

    private String rawQuery(String query) throws CleverException {
        String result = "";
        try {
            //init hashtable
            List<String> params = new ArrayList();
            params.add(query);
            result = (String) this.invoke("DatabaseManagerAgent", "rawQuery", true, params);
        } catch (CleverException ex) {
            logger.error("Query error: " + ex);
            throw new CleverException("Query error " + ex);
        }
        return result;

    }

    private String queryJoin(String location1, String location2) throws CleverException {
        String result = "";
        try {
            //init hashtable
            List<String> params = new ArrayList();
            params.add(this.agentName);
            params.add(location1);
            params.add(location2);
            result = (String) this.invoke("DatabaseManagerAgent", "queryJoin", true, params);
        } catch (CleverException ex) {
            logger.error("Query error: " + ex);
            throw new CleverException("Query error " + ex);
        }
        return result;
    }

    private Boolean checkAgent() {
        List<String> params = new ArrayList();
        Boolean existsAgent = false;
        params.add(this.agentName);
        try {
            existsAgent = (Boolean) this.invoke("DatabaseManagerAgent", "checkAgent", true, params);
        } catch (CleverException ex) {
            logger.error("Init error: " + ex);
        }
        return existsAgent;

    }

    private Boolean checkAgentNode(String location) {
        List<String> params = new ArrayList();
        Boolean existsNode = false;
        params.add(this.agentName);
        params.add(location);
        try {
            existsNode = (Boolean) this.invoke("DatabaseManagerAgent", "checkAgentNode", true, params);
        } catch (CleverException ex) {
            logger.error("Init error: " + ex);
        }
        return existsNode;
    }

    private void insertNode(String node, String where, String location) {
        List<String> params = new ArrayList();
        params.add(this.agentName);
        params.add(node);
        params.add(where);
        params.add(location);

        try {
            this.invoke("DatabaseManagerAgent", "insertNode", true, params);
        } catch (CleverException ex) {
            logger.error("Init error: " + ex);
        }

    }

    private void deleteNode(String location) throws CleverException {
        List<String> params = new ArrayList();
        params.add(this.agentName);
        params.add(location);
        try {
            this.invoke("DatabaseManagerAgent", "deleteNode", true, params);
        } catch (CleverException ex) {
            logger.error("Init error: " + ex);
            throw new CleverException("Error deleting node " + ex);
        }
    }
    
    private void replaceNode(String location, String node) throws CleverException {
        List<String> params = new ArrayList();
        params.add(this.agentName);
        params.add(location);
        params.add(node);
        try {
            this.invoke("DatabaseManagerAgent", "replaceNode", true, params);
        } catch (CleverException ex) {
            logger.error("Init error: " + ex);
            throw new CleverException("Error replacing node " + ex);
        }
        
        
    }

    private void initDb() {
        //logger.debug("Francesco initDb");
        List<String> params = new ArrayList();
        MethodInvoker mi = null;
        Boolean existsAgent = false;
        Boolean existsAdv = false;
        if (!checkAgent()) {
            //insert agent
            params.add(agentName);
            try {
                this.invoke("DatabaseManagerAgent", "addAgent", true, params);
            } catch (CleverException ex) {
                logger.error("Init error: " + ex);
            }

        }

        //check advertisements
        if (!checkAgentNode("/advertisements")) {
            insertNode("<advertisements></advertisements>", "into", "");
        }

        //check capabilities
        if (!checkAgentNode("/capabilities")) {
            insertNode("<capabilities>"
                    + "<Contents>"
                    + "<AcceptAdvertisements>true</AcceptAdvertisements>"
                    + "<SubscriptionOfferingList></SubscriptionOfferingList>"
                    + "</Contents>"
                    + "<ServiceIdentification></ServiceIdentification>"
                    + "<ServiceProvider></ServiceProvider>"
                    + "<OperationsMetadata></OperationsMetadata></capabilities>", "into", "");
        }
        //check subscriptions
        if (!checkAgentNode("/subscriptions")) {
            insertNode("<subscriptions></subscriptions>", "into", "");
        }

        //get agent db prefix
        params = new ArrayList();
        params.add(agentName);
        try {
            this.agentPrefix = (String) this.invoke("DatabaseManagerAgent", "getAgentPrefix", true, params);
        } catch (CleverException ex) {
            logger.error("Init error: " + ex);
        }

        logger.debug(" initDb Done");
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
    public void handleNotification(Notification notification) throws CleverException {
        String notificationType = notification.getId();
        logger.debug(" handlenotification: "+notificationType);
        
        if (notificationType.equals("SAS/Advertise")) {
            this.handleAdvertiseRequest(notification);
        } else if (notificationType.equals("SAS/Publish")) {
            this.handlePublish(notification);
        } else if (notificationType.equals("SAS/Presence")) {
            this.handlePresence(notification);
        /*} else if (notificationType.equals("SAS/CancelAdvertisement")) {
            this.handleCancelAdvertisementRequest(notification);*/
        } else if (notificationType.endsWith("SAS/RenewAdvertisement")) {
            this.handleRenewAdvertisement(notification);
        }


    }

    public void handleRenewAdvertisement(Notification notification) {
        String renewAdvertisement = (String) notification.getBody();
        Document doc = stringToDom(renewAdvertisement); //TODO: validate document
        logger.debug("Received renew advertise request:\n" + renewAdvertisement);
        Element renewalStatus = new Element("renewalStatus");
        renewalStatus.setText("confirmed");
        Element renewAdvertisementElement = doc.detachRootElement();

        String requestId = renewAdvertisementElement.getChildText("RequestID");
        Element publicationIdElement = renewAdvertisementElement.getChild("PublicationID");
        String publicationId = publicationIdElement.getText();
        Element desiredPublicationExpirationElement = renewAdvertisementElement.getChild("DesiredPublicationExpiration");
        String desiredPublicationExpiration = desiredPublicationExpirationElement.getText();
        try {
            
            Date expirationDate = sdf.parse(desiredPublicationExpiration);

            //get advertisement from db
            String advertisement = this.query("/advertisements/advertise[@pubId=\"" + publicationId + "\"]");
            if (!advertisement.equals("")) {
                Document advertisementDoc = this.stringToDom(advertisement);
                Element advertiseElement = advertisementDoc.detachRootElement();
                String currentPublicationExpiration = advertiseElement.getChildText("DesiredPublicationExpiration");
                Date currentExpiration = sdf.parse(currentPublicationExpiration);
                if (expirationDate.before(currentExpiration)) {
                    renewalStatus.setText("rejected");
                } else {
                    //update expiration date
                    this.replaceNode("/advertisements/advertise[@pubId=\"" + publicationId + "\"]/DesiredPublicationExpiration", outputter.outputString(desiredPublicationExpirationElement));

                }
            } else {
                renewalStatus.setText("rejected");
            }

            //check if date is after current
        } catch (CleverException ex) {
            logger.error("Error updating expiration date: " + ex);
            renewalStatus.setText("rejected");
        } catch (ParseException ex) {
            logger.error("ParseException: " + ex);
            renewalStatus.setText("rejected");
        }
        //build renew advertise response
        Element renewAdvertisementResponse = new Element("RenewAdvertisementResponse");
        renewAdvertisementResponse.addContent(publicationIdElement.detach());
        renewAdvertisementResponse.addContent(renewalStatus);
        renewAdvertisementResponse.setAttribute("expires", desiredPublicationExpiration);
        Document renewAdvertisementResponseDoc = new Document(renewAdvertisementResponse);
        //send response to hm
        List params = new ArrayList();
        params.add(requestId);
        params.add(outputter.outputString(renewAdvertisementResponseDoc));
        try {
            this.remoteInvocation(notification.getHostId(), "SASAgentHm", "handleRenewAdvertisementResponse", true, params);
        } catch (CleverException ex) {
            logger.error("Error sending renew adv response on hm " + notification.getHostId());
        }

    }

    public void handlePresence(Notification notification) {
        try {
            String queryResult = this.queryJoin("/advertisements/advertise[@hm='" + notification.getHostId() + "']",
                    "/capabilities//SubscriptionOfferingList"
                    + "/SubscriptionOffering[./SubscriptionOfferingID=../../../..//advertise[@hm='" + notification.getHostId() + "']/SubscriptionOfferingID/text()]");
                    
                List params = new ArrayList();
                params.add(queryResult);
                logger.debug("?=) Query result= "+queryResult);
                if(!queryResult.equals(""))
                    this.remoteInvocation(notification.getHostId(), "SASAgentHm", "publicationsRecovery", true, params);

            
        } catch (CleverException ex) {
            logger.error("Error recovering publications on hm " + notification.getHostId());
        }
    }

    public void handlePublish(Notification notification) {
        SensorAlertMessage alertMessage = (SensorAlertMessage) notification.getBody();
        logger.debug("Received alert from " + notification.getHostId() + ": " + alertMessage.toString());


        //get soi
        String soi = this.pubSoi.get(alertMessage.getPublicationId());

        logger.debug("?=) handle publish soi:"+soi);
        //get muc and filter list
        List<PublishDeliveryEntry> publishDeliveryEntryList = this.publishDelivery.get(soi);
        if (publishDeliveryEntryList != null) {
            Iterator iterator = publishDeliveryEntryList.iterator();
            PublishDeliveryEntry publishDeliveryEntry = null;
            while (iterator.hasNext()) {
                publishDeliveryEntry = (PublishDeliveryEntry) iterator.next();
                //deliver alert message to muc, applying filter
                logger.debug("?=) handel publishDeliveryEntry: "+publishDeliveryEntry);
                MucSensorAlertMessage message = new MucSensorAlertMessage();
                message.setPublishDeliveryEntry(publishDeliveryEntry);
                message.setSensorAlertMessage(alertMessage);
                logger.debug("?=)% pushMessage: "+message);
                this.threadMessageDispatcher.pushMessage(message);
                //deliverPublish(subscriptionEntry, alertMessage);
            }
        }


    }

    private boolean applyFilterRule(String type, String operator, String observedValue, String filterParameter) {
        boolean send = false;
        if (type.equals("QuantityProperty")) {
            if (operator.equals("isSmallerThan")) {
                send = Double.parseDouble(observedValue) < Double.parseDouble(filterParameter);
            } else if (operator.equals("isGreaterThan")) {
                send = Double.parseDouble(observedValue) > Double.parseDouble(filterParameter);
            } else if (operator.equals("equals")) {
                send = Double.parseDouble(observedValue) == Double.parseDouble(filterParameter);
            } else if (operator.equals("isNotEqualTo")) {
                send = Double.parseDouble(observedValue) != Double.parseDouble(filterParameter);
            }

        } else if (type.equals("CategoryProperty")) {
            if (operator.equals("equals")) {
                send = observedValue.equals(filterParameter);
            } else if (operator.equals("isNotEqualTo")) {
                send = !observedValue.equals(filterParameter);
            }
        } else if (type.equals("CountProperty")) {
            if (operator.equals("isSmallerThan")) {
                send = Integer.parseInt(observedValue) < Integer.parseInt(filterParameter);
            } else if (operator.equals("isGreaterThan")) {
                send = Integer.parseInt(observedValue) > Integer.parseInt(filterParameter);
            } else if (operator.equals("equals")) {
                send = Integer.parseInt(observedValue) == Integer.parseInt(filterParameter);
            } else if (operator.equals("isNotEqualTo")) {
                send = Integer.parseInt(observedValue) != Integer.parseInt(filterParameter);
            }


        } else if (type.equals("BooleanProperty")) {
            if (operator.equals("equals")) {
                send = Boolean.parseBoolean(observedValue) == Boolean.parseBoolean(filterParameter);
            } else if (operator.equals("isNotEqualTo")) {
                send = Boolean.parseBoolean(observedValue) != Boolean.parseBoolean(filterParameter);
            }
        }

        return send;
    }

    protected void deliverPublish(MucSensorAlertMessage message) {

        SensorAlertMessage sensorAlertMessage = message.getSensorAlertMessage();
        PublishDeliveryEntry publishDeliveryEntry = message.getPublishDeliveryEntry();
        Document alertMessageStructureDocument = this.stringToDom(sensorAlertMessage.getAlertMessageStructure());
        Element alertMessageStructure = alertMessageStructureDocument.detachRootElement();

        //get message type
        Element observedProperty = (Element) alertMessageStructure.getChildren().get(0);
        String type = observedProperty.getName();

        //get definition and uom of alert message structures
        Element content = observedProperty.getChild("Content");
        String definition = content.getAttributeValue("definition");
        String uom = content.getAttributeValue("uom");

        //get the correct filter from hashmap
        FilterKey filterKey = new FilterKey();
        filterKey.setDefinition(definition);
        filterKey.setUom(uom);

        Element operator = publishDeliveryEntry.resultFilter.get(filterKey);
        Boolean send=true;
        if(operator!=null){
            String value = operator.getText();

            //apply filter rule depending on message type
            send = applyFilterRule(type, operator.getName(), sensorAlertMessage.getValue(), value);
        }
        //build sas alert message and send it into muc
        if (send) {
            SASAlertMessage sasAlertMessage = new SASAlertMessage();
            sasAlertMessage.setBodyText(sensorAlertMessage.toString());
            sasAlertMessage.setHeader(sensorAlertMessage.getAlertMessageStructure());
            logger.debug("Prepared SASAlertMessage:" + "\n" + sasAlertMessage.toXml());
            sendSASAlertMessage(publishDeliveryEntry.mucName, sasAlertMessage);
        }

    }

    private void sendSASAlertMessage(String mucName, SASAlertMessage sasAlertMessage) {
        List<String> params = new ArrayList();
        params.add(mucName);
        params.add(sasAlertMessage.toXml());
        try {
            this.invoke("DispatcherAgent", "sendMessageAgentRoom", true, params);
        } catch (CleverException ex) {
            logger.error("Error invoking sendMessage on dispatcher " + ex);
        }
    }

    private Document stringToDom(String xmlSource) {
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

    public void handleAdvertiseRequest(Notification notification) {
        try {
            String advertiseRequest = (String) notification.getBody();
            Document doc = stringToDom(advertiseRequest); //TODO: validate document
            logger.debug("Received advertise request:\n" + advertiseRequest);

            Element advertiseElement = doc.detachRootElement();
            String requestId = ((Element) advertiseElement.getChild("AlertMessageStructure").getChildren().get(0)).getChild("Content").getAttributeValue("definition");
            String advertiseResponse = "";
            String advertiseQuery;
            requestId = requestId.replaceAll(Pattern.quote(":"), "_");


            Element desiredPublicationExpirationElement = advertiseElement.getChild("DesiredPublicationExpiration");
            String desiredPublicationExpiration = desiredPublicationExpirationElement.getText();

            //check desiredPublicationExpiration>currentDate
            Date currentDate = new Date();
            Date desiredExpirationDate = sdf.parse(desiredPublicationExpiration);
            Element advertiseResponseElement = new Element("AdvertiseResponse");
            Element publicationIdElement = new Element("PublicationID");
            if (desiredExpirationDate.after(currentDate)) {
                //check if advertise already exists
                advertiseQuery = this.query("/advertisements/advertise[RequestID=\"" + requestId + "\" and @hm=\""+notification.getHostId()+"\"]");

                if (advertiseQuery.equals("")) {//advertise doesn't exists

                    //generate pubId and soi
                    String publicationId = "Pub-" + Math.abs(uuidGenerator.generateTimeBasedUUID().hashCode());
                    String subscriptionOfferingId = "Soi-" + Math.abs(uuidGenerator.generateTimeBasedUUID().hashCode());




                    //update hashtable
                    this.pubSoi.put(publicationId, subscriptionOfferingId);


                    //insert new advertise in db
                    this.insertNode("<advertise pubId='" + publicationId /*+ "' expires='" + desiredPublicationExpiration*/ + "' hm='" + notification.getHostId() + "'>"
                            + "<SubscriptionOfferingID>" + subscriptionOfferingId + "</SubscriptionOfferingID>"
                            + "<DesiredPublicationExpiration>" + desiredPublicationExpiration + "</DesiredPublicationExpiration>"
                            + "<RequestID>" + requestId + "</RequestID>"
                            + "</advertise>", "into", "/advertisements");

                    //update capabilities document
                    Element root = new Element("SubscriptionOffering");
                    Element subscriptionOfferingIdElement = new Element("SubscriptionOfferingID");
                    subscriptionOfferingIdElement.addContent(subscriptionOfferingId);
                    root.addContent(subscriptionOfferingIdElement);
                    root.addContent(advertiseElement.getChild("AlertMessageStructure").detach());
                    root.addContent(advertiseElement.getChild("FeatureOfInterest").detach());
                    root.addContent(advertiseElement.getChild("OperationArea").detach());
                    root.addContent(advertiseElement.getChild("AlertFrequency").detach());
                    String capabilities = outputter.outputString(root);
                    this.insertNode(capabilities, "into", "/capabilities/Contents/SubscriptionOfferingList");


                    //build advertise response
                    advertiseResponseElement.setAttribute("expires", desiredPublicationExpiration); //TODO: manage expiration
                    publicationIdElement.setText(publicationId);
                    advertiseResponseElement.addContent(publicationIdElement);

                } else {//advertise exists
                    //update advertisement
                    
                    
                    //update expiration
                    this.replaceNode("/advertisements/advertise[./RequestID=\"" + requestId + "\"]/DesiredPublicationExpiration", outputter.outputString(desiredPublicationExpirationElement));
                    
                    //update subscriptionOffering
                    String getSoiQuery = "../../../../advertisements/advertise[./RequestID=\"" + requestId + "\"]/SubscriptionOfferingID/text()";
                    String subscriptionOfferingQuery = "/capabilities/Contents/SubscriptionOfferingList"
                            + "/SubscriptionOffering[./SubscriptionOfferingID=" + getSoiQuery + "]";

                    this.replaceNode(subscriptionOfferingQuery + "/FeatureOfInterest", outputter.outputString(advertiseElement.getChild("FeatureOfInterest")));
                    this.replaceNode(subscriptionOfferingQuery + "/OperationArea", outputter.outputString(advertiseElement.getChild("OperationArea")));
                    this.replaceNode(subscriptionOfferingQuery + "/AlertFrequency", outputter.outputString(advertiseElement.getChild("AlertFrequency")));
                    



                    //build advertise response
                    Document advertiseResponseDocument = this.stringToDom(advertiseQuery);
                    Element advertiseQueryElement = advertiseResponseDocument.detachRootElement();
                    publicationIdElement.setText(advertiseQueryElement.getAttributeValue("pubId"));
                    advertiseResponseElement.addContent(publicationIdElement);
                    advertiseResponseElement.setAttribute("expires", advertiseQueryElement.getChildText("DesiredPublicationExpiration"));
                }
            } else {
                logger.debug("Desired expiration date is after current date");
                advertiseResponseElement.addContent(publicationIdElement);
                advertiseResponseElement.setAttribute("expires", "");
            }
            advertiseResponse = outputter.outputString(advertiseResponseElement);
            //send response to hm agent
            List params = new ArrayList();
            params.add(requestId);
            params.add(advertiseResponse);
            try {
                this.remoteInvocation(notification.getHostId(), "SASAgentHm", "handleAdvertiseResponse", true, params);
            } catch (CleverException ex) {
                logger.error("Error sending advertisement response " + ex);
            }


        } catch (ParseException ex) {
            logger.error("Error checking expiration date: " + ex);
        } catch (CleverException ex) {
            logger.error("Error handling advertise request: " + ex);
        }

    }

    public String getCapabilities(String getCapabilitiesRequest) {
        
        Document doc = stringToDom(getCapabilitiesRequest); //TODO: validate document
        Element getCapabilitiesElement = doc.detachRootElement();
        
        Element sectionsElement = getCapabilitiesElement.getChild("Sections");
        Document getCapabilitiesResponseDoc = new Document();
        String getCapabilitiesResponse = "";
        Element capabilitiesElement = new Element("Capabilities");
        
        if (sectionsElement != null) {
            List children = sectionsElement.getChildren();
            Iterator iterator = children.iterator();
            Element element = null;
            String parameterName = "";
            String section = "";
            
            while (iterator.hasNext()) {
                try {
                    element = (Element) iterator.next();
                    parameterName = element.getText();
                    
                    section = this.query("/capabilities/" + parameterName);
                    
                    capabilitiesElement.addContent(this.stringToDom(section).detachRootElement());
                } catch (CleverException ex) {
                    logger.error("Error getting capabilities: " + ex);
                }


            }
            getCapabilitiesResponseDoc.addContent(capabilitiesElement);
            getCapabilitiesResponse = outputter.outputString(getCapabilitiesResponseDoc);

        }

        return getCapabilitiesResponse;
            
    }

    public String renewSubscription(String renewSubscriptionRequest) {
        String renewSubscriptionResponseString = "";
        try {
            Document renewSubscriptionRequestDoc = this.stringToDom(renewSubscriptionRequest);
            Element renewSubscriptionElement = renewSubscriptionRequestDoc.detachRootElement();

            String subscriptionId = renewSubscriptionElement.getChildText("SubscriptionID");
            Element status = new Element("Status");
            status.setText("OK");
            String soiExpiration = "";
            String subscription = this.query("/subscriptions/subscription[@subId=\"" + subscriptionId + "\"]");

            String newExpiration = "";
            if (!subscription.equals("")) {
                try {

                    soiExpiration = this.getSubscriptionExpiration(subscriptionId);
                    Element subscriptionElement = this.stringToDom(subscription).detachRootElement();
                    String actualSubscriptionExpiration = subscriptionElement.getAttributeValue("expires");
                    newExpiration = actualSubscriptionExpiration;
                    Date actualSubscriptionExpirationDate = sdf.parse(actualSubscriptionExpiration);
                    Date soiExpirationDate = sdf.parse(soiExpiration);
                    //renew it's possible
                    if (actualSubscriptionExpirationDate.before(soiExpirationDate)) {
                        //update db
                        subscriptionElement.setAttribute("expires", soiExpiration);
                        newExpiration = soiExpiration;
                        
                        this.replaceNode("/subscriptions/subscription[@subId=\"" + subscriptionId + "\"]", outputter.outputString(subscriptionElement));

                        List soiList = subscriptionElement.getChild("AssignedSoiList").getChildren();
                        //insert into hashtables
                        PublishDeliveryEntry publishDeliveryEntry = new PublishDeliveryEntry();
                        publishDeliveryEntry.mucName = subscriptionElement.getChildText("XMPPURI");
                        publishDeliveryEntry.mucPassword = subscriptionElement.getChild("XMPPURI").getAttributeValue("password");
                        publishDeliveryEntry.resultFilter = createFilterHashMap(subscriptionElement.getChildren("ResultFilter"));
                        updatePublishDelivery(soiList, publishDeliveryEntry, subscriptionId, soiExpiration);
                        //join the room
                        try {
                            List<String> joinParameters = new ArrayList();
                            joinParameters.add(agentName);
                            joinParameters.add(publishDeliveryEntry.mucName);
                            joinParameters.add(publishDeliveryEntry.mucPassword);
                            this.invoke("DispatcherAgent", "joinAgentRoom", true, joinParameters);
                        } catch (CleverException ex) {
                            logger.error("Error joining rooms: " + ex);
                        }

                    } else {
                        status.setText("Error");
                    }
                } catch (CleverException ex) {
                    logger.error("Error updating db: " + ex);
                    status.setText("Error");
                } catch (ParseException ex) {
                    logger.error("Parse date error: " + ex);
                    status.setText("Error");

                }
            } else {
                status.setText("Error");
            }

            Element renewSubscriptionResponse = new Element("RenewSubscriptionResponse");
            renewSubscriptionResponse.setAttribute("expires", newExpiration);
            Element subscriptionIdElement = new Element("SubscriptionID");
            subscriptionIdElement.setText(subscriptionId);
            renewSubscriptionResponse.addContent(subscriptionIdElement);
            renewSubscriptionResponse.addContent(status);
            renewSubscriptionResponseString = outputter.outputString(renewSubscriptionResponse);
        } catch (CleverException ex) {
            logger.error("Error renewing advertisement: " + ex);
        }
        return renewSubscriptionResponseString;
    }

    public String subscribe(String subscribeRequest) {
        Document subscribeRequestDocument = this.stringToDom(subscribeRequest);
        Element subscribeRequestElement = subscribeRequestDocument.detachRootElement();

        //generate subId
        String subscriptionId = "Sub-" + Math.abs(uuidGenerator.generateTimeBasedUUID().hashCode());

        //build subscription
        Element subscriptionElement = new Element("subscription");
        subscriptionElement.setAttribute("subId", subscriptionId);


        List subscribeRequestList = subscribeRequestElement.getChildren();
        Iterator subscribeRequestIterator = subscribeRequestList.iterator();
        Element element = null;
        while (subscribeRequestIterator.hasNext()) {
            element = (Element) subscribeRequestIterator.next();
            Element elementClone = (Element) element.clone();
            subscriptionElement.addContent(elementClone.detach());
        }

        //assign the soi
        AssignedSoi assignedSoi = assignSoi(subscribeRequestElement);
        //List<String> soiList=assignedSoi.soiList;
        List soiList = assignedSoi.soiList.getChildren();
        
        //assign expiration date (min expiration date of assigned soi)
        String expirationDate = assignedSoi.minExpirationDate;
        subscriptionElement.setAttribute("expires", expirationDate);


        Element xmppUri = new Element("XMPPURI");
        xmppUri.setAttribute("password", "");
        if (soiList.size() > 0) {
            //assign the muc
            List<String> joinParameters = new ArrayList();
            joinParameters.add(agentName);
            String mucPassword = Support.generatePassword(7);
            joinParameters.add(mucPassword);
            String mucName = "";
            try {
                mucName = (String) this.invoke("DispatcherAgent", "joinAgentRoom", true, joinParameters);
            } catch (CleverException ex) {
                logger.error("Error joining muc: " + ex);
            }

            xmppUri.setAttribute("password", mucPassword);
            xmppUri.setText(mucName);



            //retreive filter list
            List resultFilterList = subscribeRequestElement.getChildren("ResultFilter");

            //update hashtable
            PublishDeliveryEntry publishDeliveryEntry = new PublishDeliveryEntry();
            publishDeliveryEntry.mucName = mucName;
            publishDeliveryEntry.mucPassword = mucPassword;
            publishDeliveryEntry.resultFilter = createFilterHashMap(resultFilterList);
            updatePublishDelivery(soiList, publishDeliveryEntry, subscriptionId, expirationDate);

            //insert subscription into db
            subscriptionElement.addContent(xmppUri);
            subscriptionElement.addContent(assignedSoi.soiList);
            this.insertNode(outputter.outputString(subscriptionElement), "into", "/subscriptions");
        }
        //build subscribe response
        Element subscribeResponse = new Element("SubscribeResponse");

        subscribeResponse.setAttribute("SubscriptionID", subscriptionId);
        subscribeResponse.setAttribute("expires", expirationDate);
        Element xmppResponse = new Element("XMPPResponse");
        xmppResponse.addContent(xmppUri.detach());
        subscribeResponse.addContent(xmppResponse);
        Document subscribeResponseDocument = new Document(subscribeResponse);
        return outputter.outputString(subscribeResponseDocument);
    }

    private void updatePublishDelivery(List soiList, PublishDeliveryEntry publishDeliveryEntry,
            String subscriptionId, String expirationDate) {
        try {
            Iterator iterator = soiList.iterator();
            Element assignedSoi = null;
            List<PublishDeliveryEntry> se = null;
            SubscriptionEntry subscriptionEntry = null;
            //HashMap<String, Integer> publishDeliveryEntryMap = null;
            while (iterator.hasNext()) {
                assignedSoi = (Element) iterator.next();
                se = this.publishDelivery.get(assignedSoi.getText());
                if (se == null) {
                    se = new ArrayList<PublishDeliveryEntry>();
                }
                se.add(publishDeliveryEntry);
                this.publishDelivery.put(assignedSoi.getText(), se);
                //update subscriptions
                subscriptionEntry = this.subscriptions.get(subscriptionId);
                if (subscriptionEntry == null) {
                    subscriptionEntry = new SubscriptionEntry();
                    subscriptionEntry.soiIndex = new HashMap<String, Integer>();
                }
                subscriptionEntry.soiIndex.put(assignedSoi.getText(), se.size() - 1);


            }
            if (subscriptionEntry.timer != null) {
                subscriptionEntry.timer.cancel();
                subscriptionEntry.timer.purge();
            }
            subscriptionEntry.timer = new Timer();
            SubscriptionExpirationTask subscriptionExpirationTask = new SubscriptionExpirationTask(this, subscriptionId);
            Date expiration = sdf.parse(expirationDate);
            subscriptionEntry.timer.schedule(subscriptionExpirationTask, expiration);
            this.subscriptions.put(subscriptionId, subscriptionEntry);
        } catch (ParseException ex) {
            logger.error("Error updating subscriptions hashtable: " + ex);
        }

    }

    private HashMap<FilterKey, Element> createFilterHashMap(List resultFilterList) {
        HashMap<FilterKey, Element> resultFilter = new HashMap<FilterKey, Element>();

        Iterator iterator = resultFilterList.iterator();
        Element element = null;
        while (iterator.hasNext()) {
            element = (Element) iterator.next();
            FilterKey filterKey = new FilterKey();
            filterKey.setDefinition(element.getAttributeValue("ObservedPropertyDefinition"));
            filterKey.setUom(element.getAttributeValue("uom"));
            resultFilter.put(filterKey, (Element) (element.getChildren().get(0)));

        }
        return resultFilter;
    }

   /* public void handleCancelAdvertisementRequest(Notification notification) {
        String cancelAdvertisementRequest = (String) notification.getBody();
        Document doc = stringToDom(cancelAdvertisementRequest); //TODO: validate document
        logger.debug("Received cancel advertise request:\n" + cancelAdvertisementRequest);
        Element cancelAdvertisementElement = doc.detachRootElement();
        Element cancellationStatusElement = new Element("CancellationStatus");
        cancellationStatusElement.setText("confirmed");

        //get pubId from request
        String publicationId = cancelAdvertisementElement.getChildText("PublicationID");

        //get soi from pub-Soi hashtable
        String soi = this.pubSoi.get(publicationId);
        if (soi == null) {
            cancellationStatusElement.setText("invalid_PublicationID");
        } else {
            //delete soi from soi-SubscribeEntryList hashtable
            this.publishDelivery.remove(soi);
            try {
                //delete advertisement from db
                this.deleteNode("/advertisements/advertise[@pubId=\"" + publicationId + "\"]");
                //delete soi from capabilities document
                this.deleteNode("/capabilities/Contents/SubscriptionOfferingList/SubscriptionOffering[SubscriptionOfferingID=\"" + soi + "\"]");
                //delete soi from hashtable pub-soi
                this.pubSoi.remove(publicationId);

                //get all subscriptions assigned to this soi
                String affectedSubscriptions = this.query("/subscriptions[subscription[//AssignedSoi=\"" + soi + "\"]]");

                //iterate affected subId
                Document affectedSubscriptionsDoc = this.stringToDom(affectedSubscriptions);
                Element affectedSubscriptionsElement = affectedSubscriptionsDoc.detachRootElement();
                List affectedSubscriptionsList = affectedSubscriptionsElement.getChildren();
                Iterator iterator = affectedSubscriptionsList.iterator();
                Element affectedSubscription = null;

                while (iterator.hasNext()) {
                    affectedSubscription = (Element) iterator.next();
                    String affectedSubId = affectedSubscription.getAttributeValue("subId");

                    //update subscription hashtable
                    SubscriptionEntry subscriptionEntry = this.subscriptions.get(affectedSubId);
                    HashMap<String, Integer> publishDeliveryMap = subscriptionEntry.soiIndex;
                    publishDeliveryMap.remove(soi);
                    if (publishDeliveryMap.isEmpty()) { //it was the only soi assigned
                        //remove subId entry
                        this.subscriptions.remove(affectedSubId);
                        //delete also subscription from db
                        this.deleteNode("/subscriptions/subscription[@subId=\"" + affectedSubId + "\"]");

                    }

                }

                //delete soi assignation from residual subscriptions
                this.deleteNode("/subscriptions//AssignedSoi[text()=\"" + soi + "\"]");

            } catch (CleverException ex) {
                cancellationStatusElement.setText("error");
                logger.error("Error deleting advertisement from db: " + ex);
            }


        }

        //build cancelAdvertisement Response
        Element cancelAdvertisementResponseElement = new Element("CancelAdvertisementResponse");
        Element publicationIdElement = new Element("PublicationID");
        publicationIdElement.setText(publicationId);

        cancelAdvertisementResponseElement.addContent(publicationIdElement);
        cancelAdvertisementResponseElement.addContent(cancellationStatusElement);
        Document cancelAdvertisementResponseDoc = new Document(cancelAdvertisementResponseElement);
        String cancelAdvertisementResponse = outputter.outputString(cancelAdvertisementResponseDoc);

        //invoke handleCancelAdvertiseResponse on hm agent
        String requestId = cancelAdvertisementElement.getChildText("RequestID");
        List params = new ArrayList();
        params.add(requestId);
        params.add(cancelAdvertisementResponse);
        try {
            this.remoteInvocation(notification.getHostId(), "SASAgentHm", "handleCancelAdvertisementResponse", true, params);
        } catch (CleverException ex) {
            logger.error("Error sending cancelAdvertisement response " + ex);
        }


    }*/

    public String removeSubscriptionFromTables(String subscriptionId) {
        String message = "OK";
        //remove from hashtable
        SubscriptionEntry subscriptionEntry = this.subscriptions.get(subscriptionId);
        if (subscriptionEntry != null) {
            HashMap subscriptionEntryMap = subscriptionEntry.soiIndex;
            Iterator iterator = subscriptionEntryMap.entrySet().iterator();
            subscriptionEntry.timer.cancel();
            subscriptionEntry.timer.purge();
            subscriptions.remove(subscriptionId);

            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                List<PublishDeliveryEntry> publishDeliveryEntryList = this.publishDelivery.get((String) entry.getKey());

                //leave muc
                List<String> joinParameters = new ArrayList();
                joinParameters.add(publishDeliveryEntryList.get((Integer) entry.getValue()).mucName);
                try {
                    this.invoke("DispatcherAgent", "leaveAgentRoom", true, joinParameters);
                } catch (CleverException ex) {
                    logger.error("Error leaving muc: " + ex);
                    message = "ERROR";
                }

                publishDeliveryEntryList.remove(((Integer) entry.getValue()).intValue());
                this.publishDelivery.put((String) entry.getKey(), publishDeliveryEntryList);


            }
        }

        return message;

    }

    public String cancelSubscription(String cancelSubscriptionRequest) {
        String cancelSubscriptionResponseString = "";
        try {
            Document cancelSubscriptionRequestDocument = this.stringToDom(cancelSubscriptionRequest);
            Element cancelSubscriptionRequestElement = cancelSubscriptionRequestDocument.detachRootElement();

            //get subscription id
            String subscriptionId = cancelSubscriptionRequestElement.getChild("SubscriptionID").getText();
            Element statusElement = new Element("Status");
            statusElement.setText("OK");
            //remove subscription from db
            String subscription = query("/subscriptions/subscription[@subId=\"" + subscriptionId + "\"]");
            if (!subscription.equals("")) {
                try {
                    this.deleteNode("/subscriptions/subscription[@subId=\"" + subscriptionId + "\"]");

                    statusElement.setText(this.removeSubscriptionFromTables(subscriptionId));
                } catch (CleverException ex) {
                    statusElement.setText("ERROR");
                }

            } else {//status=error
                statusElement.setText("ERROR");
            }

            //build cancel subscription response
            Element cancelSubscriptionResponse = new Element("CancelSubscriptionResponse");
            cancelSubscriptionResponse.addContent(cancelSubscriptionRequestElement.getChild("SubscriptionID").detach());
            cancelSubscriptionResponse.addContent(statusElement);
            Document cancelSubscriptionResponseDoc = new Document(cancelSubscriptionResponse);
            cancelSubscriptionResponseString = outputter.outputString(cancelSubscriptionResponseDoc);
        } catch (CleverException ex) {
            logger.error("Error deleting subscription: " + ex);
        }

        return cancelSubscriptionResponseString;
    }

    private AssignedSoi assignSoi(Element subscribeRequestElement) {        
        Element assignedSoiList=new Element("AssignedSoiList");
        String expirationDate = "";
        //check soi
        Element subscriptionOfferingIdElement = subscribeRequestElement.getChild("SubscriptionOfferingId");
        if (subscriptionOfferingIdElement != null) {
            try {
                //there is a soi, don't check other parameters
                String desiredSoi = subscriptionOfferingIdElement.getText();
                String queryResult = this.query("/capabilities/Contents/SubscriptionOfferingList/SubscriptionOffering[SubscriptionOfferingID=\"" + desiredSoi + "\"]");
                if (!queryResult.equals("")) {
                    expirationDate = this.query("/advertisements/advertise[./SubscriptionOfferingID=\"" + desiredSoi + "\"]/DesiredPublicationExpiration/text()");
                    expirationDate=expirationDate.replaceAll("\n", "");
                    Element assignedSoi=new Element("AssignedSoi");
                    assignedSoi.setText(desiredSoi);
                    assignedSoiList.addContent(assignedSoi);
                    
                }
            } catch (CleverException ex) {
                logger.error("Error assigning soi: " + ex);
            }
        } else {
            try {
                //get SubscriptionOfferingList from Capabilities Document
                //get SubscriptionOfferingList from Capabilities Document


                Element subscribeRequestElementLocation = subscribeRequestElement.getChild("Location");
                Element featureOfInterestName = subscribeRequestElement.getChild("FeatureOfInterestName");
                List resultFilterList = subscribeRequestElement.getChildren("ResultFilter");

                StringBuffer xpathConditions = new StringBuffer();


                //get feature of interest xpath
                xpathConditions.append("true() and ");
                String featureOfInterestXpath = "";
                if (featureOfInterestName != null) {
                    featureOfInterestXpath = "./FeatureOfInterest"
                            + "/Name[text()=\"" + featureOfInterestName.getText() + "\"] and ";
                }
                xpathConditions.append(featureOfInterestXpath);

                //get location xpaths
                xpathConditions.append("true() and ");
                String locationXpaths = "";
                if (subscribeRequestElementLocation != null) {
                    locationXpaths = this.getNodeExistsXPathQuery((Element) subscribeRequestElementLocation.getChildren().get(0)) + " and ";
                }
                xpathConditions.append(locationXpaths);


                //get filter xpaths
                xpathConditions.append("true()");
                String filterXpaths = "";
                if (resultFilterList.size() > 0) {
                    filterXpaths = " and " + this.getFilterExistsXPathQuery(resultFilterList);
                }
                xpathConditions.append(filterXpaths);

                //build query
                String query = "/capabilities"
                        + "/Contents"
                        + "/SubscriptionOfferingList"
                        + "/SubscriptionOffering[" + xpathConditions.substring(0) + "]"
                        + "/SubscriptionOfferingID";

                //execute query
                String soiListString = "";
                soiListString = this.query(query);

                //get expiration date
                String expirationDateQuery = "min("
                        + this.agentPrefix + "/advertisements/advertise[SubscriptionOfferingID="
                        + "../.." + query + "/text()]/DesiredPublicationExpiration/xs:dateTime(.))";


                expirationDate = this.rawQuery(expirationDateQuery);
                expirationDate = expirationDate.replaceAll("\n", "");



                //build soi List
                if (!soiListString.equals("")) {
                    Document soiListDocument = this.stringToDom("<soiList>" + soiListString + "</soiList>");
                    Element soiListElement = soiListDocument.detachRootElement();

                    List soiListList = soiListElement.getChildren();
                    Iterator iterator = soiListList.iterator();

                    while (iterator.hasNext()) {
                        Element soi = (Element) iterator.next();
                        Element soiListElem = new Element("AssignedSoi");
                        soiListElem.setText(soi.getText());
                        assignedSoiList.addContent(soiListElem);

                    }


                }
            } catch (CleverException ex) {
                logger.error("Error assigning soi: " + ex);
            }


        }
        AssignedSoi assignedSoi = new AssignedSoi();
        assignedSoi.soiList = assignedSoiList;
        assignedSoi.minExpirationDate = expirationDate;
        return assignedSoi;
    }

    private String getNodeExistsXPathQuery(Element node) {
        StringBuffer buffer = new StringBuffer();
        XPathContentHandler fragmentContentHandler = new XPathContentHandler(xr, buffer);
        xr.setContentHandler(fragmentContentHandler);
        try {
            xr.parse(new InputSource(new StringReader(outputter.outputString(node))));
        } catch (IOException ex) {
            logger.error("IOException: " + ex);
        } catch (SAXException ex) {
            logger.error("SAXException: " + ex);
        }

        return buffer.substring(0);

    }

    private String getFilterExistsXPathQuery(List resultFilterList) {
        StringBuffer xpathQuery = new StringBuffer();
        Iterator iterator = resultFilterList.iterator();
        Element element = null;
        String definition = "";
        String uom = "";
        xpathQuery.append("(");
        int i = 0;
        while (iterator.hasNext()) {
            element = (Element) iterator.next();
            definition = element.getAttributeValue("ObservedPropertyDefinition");
            uom = element.getAttributeValue("uom");
            xpathQuery.append("./AlertMessageStructure//Content[@definition=\"" + definition + "\" and @uom=\"" + uom + "\"]");

            if (iterator.hasNext() && resultFilterList.size() > 1) {
                xpathQuery.append(" or ");
            } else {
                xpathQuery.append(")");
            }

        }

        return xpathQuery.substring(0);

    }

    @Override
    public void initialization() throws Exception {
        
        
    }

    @Override
    public void shutDown() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initCleverResouces() {
        try {
            InputStream inxml = getClass().getResourceAsStream( "/org/clever/ClusterManager/SAS/configuration_web.xml" );
            ParserXML pXML = new ParserXML( fs.xmlToString( inxml ) );
            
            
                parameterContainer=ParameterDbContainer.getInstance();
                Element dbParams=pXML.getRootElement().getChild("dbParams");
                parameterContainer.setDbDriver(dbParams.getChildText("driver"));
                parameterContainer.setDbServer(dbParams.getChildText("server"));
                parameterContainer.setDbPassword(dbParams.getChildText("password"));
                parameterContainer.setDbUsername(dbParams.getChildText("username"));
                parameterContainer.setDbName(dbParams.getChildText("name"));
                
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SASAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
        
    }
    
    
    public String servicecommand(String command){
    String ret="service command not found" ;  
   if (command.equalsIgnoreCase("updateoffers")){
       checkCapabilities();
       checkAdvertisement();
       ret="updateoffers done";
   }
   if (command.startsWith("addsub")){
       String value = command.split("#")[1];
       //logger.debug("?=) "+value);
       addsub(value);
       ret="addsub done";
   }    
   if (command.startsWith("ondemand")){
       String value = command.split("#")[1];
       //logger.debug("?=) "+value);
       onDemand(value);
       ret="addsub done";
   }    
        
        
       return ret; 
    }
    
    public void onDemand(String soi){
        try {
            String checkpub = "//advertise";
            String rAd="<adv>"+this.query(checkpub)+"</adv>";
           //logger.info("?=)%* Advertise result:\n"+rAd);
            
            org.jdom.Document docSol = this.stringToDom(rAd);
            Element getCapabilitiesElement = docSol.detachRootElement();
            
            List adv = getCapabilitiesElement.getChildren();
            Iterator adviterator = adv.iterator();
            String idAdv=null;
            String nAdv=null;
            String sAdv=null;
            String dAdv=null;
            String vAdv=null;
             String pubId=null;
             String hostId=null;
             int idSens=0;
             int idFen=0;
            while (adviterator.hasNext()) {
            
            Element sectionsElement = (Element) adviterator.next();
            if (sectionsElement != null) {
                idAdv=sectionsElement.getAttributeValue("pubId");
                dAdv=sectionsElement.getAttributeValue("hm");
                sAdv=sectionsElement.getChildText("SubscriptionOfferingID");
                nAdv=sectionsElement.getChildText("RequestID");
                vAdv=sectionsElement.getChildText("DesiredPublicationExpiration");
           
          // logger.info("?=)%* soi: "+sAdv+" pubid: "+idAdv);
           if(sAdv.equalsIgnoreCase(soi)){
               pubId=idAdv;
               hostId=dAdv;
           }
           
            }
            }
           
            
           logger.debug(" Publication Id: "+pubId+" host: "+hostId);
           
           //INSerisco un finto sensore
           
           db=Database.getNewInstance(this.parameterContainer);
           String vsens="SELECT ID FROM `Sensore` WHERE `Nome`='"+soi+"' AND `MAC`='"+pubId+"'";
            ResultSet rsens = db.exQuery(vsens);
            if(rsens.next()){
               idSens=rsens.getInt(1); 
            }else{
               String ins="INSERT INTO `CleverResources`.`Sensore` (`ID`, `Nome`, `MAC`, `Descrizione`, `Coordinata`, `Proprietario`) VALUES (NULL, '"+soi+"', '"+pubId+"', NULL, '5', '1');";
               idSens=db.exInsert(ins);
            
            }
           
           //recupero l'id del fenomeno
            
            String selfen="SELECT `Fenomeno`.ID FROM `Fenomeno`,`SottoscrizioneOfferta`,`FenomeniSottoscrizione` WHERE (`Fenomeno`.ID=`FenomeniSottoscrizione`.Fenomeno) AND (`SottoscrizioneOfferta`.ID=`FenomeniSottoscrizione`.SottoscrizioneOff) AND `SottoscrizioneOfferta`.SoId='"+soi+"' ";
            ResultSet rfen = db.exQuery(selfen);
            if(rfen.next()){
              idFen=rfen.getInt(1);  
            }
           // cerco tutte le publicazioni 
           
           String qpub="//org.clever.HostManager.SAS.SensorAlertMessage[publicationId=\""+pubId+"\"]";
     
         
          
           
            List<String> params = new ArrayList();
          
            params.add(hostId);
            params.add("SASAgentHm");
            params.add(qpub);
            String rpu = (String) this.invoke("DatabaseManagerAgent", "query", true, params);
            //logger.info("?=)%* Publication result:\n"+rpu);
            String rpu2="<pub>"+rpu+"</pub>";
            org.jdom.Document docpub = this.stringToDom(rpu2);
            Element publicat = docpub.detachRootElement();
            List puc = publicat.getChildren();
            Iterator pubi = puc.iterator();
            while (pubi.hasNext()) {
            
            Element pubElement = (Element) pubi.next();
            
            
            if (pubElement != null) {
                String dato = pubElement.getChildText("value");
                String tempo = pubElement.getChildText("timeOfAlert");
                
                
//                String latitud = pubElement.getChildText("latitude");
//                String longitud = pubElement.getChildText("longitude");
//                String altitud = pubElement.getChildText("altitude");
//                db=Database.getNewInstance(this.parameterContainer);
//                //verifico se la coordinata esiste
//                int idcoord=0;
//                String vcoor="SELECT ID FROM Coordinata WHERE latitudine='"+latitud+" AND Longitudine='"+longitud+"' AND Altitudine='"+altitud+"'";
//                ResultSet rcoor = db.exQuery(vcoor);
//                if(rcoor.next()){
//                    idcoord=rcoor.getInt(1);
//                }else{
//                    //inseirsco la nuova coordinata
//                    String inscoo="INSERT INTO `CleverResources`.`Coordinata` (`ID`, `Latitudine`, `Longitudine`, `Altitudine`, `Descrizione`) VALUES (NULL, '"+latitud+"', '"+longitud+"', '"+altitud+"', NULL);";
//                    idcoord = db.exInsert(inscoo);
//                }
//                
//                
//                logger.info("?=)%* -"+latitud+"-"+longitud+"-"+altitud+" - ");
//                
                
             
            
            String tmp= tempo.replace("T"," ");
            int idDat=0;
            //verificare se il dato esiste gia
            String vdat="SELECT ID FROM `Dato` WHERE `Valore`='"+dato+"' AND `TimeStamp`='"+tmp+"'";
            ResultSet rdat = db.exQuery(vdat);
            if(rdat.next()){
             idDat=rdat.getInt(1);   
            }else{
            String Insdat="INSERT INTO `CleverResources`.`Dato` (`ID`, `Valore`, `TimeStamp`) VALUES (NULL, '"+dato+"', '"+tmp+"');";
            idDat = db.exInsert(Insdat);
            }
            String mis="INSERT INTO `CleverResources`.`Misurazione` (`Sensore`, `Fenomeno`, `Dato`) VALUES ('"+idSens+"', '"+idFen+"', '"+idDat+"');";
            db.exUpdate(mis);
            
            logger.debug("Misuraz:"+idSens+"/"+idFen+"/"+idDat+"-"+dato+"-"+tmp);
            }
            }
            
           
           
           
        } catch (SQLException ex) {
            logger.error("SQLException "+ex);
        } catch (CleverException ex) {
            logger.error("Exception "+ex);
            //java.util.logging.Logger.getLogger(SASAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    
    
    
    public void checkCapabilities(){
        String SoId = null;
        String Alertfreq= null;
       
        
        try {
            String subscr = "/capabilities/Contents/SubscriptionOfferingList";
            String qresult=this.query(subscr);
           // logger.info("?=) query result:\n"+qresult);
            db=Database.getNewInstance(parameterContainer);
           // logger.info("DB: "+parameterContainer.getDbName());
            org.jdom.Document docSol = this.stringToDom(qresult);
            Element getCapabilitiesElement = docSol.detachRootElement();
            
            
            List so = getCapabilitiesElement.getChildren();
            Iterator soiterator = so.iterator();
            while (soiterator.hasNext()) {
                
            
            Element sectionsElement = (Element) soiterator.next();//getCapabilitiesElement.getChild("SubscriptionOffering");
            if (sectionsElement != null) {
               List children = sectionsElement.getChildren();
               Iterator iterator = children.iterator();
               Element element = null; 
               String parameterName = "";
               String section = "";
               String definition=null;
               String uom=null;
               String name=null;
               String desc=null;
               int idprop = -1;
               int idalert=-1;
               int idfen=-1;
               int iduom=-1;
            while (iterator.hasNext()) {
//                try {
                
                    element = (Element) iterator.next();
                    
                        
                    if(element.getName().equalsIgnoreCase("AlertMessageStructure")){
                     definition = element.getChild("QuantityProperty").getChild("Content").getAttributeValue("definition");
                     uom=element.getChild("QuantityProperty").getChild("Content").getAttributeValue("uom");    
                     
                     String fenomeno[]=definition.split(":");
                     String fen=fenomeno[fenomeno.length-1];
                     logger.debug("?=) fenomeno: "+fen);
                     
                     //inserisco nel db il fenomeno
                     //prima verifico se già esiste
                     String verfen="SELECT `ID` FROM `Fenomeno` WHERE `Nome`='"+fen+"' AND `Descrizione`='"+definition+"'";
                     ResultSet rfen = db.exQuery(verfen);
                     
                     if(rfen.next()){
                         idfen=rfen.getInt(1);
                     }else{
                      String insfen="INSERT INTO `CleverResources`.`Fenomeno` (`ID`, `Nome`, `Descrizione`) VALUES (NULL, '"+fen+"', '"+definition+"');";
                      idfen=db.exInsert(insfen);
                                            
                         
                     }
                     
                     
                     //inserisco l uom del fenomeno
                     //verifico se esiste 
                     String vuom="SELECT `ID` FROM `UnitaDiMisura` WHERE `Nome`='"+uom+"' AND `Simbolo`='"+uom+"'";
                     ResultSet rvuom = db.exQuery(vuom);
                     if(rvuom.next()){
                        iduom=rvuom.getInt(1);
                     }else{
                        String insuom="INSERT INTO `CleverResources`.`UnitaDiMisura` (`ID`, `Nome`, `Descrizione`, `Simbolo`) VALUES (NULL, '"+uom+"', '', '"+uom+"');";
                        iduom=db.exInsert(insuom);
                        
                        
                        
                        
                        
                     }
                     if(idfen!=-1 && iduom!=-1){
                     String colfen="INSERT INTO `CleverResources`.`Definito` (`Fenomeno`, `UdM`) VALUES ('"+idfen+"', '"+iduom+"')";
                     db.exUpdate(colfen);
                     }
                     //inserisco nel db questa proprietà
                     //prima verifico se già esiste
                     String verifyprop="SELECT `ID` FROM `ProprietaSottoscrizione` WHERE `Nome`='AlertMessageStructure' AND `Descrizione`='"+definition+"' AND `Uom`='"+uom+"'";
                     ResultSet rprop = db.exQuery(verifyprop);
                     if (rprop.next()){
                         idprop=rprop.getInt(1);
                     }else{
                     
                     String prop="INSERT INTO `CleverResources`.`ProprietaSottoscrizione` (`ID`, `Nome`, `Tipo`, `Valore`, `Uom`, `Descrizione`) VALUES (NULL, 'AlertMessageStructure', 'Capabilities',NULL, '"+uom+"','"+definition+"');";
                     logger.debug(" query: "+prop);
                     idprop = db.exInsert(prop);
                     //db.exUpdate(prop);
                     }
                     logger.debug("prop id: "+idprop);
                     
                     
                     
                    }else if (element.getName().equalsIgnoreCase("FeatureOfInterest")){
                         name=element.getChild("Name").getText();
                        desc=element.getChild("Description").getText();
                        logger.debug("name: "+name+" description: "+desc);
                    }
//                    else if(element.getName().equalsIgnoreCase("OperationArea")){
//                       // Element child = element.getChild("GeoLocation");
//                                logger.info("?=) Operation Area ");
//                                if (element.getChild("swe:GeoLocation").getChild("gml:boundedBy")!=null){
//                                    logger.info("?=) boundedBy");
//                                }
//                                else if (element.getChild("swe:GeoLocation").getChild("swe:longitude")!=null){
//                                    logger.debug("?=) coord");
//                                    String longit= element.getChild("swe:GeoLocation").getChild("swe:longitude").getChildText("Quantity");
//                                    String latit=element.getChild("swe:GeoLocation").getChild("swe:latitude").getChildText("Quantity");
//                                    String altitu=element.getChild("swe:GeoLocation").getChild("swe:altitude").getChildText("Quantity");
//                                    logger.info("?=) long= "+longit+" latitu= "+latit+" altitu= "+altitu);
//                                    
//                                    
//                                }
//                                else{
//                                    logger.debug("?=) else ");
//                                    
//                                    
//                                }
                    
//                    }
                    
                    
                    
                    else{
                    
                        
                    
                        String elementName=element.getName();
                        parameterName = element.getText();
                        
                        if (elementName.equalsIgnoreCase("SubscriptionOfferingID"))
                                SoId=parameterName;
                            
                        if (elementName.equalsIgnoreCase("AlertFrequency")){
                                Alertfreq=parameterName;
                                //prima verifico se già esiste
                                String verifyal="SELECT `ID` FROM `ProprietaSottoscrizione` WHERE `Nome`='AlertFrequency' AND `Tipo`='Capabilities' AND `Valore`='"+Alertfreq+"' AND `Uom`='Hz'";
                                ResultSet ral = db.exQuery(verifyal);
                                if (ral.next()){
                                    idalert=ral.getInt(1);
                                }else{
                                String alertquery="INSERT INTO `CleverResources`.`ProprietaSottoscrizione` (`ID`, `Nome`, `Tipo`, `Valore`, `Uom`, `Descrizione`) VALUES (NULL, 'AlertFrequency', 'Capabilities', '"+Alertfreq+"', 'Hz', '');";
                                idalert = db.exInsert(alertquery);
                                }
                        }
                        //logger.info("?=) element: "+element.getName()+" parameterName: "+parameterName);
                        
                    }
                    
                    
                    
                    
                    
                    
                    //section = this.query("/capabilities/" + parameterName);
                    
                    
//                } 
//                catch (CleverException ex) {
//                    logger.error("Error getting capabilities: " + ex);
//                }


            }
             //inserisco nel db queste info
            //verifico se è gia presente 
            String verysoi="SELECT * FROM `SottoscrizioneOfferta` WHERE `SoId`='"+SoId+"'";
            ResultSet rvsoi = db.exQuery(verysoi);
            if(rvsoi.next()){
                logger.debug(" SubscriptionOfferingId presente: "+rvsoi.getInt(1));
            }else{
            
            String querySoi="INSERT INTO `CleverResources`.`SottoscrizioneOfferta` (`ID`, `SoId`, `Nome`, `Descrizione`) VALUES (NULL, '"+SoId+"', '"+name+"', '"+desc+"');";
            int idSoi=db.exInsert(querySoi);
            logger.debug("return id: "+idSoi);
            
            String querycar="INSERT INTO `CleverResources`.`CaratteristicheSottoscrizione` (`SottoscrizioneOff`, `ProprietaSottoscrizione`) VALUES ('"+idSoi+"', '"+idprop+"');";
            db.exUpdate(querycar);
            
            String querycar2="INSERT INTO `CleverResources`.`CaratteristicheSottoscrizione` (`SottoscrizioneOff`, `ProprietaSottoscrizione`) VALUES ('"+idSoi+"', '"+idalert+"');";
            db.exUpdate(querycar2);
                
            String querycar3="INSERT INTO `CleverResources`.`FenomeniSottoscrizione` (`SottoscrizioneOff`, `Fenomeno`) VALUES  ('"+idSoi+"', '"+idfen+"');";
            db.exUpdate(querycar3);
            
            
            }
            
            
            
            
            
            
            
            
            }
            
            } 
            
            
            
            
            
            
        } catch (SQLException ex) {
            logger.error(ex);
        } catch (CleverException ex) {
            logger.error(ex);
        }
       
       
        
        
    }
    
    private void checkAdvertisement(){
        try {
           
            String checkAd = "/advertisements";
            String rAd=this.query(checkAd);
            
            //logger.info("?=) Advertisements result:\n"+rAd);
        
            db=Database.getNewInstance(this.parameterContainer);
           // logger.info("DB: "+this.parameterContainer.getDbName());
            org.jdom.Document docAdv = this.stringToDom(rAd);
            Element getCapabilitiesElement = docAdv.detachRootElement();
            
            List adv = getCapabilitiesElement.getChildren();
            Iterator adviterator = adv.iterator();
            String idAdv=null;
            String nAdv=null;
            String sAdv=null;
            String dAdv=null;
            String vAdv=null;
            while (adviterator.hasNext()) {
            
            Element sectionsElement = (Element) adviterator.next();
            if (sectionsElement != null) {
                idAdv=sectionsElement.getAttributeValue("pubId");
                dAdv=sectionsElement.getAttributeValue("hm");
                sAdv=sectionsElement.getChildText("SubscriptionOfferingID");
                nAdv=sectionsElement.getChildText("RequestID");
                vAdv=sectionsElement.getChildText("DesiredPublicationExpiration");
                
             //   logger.info("Advertisements of so "+sAdv+" :\n"+idAdv+" "+dAdv+" "+nAdv+" "+vAdv);
                 
                 int idsub=0;
                 int idprop=0;
                 int idprop2=0;
                //ottengo l'id della sottoscrizione offerta
                 String idsoi="SELECT `ID` FROM `SottoscrizioneOfferta` WHERE `SoId`='"+sAdv+"'";
                 ResultSet rs = db.exQuery(idsoi);
                 if (rs.next()){
                 idsub=rs.getInt(1);
                 
                 }
                
                
                
                //verifico se la prop esiste già
                String verprop="SELECT ID FROM `ProprietaSottoscrizione` WHERE `Nome`='Publication' AND `Tipo`='Advertise' AND `Valore`='"+idAdv+"' AND `Descrizione`='"+dAdv+"'";
                ResultSet rprop = db.exQuery(verprop);
                if (rprop.next()){
                    logger.debug("?=) Advertise gia esistente id: "+rprop.getInt(1));
                    idprop=rprop.getInt(1);
                    
                }else{
                 //inserisco la proprietà   
                 String insp ="INSERT INTO `CleverResources`.`ProprietaSottoscrizione` (`ID`, `Nome`, `Tipo`, `Valore`, `Uom`, `Descrizione`) VALUES (NULL, 'Publication', 'Advertise', '"+idAdv+"', NULL, '"+dAdv+"');";
                 idprop = db.exInsert(insp);
                   
                }
                 if (idsub!=0 && idprop!=0){
                 
                 //collego la proprieta con la sottoscrizione offerta
                 String carp="INSERT INTO `CleverResources`.`CaratteristicheSottoscrizione` (`SottoscrizioneOff`, `ProprietaSottoscrizione`) VALUES ('"+idsub+"', '"+idprop+"');";
                 db.exUpdate(carp);  
                 }
                
                 //seconda prop
                 
                 //verifico se la prop esiste già
                String verprop2="SELECT ID FROM `ProprietaSottoscrizione` WHERE `Nome`='Expiration' AND `Tipo`='Advertise' AND `Valore`='"+vAdv+"' AND `Descrizione`='"+nAdv+"'";
                ResultSet rprop2 = db.exQuery(verprop2);
                if (rprop2.next()){
                    logger.debug(" Advertise gia' esistente id: "+rprop2.getInt(1));
                    idprop2=rprop2.getInt(1);
                }else{
                 //inserisco la proprietà   
                 String insp2 ="INSERT INTO `CleverResources`.`ProprietaSottoscrizione` (`ID`, `Nome`, `Tipo`, `Valore`, `Uom`, `Descrizione`) VALUES (NULL, 'Expiration', 'Advertise', '"+vAdv+"', NULL, '"+nAdv+"');";
                 idprop2 = db.exInsert(insp2);
                 logger.debug("inserito un nuovo advertise");
                }
                 if (idsub!=0 && idprop2!=0){
                 
                 //collego la proprieta con la sottoscrizione offerta
                 String carp2="INSERT INTO `CleverResources`.`CaratteristicheSottoscrizione` (`SottoscrizioneOff`, `ProprietaSottoscrizione`) VALUES ('"+idsub+"', '"+idprop2+"');";
                 db.exUpdate(carp2);  
                 
                 
                 }
                 
                 
                 
                
                 
                 
                 
                 
                 
                 
                 
                 
                 
                 
                 
                 
                 
                 
                 
                                
                
                
                
            }  
                         
                
                
                
            }
            
            
            
            
            
        
        
        
        
        } catch (SQLException ex) {
            logger.error("SQLException"+ex);
        } catch (CleverException ex) {
            logger.error("CleverException"+ex);
        }
        
        
        
    }  
  
    public void addsub(String value){
        try {
            int idso=0;
            String idclient=value.split("@")[0];
            String soId=value.split("@")[1];
            db=Database.getNewInstance(parameterContainer);
            String qidsoff="SELECT ID FROM SottoscrizioneOfferta WHERE SoId='"+soId+"'";
            ResultSet rsoi = db.exQuery(qidsoff);
            if(rsoi.next()){
               idso=rsoi.getInt(1);   
              }
              
            String subreq="<?xml version=\"1.0\" encoding=\"UTF-8\"?><Subscribe><SubscriptionOfferingId>"+soId+"</SubscriptionOfferingId></Subscribe>";  
            String response = subscribe(subreq);
            org.jdom.Document docsub = this.stringToDom(response);
            Element subelem = docsub.detachRootElement();
            String subId=subelem.getAttributeValue("SubscriptionID");
            String subexpT=subelem.getAttributeValue("expires");
            String subexp=subexpT.replace("T"," ");  
            Element child = subelem.getChild("XMPPResponse");
            Element child1 = child.getChild("XMPPURI");
            String muc = child1.getValue();
            logger.debug("?=)*% response: \n"+response+" MUC: "+muc);
            String insSub="INSERT INTO `CleverResources`.`Sottoscrivi` (`ID`, `SubId`, `Sottoscrivente`, `SottoscrizioneOff`, `Scadenza`, `TimeStamp`, `Descrizione`) VALUES (NULL, '"+subId+"', '"+idclient+"', '"+idso+"', '"+subexp+"', CURRENT_TIMESTAMP, '"+muc+"' );";  
            db.exUpdate(insSub); 
              
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(SASAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    
    
    
    
}
