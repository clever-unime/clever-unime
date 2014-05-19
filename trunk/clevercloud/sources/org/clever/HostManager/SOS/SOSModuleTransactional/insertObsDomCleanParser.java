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

import java.util.Vector;
import org.w3c.dom.*;

/**
 *
 * @author user
 */
public class insertObsDomCleanParser {

    private ObservationInfo info;
    private int phen_length;
    private int count_phen;

    insertObsDomCleanParser() {
        info = new ObservationInfo();
        phen_length = 1;
        count_phen = 0;
    }

    ObservationInfo getinfo() {
        return this.info;
    }

    public void insertObsInfo(Node node) {
        Vector<Node> tovisit = new Vector<Node>(1);
        Vector<Node> visited = new Vector<Node>(1);
        tovisit.add(node);
        do {

            Node currentNode = tovisit.firstElement();

            if (visited.contains(currentNode) == false) {

                short sNodeType = currentNode.getNodeType();
                if (sNodeType == Node.ELEMENT_NODE) {
                    String sNodeName = currentNode.getNodeName();
                    String sNodeValue = utils.searchTextInElement(currentNode).trim();
                    NamedNodeMap nnmAttributes = currentNode.getAttributes();
                    String dimension = "";

                    if (sNodeName.equals("AssignedSensorId")) {
                        info.setSensor_id(sNodeValue);
                    }
                    if (sNodeName.equals("gml:timePosition")) {
                        String dataprova = sNodeValue.replace("T", " ").replace("Z", "");
                        info.setTime_stamp(dataprova);
                    }
                    if (sNodeName.equals("swe:Time")) {
                        String[] id_tim = utils.printAttributes(nnmAttributes).split("=");
                        info.settime_definition(id_tim[1]);
                    }
                    if (sNodeName.equals("om:observedProperty") && currentNode.hasChildNodes() == false) {
                        String[] id_phen = utils.printAttributes(nnmAttributes).split("=");
                        ObsPhenomenaValue phen_temp = new ObsPhenomenaValue();
                        phen_temp.setPhenomena_id(id_phen[1]);
                        info.getObsPhenomena().add(phen_temp);
                    }
                    if (sNodeName.equals("swe:CompositePhenomenon")) {

                        dimension = utils.printAttributes(nnmAttributes);
                        String[] dimension2 = dimension.split(";");
                        dimension2 = dimension2[0].split("=");
                        phen_length = Integer.parseInt(dimension2[1]);
                    }
                    if (sNodeName.equals("gml:name") && currentNode.getParentNode().getNodeName().equals("swe:CompositePhenomenon")) {
                        info.setPhenomenon_composite(sNodeValue);
                    }
                    if (sNodeName.equals("swe:component")) {
                        String[] id_phen = utils.printAttributes(nnmAttributes).split("=");
                        if (count_phen < phen_length) {
                            ObsPhenomenaValue phen_temp = new ObsPhenomenaValue();
                            phen_temp.setPhenomena_id(id_phen[1]);
                            count_phen++;
                            info.getObsPhenomena().add(phen_temp);
                        }
                    }
                    if (sNodeName.equals("swe:uom")) {
                        String[] id_quan = utils.printAttributes(currentNode.getParentNode().getAttributes()).split("=");
                        if (id_quan[1].indexOf("longitude") > -1) {
                            info.setLong_uom(utils.printAttributes(currentNode.getAttributes()));
                        } else if (id_quan[1].indexOf("latitude") > -1) {
                            info.setLat_uom(utils.printAttributes(currentNode.getAttributes()));
                        } else {
                            for (int i = 0; i < info.getObsPhenomena().size(); i++) {
                                if (id_quan[1].equals(info.getObsPhenomena().elementAt(i).getPhenomena_id())) {
                                    info.getObsPhenomena().elementAt(i).setUom(utils.printAttributes(currentNode.getAttributes()));
                                }


                            }
                        }

                    }
                    if (sNodeName.equals("swe:Quantity")) {
                        String[] id_quan = utils.printAttributes(currentNode.getParentNode().getAttributes()).split("=");
                        if (id_quan[1].indexOf("longitude") > -1) {
                            String[] id_lon = utils.printAttributes(currentNode.getAttributes()).split("=");
                            info.setlong_definition(id_lon[1]);
                        } else if (id_quan[1].indexOf("latitude") > -1) {
                            String[] id_lat = utils.printAttributes(currentNode.getAttributes()).split("=");
                            info.setlat_definition(id_lat[1]);
                        }
                    }
                    if (sNodeName.equals("swe:value")) {

                        String[] id_quan = utils.printAttributes(currentNode.getParentNode().getAttributes()).split("=");
                        if (id_quan[1].indexOf("longitude") > -1) {
                            info.setLongitude(Float.valueOf(sNodeValue.trim()).floatValue());
                        } else if (id_quan[1].indexOf("latitude") > -1) {
                            info.setLatitude(Float.valueOf(sNodeValue.trim()).floatValue());
                        } else {
                            for (int i = 0; i < info.getObsPhenomena().size(); i++) {

                                if (id_quan[1].indexOf(info.getObsPhenomena().elementAt(i).getPhenomena_id().trim()) > -1) {
                                    info.getObsPhenomena().elementAt(i).setValue(sNodeValue);
                                }


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
        } while (tovisit.isEmpty() == false);



    }
}