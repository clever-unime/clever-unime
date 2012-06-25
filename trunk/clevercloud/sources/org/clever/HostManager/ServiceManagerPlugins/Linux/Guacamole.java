/*
 *  The MIT License
 *
 *  Copyright 2011 brady.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.clever.HostManager.ServiceManagerPlugins.Linux;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.log4j.Logger;
import org.clever.Common.VEInfo.DesktopVirtualization;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.HostManager.ServiceManager.ServiceObject;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 *
 * @author giovalenti
 */
public class Guacamole {
    private ServiceObject serviceobject;
    private Logger logger;
    private String path;

    private String RegisterVirtualDeskHTML5 = "Virtualization/RegisterVirtualDesktopHTML5";
    private String UnRegisterVirtualDeskHTML5 = "Virtualization/UnRegisterVirtualDesktopHTML5";

    public Guacamole(ServiceObject serviceobject){
        this.logger = Logger.getLogger("Guacamole Class");
        this.serviceobject = serviceobject;
        try {
            FileStreamer fs = new FileStreamer();
            InputStream inxml = getClass().getResourceAsStream("/org/clever/HostManager/ServiceManagerPlugins/Linux/configuration_guacamole.xml");
            ParserXML pXML = new ParserXML(fs.xmlToString(inxml));
            this.path = pXML.getElementContent("pathFileConf");
        } catch (IOException ex) {
            logger.error( "Error: Class Guacamole is not created " + ex );
        }
        this.logger.info("Guacamole Class created: ");
    }

    public void update(){
        try {
            if(this.serviceobject.getOperation().equals(this.RegisterVirtualDeskHTML5))
                this.RegisterNewAccount((DesktopVirtualization)serviceobject.getObject());
            if(this.serviceobject.getOperation().equals(this.UnRegisterVirtualDeskHTML5)){
                this.UnRegisterAccount((String)serviceobject.getObject());
            }
        } catch (Exception ex) {
            this.logger.error("Errore aggiornamento file di configurazione del servizio Guacamole: "+ex);
        }
    }

    public void RegisterNewAccount(DesktopVirtualization desktop) throws Exception{

        File file = new File(this.path);
        ParserXML pXML = new ParserXML(file);

        Element elem;
	Attribute attr;
	Element elem_into;
	Attribute attr_into;
        boolean stop=false;

	List list = pXML.getDocument().getRootElement().getChildren("authorize");
	for (int i=0; i<list.size(); i++){
            elem = (Element) list.get(i);
            if(elem.getAttribute("username").getValue().equals(desktop.getUsername())){
                this.logger.error("Error: username present!!");
		stop=true;
            }
	}

	if(!stop){
            elem = new Element("authorize");
            attr = new Attribute("username",desktop.getUsername());
            elem.setAttribute(attr);

            attr = new Attribute("password",desktop.getUserPassword());
            elem.setAttribute(attr);
            attr = new Attribute("encoding","md5");
            elem.setAttribute(attr);

            elem_into = new Element("protocol");
            elem_into.setText("vnc");
            elem.addContent(elem_into);

            elem_into = new Element("param");
            attr_into = new Attribute("name","hostname");
            elem_into.setAttribute(attr_into);
            elem_into.setText(desktop.getIpVNC());
            elem.addContent(elem_into);

            elem_into = new Element("param");
            attr_into = new Attribute("name","port");
            elem_into.setAttribute(attr_into);
            elem_into.setText(desktop.getPort());
            elem.addContent(elem_into);

            elem_into = new Element("param");
            attr_into = new Attribute("name","password");
            elem_into.setAttribute(attr_into);
            elem_into.setText(desktop.getVmVNCPassword());
            elem.addContent(elem_into);


            pXML.getDocument().getRootElement().addContent(elem);

            pXML.saveXML(this.path);

            this.logger.info("User-Mapping Guacamole updated with Register username "+desktop.getUsername());

        }

    }

    public void UnRegisterAccount(String id) throws Exception{

//        System.out.println(id);

        File file = new File(this.path);
        ParserXML pXML = new ParserXML(file);

        Element elem;
        List list = pXML.getDocument().getRootElement().getChildren("authorize");
	for (int i=0; i<list.size(); i++){
            elem = (Element) list.get(i);
            if(elem.getAttribute("username").getValue().equals(id)){
                elem.detach();
		pXML.saveXML(this.path);
            }
	}

        this.logger.info("User-Mapping Guacamole updated with UnRegister username "+id);
    }

}
