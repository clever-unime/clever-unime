 /*
 *  Copyright (c) 2010 Antonino Longo
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
package org.clever.ClusterManager.DatabaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.apache.log4j.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;




public class DatabaseManagerAgent extends CmAgent
{
    private DatabaseManagerPlugin DbManagerPlugin;
    private Class cl;
    private String configPath="./cfg/configuration_dbManagerPlugin.xml";
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger=null;
    private String pathLogConf="/sources/org/clever/ClusterManager/DatabaseManager/conf_log";
    private String pathDirOut="/LOGS/ClusterManager/DatabaseManager";
    //########
    
    public DatabaseManagerAgent() 
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
        if(super.getAgentName().equals("NoName"))
            super.setAgentName("DatabaseManagerAgent");
        
        super.start();
        
        
        FileStreamer fs = new FileStreamer();     
        
        try
        {
         //   Properties prop = new Properties();
         //   InputStream in = getClass().getResourceAsStream( "/org/clever/Common/Shared/logger.properties" );
         //   prop.load( in );
         //   PropertyConfigurator.configure( prop );
            File configfile=new File(this.configPath);
            InputStream inxml;
            
            if(!configfile.exists()){
                
                
                inxml= getClass().getResourceAsStream( "/org/clever/ClusterManager/DatabaseManager/configuration_dbManagerPlugin.xml" );
            }
            else
            {
                inxml = new FileInputStream(this.configPath);
                
            }
            ParserXML pXML = new ParserXML( fs.xmlToString( inxml ) );
            
            cl = Class.forName( pXML.getElementContent( "DbManagerPlugin" ) );
            DbManagerPlugin = ( DatabaseManagerPlugin ) cl.newInstance();
            
            logger.debug( "called init of " + pXML.getElementContent( "DbManagerPlugin" ) );
            
            DbManagerPlugin.init( pXML.getRootElement().getChild( "pluginParams" ),this );
            
            //agentName=pXML.getElementContent( "moduleName" );
            
            logger.info( "DbManagerPlugin created " );
            DbManagerPlugin.setOwner(this);
            List params = new ArrayList();
            params.add(super.getAgentName());
            params.add("PRESENCE/HM");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);
        }
        catch( java.lang.NullPointerException e )
        { 
            throw new CleverException( e, "Missing logger.properties or configuration not found" );       
        }
        catch( java.io.IOException e )
        {
            throw new CleverException( e, "Error on reading logger.properties" );
        }
        catch( ClassNotFoundException e )      
        {
            throw new CleverException( e, "Plugin Class not found" );
        }
        catch( InstantiationException e )
        {
            throw new CleverException( e, "Plugin Instantiation error" );
        }
        catch( IllegalAccessException e )
        {
            throw new CleverException( e, "Error Access" );
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
        return DbManagerPlugin;
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
    
    @Override
    public void shutDown()
    {
        
    }
}
