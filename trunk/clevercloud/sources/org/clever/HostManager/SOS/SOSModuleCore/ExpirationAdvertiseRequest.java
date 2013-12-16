/*
 * The MIT License
 *
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
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;

/**
 *
 * @author user
 */
public class ExpirationAdvertiseRequest {

    private Database db;
    private ParameterContainer parameterContainer = null;
    //private String filename_output;
    private String phen_id;

    public ExpirationAdvertiseRequest(String expirationAdvertiseRequest) throws ParserConfigurationException, SAXException, IOException {
        parameterContainer = ParameterContainer.getInstance();
        //db=new DataBase();

        //db.openDB(this.parameterContainer.getDbServer(),this.parameterContainer.getDbDriver(),this.parameterContainer.getDbName(),
        //         this.parameterContainer.getDbUsername(),this.parameterContainer.getDbPassword());


        this.phen_id = "error";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        //File xmlFilereg = new File(filename);
        Document document = builder.parse(new ByteArrayInputStream(expirationAdvertiseRequest.getBytes()));
        parseinput(document);


        /*this.filename_output=filename_output;
        if(this.filename_output.equals(""))
        this.filename_output="/home/user/phenomenon_advertise.xml";*/
    }

    public ArrayList<String> sendAdvertise() throws ParserConfigurationException, SQLException, TransformerConfigurationException, TransformerException {
        db = Database.getNewInstance();
        ArrayList phenomenonAdvertisements = new ArrayList();
        if (phen_id.equals("")) {
            Vector<String> all_id = new Vector<String>(1);
            String phen = "SELECT `unique_id` FROM `phenomenon` WHERE 1";
            ResultSet rs = db.exQuery(phen);
            while (rs.next()) {

                all_id.add(rs.getString(1));
            }
            for (int i = 0; i < all_id.size(); i++) {
                phenomenonAdvertisements.add(phenomenonAdvertiseXml(all_id.elementAt(i)));

            }


        } else {
            if (phen_id.equals("error")) {
                System.out.println("ExpirationAdvertiseRequest error in parsing arguments");
                //return;
            } else {
                phenomenonAdvertisements.add(phenomenonAdvertiseXml(phen_id));
            }
        }

        db.getCon().close();
        return phenomenonAdvertisements;
    }

    public String phenomenonAdvertiseXml(String phen_unique_id) throws ParserConfigurationException, SQLException, TransformerConfigurationException, TransformerException {

        db = Database.getNewInstance();
        DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder2 = dbf2.newDocumentBuilder();
        Document doc = builder2.newDocument();
        //db.openDB(this.parameterContainer.getDbServer(),this.parameterContainer.getDbDriver(),this.parameterContainer.getDbName(),
        //           this.parameterContainer.getDbUsername(),this.parameterContainer.getDbPassword());

        //File file = new File(this.filename_output);
        Element root = doc.createElement("PhenomenonAdvertise");
        //root.setAttribute("xmlns","http://www.opengis.net/sas");
        root.setAttribute("xsi:schemaLocation", "http://www.opengis.net/sas ../sasAll.xsd");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:swe", "http://www.opengis.net/swe");
        root.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
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
        } else {
            System.out.println("Phenomenon " + phen_unique_id + " not available");
            return "";
        }
        root.appendChild(foi);

        Element area = doc.createElement("OperationArea");
        Element geo = doc.createElement("swe:GeoLocation");
        String sel_area = "SELECT max(`longitude`), max(`latitude`), max(`altitude`),min(`longitude`), min(`latitude`), min(`altitude`) FROM `sensor`, `phenomenon`,`sens_phen` WHERE `sens_phen`.`sensor_id`=`sensor`.`sensor_id` AND `sens_phen`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + phen_unique_id + "'";
        //System.out.println(sel_area);
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
                Text lowmaxtext = doc.createTextNode(rs.getString(4) + " " + rs.getString(5));
                lowmax.appendChild(lowmaxtext);
                Element lowmin = doc.createElement("gml:upperCorner");
                Text lowmintext = doc.createTextNode(rs.getString(1) + " " + rs.getString(2));
                lowmin.appendChild(lowmintext);
                env.appendChild(lowmin);
                env.appendChild(lowmax);
                bounded.appendChild(env);
                geo.appendChild(bounded);





            }
        }
        area.appendChild(geo);
        root.appendChild(area);
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
        String sel_freq = "SELECT max(`frequency`) FROM `phenomenon`,`sensor`, `sens_phen` WHERE  `sens_phen`.`sensor_id`=`sensor`.`sensor_id` AND `sens_phen`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phenomenon`.`unique_id` LIKE '" + phen_unique_id + "'";
        rs = db.exQuery(sel_freq);
        if (rs.next()) {
            Text altfreqtext = doc.createTextNode(rs.getString(1));
            alertfreq.appendChild(altfreqtext);
        }
        root.appendChild(alertfreq);
        Element timeexp = doc.createElement("DesiredPublicationExpiration");
        Text timeexptext = doc.createTextNode("000-00-00T00:00:00Z");
        timeexp.appendChild(timeexptext);
        root.appendChild(timeexp);
        doc.appendChild(root);



        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StringWriter stringWriter = new StringWriter();
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);

        db.getCon().close();

        return stringWriter.getBuffer().toString();


    }

    public void parseinput(Node currentNode) {
        short sNodeType = currentNode.getNodeType();
        //Se è di tipo Element ricavo le informazioni e le stampo
        if (sNodeType == Node.ELEMENT_NODE) {
            String sNodeName = currentNode.getNodeName();
            //per ogni componente 
            if (sNodeName.equals("Content")) {
                NamedNodeMap nnmAttributes = currentNode.getAttributes();
                //System.out.println("Content:"+utils.printAttributes(nnmAttributes));
                if (utils.printAttributes(nnmAttributes).indexOf("definition=; uom=;") != -1) {
                    this.phen_id = "";
                } else {
                    this.phen_id = utils.printAttributes(nnmAttributes).split(";")[0].split("=")[1];
                }

                //System.out.println("phen:"+this.phen_id);
                currentNode = currentNode.getNextSibling();
            }
        }
        int iChildNumber = currentNode.getChildNodes().getLength();
        //Se non si tratta di una foglia continua l'esplorazione
        if (currentNode.hasChildNodes()) {
            NodeList nlChilds = currentNode.getChildNodes();
            for (int iChild = 0; iChild < iChildNumber; iChild++) {
                parseinput(nlChilds.item(iChild));
            }
        }

    }
}
