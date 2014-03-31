/*
 *  The MIT License
 * 
 *  Copyright 2011 brady.
 */
package org.clever.HostManager.ServiceManager;

import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;

/**
 *
 * @author giovalenti
 */
public class ServiceManagerAgent extends Agent {

    private ServiceManagerPlugin service_manager;
    //private Class cl;
    
     //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/ServiceManager/log_conf/";
    private String pathDirOut="/LOGS/HostManager/ServiceManager";
    //########
    

    public ServiceManagerAgent() throws CleverException {
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
        return this.pluginInstantiation;
    }

    @Override
    public void initialization() throws Exception {
      
        
      //logger.debug("Debug Message! su ServiceManagerAgent.java");
      //logger.info("Info Message! su ServiceManagerAgent.java");
      //logger.warn("Warn Message! su ServiceManagerAgent.java");
      //logger.error("Error Message! su ServiceManagerAgent.java");
      //logger.fatal("Fatal Message! su ServiceManagerAgent.java"); 
      
      
       try {
           service_manager = (ServiceManagerPlugin) super.startPlugin("./cfg/configuration_ServiceManager.xml","/org/clever/HostManager/ServiceManager/configuration_ServiceManager.xml");
           service_manager.setOwner(this);
           logger.info("ServiceManagerAgent created ");
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
