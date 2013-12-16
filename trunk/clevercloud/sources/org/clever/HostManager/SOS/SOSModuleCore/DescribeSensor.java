/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleCore;

import org.clever.HostManager.SOS.Database;
import org.clever.HostManager.SOS.ParameterContainer;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.io.File;
import java.io.IOException;
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
/*import org.jdom.*;
import org.jdom.output.*;
import java.io.*; */

/**
 *
 * @author user
 */
class ComponentSensor {

    private SensorDescription sensorDescription;
    private Vector<PhenomenonDescription> phenomenonDescription;
    private Vector<ClassifierDescription> classifierDescription;
    private Vector<IdentifierDescription> identifierDescription;

    ComponentSensor() {
        sensorDescription = new SensorDescription();
        phenomenonDescription = new Vector<PhenomenonDescription>(1);
        classifierDescription = new Vector<ClassifierDescription>(1);
        identifierDescription = new Vector<IdentifierDescription>(1);
    }

    Vector<PhenomenonDescription> getphenomenonDescription() {
        return this.phenomenonDescription;
    }

    SensorDescription getsensorDescription() {
        return this.sensorDescription;
    }

    Vector<ClassifierDescription> getclassifierDescription() {
        return this.classifierDescription;
    }

    Vector<IdentifierDescription> getidentifierDescription() {
        return this.identifierDescription;
    }
}

public class DescribeSensor {

    protected SensorDescription sensorDescription;
    protected Vector<PhenomenonDescription> phenomenonDescription;
    protected Vector<ClassifierDescription> classifierDescription;
    protected Vector<IdentifierDescription> identifierDescription;
    protected Vector<ComponentSensor> component;
    protected Database db;
    private String sensor_id;
    private String sensor_unique_id;
    private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder = dbf.newDocumentBuilder();
    private Document doc = builder.newDocument();
    private ParameterContainer parameterContainer = null;
    //private String filename_output;
   /* DescribeSensor(String filename) throws ParserConfigurationException, SAXException, IOException{
    sensorDescription=new SensorDescription();
    phenomenonDescription=new Vector<PhenomenonDescription>(1);
    classifierDescription=new Vector<ClassifierDescription>(1);
    identifierDescription=new Vector<IdentifierDescription>(1);  
    component=new Vector<ComponentSensor>(1);
    db=new DataBase();
    sensor_unique_id="";
    sensor_id="";
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = dbf.newDocumentBuilder();
    File xmlFilereg = new File(filename);
    org.w3c.dom.Document documentreg = builder.parse(xmlFilereg);
    parse_sensor_id(documentreg);
    this.filename_output="/home/user/file.xml";
    }*/

    public DescribeSensor(String describeSensorRequest) throws ParserConfigurationException, SAXException, IOException {
        sensorDescription = new SensorDescription();
        phenomenonDescription = new Vector<PhenomenonDescription>(1);
        classifierDescription = new Vector<ClassifierDescription>(1);
        identifierDescription = new Vector<IdentifierDescription>(1);
        component = new Vector<ComponentSensor>(1);
        db = Database.getInstance();
        sensor_unique_id = "";
        sensor_id = "";
        parameterContainer = ParameterContainer.getInstance();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        //File xmlFilereg = new File(filename);
        org.w3c.dom.Document documentreg = builder.parse(new ByteArrayInputStream(describeSensorRequest.getBytes()));
        parse_sensor_id(documentreg);
        /*this.filename_output=filename_output;
        if(this.filename_output.equals(""))
        this.filename_output=System.getProperty("user.home")+"/file_describesensor.xml";*/
    }

    void parse_sensor_id(Node node) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        do {

            Node currentNode = tovisit.lastElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    if (sNodeName.equals("procedure")) {
                        this.sensor_unique_id = utils.searchTextInElement(currentNode);
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
    }

    public void describe_db() throws SQLException {
        //db=new DataBase();
        //db.openDB(this.parameterContainer.getDbServer(),this.parameterContainer.getDbDriver(),this.parameterContainer.getDbName(),
        //        this.parameterContainer.getDbUsername(),this.parameterContainer.getDbPassword());

        //System.out.println("\nDescribe sensor id :"+this.sensor_unique_id);
        //informazioni sensore
        String query_sens = "SELECT `sensor`.`sensor_id`, `sensor`.`unique_id`, `sensor`.`description_type`, `sensor`.`status`, `sensor`.`mobile`, `sensor`.`srs`, `sensor`.`longitude`, `sensor`.`long_uom`, `sensor`.`latitude`, `sensor`.`lat_uom`, `sensor`.`altitude`, `sensor`.`alt_uom`,`sensor`.`fixed`,`frequency`, `frequency_uom` FROM `sensor` WHERE `unique_id` LIKE '" + this.sensor_unique_id + "'";

        ResultSet rs = db.exQuery(query_sens);
        //se il risultato della query non è un insieme vuoto, vuol dire che il sensore esiste e si può procedere, altrimenti si esce dal programma
        if (rs.next()) {
            //i campi della classe sensorDescription vengono riempite con le informazioni sul sensore 
            sensor_id = rs.getString(1);
            //System.out.println("\nDescribe sensor id :"+this.sensor_id);
            sensorDescription.setSensor_id(rs.getString(2));
            sensorDescription.setDescription_type(rs.getString(3));
            sensorDescription.setStatus(Integer.parseInt(rs.getString(4)));
            sensorDescription.setMobile(Integer.parseInt(rs.getString(5)));
            sensorDescription.setCrs(rs.getString(6));
            sensorDescription.setLongitude(Float.parseFloat(rs.getString(7)));
            sensorDescription.setlong_uom(rs.getString(8));
            sensorDescription.setLatitude(Float.parseFloat(rs.getString(9)));
            sensorDescription.setlat_uom(rs.getString(10));
            sensorDescription.setAltitude(Float.parseFloat(rs.getString(11)));
            sensorDescription.setalt_uom(rs.getString(12));
            sensorDescription.setFixed(Integer.parseInt(rs.getString(13)));
            sensorDescription.setFrequency(rs.getString(14));
            sensorDescription.setFrequencyUom(rs.getString(15));
        } else {
            System.out.println("\n sensore non presente con id :" + sensor_unique_id);
            return;
        }
        //in base all'id del sensore, si trovano i phenomena che il sensore è in grado di misurare. La relazione tra id del sensore e id del phenomeno è contenuta nella tabella sens_phen
        String query_phen = "SELECT `phenomenon`.`phenomenon_id`, `phenomenon`.`unique_id`, `phenomenon`.`phenomenon_description`, `phenomenon`.`unit`, `phenomenon`.`valuetype`,`offering`.`unique_id`,  `offering`.`offering_name` FROM `phenomenon`,`sens_phen`,`offering`,`sens_off`,`phen_off` WHERE `phenomenon`.`phenomenon_id`=`sens_phen`.`phenomenon_id` AND`sens_off`.`offering_id`=`offering`.`offering_id` AND `phen_off`.`offering_id`= `offering`.`offering_id` AND `phen_off`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `sens_off`.`sensor_id`=`sens_phen`.`sensor_id` AND`sens_phen`.`sensor_id`='" + sensor_id + "'";
        rs = db.exQuery(query_phen);
        // System.out.println(query_phen);
        while (rs.next()) {
            PhenomenonDescription phentemp = new PhenomenonDescription();
            phentemp.setOffering_id(rs.getString(6));
            phentemp.setOffering_name(rs.getString(7));
            phentemp.setPhenomenon_description(rs.getString(3));
            phentemp.setPhenomenon_id(rs.getString(2));
            phentemp.setPhenomenon_unit(rs.getString(4));
            phentemp.setPhenomenon_valuetype(rs.getString(5));
            phenomenonDescription.add(phentemp);
        }
        //in base all'id del sensore, si trovano i classifier che lo descrivono. La relazione tra id del sensore e id del classifier è contenuta nella tabella sens_class
        String query_class = "SELECT `classifier`.`unique_id`, `classifier_value`, `classifier_description` FROM `sens_class`,`classifier` WHERE `sens_class`.`sensor_id`='" + sensor_id + "' AND `sens_class`.`classifier_id`=`classifier`.`classifier_id`";
        db.exQuery(query_class);
        //System.out.println(query_class);

        while (rs.next()) {
            ClassifierDescription classtemp = new ClassifierDescription();
            classtemp.setClassifier_description(rs.getString(3));
            classtemp.setClassifier_id(rs.getString(1));
            classtemp.setClassifier_value(rs.getString(2));
            classifierDescription.add(classtemp);
        } //in base all'id del sensore, si trovano gli identifier corrispondenti. La relazione tra id del sensore e id del identifier è contenuta nella tabella sens_ident
        String query_ident = "SELECT `identifier`.`unique_id`, `identifier_value`, `identifier_description` FROM `sens_ident`,`identifier` WHERE `sens_ident`.`sensor_id`='" + sensor_id + "' AND `sens_ident`.`identifier_id`=`identifier`.`identifier_id`";
        rs = db.exQuery(query_ident);
        //System.out.println(query_ident);

        while (rs.next()) {
            IdentifierDescription identtemp = new IdentifierDescription();
            identtemp.setidentifier_description(rs.getString(3));
            identtemp.setidentifier_id(rs.getString(1));
            identtemp.setidentifier_value(rs.getString(2));
            identifierDescription.add(identtemp);
        }
        // component varie query
        //per quanto riguarda i singoli sensori sulle board, grazie alla tabella sens_comp, dove vi sono le associazioni id della board id del componente, si ottengono gli id per ogni componente. Tramite questo id si effettuano delle query simili a quelle per la board, su phenomena, classifier e identifier, per ogni componente. Ogni componente è un elemento del vettore delle componenti asscoicati al sensore.
        Vector<String> comp_id = new Vector<String>(1);
        String query_comp = "SELECT `component`.`component_id` FROM  `component`,`sens_comp` WHERE `sens_comp`.`component_id`= `component`.`component_id` AND `sens_comp`.`sensor_id`='" + sensor_id + "'";
        db.exQuery(query_comp);
        //System.out.println(query_comp);
        while (rs.next()) {
            comp_id.add(rs.getString(1));
        }
        for (int i = 0; i < comp_id.size(); i++) {
            //System.out.println("\n component id:"+comp_id.elementAt(i));
            ComponentSensor comp_temp = new ComponentSensor();
            String query_comp_des = " SELECT `component`.`component_id`, `unique_id`, `description`, `status`, `mobile`, `crs`, `longitude`, `long_uom`, `latitude`, `lat_uom`, `altitude`, `alt_uom` FROM `component`  WHERE `component`.`component_id`= '" + comp_id.elementAt(i) + "'";
            db.exQuery(query_comp_des);
            //System.out.println(query_comp_des);
            if (rs.next()) {
                comp_temp.getsensorDescription().setSensor_id(rs.getString(2));
                comp_temp.getsensorDescription().setDescription_type(rs.getString(3));
                comp_temp.getsensorDescription().setStatus(Integer.parseInt(rs.getString(4)));
                comp_temp.getsensorDescription().setMobile(Integer.parseInt(rs.getString(5)));
                comp_temp.getsensorDescription().setCrs(rs.getString(6));
                comp_temp.getsensorDescription().setLongitude(Float.parseFloat(rs.getString(7)));
                comp_temp.getsensorDescription().setlong_uom(rs.getString(8));
                comp_temp.getsensorDescription().setLatitude(Float.parseFloat(rs.getString(9)));
                comp_temp.getsensorDescription().setlat_uom(rs.getString(10));
                comp_temp.getsensorDescription().setAltitude(Float.parseFloat(rs.getString(11)));
                comp_temp.getsensorDescription().setalt_uom(rs.getString(12));

            }
            String comp_phen = "SELECT `phenomenon`.`phenomenon_id`, `phenomenon`.`unique_id`, `phenomenon`.`phenomenon_description`, `phenomenon`.`unit`, `phenomenon`.`valuetype`,`offering`.`unique_id`,  `offering`.`offering_name` FROM `comp_phen`,`phen_off`,`phenomenon`,`offering` WHERE  `comp_phen`.`phenomenon_id`=`phen_off`.`phenomenon_id` AND `comp_phen`.`phenomenon_id`=`phenomenon`.`phenomenon_id` AND `phen_off`.`offering_id`=`offering`.`offering_id` AND `comp_phen`.`component_id`='" + comp_id.elementAt(i) + "'";
            rs = db.exQuery(comp_phen);
            //System.out.println(comp_phen);
            while (rs.next()) {
                PhenomenonDescription phentemp = new PhenomenonDescription();
                phentemp.setOffering_id(rs.getString(6));
                phentemp.setOffering_name(rs.getString(7));
                phentemp.setPhenomenon_description(rs.getString(3));
                phentemp.setPhenomenon_id(rs.getString(2));
                phentemp.setPhenomenon_unit(rs.getString(4));
                phentemp.setPhenomenon_valuetype(rs.getString(5));
                comp_temp.getphenomenonDescription().add(phentemp);
            }

            String comp_class = "SELECT `unique_id`, `classifier_value`, `classifier_description` FROM `comp_class`,`classifier` WHERE `comp_class`.`component_id`='" + comp_id.elementAt(i) + "' AND `comp_class`.`classifier_id`=`classifier`.`classifier_id`";
            rs = db.exQuery(comp_class);
            //System.out.println(comp_class);
            while (rs.next()) {
                ClassifierDescription classtemp = new ClassifierDescription();
                classtemp.setClassifier_description(rs.getString(3));
                classtemp.setClassifier_id(rs.getString(1));
                classtemp.setClassifier_value(rs.getString(2));
                comp_temp.getclassifierDescription().add(classtemp);
            }
            String comp_ident = "SELECT `unique_id`, `identifier_value`, `identifier_description` FROM `comp_ident`,`identifier` WHERE `comp_ident`.`component_id`='" + comp_id.elementAt(i) + "' AND `comp_ident`.`identifier_id`=`identifier`.`identifier_id`";
            rs = db.exQuery(comp_ident);
            //System.out.println(comp_ident);
            while (rs.next()) {
                IdentifierDescription identtemp = new IdentifierDescription();
                identtemp.setidentifier_description(rs.getString(3));
                identtemp.setidentifier_id(rs.getString(1));
                identtemp.setidentifier_value(rs.getString(2));
                comp_temp.getidentifierDescription().add(identtemp);
            }
            component.add(comp_temp);

        }

    }
//scrive il file xml risultato dell'interrogazione
    public String write_descrsens_xml() throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        File xmlFilereg = new File("/home/user/describesensor.xml");

        //Elemento radice
        Element root = doc.createElement("DescribeSensor");
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

        if (sensor_id.equals("") == false) {

            Element sensorml = doc.createElement("sml:SensorML");
            sensorml.setAttribute("version", "1.0.1");
            Element member1 = doc.createElement("sml:member");
            Element system = doc.createElement("sml:System");
            Element identification = doc.createElement("sml:identification");
            Element identifierlist = doc.createElement("sml:IdentifierList");
            Element identifier = doc.createElement("sml:identifier");
            identifier.setAttribute("name", "uniqueID");
            Element idTerm = doc.createElement("sml:Term");
            idTerm.setAttribute("definition", "urn:ogc:def:identifier:OGC:uniqueID");
            Element idvalue = doc.createElement("sml:value");
            Text idvaluetext = doc.createTextNode(sensorDescription.getSensor_id());
            idvalue.appendChild(idvaluetext);
            idTerm.appendChild(idvalue);
            identifier.appendChild(idTerm);
            identifierlist.appendChild(identifier);
            for (int i = 0; i < identifierDescription.size(); i++) {
                Element identifiert = doc.createElement("sml:identifier");
                identifiert.setAttribute("name", identifierDescription.elementAt(i).getidentifier_description());
                Element idTermt = doc.createElement("sml:Term");
                idTermt.setAttribute("definition", identifierDescription.elementAt(i).getidentifier_id());
                Element idvaluet = doc.createElement("sml:value");
                Text idvaluetextt = doc.createTextNode(identifierDescription.elementAt(i).getidentifier_value());
                idvaluet.appendChild(idvaluetextt);
                idTermt.appendChild(idvaluet);
                identifiert.appendChild(idTermt);
                identifierlist.appendChild(identifiert);
            }
            identification.appendChild(identifierlist);
            system.appendChild(identification);
            Element classification = doc.createElement("sml:classification");
            Element classifierlist = doc.createElement("sml:ClassifierList");
            for (int i = 0; i < classifierDescription.size(); i++) {
                Element identifiert = doc.createElement("sml:classifier");
                identifiert.setAttribute("name", classifierDescription.elementAt(i).getClassifier_description());
                Element idTermt = doc.createElement("sml:Term");
                idTermt.setAttribute("definition", classifierDescription.elementAt(i).getClassifier_id());
                Element idvaluet = doc.createElement("sml:value");
                Text idvaluetextt = doc.createTextNode(classifierDescription.elementAt(i).getClassifier_value());
                idvaluet.appendChild(idvaluetextt);
                idTermt.appendChild(idvaluet);
                identifiert.appendChild(idTermt);
                classifierlist.appendChild(identifiert);
            }
            classification.appendChild(classifierlist);
            system.appendChild(classification);
            Element capabilities = doc.createElement("sml:capabilities");
            Element simpledatarecord = doc.createElement("swe:SimpleDataRecord");
            Element field = doc.createElement("swe:field");
            field.setAttribute("name", "status");
            Element booleanfield = doc.createElement("swe:Boolean");
            Element valuestatus = doc.createElement("swe:value");
            if (sensorDescription.getStatus() == 0) {
                Text valuestatustext = doc.createTextNode("false");
                valuestatus.appendChild(valuestatustext);
            } else {
                if (sensorDescription.getStatus() == 1) {
                    Text valuestatustext = doc.createTextNode("true");
                    valuestatus.appendChild(valuestatustext);
                }
            }

            booleanfield.appendChild(valuestatus);
            field.appendChild(booleanfield);
            simpledatarecord.appendChild(field);

            Element fieldm = doc.createElement("swe:field");
            fieldm.setAttribute("name", "mobile");
            Element booleanfieldm = doc.createElement("swe:Boolean");
            Element valuemobile = doc.createElement("swe:value");
            if (sensorDescription.getMobile() == 0) {
                Text valuemobiletext = doc.createTextNode("false");
                valuemobile.appendChild(valuemobiletext);
            } else {
                if (sensorDescription.getMobile() == 1) {
                    Text valuemobiletext = doc.createTextNode("true");
                    valuemobile.appendChild(valuemobiletext);
                }
            }

            booleanfieldm.appendChild(valuemobile);
            fieldm.appendChild(booleanfieldm);
            simpledatarecord.appendChild(fieldm);

            Element fieldf = doc.createElement("swe:field");
            fieldf.setAttribute("name", "transmissionFrequency");
            Element quanfield = doc.createElement("swe:Quantity");
            quanfield.setAttribute("definition", "urn:ogc:def:property:OGC:1.0:transmissionFrequency");
            Element fuom = doc.createElement("swe:uom");
            //  System.out.println(sensorDescription.getFrequencyUom());
            fuom.setAttribute("code", sensorDescription.getFrequencyUom());
            Element valuef = doc.createElement("swe:value");
            //    System.out.println(sensorDescription.getFrequency());
            Text valueftext = doc.createTextNode(sensorDescription.getFrequency());
            valuef.appendChild(valueftext);
            quanfield.appendChild(fuom);
            quanfield.appendChild(valuef);
            fieldf.appendChild(quanfield);
            simpledatarecord.appendChild(fieldf);

            capabilities.appendChild(simpledatarecord);
            system.appendChild(capabilities);
            //position
            Element position = doc.createElement("swe:position");
            position.setAttribute("name", "sensorPosition");
            Element positionGr = doc.createElement("swe:Position");
            positionGr.setAttribute("referenceFrame", sensorDescription.getCrs());
            if (sensorDescription.getFixed() != -1) {
                if (sensorDescription.getFixed() == 0) {
                    positionGr.setAttribute("fixed", "false");
                }
                if (sensorDescription.getFixed() == 1) {
                    positionGr.setAttribute("fixed", "true");
                }
            }
            Element Vectorcoord = doc.createElement("swe:Vector");
            Vectorcoord.setAttribute("gml:id", "STATION_LOCATION");
            Element lat = doc.createElement("swe:coordinate");
            lat.setAttribute("name", "latitude");
            Element quantity = doc.createElement("swe:Quantity");
            quantity.setAttribute("axisID", "x");
            Element uomcode = doc.createElement("swe:uom");
            uomcode.setAttribute("code", sensorDescription.getlat_uom());
            Element value = doc.createElement("swe:value");
            Text valuetext = doc.createTextNode("" + sensorDescription.getLatitude());
            value.appendChild(valuetext);
            quantity.appendChild(uomcode);
            quantity.appendChild(value);
            lat.appendChild(quantity);
            Vectorcoord.appendChild(lat);
            Element longit = doc.createElement("swe:coordinate");
            longit.setAttribute("name", "longitude");
            Element quantityl = doc.createElement("swe:Quantity");
            quantityl.setAttribute("axisID", "y");
            Element uomcodel = doc.createElement("swe:uom");
            uomcodel.setAttribute("code", sensorDescription.getlong_uom());
            Element valuel = doc.createElement("swe:value");
            Text valuetextl = doc.createTextNode("" + sensorDescription.getLongitude());
            valuel.appendChild(valuetextl);
            quantityl.appendChild(uomcodel);
            quantityl.appendChild(valuel);
            longit.appendChild(quantityl);
            Vectorcoord.appendChild(longit);

            Element lata = doc.createElement("swe:coordinate");
            lata.setAttribute("name", "altitude");
            Element quantitya = doc.createElement("swe:Quantity");
            quantitya.setAttribute("axisID", "x");
            Element uomcodea = doc.createElement("swe:uom");
            uomcodea.setAttribute("code", sensorDescription.getalt_uom());
            Element valuea = doc.createElement("swe:value");
            Text valuetexta = doc.createTextNode("" + sensorDescription.getAltitude());
            valuea.appendChild(valuetexta);
            quantitya.appendChild(uomcodea);
            quantitya.appendChild(valuea);
            lata.appendChild(quantitya);
            Vectorcoord.appendChild(lata);

            positionGr.appendChild(Vectorcoord);
            position.appendChild(positionGr);
            system.appendChild(position);

            Element input = doc.createElement("sml:inputs");
            Element inputl = doc.createElement("sml:InputList");
            for (int i = 0; i < phenomenonDescription.size(); i++) {
                Element inputfield = doc.createElement("sml:input");
                inputfield.setAttribute("name", phenomenonDescription.elementAt(i).getPhenomenon_description());
                Element observablep = doc.createElement("sml:ObservableProperty");
                observablep.setAttribute("definition", phenomenonDescription.elementAt(i).getPhenomenon_id());
                inputfield.appendChild(observablep);
                inputl.appendChild(inputfield);
            }
            input.appendChild(inputl);
            system.appendChild(input);

            Element output = doc.createElement("sml:outputs");
            Element outputl = doc.createElement("sml:OutputList");
            for (int i = 0; i < phenomenonDescription.size(); i++) {
                Element outputfield = doc.createElement("sml:output");
                outputfield.setAttribute("name", phenomenonDescription.elementAt(i).getPhenomenon_description());
                Element observablep = doc.createElement("swe:Quantity");
                observablep.setAttribute("definition", phenomenonDescription.elementAt(i).getPhenomenon_id());
                Element metadata = doc.createElement("gml:metaDataProperty");
                Element offering = doc.createElement("offering");
                Element id = doc.createElement("id");
                Text idtext = doc.createTextNode(phenomenonDescription.elementAt(i).getOffering_id());
                id.appendChild(idtext);
                offering.appendChild(id);
                Element name = doc.createElement("name");
                Text nametext = doc.createTextNode(phenomenonDescription.elementAt(i).getOffering_name());
                name.appendChild(nametext);
                offering.appendChild(name);
                metadata.appendChild(offering);
                observablep.appendChild(metadata);
                Element uom = doc.createElement("swe:uom");
                uom.setAttribute("code", phenomenonDescription.elementAt(i).getPhenomenon_unit());
                observablep.appendChild(uom);
                outputfield.appendChild(observablep);
                outputl.appendChild(outputfield);
            }
            output.appendChild(outputl);
            system.appendChild(output);

            Element components = doc.createElement("sml:components");
            Element componentlist = doc.createElement("cml:ComponentList");
            for (int i = 0; i < component.size(); i++) {
                Element comptemp = print_component_xml(component.elementAt(i));
                componentlist.appendChild(comptemp);

            }
            components.appendChild(componentlist);
            system.appendChild(components);
            member1.appendChild(system);
            sensorml.appendChild(member1);
            root.appendChild(sensorml);
        }
        doc.appendChild(root);
        //File file = new File(this.filename_output);
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StringWriter stringWriter = new StringWriter();
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);

        return stringWriter.getBuffer().toString();



    }
//scrive le informazioni negli opportuni tag per ogni componente
    Element print_component_xml(ComponentSensor comp) throws ParserConfigurationException {

        Element componentnode = doc.createElement("sml:component");
        componentnode.setAttribute("name", comp.getsensorDescription().getDescription_type());
        Element system = doc.createElement("sml:Component");
        Element identification = doc.createElement("sml:identification");
        Element identifierlist = doc.createElement("sml:IdentifierList");
        Element identifier = doc.createElement("sml:identifier");
        identifier.setAttribute("name", "uniqueID");
        Element idTerm = doc.createElement("sml:Term");
        idTerm.setAttribute("definition", "urn:ogc:def:identifier:OGC:uniqueID");
        Element idvalue = doc.createElement("sml:value");
        Text idvaluetext = doc.createTextNode(comp.getsensorDescription().getSensor_id());
        idvalue.appendChild(idvaluetext);
        idTerm.appendChild(idvalue);
        identifier.appendChild(idTerm);
        identifierlist.appendChild(identifier);
        for (int i = 0; i < comp.getidentifierDescription().size(); i++) {
            Element identifiert = doc.createElement("sml:identifier");
            identifiert.setAttribute("name", comp.getidentifierDescription().elementAt(i).getidentifier_description());
            Element idTermt = doc.createElement("sml:Term");
            idTermt.setAttribute("definition", comp.getidentifierDescription().elementAt(i).getidentifier_id());
            Element idvaluet = doc.createElement("sml:value");
            Text idvaluetextt = doc.createTextNode(comp.getidentifierDescription().elementAt(i).getidentifier_value());
            idvaluet.appendChild(idvaluetextt);
            idTermt.appendChild(idvaluet);
            identifiert.appendChild(idTermt);
            identifierlist.appendChild(identifiert);
        }
        identification.appendChild(identifierlist);
        system.appendChild(identification);
        Element classification = doc.createElement("sml:classification");
        Element classifierlist = doc.createElement("sml:ClassifierList");
        for (int i = 0; i < comp.getclassifierDescription().size(); i++) {
            Element identifiert = doc.createElement("sml:classifier");
            identifiert.setAttribute("name", comp.getclassifierDescription().elementAt(i).getClassifier_description());
            Element idTermt = doc.createElement("sml:Term");
            idTermt.setAttribute("definition", comp.getclassifierDescription().elementAt(i).getClassifier_id());
            Element idvaluet = doc.createElement("sml:value");
            Text idvaluetextt = doc.createTextNode(comp.getclassifierDescription().elementAt(i).getClassifier_value());
            idvaluet.appendChild(idvaluetextt);
            idTermt.appendChild(idvaluet);
            identifiert.appendChild(idTermt);
            classifierlist.appendChild(identifiert);
        }
        classification.appendChild(classifierlist);
        system.appendChild(classification);
        Element capabilities = doc.createElement("sml:capabilities");
        Element simpledatarecord = doc.createElement("swe:SimpleDataRecord");
        Element field = doc.createElement("swe:field");
        field.setAttribute("name", "status");
        Element booleanfield = doc.createElement("swe:Boolean");
        Element valuestatus = doc.createElement("swe:value");
        if (comp.getsensorDescription().getStatus() == 0) {
            Text valuestatustext = doc.createTextNode("false");
            valuestatus.appendChild(valuestatustext);
        } else {
            if (comp.getsensorDescription().getStatus() == 1) {
                Text valuestatustext = doc.createTextNode("true");
                valuestatus.appendChild(valuestatustext);
            }
        }

        booleanfield.appendChild(valuestatus);
        field.appendChild(booleanfield);
        simpledatarecord.appendChild(field);
        Element fieldm = doc.createElement("swe:field");
        fieldm.setAttribute("name", "mobile");
        Element booleanfieldm = doc.createElement("swe:Boolean");
        Element valuemobile = doc.createElement("swe:value");
        if (comp.getsensorDescription().getMobile() == 0) {
            Text valuemobiletext = doc.createTextNode("false");
            valuemobile.appendChild(valuemobiletext);
        } else {
            if (comp.getsensorDescription().getMobile() == 1) {
                Text valuemobiletext = doc.createTextNode("true");
                valuemobile.appendChild(valuemobiletext);
            }
        }

        booleanfieldm.appendChild(valuemobile);
        fieldm.appendChild(booleanfieldm);
        simpledatarecord.appendChild(fieldm);
        capabilities.appendChild(simpledatarecord);
        system.appendChild(capabilities);
        //position
        Element position = doc.createElement("swe:position");
        position.setAttribute("name", "" + comp.getsensorDescription().getDescription_type() + "Position");
        Element positionGr = doc.createElement("swe:Position");
        positionGr.setAttribute("referenceFrame", comp.getsensorDescription().getCrs());
        if (comp.getsensorDescription().getFixed() != -1) {
            if (comp.getsensorDescription().getFixed() == 0) {
                positionGr.setAttribute("fixed", "false");
            }
            if (comp.getsensorDescription().getFixed() == 1) {
                positionGr.setAttribute("fixed", "true");
            }
        }
        Element Vectorcoord = doc.createElement("swe:Vector");
//      Vectorcoord.setAttribute("id", "STATION_LOCATION");
        Element lat = doc.createElement("swe:coordinate");
        lat.setAttribute("name", "latitude");
        Element quantity = doc.createElement("swe:Quantity");
        quantity.setAttribute("axisID", "x");
        Element uomcode = doc.createElement("swe:uom");
        uomcode.setAttribute("code", comp.getsensorDescription().getlat_uom());
        Element value = doc.createElement("swe:value");
        Text valuetext = doc.createTextNode("" + comp.getsensorDescription().getLatitude());
        value.appendChild(valuetext);
        quantity.appendChild(uomcode);
        quantity.appendChild(value);
        lat.appendChild(quantity);
        Vectorcoord.appendChild(lat);
        Element longit = doc.createElement("swe:coordinate");
        longit.setAttribute("name", "longitude");
        Element quantityl = doc.createElement("swe:Quantity");
        quantityl.setAttribute("axisID", "y");
        Element uomcodel = doc.createElement("swe:uom");
        uomcodel.setAttribute("code", comp.getsensorDescription().getlong_uom());
        Element valuel = doc.createElement("swe:value");
        Text valuetextl = doc.createTextNode("" + comp.getsensorDescription().getLongitude());
        valuel.appendChild(valuetextl);
        quantityl.appendChild(uomcodel);
        quantityl.appendChild(valuel);
        longit.appendChild(quantityl);
        Vectorcoord.appendChild(longit);

        Element lata = doc.createElement("swe:coordinate");
        lata.setAttribute("name", "altitude");
        Element quantitya = doc.createElement("swe:Quantity");
        quantitya.setAttribute("axisID", "x");
        Element uomcodea = doc.createElement("swe:uom");
        uomcodea.setAttribute("code", comp.getsensorDescription().getalt_uom());
        Element valuea = doc.createElement("swe:value");
        Text valuetexta = doc.createTextNode("" + comp.getsensorDescription().getAltitude());
        valuea.appendChild(valuetexta);
        quantitya.appendChild(uomcodea);
        quantitya.appendChild(valuea);
        lata.appendChild(quantitya);
        Vectorcoord.appendChild(lata);

        positionGr.appendChild(Vectorcoord);
        position.appendChild(positionGr);
        system.appendChild(position);

        Element input = doc.createElement("sml:inputs");
        Element inputl = doc.createElement("sml:InputList");
        for (int i = 0; i < comp.getphenomenonDescription().size(); i++) {
            Element inputfield = doc.createElement("sml:input");
            inputfield.setAttribute("name", comp.getphenomenonDescription().elementAt(i).getPhenomenon_description());
            Element observablep = doc.createElement("sml:ObservableProperty");
            observablep.setAttribute("definition", comp.getphenomenonDescription().elementAt(i).getPhenomenon_id());
            inputfield.appendChild(observablep);
            inputl.appendChild(inputfield);
        }
        input.appendChild(inputl);
        system.appendChild(input);

        Element output = doc.createElement("sml:outputs");
        Element outputl = doc.createElement("sml:OutputList");
        for (int i = 0; i < comp.getphenomenonDescription().size(); i++) {
            Element outputfield = doc.createElement("sml:output");
            outputfield.setAttribute("name", comp.getphenomenonDescription().elementAt(i).getPhenomenon_description());
            Element observablep = doc.createElement("swe:Quantity");
            observablep.setAttribute("definition", comp.getphenomenonDescription().elementAt(i).getPhenomenon_id());
            Element metadata = doc.createElement("gml:metaDataProperty");
            Element offering = doc.createElement("offering");
            Element id = doc.createElement("id");
            Text idtext = doc.createTextNode(comp.getphenomenonDescription().elementAt(i).getOffering_id());
            id.appendChild(idtext);
            offering.appendChild(id);
            Element name = doc.createElement("name");
            Text nametext = doc.createTextNode(comp.getphenomenonDescription().elementAt(i).getOffering_name());
            name.appendChild(nametext);
            offering.appendChild(name);
            metadata.appendChild(offering);
            observablep.appendChild(metadata);
            Element uom = doc.createElement("swe:uom");
            uom.setAttribute("code", comp.getphenomenonDescription().elementAt(i).getPhenomenon_unit());
            observablep.appendChild(uom);
            outputfield.appendChild(observablep);
            outputl.appendChild(outputfield);
        }
        output.appendChild(outputl);
        system.appendChild(output);
        componentnode.appendChild(system);

        return componentnode;
    }
//Stampa informazioni sul sensore 
    void print_info() {
        System.out.println("Sensor id da attributo: " + sensorDescription.getSensor_id());
        System.out.println("Sensor description da attributo: " + sensorDescription.getDescription_type());
        System.out.println("Sensor mobile da attributo: " + sensorDescription.getMobile());
        System.out.println("Sensor status da attributo: " + sensorDescription.getStatus());
        System.out.println("Sensor crs da attributo: " + sensorDescription.getCrs());
        System.out.println("Sensor fixed da attributo: " + sensorDescription.getFixed());
        System.out.println("Sensor x da attributo: " + sensorDescription.getLongitude());
        System.out.println("Sensor x uom : " + sensorDescription.getlong_uom());
        System.out.println("Sensor y da attributo: " + sensorDescription.getLatitude());
        System.out.println("Sensor y uom : " + sensorDescription.getlat_uom());
        System.out.println("Sensor z da attributo: " + sensorDescription.getAltitude());
        System.out.println("Sensor z uom : " + sensorDescription.getalt_uom());



        for (int i = 0; i < phenomenonDescription.size(); i++) {
            System.out.println("\n phenomena numero " + i);
            System.out.println("offering id da attributo: " + phenomenonDescription.elementAt(i).getOffering_id());
            System.out.println("offering description da attributo: " + phenomenonDescription.elementAt(i).getOffering_name());
            System.out.println("phenomenon description da attributo: " + phenomenonDescription.elementAt(i).getPhenomenon_description());
            System.out.println("phenomenon id da attributo: " + phenomenonDescription.elementAt(i).getPhenomenon_id());
            System.out.println("phenomenon unit da attributo: " + phenomenonDescription.elementAt(i).getPhenomenon_unit());
            System.out.println("phenomenon valuetype da attributo: " + phenomenonDescription.elementAt(i).getPhenomenon_valuetype());



        }
        for (int i = 0; i < classifierDescription.size(); i++) {
            System.out.println("\n classifier numero " + i);
            System.out.println("classifier description: " + classifierDescription.elementAt(i).getClassifier_description());
            System.out.println("classifier id: " + classifierDescription.elementAt(i).getClassifier_id());
            System.out.println("classifier value: " + classifierDescription.elementAt(i).getClassifier_value());


        }
        for (int i = 0; i < identifierDescription.size(); i++) {
            System.out.println("\n identifier numero " + i);
            System.out.println("identifier description: " + identifierDescription.elementAt(i).getidentifier_description());
            System.out.println("identifier id: " + identifierDescription.elementAt(i).getidentifier_id());
            System.out.println("identifier value: " + identifierDescription.elementAt(i).getidentifier_value());


        }



        for (int ii = 0; ii < component.size(); ii++) {
            System.out.println("\n componente numero " + ii);
            System.out.println("Component id da attributo: " + component.elementAt(ii).getsensorDescription().getSensor_id());

            System.out.println("Sensor description da attributo: " + component.elementAt(ii).getsensorDescription().getDescription_type());
            System.out.println("Sensor mobile da attributo: " + component.elementAt(ii).getsensorDescription().getMobile());
            System.out.println("Sensor status da attributo: " + component.elementAt(ii).getsensorDescription().getStatus());
            System.out.println("Sensor crs da attributo: " + component.elementAt(ii).getsensorDescription().getCrs());
            System.out.println("Sensor x da attributo: " + component.elementAt(ii).getsensorDescription().getLongitude());
            System.out.println("Sensor x uom : " + component.elementAt(ii).getsensorDescription().getlong_uom());
            System.out.println("Sensor y da attributo: " + component.elementAt(ii).getsensorDescription().getLatitude());
            System.out.println("Sensor y uom : " + component.elementAt(ii).getsensorDescription().getlat_uom());
            System.out.println("Sensor z da attributo: " + component.elementAt(ii).getsensorDescription().getAltitude());
            System.out.println("Sensor z uom : " + component.elementAt(ii).getsensorDescription().getalt_uom());



            for (int i = 0; i < component.elementAt(ii).getphenomenonDescription().size(); i++) {
                System.out.println("\n phenomena numero " + i);
                System.out.println("offering id da attributo: " + component.elementAt(ii).getphenomenonDescription().elementAt(i).getOffering_id());
                System.out.println("offering description da attributo: " + component.elementAt(ii).getphenomenonDescription().elementAt(i).getOffering_name());
                System.out.println("phenomenon description da attributo: " + component.elementAt(ii).getphenomenonDescription().elementAt(i).getPhenomenon_description());
                System.out.println("phenomenon id da attributo: " + component.elementAt(ii).getphenomenonDescription().elementAt(i).getPhenomenon_id());
                System.out.println("phenomenon unit da attributo: " + component.elementAt(ii).getphenomenonDescription().elementAt(i).getPhenomenon_unit());
                System.out.println("phenomenon valuetype da attributo: " + component.elementAt(ii).getphenomenonDescription().elementAt(i).getPhenomenon_valuetype());



            }
            for (int i = 0; i < component.elementAt(ii).getclassifierDescription().size(); i++) {
                System.out.println("\n classifier numero " + i);
                System.out.println("classifier description: " + component.elementAt(ii).getclassifierDescription().elementAt(i).getClassifier_description());
                System.out.println("classifier id: " + component.elementAt(ii).getclassifierDescription().elementAt(i).getClassifier_id());
                System.out.println("classifier value: " + component.elementAt(ii).getclassifierDescription().elementAt(i).getClassifier_value());


            }
            for (int i = 0; i < component.elementAt(ii).getidentifierDescription().size(); i++) {
                System.out.println("\n identifier numero " + i);
                System.out.println("identifier description: " + component.elementAt(ii).getidentifierDescription().elementAt(i).getidentifier_description());
                System.out.println("identifier id: " + component.elementAt(ii).getidentifierDescription().elementAt(i).getidentifier_id());
                System.out.println("identifier value: " + component.elementAt(ii).getidentifierDescription().elementAt(i).getidentifier_value());


            }
        }
    }
}
