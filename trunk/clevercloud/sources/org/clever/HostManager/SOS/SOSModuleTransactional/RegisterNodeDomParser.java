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
                        if (utils.printAttributes(nnmAttributes).indexOf("mobile") != -1) {
                            printCapability(currentNode, 1);

                        }
                        if (utils.printAttributes(nnmAttributes).indexOf("status") != -1) {
                            printCapability(currentNode, 0);

                        }
                        if (utils.printAttributes(nnmAttributes).indexOf("transmissionFrequency") != -1) {
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
                        
                        phenomenonTemp.setPhenomenon_description(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0].trim());
                    }


                    if (sNodeName.equals("swe:Quantity")) {
                        phenomenonTemp.setPhenomenon_id(utils.printAttributes(nnmAttributes).split("=")[1].trim());

                    }
                    if (sNodeName.equals("id")) {
                        String sNodeValueChild = utils.searchTextInElement(currentNode);
                        if (!sNodeValueChild.trim().equalsIgnoreCase("")) {

                            phenomenonTemp.setOffering_id(sNodeValueChild.trim());
                        }
                    }
                    if (sNodeName.equals("name")) {
                        String sNodeValueChild = utils.searchTextInElement(currentNode);
                        if (!sNodeValueChild.trim().equalsIgnoreCase("")) {

                            phenomenonTemp.setOffering_name(sNodeValueChild.trim());
                        }
                    }
                    if (sNodeName.equals("swe:uom")) {
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
                        classifierTemp.setClassifier_description(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0].trim());
                    }


                    if (sNodeName.equals("sml:Term")) {
                        classifierTemp.setClassifier_id(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0].trim());

                    }
                    if (sNodeName.equals("sml:value")) {
                        String sNodeValueChild = utils.searchTextInElement(currentNode);
                        if (!sNodeValueChild.trim().equalsIgnoreCase("")) {

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
                        
                    }
                    if (sNodeName.equals("swe:value")) {
                        if (flag == 0) {
                            sensorDescription.setLongitude(Float.valueOf(sNodeValue.trim()).floatValue());
                        }
                        if (flag == 1) {
                            sensorDescription.setLatitude(Float.valueOf(sNodeValue.trim()).floatValue());
                        }
                        if (flag == 2) {
                            sensorDescription.setAltitude(Float.valueOf(sNodeValue.trim()).floatValue());
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
                    if (sNodeName.equals("swe:Position")) {
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
                        printCoordinate(currentNode, 0);
                    }
                    if (sNodeName.equals("swe:coordinate") && (utils.printAttributes(nnmAttributes).indexOf("northing") != -1 || utils.printAttributes(nnmAttributes).indexOf("latitude") != -1)) {
                        printCoordinate(currentNode, 1);
                    }
                    if (sNodeName.equals("swe:coordinate") && utils.printAttributes(nnmAttributes).indexOf("altitude") != -1) {
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
                        printNodeSensorCapabilities(currentNode);
                        //if(visited.contains(currentNode.getNextSibling())==false)
                        //  tovisit.add(currentNode.getNextSibling());
                        //currentNode=currentNode.getNextSibling();
                    }
                    if (sNodeName.equals("sml:identifier")) {
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
                                    if (sNodeName2.equals("sml:Term") && utils.printAttributes(nnmAttributes).indexOf("uniqueID") != -1) {
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
        while (tovisit.isEmpty() == false) {

            Node currentNode = tovisit.firstElement();
            if (visited.contains(currentNode) == false) {
                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();
                    if (sNodeName.equals("sml:output")) {
                        PhenomenonDescription phenomenonTemp = new PhenomenonDescription();
                        printNodeSensorPhenomena(currentNode, phenomenonTemp);
                        phenomenonDescription.add(phenomenonTemp);
                        //currentNode=currentNode.getNextSibling();
                    }
                    if (sNodeName.equals("sml:classifier")) {
                        ClassifierDescription classifierTemp = new ClassifierDescription();
                        printNodeSensorClassifier(currentNode, classifierTemp);
                        classifierDescription.add(classifierTemp);
                        //currentNode=currentNode.getNextSibling();
                    }
                    if (sNodeName.equals("sml:components")) {
                        node.removeChild(currentNode);
                        //logger.debug("sensorNodeInfoE");
                        // if(visited.contains(currentNode.getNextSibling())==false)
                        //   tovisit.add(currentNode.getNextSibling());
                        //currentNode=currentNode.getNextSibling();
                    }
                    if (sNodeName.equals("sml:capabilities") || sNodeName.equals("sml:identifier") || sNodeName.equals("swe:Position")) {
                        printNodeSensor(currentNode);
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
                        if (utils.printAttributes(nnmAttributes).equals("assenti")) {
                            identifierTemp.setidentifier_description(utils.printAttributes(nnmAttributes));
                        } else {
                            identifierTemp.setidentifier_description(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0]);
                        }

                    }


                    if (sNodeName.equals("sml:Term")) {
                        if (utils.printAttributes(nnmAttributes).equals("assenti")) {
                            identifierTemp.setidentifier_id(utils.printAttributes(nnmAttributes));
                        } else {
                            identifierTemp.setidentifier_id(utils.printAttributes(nnmAttributes).split("=")[1].split(";")[0]);
                        }

                    }
                    if (sNodeName.equals("sml:value")) {
                        String sNodeValueChild = utils.searchTextInElement(currentNode);
                        if (!sNodeValueChild.trim().equalsIgnoreCase("")) {

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
