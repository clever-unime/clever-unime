/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleTransactional;

import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Sensor_Struct;
import org.clever.HostManager.SOS.SOSModuleTransactional.Readers.Sensor_Component;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.io.StringWriter;
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
import org.xml.sax.*;

/**
 *
 * @author user
 */
public class RegisterSensorXml {

    Sensor_Struct sens_nodeid;
    int i;
    private DocumentBuilderFactory dbf;
    private DocumentBuilder builder;
    private Document doc;
    private SOSmodule sosModule;
    private ParameterContainer parameterContainer;
    private Logger logger;

    public RegisterSensorXml(Sensor_Struct sens_nodeid, int i, SOSmodule sosModule) throws ParserConfigurationException {
        this.parameterContainer = ParameterContainer.getInstance();
        logger = this.parameterContainer.getLogger();
        logger.debug("registersensorxml");
        this.sens_nodeid = sens_nodeid;
        this.sosModule = sosModule;
        dbf = DocumentBuilderFactory.newInstance();
        builder = dbf.newDocumentBuilder();
        doc = builder.newDocument();
        this.i = i;
        
        

    }

    public void write_descrsens_xml() throws ParserConfigurationException, TransformerConfigurationException, TransformerException, SAXException, IOException, SQLException, ParseException {
       // logger.debug("RegisterSensor: type=" + sens_nodeid.getSensor_info().gettype_id() + " id=" + sens_nodeid.getSensor_info().getid());

        // File xmlFilereg = new File("/home/user/registersensor.xml");

        //Elemento radice
        Element root = doc.createElement("RegisterSensor");
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
        Text idvaluetext = doc.createTextNode(sens_nodeid.getSensor_info().gettype_id() + "" + sens_nodeid.getSensor_info().getid());
        idvalue.appendChild(idvaluetext);
        idTerm.appendChild(idvalue);
        identifier.appendChild(idTerm);
        identifierlist.appendChild(identifier);
        //identifier
        Element identifiert = doc.createElement("sml:identifier");
        identifiert.setAttribute("name", "productName");
        Element idTermt = doc.createElement("sml:Term");
        idTermt.setAttribute("definition", "urn:ogc:def:identifier:OGC:1.0:productName");
        Element idvaluet = doc.createElement("sml:value");
        Text idvaluetextt = doc.createTextNode(sens_nodeid.getSensor_info().getproduct_description());
        idvaluet.appendChild(idvaluetextt);
        idTermt.appendChild(idvaluet);
        identifiert.appendChild(idTermt);
        identifierlist.appendChild(identifiert);
        
        Element identifiert1 = doc.createElement("sml:identifier");
        identifiert1.setAttribute("name", "modelNumber");
        Element idTermt1 = doc.createElement("sml:Term");
        idTermt1.setAttribute("definition", "urn:ogc:def:identifier:OGC:1.0:modelNumber");
        Element idvaluet1 = doc.createElement("sml:value");
        Text idvaluetextt1 = doc.createTextNode(sens_nodeid.getSensor_info().getmodel());
        idvaluet1.appendChild(idvaluetextt1);
        idTermt1.appendChild(idvaluet1);
        identifiert1.appendChild(idTermt1);
        identifierlist.appendChild(identifiert1);
        Element identifiert2 = doc.createElement("sml:identifier");
        identifiert2.setAttribute("name", "manifacturer");
        Element idTermt2 = doc.createElement("sml:Term");
        idTermt2.setAttribute("definition", "urn:ogc:def:identifier:OGC:1.0:manifacturer");
        Element idvaluet2 = doc.createElement("sml:value");
        Text idvaluetextt2 = doc.createTextNode(sens_nodeid.getSensor_info().getmanufacturer());
        idvaluet2.appendChild(idvaluetextt2);
        idTermt2.appendChild(idvaluet2);
        identifiert2.appendChild(idTermt2);
        identifierlist.appendChild(identifiert2);
        Element identifiert3 = doc.createElement("sml:identifier");
        identifiert3.setAttribute("name", "operator");
        Element idTermt3 = doc.createElement("sml:Term");
        idTermt3.setAttribute("definition", "urn:ogc:def:identifier:OGC:1.0:operator");
        Element idvaluet3 = doc.createElement("sml:value");
        Text idvaluetextt3 = doc.createTextNode(sens_nodeid.getSensor_info().getoperator_area());
        idvaluet3.appendChild(idvaluetextt3);
        idTermt3.appendChild(idvaluet3);
        identifiert3.appendChild(idTermt3);
        identifierlist.appendChild(identifiert3);
        //fine identifier
        identification.appendChild(identifierlist);
        system.appendChild(identification);

        //classifier
        Element classification = doc.createElement("sml:classification");
        Element classifierlist = doc.createElement("sml:ClassifierList");
        Element identifiert4 = doc.createElement("sml:classifier");
        identifiert4.setAttribute("name", "intendedApplication");
        Element idTermt4 = doc.createElement("sml:Term");
        idTermt4.setAttribute("definition", "urn:ogc:def:classifier:OGC:1.0:application");
        Element idvaluet4 = doc.createElement("sml:value");
        Text idvaluetextt4 = doc.createTextNode(sens_nodeid.getSensor_info().getclass_application());
        idvaluet4.appendChild(idvaluetextt4);
        idTermt4.appendChild(idvaluet4);
        identifiert4.appendChild(idTermt4);
        classifierlist.appendChild(identifiert4);
        for (int j = 0; j < sens_nodeid.getSensor_Component().size(); j++) {
            Element identifiert5 = doc.createElement("sml:classifier");
            identifiert5.setAttribute("name", "sensorType");
            Element idTermt5 = doc.createElement("sml:Term");
            idTermt5.setAttribute("definition", "urn:ogc:def:classifier:OGC:1.0:sensorType");
            Element idvaluet5 = doc.createElement("sml:value");
            Text idvaluetextt5 = doc.createTextNode(sens_nodeid.getSensor_Component().elementAt(j).getcomp_descr());
            idvaluet5.appendChild(idvaluetextt5);
            idTermt5.appendChild(idvaluet5);
            identifiert5.appendChild(idTermt5);
            classifierlist.appendChild(identifiert5);
        }
        classification.appendChild(classifierlist);
        system.appendChild(classification);
        Element capabilities = doc.createElement("sml:capabilities");
        Element simpledatarecord = doc.createElement("swe:SimpleDataRecord");
        Element field = doc.createElement("swe:field");
        field.setAttribute("name", "status");
        Element booleanfield = doc.createElement("swe:Boolean");
        Element valuestatus = doc.createElement("swe:value");
        if (sens_nodeid.getSensor_info().getactive().equals("0")) {
            Text valuestatustext = doc.createTextNode("false");
            valuestatus.appendChild(valuestatustext);
        } else {
            if (sens_nodeid.getSensor_info().getactive().equals("1")) {
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
        if (sens_nodeid.getSensor_info().getmobile().equals("0")) {
            Text valuemobiletext = doc.createTextNode("false");
            valuemobile.appendChild(valuemobiletext);
        } else {
            if (sens_nodeid.getSensor_info().getmobile().equals("1")) {
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
        fuom.setAttribute("code", sens_nodeid.getSensor_info().getmeasures_interval_uom());
        Element valuef = doc.createElement("swe:value");
        Text valueftext = doc.createTextNode(sens_nodeid.getSensor_info().getmeasures_interval());
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
        positionGr.setAttribute("referenceFrame", sens_nodeid.getSensor_info().getref());
        Element Vectorcoord = doc.createElement("swe:Vector");
        Vectorcoord.setAttribute("gml:id", "STATION_LOCATION");
        Element lat = doc.createElement("swe:coordinate");
        lat.setAttribute("name", "latitude");
        Element quantity = doc.createElement("swe:Quantity");
        quantity.setAttribute("axisID", "x");
        Element uomcode = doc.createElement("swe:uom");
        uomcode.setAttribute("code", sens_nodeid.getSensor_info().getlat_uom());
        Element value = doc.createElement("swe:value");
        Text valuetext = doc.createTextNode(sens_nodeid.getSensor_info().getlat_val());
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
        uomcodel.setAttribute("code", sens_nodeid.getSensor_info().getlong_uom());
        Element valuel = doc.createElement("swe:value");
        Text valuetextl = doc.createTextNode(sens_nodeid.getSensor_info().getlong_val());
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
        uomcodea.setAttribute("code", sens_nodeid.getSensor_info().getalt_uom());
        Element valuea = doc.createElement("swe:value");
        Text valuetexta = doc.createTextNode(sens_nodeid.getSensor_info().getalt_val());
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
        for (int j = 0; j < sens_nodeid.getSensor_Phenomena().size(); j++) {
            //   System.out.println("phen elem:"+sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_id());
            Element inputfield = doc.createElement("sml:input");
            inputfield.setAttribute("name", sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_descr());
            Element observablep = doc.createElement("sml:ObservableProperty");
            observablep.setAttribute("definition", "urn:ogc:def:property:OGC:" + sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_id());
            inputfield.appendChild(observablep);
            inputl.appendChild(inputfield);
        }
        input.appendChild(inputl);
        system.appendChild(input);
        Element output = doc.createElement("sml:outputs");
        Element outputl = doc.createElement("sml:OutputList");
        for (int j = 0; j < sens_nodeid.getSensor_Phenomena().size(); j++) {
            Element outputfield = doc.createElement("sml:output");
            outputfield.setAttribute("name", sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_descr());
            Element observablep = doc.createElement("swe:Quantity");
            observablep.setAttribute("definition", "urn:ogc:def:property:OGC:" + sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_id());
            Element metadata = doc.createElement("gml:metaDataProperty");
            Element offering = doc.createElement("offering");
            Element id = doc.createElement("id");
            Text idtext = doc.createTextNode(sens_nodeid.getSensor_Phenomena().elementAt(j).getoffering_id());
            id.appendChild(idtext);
            offering.appendChild(id);
            Element name = doc.createElement("name");
            Text nametext = doc.createTextNode(sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_descr());
            name.appendChild(nametext);
            offering.appendChild(name);
            metadata.appendChild(offering);
            observablep.appendChild(metadata);
            Element uom = doc.createElement("swe:uom");
            uom.setAttribute("code", sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_uom());
            uom.setAttribute("xlink:href", "urn:ogc:def:property:OGC:" + sens_nodeid.getSensor_Phenomena().elementAt(j).getphen_uom_id());
            observablep.appendChild(uom);
            outputfield.appendChild(observablep);
            outputl.appendChild(outputfield);
        }
        output.appendChild(outputl);
        system.appendChild(output);
        Element components = doc.createElement("sml:components");
        Element componentlist = doc.createElement("sml:ComponentList");
        for (int j = 0; j < sens_nodeid.getSensor_Component().size(); j++) {
            Element comptemp = print_component_xml(sens_nodeid.getSensor_Component().elementAt(j), i);
            componentlist.appendChild(comptemp);

        }
        components.appendChild(componentlist);
        system.appendChild(components);
        member1.appendChild(system);
        sensorml.appendChild(member1);
        root.appendChild(sensorml);
        doc.appendChild(root);


        StringWriter stringWriter = new StringWriter();




        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(stringWriter);
        transformer.transform(source, result);

        sosModule.SOSservice(stringWriter.getBuffer().toString());



    }
//scrive le informazioni negli opportuni tag per ogni componente

    public Element print_component_xml(Sensor_Component comp, int i) throws ParserConfigurationException {

        Element componentnode = doc.createElement("sml:component");
        componentnode.setAttribute("name", comp.getcomp_descr());
        Element system = doc.createElement("sml:Component");
        Element identification = doc.createElement("sml:identification");
        Element identifierlist = doc.createElement("sml:IdentifierList");
        Element identifier = doc.createElement("sml:identifier");
        identifier.setAttribute("name", "uniqueID");
        Element idTerm = doc.createElement("sml:Term");
        idTerm.setAttribute("definition", "urn:ogc:def:identifier:OGC:uniqueID");
        Element idvalue = doc.createElement("sml:value");
        Text idvaluetext = doc.createTextNode(sens_nodeid.getSensor_info().gettype_id() + "" + comp.getcomp_id());
        idvalue.appendChild(idvaluetext);
        idTerm.appendChild(idvalue);
        identifier.appendChild(idTerm);
        identifierlist.appendChild(identifier);

        identification.appendChild(identifierlist);
        system.appendChild(identification);
        Element classification = doc.createElement("sml:classification");
        Element classifierlist = doc.createElement("sml:ClassifierList");

        Element identifiert = doc.createElement("sml:classifier");
        identifiert.setAttribute("name", "sensorType");
        Element idTermt = doc.createElement("sml:Term");
        idTermt.setAttribute("definition", "urn:ogc:def:classifier:OGC:1.0:sensorType");
        Element idvaluet = doc.createElement("sml:value");
        Text idvaluetextt = doc.createTextNode(comp.getcomp_descr());
        idvaluet.appendChild(idvaluetextt);
        idTermt.appendChild(idvaluet);
        identifiert.appendChild(idTermt);
        classifierlist.appendChild(identifiert);

        classification.appendChild(classifierlist);
        system.appendChild(classification);
        Element capabilities = doc.createElement("sml:capabilities");
        Element simpledatarecord = doc.createElement("swe:SimpleDataRecord");
        Element fieldm = doc.createElement("swe:field");
        fieldm.setAttribute("name", "status");
        Element booleanfieldm = doc.createElement("swe:Boolean");
        Element valuemobile = doc.createElement("swe:value");
        if (comp.getcomp_status().equals("0")) {
            Text valuemobiletext = doc.createTextNode("false");
            valuemobile.appendChild(valuemobiletext);
        } else {
            if (comp.getcomp_status().equals("1")) {
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
        position.setAttribute("name", "" + comp.getcomp_id() + "Position");
        Element positionGr = doc.createElement("swe:Position");
        Element Vectorcoord = doc.createElement("swe:Vector");
//      Vectorcoord.setAttribute("id", "STATION_LOCATION");
        Element lat = doc.createElement("swe:coordinate");
        lat.setAttribute("name", "latitude");
        Element quantity = doc.createElement("swe:Quantity");
        quantity.setAttribute("axisID", "x");
        Element uomcode = doc.createElement("swe:uom");
        uomcode.setAttribute("code", sens_nodeid.getSensor_info().getlat_uom());
        Element value = doc.createElement("swe:value");
        Text valuetext = doc.createTextNode("0");
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
        uomcodel.setAttribute("code", sens_nodeid.getSensor_info().getlong_uom());
        Element valuel = doc.createElement("swe:value");
        Text valuetextl = doc.createTextNode("0");
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
        uomcodea.setAttribute("code", sens_nodeid.getSensor_info().getalt_uom());
        Element valuea = doc.createElement("swe:value");
        Text valuetexta = doc.createTextNode("0");
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
        // System.out.println("phen comp:"+comp.getcomp_phenomena());
        String[] list = comp.getcomp_phenomena().split("_");

        for (int j = 0; j < list.length; j++) {
            // System.out.println("list phen comp elem:"+list[j]);
            for (int k = 0; k < sens_nodeid.getSensor_Phenomena().size(); k++) {
                if (sens_nodeid.getSensor_Phenomena().elementAt(k).getphen_id().indexOf(list[j]) != -1) {
                    Element inputfield = doc.createElement("sml:input");
                    inputfield.setAttribute("name", sens_nodeid.getSensor_Phenomena().elementAt(k).getphen_descr());
                    Element observablep = doc.createElement("sml:ObservableProperty");
                    observablep.setAttribute("definition", sens_nodeid.getSensor_Phenomena().elementAt(k).getphen_id());
                    inputfield.appendChild(observablep);
                    inputl.appendChild(inputfield);
                }
            }
        }
        input.appendChild(inputl);
        system.appendChild(input);

        Element output = doc.createElement("sml:outputs");
        Element outputl = doc.createElement("sml:OutputList");
        for (int j = 0; j < list.length; j++) {
            for (int k = 0; k < sens_nodeid.getSensor_Phenomena().size(); k++) {
                if (sens_nodeid.getSensor_Phenomena().elementAt(k).getphen_id().indexOf(list[j]) != -1) {

                    Element outputfield = doc.createElement("sml:output");
                    outputfield.setAttribute("name", sens_nodeid.getSensor_Phenomena().elementAt(k).getphen_descr());
                    Element observablep = doc.createElement("swe:Quantity");
                    observablep.setAttribute("definition", "urn:ogc:def:property:OGC:" + sens_nodeid.getSensor_Phenomena().elementAt(k).getphen_id());
                    Element metadata = doc.createElement("gml:metaDataProperty");
                    Element offering = doc.createElement("offering");
                    Element id = doc.createElement("id");
                    Text idtext = doc.createTextNode(sens_nodeid.getSensor_Phenomena().elementAt(k).getoffering_id());
                    id.appendChild(idtext);
                    offering.appendChild(id);
                    Element name = doc.createElement("name");
                    Text nametext = doc.createTextNode(sens_nodeid.getSensor_Phenomena().elementAt(k).getphen_descr());
                    name.appendChild(nametext);
                    offering.appendChild(name);
                    metadata.appendChild(offering);
                    observablep.appendChild(metadata);
                    Element uom = doc.createElement("swe:uom");
                    uom.setAttribute("code", sens_nodeid.getSensor_Phenomena().elementAt(k).getphen_uom());
                    uom.setAttribute("xlink:href", sens_nodeid.getSensor_Phenomena().elementAt(k).getphen_uom_id());
                    observablep.appendChild(uom);
                    outputfield.appendChild(observablep);
                    outputl.appendChild(outputfield);
                }
            }
        }
        output.appendChild(outputl);
        system.appendChild(output);
        componentnode.appendChild(system);

        return componentnode;
    }
}
