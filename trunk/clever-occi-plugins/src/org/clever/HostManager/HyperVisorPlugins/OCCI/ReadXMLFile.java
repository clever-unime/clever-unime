package org.clever.HostManager.HyperVisorPlugins.Occi;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReadXMLFile {

    String server = "";
    String portToken = "";
    String portNova = "";
    String tenant = "";
    String username = "";
    String password = "";
    String portImageGlance = "";
    String portOCCI = "";
     String img = null;
    String flavor = null;

    public ReadXMLFile(String nome) {

        try {

            File fXmlFile = new File(nome);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("configuration");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    this.server = getTagValue("server", eElement);
                    this.portToken = getTagValue("portToken", eElement);
                    this.portNova = getTagValue("portNova", eElement);
                    this.tenant = getTagValue("tenant", eElement);
                    this.username = getTagValue("username", eElement);
                    this.password = getTagValue("password", eElement);
                    this.portImageGlance = getTagValue("portImageGlance", eElement);
                    this.portOCCI = getTagValue("portOCCI", eElement);
                     this.img = getTagValue("img", eElement);
                      this.flavor = getTagValue("flavor", eElement);

                    

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readToken(String xmlRecords) {

        String token = null;

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlRecords);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("access");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    token = getTagValue("token", eElement);


                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;

    }

    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();

        Node nValue = (Node) nlList.item(0);

        return nValue.getNodeValue();
    }
    
    
    
    

//    public static void main(String[] args) {
//        String nome = "/home/davide/NetBeansProjects/ApacheHttp4/src/httpclient4/configuration.xml";
//
//        String xmlRecords =
//                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//                + " <access xmlns=\"http://docs.openstack.org/identity/api/v2.0\">"
//                + "   <token expires=\"2012-10-05T11:51:33Z\" id=\"fcd0f737baff419cba478bd5a34d25c3\">"
//                + "     <tenant enabled=\"true\" id=\"40f49e560cde42c49dd8d9cd7e29b2b2\""
//                + "      <tenant enabled=\"true\" id=\"40f49e560cde42c49dd8d9cd7e29b2b2\" name=\"admin\"/>"
//                + "   </token>"
//                + " </access>";
//
//        ReadXMLFile conf = new ReadXMLFile(nome);
//
//        System.out.println(conf.server);
//        System.out.println(conf.portToken);
//        System.out.println(conf.portNova);
//        System.out.println(conf.tenant);
//        System.out.println(conf.username);
//        System.out.println("password " + conf.password);
//        System.out.println("portImageGlance " + conf.portImageGlance);
//        System.out.println("portOCCI " + conf.portOCCI);
//
//        System.out.println(conf.readToken(xmlRecords));
//    }
    
    
    
}