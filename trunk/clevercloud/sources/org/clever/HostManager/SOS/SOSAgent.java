/*
 * Copyright [2014] [Università di Messina]
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
            
    public SOSAgent(/*String agentName*/) throws CleverException {
        super();
        super.setAgentName("SOSAgent");
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
         super.setAgentName("SOSAgent");
         super.start();
         ParserXML pXML = this.getconfiguration(this.cfgPath,"/org/clever/HostManager/SOS/configuration_sosagent.xml");
         org.clever.HostManager.SOS.ParameterContainer parameterContainer=org.clever.HostManager.SOS.ParameterContainer.getInstance();    
            
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
    }

    @Override
    public void shutDown() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
