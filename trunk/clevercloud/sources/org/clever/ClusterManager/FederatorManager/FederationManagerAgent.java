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
 *  The MIT License
 *
 *  Copyright (c) 2014 Tricomi Giuseppe
 *  Copyright (c) 2014 Giovanni Volpintesta
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.clever.ClusterManager.FederatorManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.RequestExpired;
import org.clever.Common.Shared.Support;
import org.clever.Common.Storage.VFSDescription;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.UUIDProvider.UUIDProvider;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.CleverMessageHandler;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ExecOperation;
import org.jivesoftware.smack.packet.Presence;
import org.clever.Common.Communicator.Request;
import org.clever.Common.Communicator.RequestsManager;
import org.clever.Common.XMPPCommunicator.RoomListener;
import org.jdom2.Element;
import org.clever.ClusterManager.FederatorManagerPlugins.FederationMessagePoolThread;
import org.clever.ClusterManager.FederatorManagerPlugins.FederationRequestExecutor;

/**
 *
 * @author Giuseppe Tricomi <giu.tricomi@gmail.com>
 * @author Giovanni Volpintesta
 */
public class FederationManagerAgent extends CmAgent implements CleverMessageHandler{
    private String Name="";
    private Agent owner;
    private String agentName="FederationManagerAgent";
    private String nodoFederatedpartner="Matching_FED_Partner";
    private String nodoMigratedVm="Matching_VM_Federator_Host";
    private String nodoSharedVFS="Matching_VFS_Federation";
    private String nodeFederateDomain="Domain_name";
    private String nodefederatedCM="CM_Name";
    private final String cfgPath = "./cfg/configuration_federation.xml";
    private ConnectionXMPP conn = null;
    private String room = "";
    private String username = "";
    private String password = "";
    private String nickname = "";
    private String fedName="";
    
    private String server = "";
    private int port = 0;
    private ParserXML pXML;
    private String domain="";
    private long defaultTimeout;
    private int attempts;
    private RoomListener messageListener;
    private FederationMessagePoolThread federationMessagePoolThread;
    private final HashMap<Integer, Request> requestPool;
    private FederationManagerPlugin federationManagerPlugin;
   //<editor-fold defaultstate="collapsed" desc="SETTER&GETTER METHODS">
    public ConnectionXMPP getConn() {
        return conn;
    }
    public String getFedName(){
        return fedName;
    }

    public FederationManagerPlugin getFederationManagerPlugin() {
        return federationManagerPlugin;
    }
    public void setFedName(String f){
        this.fedName=f;
    }
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
    //</editor-fold>
    @Override
    public synchronized void setPluginState(boolean pluginState) {
        super.setPluginState(pluginState);
        notifyAll();
    }
    
    public FederationManagerAgent() throws CleverException{
        super();
        //if(super.getAgentName().equals("NoName")|| super.getAgentName().equals(""))
        super.setAgentName("FederationManagerAgent");
        this.requestPool = new HashMap<Integer, Request>();
        
    }
    @Override
    public void initialization() throws Exception {
        try {
            if(super.getAgentName().equals("NoName")|| super.getAgentName().equals(""))
               super.setAgentName("FederationManagerAgent");
            super.start();
            federationManagerPlugin=(FederationManagerPlugin)super.startPlugin(cfgPath, "/org/clever/ClusterManager/FederatorManager/configuration_federator.xml");
                this.federationManagerPlugin.setOwner(this);
            
            //If the data struct, for matching between VM and HM, isen't into DB then init it.
                /*this.pXML=this.getconfiguration(cfgPath, "/org/clever/ClusterManager/FederatorManager/configuration_federator.xml");
                this.prepareConnParameter();*/
                //this.connectionEstablished();
                this.federationMessagePoolThread = new FederationMessagePoolThread (this);
                this.federationMessagePoolThread.setLogger(logger);
                this.federationMessagePoolThread.start();
                logger.info("FederatioMessagePoolThread started");
                this.messageListener = new RoomListener (this);
                //this.setPluginState(true);
                }
            catch (Exception e) {
                logger.error(e.getMessage(),e);
                this.owner.setPluginState(false);
            }
        
    }

    @Override
    public Class getPluginClass() {
        return this.cl;
    }

    @Override
    public Object getPlugin() {
        return this;
    }

    @Override
    public void shutDown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleNotification(Notification notification) throws CleverException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void handleCleverMessage (final CleverMessage msg)  {
        if (msg.getDst().compareTo(this.username)==0) {
            //logger.debug ("Message: " + msg.toXML());
            switch (msg.getType()) {
                case REQUEST:
                    FederationRequestExecutor th = new FederationRequestExecutor (this, msg, logger);
                    th.start();
                    break;
                case REPLY:{
                    logger.debug ("REPLY id risposta: "+msg.getId());
                    logger.debug("Prendo la request alla key "+msg.getId()+" dalla hashmap che fa da pool");
                    
                }
                    
                case ERROR:
                    logger.debug ("id risposta: "+msg.getId());
                    logger.debug("Prendo la request alla key "+msg.getId()+" dalla hashmap che fa da pool");
                    Integer ID = new Integer(msg.getReplyToMsg());
                    if (!this.requestPool.containsKey(ID)) //a timeout occurred
                        break;
                    Request request = this.requestPool.get(ID);
                    logger.debug("Request = "+request);
                    requestPool.remove(ID);
                    try {
                        Object replyObject = msg.getObjectFromMessage();
                        logger.debug("reply object: "+replyObject);
                        request.setReturnValue(replyObject);
                        logger.debug("setted return value to request");
                    } catch (CleverException ex) {
                        logger.info("Exception retrieving object from message: "+ex.getMessage());
                        request.setReturnValue(ex);
                    }
                    break;
                default:
                    logger.error("Message type is "+msg.getType()+". FederationListenerAgent isn't allowed to manage a type of message different to REQUEST, REPLY or ERROR. You need to implement managing of other type of messages.");
            }
        }
    }
    
  public FederationMessagePoolThread getFederationMessagePoolThread() {
        return this.federationMessagePoolThread;
    }
    
    public HashMap<Integer, Request> getRequestPool() {
        return this.requestPool;
    }
    
    public void sendMessage (final CleverMessage msg) {
       String target = msg.getDst();
        this.conn.sendMessage(target, msg);
    }
    /**
     * NOT USED HERE
     * @param vfsD
     * @param otherCMName
     * @return 
     */
    /*private boolean make_sharedVSF(VFSDescription vfsD,String otherCMName){
        //TODO: this function have to substituited with an s3 shared node
        String name="FederatedNode_"+this.Name+"_"+otherCMName;
        boolean testingVariable=false;
        try{
            ArrayList params = new ArrayList();
            params.add(name);
            params.add("mount");
            params.add(vfsD);
            params.add("");
            testingVariable=(Boolean)this.owner.invoke("StorageManagerAgent", "createNode", true, params); 
        }
        catch(Exception e){}
        return testingVariable;
    }*/
    
    private boolean registerFederationElement(functionEnum fE,FederatorDataContainer container){
        boolean result=false;
        
        switch (fE){
            case VM_Migration:{
                
            }
            case CreateFederation:{
                
            }
            
        }
        
        return result;
    }
    
    public static enum functionEnum
    {
        VM_Migration,CreateFederation;
    }
    //<editor-fold defaultstate="collapsed" desc="Management federation status on DB METHODS">
    /**
     * This method must be launched by a CM that becomes active.
     * Parameters nick and domain are the ones of the CM that has
     * just become active, so that this CM can add it in SednaDB, and then this
     * method returns an array containing the domain [0] and the nickname [1]
     * of this CM, so that the CM that has just become active can add them to its
     * SednaDB.
     * @param nick
     * @param domain
     * @return 
     * @throws org.clever.Common.Exceptions.CleverException
     */
    /*public String[] addAsActiveCMandReply (String domain, String nick) throws CleverException {
        this.insertFederationOnDBHigh(this.fedName, domain, nick);
        logger.info(nick+" cm added to federation for "+domain+" domain.");
        //logger.info("Actual Federation:\n"+this.scanFederation().toString());
        String[] reply = new String[2];
        reply[0] = this.domain;
        reply[1] = this.conn.getUsername();
        logger.info("Nick and domain of this CM was communicated to CM "+nick);
        return reply;
    }*/
    
    /**
     * Returns CM active for selected domain
     * @param domain
     * @return
     * @throws CleverException 
     */
     /*private String getCM (String domain) throws CleverException {
        String location = "/"+this.nodoFederatedpartner+"/"+this.nodeFederateDomain+"[@name='"+domain+"']";
        ArrayList params = new ArrayList();
        params.add(this.agentName);
        params.add(location);
        params.add("/"+this.nodefederatedCM+"/text()");
        String result = (String) this.owner.invoke("DatabaseManagerAgent", "getContentNodeXML", true, params);
        if (result.isEmpty())
            return null;
        else
            return result;
    }*/
    /**
     * Returns an HashMap containing all the domains present in the federation
     * and the CM of each of them.
     * This Function have to controlled because it is not properly work
     * @return 
     */
    /*private HashMap<String,String> scanFederation () throws CleverException {
        HashMap<String,String> result = new HashMap<String,String>();
        ArrayList<Object> params = new ArrayList<Object>();
        String location = "/cm/agent[@name=\""+this.agentName+"\"]/"+this.nodoFederatedpartner;
        
        params.add(location);
        String xml = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        //logger.debug(xml);
        //xml = this.decodeSednaXml(xml);
        ParserXML pXML=new ParserXML(xml);
        Element root=pXML.getRootElement();
        ArrayList<Element> l=(ArrayList<Element>)root.getChildren(this.nodeFederateDomain);
        
        for(Element elem:l){
           String dominio=elem.getAttributeValue("Name");
           String cmNick=elem.getChild(this.nodefederatedCM).getValue();
           result.put(dominio, cmNick);
        }
        
        return result;
    }*/
    /**
     * Return the array List of all other domains in federation 
     * @return
     * @throws CleverException 
     */
    /*public ArrayList<String> getFederatedDomains() throws CleverException {
        HashMap<String, String> federation = this.scanFederation();
        ArrayList<String> result = new ArrayList<String> (federation.keySet());
        result.remove(this.domain);
        return result;
    }*/
    
    /**
     * This function is used to send a command to another federated CM.
     * @param cm
     * @param agent
     * @param command
     * @param hasReply
     * @param params
     * @param timeout
     * @return
     * @throws CleverException 
     */
    /*private Object forwardCommandToCM(String cm, String agent, String command, boolean hasReply, List params, long timeout) throws CleverException {
        //logger.debug("Timeout = "+timeout);
        if (timeout < 0)
            timeout = this.defaultTimeout;
        logger.debug("Timeout = "+timeout);
        CleverMessage msg = new CleverMessage ();
        String src = this.conn.getUsername();
        int id =org.clever.Common.UUIDProvider.UUIDProvider.getPositiveInteger();// new Integer(id);
        logger.debug("ID for this msg: "+id);
        msg.fillMessageFields(CleverMessage.MessageType.REQUEST, src, cm, hasReply, params, new ExecOperation(command, params, agent), id);
        msg.setId(id);
        Object result;
        int tries;
        for (tries=0; tries<this.attempts; tries++) {
            Request request = new Request (msg.getId(), timeout);
            //aggiungo la request alla requestPool
            this.requestPool.put(org.clever.Common.UUIDProvider.UUIDProvider.getPositiveInteger(), request);
            logger.debug("Launching "+command+" method of "+agent+" agent to CM="+cm+").");
            logger.debug ("Message type: "+msg.getType().toString()+" type value: "+msg.getType().ordinal()+" id: "+msg.getId());
            this.sendMessage(msg);
            logger.info("Launched "+command+" method of "+agent+" agent to CM="+cm+").");
            logger.debug("Waiting and retrieving result for "+command+" method of "+agent+" agent, lauched to CM="+cm+").");
            try {
                result = request.getReturnValue();
                logger.info(command+" method of "+agent+" agent successfully launched to CM="+cm+" after "+tries+" failed tries.");
                return result;
            } catch (CleverException ex) {
                if (ex instanceof RequestExpired) {
                    logger.info ("The launch of "+command+" method of "+agent+" to CM="+cm+" failed due to reached timeout (attempt "+(tries+1)+"). Trying to relaunch it.");
                    //continue the cycle.
                } else
                    throw ex;
            }
        }
        //If arrived here, the attempts of failed tries have been reached
        throw new org.clever.Common.Exceptions.RequestAborted ("The launch of "+command+" method of "+agent+" to CM="+cm+" failed due to reached timeout for "+tries+" attempts. Request aborted");
    }
   
    private Object forwardCommandToDomain(String domain, String agent, String command, boolean hasReply, List params, List<String> history, long timeout) throws CleverException {
        history.add(domain); //questa riga deve essere la prima perchè deve essere eseguita prima di incorrere in qualsiasi eccezione
        logger.info(domain+" domain added to the history of the method.");
        if (!this.existsDomain(domain))
            throw new CleverException (domain+" domain doesn't exist in federation");
        String cm = this.getCM(domain);
        logger.info("Preparing to launch "+command+" method of "+agent+" agent to "+domain+" domain (CM="+cm+").");
        return this.forwardCommandToCM(cm, agent, command, hasReply, params, timeout);
    }*/
        //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="INITIALIZATION Agent METHODS">
   /* private void prepareConnParameter(){
        if(this.pXML!=null){
            this.domain=this.pXML.getElementContent("domain");
            this.server=this.pXML.getElementContent("server");
            this.port=Integer.parseInt(this.pXML.getElementContent("port"));
            this.room=this.pXML.getElementContent("room");
            this.username=this.pXML.getElementContent("username");
            this.password=this.pXML.getElementContent("password");
            this.nickname=this.pXML.getElementContent("nickname");
            this.fedName=this.pXML.getElementContent("fedName");
            this.defaultTimeout=Long.parseLong(this.pXML.getElementContent("defaultTimeout"));
            this.attempts=Integer.parseInt(this.pXML.getElementContent("attempts"));
        }
    }
    */
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="INITIALIZATION DB METHODS">
  /*  private boolean checkDBAgent()throws CleverException{
        List params = new ArrayList();
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "isCorrectedStarted", true, params); 
    
    }
    private boolean checkVFS_FEDNode() throws CleverException{
       List params = new ArrayList();
        params.add(this.agentName);
        params.add("/"+this.nodoSharedVFS); //XPath location with eventual predicate
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params); 
    }
    
    private boolean checkVmMigratedNode() throws CleverException{
         
        List params = new ArrayList();
        params.add(this.agentName);
        params.add("/"+this.nodoMigratedVm); //XPath location with eventual predicate
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params); 
    }

    private boolean checkMatchingFEDElemNode() throws CleverException{
        List params = new ArrayList();
        params.add(this.agentName);
        params.add("/"+this.nodoFederatedpartner); //XPath location with eventual predicate
        try{
            logger.debug("verifica del nodo check fedHM");
            return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params);
        }catch(Exception e){
            logger.error(e.getMessage());
            return false;
        }
    }
    
    private void initVFS_FEDNode() throws CleverException{
        String node="<"+this.nodoSharedVFS+"/>";
        List params = new ArrayList();
        params.add(this.agentName);
        params.add(node);
        params.add("into");
        params.add(""); //XPath location with eventual predicate
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
    
    private void initVmMigratedNode() throws CleverException{
        String node="<"+this.nodoMigratedVm+"/>";
        List params = new ArrayList();
        params.add(this.agentName);
        params.add(node);
        params.add("into");
        params.add(""); //XPath location with eventual predicate
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
     }
     
    private void initMatchingFEDElemNode() throws CleverException{
        String node="<"+this.nodoFederatedpartner+"/>";
        List params = new ArrayList();
        params.add(this.agentName);
        params.add(node);
        params.add("into");
        params.add(""); //XPath location with eventual predicate
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
        
    }*/
    /**
     * 
     * @param fedid, in this moment this parameter is not needed, because always matchs with fedName parameter, but is added for future uses(case CLOUD connected to several federations)
     * @param cmname, name of the last active CM for domain 
     * @param domainname, name of domain 
     * @return
     * @throws CleverException 
     */
   /* private void insertFederationOnDBHigh(String fedid,String domainname,String cmname) throws CleverException{
        List params = new ArrayList();
        //TODO: verificare il funzionamento in alternativa si faranno due chiamate prima questa params.add("cm/agent[@name=\""+this.agentName+"\"]/"+this.nodoFederatedHm+"/"+this.nodeFederateDomain+"[@name=\""+domainname+"\"]
        //e dopo quella ripotata sotto
        params.add("/cm/agent[@name=\""+this.agentName+"\"]/"+this.nodoFederatedpartner+"/"+this.nodeFederateDomain+"[@name=\""+domainname+"\"]/"+this.nodefederatedCM+"/text()");
        String r = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);     
        logger.debug("query result is:"+r);
        if(r==null||r.equals("")){
            logger.debug("insert direct on Sedna federation");
            insertFederationOnDBLow(fedid,domainname,cmname);
        }
        else{
            logger.debug("insert after delete on Sedna federation");
            params.clear();
            params.add(this.agentName);
            params.add("/"+this.nodoFederatedpartner+"/"+this.nodeFederateDomain+"[@name=\""+domainname+"\"]");
            this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
            insertFederationOnDBLow(fedid,domainname,cmname);
        }
        
    }
    private void insertFederationOnDBLow(String fedid,String domainname,String cmname)throws CleverException{
        List params = new ArrayList();
        //TODO: inserire la gestione per la multi federazione 
        //String node="<"+this.nodoFederatedpartner+" name=\""+fedid+"\" > "+
        String node="<"+this.nodeFederateDomain+" name=\""+domainname+"\"><"+this.nodefederatedCM+">"+cmname+"</"+this.nodefederatedCM+"></"+this.nodeFederateDomain+">";
        //</"+this.nodoFederatedpartner+">";
        params = new ArrayList();
        params.add(this.agentName);
        params.add(node);
        params.add("into");
        params.add("/"+this.nodoFederatedpartner);
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
    private boolean existsDomain (String domainname) throws CleverException{
        List params = new ArrayList();
        boolean result=false;
        //TODO: verificare il funzionamento in alternativa si faranno due chiamate prima questa params.add("cm/agent[@name=\""+this.agentName+"\"]/"+this.nodoFederatedHm+"/"+this.nodeFederateDomain+"[@name=\""+domainname+"\"]
        //e dopo quella ripotata sotto
        params.add(this.agentName);
        params.add("/"+this.nodoFederatedpartner+"/"+this.nodeFederateDomain+"[@name=\""+domainname+"\"]");
        result = (Boolean)this.invoke("DatabaseManagerAgent", "existNode", true, params);     
        logger.debug("query result is:"+result);
        return result;
    }
    */
    //</editor-fold>
    
    
    //<editor-fold defaultstate="collapsed" desc="XMPP CONNECTION METHODS">
    /**
     * Creates a new connection to the FEDERATION server and add the agent himself
     * to che chat listeners.
     * @throws java.lang.Exception
     */
    public void connectionEstablished () throws Exception {
        this.conn = new ConnectionXMPP();
        logger.info("Connection to federation's server XMPP: "+server +" at port: "+port);
        this.conn.connect(server, port);
        logger.info("authentication with federation's XMPP server....");  
        this.conn.authenticate (username, password);
        logger.info("authenticated with federation's XMPP server!");
        //Not joining to any room because only the active CM can join    
    }
    /**
     * This function will be used when the Cluster Manager pass to active state to make connection
     * with Federation room
     * @param active 
     */
    public synchronized void setAsActiveCM (Boolean active) {
        try{
        if (active) {
            do{
                if(!this.isPluginState())
                {
                    logger.debug("START federation wait");
                    wait(3000);
                    logger.debug("STOP federation wait");
                }
            }while(!this.isPluginState());
            //if(this.conn==null){
                this.connectionEstablished();
            //}
            
            logger.debug("ConnectionXMPP.ROOM.FEDERATION = " + ConnectionXMPP.ROOM.FEDERATION);
            logger.debug("conn = " + this.conn);
            logger.debug("room = " + room);
            logger.debug("username = " + this.conn.getUsername());
            this.conn.joinInRoom(room, ConnectionXMPP.ROOM.FEDERATION, this.conn.getUsername(), "CM_ACTIVE"); //Tutti gli occupanti della chat sono CM_ACTIVE. Qui si potrebbe aggiungere il dominio come stato in modo da poter identificare i domini
            logger.info("CM joined in FEDERATION room");
            logger.debug("MultiUserChat = " + this.conn.getMultiUserChat(ConnectionXMPP.ROOM.FEDERATION,room));
            //Il getMultiUserChat va fatto dopo essersi uniti alla room
            this.conn.getMultiUserChat(ConnectionXMPP.ROOM.FEDERATION,room).changeAvailabilityStatus("CM_ACTIVE", Presence.Mode.chat ); //set the status of this FederationListenerAgent active
            this.conn.getMultiUserChat(ConnectionXMPP.ROOM.FEDERATION,room).addMessageListener(this.messageListener);
            logger.info("Room listener added in FEDERATION MultiUserChat");
            this.conn.addChatManagerListener(this);
            logger.info("Chat manager added for the connection with federation server");
            ((FederationManagerPlugin)this.pluginInstantiation).initAsActive();
            logger.info("Plugin initialized as ActiveCM");
        } else {
            //TODO Verify this functionality
            this.conn.getMultiUserChat(ConnectionXMPP.ROOM.FEDERATION,room).changeAvailabilityStatus( "CM_MONITOR", Presence.Mode.away );
            this.conn.getMultiUserChat(ConnectionXMPP.ROOM.FEDERATION,room).removeMessageListener(this.messageListener);
        }
        }catch(Exception e){
            logger.error("Error occurred in FederatorAgent method :SetASActiveCM ",e);
        }
    }
    
    
    
    
    //</editor-fold>
}
