/*
 *  The MIT License
 * 
 *  Copyright (c) 2013 Nicola Peditto
 *  Copyright (c) 2013 Carmelo Romeo
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

package org.clever.HostManager.CloudMonitor;

import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;

import org.clever.HostManager.CloudMonitor.ThSendMeasure;


public class CloudMonitorAgent extends Agent{
    
    private CloudMonitorPlugin monitorPlugin;
    private Class cl;
    
    
    private int freq_monitor;
    private boolean flag_monitor; //se true viene attivato il monitoring continuo
    
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/CloudMonitor/log_conf/";
    private String pathDirOut="/LOGS/HostManager/CloudMonitor";
    //########
    
    
    
    
    
    public CloudMonitorAgent()  {
        
        super();

      //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("CloudMonitorAgent");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
        
              
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
            logger.error("CloudMonitorPlugin creation failed: " + ex,ex);
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
