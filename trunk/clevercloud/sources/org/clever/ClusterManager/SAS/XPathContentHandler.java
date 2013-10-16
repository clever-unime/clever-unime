/*
 * The MIT License
 *
 * Copyright 2011 alessiodipietro.
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
package org.clever.ClusterManager.SAS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author alessiodipietro
 */
public class XPathContentHandler extends DefaultHandler {
    private String xPath = "./";
    private XMLReader xmlReader;
    private XPathContentHandler parent;
    private StringBuilder characters = new StringBuilder();
    private Map<String, Integer> elementNameCount = new HashMap<String, Integer>();
    private StringBuffer buffer=null;

    public XPathContentHandler(XMLReader xmlReader,StringBuffer buffer) {
        this.xmlReader = xmlReader;
        this.buffer=buffer;
    }

    private XPathContentHandler(String xPath, XMLReader xmlReader, XPathContentHandler parent, StringBuffer buffer) {
        this(xmlReader,buffer);
        this.xPath = xPath;
        this.parent = parent;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        Integer count = elementNameCount.get(qName);
        if(null == count) {
            count = 1;
        } else {
            count++;
        }
        elementNameCount.put(qName, count);
        String childXPath = xPath + "/" + qName + "[" + count + "]";
        
        int attsLength = atts.getLength();
        for(int x=0; x<attsLength; x++) {
            childXPath += "[@" + atts.getQName(x) + "='" + atts.getValue(x) + ']';
            //System.out.println(childXPath + "[@" + atts.getQName(x) + "='" + atts.getValue(x) + ']');
        }

        XPathContentHandler child = new XPathContentHandler(childXPath, xmlReader, this,buffer);
        xmlReader.setContentHandler(child);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String value = characters.toString().trim();
        if(value.length() > 0) {
            buffer.append(xPath + "='" + characters.toString() + "' and ");
            //System.out.println(xPath + "='" + getCharacters().toString() + "'");
        }
        xmlReader.setContentHandler(parent);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        characters.append(ch, start, length);
    }
    
    @Override
    public void endDocument(){
        buffer.delete(buffer.lastIndexOf(" and "), buffer.length());
    }

    

    
    
}
