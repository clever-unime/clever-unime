/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleTransactional;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author user
 */
public class utils {

    public static String searchTextInElement(Node elementNode) {
        String sText = "";
        if (elementNode.hasChildNodes()) {
            //Il child node di tipo testo è il primo
            Node nTextChild = elementNode.getChildNodes().item(0);
            sText = nTextChild.getNodeValue();
        }
        return sText;
    }

    public static String printAttributes(NamedNodeMap nnm) {
        String sAttrList = new String();
        if (nnm != null && nnm.getLength() > 0) {
            for (int iAttr = 0; iAttr < nnm.getLength(); iAttr++) {
                sAttrList += nnm.item(iAttr).getNodeName();
                sAttrList += "=";
                sAttrList += nnm.item(iAttr).getNodeValue();
                sAttrList += "; ";
            }
            return sAttrList;
        } else {
            return "assenti";
        }
    }
}
