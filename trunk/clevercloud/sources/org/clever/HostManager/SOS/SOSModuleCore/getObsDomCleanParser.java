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
package org.clever.HostManager.SOS.SOSModuleCore;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 *
 * @author user
 */
public class getObsDomCleanParser {

    private getObsInfo info;
    private Vector<String> senstemp;
    private Vector<String> phentemp;
    private Vector<Float> coortemp;
    private Vector<String> c_uomtemp;

    getObsDomCleanParser() {
        info = new getObsInfo();
        senstemp = new Vector<String>(1);
        phentemp = new Vector<String>(1);
        coortemp = new Vector<Float>(1);
        c_uomtemp = new Vector<String>(1);

    }

    getObsInfo getInfo() {
        return this.info;
    }

    public void getObsInfo(Node node) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        do {

            Node currentNode = tovisit.lastElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode);
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();

                    if (sNodeName.equals("offering")) {
                        info.setOffering(sNodeValue);
                    }
                    if (sNodeName.equals("eventTime")) {
                        TimeInfo(currentNode);
                        //currentNode=currentNode.getNextSibling();
                    }
                    if (sNodeName.equals("featureOfInterest")) {
                        FoIinfo(currentNode);
                        //currentNode=currentNode.getNextSibling();                 
                    }
                    if (sNodeName.equals("observedProperty")) {
                        info.getObsPhenomena().add(sNodeValue);
                        //currentNode=currentNode.getNextSibling();                 
                    }
                    if (sNodeName.equals("procedure")) {
                        info.getSensor_id().add(sNodeValue);
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
        } while (tovisit.isEmpty() == false);


    }

    private void FoIinfo(Node node) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        do {

            Node currentNode = tovisit.lastElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode);
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();

                    if (sNodeName.equals("ogc:PropertyName")) {
                        info.setGeom_property(sNodeValue);
                    }
                    if (sNodeName.equals("gml:Envelope")) {
                        info.setGeom_type(sNodeName);
                    }
                    if (sNodeName.equals("gml:lowerCorner")) {
                        info.getCoordinate().add(Float.parseFloat(sNodeValue.split(" ")[0]));
                        info.getCoordinate().add(Float.parseFloat(sNodeValue.split(" ")[1]));
                        //currentNode=currentNode.getNextSibling();
                    }
                    if (sNodeName.equals("gml:upperCorner")) {
                        info.getCoordinate().add(Float.parseFloat(sNodeValue.split(" ")[0]));
                        info.getCoordinate().add(Float.parseFloat(sNodeValue.split(" ")[1]));
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
        } while (tovisit.isEmpty() == false);
    }

    private void TimeInfo(Node node) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        do {

            Node currentNode = tovisit.lastElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode);
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();

                    if (sNodeName.equals("gml:beginPosition")) {
                        String time = sNodeValue.replace(".", "ppp");
                        //org.apache.log4j.Logger.getLogger("getOBSDOM").debug("|!|!| :"+time);
                        info.setTime_stamp_min(time.split("ppp")[0].replace("T", " "));
                        //  info.setTime_stamp_min(sNodeValue.replace("T", " "));

                    }
                    if (sNodeName.equals("gml:endPosition")) {
                        String time = sNodeValue.replace(".", "ppp");
                        info.setTime_stamp_max(time.split("ppp")[0].replace("T", " "));
                        //  info.setTime_stamp_max(sNodeValue.replace("T", " "));                        

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
}
