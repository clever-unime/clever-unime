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
import java.util.logging.Level;
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
    private  ArrayList <String> objectStorageActive = new ArrayList();
    private ModuleFactory mf;
    
    public InfoAgent( ConnectionXMPP connectionXMPP ) throws CleverException
    {   super();
        this.connectionXMPP= connectionXMPP;
    }
    
    @Override
    public void initialization()throws CleverException
    {     
        ArrayList params=new ArrayList();
        super.setAgentName("InfoAgent");
        super.start();
        mf = ModuleFactory.getInstance();
        params.add("InfoAgent");
        params.add("OBS/Presence");
        this.invoke("DispatcherAgent", "subscribeNotification", true, params);
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
        if(notification.getId().equals("OBS/Presence")){
            this.managePresence(notification);
        }
    }

    
    @Override
   public void shutDown()
    {
        
    }
   
   /**
    * this method is used to find host name which have a specific Agent active
    * @param agentName name of agent which you want found
    * @return host name if agent is active, null otherwise
    */
   public String getHostActive(String agentName){
   
       boolean isActive=false;
       String hostName= null;
       List<HostEntityInfo> listHostManager;
       int count;
       ArrayList params= new ArrayList();
       ArrayList<String> listUsed= new ArrayList();
       ArrayList<String> listAll= new ArrayList();
       HostEntityInfo host = null;
       
       //fai la ricerca e restituisci null se nessun host soddisfa i requisiti
   //richiedo la lista degli hostmanager attivi
       listHostManager=this.listHostManager();
       logger.debug("lista host manager ottenuta");
       params.add(agentName);
   //ad ogni hostmanager attivo richiedo se ha l'agente attivo
       for(count=0;count<listHostManager.size();count++){
           try {
               host=(HostEntityInfo) listHostManager.get(count);
               logger.debug("remote invocation verso: "+host.getNick());
               isActive=(boolean) this.remoteInvocation(host.getNick(), "InfoAgent" , "isActiveAgent" , true, params);
           } catch (CleverException ex) {
               logger.error("errore nella remote invocation",ex);
           }
           //se attivo lo aggiungo in lista
           if(isActive){
               listAll.add(host.getNick());
           }
       }
       logger.debug("lista degli host con l'agente"+ agentName +" attivo ricevuta");
/*controllo la dimensione della lista:
       se è zero allora non ci sono host con al'agente attivo
       se è 1 allora posso restituire l'unico attivo ed aggiungerlo nella lista used 
       nei restanti casi devo fare il polling
  */
       if(listAll.isEmpty()){
           return null;
       }
       else
           if(listAll.size()==1){
               hostName =listAll.get(0);
               listUsed.add(0,hostName);
           return hostName;
           }
           else{
           return this.getHostName(listAll,listUsed);
           }
   
   }
   
   /**
    * questo metodo restituisce la prima occorrenza che in listAll è presente ed il listUsed no
    * se tutte le occorrenze di listAll sono presenti il listUsed allora viene svuotata listUsed e viene
    * restituito il primo valore di listAll
    * @param listAll lista con tutte le occorrenze
    * @param listUsed lista con le occorrenze già utilizzate
    * @return ocvcorrenza presente in listAll e non il listUsed
    */
   private String getHostName(ArrayList listAll,ArrayList listUsed){
       String hostName;
       int countAll;
       boolean isPresent;
       
    //se la lista Used è vuota vuol dire che siamo alla prima iterazione quindi va bene che io inserisca uno qualsiasi
    //degli elementi della lista All
       
       if(listUsed.isEmpty()){
           hostName=(String) listAll.get(0);
           listUsed.add(hostName);
           return hostName;
       }
       else{
           /*
           mi faccio restituire gli elementi di lista All e li scorro fino a trovare la
           prima occorrenza non presente in Used
           */
           for(countAll=0;countAll<listAll.size();countAll++){
               hostName=(String) listAll.get(countAll);
               isPresent=listUsed.contains(hostName);
                    if(!isPresent){
                        listUsed.add(hostName);
                        return hostName;
                    }
           }
           /*
           se tutti gli elementi di listAll sono presenti in listUsed allora svuoto listUsed 
           estraggo il primo elemento di listAll lo inserisco in listUsed e ritorno il valore
           */
           listUsed.clear();
           hostName=(String) listAll.get(0);
           listUsed.add(hostName);
           return hostName;
           
       
       
       }
   }
   /**
    * arriva una notifica estrapolo nome host, e lo inserisco nella mia lista locale
    * ed invio il nome a Sedna 
    * @param notification 
    */
   public void managePresence(Notification notification){
       
       List<String> params = new ArrayList();
       String nomeHost=notification.getHostId();
       objectStorageActive.add(nomeHost);
       
   
   
   }
    /*
   dopo inizializzazione controllo sedna se ricevo elemento vuoto allora devo fare 
   il polling e richiedere il reinvio della obs/presence altrimenti me ne frego
   c'è la possibilità di perdere qualcosa ma la probabilità è bassa
   */
}
