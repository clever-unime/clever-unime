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

package org.clever.HostManager.SOS.SOSModuleTransactional.Readers;

/**
 *
 * @author Giuseppe Tricomi
 */
import org.clever.HostManager.SOS.SOSModuleTransactional.utils;
import java.io.IOException;
import java.util.Enumeration;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.clever.HostManager.SOS.ParameterContainer;
import org.clever.HostManager.SOS.SOSModuleTransactional.InsertObservationXml;
import org.clever.HostManager.SOS.SOSModuleTransactional.RegisterSensorXml;
import org.clever.HostManager.SOS.SOSModuleTransactional.SOSmodule;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Utils.ConnectorDB;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Utils.TimeStampComp;
import org.clever.Common.XMLTools.ParserXML;




public class DBMeridionaleReader implements ReaderInterface{
    private SOSmodule sosModule;
    private int Port;
    private String Hostname;
    private String DBName;
    
    private int resettime;
    private Logger logger=Logger.getLogger("dbMeridionaleReader");//Logger.getLogger("debugLogger");
    //private ParameterContainer parameterContainer;
    private boolean notread=true;
    //private boolean debug=true;
    //private boolean init=true;
    Vector<Sensor_Struct> sens_nodeid;
    private NodeList sensorsList;
    private Integer sensorsNumber;
    private String times;
    //private ArrayList<Sensor_Struct> sensors;
    Element params;
    
    public void init(Node Params,SOSmodule sosModule){
        //istanziazione del logger che consente di scrivere i log in un file separato
        logger.info("db meridionale Start init");
        this.sosModule=sosModule;
        parseElement(Params);
        this.sens_nodeid = new Vector<Sensor_Struct>(1);
        this.params=(Element)Params;
        sensorsList = ((Element)(params.getElementsByTagName("sensors").item(0))).getElementsByTagName("sensor");
        // initialization algoritm:
        //inizializzare la classe di collegamento con il Db della Meridionale impianti sviluppata da Davide
        Thread readInput = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO: ripulire questa funzione e risistemare alcune parti della funzione che possono essere fatte in maniera più performante, si attende per questa operazione che meridionale impianti cominci a inserire i dati nella propria struttura
                ///////////////////////////////////////////////////////////////
                ConnectorDB dbMerid=new ConnectorDB("93.63.205.216","simone","simone","S1mone","Data E Ora","ID impianto");
                ///////////////////////////////////////////////////////////////
                Vector<String> line,line2;
                //String times="2013-04-26 15:34:00";
                logger.info("thread reader Start init");
                String impianto=dbMerid.getSensorBoard("ID impianto","impianti").elementAt(0);
                try {
                //fase di conoscenza della rete - acquisizione dei nodi
                  boolean ciclotest=true;
                  TimeStampComp tsc=null;
                  while (ciclotest) {
                      logger.debug("Sensors readerStart procedure");
                      
                      dbMerid.createRSMap(times, "misure_energie", impianto);
                      dbMerid.createRSMap(times, "misure_meteo", impianto);
                
                      if (notread) {
                          RegisterInfo();
                          logger.debug("Sensors Struct created");
                          notread = false;
                      }
                      //**********************************************************************
                      line=dbMerid.getMisure("ID","misure_energie");
                      line2=dbMerid.getMisure("ID","misure_meteo");
                      logger.debug("element in line "+line.size());
                      logger.debug("element in line2 "+line2.size());
                      //**********************************************************************
                      //logger.debug("info " + line + " t: " + new GregorianCalendar().getTimeInMillis());
                      //logger.debug("read entry from db is ended");
                      String lasttime="";
                      String lasttime2="";
                      java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                      Date now= new Date();
                      
                      if (!line.isEmpty()) {
                         Enumeration e=line.elements();
                         while (e.hasMoreElements()){
                             String entry=(String)e.nextElement();
                             String timeobs=(String)dbMerid.getMisure("Data e Ora",entry,"misure_energie","ID");
                             if(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeobs.substring(0,timeobs.indexOf('.'))).compareTo(sdf.parse(sdf.format(now)))>0){
                                 continue;}
                             else{
                                String sens=dbMerid.getMisure("ID Nodo",entry,"misure_energie","ID");
                                parseIncomingLine(entry,sens,"misure_energie",dbMerid);
                                lasttime=timeobs;
                             }
                         }
                         
                      }
                      if (!line2.isEmpty()) {
                         Enumeration e=line2.elements();
                         
                         while (e.hasMoreElements()){
                             String entry=(String)e.nextElement();
                             String timeobs=(String)dbMerid.getMisure("Data e Ora",entry,"misure_meteo","ID");
                             if(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeobs.substring(0,timeobs.indexOf('.'))).compareTo(sdf.parse(sdf.format(now)))>0){
                                 continue;}
                             else{
                                String sens=dbMerid.getMisure("ID Nodo",entry,"misure_meteo","ID");
                                parseIncomingLine(entry,sens,"misure_meteo",dbMerid);
                                lasttime2=timeobs;
                             }
                           }
                      }
                      try{
                        if((lasttime!=null)&&(lasttime2!=null))
                        {
                            tsc=new TimeStampComp(lasttime,lasttime2);
                            if(tsc.compare()<0)
                                lasttime=lasttime2;
                            tsc=new TimeStampComp(lasttime,times);
                            if(tsc.compare()>0)
                            {
                                times=lasttime;
                                logger.debug("Sto modificando l'ultimo tempo acquisito con: \""+times+"\"");
                                setLastAcquireTime(times);
                            }
                         }
                      }
                      catch(Exception e){
                        logger.error("errore occurred in compare timestamp procedure!!");
                          }
                      
                          
                          
                      
                      //ciclotest=false;
                      Thread.sleep(resettime);
                    }
                  
                } 
                catch (Exception ex){
                    logger.debug("Error in acquisition phase from Database:"+ex.getMessage()+" "+ex.getStackTrace()[0].toString());
                }
                
            }
        }, "read input stream thread");
        readInput.start();
        
    }
   
    public void RegisterInfo(){
        //permette il reperimento delle informazioni necessarie per costruire una richiesta
        //RegisterSensor, andando a riempire i campi dell'oggetto Sensor_Struct
        try{
        for (int i = 0; i < sensorsNumber; i++) {
            logger.debug("start registerInfo.sensorinfo");
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
            //logger.debug("start registerInfo.sensorcomponent");
            //Sensor Components
            NodeList sensorComponentsList = ((Element) ((Element) sensorsList.item(0)).getElementsByTagName("sensorComponents").item(0)).getElementsByTagName("sensorComponent");
            Vector<Sensor_Component> sensorComponentVector = sensorStruct.getSensor_Component();
            for (int j = 0; j < sensorComponentsList.getLength(); j++) {
                Element sensorComponentElement = (Element) sensorComponentsList.item(j);
                Sensor_Component sensorComponent = new Sensor_Component();
                sensorComponent.setcomp_id(sensorComponentElement.getElementsByTagName("id").item(0).getTextContent());
                sensorComponent.setcomp_descr(sensorComponentElement.getElementsByTagName("description").item(0).getTextContent());
                sensorComponent.setcomp_phenomena(sensorComponentElement.getElementsByTagName("phenomena").item(0).getTextContent());
                sensorComponent.setcomp_status(sensorComponentElement.getElementsByTagName("status").item(0).getTextContent());
                sensorComponentVector.add(sensorComponent);
            }
            //logger.debug("start registerInfo.sensorphenomena");
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
            
            //logger.debug("start registerInfo.register struct");
            RegisterSensorXml registerSensorXml;
            try {
                registerSensorXml = new RegisterSensorXml(sensorStruct, i, sosModule);
                registerSensorXml.write_descrsens_xml();
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
            }catch (Exception ex)
            {
                logger.error("err generico"+ex.getMessage());
            }
            this.sens_nodeid.add(sensorStruct);
        }
         }
        catch(Exception eix){logger.error("errore generico"+eix.getMessage());}
    }
    
    
    
    
    public void parseIncomingLine(String identry,String idsens,String tab,Object dbMer){
        //utilizzato per il reperimento delle informazioni necessarie per costruire la richiesta InsertObservation
        ConnectorDB dbMerid= null;
        try{
        dbMerid=(ConnectorDB)dbMer;
        }
        catch(Exception e){logger.error(e.getMessage());}
        if (idsens == null) {
            logger.error("Parsing null line");
            return;
        }
        String components="";
        for (int i = 0; i < sens_nodeid.size(); i++) {
            for(int j=0;j<sens_nodeid.elementAt(i).getSensor_Component().size();j++){
                //logger.debug("idsens:"+idsens+" other:"+sens_nodeid.elementAt(i).getSensor_Component().get(j).getcomp_id().trim()+" descript: "+sens_nodeid.elementAt(i).getSensor_Component().get(j).getcomp_descr().trim());
                if (sens_nodeid.elementAt(i).getSensor_Component().get(j).getcomp_id().trim().equals(idsens)) {//sens_nodeid.elementAt(i).getSensor_info().getid().trim().equals(idsens)) {
                    for (int k = 0; k < sens_nodeid.elementAt(i).getSensor_Component().get(j).getcomp_phenomena().split("_").length; k++) {
                        components=dbMerid.getMisure(new phenomenaMapping().parseMapping(sens_nodeid.elementAt(i).getSensor_Component().get(j).getcomp_phenomena().split("_")[k]),identry,tab,"ID");
                        InsertObservationXml ios = new InsertObservationXml(sens_nodeid.elementAt(i), idsens, sosModule);
                        //funzione che prende il timestamp dal db lo elabora e lo formatta secondo la seguente formattazione "yyyy-MM-dd'T'hh:mm:ss'Z'"
                        String time="";
                        //logger.debug("inizia il parsing incoming 5"+sens_nodeid.elementAt(i)+"&&"+idsens);
                        
                        time=dbMerid.getMisure("Data e Ora",identry,tab,"ID");
                        time=time.substring(0, time.indexOf('.'));
                        //logger.debug("inizia il parsing incoming 6time:"+time);
                        
                        time=time.split(" ")[0]+"T"+time.split(" ")[1]+"Z";
                        //logger.debug("inizia il parsing incoming 7 time:"+time);
                        //logger.debug("parseincomingLine identry:"+identry+" idsens:"+idsens+"components:"+sens_nodeid.elementAt(i).getSensor_info().getpacket().split("_")[k]+"=="+sens_nodeid.elementAt(i).getSensor_Component().get(j).getcomp_phenomena().split("_")[k]+" "+components);
                        ios.writexml(components, sens_nodeid.elementAt(i).getSensor_Component().get(j).getcomp_phenomena().split("_")[k],time);
                        //logger.debug("inizia il parsing incoming 8");
                    }
                }
            }
        }
        //logger.debug("fine del parsing incoming");
    }
   
    private void parseElement(Node currentNode) {
        short sNodeType = currentNode.getNodeType();
         //Se è di tipo Element ricavo le informazioni e le stampo
         if (sNodeType == Node.ELEMENT_NODE) {
             String sNodeValue = utils.searchTextInElement(currentNode);
             //per ogni componente
             if (currentNode.getNodeName().equals("port")) {
                 this.Port = Integer.parseInt(utils.searchTextInElement(currentNode).trim());
                 currentNode = currentNode.getNextSibling();
             }
             if (currentNode.getNodeName().equals("hostname")) {
                 this.Hostname = utils.searchTextInElement(currentNode).trim();
                 currentNode = currentNode.getNextSibling();
             }
             if (currentNode.getNodeName().equals("DBName")) {
                 this.DBName = utils.searchTextInElement(currentNode).trim();
                 currentNode = currentNode.getNextSibling();
             }
             if (currentNode.getNodeName().equals("resettimeMillisec")) {
                 this.resettime = Integer.parseInt(utils.searchTextInElement(currentNode).trim());
                 currentNode = currentNode.getNextSibling();
             }
             if (currentNode.getNodeName().equals("sensorNumber")) {
                 this.sensorsNumber = Integer.parseInt(utils.searchTextInElement(currentNode).trim());
                 currentNode = currentNode.getNextSibling();
             }
             if (currentNode.getNodeName().equals("firstvalue")) {
                 this.times = utils.searchTextInElement(currentNode).trim();
                 currentNode = currentNode.getNextSibling();
             }
         }
         int iChildNumber = currentNode.getChildNodes().getLength();
         //Se non si tratta di una foglia continua l'esplorazione
         if (currentNode.hasChildNodes()) {
             NodeList nlChilds = currentNode.getChildNodes();
             for (int iChild = 0; iChild < iChildNumber; iChild++) {
                 parseElement(nlChilds.item(iChild));
             }
         }
    }
    private void setLastAcquireTime(String time){
        //logger.debug("modifica partita");
        ParserXML p=new ParserXML(new java.io.File("/home/giusimone/NetBeansProjects/clever-unime-manera/trunk/clevercloud/sources/org/clever/HostManager/SOS/SOSModuleTransactional/configuration.xml"));
        org.jdom2.Document doc=p.getDocument();
        org.jdom2.Element d=doc.getRootElement().getChild("readers").getChild("reader").getChild("pluginParams").getChild("firstvalue");
        d.setText(time.toString());   
        //logger.debug("modifica eseguita");
        p.saveXML("/home/giusimone/NetBeansProjects/clever-unime-manera/trunk/clevercloud/sources/org/clever/HostManager/SOS/SOSModuleTransactional/configuration.xml");
        
        
    }
    
    
   /*
   
    public void Discovery_nodes() {
        Vector<String> line ;
        String cmd;
        //fase di conoscenza della rete - acquisizione dei nodi considero l' impianto come una sensor board
        String temp = "";
        if (debug) {
            System.out.println("netcmd sendernodeid, t: " + new GregorianCalendar().getTimeInMillis());
        }
        try{
            line =null;//chiamata al db che mi resituisce gli impianti
            if (line != null) {
                for(int i=0;i<line.size();i++) {
                    if (debug) {
                        logger.debug(" " + line);
                    }
                    temp = (String)line.get(i);
                    if (debug) {
                        logger.debug("node " + temp);
                    }
                    int flag_node = 0;
                    for (int j = 0; j < sens_nodeid.size(); j++) {
                        if (sens_nodeid.elementAt(j).getSensor_info().getid().indexOf(temp) != -1) {
                            flag_node = 1;
                            if (debug) {
                                logger.debug("node control " + temp);
                            }
                        }
                    }
                    if (flag_node == 0) {
                        Sensor_Struct tempsens = new Sensor_Struct();
                        tempsens.getSensor_info().setid(temp);
                        if (debug ) {
                            System.out.println("node write" + temp);
                        }
                        sens_nodeid.add(tempsens);
                    }
                }
            }
        }
        catch(Exception e){}
        //verifica nodi acquisiti
        for (int i = 0; i < sens_nodeid.size(); i++) {
            if (debug ) {
                logger.debug("node " + sens_nodeid.elementAt(i).getSensor_info().getid());
            }
        }
        if (!init) {
            //check("netcmd senderinfo " + new GregorianCalendar().getTimeInMillis());
            //check("netcmd senderposition " + new GregorianCalendar().getTimeInMillis());
            //check("netcmd sendercapab " + new GregorianCalendar().getTimeInMillis());
            //check("netcmd sendercomp " + new GregorianCalendar().getTimeInMillis());
            //check("netcmd senderphen " + new GregorianCalendar().getTimeInMillis());
            //check("netcmd senderclass " + new GregorianCalendar().getTimeInMillis());
            for (int i = 0; i < sens_nodeid.size(); i++) {
                try {
                    RegisterSensorXml rsx = new RegisterSensorXml(sens_nodeid.elementAt(i), i, sosModule);
                    rsx.write_descrsens_xml();
                } catch (SQLException ex) {
                    logger.debug(ex.getSQLState()+" "+ex.getErrorCode());
                } catch (ParseException ex) {
                    logger.debug(ex.getErrorOffset()+ex.getMessage());
                } catch (ParserConfigurationException ex) {
                    logger.debug(ex.getMessage());
                } catch (TransformerConfigurationException ex) {
                    logger.debug(ex.getMessage());
                } catch (TransformerException ex) {
                    logger.debug(ex.getMessageAndLocation());
                } catch (SAXException ex) {
                    logger.debug(ex.getMessage()+ex.getCause());
                } catch (IOException ex) {
                    logger.debug(ex.getMessage());
                }
            }
        }
    }*/
     @Override
    public void parseIncomingLine(String line, String cmd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
class phenomenaMapping{
    enum phenomena {windDirection,windSpeed,temperature,humidity,UV,solarRadiation,rain,
    pressure,batteryVoltage,supplyVoltage,temperaturePT100,Voltagephase1,Voltagephase2,
    Voltagephase3,Currentphase1,Currentphase2,Currentphase3,powerFactor,frequency,activePower,
    reactivePower,activeEnergy,reactiveEnergy};
   
    String parsePhenomena(phenomena p){
        String parse="";
        switch (p){
            case windDirection:{parse="Direzione vento";break;}
            case windSpeed:{parse="Velocita vento";break;}
            case temperature:{parse="Temperatura da sensori";break;}
            case humidity:{parse="Umidita";break;}
            case UV:{parse="UV";break;}
            case solarRadiation:{parse="Radiazione Solare";break;}
            case rain:{parse="Pioggia";break;}
            case pressure:{parse="Pressione";break;}
            case batteryVoltage:{parse="Tensione Batteria";break;}
            case supplyVoltage:{parse="Tensione Alimentazione";break;}
            case temperaturePT100:{parse="Temperatura PT100";break;}
            case Voltagephase1:{parse="V1FN";break;}
            case Voltagephase2:{parse="V2FN";break;}
            case Voltagephase3:{parse="V3FN";break;}
            case Currentphase1:{parse="I1FN";break;}
            case Currentphase2:{parse="I2FN";break;}
            case Currentphase3:{parse="I3FN";break;}
            case powerFactor:{parse="Fattore Di Potenza";break;}
            case frequency:{parse="Frequenza";break;}
            case activePower:{parse="Potenza Attiva";break;}
            case reactivePower:{parse="Potenza Reattiva";break;}
            case activeEnergy:{parse="Energia Attiva";break;}
            case reactiveEnergy:{parse="Energia Reattiva";break;}
        }
        return parse;
    }
    String parseMapping(String representation){
        String parse="";
        if(representation.isEmpty())
            return null;
        else if(representation.equals("Currentphase1"))
            parse="I1FN";
        else if(representation.equals("Currentphase2"))
            parse="I2FN";
        else if(representation.equals("Currentphase3"))
            parse="I3FN";
        else if(representation.equals("Voltagephase1"))
            parse="V1FN";
        else if(representation.equals("Voltagephase2"))
            parse="V2FN";
        else if(representation.equals("Voltagephase3"))
            parse="V3FN";
        else if(representation.equals("Wind Direction"))
            parse="Direzione vento";
        else if(representation.equals("Wind Speed"))
            parse="Velocita vento";
        else if(representation.equals("Temperature"))
            parse="Temperatura da sensori";
        else if(representation.equals("atmospheric humidity"))
            parse="Umidita";
        else if(representation.equals("UltraViolet"))
            parse="UV";
        else if(representation.equals("Solar Radiation"))
            parse="Radiazione Solare";
        else if(representation.equals("Rain"))
            parse="Pioggia";
        else if(representation.equals("Pressure"))
            parse="Pressione";
        else if(representation.equals("Battery Voltage"))
            parse="Tensione Batteria";
        else if(representation.equals("Supply Voltage"))
            parse="Tensione Alimentazione";
        else if(representation.equals("TemperaturePT100"))
            parse="Temperatura PT100";
        else if(representation.equals("powerFactor"))
            parse="Fattore Di Potenza";
        else if(representation.equals("Frequency"))
            parse="Frequenza";
        else if(representation.equals("Active Power"))
            parse="Potenza Attiva";
        else if(representation.equals("Reactive Power"))
            parse="Potenza Reattiva";
        else if(representation.equals("Active Energy"))
            parse="Energia Attiva";
        else if(representation.equals("Reactive Energy"))
            parse="Energia Reattiva";
        return parse;
    }
}
