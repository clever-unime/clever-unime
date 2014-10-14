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
 * The MIT License
 *
 * Copyright 2011 alessiodipietro.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.clever.HostManager.Info;

/**
 *
 * @author alessiodipietro
 */
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Initiator.ModuleFactory.ModuleFactory;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;



public class InfoAgent extends Agent
{
    private String version = "0.0.1";
    private String description = "Information about Host Manager";
    private ConnectionXMPP connectionXMPP = null;
    private  ArrayList <Agent> Agents = new ArrayList(3);
    private ModuleFactory mf;
    
    public InfoAgent( ConnectionXMPP connectionXMPP ) throws CleverException
    {
        //nota questo infoagent deve avere un nome diverso dall'info agentr del cluster managre residente
        //sullo stesso host, altrimenti si verificano problemi di registrazione sul bus!
        super();
       
        
        mf = ModuleFactory.getInstance();
        
        try
        {
            this.connectionXMPP= connectionXMPP;
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
    public InfoAgent() throws CleverException{
        super();
    }
    
    @Override
  public void initialization()
  {
      super.setAgentName("InfoAgent");
      
      try 
      {
          super.start();
      }
      catch (CleverException ex) 
      {
          java.util.logging.Logger.getLogger(InfoAgent.class.getName()).log(Level.SEVERE, null, ex);
      }
  }

    @Override
    public Class getPluginClass() {
        return InfoAgent.class;
    }

    @Override
    public Object getPlugin() {
        return this;
    }


    public String getVersion() {
        return version;
    }


    public String getDescription() {
        return description;
    }

    
    public void createAgent(String className){
        mf.createAgent(className);
            
    }

    public void addActiveAgent(String agentName){
        mf.addActiveAgents(agentName); 
    }
    
    public List listActiveAgents(){
        return mf.listActiveAgents();
    }
    
    @Override
   public void shutDown()
    {
        
    }
}
