/*
 * Copyright 2014 Università di Messina
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
/*
 *  Copyright (c) 2010 Antonio Nastasi
 *  Copyright (c) 2011 Marco Sturiale
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
package org.clever.ClusterManager.Dispatcher;

import org.apache.log4j.Logger;
import org.clever.ClusterManager.Brain.BrainInterface;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Communicator.ThreadMessageDispatcher;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.CleverMessageHandler;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;



public class DispatcherAgent extends CmAgent  implements CleverMessageHandler
{
    private CLusterManagerDispatcherPlugin dispatcherPlugin = null;
    //private Class cl = null;
    private BrainInterface brainInterface;
    private ThreadMessageDispatcher threadMessageDispatcher;
    public ConnectionXMPP connectionXMPP = null;
    
    public DispatcherAgent( ConnectionXMPP connectionXMPP ) throws CleverException 
    {   
        super();
        this.connectionXMPP = connectionXMPP;   
    }
    public DispatcherAgent() throws CleverException{
        super();
    }
    
    @Override
    public void initialization() throws CleverException
    {
        super.setAgentName("DispatcherAgent");
        super.start();
        
        try
        {
            
            dispatcherPlugin = ( CLusterManagerDispatcherPlugin )super.startPlugin("./cfg/configuration_dispatcher.xml","/org/clever/ClusterManager/Dispatcher/configuration_dispatcher.xml");
            dispatcherPlugin.setOwner(this);
            dispatcherPlugin.setConnectionXMMP( this.connectionXMPP );
            logger.info( "Dispatcher created" );
            
            this.threadMessageDispatcher = new ThreadMessageDispatcher(dispatcherPlugin,2000, 20); //TODO: retrieve parameters from configuration file
            this.threadMessageDispatcher.start();
        }
        catch( java.lang.NullPointerException e )
        {
            throw new CleverException( e, "Missing logger.properties or configuration not found" );
        }
        catch( Exception e )
        {
            logger.error("errore generico dispatcherAgent CC");
            throw new CleverException( e );
        }
    }


  @Override
  public Class getPluginClass()
  {
    return cl;
  }

  public CLusterManagerDispatcherPlugin getDispatcherPlugin()
  {
    return dispatcherPlugin;
  }



  @Override
  public Object getPlugin()
  {
    return this.pluginInstantiation;
  }

    @Override
    public synchronized void handleNotification(Notification notification) throws CleverException {
        this.brainInterface.handleNotification(notification);
        this.dispatcherPlugin.handleNotification(notification);
    }

    /**
     * @return the brainInterface
     */
    public BrainInterface getBrainInterface() {
        return brainInterface;
    }

    /**
     * @param brainInterface the brainInterface to set
     */
    public void setBrainInterface(BrainInterface brainInterface) {
        this.brainInterface = brainInterface;
    }

    @Override
    public void handleCleverMessage(CleverMessage message) {
        logger.debug("receivedddd: "+message.getBody());
        this.threadMessageDispatcher.pushMessage(message);
    }

    /*public void sendNotification(Notification notification){
        System.out.println("Received notification");
        dispatcherPlugin.handleNotification(notification);
    }*/


@Override
   public void shutDown()
    {
        
    }

}
