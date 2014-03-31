/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
 *
 */
package org.clever.HostManager.Monitor;

import org.clever.Common.Exceptions.CleverException;
import org.apache.log4j.Logger;

import org.clever.Common.Communicator.Agent;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;





public class MonitorAgent extends Agent
{
    private MonitorPlugin monitorPlugin;
    //private Class cl;
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/Monitor/log_conf/";
    private String pathDirOut="/LOGS/HostManager/Monitor";
    //########
   

  public MonitorAgent() throws CleverException
  {   
      super();
     //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("MonitorAgent");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
      
  }
  
   @Override
    public void initialization()
    {
              
        if(super.getAgentName().equals("NoName"))
            super.setAgentName("MonitorAgent");
        
        
      try 
        {
            super.start();
            logger.debug( "MonitorPlugin start creation" );
            monitorPlugin = (MonitorPlugin)super.startPlugin("./cfg/configuration_monitor.xml","/org/clever/HostManager/Monitor/configuration_monitor.xml");
            monitorPlugin.setOwner(this);
            logger.info( "MonitorPlugin Created" );
            
        }
        
        catch (CleverException ex) 
        {
            logger.error("CleverException is occurred in Monitor Agent initialization.Message"+ex.getMessage());
        }
        catch( Exception e )
        {
            logger.error( "MonitorPlugin creation failed: " + e );
        }
        
            
    
    }
    
        
    
   
   @Override
   public Class getPluginClass()
   {
       return cl;
   }


  @Override
  public Object getPlugin()
  {
    return this.pluginInstantiation;
  }
  
  @Override
   public void shutDown()
    {
        
    }
   
     
}
