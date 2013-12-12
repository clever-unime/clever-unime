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
//import org.clever.Common.Shared.LoggerInstantiator;

public class NetworkManagerAgent extends Agent
{
    private NetworkManagerPlugin networkManager;  
    //private Class cl;
    
    
    
    public NetworkManagerAgent() throws  CleverException
    {
       super();
            
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
