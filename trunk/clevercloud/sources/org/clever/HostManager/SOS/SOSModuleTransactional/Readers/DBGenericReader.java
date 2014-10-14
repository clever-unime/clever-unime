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
 * Copyright (c) 2013 Universita' degli studi di Messina
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



import org.clever.HostManager.SOS.SOSModuleTransactional.utils;
import java.io.IOException;
import java.util.Enumeration;
import java.sql.SQLException;
import java.text.ParseException;
//import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import org.apache.log4j.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.clever.HostManager.SOS.ParameterContainer;
import org.clever.HostManager.SOS.SOSModuleTransactional.InsertObservationXml;
import org.clever.HostManager.SOS.SOSModuleTransactional.RegisterSensorXml;
import org.clever.HostManager.SOS.SOSModuleTransactional.SOSmodule;
//import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Utils.ConnectorDB;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Utils.IndexStruct;
import com.google.common.collect.Maps;
import com.google.common.collect.HashMultimap;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Utils.TimeStampComp;
/**
 * 
 * This class is the generic reader class used to acquire observation from db. 
 * @author Giuseppe Tricomi 
 */
public class DBGenericReader implements ReaderInterface{
    private SOSmodule sosModule;
    private int Port;
    private int resettime;
    private Logger logger;
    //private ParameterContainer parameterContainer;
    private String Hostname;
    private String DBName;
    private String user;
    private String pass;
    private boolean read=true;
    private boolean debug=true;
    private boolean init=true;
    Vector<Sensor_Struct> sens_nodeid;
    //private NodeList sensorsList;
    private Integer sensorsNumber;
    private String times;
    private String times2;
    //private ArrayList<Sensor_Struct> sensors;
    private HashMap<String,IndexStruct> structure_index;
    private HashMultimap<String,HashMap> sensor_registered_table;
    
    ConnectorDB db;
    
    
    public void init(Node Params,SOSmodule sosModule){
        
        this.logger = Logger.getLogger("DBGenericReader");
        logger.info("Reader from DB Start init");
        
        this.sosModule=sosModule;
        parseElement(Params);
        
        this.sens_nodeid = new Vector<Sensor_Struct>();
        
        this.structure_index = Maps.newHashMap();
        this.sensor_registered_table=HashMultimap.create();
        
        /////////////////////////////////////////////////////////////////
        //FOR TESTING WE WILL USE A LOCALHOST TEST DB
        db=new ConnectorDB(this.Hostname,this.DBName,this.user,this.pass,"timestamp_osservazione","sensore_anagrafica_idsensore_anagrafica");
        /////////////////////////////////////////////////////////////////
        Thread readInput = new Thread(new Runnable() {
        public void run() {
            
            TimeStampComp tsc=null;
            Vector<String> line;
              
            logger.debug("Sensors Start procedure "+db.toString());
            try {
                //fase di conoscenza della rete - acquisizione dei nodi
                boolean iterate=true;
                int exceptionObtained=0;
                int numberOFexception=25;
                times2=times;
                while (iterate) 
                {
                    try
                    {
                        Thread.sleep(resettime);
                        if (read) {
                            RegisterInfo();
                            logger.info("Sensors Struct created");
                            read = false;
                        }
                        else
                        {
                           // DBGenericReader.this.Discovery_nodes();
                        }
                        //**********************************************************************
                        //START ACQUISITION
                        //logger.debug("Read observation started");
                        db.remove_EL_RSMap("osservazioni");
                        db.createRSMap(times, "osservazioni", null);
                        line=db.getMisure("idosservazioni","osservazioni");
                        
                        //**********************************************************************
                        if (!line.isEmpty()) 
                        {
                             
                             Enumeration e=line.elements();
                             while (e.hasMoreElements()){
                                 logger.debug("ciclo lettura osservazioni passo 1");
                                 String entry=(String)e.nextElement();
                                 logger.debug("ciclo lettura osservazioni passo 2. valore id osservazioni:"+entry);
                                 String sens=db.getMisure("sensore_anagrafica_idsensore_anagrafica",entry,"osservazioni","idosservazioni");
                                 logger.debug("ciclo lettura osservazioni passo 3.collegamento con il sensore che ha realizzato osservazione:"+sens);
                                 parseIncomingLine(entry,sens,"osservazioni",null);
                             }
                        }
                    }
                    catch(Exception e)
                    {
                        exceptionObtained++;
                        
                        logger.error("an exception is occourred in retrieving phase of sensor acquisition.",e);
                        logger.error(e.getMessage()+"||"+e.getLocalizedMessage(),e);
                        if(exceptionObtained>numberOFexception){
                            iterate=false;
                            logger.info("iterate stop!");
                            logger.error("iterate stop!An exception is occourred for more than "+numberOFexception);
                        }
                    }
                      logger.debug("inserimento osservazioni line terminata");
                      
                      tsc=new TimeStampComp(times,times2);
                      int comp=tsc.compare();
                      //logger.debug("tsc " +comp);
                      if(comp<0){
                        setLastAcquireTime(times2);
                        times=times2;
                      }
                    }
                } 
                catch (Exception ex){
                    logger.error(""+ ex);
                }
            }
        }, "read input stream thread");
        readInput.start();
    }
    
    /**
     * This function is used in inizialization procedure to memorize the Sensor information on SOS DB.
     */
    public void RegisterInfo(){
        //permette il reperimento delle informazioni necessarie per costruire una richiesta
        //RegisterSensor, andando a riempire i campi dell'oggetto Sensor_Struct
        try
        {
            logger.info("Register info start");
            Sensor_Struct sensorStruct;
            this.db.createRSMap(null, "sensore_anagrafica", null);
            Vector<String> sVector=this.db.getMisure("idsensore_anagrafica", "sensore_anagrafica");
            if (!sVector.isEmpty()) 
            {
                int sensorBoardindex=0;
                Enumeration eSens=sVector.elements();
                while (eSens.hasMoreElements()) 
                {
                    sensorStruct=new Sensor_Struct();
                    String eSensid=(String)eSens.nextElement();
                    //logger.debug("eSensid"+eSensid);
                    this.register_newSensorBoard(sensorStruct, eSensid,sensorBoardindex);
                    
                    sensorBoardindex++;
                }
            }
       }
       catch(Exception eix){
           logger.error(eix.getMessage(), eix);
       }
    }
    
   
    //TODO: Make test
    /**
     * This function is used to parse the observation retrieved from the db of the host.
     * The information parsed is putted in SOS db.
     * @param identry String: it represents the observation id that parse 
     * @param idsens String: it represents the Sensor board id that parse
     * @param tab String: it is the table name where are stored the observation 
     */
    public void parseIncomingLine(String identry,String idsens,String tab,Object ob){
        //utilizzato per il reperimento delle informazioni necessarie per costruire la richiesta InsertObservation
        if (idsens == null) {
            logger.error("Parsing null line");
            return;
        }
       
        int sensboardIndex= -1;
        try {
            //logger.debug("cparse incoming passo 1:sensboardIndex"+sensboardIndex+" size"+sens_nodeid.size());
            for (int i = 0; i < sens_nodeid.size(); i++) {
                //logger.debug("cparse incoming passo 2:sensboardIndex"+sensboardIndex+" i"+i+" id"+sens_nodeid.elementAt(i).getSensor_info().getid());
                if (sens_nodeid.elementAt(i).getSensor_info().getid().equals(idsens)) {
                    sensboardIndex = i;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("errore causato da :" + e.getMessage(), e);
        }
        //logger.debug("cparse incoming passo 3:sensboardIndex"+sensboardIndex+" identry"+identry+" tab"+tab);
        String value="";
        value=this.db.getMisure("valore_osservato", identry, tab,"idosservazioni");
        String time="";
        //logger.debug("cparse incoming passo 4:value"+value);
        time=db.getMisure("timestamp_osservazione",identry,tab,"idosservazioni");
        //logger.debug("cparse incoming passo 5:timestamp"+time);
        String time2;
        time2=time.split(" ")[0]+"T"+time.split(" ")[1]+"Z";
        //logger.debug("cparse incoming passo 6:time"+time2);
        //logger.debug("(/)Sens_nodeid"+sens_nodeid.size());
        //misura_anagrafica_idtipomisura
        InsertObservationXml ios = new InsertObservationXml(sens_nodeid.elementAt(sensboardIndex), idsens, sosModule);
        //logger.debug("cparse incoming passo 7");
        //////controllare da qui
        String idMisAnag=this.db.getMisure("misura_anagrafica_idmisura_anagrafica", identry, "osservazioni","idosservazioni");
        //logger.debug("cparse incoming passo 7a. id found"+idMisAnag);
        ios.writexml(value,this.db.getMisure("tipo_misura",idMisAnag , "misura_anagrafica","idmisura_anagrafica") ,time2);
        //logger.debug("cparse incoming passo 8 "+ time+"    "+this.times);
        this.times2=time;
    }
   
    
    // TODO: testare questa funzionalità per verificarne l'effettivo funzionamento e vedere se corretto
    /**
     * This function is used to add in SOS DB new element(Sensor Board, Component or Phenomena) insert into Host DB.
     */
    public void Discovery_nodes() 
    {
        this.db.createRSMap(null, "sensore_anagrafica", null);
        Vector<String> sVector=this.db.getMisure("idsensore_anagrafica", "sensore_anagrafica");
        if (!sVector.isEmpty()) 
        {
            Enumeration eSens=sVector.elements();
            while (eSens.hasMoreElements()) 
            {
                String idsens_toregister=(String)eSens.nextElement();
                if(!this.sensor_registered_table.containsKey(idsens_toregister))
                {
                    Sensor_Struct sensorStruct=null;
                    this.register_newSensorBoard(sensorStruct, idsens_toregister,this.sens_nodeid.size());
                    this.sens_nodeid.add(sensorStruct);
                }
                else
                {
                    this.db.createRSMap(null, "misura_anagrafica", idsens_toregister);
                    Vector<String> sComponent=this.db.getMisure("idmisura_anagrafica", "misura_anagrafica");
                    Enumeration eComp=sComponent.elements();
                    if (!sComponent.isEmpty()) 
                    {
                        String idcomp_toregister="";
                        while (eComp.hasMoreElements()) 
                        {
                            idcomp_toregister=(String)eComp.nextElement();
                            if(!((HashMap)this.sensor_registered_table.get(idsens_toregister)).containsKey(idcomp_toregister))
                            {
                                int sBoardInd=((IndexStruct)((HashMap)this.sensor_registered_table.get(idsens_toregister)).get(idcomp_toregister)).getSensorStructIndex();
                                this.register_sensorComponent(idsens_toregister, idcomp_toregister, null,sBoardInd,this.sens_nodeid.get(((IndexStruct)((HashMap)this.sensor_registered_table.get(idsens_toregister)).get(idcomp_toregister)).getSensorStructIndex()).getSensor_Component().size());
                            }
                        }
                        Vector<String> sPhenom=this.db.getMisure("idmisura_anagrafica", "misura_anagrafica");
                        Enumeration ePhenom=sPhenom.elements();
                        while (ePhenom.hasMoreElements()) 
                        {
                            String idPhen_toregister=(String)ePhenom.nextElement();
                            boolean find=false;
                            for(Sensor_Phenomena sensorPhenomena : this.sens_nodeid.get(((IndexStruct)((HashMap)this.sensor_registered_table.get(idsens_toregister)).get(idcomp_toregister)).getSensorStructIndex()).getSensor_Phenomena())
                                if(!sensorPhenomena.getphen_id().equals(idPhen_toregister))
                                    continue;
                                else{
                                    find=true;
                                    break;
                                }
                            if(!find)        
                                this.register_sensorPhenomena(idPhen_toregister,this.sens_nodeid.get(((IndexStruct)((HashMap)this.sensor_registered_table.get(idsens_toregister)).get(idcomp_toregister)).getSensorStructIndex()).getSensor_Phenomena());
                        }
                    }
                }
            }
        }    
        for (int i = 0; i < sens_nodeid.size(); i++) 
        {
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
    
    //TODO: testare che funzionino correttamente
    /**
     * This function is used to register in sensorStruct the sensor info element retrieved from DB.
     * @param idsens_toregister
     * @param sensorStruct 
     */
    private Sensor_Struct register_sensorInfo(String idsens_toregister,Sensor_Struct sensorStruct){
        //SensorInfo
        sensorStruct.getSensor_info().setid(idsens_toregister);
        sensorStruct.getSensor_info().settype_id("urn:ogc:object:feature:"+this.db.getMisure("id_tipo",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));            
        sensorStruct.getSensor_info().setproduct_description(this.db.getMisure("descrizione",idsens_toregister,"sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setmanufacturer(this.db.getMisure("costruttore",idsens_toregister,"sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setmodel(this.db.getMisure("modello",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setoperator_area(this.db.getMisure("operatorArea",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setclass_application("_");
        sensorStruct.getSensor_info().setmeasures_interval(this.db.getMisure("intervallo_misura",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setmeasures_interval_uom(this.db.getMisure("intervallo_misura_uom",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setalt_val(this.db.getMisure("altitudine",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setalt_uom(this.db.getMisure("altitudine_uom",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setlat_val(this.db.getMisure("latitudine",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setlat_uom(this.db.getMisure("latitudine_uom",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setlong_val(this.db.getMisure("longitudine",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setlong_uom(this.db.getMisure("longitudine_uom",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setref("urn:ogc:def:crs:EPSG:4326");
        sensorStruct.getSensor_info().setpacket(this.db.getMisure("packet",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setactive(this.db.getMisure("active",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        sensorStruct.getSensor_info().setmobile(this.db.getMisure("mobile",idsens_toregister, "sensore_anagrafica","idsensore_anagrafica"));
        logger.info("end registerInfo.sensorinfo");
        return sensorStruct;
        //Identifyng sensors ended
    }
    /**
     * This function is used to register in sensorStruct the component element retrieved from DB.
     * @param idsens_toregister
     * @param idcomponent
     * @param sensorComponentVector 
     */
    private Vector<Sensor_Component> register_sensorComponent(String idsens_toregister,String idcomponent,Vector<Sensor_Component> sensorComponentVector,int sensorBoardindex,int componentindex){
        Sensor_Component sensorComponent = new Sensor_Component();
        sensorComponent.setcomp_id(idcomponent);
        sensorComponent.setcomp_descr(idsens_toregister+this.db.getMisure("tipo_misura",idcomponent,"misura_anagrafica","idmisura_anagrafica"));
        sensorComponent.setcomp_phenomena(this.db.getMisure("tipo_misura",idcomponent,"misura_anagrafica","idmisura_anagrafica"));
        sensorComponent.setcomp_status(this.db.getMisure("status",idcomponent,"misura_anagrafica","idmisura_anagrafica"));
        sensorComponentVector.add(sensorComponent);
        
        this.structure_index.put(idcomponent, new IndexStruct(sensorBoardindex,componentindex));
        this.sensor_registered_table.put(idsens_toregister, structure_index);
        return sensorComponentVector;
    }
    /**
     * This function is used to register in sensorStruct the phenomena element retrieved from DB.
     * @param idPhen_toregister
     * @param sensorPhenomenaVector 
     */
    private Vector<Sensor_Phenomena> register_sensorPhenomena(String idPhen_toregister,Vector<Sensor_Phenomena> sensorPhenomenaVector){
        Sensor_Phenomena sensorPhenomena = new Sensor_Phenomena();
        String tipo_misura=this.db.getMisure("tipo_misura",idPhen_toregister,"misura_anagrafica","idmisura_anagrafica");
        
        sensorPhenomena.setphen_id(this.db.getMisure("idmisura_anagrafica",idPhen_toregister,"misura_anagrafica","idmisura_anagrafica")+":"+tipo_misura);
        sensorPhenomena.setphen_descr(tipo_misura);
        sensorPhenomena.setphen_uom(this.db.getMisure("tipo_misura_uom",idPhen_toregister,"misura_anagrafica","idmisura_anagrafica"));
        sensorPhenomena.setphen_uom_id(this.db.getMisure("id_uom_tipomisura",idPhen_toregister,"misura_anagrafica","idmisura_anagrafica"));
        sensorPhenomena.setoffering_id(tipo_misura);
        sensorPhenomenaVector.add(sensorPhenomena);
        return sensorPhenomenaVector;
    }
    /**
     * This function is invoked when a new sensor board must be registered on SOS DB
     * @param sensorStruct
     * @param eSens 
     */
    private void register_newSensorBoard(Sensor_Struct sensorStruct,String eSensid,int sensorBoardindex)
    {
        //sensorStruct = new Sensor_Struct();
        String idsens_toregister=eSensid;
        logger.debug("\n\n\n\n\n\nStart registerInfo.sensorinfo"+eSensid);
        sensorStruct=this.register_sensorInfo(idsens_toregister, sensorStruct);
        //Sensor Components
        Vector<Sensor_Component> sensorComponentVector = sensorStruct.getSensor_Component();
        db.remove_EL_RSMap("misura_anagrafica");
        this.db.createRSMap(null, "misura_anagrafica", idsens_toregister);
        Vector<String> sComponent=this.db.getMisure("idmisura_anagrafica", "misura_anagrafica");
        if (!sComponent.isEmpty()) 
        {
            int componentindex=0;
            Enumeration eComp=sComponent.elements();
            while (eComp.hasMoreElements()) 
            {
                String idcomp_toregister=(String)eComp.nextElement();
                //Element sensorComponentElement = (Element) sensorComponentsList.item(j);
                String idcomponent=this.db.getMisure("idmisura_anagrafica",idcomp_toregister,"misura_anagrafica","idmisura_anagrafica");
                sensorComponentVector=this.register_sensorComponent(idsens_toregister,idcomponent,sensorComponentVector,sensorBoardindex,componentindex);
                componentindex++;
            }
        }
        else
        {
            logger.error("No one component entry taken from DB for sensor "+idsens_toregister);
            this.structure_index.put("", new IndexStruct(sensorBoardindex,-1));
            this.sensor_registered_table.put(idsens_toregister, structure_index);
        
        }
        logger.debug("start registerInfo.sensorphenomena");
        Vector<Sensor_Phenomena> sensorPhenomenaVector = sensorStruct.getSensor_Phenomena();
        //TODO come ottimizzazione si potrebbe usare lo stesso vettore dei componenti
        Vector<String> sPhenom=this.db.getMisure("idmisura_anagrafica", "misura_anagrafica");
        if (!sPhenom.isEmpty()) 
        {
            Enumeration ePhenom=sPhenom.elements();
            while (ePhenom.hasMoreElements()) 
            {
                String idPhen_toregister=(String)ePhenom.nextElement();
                sensorPhenomenaVector=this.register_sensorPhenomena(idPhen_toregister,sensorPhenomenaVector);
            }
        }
        else
        {
            logger.error("No one phenomena entry taken from DBfor sensor "+idsens_toregister);
        }

                    //this.sensors.add(sensorStruct);
        logger.debug("start registerInfo.register struct");
        RegisterSensorXml registerSensorXml;
        try {
            registerSensorXml = new RegisterSensorXml(sensorStruct,Integer.parseInt(idsens_toregister), sosModule);
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
        }
        this.sens_nodeid.add(sensorStruct);
    }        
 
    /**
     * This function is used to parse XML element taken from configuration file.
     * @param currentNode 
     */
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
                 //logger.debug("Hostname: "+this.Hostname);
                 currentNode = currentNode.getNextSibling();
             }
             if (currentNode.getNodeName().equals("DBName")) {
                 this.DBName = utils.searchTextInElement(currentNode).trim();
                 //logger.debug("DBName: "+this.DBName);
                 currentNode = currentNode.getNextSibling();
             }
             if (currentNode.getNodeName().equals("UserName")) {
                 this.user = utils.searchTextInElement(currentNode).trim();
                 //logger.debug("user: "+this.user);
                 currentNode = currentNode.getNextSibling();
             }
             if (currentNode.getNodeName().equals("Password")) {
                 this.pass = utils.searchTextInElement(currentNode).trim();
                 //logger.debug("pass: "+this.pass);
                 currentNode = currentNode.getNextSibling();
             }
             if (currentNode.getNodeName().equals("resettimeMillisec")) {
                 //logger.debug("sNodeValue: " + utils.searchTextInElement(currentNode));
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
    
    @Override
    public void parseIncomingLine(String line, String cmd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /* 
    public void printSensorStruct(){
        for(int i=0;i<sens_nodeid.size();i++){
            logger.debug("cparse incoming passo 2:sensboardIndex"+sensboardIndex+" i"+i+" id"+sens_nodeid.elementAt(i).getSensor_info().getid());
            if(sens_nodeid.elementAt(i).getSensor_info().getid()
    }*/
    
     private void setLastAcquireTime(String time){
        //logger.debug("modifica partita" + time);
        ParserXML p=new ParserXML(new java.io.File("./cfg/configuration_Readers.xml"));
        org.jdom2.Document doc=p.getDocument();
        org.jdom2.Element d=doc.getRootElement().getChild("readers").getChild("reader").getChild("pluginParams").getChild("firstvalue");
        d.setText(time.toString());   
        //logger.debug("modifica eseguita");
        p.saveXML("./cfg/configuration_Readers.xml");
        
        
    }
}

