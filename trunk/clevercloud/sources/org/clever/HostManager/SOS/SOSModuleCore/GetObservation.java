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
 * The MIT License
 *
 * Copyright 2012 Università di Messina.
 * Copyright 2012 alessiodipietro.
 * Copyright 2012 user.
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
package org.clever.HostManager.SOS.SOSModuleCore;

import org.clever.HostManager.SOS.Database;
import org.clever.HostManager.SOS.ParameterContainer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;

import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.Vector;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author user
 */
public class GetObservation {

    private getObsDomCleanParser iod;
    private Database db;
    private Vector<GetObservationBufferData> gobd_all;
    private DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder2 = dbf2.newDocumentBuilder();
    private Document doc = builder2.newDocument();
    private ParameterContainer parameterContainer = null;
    private Logger logger=Logger.getLogger("getObservation");
    //private String filename_output;
/*GetObservation(String filename) throws ParserConfigurationException, SAXException, IOException {
    iod=new getObsDomCleanParser();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = dbf.newDocumentBuilder();
    File xmlFile = new File(filename);
    Document document = builder.parse(xmlFile);
    iod.getObsInfo(document);
    this.filename_output="/home/user/file_observation.xml";
    
    }*/

    public GetObservation(String getObservationRequest) throws ParserConfigurationException, SAXException, IOException {
        this.parameterContainer = ParameterContainer.getInstance();
        iod = new getObsDomCleanParser();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        //File xmlFile = new File(getObservationRequest);
        Document document = builder.parse(new ByteArrayInputStream(getObservationRequest.getBytes()));
        iod.getObsInfo(document);

        /*this.filename_output=filename_output;
        if(this.filename_output.equals(""))
        this.filename_output=System.getProperty("user.home")+"/file_observation.xml";*/

    }

    public void printResult() {
       logger.debug("\n risultatio interrogazione");

        for (int i = 0; i < gobd_all.size(); i++) {
            logger.debug("\n phenomena " + i);

            for (int k = 0; k < gobd_all.elementAt(i).getvalues().size(); k++) {
                logger.debug("\n sensore: " + gobd_all.elementAt(i).getvalues().elementAt(k).getsens_id());
                logger.debug("\n values: " + gobd_all.elementAt(i).getvalues().elementAt(k).gettime() + "," + gobd_all.elementAt(i).getvalues().elementAt(k).getlong() + "," + gobd_all.elementAt(i).getvalues().elementAt(k).getlat() + "," + gobd_all.elementAt(i).getvalues().elementAt(k).getvalue());
                logger.debug("\n phen: " + gobd_all.elementAt(i).getvalues().elementAt(k).getphen_id());
                for (int j = 0; j < gobd_all.elementAt(i).getvalues().elementAt(k).getfield().size(); j++) {
                    logger.debug("\n campo nome: " + gobd_all.elementAt(i).getvalues().elementAt(k).getfield().elementAt(j).getname() + " def: " + gobd_all.elementAt(i).getvalues().elementAt(k).getfield().elementAt(j).getdef() + " uom: " + gobd_all.elementAt(i).getvalues().elementAt(k).getfield().elementAt(j).getuom());
                }
            }

        }
    }

    public String write_getobs_xml() throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        logger.debug("write_getobs_xml");
        //Elemento radice
        Element root = doc.createElement("om:ObservationCollection");

        root.setAttribute("xmlns:swe", "http://www.opengis.net/swe/1.0.1");
        root.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
        root.setAttribute("xmlns:om", "http://www.opengis.net/om/1.0");
        root.setAttribute("xsi:schemaLocation", "http://www.opengis.net/om/1.0 http://schemas.opengis.net/om/1.0.0/om.xsd");
        root.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        doc.appendChild(root);

        for (int i = 0; i < gobd_all.size(); i++) {

            Vector<Integer> checked = new Vector<Integer>(1);
            for (int j = 0; j < gobd_all.elementAt(i).getvalues().size(); j++) {

                String sensor_id = "";
                String phen_id = "";
                String strvalues = "";
                int count = 0;
                int flag = 0;
                for (int k = 0; k < checked.size(); k++) {
                    if (checked.elementAt(k) == j) {
                        //se già stato esaminato
                        flag = 1;
                    }

                }

                if (flag == 0) {

                    sensor_id = gobd_all.elementAt(i).getvalues().elementAt(j).getsens_id();
                    phen_id = gobd_all.elementAt(i).getvalues().elementAt(j).getphen_id();


                    checked.add(j);
                    for (int k = 0; k < gobd_all.elementAt(i).getvalues().size(); k++) {
                        if (gobd_all.elementAt(i).getvalues().elementAt(k).getsens_id().equals(sensor_id)) {
                            count++;
                            checked.add(k);
                            if (strvalues.equals("")) {
                                String strtemp = "" + gobd_all.elementAt(i).getvalues().elementAt(k).gettime().replace(" ", "T") + "Z," + gobd_all.elementAt(i).getvalues().elementAt(k).getlong() + "," + gobd_all.elementAt(i).getvalues().elementAt(k).getlat() + "," + gobd_all.elementAt(i).getvalues().elementAt(k).getvalue();
                                strvalues = "" + strvalues + "" + strtemp;
                            } else {
                                String strtemp = "@@" + gobd_all.elementAt(i).getvalues().elementAt(k).gettime().replace(" ", "T") + "Z," + gobd_all.elementAt(i).getvalues().elementAt(k).getlong() + "," + gobd_all.elementAt(i).getvalues().elementAt(k).getlat() + "," + gobd_all.elementAt(i).getvalues().elementAt(k).getvalue();
                                strvalues = "" + strvalues + "" + strtemp;
                            }



                        }

                    }
                    Element member = doc.createElement("om:member");
                    Element observ = doc.createElement("om:Observation");
                    Element name = doc.createElement("gml:name");
                    Text nametext = doc.createTextNode("");
                    name.appendChild(nametext);
                    observ.appendChild(name);
                    Element samplingtime = doc.createElement("om:samplingtime");
                    Element timeper = doc.createElement("gml:TimePeriod");
                    Element timemin = doc.createElement("gml:beginPosition");
                    Element timemax = doc.createElement("gml:endPosition");
                    Text timemintext = doc.createTextNode(gobd_all.elementAt(i).getvalues().elementAt(j).getmintime().replace(" ", "T") + "Z");
                    Text timemaxtext = doc.createTextNode(gobd_all.elementAt(i).getvalues().elementAt(j).getmaxtime().replace(" ", "T") + "Z");
                    timemin.appendChild(timemintext);
                    timemax.appendChild(timemaxtext);
                    timeper.appendChild(timemin);
                    timeper.appendChild(timemax);
                    samplingtime.appendChild(timeper);
                    observ.appendChild(samplingtime);
                    Element procedure = doc.createElement("om:procedure");
                    Text proceduretext = doc.createTextNode(sensor_id);
                    procedure.appendChild(proceduretext);
                    observ.appendChild(procedure);
                    Element phen = doc.createElement("om:observedProperty");
                    Text phentext = doc.createTextNode(phen_id);
                    phen.appendChild(phentext);
                    observ.appendChild(phen);
                    Element result = doc.createElement("om:result");
                    Element data_array = doc.createElement("swe:DataArray");
                    Element elementcount = doc.createElement("swe:elementCount");
                    Element el_count = doc.createElement("swe:Count");
                    Element countval = doc.createElement("swe:value");
                    Text countvalt = doc.createTextNode("" + count);
                    countval.appendChild(countvalt);
                    el_count.appendChild(countval);
                    elementcount.appendChild(el_count);
                    data_array.appendChild(elementcount);
                    if (j == 0) {
                        Element elmtype = doc.createElement("swe:elementType");
                        elmtype.setAttribute("name", "Components");
                        Element simpledata = doc.createElement("swe:SimpleDataRecord");
                        simpledata.setAttribute("gml:id", "DataDefinition");
                        for (int jj = 0; jj < gobd_all.elementAt(i).getvalues().elementAt(0).getfield().size(); jj++) {
                            if (gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getname().equals("time")) {
                                Element field = doc.createElement("swe:field");
                                field.setAttribute("name", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getname());
                                Element quantity = doc.createElement("swe:Quantity");
                                quantity.setAttribute("definition", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getdef());
                                if (gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getuom().equals("") == false) {
                                    Element uom = doc.createElement("swe:uom");
                                    uom.setAttribute("code", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getuom());
                                    quantity.appendChild(uom);
                                }
                                field.appendChild(quantity);
                                simpledata.appendChild(field);
                            }

                        }
                        for (int jj = 0; jj < gobd_all.elementAt(i).getvalues().elementAt(0).getfield().size(); jj++) {
                            if (gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getname().equals("latitude")) {
                                Element field = doc.createElement("swe:field");
                                field.setAttribute("name", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getname());
                                Element quantity = doc.createElement("swe:Quantity");
                                quantity.setAttribute("definition", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getdef());
                                if (gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getuom().equals("") == false) {
                                    Element uom = doc.createElement("swe:uom");
                                    uom.setAttribute("code", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getuom());
                                    quantity.appendChild(uom);
                                }
                                field.appendChild(quantity);
                                simpledata.appendChild(field);
                            }

                        }
                        for (int jj = 0; jj < gobd_all.elementAt(i).getvalues().elementAt(0).getfield().size(); jj++) {
                            if (gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getname().equals("longitude")) {
                                Element field = doc.createElement("swe:field");
                                field.setAttribute("name", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getname());
                                Element quantity = doc.createElement("swe:Quantity");
                                quantity.setAttribute("definition", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getdef());
                                if (gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getuom().equals("") == false) {
                                    Element uom = doc.createElement("swe:uom");
                                    uom.setAttribute("code", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getuom());
                                    quantity.appendChild(uom);
                                }
                                field.appendChild(quantity);
                                simpledata.appendChild(field);
                            }

                        }
                        for (int jj = 0; jj < gobd_all.elementAt(i).getvalues().elementAt(0).getfield().size(); jj++) {
                            if (gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getname().equals("latitude") == false && gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getname().equals("longitude") == false && gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getname().equals("time") == false) {
                                Element field = doc.createElement("swe:field");
                                field.setAttribute("name", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getname());
                                Element quantity = doc.createElement("swe:Quantity");
                                quantity.setAttribute("definition", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getdef());
                                if (gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getuom().equals("") == false) {
                                    Element uom = doc.createElement("swe:uom");
                                    uom.setAttribute("code", gobd_all.elementAt(i).getvalues().elementAt(0).getfield().elementAt(jj).getuom());
                                    quantity.appendChild(uom);
                                }
                                field.appendChild(quantity);
                                simpledata.appendChild(field);
                            }

                        }
                        elmtype.appendChild(simpledata);
                        data_array.appendChild(elmtype);

                    } else {
                        Element elmtype = doc.createElement("swe:elementType");
                        elmtype.setAttribute("name", "Components");
                        elmtype.setAttribute("xlink:href", "#DataDefinition");
                        data_array.appendChild(elmtype);
                    }

                    Element encoding = doc.createElement("swe:encoding");
                    Element textblock = doc.createElement("swe:TextBlock");
                    textblock.setAttribute("tokenSeparator", ",");
                    textblock.setAttribute("decimalSeparator", ".");
                    textblock.setAttribute("blockSeparator", "@@");
                    encoding.appendChild(textblock);

                    data_array.appendChild(encoding);
                    Element values = doc.createElement("swe:values");
                    //System.out.println("GetObservation(267): VALUES: " + strvalues);
                    Text valtext = doc.createTextNode(strvalues);
                    values.appendChild(valtext);
                    data_array.appendChild(values);
                    result.appendChild(data_array);
                    observ.appendChild(result);
                    member.appendChild(observ);
                    root.appendChild(member);
                }
                //File file = new File(this.filename_output);
            }
        }

        StringWriter stringWriter = new StringWriter();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);

            
        return stringWriter.getBuffer().toString();



    }

    public void getObsDb() throws SQLException {

        gobd_all = new Vector<GetObservationBufferData>(1);
        Vector<String> query = new Vector<String>(1);
        if (iod.getInfo().getObsPhenomena().isEmpty() == true) {
        } else {
            //ricerca per tempo, spazio, id del sensore e phenomena
            if (iod.getInfo().getTime_stamp_min().equals("") == false && iod.getInfo().getSensor_id().isEmpty() == false && iod.getInfo().getCoordinate().isEmpty() == false) {
                if (iod.getInfo().getTime_stamp_max().equals("") == false) {
                    GetObservationBufferData gobd = new GetObservationBufferData();
                    for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {
                        for (int j = 0; j < iod.getInfo().getSensor_id().size(); j++) {
                            //SELECT max(`observation`.`time_stamp`) ,min(`observation`.`time_stamp`) 

                            query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE MBRContains(GeomFromText(AsText( Envelope( GeomFromText( 'LineString(" + iod.getInfo().getCoordinate().elementAt(0) + " " + iod.getInfo().getCoordinate().elementAt(1) + "," + iod.getInfo().getCoordinate().elementAt(2) + " " + iod.getInfo().getCoordinate().elementAt(3) + ")')))),`observation`.`coordinate`)='1' AND  `observation`.`time_stamp`>= TIMESTAMP('" + iod.getInfo().getTime_stamp_min() + "') AND `observation`.`time_stamp`<= TIMESTAMP('" + iod.getInfo().getTime_stamp_max() + "') AND `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "' AND `sensor`.`unique_id` LIKE '" + iod.getInfo().getSensor_id().elementAt(j) + "'");



                        }
                    }
                } else {
                    for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {
                        for (int j = 0; j < iod.getInfo().getSensor_id().size(); j++) {
                            //se non c'è un intervallo di tempo massimo
                            query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE MBRContains(GeomFromText(AsText( Envelope( GeomFromText( 'LineString(" + iod.getInfo().getCoordinate().elementAt(0) + " " + iod.getInfo().getCoordinate().elementAt(1) + "," + iod.getInfo().getCoordinate().elementAt(2) + " " + iod.getInfo().getCoordinate().elementAt(3) + ")')))),`observation`.`coordinate`)='1' AND  `observation`.`time_stamp`>= TIMESTAMP('" + iod.getInfo().getTime_stamp_min() + "') AND `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "' AND `sensor`.`unique_id` LIKE '" + iod.getInfo().getSensor_id().elementAt(j) + "'");

                        }
                    }
                }

            }
            //ricerca per tempo e sensore
            if (iod.getInfo().getTime_stamp_min().equals("") == false && iod.getInfo().getSensor_id().isEmpty() == false && iod.getInfo().getCoordinate().isEmpty()) {
                if (iod.getInfo().getTime_stamp_max().equals("") == false) {
                    GetObservationBufferData gobd = new GetObservationBufferData();
                    for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {
                        for (int j = 0; j < iod.getInfo().getSensor_id().size(); j++) {
                            query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE  `observation`.`time_stamp`>= TIMESTAMP('" + iod.getInfo().getTime_stamp_min() + "') AND `observation`.`time_stamp`<= TIMESTAMP('" + iod.getInfo().getTime_stamp_max() + "') AND `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "' AND `sensor`.`unique_id` LIKE '" + iod.getInfo().getSensor_id().elementAt(j) + "'");
                        }
                    }
                } else {
                    for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {
                        for (int j = 0; j < iod.getInfo().getSensor_id().size(); j++) {
                            //se non c'è un intervallo di tempo massimo
                            query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE  `observation`.`time_stamp`>= TIMESTAMP('" + iod.getInfo().getTime_stamp_min() + "') AND `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "' AND `sensor`.`unique_id` LIKE '" + iod.getInfo().getSensor_id().elementAt(j) + "'");
                        }
                    }


                }
            }
            //ricerca per tempo e luogo
            if (iod.getInfo().getTime_stamp_min().equals("") == false && iod.getInfo().getSensor_id().isEmpty() && iod.getInfo().getCoordinate().isEmpty() == false) {
                if (iod.getInfo().getTime_stamp_max().equals("") == false) {
                    GetObservationBufferData gobd = new GetObservationBufferData();
                    for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {
                        query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE MBRContains(GeomFromText(AsText( Envelope( GeomFromText( 'LineString(" + iod.getInfo().getCoordinate().elementAt(0) + " " + iod.getInfo().getCoordinate().elementAt(1) + "," + iod.getInfo().getCoordinate().elementAt(2) + " " + iod.getInfo().getCoordinate().elementAt(3) + ")')))),`observation`.`coordinate`)='1' AND  `observation`.`time_stamp`>= TIMESTAMP('" + iod.getInfo().getTime_stamp_min() + "') AND `observation`.`time_stamp`<= TIMESTAMP('" + iod.getInfo().getTime_stamp_max() + "') AND `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "' ");
                    }
                } else {
                    for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {

                        //se non c'è un intervallo di tempo massimo
                        query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE MBRContains(GeomFromText(AsText( Envelope( GeomFromText( 'LineString(" + iod.getInfo().getCoordinate().elementAt(0) + " " + iod.getInfo().getCoordinate().elementAt(1) + "," + iod.getInfo().getCoordinate().elementAt(2) + " " + iod.getInfo().getCoordinate().elementAt(3) + ")')))),`observation`.`coordinate`)='1' AND  `observation`.`time_stamp`>= TIMESTAMP('" + iod.getInfo().getTime_stamp_min() + "') AND `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "' ");
                    }
                }
            }

            //rcerca per luogo e sensore
            if (iod.getInfo().getTime_stamp_min().equals("") == true && iod.getInfo().getSensor_id().isEmpty() == false && iod.getInfo().getCoordinate().isEmpty() == false) {
                GetObservationBufferData gobd = new GetObservationBufferData();
                for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {
                    for (int j = 0; j < iod.getInfo().getSensor_id().size(); j++) {
                        query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE MBRContains(GeomFromText(AsText( Envelope( GeomFromText( 'LineString(" + iod.getInfo().getCoordinate().elementAt(0) + " " + iod.getInfo().getCoordinate().elementAt(1) + "," + iod.getInfo().getCoordinate().elementAt(2) + " " + iod.getInfo().getCoordinate().elementAt(3) + ")')))),`observation`.`coordinate`)='1' AND `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "' AND `sensor`.`unique_id` LIKE '" + iod.getInfo().getSensor_id().elementAt(j) + "'");
                    }
                }
            }
            //ricerca per tempo
            if (iod.getInfo().getTime_stamp_min().equals("") == false && iod.getInfo().getSensor_id().isEmpty() && iod.getInfo().getCoordinate().isEmpty() == true) {
                logger.debug("gobs");
                if (iod.getInfo().getTime_stamp_max().equals("") == false) {
                logger.debug("gobs: tmin:"+iod.getInfo().getTime_stamp_min()+" tmax:"+iod.getInfo().getTime_stamp_max());
                    GetObservationBufferData gobd = new GetObservationBufferData();
                    for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {

                        query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE `observation`.`time_stamp`>= TIMESTAMP('" + iod.getInfo().getTime_stamp_min() + "') AND `observation`.`time_stamp`<= TIMESTAMP('" + iod.getInfo().getTime_stamp_max() + "') AND `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "' ");
                     }
                } else {
                    logger.debug("gobs");
                    for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {

                        //se non c'è un intervallo di tempo massimo
                        query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE  `observation`.`time_stamp`>= TIMESTAMP('" + iod.getInfo().getTime_stamp_min() + "') AND `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "'");
                    }
                }
            }
            //ricerca per luogo
            if (iod.getInfo().getTime_stamp_min().equals("") == true && iod.getInfo().getSensor_id().isEmpty() && iod.getInfo().getCoordinate().isEmpty() == false) {
                logger.debug("gobs");
                GetObservationBufferData gobd = new GetObservationBufferData();
                for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {
                    query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE MBRContains(GeomFromText(AsText( Envelope( GeomFromText( 'LineString(" + iod.getInfo().getCoordinate().elementAt(0) + " " + iod.getInfo().getCoordinate().elementAt(1) + "," + iod.getInfo().getCoordinate().elementAt(2) + " " + iod.getInfo().getCoordinate().elementAt(3) + ")')))),`observation`.`coordinate`)='1' AND `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "' ");
                    

                }
            }
            // ricerca per sensore
            if (iod.getInfo().getTime_stamp_min().equals("") == true && iod.getInfo().getSensor_id().isEmpty() == false && iod.getInfo().getCoordinate().isEmpty() == true) {
                logger.debug("gobs");
                GetObservationBufferData gobd = new GetObservationBufferData();
                for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {
                    for (int j = 0; j < iod.getInfo().getSensor_id().size(); j++) {
                        query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "' AND `sensor`.`unique_id` LIKE '" + iod.getInfo().getSensor_id().elementAt(j) + "'");
                    }
                }
            }
            // ricerca per phenomena
            if (iod.getInfo().getTime_stamp_min().equals("") == true && iod.getInfo().getSensor_id().isEmpty() == true && iod.getInfo().getCoordinate().isEmpty() == true) {
                logger.debug("gobs");
                GetObservationBufferData gobd = new GetObservationBufferData();
                for (int i = 0; i < iod.getInfo().getObsPhenomena().size(); i++) {

                    query.add("SELECT `sensor`.`unique_id`, `phenomenon`.`unique_id`, `observation`.`time_stamp`, AsText(`observation`.`coordinate`) , `observation`.`uom_code`, `observation`.`value`, `time_definition`,`lat_definition`, `long_definition` , `phenomenon_description`,`long_def_uom`, `lat_def_uom` FROM `observation`,`sensor`,`phenomenon` WHERE `observation`.`sensor_id`=`sensor`.`sensor_id` AND `observation`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + iod.getInfo().getObsPhenomena().elementAt(i) + "'");
                }
            }
            db = Database.getNewInstance();
            boolean rsconsistent=true;
            for (int i = 0; i < query.size(); i++) {
                rsconsistent=true;
                logger.debug("gobs query:"+query.elementAt(i));
                if (db==null)
                    logger.debug("dbvuoto");
                ResultSet rs = db.exQuery(query.elementAt(i));
                GetObservationBufferData gobd = new GetObservationBufferData();
                try{
                    logger.debug("gobs:"+(rs.isBeforeFirst()?"prima della prima":"non è prima della prima"));
                    logger.debug("gobs:"+(rs.isAfterLast()?"dopo dell' ultima":"non è dopo dell' ultima"));
                    
                }
                catch(SQLException e){
                    logger.debug("si è generata un eccezione nella gestione del resultset:"+e.getSQLState()+"|"+e.getErrorCode()+"|"+e.getMessage()+"|"+e.getStackTrace()[e.getStackTrace().length-1].toString());
                    try{
                        
                        this.wait(120000);
                        rs = db.exQuery(query.elementAt(i));
                        logger.debug("gobs:"+(rs.isBeforeFirst()?"prima della prima":"non è prima della prima"));
                        logger.debug("gobs:"+(rs.getString("phenomenon_description")));
                    }
                    catch(Exception ex)
                    {
                        logger.error("eccezione getobservation",ex);
                        rsconsistent=false;
                    }
                    
                }
                boolean testval=rs.next();
                //logger.debug("gobspostexception "+rsconsistent+" "+testval);
                
                while (testval&&rsconsistent) {
                    //System.out.println("\n "+rs.getString(1)+","+rs.getString(2)+","+rs.getString(3)+","+rs.getString(4)+","+rs.getString(5)+","+rs.getString(6)+","+rs.getString(7)+","+rs.getString(8)+","+rs.getString(9)+";");
                    //aggiunta sensor id campo 1
                    //logger.debug("gobs:qui entro");
                    GetObservationValueBufferData valtemp = new GetObservationValueBufferData();
                    GetObservationFieldBufferData gofd = new GetObservationFieldBufferData();
                    gofd.setdef(rs.getString("long_definition"));
                    gofd.setname("longitude");
                    gofd.setuom(rs.getString("long_def_uom"));
                    valtemp.getfield().add(gofd);
                    GetObservationFieldBufferData gofd1 = new GetObservationFieldBufferData();
                    gofd1.setdef(rs.getString("lat_definition"));
                    gofd1.setname("latitude");
                    gofd1.setuom(rs.getString("lat_def_uom"));
                    valtemp.getfield().add(gofd1);
                    GetObservationFieldBufferData gofd2 = new GetObservationFieldBufferData();
                    gofd2.setdef(rs.getString("time_definition"));
                    gofd2.setname("time");
                    valtemp.getfield().add(gofd2);
                    GetObservationFieldBufferData gofd3 = new GetObservationFieldBufferData();
                    gofd3.setdef(rs.getString(2));
                    gofd3.setname(rs.getString("phenomenon_description"));
                    gofd3.setuom(rs.getString("uom_code"));
                    valtemp.getfield().add(gofd3);
                    valtemp.setsens_id(rs.getString(1));
                    valtemp.setphen_id(rs.getString(2));
                    valtemp.setvalue(rs.getString(6));
                    //            System.out.println("\n prova coord:" +rs.getString(4).split("\\)")[0].split("\\(")[1].split(" ")[1]);
                    valtemp.setlong(rs.getString(4).split("\\)")[0].split("\\(")[1].split(" ")[1]);
                    valtemp.setlat(rs.getString(4).split("\\)")[0].split("\\(")[1].split(" ")[0]);
                    valtemp.settime(rs.getString(3));
                    gobd.getvalues().add(valtemp);
                    //logger.debug("gobs valtemp:"+valtemp);
                    testval=rs.next();
                }
                gobd_all.add(gobd);
            }


            for (int i = 0; i < gobd_all.size(); i++) {
                for (int k = 0; k < gobd_all.elementAt(i).getvalues().size(); k++) {
                    String query_time = "SELECT max( `observation`.`time_stamp` ) , min( `observation`.`time_stamp` ) FROM `observation` , `sensor` , `phenomenon` WHERE  `observation`.`sensor_id` = `sensor`.`sensor_id` AND `observation`.`phenomenon_id` = `phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + gobd_all.elementAt(i).getvalues().elementAt(k).getphen_id() + "' AND `sensor`.`unique_id`LIKE '" + gobd_all.elementAt(i).getvalues().elementAt(k).getsens_id() + "'";
                    ResultSet rs = db.exQuery(query_time);
                    logger.debug(query_time);
                    if (rs.next()) {
                        gobd_all.elementAt(i).getvalues().elementAt(k).setmaxtime(rs.getString(1));
                        gobd_all.elementAt(i).getvalues().elementAt(k).setmintime(rs.getString(2));
                        //logger.debug("gobsiteration");
                    }
                }
            }
            //logger.debug("gobs");
            db.getCon().close();
        }



    }

    public void printInfo() {
        logger.debug("\noffering: "+iod.getInfo().getOffering());
        logger.debug("intervallo di tempo inizio: "+iod.getInfo().getTime_stamp_min());
        logger.debug("intervallo di tempo fine: "+iod.getInfo().getTime_stamp_max());
        logger.debug("\nproprietà geometrica: "+iod.getInfo().getGeom_property());
        logger.debug("\ntipo geometrico area: "+iod.getInfo().getGeom_type());
        logger.debug("\ncoordinate: ");        
        for (int i=0; i<iod.getInfo().getCoordinate().size();i++)
            logger.debug(" "+iod.getInfo().getCoordinate().elementAt(i));
        for (int i=0; i<iod.getInfo().getSensor_id().size();i++)
            logger.debug("\nsensore: "+iod.getInfo().getSensor_id().elementAt(i));
        for (int i=0; i<iod.getInfo().getObsPhenomena().size();i++)
            logger.debug("\nphenomena da misurare: "+iod.getInfo().getObsPhenomena().elementAt(i));
         
    }
}
