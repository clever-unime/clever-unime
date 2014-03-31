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
package org.clever.HostManager.ServiceManager;

import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.jdom.Element;

/**
 *
 * @author giovalenti
 */
public class ServiceManagerAgent extends Agent {

    private ServiceManagerPlugin service_manager;
    private Class cl;
    
     //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/ServiceManager/log_conf/";
    private String pathDirOut="/LOGS/HostManager/ServiceManager";
    //########

    public ServiceManagerAgent() {
        super();
        
      //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("ServiceManager");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
    }

    @Override
    public Class getPluginClass() {
        return this.cl;
    }

    @Override
    public Object getPlugin() {
        return this.service_manager;
    }

    @Override
    public void initialization() throws Exception {
        //TODO: implement initialization
         FileStreamer fs = new FileStreamer();
       try {
            InputStream inxml = getClass().getResourceAsStream("/org/clever/HostManager/ServiceManager/configuration_ServiceManager.xml");
            ParserXML pXML = new ParserXML(fs.xmlToString(inxml));
            cl = Class.forName(pXML.getElementContent("ServiceManager"));
            service_manager = (ServiceManagerPlugin) cl.newInstance();
            service_manager.setOwner(this);
            Element pp = pXML.getRootElement().getChild("pluginParams");
            if (pp != null) {
                service_manager.init(pp, this);
            } else {
                service_manager.init(null, this);
            }

            logger.debug("called init of " + pXML.getElementContent("ServiceManager"));
            logger.info("ServiceManagerAgent created ");
        } catch (ClassNotFoundException ex) {
            logger.error("Error: " + ex);
        } catch (IOException ex) {
            logger.error("Error: " + ex);
        } catch (InstantiationException ex) {
            logger.error("Error: " + ex);
        } catch (IllegalAccessException ex) {
            logger.error("Error: " + ex);
        } catch (Exception ex) {
            logger.error("ServiceManagerPlugin creation failed: " + ex);
        }
    }

    @Override
    public void shutDown() {
        //TODO: implement shutdown
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
