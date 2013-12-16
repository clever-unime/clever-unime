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

                    // System.out.println("node name: "+sNodeName+ " attibute: "+utils.printAttributes(nnmAttributes)+" value: "+sNodeValue);
                    if (sNodeName.equals("AssignedSensorId")) {
                        info.setSensor_id(sNodeValue);
                    }
                    if (sNodeName.equals("gml:timePosition")) {
                        String dataprova = sNodeValue.replace("T", " ").replace("Z", "");
                        // System.out.println("nuovo timestamp"+dataprova);
                        info.setTime_stamp(dataprova);
                    }
                    if (sNodeName.equals("swe:Time")) {
                        String[] id_tim = utils.printAttributes(nnmAttributes).split("=");
                        // System.out.println("nuovo timestamp"+dataprova);
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
                        //System.out.println("dimensione: "+dimension2[1]);
                        phen_length = Integer.parseInt(dimension2[1]);
                        //     System.out.println("dimensione: "+phen_length);
                    }
                    if (sNodeName.equals("gml:name") && currentNode.getParentNode().getNodeName().equals("swe:CompositePhenomenon")) {
                        info.setPhenomenon_composite(sNodeValue);
                    }
                    //System.out.println("id phen: "+sNodeValue);}
                    if (sNodeName.equals("swe:component")) {
                        String[] id_phen = utils.printAttributes(nnmAttributes).split("=");
                        //  System.out.println("id phen: "+id_phen[1]);
                        if (count_phen < phen_length) {
                            ObsPhenomenaValue phen_temp = new ObsPhenomenaValue();
                            phen_temp.setPhenomena_id(id_phen[1]);
                            count_phen++;
                            info.getObsPhenomena().add(phen_temp);
                        }
                        //System.out.println("phen length: "+info.getObsPhenomena().size());

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
                        /* for(int i=0; i<info.getObsPhenomena().size();i++)
                        //System.out.println("phen: "+info.getObsPhenomena().elementAt(i).getPhenomena_id()+" uom " +info.getObsPhenomena().elementAt(i).getUom()+" value: "+info.getObsPhenomena().elementAt(i).getValue());
                         */
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