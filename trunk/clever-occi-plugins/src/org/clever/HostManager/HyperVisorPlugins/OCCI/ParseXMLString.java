package org.clever.HostManager.HyperVisorPlugins.Occi;

import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ParseXMLString {

//    public static void main(String[] args) throws Exception {
//        String xmlRecords =
//                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//                + " <access xmlns=\"http://docs.openstack.org/identity/api/v2.0\">"
//                + "   <token expires=\"2012-10-05T11:51:33Z\" id=\"fcd0f737baff419cba478bd5a34d25c3\">"
//                + "      <tenant enabled=\"true\" id=\"40f49e560cde42c49dd8d9cd7e29b2b2\" name=\"admin\"/>"
//                + "   </token>"
//                + " </access>";
//        ParseXMLString px = new ParseXMLString();
//        System.out.print(px.readToken(xmlRecords));
//    }

    public String readToken(String xmlToken) throws ParserConfigurationException, SAXException, UnsupportedEncodingException, IOException {

        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        DocumentBuilder b = f.newDocumentBuilder();
        Document doc = b.parse(new ByteArrayInputStream(xmlToken.getBytes("UTF-8")));
        NodeList nodes = doc.getElementsByTagName("token");
        Element etoken = (Element) nodes.item(0);

        Node id = etoken.getAttributeNode("id");
        return id.getTextContent();
    }
}