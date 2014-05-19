/*
 * Copyright [2014] [Università di Messina]
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
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
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
package org.clever.HostManager.Monitor;

import java.util.logging.Level;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import java.io.InputStream;
import org.apache.log4j.Logger;

import org.clever.Common.Communicator.Agent;





public class MonitorAgent extends Agent
{
    private MonitorPlugin monitorPlugin;
    //private Class cl;
    
   

  public MonitorAgent() throws CleverException
  {   
      super();
         
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
            monitorPlugin = ( MonitorPlugin )super.startPlugin("./cfg/configuration_monitor.xml","/org/clever/HostManager/Monitor/configuration_monitor.xml");
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
