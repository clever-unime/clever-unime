/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.CloudMonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import static org.clever.Common.Communicator.Agent.logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;

import org.clever.HostManager.CloudMonitor.ThSendMeasure;

/**
 *
 * @author webwolf
 */
public class CloudMonitorAgent extends Agent{
    
    private CloudMonitorPlugin monitorPlugin;
    private Class cl;
    
    private int freq_monitor;
    private boolean flag_monitor; //se true viene attivato il monitoring continuo
    
    
    
    public CloudMonitorAgent()  {
        
        super();

        logger=Logger.getLogger("CloudMonitorAgent");  
        
        flag_monitor = true;
        
      
    }
    
    @Override
    public void initialization() throws CleverException {
        
        
        
        if (super.getAgentName().equals("NoName")) {
            super.setAgentName("CloudMonitorAgent");
        }

        super.start();
      
        
        FileStreamer fs = new FileStreamer();

        try {
            
            InputStream inxml=getClass().getResourceAsStream("/org/clever/HostManager/CloudMonitor/configuration_cloudmonitor.xml");
            
            if(inxml==null)
                logger.debug("The variable inxml is null check configursarion file");
            
            ParserXML pXML = new ParserXML(fs.xmlToString(inxml));
            
            cl = Class.forName(pXML.getElementContent("PluginName"));
            monitorPlugin = (CloudMonitorPlugin) cl.newInstance();
            
            monitorPlugin.init( null,this );
            monitorPlugin.setOwner(this);

            logger.info("CloudMonitorPlugin created!");
            
            
            flag_monitor = Boolean.parseBoolean(pXML.getElementContent("active_monitor"));
            
            if(flag_monitor){
                
                logger.info("Monitoring active!");
                
                freq_monitor= Integer.parseInt( pXML.getElementContent( "freq_monitor" ) ); 

                logger.info("Sample frequency: "+freq_monitor+" sec");

                Thread Monitoring = new Thread(new ThSendMeasure(this, monitorPlugin, freq_monitor ));
                Monitoring.setDaemon(true);
                Monitoring.start();
                
            }
            else
                logger.info("Monitoring disabled!");
            
            
            
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
