 /*
 * The MIT License
 *  Copyright (c) 2010 Antonino Longo
 *  Copyright (c) 2012 Marco Carbone
 *
 */
package org.clever.ClusterManager.DatabaseManager;


import java.io.IOException;
import org.clever.Common.Communicator.Notification;
import org.apache.log4j.*;
import java.util.ArrayList;
import java.util.List;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;




public class DatabaseManagerAgent extends CmAgent
{
    private DatabaseManagerPlugin DbManagerPlugin;
    //private Class cl;
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger=null;
    private String pathLogConf="/sources/org/clever/ClusterManager/DatabaseManager/conf_log";
    private String pathDirOut="/LOGS/ClusterManager/DatabaseManager";
    //########
    
    
    public DatabaseManagerAgent() throws CleverException 
    {
        super();
        //#############################################
        //Inizializzazione meccanismo di logging
        logger=Logger.getLogger("DatabaseManager");    
        Log4J log =new Log4J();
       log.setLog4J(logger, pathLogConf, pathDirOut);
       //#############################################    
              
    }
    
    
    @Override
    public void initialization() throws CleverException, IOException
    {
        
            
    //////
   //logger.debug("Debug Message! su DatabaseManager.java");
   //logger.info("Info Message!  su DatabaseManager.java");
   //logger.warn("Warn Message!  su DatabaseManager.java");
   //logger.error("Error Message!  su DatabaseManager.java");
   //logger.fatal("Fatal Message!  su DatabaseManager.java");
   /////// 
        
        if(super.getAgentName().equals("NoName"))
            super.setAgentName("DatabaseManagerAgent");
        
        super.start();
        try
        {
            this.startPlugin();
            
            List params = new ArrayList();
            params.add(super.getAgentName());
            params.add("PRESENCE/HM");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);
         }
        catch( java.lang.NullPointerException e )
        { 
            throw new CleverException( e, "Missing logger.properties or configuration not found" );       
        }
        catch( Exception e )
        {
            throw new CleverException( e );
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
    public void handleNotification(Notification notification) throws CleverException {
        if(notification.getId().equals("PRESENCE/HM")){            
            logger.debug("Received notification type "+notification.getId());
            if(!DbManagerPlugin.checkHm(notification.getHostId())){
                DbManagerPlugin.addHm(notification.getHostId());
            }
        }
    }
   
    public void startPlugin()throws CleverException, IOException{
        try
        {
                        
         //   Properties prop = new Properties();
         //   InputStream in = getClass().getResourceAsStream( "/org/clever/Common/Shared/logger.properties" );
         //   prop.load( in );
         //   PropertyConfigurator.configure( prop );
            
            DbManagerPlugin = ( DatabaseManagerPlugin )super.startPlugin("./cfg/configuration_dbManagerPlugin.xml","/org/clever/ClusterManager/DatabaseManager/configuration_dbManagerPlugin.xml");
            /*InputStream inxml = getClass().getResourceAsStream( "/org/clever/ClusterManager/DatabaseManager/configuration_dbManagerPlugin.xml" );
            ParserXML pXML = new ParserXML( fs.xmlToString( inxml ) );
            
            cl = Class.forName( pXML.getElementContent( "DbManagerPlugin" ) );
             cl.newInstance();
            
            logger.debug( "called init of " + pXML.getElementContent( "DbManagerPlugin" ) );
            
            DbManagerPlugin.init( pXML.getRootElement().getChild( "pluginParams" ),this );
            
            //agentName=pXML.getElementContent( "moduleName" );
            DbManagerPlugin.setOwner(this);*/
           this.DbManagerPlugin.setOwner(this);
            logger.info( "DbManagerPlugin created " );
            
        }
        catch( java.lang.NullPointerException e )
        { 
            throw new CleverException( e, "Missing logger.properties or configuration not found" );       
        }
        catch( Exception e )
        {
            throw new CleverException( e );
        }
    }
    
    @Override
   public void shutDown()
    {
        
    }
   
       
}
