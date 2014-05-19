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
 *  Copyright user
 *  Copyright (c) 2013 Giuseppe Tricomi
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package org.clever.HostManager.SOS.SOSModuleTransactional;

import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.ReaderInterface;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;

import java.util.HashMap;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Shared.Support;
import org.clever.HostManager.SOS.ParameterContainer;
import org.clever.HostManager.SOS.SOSAgent;
import org.xml.sax.SAXException;

/**
 * @author Giuseppe Tricomi
 * @author user
 */
public class SOSmodule implements SOSforReaderInterface {

    private Logger logger;
    private String service;
    private ReaderInterface usb_reader,test_reader;
    private HashMap mapReaders;
    private ParameterContainer parameterContainer;
    
    public SOSmodule() {
        parameterContainer = ParameterContainer.getInstance();
        logger = parameterContainer.getLogger();
    }

    public void init() {
        try {
            this.service = "";
            logger.debug("SOSmodule init");
            String filename = parameterContainer.getConfigurationFile();
            logger.debug("filepath:"+filename);
            this.mapReaders = new HashMap(1);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            File f=new File(filename);
            InputStream inxml=null;
            if(!f.exists()){
                logger.debug("file non trovato");
                inxml = getClass().getResourceAsStream( "/org/clever/HostManager/SOS/SOSModuleTransactional/configuration.xml" );
                try
                {
                    Support.copy( inxml, f );
                }
                catch( IOException ex )
                {
                    logger.error( "Copy file failed" + ex );
                    System.exit( 1 );
                }
            }
            else{
                inxml = new FileInputStream(filename);
            }
            //File xmlFilereg = new File(filename);
            Document document = builder.parse(inxml);
            parseConfiguration(document);
            
        } catch (SAXException ex) {
            logger.error("SOSModuleTransactional: SAXException: " + ex);
        } catch (IOException ex) {
            logger.error("SOSModuleTransactional: IOException: " + ex);
        } catch (ParserConfigurationException ex) {
            logger.error("SOSModuleTransactional: ParserConfigurationException: " + ex);
        }
    }

    public void SOSservice(String request) {
        try {
            SOSserviceSelection(request);
        } catch (ParserConfigurationException ex) {
            logger.error("SOSModuleTransactional: ParserConfigurationException: " + ex);
        } catch (SAXException ex) {
            logger.error("SOSModuleTransactional: SAXException: " + ex);
        } catch (IOException ex) {
            logger.error("SOSModuleTransactional: IOException: " + ex);
        } catch (TransformerConfigurationException ex) {
            logger.error("SOSModuleTransactional: TransformerConfigurationException: " + ex);
        } catch (TransformerException ex) {
            logger.error("SOSModuleTransactional: TransformerException: " + ex);
        } catch (SQLException ex) {
            logger.error("SOSModuleTransactional: SQLException: " + ex);
        } catch (ParseException ex) {
            logger.error("SOSModuleTransactional: ParseException: " + ex);
        }
    }
    /*public void SOSservice(String filename_input,String filename_output) {
    try {
    SOSserviceSelection(filename_input, filename_output);
    } catch (ParserConfigurationException ex) {
    logger.error("ParserConfigurationException: "+ex);
    } catch (SAXException ex) {
    logger.error("SAXException: "+ex);
    } catch (IOException ex) {
    logger.error("IOException: "+ex);            
    } catch (TransformerConfigurationException ex) {
    logger.error("TransformerConfigurationException: "+ex);
    } catch (TransformerException ex) {
    logger.error("TransformerException: "+ex);
    } catch (SQLException ex) {
    logger.error("SQLException: "+ex);
    } catch (ParseException ex) {
    logger.error("ParseException: "+ex);            
    }
    }*/

    public void SOSserviceSelection(String request) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException, SQLException, ParseException {

        logger.debug("SOSserviceSelection");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        //File xmlFilereg = new File(filename_input);
        Document documentreg = builder.parse(new ByteArrayInputStream(request.getBytes()));
        //  controllo il nodo radice del file di input, in cui è decritto il tipo di operazione invocata
        int iChildNumber = documentreg.getChildNodes().getLength();
        //Se non si tratta di una foglia continua l'esplorazione
        if (documentreg.hasChildNodes()) {
            NodeList nlChilds = documentreg.getChildNodes();
            for (int iChild = 0; iChild < iChildNumber; iChild++) {
                this.service = (nlChilds.item(iChild).getNodeName());
            }
        }
        //controllo il servizio e alloco gli oggetti corrispondenti all'operazione desiderata

        if (this.service.contains("RegisterSensor")) {
            logger.debug("Sosmod-regsens");
            SOSAgent sosAgent = this.parameterContainer.getSosAgent();
            sosAgent.registerRequest(request);
            RegisterSensor rg = new RegisterSensor(request);
            rg.insertSensor();
            rg.write_register_xml();

        } else if (this.service.contains("InsertObservation")) {
            logger.info("InsertObservation"+request);
            InsertObservation obs = new InsertObservation(request);
            obs.insertObsdb();
        } else {
            logger.debug("function is not know");
        }
    }

        public void parseConfiguration(Node currentNode) {
        short sNodeType = currentNode.getNodeType();
        
        //Se è di tipo Element ricavo le informazioni e le stampo
        if (sNodeType == Node.ELEMENT_NODE) {
            String sNodeName = currentNode.getNodeName();
            String sNodeValue = utils.searchTextInElement(currentNode);
            //per ogni componente 
          //  logger.info("SOS NodeName="+sNodeName);
            if (sNodeName.equals("reader")) {
                parseReader(currentNode);
            }
        }
        int iChildNumber = currentNode.getChildNodes().getLength();
        //Se non si tratta di una foglia continua l'esplorazione
        if (currentNode.hasChildNodes()) {
            NodeList nlChilds = currentNode.getChildNodes();
            for (int iChild = 0; iChild < iChildNumber; iChild++) {
                parseConfiguration(nlChilds.item(iChild));
            }
        }
    }

    public void parseReader(Node currentNode) {
        short sNodeType = currentNode.getNodeType();
       // logger.info("parseReader sNodetype="+sNodeType+" and Node.Elem="+Node.ELEMENT_NODE);
        if (sNodeType == Node.ELEMENT_NODE) {
            String sNodeName = currentNode.getNodeName();
            String sNodeValue = utils.searchTextInElement(currentNode);
          //  logger.info("sNodeName="+sNodeName+" and sNodeValue="+sNodeValue);
            if (sNodeName.equals("className")) {
                //if(sNodeValue.contains("Usb_reader")){
                try {
                    logger.info("Reader="+sNodeValue);
                    Class cl = Class.forName(sNodeValue);

                    usb_reader = (ReaderInterface) cl.newInstance();
                    while (true) {
                        currentNode = currentNode.getNextSibling();
                        if (currentNode.getNodeName().equals("moduleName")) {
                            mapReaders.put(usb_reader, sNodeValue);
                           // logger.info("mapReaders put done!");
                        }
                        if (currentNode.getNodeName().equals("pluginParams")) {
                            usb_reader.init(currentNode, this);
                          //  logger.info("init reader done!");
                            break;
                        }
                    }
                } catch (InstantiationException ex) {
                    logger.error("SOSModuleTransactional: InstantiationException: " + ex);
                } catch (IllegalAccessException ex) {
                    logger.error("SOSModuleTransactional: IllegalAccessException: " + ex);
                } catch (ClassNotFoundException ex) {
                    logger.error("SOSModuleTransactional: ClassNotFoundException: " + ex);
                }
//                }else if(sNodeValue.contains("Test_Reader")){
//                    logger.info("SOSmodule Test_reader ->"+sNodeValue);
//                    try {
//                    Class cl = Class.forName(sNodeValue);
//
//                    test_reader = (ReaderInterface) cl.newInstance();
//                    while (true) {
//                        currentNode = currentNode.getNextSibling();
//                        if (currentNode.getNodeName().equals("moduleName")) {
//                            mapReaders.put(test_reader, sNodeValue);
//                        }
//                        if (currentNode.getNodeName().equals("pluginParams")) {
//                            test_reader.init(currentNode, this);
//                            break;
//                        }
//                    }
//                } catch (InstantiationException ex) {
//                    logger.error("SOSModuleTransactional: InstantiationException: " + ex);
//                } catch (IllegalAccessException ex) {
//                    logger.error("SOSModuleTransactional: IllegalAccessException: " + ex);
//                } catch (ClassNotFoundException ex) {
//                    logger.error("SOSModuleTransactional: ClassNotFoundException: " + ex);
//                }
                    
                    
                //}
            }
        }
        int iChildNumber = currentNode.getChildNodes().getLength();
        //Se non si tratta di una foglia continua l'esplorazione
        if (currentNode.hasChildNodes()) {
            NodeList nlChilds = currentNode.getChildNodes();
            for (int iChild = 0; iChild < iChildNumber; iChild++) {
                parseReader(nlChilds.item(iChild));
            }
        }

    }
}