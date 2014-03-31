/*
 *  Copyright (c) 2010 Patrizio Filloramo
 *  Copyright (c) 2010 Salvatore Barbera
 *  Copyright (c) 2010 Antonio Nastasi
 *
 */
package org.clever.HostManager.NetworkManager;


import org.clever.Common.Exceptions.CleverException;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
//import org.clever.Common.Shared.LoggerInstantiator;

public class NetworkManagerAgent extends Agent
{
    private NetworkManagerPlugin networkManager;  
    //private Class cl;
     //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/NetworkManager/log_conf/";
    private String pathDirOut="/LOGS/HostManager/NetworkManager";
    //########
    
    
    public NetworkManagerAgent() throws  CleverException
    {
       super();
      
       //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("NetworkManager");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
       
    }
    
    @Override
    public void initialization()
    {
      
      
      
      
        
      if(super.getAgentName().equals("NoName"))
        super.setAgentName("NetworkManagerAgent");
        logger.info( "NetworkManagerPlugin Started" );
        try 
        {
            super.start();
            networkManager = ( NetworkManagerPlugin )super.startPlugin("./cfg/configuration_networkManager.xml","/org/clever/HostManager/NetworkManager/configuration_networkManager.xml");
            networkManager.setOwner(this);
            logger.info( "NetworkManagerPlugin created " );
        }
        catch (CleverException ex) 
        {
            logger.error("CleverException in Network Agent initialization"+ex);
        }
        catch( Exception ex )
        {
            logger.error( "NetworkManagerPlugin creation failed: " + ex );
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
