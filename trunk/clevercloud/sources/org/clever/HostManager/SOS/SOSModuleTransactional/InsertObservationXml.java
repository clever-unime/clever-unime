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

import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.SensorData;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Sensor_Struct;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.clever.HostManager.SOS.ParameterContainer;
import org.w3c.dom.*;

/**
 *
 * @author user
 */
public class InsertObservationXml {

    private String id;
    private Sensor_Struct sens_nodeid;
    private SOSmodule sosModule;
    private Logger logger;
    private ParameterContainer parameterContainer;

    public InsertObservationXml(Sensor_Struct sens_nodeid, String id, SOSmodule sosModule) {
        this.parameterContainer = ParameterContainer.getInstance();
        logger = parameterContainer.getLogger();
        this.id = id;
        this.sosModule = sosModule;
        this.sens_nodeid = sens_nodeid;

    }

    public void writexml(String temper, String phen) {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            SensorData ss = new SensorData();

            Element root = doc.createElement("InsertObservation");
            //root.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sensorML/1.0.1 http://schemas.opengis.net/sensorML/1.0.1/sensorML.xsd");
            //version="1.0.1" xmlns="http://www.opengis.net/sensorML/1.0.1"
            root.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosAll.xsd");

            root.setAttribute("xmlns", "http://www.opengis.net/sos/1.0");

            root.setAttribute("xmlns:swe", "http://www.opengis.net/swe/1.0.1");
            root.setAttribute("xmlns:sml", "http://www.opengis.net/sensorML/1.0.1");
            root.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
            root.setAttribute("xmlns:om", "http://www.opengis.net/om/1.0");
            root.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            //Documento
            Element sens_id = doc.createElement("AssignedSensorId");
            Text idvaluetext = doc.createTextNode(sens_nodeid.getSensor_info().gettype_id() + "" + id);
            sens_id.appendChild(idvaluetext);
            root.appendChild(sens_id);

            Element obs = doc.createElement("om:Observation");
            Element st = doc.createElement("om:samplingTime");
            Element ti = doc.createElement("gml:TimeInstant");
            Element tp = doc.createElement("gml:timePosition");

            GregorianCalendar gc = new GregorianCalendar();
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            Text tpt = doc.createTextNode(sdf.format(now));
            //Text tpt=doc.createTextNode(""+gc.get(GregorianCalendar.YEAR)+"-"+(gc.get(GregorianCalendar.MONTH)+1)+"-"+gc.get(GregorianCalendar.DAY_OF_MONTH)+"T"+gc.get(GregorianCalendar.HOUR)+":"+gc.get(GregorianCalendar.MINUTE)+":"+gc.get(GregorianCalendar.SECOND)+"Z");
            tp.appendChild(tpt);
            ti.appendChild(tp);
            st.appendChild(ti);
            obs.appendChild(st);

            Element proc = doc.createElement("om:procedure");
            proc.setAttribute("xlink:href", sens_nodeid.getSensor_info().gettype_id() + "" + id);
            obs.appendChild(proc);
            int flag = 0;
            Element op = doc.createElement("om:observedProperty");
            for (int j = 0; j < sens_nodeid.getSensor_Phenomena().size(); j++) {
                if (sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_descr().contains(phen)) {
                    flag = 1;
                    op.setAttribute("xlink:href", "urn:ogc:def:property:OGC:" + sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_id());
                }
            }
            //se non è stato trovato alcun phenomeno associato al sensore che corrisponda alla misura indicata nella struttura del pacchetto si esce
            if (flag == 0) {
                return;
            }
            obs.appendChild(op);
            Element foi = doc.createElement("om:featureOfInterest");
            foi.setAttribute("xlink:href", "urn:ogc:def:feature:OGC-SWE:3:transient");
            obs.appendChild(foi);
            Element sdr = doc.createElement("swe:SimpleDataRecord");
            Element rs = doc.createElement("om:result");
            Element ft = doc.createElement("swe:field");
            ft.setAttribute("name", "time");
            Element tm = doc.createElement("swe:Time");
            tm.setAttribute("definition", "urn:ogc:property:time:iso8601");
            Element val = doc.createElement("swe:value");

            Text valt = doc.createTextNode(sdf.format(now));

            //Text valt=doc.createTextNode(""+gc.get(GregorianCalendar.YEAR)+"-"+(gc.get(GregorianCalendar.MONTH)+1)+"-"+gc.get(GregorianCalendar.DAY_OF_MONTH)+"T"+gc.get(GregorianCalendar.HOUR)+":"+gc.get(GregorianCalendar.MINUTE)+":"+gc.get(GregorianCalendar.SECOND)+"Z");
            val.appendChild(valt);
            tm.appendChild(val);
            ft.appendChild(tm);
            sdr.appendChild(ft);

            Element fl = doc.createElement("swe:field");
            fl.setAttribute("name", "longitude");
            Element tl = doc.createElement("swe:Quantity");
            tl.setAttribute("definition", "urn:ogc:property:location:EPSG:4326:longitude");
            Element uoml = doc.createElement("swe:uom");
            uoml.setAttribute("code", sens_nodeid.getSensor_info().getlong_uom());
            Element vall = doc.createElement("swe:value");
            Text vallt = doc.createTextNode(sens_nodeid.getSensor_info().getlong_val());
            vall.appendChild(vallt);
            tl.appendChild(uoml);
            tl.appendChild(vall);
            fl.appendChild(tl);
            sdr.appendChild(fl);

            Element fla = doc.createElement("swe:field");
            fla.setAttribute("name", "latitude");
            Element tla = doc.createElement("swe:Quantity");
            tla.setAttribute("definition", "urn:ogc:property:location:EPSG:4326:latitude");
            Element uomla = doc.createElement("swe:uom");
            uomla.setAttribute("code", sens_nodeid.getSensor_info().getlat_uom());
            Element valla = doc.createElement("swe:value");
            Text vallta = doc.createTextNode(sens_nodeid.getSensor_info().getlat_val());
            valla.appendChild(vallta);
            tla.appendChild(valla);
            tla.appendChild(uomla);
            fla.appendChild(tla);
            sdr.appendChild(fla);
            for (int j = 0; j < sens_nodeid.getSensor_Phenomena().size(); j++) {
                if (sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_descr().contains(phen)) {

                    Element flp = doc.createElement("swe:field");
                    flp.setAttribute("name", sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_id().trim().split(":")[1]);
                    Element tlf = doc.createElement("swe:Quantity");
                    tlf.setAttribute("definition", "urn:ogc:def:property:OGC:" + sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_id());
                    Element uomlf = doc.createElement("swe:uom");
                    uomlf.setAttribute("code", sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_uom());
                    Element vallf = doc.createElement("swe:value");
                    Text valltf = doc.createTextNode(ss.getData(temper, phen));
                    vallf.appendChild(valltf);
                    tlf.appendChild(uomlf);
                    tlf.appendChild(vallf);
                    flp.appendChild(tlf);
                    sdr.appendChild(flp);

                }
            }
            rs.appendChild(sdr);
            obs.appendChild(rs);
            root.appendChild(obs);
            doc.appendChild(root);







            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);

            logger.info("new observation");

            sosModule.SOSservice(stringWriter.getBuffer().toString());
        } catch (ParserConfigurationException ex) {
            logger.error("InsertObservation: ParserConfigurationException " + ex);
        } catch (TransformerConfigurationException ex) {
            logger.error("InsertObservation: TransformerConfigurationException " + ex);
        } catch (TransformerException ex) {
            logger.error("InsertObservation: TransformerException " + ex);
        }


    }
    
    public void writexml(String temper, String phen,String sdf) {

        try {
            logger.debug("writexml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
            SensorData ss = new SensorData();

            Element root = doc.createElement("InsertObservation");
            //root.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sensorML/1.0.1 http://schemas.opengis.net/sensorML/1.0.1/sensorML.xsd");
            //version="1.0.1" xmlns="http://www.opengis.net/sensorML/1.0.1"
            root.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosAll.xsd");

            root.setAttribute("xmlns", "http://www.opengis.net/sos/1.0");

            root.setAttribute("xmlns:swe", "http://www.opengis.net/swe/1.0.1");
            root.setAttribute("xmlns:sml", "http://www.opengis.net/sensorML/1.0.1");
            root.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
            root.setAttribute("xmlns:om", "http://www.opengis.net/om/1.0");
            root.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            //Documento
            Element sens_id = doc.createElement("AssignedSensorId");
            /*precedente implementazione probabile errore concettuale non andrebbe inserito l'elemento id
            Text idvaluetext = doc.createTextNode(sens_nodeid.getSensor_info().gettype_id() + "" + id);*/
            Text idvaluetext = doc.createTextNode(sens_nodeid.getSensor_info().gettype_id()); 
            sens_id.appendChild(idvaluetext);
            root.appendChild(sens_id);
            
            Element obs = doc.createElement("om:Observation");
            Element st = doc.createElement("om:samplingTime");
            Element ti = doc.createElement("gml:TimeInstant");
            Element tp = doc.createElement("gml:timePosition");

            GregorianCalendar gc = new GregorianCalendar();
            Text tpt = doc.createTextNode(sdf);
            tp.appendChild(tpt);
            ti.appendChild(tp);
            st.appendChild(ti);
            obs.appendChild(st);
            
            Element proc = doc.createElement("om:procedure");
            proc.setAttribute("xlink:href", sens_nodeid.getSensor_info().gettype_id() + "" + id);
            obs.appendChild(proc);
            int flag = 0;
            
            Element op = doc.createElement("om:observedProperty");
            for (int j = 0; j < sens_nodeid.getSensor_Phenomena().size(); j++) {
                //logger.debug("struttura:"+sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_descr()+" phen:"+phen);
                if (sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_descr().contains(phen)) {
                    logger.debug("Match!");
                    flag = 1;
                    op.setAttribute("xlink:href", "urn:ogc:def:property:OGC:" + sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_id());
                }
            }
            //se non è stato trovato alcun phenomeno associato al sensore che corrisponda alla misura indicata nella struttura del pacchetto si esce
            if (flag == 0) {
                return;
            }
            //logger.debug("inxml1");
            obs.appendChild(op);
            Element foi = doc.createElement("om:featureOfInterest");
            foi.setAttribute("xlink:href", "urn:ogc:def:feature:OGC-SWE:3:transient");
            obs.appendChild(foi);
            Element sdr = doc.createElement("swe:SimpleDataRecord");
            Element rs = doc.createElement("om:result");
            Element ft = doc.createElement("swe:field");
            ft.setAttribute("name", "time");
            Element tm = doc.createElement("swe:Time");
            tm.setAttribute("definition", "urn:ogc:property:time:iso8601");
            Element val = doc.createElement("swe:value");

            Text valt = doc.createTextNode(sdf);

            //Text valt=doc.createTextNode(""+gc.get(GregorianCalendar.YEAR)+"-"+(gc.get(GregorianCalendar.MONTH)+1)+"-"+gc.get(GregorianCalendar.DAY_OF_MONTH)+"T"+gc.get(GregorianCalendar.HOUR)+":"+gc.get(GregorianCalendar.MINUTE)+":"+gc.get(GregorianCalendar.SECOND)+"Z");
            val.appendChild(valt);
            tm.appendChild(val);
            ft.appendChild(tm);
            sdr.appendChild(ft);
            Element fl = doc.createElement("swe:field");
            fl.setAttribute("name", "longitude");
            Element tl = doc.createElement("swe:Quantity");
            tl.setAttribute("definition", "urn:ogc:property:location:EPSG:4326:longitude");
            Element uoml = doc.createElement("swe:uom");
            uoml.setAttribute("code", sens_nodeid.getSensor_info().getlong_uom());
            Element vall = doc.createElement("swe:value");
            Text vallt = doc.createTextNode(sens_nodeid.getSensor_info().getlong_val());
            vall.appendChild(vallt);
            tl.appendChild(uoml);
            tl.appendChild(vall);
            fl.appendChild(tl);
            sdr.appendChild(fl);
            Element fla = doc.createElement("swe:field");
            fla.setAttribute("name", "latitude");
            Element tla = doc.createElement("swe:Quantity");
            tla.setAttribute("definition", "urn:ogc:property:location:EPSG:4326:latitude");
            Element uomla = doc.createElement("swe:uom");
            uomla.setAttribute("code", sens_nodeid.getSensor_info().getlat_uom());
            Element valla = doc.createElement("swe:value");
            Text vallta = doc.createTextNode(sens_nodeid.getSensor_info().getlat_val());
            valla.appendChild(vallta);
            tla.appendChild(valla);
            tla.appendChild(uomla);
            fla.appendChild(tla);
            sdr.appendChild(fla);
            //logger.debug("inxml2");
            for (int j = 0; j < sens_nodeid.getSensor_Phenomena().size(); j++) {
                if (sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_descr().contains(phen)) {
                    //logger.debug("inxml3"+j);
                    Element flp = doc.createElement("swe:field");
                    //logger.debug("inxml3a"+j+"   "+sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_id());
                    flp.setAttribute("name", sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_id().trim().split(":")[1]);
                    Element tlf = doc.createElement("swe:Quantity");
                    //logger.debug("inxml3b"+j);
                    tlf.setAttribute("definition", "urn:ogc:def:property:OGC:" + sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_id());
                    Element uomlf = doc.createElement("swe:uom");
                    //logger.debug("inxml3c"+j);
                    uomlf.setAttribute("code", sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_uom());
                    Element vallf = doc.createElement("swe:value");
                    Text valltf = doc.createTextNode(ss.getData(temper, phen));
                    vallf.appendChild(valltf);
                    tlf.appendChild(uomlf);
                    tlf.appendChild(vallf);
                    flp.appendChild(tlf);
                    sdr.appendChild(flp);
                    //logger.debug("inxml3d"+j);
                }
            }
            rs.appendChild(sdr);
            obs.appendChild(rs);
            root.appendChild(obs);
            doc.appendChild(root);
            //logger.debug("inxml4");






            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);

            logger.debug("new observation");

            sosModule.SOSservice(stringWriter.getBuffer().toString());
        } catch (ParserConfigurationException ex) {
            logger.error("InsertObservation: ParserConfigurationException " + ex);
        } catch (TransformerConfigurationException ex) {
            logger.error("InsertObservation: TransformerConfigurationException " + ex);
        } catch (TransformerException ex) {
            logger.error("InsertObservation: TransformerException " + ex);
        }catch(Exception e){
            logger.error("error occurred in function writexml:"+e.getMessage()+e.getStackTrace()[0]+e.getStackTrace()[1]);
        }


    }
}
