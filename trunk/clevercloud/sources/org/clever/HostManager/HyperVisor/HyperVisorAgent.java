/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
 *  Copyright (c) 2012 Marco Carbone
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */
package org.clever.HostManager.HyperVisor;

import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
//import java.util.logging.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import java.io.FileInputStream;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;

public class HyperVisorAgent extends Agent {

    private HyperVisorPlugin hypervisor;
    private Class cl;
    
     //########
    //Dichiarazioni per meccanismo di logging
    Logger logger = null;
    private String pathLogConf="/sources/org/clever/HostManager/HyperVisorPlugins/VirtualBox/log_conf/";
    private String pathDirOut="/LOGS/HostManager/HyperVisor";
    //########
    
    
    public HyperVisorAgent()  {
        super();
      //############################################
      //Inizializzazione meccanismo di logging
      //logger = Logger.getLogger("HyperVisorAgent");
      logger = Logger.getLogger("VirtualBoxPlugin");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //############################################# 
      
    }

    @Override
    public void initialization() throws CleverException {
        if (super.getAgentName().equals("NoName")) {
            super.setAgentName("HyperVisorAgent");
        }

        super.start();

        FileStreamer fs = new FileStreamer();

        try {
            //InputStream inxml = getClass().getResourceAsStream("./cfg/configuration_hypervisor.xml");//("/org/clever/HostManager/HyperVisor/configuration_hypervisor.xml");
            FileInputStream inxml = new FileInputStream("./cfg/configuration_hypervisor.xml");
            if(inxml==null)
                logger.debug("The variable inxml is null check configursarion file");
            ParserXML pXML = new ParserXML(fs.xmlToString(inxml));
            cl = Class.forName(pXML.getElementContent("HyperVisor"));
            hypervisor = (HyperVisorPlugin) cl.newInstance();
            hypervisor.init(pXML.getRootElement().getChild("pluginParams"), this); 
            hypervisor.setOwner(this);
            logger.debug("called init of " + pXML.getElementContent("HyperVisor"));

            // agentName=pXML.getElementContent( "moduleName" );          
            logger.info("HyperVisorPlugin created ");
        } catch (ClassNotFoundException ex) {
            logger.error("Error: " + ex);
        } catch (IOException ex) {
            logger.error("Error: " + ex);
        } catch (InstantiationException ex) {
            logger.error("Error: " + ex);
        } catch (IllegalAccessException ex) {
            logger.error("Error: " + ex);
        } catch (Exception ex) {
            logger.error("HyperVisorPlugin creation failed: " + ex);
        }
    }

    @Override
    public Class getPluginClass() {
        return cl;
    }

    @Override
    public Object getPlugin() {
        return hypervisor;
    }

    @Override
    public void shutDown() {
    }

    
  
}
