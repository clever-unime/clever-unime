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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author user
 */
public class utils {
       protected static String searchTextInElement(Node elementNode) {
             String sText = "";
             if (elementNode.hasChildNodes()) {
                    //Il child node di tipo testo è il primo
                    Node nTextChild = elementNode.getChildNodes().item(0);
                    sText = nTextChild.getNodeValue();
             }
             return sText;
       }
 
       protected static String printAttributes(NamedNodeMap nnm) {
             String sAttrList = new String();
             if (nnm != null && nnm.getLength() > 0) {
                    for (int iAttr=0; iAttr < nnm.getLength(); iAttr++) {
                           sAttrList += nnm.item(iAttr).getNodeName();
                           sAttrList += "=";
                           sAttrList += nnm.item(iAttr).getNodeValue();
                           sAttrList += "; ";
                    }
                    return sAttrList;
             }
             else {
                    return "assenti";
             }
       }
}
