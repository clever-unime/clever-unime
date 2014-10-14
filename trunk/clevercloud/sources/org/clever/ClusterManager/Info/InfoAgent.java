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
 *  Copyright (c) 2011 Marco Sturiale
 *  Copyright (c) 2011 Alessio Di Pietro
 *  Copyright (c) 2012 Marco Carbone
 *  Copyright (c) 2013 Nicola Peditto
 *  Copyright (c) 2013 Carmelo Romeo
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
package org.clever.ClusterManager.Info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Initiator.ModuleFactory.ModuleFactory;
import org.clever.Common.Shared.HostEntityInfo;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.jivesoftware.smackx.muc.Occupant;



public class InfoAgent extends CmAgent
{
    private String version = "0.0.1";
    private String description = "Information about Cluster Manager";
    private ConnectionXMPP connectionXMPP = null;
    private  ArrayList <CmAgent> Agents = new ArrayList(3);
    private ModuleFactory mf;
    
    public InfoAgent( ConnectionXMPP connectionXMPP ) throws CleverException
    {   super();
        this.connectionXMPP= connectionXMPP;
    }
    
    @Override
    public void initialization()throws CleverException
    {        
        super.setAgentName("InfoAgent");
        super.start();
        mf = ModuleFactory.getInstance();
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

    public List listHostManager(){
        Collection <Occupant>  list_HC=connectionXMPP.getHCsInRoom(ConnectionXMPP.ROOM.CLEVER_MAIN);
        Occupant occupant = null;
        ArrayList l2 = new ArrayList();
        HostEntityInfo hostManager;

        for (Iterator i = list_HC.iterator(); i.hasNext();) {

            occupant=(Occupant) i.next();
            hostManager= new HostEntityInfo();
            hostManager.setNick(occupant.getNick());
            hostManager.setActive(true);
            l2.add(hostManager);

        }
        return l2;
    }
    
    //NEWMONITOR
    /**
    * Return the list of the active VM probe
    * @return a list of HostEntityInfo object (VM probes)
    */      
    public List listProbe(){
        logger.debug("listProbe is called!");
        Collection <Occupant>  list_HC=connectionXMPP.getProbesInRoom(ConnectionXMPP.ROOM.CLEVER_MAIN);
        Occupant occupant = null;
        ArrayList l2 = new ArrayList();
        HostEntityInfo hostManager;

        for (Iterator i = list_HC.iterator(); i.hasNext();) {

            occupant=(Occupant) i.next();
            hostManager= new HostEntityInfo();
            hostManager.setNick(occupant.getNick());
            hostManager.setActive(true);
            l2.add(hostManager);

        }
        return l2;
    }
    
    
    public List listClusterManager(){
        Collection <Occupant>  list_HC=connectionXMPP.getCCsInRoom(ConnectionXMPP.ROOM.CLEVER_MAIN);
        Occupant occupant = null;
        ArrayList l2 = new ArrayList();
        HostEntityInfo clusterManager;
        String CCActive = connectionXMPP.getActiveCC(ConnectionXMPP.ROOM.CLEVER_MAIN);

        for (Iterator i = list_HC.iterator(); i.hasNext();) {

            occupant=(Occupant) i.next();
            clusterManager= new HostEntityInfo();
            clusterManager.setNick(occupant.getNick());
            if( CCActive.equals(occupant.getNick()) )
                clusterManager.setActive(true);
            else
                clusterManager.setActive(false);
            l2.add(clusterManager);
        }
        return l2;
        
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
    public void handleNotification(Notification notification) throws CleverException {
        logger.debug("Received notification type: "+notification.getId());
    }

    
    @Override
   public void shutDown()
    {
        
    }
    
}
