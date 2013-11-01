/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.CloudMonitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import static org.clever.Common.Communicator.Agent.logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;

/**
 *
 * @author webwolf
 */
public class CloudMonitorAgent extends Agent{
    
    private CloudMonitorPlugin monitorPlugin;
    private Class cl;
    
    
    
    
    public CloudMonitorAgent()  {
        
        super();
        logger=Logger.getLogger("CloudMonitorAgent");  
        
      
    }
    
    @Override
    public void initialization() throws CleverException {
        
        
        
        if (super.getAgentName().equals("NoName")) {
            super.setAgentName("CloudMonitorAgent");
        }

        super.start();
        

        FileStreamer fs = new FileStreamer();

        try {
            //InputStream inxml = getClass().getResourceAsStream("./cfg/configuration_hypervisor.xml");//("/org/clever/HostManager/HyperVisor/configuration_hypervisor.xml");
            //FileInputStream inxml = new FileInputStream("/org/clever/HostManager/CloudMonitor/configuration_cloudmonitor.xml");
            
            InputStream inxml=getClass().getResourceAsStream("/org/clever/HostManager/CloudMonitor/configuration_cloudmonitor.xml");
            
            if(inxml==null)
                logger.debug("The variable inxml is null check configursarion file");
            
            ParserXML pXML = new ParserXML(fs.xmlToString(inxml));
            
            cl = Class.forName(pXML.getElementContent("PluginName"));
            monitorPlugin = (CloudMonitorPlugin) cl.newInstance();
            
            monitorPlugin.init( null,this );
            monitorPlugin.setOwner(this);

            logger.info("CloudMonitorPlugin created ");
            
            
        } catch (ClassNotFoundException ex) {
            
            logger.error("Error: " + ex);
        } catch (IOException ex) {
            logger.error("Error: " + ex);
        } catch (InstantiationException ex) {
            logger.error("Error: " + ex);
        } catch (IllegalAccessException ex) {
            logger.error("Error: " + ex);
        } catch (Exception ex) {
            logger.error("CloudMonitorPlugin creation failed: " + ex);
        }
        
    }
    
    @Override
    public Class getPluginClass() {
        return cl;
    }

    @Override
    public Object getPlugin() {
        return monitorPlugin;
    }

    @Override
    public void shutDown() {
    }

    
    
    
}
