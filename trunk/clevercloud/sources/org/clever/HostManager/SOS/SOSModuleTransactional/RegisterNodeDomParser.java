/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleTransactional;

/**
 *
 * @author user
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Vector;
import org.w3c.dom.*;
import org.apache.log4j.Logger;
/**
 *
 * @author user
 */
public class RegisterNodeDomParser {

    protected SensorDescription sensorDescription = new SensorDescription();
    protected Vector<PhenomenonDescription> phenomenonDescription = new Vector<PhenomenonDescription>(1);
    protected Vector<ClassifierDescription> classifierDescription = new Vector<ClassifierDescription>(1);
    protected Vector<IdentifierDescription> identifierDescription = new Vector<IdentifierDescription>(1);
    Logger logger=Logger.getLogger("RegisterNodeDomParser");
    public void printNodeSensorIdentifier(Node node) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    //per ogni componente 
                    if (sNodeName.equals("sml:value")) {

                        String sNodeValueChild = utils.searchTextInElement(currentNode);
                        if (!sNodeValueChild.trim().equalsIgnoreCase("")) {

                            //   System.out.println("sensor_id: " + sNodeValueChild);
                            sensorDescription.setSensor_id(sNodeValueChild.trim());
                        }
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
        }


    }

    private void printCapability(Node node, int flag) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();
                    //per ogni componente 
                    if (sNodeName.equals("swe:value")) {

                        //        System.out.println("value: " + sNodeValue);
                        if (flag == 0) {
                            if (sNodeValue.equals("true") | sNodeValue.equals("active")) {
                                sensorDescription.setStatus(1);
                            } else {
                                sensorDescription.setStatus(0);
                            }
                        } else if (flag == 1) {
                            if (sNodeValue.equals("true")) {
                                sensorDescription.setMobile(1);
                            } else {
                                sensorDescription.setMobile(0);
                            }
                        } else if (flag == 2) {
                            sensorDescription.setFrequency(sNodeValue);

                        }
                    }
                    if (sNodeName.equals("swe:uom")) {
                        String[] splitlis = utils.printAttributes(nnmAttributes).split(" ");
                        for (int i = 0; i < splitlis.length; i++) {
                            if (splitlis[i].indexOf("code") != -1) {
                                sensorDescription.setFrequencyUom(splitlis[i].split("=")[1]);
                            }
                        }
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
        }

    }

    public void printNodeSensorCapabilities(Node node) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();
                    if (sNodeName.equals("swe:field")) {
                        //  //System.out.println("in field ");
                        if (utils.printAttributes(nnmAttributes).indexOf("mobile") != -1) {
                            //System.out.println("mobile: ");
                            printCapability(currentNode, 1);

                        }
                        if (utils.printAttributes(nnmAttributes).indexOf("status") != -1) {
                            //System.out.println("in mobile ");
                            //System.out.println("status: ");
                            printCapability(currentNode, 0);

                        }
                        if (utils.printAttributes(nnmAttributes).indexOf("transmissionFrequency") != -1) {
                            //System.out.println("in mobile ");
                            //System.out.println("status: ");
                            printCapability(currentNode, 2);

                        }
                    }
                    if (sNodeName.equals("swe:uom")) {
                        String[] splitlis = utils.printAttributes(nnmAttributes).split(" ");
                        for (int i = 0; i < splitlis.length; i++) {
                            if (splitlis[i].indexOf("code") != -1) {
                                sensorDescription.setFrequencyUom(splitlis[i].split("=")[1]);
                            }
                        }
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
        }


    }

    public void printNodeSensorPhenomena(Node node, PhenomenonDescription phenomenonTemp) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();
                    if (sNodeName.equals("sml:output")) {
                        //System.out.println("Phenomena description: " + utils.printAttributes(nnmAttributes)); 

                        phenomenonTemp.setPhenomenon_description(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0].trim());
                    }


                    if (sNodeName.equals("swe:Quantity")) {
                        //  System.out.println("Phenomena id: " + utils.printAttributes(nnmAttributes).split("=")[1]); 
                        phenomenonTemp.setPhenomenon_id(utils.printAttributes(nnmAttributes).split("=")[1].trim());

                    }
                    if (sNodeName.equals("id")) {
                        String sNodeValueChild = utils.searchTextInElement(currentNode);
                        if (!sNodeValueChild.trim().equalsIgnoreCase("")) {

                            //System.out.println("Offering id: " + sNodeValueChild);
                            phenomenonTemp.setOffering_id(sNodeValueChild.trim());
                        }
                    }
                    if (sNodeName.equals("name")) {
                        String sNodeValueChild = utils.searchTextInElement(currentNode);
                        if (!sNodeValueChild.trim().equalsIgnoreCase("")) {

                            //System.out.println("Offering description: " + sNodeValueChild);
                            phenomenonTemp.setOffering_name(sNodeValueChild.trim());
                        }
                    }
                    if (sNodeName.equals("swe:uom")) {
                        // System.out.println("Phenomena unit: " + utils.printAttributes(nnmAttributes).split(";")[0].split("=")[1]); 
                        phenomenonTemp.setPhenomenon_unit(utils.printAttributes(nnmAttributes).split(";")[0].split("=")[1].trim());
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
        }

    }

    public void printNodeSensorClassifier(Node node, ClassifierDescription classifierTemp) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();
                    if (sNodeName.equals("sml:classifier")) {
                        //System.out.println("Phenomena description: " + utils.printAttributes(nnmAttributes)); 

                        classifierTemp.setClassifier_description(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0].trim());
                    }


                    if (sNodeName.equals("sml:Term")) {
                        //System.out.println("Phenomena id: " + utils.printAttributes(nnmAttributes)); 
                        classifierTemp.setClassifier_id(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0].trim());

                    }
                    if (sNodeName.equals("sml:value")) {
                        String sNodeValueChild = utils.searchTextInElement(currentNode);
                        if (!sNodeValueChild.trim().equalsIgnoreCase("")) {

                            //System.out.println("Offering id: " + sNodeValueChild);
                            classifierTemp.setClassifier_value(sNodeValueChild.trim());
                        }
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
        }

    }

    /**
     * Stampa le info sui nodi, in modo ricorsivo
     * @param currentNode il nodo corrente
     */
    public void printNodeInfo(Node node) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();

                    if (!sNodeValue.trim().equalsIgnoreCase("")) {
                        System.out.println("Nome " + sNodeName + " Contenuto: " + sNodeValue + " parent " + currentNode.getParentNode().getNodeName() + " attributo: " + utils.printAttributes(nnmAttributes));
                    } else {
                        System.out.println("Nome " + sNodeName + " attributo: " + utils.printAttributes(nnmAttributes));
                    }


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

    }

    public void printCoordinate(Node node, int flag) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();

                    if (sNodeName.equals("swe:uom")) {
                        NamedNodeMap nnmAttributes = currentNode.getAttributes();
                        if (flag == 0) {
                            sensorDescription.setlong_uom(utils.printAttributes(nnmAttributes).split(";")[0].split("=")[1]);
                        }
                        if (flag == 1) {
                            sensorDescription.setlat_uom(utils.printAttributes(nnmAttributes).split(";")[0].split("=")[1]);
                        }
                        if (flag == 2) {
                            sensorDescription.setalt_uom(utils.printAttributes(nnmAttributes).split(";")[0].split("=")[1]);
                        }
                        //System.out.println("uom: " + utils.printAttributes(nnmAttributes));

                    }
                    if (sNodeName.equals("swe:value")) {
                        //System.out.println("value: " + sNodeValue);
                        if (flag == 0) {
                            sensorDescription.setLongitude(Float.valueOf(sNodeValue.trim()).floatValue());
                            //System.out.println("value  x: " + sNodeValue);
                        }
                        if (flag == 1) {
                            sensorDescription.setLatitude(Float.valueOf(sNodeValue.trim()).floatValue());
                            //System.out.println("value  y: " + sNodeValue);
                        }
                        if (flag == 2) {
                            sensorDescription.setAltitude(Float.valueOf(sNodeValue.trim()).floatValue());
                            //System.out.println("value  z: " + sNodeValue);
                        }
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
        }

    }

    public void printNodeSensorPosition(Node node) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();
                    // flags 0 x, 1 y, 2 z.
                    if (sNodeName.equals("swe:Position")) {
                        //System.out.println("Position crs: " + utils.printAttributes(nnmAttributes).split("Frame=")[1]);
                        if (utils.printAttributes(nnmAttributes).contains("fixed=") == true) {
                            sensorDescription.setCrs(utils.printAttributes(nnmAttributes).split("=")[2].split(";")[0]);
                            if (utils.printAttributes(nnmAttributes).split("=")[1].contains("false")) {
                                sensorDescription.setFixed(0);
                            }
                            if (utils.printAttributes(nnmAttributes).split("=")[1].contains("true")) {
                                sensorDescription.setFixed(1);
                            } else {
                                sensorDescription.setFixed(-1);
                            }
                        } else {
                            if (utils.printAttributes(nnmAttributes).contains("referenceFrame=") == true) {
                                sensorDescription.setCrs(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0]);
                            } else {
                                sensorDescription.setCrs(utils.printAttributes(nnmAttributes));
                            }
                        }
                    }
                    if (sNodeName.equals("swe:coordinate") && (utils.printAttributes(nnmAttributes).indexOf("easting") != -1 || utils.printAttributes(nnmAttributes).indexOf("longitude") != -1)) {
                        //System.out.println("longitude: ");
                        printCoordinate(currentNode, 0);
                    }
                    if (sNodeName.equals("swe:coordinate") && (utils.printAttributes(nnmAttributes).indexOf("northing") != -1 || utils.printAttributes(nnmAttributes).indexOf("latitude") != -1)) {
                        //System.out.println("latitude: ");
                        printCoordinate(currentNode, 1);
                    }
                    if (sNodeName.equals("swe:coordinate") && utils.printAttributes(nnmAttributes).indexOf("altitude") != -1) {
                        //System.out.println("altitude: ");
                        printCoordinate(currentNode, 2);
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
        }
    }

    public void printNodeSensor(Node node) {

        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();

                    if (sNodeName.equals("sml:capabilities")) {
                        // System.out.println("capab");
                        printNodeSensorCapabilities(currentNode);
                        //if(visited.contains(currentNode.getNextSibling())==false)
                        //  tovisit.add(currentNode.getNextSibling());
                        //currentNode=currentNode.getNextSibling();
                    }
                    if (sNodeName.equals("sml:identifier")) {
                        //  System.out.println("in identifier");
                        int iChildNumber2 = currentNode.getChildNodes().getLength();
                        //Se non si tratta di una foglia continua l'esplorazione
                        if (currentNode.hasChildNodes()) {
                            NodeList nlChilds2 = currentNode.getChildNodes();
                            for (int iChild = 0; iChild < iChildNumber2; iChild++) {
                                Node currentnode2 = nlChilds2.item(iChild);
                                short sNodeType2 = currentnode2.getNodeType();
                                if (sNodeType2 == Node.ELEMENT_NODE) {
                                    String sNodeName2 = currentnode2.getNodeName();
                                    NamedNodeMap nnmAttributes = currentnode2.getAttributes();
                                    //     System.out.println("identifier attributo: "+utils.printAttributes(nnmAttributes) + "di " +sNodeName2);
                                    if (sNodeName2.equals("sml:Term") && utils.printAttributes(nnmAttributes).indexOf("uniqueID") != -1) {
                                        //         System.out.println("identifier if ");
                                        printNodeSensorIdentifier(currentnode2);
                                        //currentnode2=currentNode.getNextSibling();
                                    } else {
                                        IdentifierDescription identifierTemp = new IdentifierDescription();
                                        printNodeSensorIdentifier2(currentNode, identifierTemp);
                                        identifierDescription.add(identifierTemp);

                                    }
                                }
                            }
                        }
                    }

                    if (sNodeName.equals("swe:Position")) {
                        // System.out.println("position");
                        printNodeSensorPosition(currentNode);
                        //currentNode=currentNode.getNextSibling();
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
        }






    }

    public void sensorNodeInfo(Node node) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        //logger.debug("sensorNodeInfoA");
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();
            //logger.debug("sensorNodeInfoB"+tovisit.size());
            if (visited.contains(currentNode) == false) {
            //logger.debug("sensorNodeInfoC:"+currentNode.getNodeName());
                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();
                    if (sNodeName.equals("sml:output")) {
                        // System.out.println("phenn");
                        PhenomenonDescription phenomenonTemp = new PhenomenonDescription();
                        printNodeSensorPhenomena(currentNode, phenomenonTemp);
                        phenomenonDescription.add(phenomenonTemp);
                        //currentNode=currentNode.getNextSibling();
                    }
                    if (sNodeName.equals("sml:classifier")) {
                        //   System.out.println("class");
                        ClassifierDescription classifierTemp = new ClassifierDescription();
                        printNodeSensorClassifier(currentNode, classifierTemp);
                        classifierDescription.add(classifierTemp);
                        //currentNode=currentNode.getNextSibling();
                    }
                    if (sNodeName.equals("sml:components")) {
                        //logger.debug("sensorNodeInfoD");
                        node.removeChild(currentNode);
                        //logger.debug("sensorNodeInfoE");
                        // if(visited.contains(currentNode.getNextSibling())==false)
                        //   tovisit.add(currentNode.getNextSibling());
                        //currentNode=currentNode.getNextSibling();
                    }
                    if (sNodeName.equals("sml:capabilities") || sNodeName.equals("sml:identifier") || sNodeName.equals("swe:Position")) {
                        //  System.out.println("capab ident position");
                        printNodeSensor(currentNode);
                        //currentNode=currentNode.getNextSibling();
                    }


                }
                int iChildNumber = currentNode.getChildNodes().getLength();

                if (currentNode.hasChildNodes()) {
                    NodeList nlChilds = currentNode.getChildNodes();
                    //logger.debug("sensorNodeInfoF");
                    for (int iChild = 0; iChild < iChildNumber; iChild++) {
                        tovisit.add(nlChilds.item(iChild));

                    }

                }
                visited.add(currentNode);
                tovisit.remove(currentNode);
                //logger.debug("sensorNodeInfoG:"+currentNode.getNodeName());
            }
        }


    }

//per gli identifier che non sono unique_id
    private void printNodeSensorIdentifier2(Node node, IdentifierDescription identifierTemp) {

        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();
                    if (sNodeName.equals("sml:identifier")) {
                        //System.out.println(" description: " + utils.printAttributes(nnmAttributes)); 
                        if (utils.printAttributes(nnmAttributes).equals("assenti")) {
                            identifierTemp.setidentifier_description(utils.printAttributes(nnmAttributes));
                        } else {
                            identifierTemp.setidentifier_description(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0]);
                        }

                    }


                    if (sNodeName.equals("sml:Term")) {
                        //System.out.println("Phenomena id: " + utils.printAttributes(nnmAttributes)); 
                        if (utils.printAttributes(nnmAttributes).equals("assenti")) {
                            identifierTemp.setidentifier_id(utils.printAttributes(nnmAttributes));
                        } else {
                            identifierTemp.setidentifier_id(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0]);
                        }

                    }
                    if (sNodeName.equals("sml:value")) {
                        String sNodeValueChild = utils.searchTextInElement(currentNode);
                        if (!sNodeValueChild.trim().equalsIgnoreCase("")) {

                            //System.out.println("Offering id: " + sNodeValueChild);
                            identifierTemp.setidentifier_value(sNodeValueChild);
                        }
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
        }

    }
}
