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

import java.io.IOException;
import java.io.StringReader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author alessiodipietro
 */
public class SASAlertMessage {
    private String header;
    private Element body;

    public SASAlertMessage() { 
        body = new Element("Body");
    }

    public String toXml() {


        Element root = new Element("SASAlert");
        Element head = new Element("Header");

        StringReader stringReader = new StringReader(this.header);
        SAXBuilder builder = new SAXBuilder();
        Document docHeader = null;
        try {
            docHeader = builder.build(stringReader);
        } catch (JDOMException ex) {
        } catch (IOException ex) {
        }
        Element xmlHeader = docHeader.getRootElement();
        head.addContent(xmlHeader.detach());
        root.addContent(head);
        Element body = new Element("Body");
        body.addContent(this.body.getValue());
        root.addContent(body);
        Document doc = new Document();
        doc.addContent(root);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        String xmlString = outputter.outputString(doc);
        return xmlString;
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @return the body
     */
    public Element getBody() {
        return body;
    }

    /**
     * @param text the body text to set
     */
    public void setBodyText(String text) {
        this.body.addContent(text);
    }
}
