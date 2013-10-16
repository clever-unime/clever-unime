/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleCore;

import org.clever.HostManager.SOS.Database;
import org.clever.HostManager.SOS.ParameterContainer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class GetCapabilities {

    DocumentBuilderFactory dbf2;
    DocumentBuilder builder2;
    Document doc;
    private Database db;
    Vector<String> section;
    String filename_output;
    private ParameterContainer parameterContainer = null;
    //"/home/user/file_capabilities.xml"
    /*GetCapabilities(String filename) throws ParserConfigurationException, SAXException, IOException{
    db=new DataBase();
    db.openDB("127.0.0.1", "com.mysql.jdbc.Driver", "sensorml");
    dbf2 = DocumentBuilderFactory.newInstance();
    builder2 = dbf2.newDocumentBuilder();
    doc = builder2.newDocument();
    section=new Vector<String>(1);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = dbf.newDocumentBuilder();
    File xmlFilereg = new File(filename);
    Document document = builder.parse(xmlFilereg);
    parseinput(document);
    this.filename_output="/home/user/file_capabilities.xml";
    }*/

    public GetCapabilities(String getCapabilitiesRequest) throws ParserConfigurationException, SAXException, IOException {
        parameterContainer = ParameterContainer.getInstance();
        //db=new DataBase();
        //db.openDB(this.parameterContainer.getDbServer(),this.parameterContainer.getDbDriver(),this.parameterContainer.getDbName(),
        //         this.parameterContainer.getDbUsername(),this.parameterContainer.getDbPassword());
        Database db = Database.getInstance();
        dbf2 = DocumentBuilderFactory.newInstance();
        builder2 = dbf2.newDocumentBuilder();
        doc = builder2.newDocument();
        section = new Vector<String>(1);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        //File xmlFilereg = new File(getObservationRequest);
        Document document = builder.parse(new ByteArrayInputStream(getCapabilitiesRequest.getBytes()));
        parseinput(document);
        /*this.filename_output=filename_output;
        if(this.filename_output.equals(""))
        this.filename_output=System.getProperty("user.home")+"/file_capabilities.xml";*/
    }

    public void parseinput(Node node) {
        //section.add("OperationsMetadata");
        int flag = 0;
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        do {

            Node currentNode = tovisit.lastElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    //per ogni componente 
                    if (sNodeName.equals("ows:Section")) {
                        section.add(utils.searchTextInElement(currentNode));
                        flag = 1;
                        // currentNode=currentNode.getNextSibling();
                    }


                }
                int iChildNumber = currentNode.getChildNodes().getLength();

                if (currentNode.hasChildNodes()) {
                    NodeList nlChilds = currentNode.getChildNodes();
                    for (int iChild = 0; iChild < iChildNumber; iChild++) {
                        tovisit.add(nlChilds.item(iChild));

                    }

                }
                visited.add(currentNode);
                tovisit.remove(currentNode);
            }
        } while (tovisit.isEmpty() == false);

        if (flag != 1) {
            section.add("All");
        }
    }

    public String write_capabilities_xml() throws TransformerConfigurationException, TransformerException, SQLException {
        Element root = doc.createElement("GetCapabilities");
        root.setAttribute("xmlns", "http://www.opengis.net/sos/1.0");
        root.setAttribute("xmlns:ows", "http://www.opengis.net/ows/1.1");
        root.setAttribute("xmlns:ogc", "http://www.opengis.net/ogc");
        root.setAttribute("xmlns:swe", "http://www.opengis.net/swe/1.0.1");
        root.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
        root.setAttribute("xmlns:om", "http://www.opengis.net/om/1.0");
        root.setAttribute("xmlns:sml", "http://www.opengis.net/sensorML/1.0.1");
        root.setAttribute("xmlns:myorg", "http://www.myorg.org/features");
        root.setAttribute("xsi:schemaLocation", "http://www.opengis.net/om/1.0 http://schemas.opengis.net/sos/1.0.0 http://shemas.opengis.net/sos/1.0.1/sosGetCapabilities.xsd");
        root.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("version", "1.0.0");

        if (section.contains("ServiceIdentification") || section.contains("All")) {
            Element ServiceId = doc.createElement("ows:ServiceIdentification");
            Element Stitle = doc.createElement("ows:Title");
            Text titletext = doc.createTextNode("SOS Service");
            Stitle.appendChild(titletext);
            ServiceId.appendChild(Stitle);
            String query = "SELECT `offering_name` FROM `offering` WHERE 1";
            ResultSet rs = db.exQuery(query);
            String value = "";
            if (rs.next()) {
                value = "" + rs.getString(1);
                while (rs.next()) {
                    value = "" + rs.getString(1) + "," + value;

                }
            }
            Element key = doc.createElement("ows:Keyword");
            Text keyt = doc.createTextNode(value);
            key.appendChild(keyt);
            ServiceId.appendChild(key);

            Element servtype = doc.createElement("ows:ServiceType");
            servtype.setAttribute("codeSpace", "http://opengeospatial.net");
            Text servtypet = doc.createTextNode("OGC:SOS");
            servtype.appendChild(servtypet);
            ServiceId.appendChild(servtype);
            Element servtypev = doc.createElement("ows:ServiceTypeVersion");
            Text servtypetv = doc.createTextNode("0.0.0");
            servtypev.appendChild(servtypetv);
            ServiceId.appendChild(servtypev);
            Element fees = doc.createElement("ows:Fees");
            Text feest = doc.createTextNode("NONE");
            fees.appendChild(feest);
            ServiceId.appendChild(fees);
            Element aees = doc.createElement("ows:AccessConstraints");
            Text aeest = doc.createTextNode("NONE");
            aees.appendChild(aeest);
            ServiceId.appendChild(aees);
            root.appendChild(ServiceId);

        }
        if (section.contains("Serviceprovider") || section.contains("All")) {
            Element servicePr = doc.createElement("ows:ServiceProvider");
            Element prname = doc.createElement("ows:ProviderName");
            Text prnamet = doc.createTextNode("MyOrg");
            prname.appendChild(prnamet);
            root.appendChild(prname);
            root.appendChild(servicePr);
        }
        if (section.contains("OperationsMetadata") || section.contains("All")) {
            Element OperationM = doc.createElement("ows:OperationsMetadata");
            Element op1 = doc.createElement("ows:Operation");
            op1.setAttribute("name", "GetCapabilities");
            Element op1dcp = doc.createElement("ows:DCP");
            Element op1http = doc.createElement("ows:HTTP");
            Element op1get = doc.createElement("ows:Get");
            Element op1post = doc.createElement("ows:Post");
            Text op1postt = doc.createTextNode("");
            Text op1gett = doc.createTextNode("");
            op1post.appendChild(op1postt);
            op1get.appendChild(op1gett);
            op1http.appendChild(op1post);
            op1http.appendChild(op1get);
            op1dcp.appendChild(op1http);
            op1.appendChild(op1dcp);
            Element par = doc.createElement("ows:Parameter");
            par.setAttribute("name", "Sections");
            Element all = doc.createElement("ows:AllowedValues");
            Element val1 = doc.createElement("ows:Value");
            Text val1t = doc.createTextNode("ServiceIdentification");
            val1.appendChild(val1t);
            all.appendChild(val1);
            Element val2 = doc.createElement("ows:Value");
            Text val2t = doc.createTextNode("ServiceProvider");
            val2.appendChild(val2t);
            all.appendChild(val2);
            Element val3 = doc.createElement("ows:Value");
            Text val3t = doc.createTextNode("OperationMetadata");
            val3.appendChild(val3t);
            all.appendChild(val3);
            Element val4 = doc.createElement("ows:Value");
            Text val4t = doc.createTextNode("Contents");
            val4.appendChild(val4t);
            all.appendChild(val4);
            Element val5 = doc.createElement("ows:Value");
            Text val5t = doc.createTextNode("Contents");
            val5.appendChild(val5t);
            all.appendChild(val5);
            Element val6 = doc.createElement("ows:Value");
            Text val6t = doc.createTextNode("Filter_Capabilities");
            val6.appendChild(val6t);
            all.appendChild(val6);
            Element val7 = doc.createElement("ows:Value");
            Text val7t = doc.createTextNode("All");
            val7.appendChild(val7t);
            all.appendChild(val7);
            par.appendChild(all);
            op1.appendChild(par);
            OperationM.appendChild(op1);


            Element op2 = doc.createElement("ows:Operation");
            op2.setAttribute("name", "GetObservation");
            Element op2dcp = doc.createElement("ows:DCP");
            Element op2http = doc.createElement("ows:HTTP");
            Element op2get = doc.createElement("ows:Get");
            Element op2post = doc.createElement("ows:Post");
            Text op2postt = doc.createTextNode("");
            Text op2gett = doc.createTextNode("");
            op2post.appendChild(op2postt);
            op2get.appendChild(op2gett);
            op2http.appendChild(op2post);
            op2http.appendChild(op2get);
            op2dcp.appendChild(op2http);
            op2.appendChild(op2dcp);
            OperationM.appendChild(op2);

            Element op3 = doc.createElement("ows:Operation");
            op3.setAttribute("name", "RegisterSensor");
            Element op3dcp = doc.createElement("ows:DCP");
            Element op3http = doc.createElement("ows:HTTP");
            Element op3get = doc.createElement("ows:Get");
            Element op3post = doc.createElement("ows:Post");
            Text op3postt = doc.createTextNode("");
            Text op3gett = doc.createTextNode("");
            op3post.appendChild(op3postt);
            op3get.appendChild(op3gett);
            op3http.appendChild(op3post);
            op3http.appendChild(op3get);
            op3dcp.appendChild(op3http);
            op3.appendChild(op3dcp);
            Element constr = doc.createElement("ows:Constraint");
            constr.setAttribute("name", "SupportedSensorDescription");
            Element allr = doc.createElement("ows:AllowedValues");
            Element val1r = doc.createElement("ows:Value");
            Text val1tr = doc.createTextNode("ServiceIdentification");
            val1r.appendChild(val1tr);
            allr.appendChild(val1r);
            constr.appendChild(allr);
            op3.appendChild(constr);
            OperationM.appendChild(op3);

            Element op4 = doc.createElement("ows:Operation");
            op4.setAttribute("name", "InsertObservation");
            Element op4dcp = doc.createElement("ows:DCP");
            Element op4http = doc.createElement("ows:HTTP");
            Element op4get = doc.createElement("ows:Get");
            Element op4post = doc.createElement("ows:Post");
            Text op4postt = doc.createTextNode("");
            Text op4gett = doc.createTextNode("");
            op4post.appendChild(op4postt);
            op4get.appendChild(op4gett);
            op4http.appendChild(op4post);
            op4http.appendChild(op4get);
            op4dcp.appendChild(op4http);
            op4.appendChild(op4dcp);
            OperationM.appendChild(op4);

            Element op5 = doc.createElement("ows:Operation");
            op5.setAttribute("name", "DescribeSensor");
            Element op5dcp = doc.createElement("ows:DCP");
            Element op5http = doc.createElement("ows:HTTP");
            Element op5get = doc.createElement("ows:Get");
            Element op5post = doc.createElement("ows:Post");
            Text op5postt = doc.createTextNode("");
            Text op5gett = doc.createTextNode("");
            op5post.appendChild(op5postt);
            op5get.appendChild(op5gett);
            op5http.appendChild(op5post);
            op5http.appendChild(op5get);
            op5dcp.appendChild(op5http);
            op5.appendChild(op5dcp);
            OperationM.appendChild(op5);

            Element parameter1 = doc.createElement("ows:Parameter");
            parameter1.setAttribute("name", "service");
            Element allp = doc.createElement("ows:AllowedValues");
            Element val1p = doc.createElement("ows:Value");
            Text val1tp = doc.createTextNode("SOS");
            val1p.appendChild(val1tp);
            allp.appendChild(val1p);
            parameter1.appendChild(allp);
            OperationM.appendChild(parameter1);

            Element parameter2 = doc.createElement("ows:Parameter");
            parameter2.setAttribute("name", "version");
            Element all2p = doc.createElement("ows:AllowedValues");
            Element val2p = doc.createElement("ows:Value");
            Text val2tp = doc.createTextNode("1.0.0");
            val2p.appendChild(val2tp);
            all2p.appendChild(val2p);
            parameter2.appendChild(all2p);
            OperationM.appendChild(parameter2);
            root.appendChild(OperationM);
        }
        if (section.contains("Contents") || section.contains("All")) {
            Element cont = doc.createElement("sos:Contents");
            Element offlist = doc.createElement("sos:ObservationOfferingList");
            String queryoff = "SELECT `offering_id`,`unique_id`,`offering_name` FROM `offering` WHERE 1";
            ResultSet rs = db.exQuery(queryoff);
            Vector<String> off_id = new Vector<String>(1);
            Vector<String> off_unique = new Vector<String>(1);
            Vector<String> off_name = new Vector<String>(1);
            while (rs.next()) {
                off_id.add(rs.getString(1));
                off_unique.add(rs.getString(2));
                off_name.add(rs.getString(3));

            }
            for (int i = 0; i < off_id.size(); i++) {
                Element offfield = doc.createElement("sos:ObservationOffering");
                offfield.setAttribute("gml:id", off_unique.elementAt(i));
                Element offname = doc.createElement("gml:name");
                Text offnamet = doc.createTextNode(off_name.elementAt(i));
                offname.appendChild(offnamet);
                offfield.appendChild(offname);
                Element samplingtime = doc.createElement("sos:time");
                Element bounded = doc.createElement("gml:boundedBy");

                String temp = "SELECT max( `observation`.`time_stamp` ),min( `observation`.`time_stamp` ), max(X(`observation`.`coordinate`)),max(Y(`observation`.`coordinate`)), min(X(`observation`.`coordinate`)), min(Y(`observation`.`coordinate`)) FROM `phen_off`, `phenomenon`,`observation` WHERE `phenomenon`.`phenomenon_id`=`phen_off`.`phenomenon_id` AND `phen_off`.`offering_id`='" + off_id.elementAt(i) + "' AND `observation`.`phenomenon_id`= `phenomenon`.`phenomenon_id`";
                //System.out.println(temp);
                rs = db.exQuery(temp);
                if (rs.next()) {
                    if (rs.getString(2) != null && rs.getString(1) != null) {
                        Element timeper = doc.createElement("gml:TimePeriod");
                        Element timemin = doc.createElement("gml:beginPosition");
                        Element timemax = doc.createElement("gml:endPosition");
                        Text timemintext = doc.createTextNode(rs.getString(2));
                        Text timemaxtext = doc.createTextNode(rs.getString(1));
                        timemin.appendChild(timemintext);
                        timemax.appendChild(timemaxtext);
                        timeper.appendChild(timemin);
                        timeper.appendChild(timemax);
                        samplingtime.appendChild(timeper);
                        Element env = doc.createElement("gml:Envelope");
                        env.setAttribute("srsName", "EPSG:4326");
                        Element lowmax = doc.createElement("gml:lowerCorner");
                        Text lowmaxtext = doc.createTextNode(rs.getString(5) + " " + rs.getString(6));
                        lowmax.appendChild(lowmaxtext);
                        Element lowmin = doc.createElement("gml:upperCorner");
                        Text lowmintext = doc.createTextNode(rs.getString(3) + " " + rs.getString(4));
                        lowmin.appendChild(lowmintext);
                        env.appendChild(lowmin);
                        env.appendChild(lowmax);
                        bounded.appendChild(env);
                    }
                }
                offfield.appendChild(bounded);
                offfield.appendChild(samplingtime);

                String phen = "SELECT `phenomenon`.`unique_id` FROM `phen_off`, `phenomenon` WHERE `phenomenon`.`phenomenon_id`=`phen_off`.`phenomenon_id` AND `phen_off`.`offering_id`='" + off_id.elementAt(i) + "'";
                rs = db.exQuery(phen);
                while (rs.next()) {
                    Element obspro = doc.createElement("sos:observedProperty");
                    obspro.setAttribute("xlink:href", rs.getString(1));
                    offfield.appendChild(obspro);
                }
                String sens = "SELECT `sensor`.`unique_id` FROM `sens_off`, `sensor` WHERE `sensor`.`sensor_id`=`sens_off`.`sensor_id` AND `sens_off`.`offering_id`='" + off_id.elementAt(i) + "'";
                rs = db.exQuery(sens);
                while (rs.next()) {
                    Element obspro = doc.createElement("sos:procedure");
                    obspro.setAttribute("xlink:href", rs.getString(1));
                    offfield.appendChild(obspro);
                }
                Element field1 = doc.createElement("sos:responseFormat");
                Text field1t = doc.createTextNode("text/xml; subtype=&quot;om/1.0.0&quot;");
                field1.appendChild(field1t);
                offfield.appendChild(field1);
                Element field2 = doc.createElement("sos:resultModel");
                Text field2t = doc.createTextNode("om:Observation");
                field2.appendChild(field2t);
                offfield.appendChild(field2);
                Element field3 = doc.createElement("sos:responseMode");
                Text field3t = doc.createTextNode("inline");
                field3.appendChild(field3t);
                offfield.appendChild(field3);
                offlist.appendChild(offfield);
            }
            cont.appendChild(offlist);
            root.appendChild(cont);
        }
        if (section.contains("Filter_Capabilties") || section.contains("All")) {
            Element filt = doc.createElement("sos:Filter_Capabilties");
            Element spat = doc.createElement("ogc:SpatialCapabilities");
            Element geom = doc.createElement("ogc:GeometryOperands");
            Element geomf = doc.createElement("ogc:GeometryOperand");
            Text geomft = doc.createTextNode("gml:Envelope");
            geomf.appendChild(geomft);
            geom.appendChild(geomf);
            Element geomfp = doc.createElement("ogc:GeometryOperand");
            Text geomfpt = doc.createTextNode("gml:Point");
            geomfp.appendChild(geomfpt);
            geom.appendChild(geomfp);
            Element geomfl = doc.createElement("ogc:GeometryOperand");
            Text geomflt = doc.createTextNode("gml:Polygon");
            geomfl.appendChild(geomflt);
            geom.appendChild(geomfl);
            Element geomfs = doc.createElement("ogc:GeometryOperand");
            Text geomfst = doc.createTextNode("gml:LineString");
            geomfs.appendChild(geomfst);
            geom.appendChild(geomfs);
            spat.appendChild(geom);
            filt.appendChild(spat);

            Element temp = doc.createElement("ogc:Temporal_Capabilities");
            Element teom = doc.createElement("ogc:TemporalOperands");
            Element teomfs = doc.createElement("ogc:temporalOperand");
            Text teomfst = doc.createTextNode("gml:TimeInstant");
            teomfs.appendChild(teomfst);
            teom.appendChild(teomfs);
            Element teomps = doc.createElement("ogc:temporalOperand");
            Text teompst = doc.createTextNode("gml:TimePeriod");
            teomps.appendChild(teompst);
            teom.appendChild(teomps);
            temp.appendChild(teom);
            filt.appendChild(temp);
            root.appendChild(filt);
        }

        doc.appendChild(root);
        //File file = new File(this.filename_output);

        StringWriter stringWriter = new StringWriter();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);
        return stringWriter.getBuffer().toString();

    }
}
