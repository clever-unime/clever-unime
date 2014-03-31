/*
 * The MIT License
 *
 * Copyright 2012 alessiodipietro.
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
package org.clever.HostManager.SOS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.clever.HostManager.SOS.SOSModuleCore.GetObservation;
import org.clever.HostManager.SOS.SOSModuleCore.GetCapabilities;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
//import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
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
import org.clever.HostManager.SOS.SOSModuleCore.DescribeSensor;
import org.clever.HostManager.SOS.SOSModuleCore.ExpirationAdvertiseRequest;
import org.clever.HostManager.SOS.SOSModuleTransactional.SOSmodule;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;


/**
 *
 * @author alessiodipietro
 */
public class SOSAgent extends Agent{
    private FileStreamer fs = new FileStreamer();
    private XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String cfgPath="./cfg/configuration_sosagent.xml";
    private boolean readerstarted=false;
//    public SOSAgent() throws CleverException{
//    logger = Logger.getLogger(agentName);
//    logger.info("SOSAgent() avviato!!!!");
//    SOSAgent prova=new SOSAgent("SOSAgent");
//    }
       
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/SOS/log_conf/";
    private String pathDirOut="/LOGS/HostManager/SOS";
    //########
    
    
    public SOSAgent(/*String agentName*/) throws CleverException {
        
        //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("SOSAgentHm");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
        
        try {
            super.setAgentName("SOSAgent");
            
            
            
            //logger = Logger.getLogger(this.getAgentName());
            //sposto i log su un altro file
            //logger = Logger.getLogger("debugLogger");
            
            
            logger.info("SOSAgent(agentName) avviato!!!!");
            ParameterContainer parameterContainer=ParameterContainer.getInstance();
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream( "/org/clever/Common/Shared/logger.properties" );
            prop.load( in );
            PropertyConfigurator.configure( prop );
            File cfgFile = new File( cfgPath );
            InputStream inxml=null;
            if( !cfgFile.exists() )
            {
                inxml = getClass().getResourceAsStream( "/org/clever/HostManager/SOS/configuration_sosagent.xml" );
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
            
            
            mc = new ModuleCommunicator(this.getAgentName(),"HM");
            mc.setMethodInvokerHandler(this);
            logger.debug("setMethodInvokerHandler done!");
            //set configuration file
            parameterContainer.setConfigurationFile(pXML.getRootElement().getChildText("sosConfigurationFile"));
            
            
            
            //set db parameter
            Element dbParams=pXML.getRootElement().getChild("dbParams");
            parameterContainer.setDbDriver(dbParams.getChildText("driver"));
            parameterContainer.setDbServer(dbParams.getChildText("server"));
            parameterContainer.setDbPassword(dbParams.getChildText("password"));
            parameterContainer.setDbUsername(dbParams.getChildText("username"));
            parameterContainer.setDbName(dbParams.getChildText("name"));
            
            
            Element testDbParams=pXML.getRootElement().getChild("test").getChild("dbParams");
            parameterContainer.setTestDbDriver(testDbParams.getChildText("driver"));
            parameterContainer.setTestDbServer(testDbParams.getChildText("server"));
            parameterContainer.setTestDbPassword(testDbParams.getChildText("password"));
            parameterContainer.setTestDbUsername(testDbParams.getChildText("username"));
            parameterContainer.setTestDbName(testDbParams.getChildText("name"));
            
            
            
            parameterContainer.setLogger(logger);
            
            parameterContainer.setSosAgent(this);
            
            startReader();
//            //invio una notifica di presenza al WebAgent
//            String password="francesco";
//            Notification presence = new Notification();
//            presence.setAgentId(this.getAgentName());
//            presence.setId("Web/RegisterClient");
//            presence.setBody(password);
//            this.sendNotification(presence);
            
            
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
    
    public String sendSASAdvertisement(String advertisementRequest){
        String advertisementResponse="";
        try {
            ArrayList params=new ArrayList();
            params.add(advertisementRequest);
            advertisementResponse=(String)this.invoke("SASAgentHm", "advertise", true, params);
            logger.info(" sendSASAdvertise response=\n"+advertisementResponse);
        
        } catch (CleverException ex) {
            logger.error("Error sending advertisement to SASAgent");
        }
        return advertisementResponse;

    }
    
    public void startReader(){
        try{
            if(!this.readerstarted)
            {
                SOSmodule sos=new SOSmodule();
                sos.init(); 
                this.readerstarted=true;
            }
        }
        catch(Exception e){
            logger.error("It's occurred some problem in SOSmodule initialization! ");
            this.readerstarted=false;
        }
    }
    
    public ArrayList<String> getAdvertisements() throws CleverException{
        logger.debug(" getAdvertisements inizio");
        ArrayList<String> advertisements = new ArrayList<String>();
        //build empty expirationAdvertiseRequest
        Element expirationAdvertiseRequest = new Element("ExpirationAdvertiseRequest");
        Element featureOfInterest = new Element("FeatureOfInterest");
        Element quantityProperty = new Element("QuantityProperty");
        Element content = new Element("Content");
        content.setAttribute("definition", "");
        content.setAttribute("uom", "");
        quantityProperty.addContent(content);
        Element alertMessageStructure = new Element("AlertMessageStructure");
        alertMessageStructure.addContent(quantityProperty);
        expirationAdvertiseRequest.addContent(featureOfInterest);
        expirationAdvertiseRequest.addContent(alertMessageStructure);

        Document expirationAdvertiseRequestDoc = new Document(expirationAdvertiseRequest);

        //get all advertisements
        advertisements = this.sosService(outputter.outputString(expirationAdvertiseRequestDoc));
        
        logger.debug(" getAdvertisements fine");
        return advertisements;
    }
    
    public ArrayList<String> sosService(String request) throws CleverException{
        ArrayList<String> response=new ArrayList<String>();
        org.clever.HostManager.SOS.SOSModuleCore.SOSmodule sos=new org.clever.HostManager.SOS.SOSModuleCore.SOSmodule();
        response=sos.SOSservice(request);
        return response;
    }
    
    public String describeSensor(String describeSensorRequest) throws CleverException{
        String describeSensorResponse="";
        try {
            DescribeSensor ds=new DescribeSensor(describeSensorRequest);
            ds.describe_db();
            describeSensorResponse=ds.write_descrsens_xml();
        } catch (TransformerConfigurationException ex) {
            logger.error("Transformer Configuration Exception: "+ex);
            throw new CleverException(ex, "Transformer Configuration Exception");
        } catch (TransformerException ex) {
            logger.error("Transformer Exception: "+ex);
            throw new CleverException(ex, "Transformer Exception");
        } catch (SQLException ex) {
            logger.error("SQL Exception: "+ex);
            throw new CleverException(ex, "SQL Exception");
        } catch (ParserConfigurationException ex) {
            logger.error("Parser Configuration Exception: "+ex);
            throw new CleverException(ex, "Parser Configuration Exception ");
        } catch (SAXException ex) {
            logger.error("SAX Exception: "+ex);
            throw new CleverException(ex, "SAX Exception ");
        } catch (IOException ex) {
            logger.error("IOException: "+ex);
            throw new CleverException(ex, "IOException ");
        }
        return describeSensorResponse;
    }
    
    public String getObservation(String getObservationRequest) throws CleverException{
        String getObservationResponse="";
        try {
            
            
            GetObservation gobs= new GetObservation(getObservationRequest);
            gobs.printInfo();
            
            gobs.getObsDb();
            logger.debug("gobsfine");
            getObservationResponse=gobs.write_getobs_xml();
            logger.debug("SOSAgent trasmette:");
            logger.debug(getObservationResponse);
        } catch (TransformerConfigurationException ex) {
            logger.error("Transformer Configuration Exception: "+ex);
            throw new CleverException(ex, "Transformer Configuration Exception");
        } catch (TransformerException ex) {
            logger.error("Transformer Exception: "+ex);
            throw new CleverException(ex, "Transformer Exception");
        } catch (SQLException ex) {
            logger.error("SQL Exception: "+ex);
            throw new CleverException(ex, "SQL Exception");
        } catch (ParserConfigurationException ex) {
            logger.error("Parser Configuration Exception: "+ex);
            throw new CleverException(ex, "Parser Configuration Exception ");
        } catch (SAXException ex) {
            logger.error("SAX Exception: "+ex);
            throw new CleverException(ex, "SAX Exception ");
        } catch (IOException ex) {
            logger.error("IOException: "+ex);
            throw new CleverException(ex, "IOException ");
        }
        
        return getObservationResponse;
    }
    
    public String getCapabilities(String getCapabilitiesRequest) throws CleverException{
        String getCapabilitiesResponse="";
        try {           
            GetCapabilities gc=new GetCapabilities(getCapabilitiesRequest);
            getCapabilitiesResponse=gc.write_capabilities_xml(); 
        } catch (TransformerConfigurationException ex) {
            logger.error("Transformer Configuration Exception: "+ex);
            throw new CleverException(ex, "Transformer Configuration Exception");
        } catch (TransformerException ex) {
            logger.error("Transformer Exception: "+ex);
            throw new CleverException(ex, "Transformer Exception");        
        } catch (SQLException ex) {
            logger.error("SQL Exception: "+ex);
            throw new CleverException(ex, "SQL Exception");
        } catch (ParserConfigurationException ex) {
            logger.error("Parser Configuration Exception: "+ex);
            throw new CleverException(ex, "Parser Configuration Exception ");
        } catch (SAXException ex) {
            logger.error("SAX Exception: "+ex);
            throw new CleverException(ex, "SAX Exception ");
        } catch (IOException ex) {
            logger.error("IOException: "+ex);
            throw new CleverException(ex, "IOException ");
        }
        return getCapabilitiesResponse;
    }
    
    public String expirationAdvertiseRequest(String expirationAdvertiseRequest) throws CleverException{
        ArrayList<String> expirationAdvertiseResponseList=null;
        String expirationAdvertisement="";
        try {
            ExpirationAdvertiseRequest ea= new ExpirationAdvertiseRequest(expirationAdvertiseRequest);
            expirationAdvertiseResponseList=ea.sendAdvertise();
            expirationAdvertisement=expirationAdvertiseResponseList.get(0);
            
        } catch (SQLException ex) {
            logger.error("SQL Exception: "+ex);
            throw new CleverException(ex, "SQL Exception");
        } catch (TransformerConfigurationException ex) {
            logger.error("Transformer Configuration Exception: "+ex);
            throw new CleverException(ex, "Transformer Configuration Exception");
        } catch (TransformerException ex) {
            logger.error("Transformer Exception: "+ex);
            throw new CleverException(ex, "Transformer Exception");
        } catch (ParserConfigurationException ex) {
            logger.error("Parser Configuration Exception: "+ex);
            throw new CleverException(ex, "Parser Configuration Exception ");
        } catch (SAXException ex) {
            logger.error("SAX Exception: "+ex);
            throw new CleverException(ex, "SAX Exception ");
        } catch (IOException ex) {
            logger.error("IOException: "+ex);
            throw new CleverException(ex, "IOException ");
        } catch (NullPointerException ex){  
        }
        return expirationAdvertisement;
    }
    
    public void registerRequest(String request){
     //send the request 
//        logger.info("Sending notification...");
//        Notification reg=new Notification();
//        reg.setId("Web/RegisterSensor");
//        reg.setBody(request);
//        this.sendNotification(reg);
//        
        
        
    }
    
    @Override
    public Class getPluginClass() {
        return SOSAgent.class;
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
