/*
 * The MIT License
 *
 * Copyright 2012 Università di Messina.
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
package org.clever.HostManager.SOS.SOSModuleTransactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.clever.HostManager.SOS.Database;
import org.clever.HostManager.SOS.ParameterContainer;
import org.clever.HostManager.SOS.SOSAgent;
import org.xml.sax.SAXException;

/**
 *
 * @author user
 */
public class RegisterSensor {

    private RegisterDomCleanParser ddp;
    private Database db;
    private ParameterContainer parameterContainer = null;
    private Logger logger;
    /*
    RegisterSensor(String filename){
    try {
    ddp = new RegisterDomCleanParser();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = dbf.newDocumentBuilder();
    File xmlFilereg = new File(filename);
    Document documentreg = builder.parse(xmlFilereg);
    ddp.sensorNodeInfo(documentreg) ;
    db= new DataBase();        
    this.filename_output="/home/user/file_register_response.xml";
    
    
    } catch (SAXException ex) {
    Logger.getLogger(RegisterSensor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
    Logger.getLogger(RegisterSensor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ParserConfigurationException ex) {
    Logger.getLogger(RegisterSensor.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    
    }*/

    public RegisterSensor(String registerSensorRequest) {
        try {
            this.parameterContainer = ParameterContainer.getInstance();
            logger = this.parameterContainer.getLogger();
            logger.debug("RegisterSensor");
            logger.debug(registerSensorRequest);
            ddp = new RegisterDomCleanParser();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document documentreg = builder.parse(new ByteArrayInputStream(registerSensorRequest.getBytes()));
            ddp.sensorNodeInfo(documentreg);
            db = Database.getInstance();
            

        } catch (SAXException ex) {
            logger.error("RegisterSensor: SASException " + ex);
        } catch (IOException ex) {
            logger.error("RegisterSensor: IOException " + ex);
        } catch (ParserConfigurationException ex) {
            logger.error("RegisterSensor: ParserConfigurationException " + ex);
        } catch(Exception e){
            logger.error(e.getLocalizedMessage()+" "+e.toString()+" "+e.getMessage());
            
        }
        


    }

    void insertSensor() throws SQLException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
        SOSAgent sosAgent = this.parameterContainer.getSosAgent();
       String query_checksensorid = "SELECT `sensor_id` FROM `sensor` WHERE `unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "'";
       ResultSet rs = db.exQuery(query_checksensorid);
        if (rs.next() == false) {

            String querysensor = "INSERT INTO `sensorml`.`sensor` (`unique_id` ,`description_type` ,`status` ,`mobile` ,`srs` , `fixed`,`longitude` , `long_uom` , `latitude` , `lat_uom` , `altitude` , `alt_uom` , `coordinate`,`frequency`,`frequency_uom` ) VALUES ( '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "','" + ddp.registerNodeDomParser.sensorDescription.getDescription_type() + "','" + ddp.registerNodeDomParser.sensorDescription.getStatus() + "','" + ddp.registerNodeDomParser.sensorDescription.getMobile() + "','" + ddp.registerNodeDomParser.sensorDescription.getCrs() + "','" + ddp.registerNodeDomParser.sensorDescription.getFixed() + "','" + ddp.registerNodeDomParser.sensorDescription.getLongitude() + "','" + ddp.registerNodeDomParser.sensorDescription.getlong_uom() + "','" + ddp.registerNodeDomParser.sensorDescription.getLatitude() + "','" + ddp.registerNodeDomParser.sensorDescription.getlat_uom() + "','" + ddp.registerNodeDomParser.sensorDescription.getAltitude() + "','" + ddp.registerNodeDomParser.sensorDescription.getalt_uom() + "', GeomFromText('Point(" + ddp.registerNodeDomParser.sensorDescription.getLongitude() + " " + ddp.registerNodeDomParser.sensorDescription.getLatitude() + ")'), '" + ddp.registerNodeDomParser.sensorDescription.getFrequency() + "','" + ddp.registerNodeDomParser.sensorDescription.getFrequencyUom().split(";")[0] + "');";
            db.exUpdate(querysensor);
            for (int i = 0; i < ddp.registerNodeDomParser.phenomenonDescription.size(); i++) {
                int flag_phen = 0;
                String query_checkphenid = "SELECT `phenomenon_id` FROM `phenomenon` WHERE `unique_id` LIKE '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id().split(";")[0] + "'";
               rs = db.exQuery(query_checkphenid);
                //se il fenomeno non è già presente
                if (rs.next() == false) {
                    String queryphenomena = "INSERT INTO `sensorml`.`phenomenon` (`unique_id`, `phenomenon_description`, `unit`, `valuetype`) VALUES ('" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id().split(";")[0] + "', '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_description() + "', '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_unit() + "', '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_valuetype() + "');";
                    db.exUpdate(queryphenomena);
                    flag_phen = 1;
                } else {
                    logger.debug("\n phenomena già prensente con id "+ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id());
                }
                String query_checkoffid = "SELECT `offering_id` FROM `offering` WHERE `unique_id` LIKE '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_id().split(";")[0] + "'";
                rs = db.exQuery(query_checkoffid);
                if (rs.next() == false) {
                    String queryoffering = "INSERT INTO `sensorml`.`offering` (`unique_id`, `offering_name`) VALUES ( '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_id() + "', '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_name() + "');";
                    db.exUpdate(queryoffering);
                } else {
                    logger.debug("\n offering già prensente con id "+ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_id());
                }
                String checkphenoff = "SELECT `phen_off`.`offering_id`, `phen_off`.`phenomenon_id` FROM `phen_off`, `offering`, `phenomenon` WHERE `phen_off`.`offering_id` = `offering`.`offering_id` AND `phen_off`.`phenomenon_id`= `phenomenon`.`phenomenon_id` AND `offering`.`unique_id` LIKE '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_id() + "' AND `phenomenon`.`unique_id` LIKE '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id().split(";")[0] + "'";
                rs = db.exQuery(checkphenoff);
                if (rs.next() == false) {
                    String queryphenoff = "INSERT INTO `sensorml`.`phen_off`(`offering_id`, `phenomenon_id`) SELECT `offering_id` ,`phenomenon_id` FROM `sensorml`.`offering`,`sensorml`.`phenomenon` WHERE `sensorml`.`offering`.`unique_id` LIKE '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_id() + "' AND `sensorml`.`phenomenon`.`unique_id` LIKE '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id().split(";")[0] + "'";
                    db.exUpdate(queryphenoff);
                } else {
                    logger.debug("\n phen off già prensente con id "+ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_id()+"- "+ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id().split(";")[0]);
                }
                String checksensoff = "SELECT `sens_off`.`sensor_id`, `sens_off`.`offering_id` FROM `sens_off`, `offering`, `sensor` WHERE `sens_off`.`sensor_id` = `sensor`.`sensor_id` AND `sens_off`.`offering_id`= `offering`.`offering_id` AND `sensor`.`unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "' AND `offering`.`unique_id` LIKE '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_id() + "';";
                rs = db.exQuery(checksensoff);
                if (rs.next() == false) {
                    String querysensoff = "INSERT INTO `sensorml`.`sens_off`(`sensor_id`,`offering_id`) SELECT `sensor_id`,`offering_id`  FROM `sensorml`.`sensor`,`sensorml`.`offering` WHERE `sensorml`.`sensor`.`unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "' AND `sensorml`.`offering`.`unique_id` LIKE '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_id() + "'";
                    db.exUpdate(querysensoff);
                } else {
                    logger.debug("\n sens off già prensente con id "+ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_id()+" per sensore "+ddp.registerNodeDomParser.sensorDescription.getSensor_id());
                }

                String checksensphen = "SELECT `sens_phen`.`sensor_id`, `sens_phen`.`phenomenon_id` FROM `sens_phen`, `sensor`, `phenomenon` WHERE `sens_phen`.`sensor_id` = `sensor`.`sensor_id` AND `sens_phen`.`phenomenon_id`= `phenomenon`.`phenomenon_id` AND `sensor`.`unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "' AND `phenomenon`.`unique_id` LIKE '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id().split(";")[0] + "';";
                rs = db.exQuery(checksensphen);
                if (rs.next() == false) {
                    String querysensphen = "INSERT INTO `sensorml`.`sens_phen`(`sensor_id`, `phenomenon_id`) SELECT `sensor_id` ,`phenomenon_id` FROM `sensorml`.`sensor`,`sensorml`.`phenomenon` WHERE `sensorml`.`sensor`.`unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "' AND `sensorml`.`phenomenon`.`unique_id` LIKE '" + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id().split(";")[0] + "'";
                    db.exUpdate(querysensphen);
                } else {
                    logger.debug("\n sens phen già prensente con id "+ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id()+" per sensore "+ddp.registerNodeDomParser.sensorDescription.getSensor_id());
                }
                if (flag_phen == 1) {
                    //invio di un Phenomenon Advertise al SAS
                    
                    String advertisement = "";
                    advertisement = phenomenonAdvertiseXml(ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id().split(";")[0]);
                    sosAgent.sendSASAdvertisement(advertisement);
                    logger.info("sendSASAdvertisement");
                    //fine invio Phenomenon Advertise
                }

            }
            for (int i = 0; i < ddp.registerNodeDomParser.classifierDescription.size(); i++) {
                String query_checkclass = "SELECT `classifier_id` FROM `classifier` WHERE `unique_id` LIKE '" + ddp.registerNodeDomParser.classifierDescription.elementAt(i).getClassifier_id() + "' AND `classifier_value` LIKE '" + ddp.registerNodeDomParser.classifierDescription.elementAt(i).getClassifier_value() + "' ;";
                rs = db.exQuery(query_checkclass);
                if (rs.next() == false) {

                    String queryclass = "INSERT INTO `sensorml`.`classifier` ( `unique_id`, `classifier_description`,`classifier_value`) VALUES ( '" + ddp.registerNodeDomParser.classifierDescription.elementAt(i).getClassifier_id() + "', '" + ddp.registerNodeDomParser.classifierDescription.elementAt(i).getClassifier_description() + "','" + ddp.registerNodeDomParser.classifierDescription.elementAt(i).classifier_value + "' );";
                    db.exUpdate(queryclass);
                } else {
                    logger.debug("\n classifier già presente con id "+ddp.registerNodeDomParser.classifierDescription.elementAt(i).getClassifier_id());
                }
                String checksenclass = "SELECT `sens_class`.`sensor_id`, `sens_class`.`classifier_id` FROM `sens_class`, `sensor`, `classifier` WHERE `sens_class`.`sensor_id` = `sensor`.`sensor_id` AND `sens_class`.`classifier_id`= `classifier`.`classifier_id` AND `sensor`.`unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "' AND `classifier`.`unique_id` LIKE '" + ddp.registerNodeDomParser.classifierDescription.elementAt(i).classifier_id + "' AND `classifier`.`classifier_value` LIKE '" + ddp.registerNodeDomParser.classifierDescription.elementAt(i).getClassifier_value() + "';";
                rs = db.exQuery(checksenclass);
                if (rs.next() == false) {
                    String querysensclass = "INSERT INTO `sensorml`.`sens_class`(`sensor_id`, `classifier_id`) SELECT `sensor_id` ,`classifier_id` FROM `sensorml`.`sensor`,`sensorml`.`classifier` WHERE `sensorml`.`sensor`.`unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "' AND `sensorml`.`classifier`.`unique_id` LIKE '" + ddp.registerNodeDomParser.classifierDescription.elementAt(i).classifier_id + "'AND `sensorml`.`classifier`.`classifier_value` LIKE '" + ddp.registerNodeDomParser.classifierDescription.elementAt(i).getClassifier_value() + "';";
                    db.exUpdate(querysensclass);
                } else {
                    logger.debug("\n sens class già prensente con id "+ddp.registerNodeDomParser.classifierDescription.elementAt(i).classifier_id+" per sensore "+ddp.registerNodeDomParser.sensorDescription.getSensor_id());
                }
            }

            for (int i = 0; i < ddp.registerNodeDomParser.identifierDescription.size(); i++) {
                String query_checkident = "SELECT `identifier_id` FROM `identifier` WHERE `unique_id` LIKE '" + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_id() + "' AND `identifier_value` LIKE '" + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_value() + "' ;";
                rs = db.exQuery(query_checkident);
                if (rs.next() == false) {
                    String queryident = "INSERT INTO `sensorml`.`identifier` ( `unique_id`, `identifier_description`,`identifier_value`) VALUES ( '" + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_id() + "', '" + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_description() + "','" + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_value() + "' );";

                    db.exUpdate(queryident);
                } else {
                    logger.debug("\n identifier già presente con id "+ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_id());
                }
                String checksenident = "SELECT `sens_ident`.`sensor_id`, `sens_ident`.`identifier_id` FROM `sens_ident`, `sensor`, `identifier` WHERE `sens_ident`.`sensor_id` = `sensor`.`sensor_id` AND `sens_ident`.`identifier_id`= `identifier`.`identifier_id` AND `sensor`.`unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "' AND `identifier`.`unique_id` LIKE '" + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_id() + "';";
                rs = db.exQuery(checksenident);
                if (rs.next() == false) {
                    String querysensident = "INSERT INTO `sensorml`.`sens_ident`(`sensor_id`, `identifier_id`) SELECT `sensor_id` ,`identifier_id` FROM `sensorml`.`sensor`,`sensorml`.`identifier` WHERE `sensorml`.`sensor`.`unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "' AND `sensorml`.`identifier`.`unique_id` LIKE '" + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_id() + "'AND `sensorml`.`identifier`.`identifier_value` LIKE '" + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_value() + "';";
                    db.exUpdate(querysensident);
                } else {
                    logger.debug("\n sens ident già prensente con id "+ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_id()+" per sensore "+ddp.registerNodeDomParser.sensorDescription.getSensor_id());
                }
            }

            for (int i = 0; i < ddp.registerComponentDescription.size(); i++) {
                
                String query_checkcompid = "SELECT `component`.`component_id` FROM `component` WHERE `component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "';";
                rs = db.exQuery(query_checkcompid);
                if (rs.next() == false) {
                    String querycomp = "INSERT INTO `sensorml`.`component` (`unique_id` ,`description` ,`status` ,`mobile` ,`crs` , `longitude` , `long_uom` , `latitude` , `lat_uom` , `altitude` , `alt_uom` ) VALUES ( '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "','" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getDescription_type() + "','" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getStatus() + "','" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getMobile() + "','" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getCrs() + "','" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getLongitude() + "','" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getlong_uom() + "','" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getLatitude() + "','" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getlat_uom() + "','" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getAltitude() + "','" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getalt_uom() + "');";
                    db.exUpdate(querycomp);
                } else {
                    logger.debug("\n componente già presente con id"+ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() +" per sensore "+ddp.registerNodeDomParser.sensorDescription.getSensor_id());
                }
                String querysenscompcheck = "SELECT `sens_comp`.`component_id`, `sens_comp`.`sensor_id` FROM `sens_comp`, `sensor`, `component` WHERE `sens_comp`.`component_id` = `component`.`component_id` AND `sens_comp`.`sensor_id`= `sensor`.`sensor_id` AND `component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "' AND `sensor`.`unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "'";
                rs = db.exQuery(querysenscompcheck);
                if (rs.next() == false) {
                    String querysenscomp = "INSERT INTO `sensorml`.`sens_comp`(`sensor_id`, `component_id`) SELECT `sensor_id` ,`component_id` FROM `sensorml`.`sensor`,`sensorml`.`component` WHERE `sensorml`.`sensor`.`unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "' AND `sensorml`.`component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "'";
                    db.exUpdate(querysenscomp);
                } else {
                    logger.debug("\n coppia sensore componente già presente con id"+ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() +" per sensore "+ddp.registerNodeDomParser.sensorDescription.getSensor_id()+" e "+ddp.registerNodeDomParser.sensorDescription.getSensor_id());
                }
                for (int j = 0; j < ddp.registerComponentDescription.elementAt(i).phenomenonDescription.size(); j++) {
                    String checkcompoff = "SELECT `offering`.`offering_id` FROM `offering` WHERE `offering`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).phenomenonDescription.elementAt(j).getOffering_id() + "';";
                    rs = db.exQuery(checkcompoff);
                    if (rs.next() == true) {
                        String checkcompoffsel = "SELECT `comp_off`.`component_id`, `comp_off`.`offering_id` FROM `comp_off`, `offering`, `component` WHERE `comp_off`.`component_id` = `component`.`component_id` AND `comp_off`.`offering_id`= `offering`.`offering_id` AND `component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "' AND `offering`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).phenomenonDescription.elementAt(j).getOffering_id() + "';";
                        rs = db.exQuery(checkcompoffsel);
                        if (rs.next() == false) {
                            String querycompoff = "INSERT INTO `sensorml`.`comp_off`(`component_id`, `offering_id`) SELECT`component_id`, `offering_id`  FROM `sensorml`.`component`,`sensorml`.`offering` WHERE `sensorml`.`offering`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).phenomenonDescription.elementAt(j).getOffering_id() + "' AND `sensorml`.`component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "'";
                            db.exUpdate(querycompoff);
                        } else {
                            logger.debug("coppia offering "+ddp.registerComponentDescription.elementAt(i).phenomenonDescription.elementAt(j).getOffering_id()+" e componente "+ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id()+" già presente");
                        }
                    } else {
                        logger.debug("offering "+ddp.registerComponentDescription.elementAt(i).phenomenonDescription.elementAt(j).getOffering_id()+" da misurare col componente "+ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id());
                    }
                    String checkcompphen = "SELECT `comp_phen`.`component_id`, `comp_phen`.`phenomenon_id` FROM `comp_phen`, `component`, `phenomenon` WHERE `comp_phen`.`component_id` = `component`.`component_id` AND `comp_phen`.`phenomenon_id`= `phenomenon`.`phenomenon_id` AND `component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "' AND `phenomenon`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).phenomenonDescription.elementAt(j).getPhenomenon_id().split(";")[0] + "';";
                    rs = db.exQuery(checkcompphen);
                    if (rs.next() == false) {
                        String querycompphen = "INSERT INTO `sensorml`.`comp_phen`(`component_id`, `phenomenon_id`) SELECT `component_id` ,`phenomenon_id` FROM `sensorml`.`component`,`sensorml`.`phenomenon` WHERE `sensorml`.`component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "' AND `sensorml`.`phenomenon`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).phenomenonDescription.elementAt(j).getPhenomenon_id().split(";")[0] + "'";
                        db.exUpdate(querycompphen);
                    } else {
                        logger.debug("\n comp phen già prensente con id "+ddp.registerComponentDescription.elementAt(i).phenomenonDescription.elementAt(j).getPhenomenon_id()+" per componente "+ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id());
                    }

                }

                for (int j = 0; j < ddp.registerComponentDescription.elementAt(i).classifierDescription.size(); j++) {
                    String checkcompclass = "SELECT `classifier_id` FROM `classifier` WHERE `classifier`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).classifierDescription.elementAt(j).getClassifier_id() + "' AND `classifier_value` LIKE '" + ddp.registerComponentDescription.elementAt(i).classifierDescription.elementAt(j).getClassifier_value() + "' ;";
                    rs = db.exQuery(checkcompclass);
                    if (rs.next() == false) {
                        String queryclasscom = "INSERT INTO `sensorml`.`classifier` ( `unique_id`,  `classifier_description`,`classifier_value`) VALUES ( '" + ddp.registerComponentDescription.elementAt(i).classifierDescription.elementAt(j).getClassifier_id() + "', '" + ddp.registerComponentDescription.elementAt(i).classifierDescription.elementAt(j).getClassifier_description() + "','" + ddp.registerComponentDescription.elementAt(i).classifierDescription.elementAt(j).classifier_value + "' );";
                        db.exUpdate(queryclasscom);
                    } else {
                        logger.debug("nella tabella classifier,presente classifier "+ddp.registerComponentDescription.elementAt(i).classifierDescription.elementAt(j).getClassifier_id()+" del componente "+ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id());
                    }
                    String checkcomclassrel = "SELECT `comp_class`.`component_id`, `comp_class`.`classifier_id` FROM `comp_class`, `component`, `classifier` WHERE `comp_class`.`component_id` = `component`.`component_id` AND `comp_class`.`classifier_id`= `classifier`.`classifier_id` AND `component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "' AND `classifier`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).classifierDescription.elementAt(j) + "';";
                    rs = db.exQuery(checkcomclassrel);
                    if (rs.next() == false) {
                        String insclasscomp = "INSERT INTO `sensorml`.`comp_class`(`component_id`, `classifier_id`) SELECT `component_id` ,`classifier_id` FROM `sensorml`.`component`,`sensorml`.`classifier` WHERE `sensorml`.`component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "' AND `sensorml`.`classifier`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).classifierDescription.elementAt(j).getClassifier_id() + "' AND `sensorml`.`classifier`.`classifier_value` LIKE '" + ddp.registerComponentDescription.elementAt(i).classifierDescription.elementAt(j).getClassifier_value() + "' ";
                        db.exUpdate(insclasscomp);
                    } else {
                        logger.debug("nella tabella comp_class,coppia presente classifier "+ddp.registerComponentDescription.elementAt(i).classifierDescription.elementAt(j).getClassifier_id()+" del componente "+ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id());
                    }
                }
                for (int j = 0; j < ddp.registerComponentDescription.elementAt(i).identifierDescription.size(); j++) {
                    String checkcompident = "SELECT `identifier_id` FROM `identifier` WHERE `identifier`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).identifierDescription.elementAt(j).getidentifier_id() + "' AND `identifier_value` LIKE '" + ddp.registerComponentDescription.elementAt(i).identifierDescription.elementAt(j).getidentifier_value() + "' ;";
                    rs = db.exQuery(checkcompident);
                    if (rs.next() == false) {
                        String queryidentcom = "INSERT INTO `sensorml`.`identifier` ( `unique_id`,  `identifier_description`,`identifier_value`) VALUES ( '" + ddp.registerComponentDescription.elementAt(i).identifierDescription.elementAt(j).getidentifier_id() + "', '" + ddp.registerComponentDescription.elementAt(i).identifierDescription.elementAt(j).getidentifier_description() + "','" + ddp.registerComponentDescription.elementAt(i).identifierDescription.elementAt(j).getidentifier_value() + "' );";
                        db.exUpdate(queryidentcom);
                    } else {
                        logger.debug("nella tabella identifier,presente identifier "+ddp.registerComponentDescription.elementAt(i).identifierDescription.elementAt(j).getidentifier_id()+" del componente "+ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id());
                    }
                    String checkcomidentrel = "SELECT `comp_ident`.`component_id`, `comp_ident`.`identifier_id` FROM `comp_ident`, `component`, `identifier` WHERE `comp_ident`.`component_id` = `component`.`component_id` AND `comp_ident`.`identifier_id`= `identifier`.`identifier_id` AND `component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "' AND `identifier`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).identifierDescription.elementAt(j) + "';";
                    rs = db.exQuery(checkcomidentrel);
                    if (rs.next() == false) {
                        String insidentcomp = "INSERT INTO `sensorml`.`comp_ident`(`component_id`, `identifier_id`) SELECT `component_id` ,`identifier_id` FROM `sensorml`.`component`,`sensorml`.`identifier` WHERE `sensorml`.`component`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id() + "' AND `sensorml`.`identifier`.`unique_id` LIKE '" + ddp.registerComponentDescription.elementAt(i).identifierDescription.elementAt(j).getidentifier_id() + "' AND `sensorml`.`identifier`.`identifier_value` LIKE '" + ddp.registerComponentDescription.elementAt(i).identifierDescription.elementAt(j).getidentifier_value() + "' ";
                        db.exUpdate(insidentcomp);
                    } else {
                        logger.debug("nella tabella comp_ident,coppia presente identifier "+ddp.registerComponentDescription.elementAt(i).identifierDescription.elementAt(j).getidentifier_id()+" del componente "+ddp.registerComponentDescription.elementAt(i).sensorDescription.getSensor_id());
                    }
                }



            }

        } else {
            logger.debug("\n elemento già prensente");
        }



    }

    void printInfo() {
        //dalla classe sensorDescription:
        /*System.out.println("Sensor id da attributo: " + ddp.registerNodeDomParser.sensorDescription.getSensor_id());
        System.out.println("Sensor description da attributo: " + ddp.registerNodeDomParser.sensorDescription.getDescription_type());
        System.out.println("Sensor mobile da attributo: " + ddp.registerNodeDomParser.sensorDescription.getMobile());
        System.out.println("Sensor status da attributo: " + ddp.registerNodeDomParser.sensorDescription.getStatus());
        System.out.println("Sensor crs da attributo: " + ddp.registerNodeDomParser.sensorDescription.getCrs());
        System.out.println("Sensor fixed da attributo: " + ddp.registerNodeDomParser.sensorDescription.getFixed());
        System.out.println("Sensor frequency : " + ddp.registerNodeDomParser.sensorDescription.getFrequency() + " " + ddp.registerNodeDomParser.sensorDescription.getFrequencyUom());

        System.out.println("Sensor x da attributo: " + ddp.registerNodeDomParser.sensorDescription.getLongitude());
        System.out.println("Sensor x uom : " + ddp.registerNodeDomParser.sensorDescription.getlong_uom());
        System.out.println("Sensor y da attributo: " + ddp.registerNodeDomParser.sensorDescription.getLatitude());
        System.out.println("Sensor y uom : " + ddp.registerNodeDomParser.sensorDescription.getlat_uom());
        System.out.println("Sensor z da attributo: " + ddp.registerNodeDomParser.sensorDescription.getAltitude());
        System.out.println("Sensor z uom : " + ddp.registerNodeDomParser.sensorDescription.getalt_uom());



        for (int i = 0; i < ddp.registerNodeDomParser.phenomenonDescription.size(); i++) {
            System.out.println("\n phenomena numero " + i);
            System.out.println("offering id da attributo: " + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_id());
            System.out.println("offering description da attributo: " + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getOffering_name());
            System.out.println("phenomenon description da attributo: " + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_description());
            System.out.println("phenomenon id da attributo: " + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_id());
            System.out.println("phenomenon unit da attributo: " + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_unit());
            System.out.println("phenomenon valuetype da attributo: " + ddp.registerNodeDomParser.phenomenonDescription.elementAt(i).getPhenomenon_valuetype());



        }
        for (int i = 0; i < ddp.registerNodeDomParser.classifierDescription.size(); i++) {
            System.out.println("\n classifier numero " + i);
            System.out.println("classifier description: " + ddp.registerNodeDomParser.classifierDescription.elementAt(i).getClassifier_description());
            System.out.println("classifier id: " + ddp.registerNodeDomParser.classifierDescription.elementAt(i).getClassifier_id());
            System.out.println("classifier value: " + ddp.registerNodeDomParser.classifierDescription.elementAt(i).getClassifier_value());


        }
        for (int i = 0; i < ddp.registerNodeDomParser.identifierDescription.size(); i++) {
            System.out.println("\n identifier numero " + i);
            System.out.println("identifier description: " + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_description());
            System.out.println("identifier id: " + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_id());
            System.out.println("identifier value: " + ddp.registerNodeDomParser.identifierDescription.elementAt(i).getidentifier_value());


        }
        for (int ii = 0; ii < ddp.registerComponentDescription.size(); ii++) {
            System.out.println("\n componente numero " + ii);
            System.out.println("Component id da attributo: " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getSensor_id());

            System.out.println("Sensor description da attributo: " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getDescription_type());
            System.out.println("Sensor mobile da attributo: " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getMobile());
            System.out.println("Sensor status da attributo: " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getStatus());
            System.out.println("Sensor crs da attributo: " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getCrs());
            System.out.println("Sensor fixed da attributo: " + ddp.registerNodeDomParser.sensorDescription.getFixed());
            System.out.println("Sensor x da attributo: " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getLongitude());
            System.out.println("Sensor x uom : " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getlong_uom());
            System.out.println("Sensor y da attributo: " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getLatitude());
            System.out.println("Sensor y uom : " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getlat_uom());
            System.out.println("Sensor z da attributo: " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getAltitude());
            System.out.println("Sensor z uom : " + ddp.registerComponentDescription.elementAt(ii).sensorDescription.getalt_uom());



            for (int i = 0; i < ddp.registerComponentDescription.elementAt(ii).phenomenonDescription.size(); i++) {
                System.out.println("\n phenomena numero " + i);
                System.out.println("offering id da attributo: " + ddp.registerComponentDescription.elementAt(ii).phenomenonDescription.elementAt(i).getOffering_id());
                System.out.println("offering description da attributo: " + ddp.registerComponentDescription.elementAt(ii).phenomenonDescription.elementAt(i).getOffering_name());
                System.out.println("phenomenon description da attributo: " + ddp.registerComponentDescription.elementAt(ii).phenomenonDescription.elementAt(i).getPhenomenon_description());
                System.out.println("phenomenon id da attributo: " + ddp.registerComponentDescription.elementAt(ii).phenomenonDescription.elementAt(i).getPhenomenon_id());
                System.out.println("phenomenon unit da attributo: " + ddp.registerComponentDescription.elementAt(ii).phenomenonDescription.elementAt(i).getPhenomenon_unit());
                System.out.println("phenomenon valuetype da attributo: " + ddp.registerComponentDescription.elementAt(ii).phenomenonDescription.elementAt(i).getPhenomenon_valuetype());



            }
            for (int i = 0; i < ddp.registerComponentDescription.elementAt(ii).classifierDescription.size(); i++) {
                System.out.println("\n classifier numero " + i);
                System.out.println("classifier description: " + ddp.registerComponentDescription.elementAt(ii).classifierDescription.elementAt(i).getClassifier_description());
                System.out.println("classifier id: " + ddp.registerComponentDescription.elementAt(ii).classifierDescription.elementAt(i).getClassifier_id());
                System.out.println("classifier value: " + ddp.registerComponentDescription.elementAt(ii).classifierDescription.elementAt(i).getClassifier_value());


            }
            for (int i = 0; i < ddp.registerComponentDescription.elementAt(ii).identifierDescription.size(); i++) {
                System.out.println("\n identifier numero " + i);
                System.out.println("identifier description: " + ddp.registerComponentDescription.elementAt(ii).identifierDescription.elementAt(i).getidentifier_description());
                System.out.println("identifier id: " + ddp.registerComponentDescription.elementAt(ii).identifierDescription.elementAt(i).getidentifier_id());
                System.out.println("identifier value: " + ddp.registerComponentDescription.elementAt(ii).identifierDescription.elementAt(i).getidentifier_value());


            }
        }*/
    }

    public String write_register_xml() throws ParserConfigurationException, TransformerException, SQLException {
        DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder2 = dbf2.newDocumentBuilder();
        Document doc = builder2.newDocument();
        //DataBase db=new DataBase();
        //  db.openDB(this.parameterContainer.getDbServer(),this.parameterContainer.getDbDriver(),this.parameterContainer.getDbName(),
        //           this.parameterContainer.getDbUsername(),this.parameterContainer.getDbPassword());         
        //File file = new File(this.filename_output);
        Element root = doc.createElement("RegisterSensorResponse");
        root.setAttribute("xmlns", "http://www.opengis.net/sos/1.0");
        root.setAttribute("xsi:schemaLocation", "http://www.opengis.net/om/1.0 http://schemas.opengis.net/om/1.0.0/om.xsd");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        String assigned_id = "SELECT `sensor_id` FROM `sensor` WHERE `unique_id` LIKE '" + ddp.registerNodeDomParser.sensorDescription.getSensor_id() + "'";
        ResultSet rs = db.exQuery(assigned_id);
        if (rs.next()) {
            Element assigned = doc.createElement("AssignedSensorId");
            Text asstext = doc.createTextNode(rs.getString(1));
            assigned.appendChild(asstext);
            root.appendChild(assigned);
        }
        doc.appendChild(root);
        StringWriter stringWriter = new StringWriter();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);
        return stringWriter.getBuffer().toString();

    }

    private String phenomenonAdvertiseXml(String phen_unique_id) throws ParserConfigurationException, SQLException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder2 = dbf2.newDocumentBuilder();
        Document doc = builder2.newDocument();
        //DataBase db=new DataBase();
        // db.openDB(this.parameterContainer.getDbServer(),this.parameterContainer.getDbDriver(),this.parameterContainer.getDbName(),
        //          this.parameterContainer.getDbUsername(),this.parameterContainer.getDbPassword());         

        //File file = new File("/home/user/phenomenon_advertise.xml");
        Element root = doc.createElement("PhenomenonAdvertise");
        //root.setAttribute("xmlns","http://www.opengis.net/sas");
        root.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
        root.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sas ../sasAll.xsd");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:swe", "http://www.opengis.net/swe");
        root.setAttribute("service", "SAS");
        root.setAttribute("version", "1.0.0");
        Element foi = doc.createElement("FeatureOfInterest");
        String assigned_id = "SELECT `identifier_value`,`offering_name` FROM `phenomenon`, `sens_phen`,`sens_ident`,`identifier`, `phen_off`,`offering` WHERE `phenomenon`.`phenomenon_id`=`sens_phen`.`phenomenon_id` AND `sens_phen`.`sensor_id`= `sens_ident`.`sensor_id` AND `sens_ident`.`identifier_id`=`identifier`.`identifier_id` AND `identifier`.`unique_id` LIKE 'urn:ogc:def:identifier:OGC:1.0:operator' AND `phen_off`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phen_off`.`offering_id`=`offering`.`offering_id` AND `phenomenon`.`unique_id` LIKE '" + phen_unique_id + "' ";
        ResultSet rs = db.exQuery(assigned_id);
        if (rs.next()) {
            Element assigned = doc.createElement("Name");
            Text asstext = doc.createTextNode(rs.getString(1));
            assigned.appendChild(asstext);
            foi.appendChild(assigned);
            Element des = doc.createElement("Description");
            Text destext = doc.createTextNode(rs.getString(2) + " " + rs.getString(1));
            des.appendChild(destext);
            foi.appendChild(des);
        }
        root.appendChild(foi);

        Element area = doc.createElement("OperationArea");
        Element geo = doc.createElement("swe:GeoLocation");
        String sel_area = "SELECT max(`longitude`), max(`latitude`), max(`altitude`),min(`longitude`), min(`latitude`), min(`altitude`) FROM `sensor`, `phenomenon`,`sens_phen` WHERE `sens_phen`.`sensor_id`=`sensor`.`sensor_id` AND `sens_phen`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + phen_unique_id + "'";
        rs = db.exQuery(sel_area);

        if (rs.next()) {
            if (rs.getString(1).equals(rs.getString(4)) && rs.getString(2).equals(rs.getString(5))) {
                Element longit = doc.createElement("swe:longitude");
                Element quantlon = doc.createElement("swe:Quantity");
                Text longtext = doc.createTextNode(rs.getString(1));
                quantlon.appendChild(longtext);
                longit.appendChild(quantlon);
                geo.appendChild(longit);
                Element latit = doc.createElement("swe:latitude");
                Element quantlat = doc.createElement("swe:Quantity");
                Text lattext = doc.createTextNode(rs.getString(2));
                quantlat.appendChild(lattext);
                latit.appendChild(quantlat);
                geo.appendChild(latit);
                Element altit = doc.createElement("swe:altitude");
                Element quantalt = doc.createElement("swe:Quantity");
                Text alttext = doc.createTextNode(rs.getString(3));
                quantalt.appendChild(alttext);
                altit.appendChild(quantalt);
                geo.appendChild(altit);
            } else {
                Element bounded = doc.createElement("gml:boundedBy");
                Element env = doc.createElement("gml:Envelope");

                env.setAttribute("srsName", "EPSG:4326");
                Element lowmax = doc.createElement("gml:lowerCorner");
                Text lowmaxtext = doc.createTextNode(rs.getString(3) + " " + rs.getString(4));
                lowmax.appendChild(lowmaxtext);
                Element lowmin = doc.createElement("gml:upperCorner");
                Text lowmintext = doc.createTextNode(rs.getString(1) + " " + rs.getString(2));
                lowmin.appendChild(lowmintext);
                env.appendChild(lowmin);
                env.appendChild(lowmax);
                bounded.appendChild(env);
                geo.appendChild(bounded);





            }
            area.appendChild(geo);
            root.appendChild(area);
        }
        Element alert = doc.createElement("AlertMessageStructure");
        Element quanprop = doc.createElement("QuantityProperty");
        Element content = doc.createElement("Content");
        content.setAttribute("definition", phen_unique_id);
        String sel_uom = "SELECT `unit` FROM `phenomenon` WHERE `unique_id` LIKE '" + phen_unique_id + "'";
        rs = db.exQuery(sel_uom);
        if (rs.next()) {
            content.setAttribute("uom", rs.getString(1));
        }
        quanprop.appendChild(content);
        alert.appendChild(quanprop);
        root.appendChild(alert);
        Element alertfreq = doc.createElement("AlertFrequency");
        String sel_freq = "SELECT `frequency` FROM `phenomenon`,`sensor`, `sens_phen` WHERE  `sens_phen`.`sensor_id`=`sensor`.`sensor_id` AND `sens_phen`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + phen_unique_id + "'";
        rs = db.exQuery(sel_freq);
        if (rs.next()) {
            Text altfreqtext = doc.createTextNode(rs.getString(1));
            alertfreq.appendChild(altfreqtext);
        }
        root.appendChild(alertfreq);
        Element timeexp = doc.createElement("DesiredPublicationExpiration");
        Text timeexptext = doc.createTextNode("2015-11-28T08:12:31Z");
        timeexp.appendChild(timeexptext);
        root.appendChild(timeexp);
        doc.appendChild(root);



        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StringWriter stringWriter = new StringWriter();
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);
        return stringWriter.getBuffer().toString();

    }
}
