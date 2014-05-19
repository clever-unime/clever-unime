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
package org.clever.HostManager.SOS.SOSModuleTransactional.Readers;

import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Sensor_Phenomena;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.ReaderInterface;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Sensor_Struct;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Sensor_Component;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.clever.HostManager.SOS.Database;
import org.clever.HostManager.SOS.ParameterContainer;
import org.clever.HostManager.SOS.SOSModuleTransactional.RegisterSensorXml;
import org.clever.HostManager.SOS.SOSModuleTransactional.SOSmodule;
import org.safehaus.uuid.UUIDGenerator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author alessiodipietro
 */
public class TestReader implements ReaderInterface {

    private ArrayList<Sensor_Struct> sensors;
    private SOSmodule sosModule;
    private Element params;
    private ParameterContainer parameterContainer;
    private Logger logger;
    private NodeList sensorsList;
    private Double lambda;
    private Integer sensorsNumber;
    private int observationPoolSize;
    private UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
    private Database testDatabase;


    public TestReader() {
        sensors = new ArrayList<Sensor_Struct>();
        parameterContainer = ParameterContainer.getInstance();
        this.logger = parameterContainer.getLogger();
        testDatabase=Database.getTestInstance(parameterContainer.getTestDbServer(),parameterContainer.getTestDbDriver(),
                                              parameterContainer.getTestDbName(),
                                              parameterContainer.getTestDbUsername(),parameterContainer.getTestDbPassword());
    }

    @Override
    public void init(Node Params, SOSmodule sosModule) {
        
            try{
            Thread.sleep(13000);
            }catch(InterruptedException e){
                logger.error(e.getMessage());
            }
        logger.debug("Initializing reader");
        this.sosModule = sosModule;
        params = (Element) Params;


        sensorsList = ((Element)(params.getElementsByTagName("sensors").item(0))).getElementsByTagName("sensor");
        lambda=Double.parseDouble(((Element)(params.getElementsByTagName("lambda").item(0))).getTextContent());
        this.sensorsNumber=Integer.parseInt(((Element)(params.getElementsByTagName("sensorsNumber").item(0))).getTextContent());
        
        this.observationPoolSize=Integer.parseInt(((Element)(params.getElementsByTagName("observationPoolSize").item(0))).getTextContent());

        RegisterInfo();
        
    }
    
    @Override
    public void RegisterInfo() {
        //logger.debug("Registering sensors info");
        
        
        
        //DataBase database=new DataBase();
        //database.openDB(this.parameterContainer.getDbServer(),this.parameterContainer.getDbDriver(),this.parameterContainer.getDbName(),
        //         this.parameterContainer.getDbUsername(),this.parameterContainer.getDbPassword());
//        logger.debug("DB PARAMETRI= "+parameterContainer.getTestDbServer()+","+parameterContainer.getTestDbDriver()+","+
//                                              parameterContainer.getTestDbName()+","+
//                                              parameterContainer.getTestDbUsername()+","+parameterContainer.getTestDbPassword());
        testDatabase.exUpdate("DELETE FROM `test_reader_observation`");
     // logger.debug("DELETE FROM `test_reader_observation` done!!!");
        Object monitor=new Object();
        //build Sensors_Struct array
        for (int i = 0; i < sensorsNumber; i++) {
            Sensor_Struct sensorStruct = new Sensor_Struct();
            //SensorInfo
            Element sensorInfoElement = (Element) ((Element) sensorsList.item(0)).getElementsByTagName("sensorInfo").item(0);
            //sensorStruct.getSensor_info().setid(sensorInfoElement.getElementsByTagName("id").item(0).getTextContent());
            sensorStruct.getSensor_info().setid(Integer.toString(i));
            sensorStruct.getSensor_info().settype_id(sensorInfoElement.getElementsByTagName("typeId").item(0).getTextContent());
            sensorStruct.getSensor_info().setproduct_description(sensorInfoElement.getElementsByTagName("description").item(0).getTextContent());
            sensorStruct.getSensor_info().setmanufacturer(sensorInfoElement.getElementsByTagName("manufacturer").item(0).getTextContent());
            sensorStruct.getSensor_info().setmodel(sensorInfoElement.getElementsByTagName("model").item(0).getTextContent());
            sensorStruct.getSensor_info().setoperator_area(sensorInfoElement.getElementsByTagName("operatorArea").item(0).getTextContent());
            sensorStruct.getSensor_info().setclass_application(sensorInfoElement.getElementsByTagName("classApplication").item(0).getTextContent());
            sensorStruct.getSensor_info().setmeasures_interval(sensorInfoElement.getElementsByTagName("measuresInterval").item(0).getTextContent());
            sensorStruct.getSensor_info().setmeasures_interval_uom(sensorInfoElement.getElementsByTagName("measuresIntervalUom").item(0).getTextContent());
            sensorStruct.getSensor_info().setalt_val(sensorInfoElement.getElementsByTagName("altitude").item(0).getTextContent());
            sensorStruct.getSensor_info().setalt_uom(sensorInfoElement.getElementsByTagName("altitudeUom").item(0).getTextContent());
            sensorStruct.getSensor_info().setlat_val(sensorInfoElement.getElementsByTagName("latitude").item(0).getTextContent());
            sensorStruct.getSensor_info().setlat_uom(sensorInfoElement.getElementsByTagName("latitudeUom").item(0).getTextContent());
            sensorStruct.getSensor_info().setlong_val(Integer.toString(Integer.parseInt(sensorInfoElement.getElementsByTagName("longitude").item(0).getTextContent())+i));
            sensorStruct.getSensor_info().setlong_uom(sensorInfoElement.getElementsByTagName("longitudeUom").item(0).getTextContent());
            sensorStruct.getSensor_info().setref(sensorInfoElement.getElementsByTagName("ref").item(0).getTextContent());
            sensorStruct.getSensor_info().setactive(sensorInfoElement.getElementsByTagName("active").item(0).getTextContent());
            sensorStruct.getSensor_info().setmobile(sensorInfoElement.getElementsByTagName("mobile").item(0).getTextContent());
            sensorStruct.getSensor_info().setpacket(sensorInfoElement.getElementsByTagName("packet").item(0).getTextContent());
            try {
                InetAddress localmachine = InetAddress.getLocalHost();
                String hostname = localmachine.getHostName();
                for (int k = 0; k < this.observationPoolSize; k++) {
                    //logger.debug("query="+"INSERT INTO `test_reader_observation` (`sensor_id`,`hostname`)"
                       //     + "VALUES ('" + Integer.toString(i) + "','" + hostname + "')");
                    
                    testDatabase.exUpdate("INSERT INTO `test_reader_observation` (`sensor_id`,`hostname`)"
                            + "VALUES ('" + Integer.toString(i) + "','" + hostname + "')");
                    
                }
            } catch (UnknownHostException ex) {
                logger.error("Error inserting observation pool");
            }
            //Sensor Components
            NodeList sensorComponentsList = ((Element) ((Element) sensorsList.item(0)).getElementsByTagName("sensorComponents").item(0)).getElementsByTagName("sensorComponent");
            Vector<Sensor_Component> sensorComponentVector = sensorStruct.getSensor_Component();
            for (int j = 0; j < sensorComponentsList.getLength(); j++) {
                Element sensorComponentElement = (Element) sensorComponentsList.item(j);
                Sensor_Component sensorComponent = new Sensor_Component();
                sensorComponent.setcomp_id(sensorComponentElement.getElementsByTagName("id").item(0).getTextContent()+Integer.toString(i));
                sensorComponent.setcomp_descr(sensorComponentElement.getElementsByTagName("description").item(0).getTextContent());
                sensorComponent.setcomp_phenomena(sensorComponentElement.getElementsByTagName("phenomena").item(0).getTextContent());
                sensorComponent.setcomp_status(sensorComponentElement.getElementsByTagName("status").item(0).getTextContent());
                sensorComponentVector.add(sensorComponent);
            }

            //Sensor Phenomena
            NodeList sensorPhenomenaList = ((Element) ((Element) sensorsList.item(0)).getElementsByTagName("sensorPhenomena").item(0)).getElementsByTagName("sensorPhenomenon");
            Vector<Sensor_Phenomena> sensorPhenomenaVector = sensorStruct.getSensor_Phenomena();
            for (int j = 0; j < sensorPhenomenaList.getLength(); j++) {
                Element sensorPhenomenaElement = (Element) sensorPhenomenaList.item(j);
                Sensor_Phenomena sensorPhenomena = new Sensor_Phenomena();
                sensorPhenomena.setphen_id(sensorPhenomenaElement.getElementsByTagName("id").item(0).getTextContent());
                sensorPhenomena.setphen_descr(sensorPhenomenaElement.getElementsByTagName("description").item(0).getTextContent());
                sensorPhenomena.setphen_uom(sensorPhenomenaElement.getElementsByTagName("uom").item(0).getTextContent());
                sensorPhenomena.setphen_uom_id(sensorPhenomenaElement.getElementsByTagName("uomId").item(0).getTextContent());
                sensorPhenomena.setoffering_id(sensorPhenomenaElement.getElementsByTagName("offeringId").item(0).getTextContent());
                sensorPhenomenaVector.add(sensorPhenomena);
            }
            this.sensors.add(sensorStruct);
            RegisterSensorXml registerSensorXml;

            try {
                registerSensorXml = new RegisterSensorXml(sensorStruct, i, sosModule);
                registerSensorXml.write_descrsens_xml();
                
                //start testreader thread
               // logger.debug("Avvio thread TestReader");
                TestReaderThread testReaderThread=new TestReaderThread(monitor,sensorStruct,this,lambda,sosModule);
                testReaderThread.start();
            } catch (ParserConfigurationException ex) {
                logger.error("ParserConfigurationException: " + ex);
            } catch (SAXException ex) {
                logger.error("SAXException: " + ex);
            } catch (IOException ex) {
                logger.error("IOException: " + ex);
            } catch (TransformerConfigurationException ex) {
                logger.error("TransformerConfigurationException: " + ex);
            } catch (TransformerException ex) {
                logger.error("TransformerException: " + ex);
            } catch (SQLException ex) {
                logger.error("SQLException: " + ex);
            } catch (ParseException ex) {
                logger.error("ParseException: " + ex);
            }
            
            
        }
        
        
        //database.closeDB();
        synchronized(monitor){
            monitor.notifyAll();
        }
        
        logger.debug("Sensor info registered successfully!");

    }
     @Override
    public void parseIncomingLine(String a,String line,String t, Object cmd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void parseIncomingLine(String line, String cmd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
