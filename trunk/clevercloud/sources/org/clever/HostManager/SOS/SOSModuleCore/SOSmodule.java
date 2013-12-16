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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author user
 */
public class SOSmodule implements SOSinterface {

    private String service;

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
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    @Override
    public void SOSservice(String filename_input, String filename_output) {
        try {
            SOSserviceSelection(filename_input);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(SOSmodule.class.getName()).log(Level.SEVERE, null, ex);
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
                System.out.println("funzione non riconosciuta");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return response;
        //  controllo il nodo radice del file di input, in cui è decritto il tipo di operazione invocata

    }
}