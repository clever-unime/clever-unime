/*
 *  Copyright (c) 2010 Patrizio Filloramo
 *  Copyright (c) 2010 Salvatore Barbera
 *  Copyright (c) 2010 Antonio Nastasi
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
package org.clever.HostManager.NetworkManager;


import java.util.logging.Level;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import java.io.IOException;
//import org.apache.log4j.*;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
//import org.clever.Common.Shared.LoggerInstantiator;

public class NetworkManagerAgent extends Agent
{
    private NetworkManagerPlugin networkManager;  
    private Class cl;
      //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/NetworkManager/log_conf/";
    private String pathDirOut="/LOGS/HostManager/NetworkManager";
    //########
    
    
    public NetworkManagerAgent() throws IOException
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
       
        try 
        {
            super.start();
            
            FileStreamer fs = new FileStreamer();
            InputStream inxml = getClass().getResourceAsStream( "/org/clever/HostManager/NetworkManager/configuration_networkManager.xml" );
            ParserXML pXML = new ParserXML( fs.xmlToString( inxml ) );
            
            logger.info( "NetworkManagerPlugin creating " );
            
            cl = Class.forName( pXML.getElementContent( "NetworkManager" ) );
            networkManager = ( NetworkManagerPlugin ) cl.newInstance();
            
            logger.debug( "called init of " + pXML.getElementContent( "NetworkManager" ) );
            networkManager.setOwner(this);
            //agentName=pXML.getElementContent( "moduleName" );
            //mc = new ModuleCommunicator(agentName);
            //mc.setMethodInvokerHandler( this );
            
            logger.info( "NetworkManagerPlugin created " );
        }
        catch (CleverException ex) 
        {
            java.util.logging.Logger.getLogger(NetworkManagerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch( ClassNotFoundException ex )
        {
            logger.error( "Error: " + ex );
        }
        catch( IOException ex )
        {
            logger.error( "Error: " + ex );
        }
        catch( InstantiationException ex )
        {
            logger.error( "Error: " + ex );
        }
        catch( IllegalAccessException ex )
        {
            logger.error( "Error: " + ex );
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
    return networkManager;
  }
  
  @Override
   public void shutDown()
    {
        
    }
}
