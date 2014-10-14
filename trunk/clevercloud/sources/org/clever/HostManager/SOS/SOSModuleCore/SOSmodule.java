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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleCore;

import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.apache.log4j.Logger;
/**
 *
 * @author user
 */
public class SOSmodule implements SOSinterface {

    private String service;
    private Logger logger=Logger.getLogger("SOSAGENT-SOSmodule");
    @Override
    public void init() {
        this.service = "";
    }

    @Override
    public ArrayList<String> SOSservice(String request) {
        ArrayList<String> response = new ArrayList();
        try {
            response = SOSserviceSelection(request);
        } catch (ParserConfigurationException ex) {
            logger.error("ParserConfigurationException",ex);
        } catch (SAXException ex) {
           logger.error("SAXException",ex);
        } catch (IOException ex) {
            logger.error("IOException",ex);
        } catch (TransformerConfigurationException ex) {
            logger.error("TransformerConfigurationException",ex);
        } catch (TransformerException ex) {
            logger.error("TransformerException",ex);
        } catch (SQLException ex) {
            logger.error("SQLException",ex);
        } catch (ParseException ex) {
            logger.error("ParseException",ex);
        }
        return response;
    }

    @Override
    public void SOSservice(String filename_input, String filename_output) {
        try {
            SOSserviceSelection(filename_input);
        } catch (ParserConfigurationException ex) {
             logger.error("ParserConfigurationException",ex);
        } catch (SAXException ex) {
           logger.error("SAXException",ex);
        } catch (IOException ex) {
            logger.error("IOException",ex);
        } catch (TransformerConfigurationException ex) {
            logger.error("TransformerConfigurationException",ex);
        } catch (TransformerException ex) {
            logger.error("TransformerException",ex);
        } catch (SQLException ex) {
            logger.error("SQLException",ex);
        } catch (ParseException ex) {
            logger.error("ParseException",ex);
        }
    }

    public ArrayList<String> SOSserviceSelection(String request) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException, SQLException, ParseException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        //File xmlFilereg = new File(filename_input);
        ArrayList<String> response = new ArrayList();
        try {
            Document documentreg = builder.parse(new ByteArrayInputStream(request.getBytes()));
            int iChildNumber = documentreg.getChildNodes().getLength();
            //Se non si tratta di una foglia continua l'esplorazione
            if (documentreg.hasChildNodes()) {
                NodeList nlChilds = documentreg.getChildNodes();
                for (int iChild = 0; iChild < iChildNumber; iChild++) {
                    this.service = (nlChilds.item(iChild).getNodeName());
                }
            }
            //controllo il servizio e alloco gli oggetti corrispondenti all'operazione desiderata
            if (this.service.contains("DescribeSensor")) {

                DescribeSensor ds = new DescribeSensor(request);
                ds.describe_db();
                //  ds.print_info();
                response.add(ds.write_descrsens_xml());
            }/*
            else if(this.service.contains("RegisterSensor")){
            RegisterSensor rg = new RegisterSensor(filename_input,filename_output);
            rg.insertSensor();
            rg.write_register_xml();
            
            }*/ else if (this.service.contains("GetObservation")) {
                GetObservation gobs = new GetObservation(request);
                //gobs.printInfo();
                gobs.getObsDb();
                response.add(gobs.write_getobs_xml());
            }/*
            else if(this.service.contains("InsertObservation")){
            InsertObservation obs= new InsertObservation(filename_input,filename_output);
            obs.insertObsdb();
            }*/ else if (this.service.contains("GetCapabilities")) {
                GetCapabilities gc = new GetCapabilities(request);
                response.add(gc.write_capabilities_xml());
            } else if (this.service.contains("ExpirationAdvertiseRequest")) {
                ExpirationAdvertiseRequest ea = new ExpirationAdvertiseRequest(request);
                response = ea.sendAdvertise();
            } else {
                logger.debug("funzione non riconosciuta");
            }
        } catch (Exception e) {
           logger.error("funzione non riconosciuta",e);
        }
        return response;
        //  controllo il nodo radice del file di input, in cui è decritto il tipo di operazione invocata

    }
}