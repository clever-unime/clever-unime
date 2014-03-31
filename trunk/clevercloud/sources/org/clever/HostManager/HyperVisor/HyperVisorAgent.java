/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
 *  Copyright (c) 2012 Marco Carbone
 *
 */
package org.clever.HostManager.HyperVisor;


import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.apache.log4j.Logger;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;

public class HyperVisorAgent extends Agent {

    private HyperVisorPlugin hypervisor;
    //private Class cl;
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger = null;
    private String pathLogConf="/sources/org/clever/HostManager/HyperVisorPlugins/VirtualBox/log_conf/";
    private String pathDirOut="/LOGS/HostManager/HyperVisor";
    //########
    
    
    public HyperVisorAgent() throws CleverException  {
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
      
       
        logger.info("\n\nHyperVisorAgent Started!\n\n");
        
        if (super.getAgentName().equals("NoName")) {
            super.setAgentName("HyperVisorAgent");
        }
        super.start();
        try 
        {
            
            hypervisor = (HyperVisorPlugin) super.startPlugin("./cfg/configuration_hypervisor.xml","/org/clever/HostManager/HyperVisor/configuration_hypervisor.xml");        
            hypervisor.setOwner(this);
            logger.info("HyperVisorPlugin created ");
            
        } catch (Exception ex) {
            logger.error("HyperVisorPlugin creation failed: " + ex.getMessage());
            this.errorStr=ex.getMessage();
        }
    }

    @Override
    public Class getPluginClass() {
        return cl;
    }

    @Override
    public Object getPlugin() {
         
        return this.pluginInstantiation;
    }

    @Override
    public void shutDown() {
    }

       
}
