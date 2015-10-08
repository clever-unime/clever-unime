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
 *  Copyright (c) 2013 Giovanni Volpintesta
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

package org.clever.ClusterManager.FederatorManagerPlugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.FederatorManager.FederationManagerPlugin;
import org.clever.ClusterManager.FederatorManager.FederationManagerAgent;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Request;
import org.clever.Common.Communicator.RequestsManager;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.RequestAborted;
import org.clever.Common.Exceptions.RequestExpired;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ExecOperation;
import org.jdom2.Element;
import org.clever.Common.UUIDProvider.UUIDProvider;
import org.clever.ClusterManager.FederatorManager.FederatorDataContainer;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.XMLTools.MessageFormatter;

/**
 *
 * @author Giovanni Volpintesta
 * @author Giuseppe Tricomi <giu.tricomi@gmail.com>
 */
public class FederationManagerSimple implements FederationManagerPlugin{

    private Hashtable operation_destination;
    private Hashtable domain_cm;
    private Hashtable cm_domain;
    //private Hashtable diskCopiedOnCommonVFS;
    private Agent owner;
    private Logger logger;
    private String domain;
    private long defaultTimeout;
    private int attempts;
    private ConnectionXMPP conn;
    private String nodoFederatedpartner="Matching_FED_Partner";
    private String nodoMigratedVm="Matching_VM_Federator_Host";
    private String nodoSharedVFS="Matching_VFS_Federation";
    private String nodeFederateDomain="Domain_name";
    private String nodefederatedCM="CM_Name";
    private String nodefederatedOperation="Migration_Operation";
    private RequestsManager requestsManager=null;
    /*private String room = "";
    private String username = "";
    private String password = "";
    private String nickname = "";
    private String server = "";
    private int port = 0;
    private ParserXML pXML;
    private String fedName="";*/
        
    @Override
    public void setOwner(Agent owner) {
        this.owner = (CmAgent) owner;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setConnection(ConnectionXMPP conn) {
        this.conn = conn;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    @Override
    public void init(Element params, Agent owner) throws CleverException {
        this.owner=owner;
        logger=Logger.getLogger("FederationManagerPlugin");
        logger.debug("initfedplugin");
        if(params!=null){
            ((FederationManagerAgent)this.owner).setDomain(params.getChildText("domain"));
            ((FederationManagerAgent)this.owner).setServer(params.getChildText("server"));
            ((FederationManagerAgent)this.owner).setPort(Integer.parseInt(params.getChildText("port")));
            ((FederationManagerAgent)this.owner).setRoom(params.getChildText("room"));
            ((FederationManagerAgent)this.owner).setUsername(params.getChildText("username"));
            ((FederationManagerAgent)this.owner).setPassword(params.getChildText("password"));
            ((FederationManagerAgent)this.owner).setNickname(params.getChildText("nickname"));
            ((FederationManagerAgent)this.owner).setFedName(params.getChildText("fedName"));
            ((FederationManagerAgent)this.owner).setDefaultTimeout(Long.parseLong(params.getChildText("defaultTimeout")));
            ((FederationManagerAgent)this.owner).setAttempts(Integer.parseInt(params.getChildText("attempts")));
            
        }
        this.requestsManager=new RequestsManager();
        this.owner.setPluginState(true);
        this.domain_cm=new Hashtable<String,String>();
        this.cm_domain=new Hashtable<String,String>();
        this.operation_destination=new Hashtable<String,String>();
       // this.forwardCommand4VMM("7ffbfedf21884c07a2020d0573277fe2", null);
    }
    
    
    private ArrayList<String> getFederatedCMs () {
        return conn.getFederatedCMs(((FederationManagerAgent)this.owner).getFedName());
    }
    private ArrayList<String> getFederatedDomains (ArrayList cms) {
        ArrayList tmp= new ArrayList();
        Iterator i=cms.iterator();
        while(i.hasNext()){
            tmp.add(this.cm_domain.get(i.next()));
        }
        return tmp;
    }
    /**
     * This method actually returns a random domain from the HashMap, but it's written as a method
     * because in future there could be more complex politics to decide the domain.
     * @param federation
     * @return 
     */
    private String chooseDomain (HashMap<String,String> federation) {
        int N = federation.size();
        Random rnd = new Random();
        Set<String> domains = federation.keySet();
        return (String) domains.toArray()[rnd.nextInt(N)];
    }
    
 
    @Override
    public void initAsActive(){
        try{
            if (this.checkDBAgent()){
                if (!this.checkVFS_FEDNode()) {
                    this.initVFS_FEDNode();
                }
                if (!this.checkMatchingFEDElemNode()) {
                    this.initMatchingFEDElemNode();
                }
                if(!this.checkVmMigratedNode()){
                    this.initVmMigratedNode();
                }
                if(!this.checkFED_OpNode()){
                    this.initFED_OPNode();
                }
                if(!this.existsDomain(this.domain)){
                    
                    this.insertFederationOnDBHigh(((FederationManagerAgent)this.owner).getFedName(), ((FederationManagerAgent)this.owner).getDomain(),((FederationManagerAgent)this.owner).getConn().getUsername());
               
                }
                //todo: in questa parte manca la parte di algoritmo che gestisce correttamente l'aggiornamento dei domini
                ArrayList<String> federatedCMs =(ArrayList<String>)((FederationManagerAgent)this.owner).getConn().getFederatedCMs(((FederationManagerAgent)this.owner).getRoom());
                ArrayList<Object> params = new ArrayList<Object>();
                params.add(((FederationManagerAgent)this.owner).getDomain());
                params.add(((FederationManagerAgent)this.owner).getConn().getUsername());
                for(Object cmInFed: federatedCMs.toArray()){
                    try{
                        logger.debug("sto per invocare forwardCommandToCM per: "+ cmInFed+" "+"FederationManagerAgent");//((FederationManagerAgent)this.owner).getName());
                        String[] reply = (String[]) this.forwardCommandToCM ((String)cmInFed, "FederationManagerAgent", "addAsActiveCMandReply", true, params, this.defaultTimeout);
                        if(reply.length!=0)
                            this.insertFederationOnDBHigh(((FederationManagerAgent)this.owner).getFedName(), reply[0],reply[1]);
                    }catch(CleverException ce){
                        logger.error("A Cleverexception:"+ce.getMessage()+" is occurred!",ce);
                    }
                    catch(Exception e){
                        logger.error("A generic Exception is occured!",e);
                    }
                }
                
                
            }
            this.testmigrate();
        }
        catch(CleverException Ce){
            logger.error("A CleverException is throwed in initAsActive function",Ce);
        }
        catch(Exception e){
            logger.error("An Exception is generated in initAsActive function",e);
        }
    }

    
    /**
     * This method choose one of the federated CMs and launch the method using it
     * as target.
     * List containing 2 Objects:
     * 1) String representing the nickname of the choosen CM
     * 2) Object returned by the called method
     * @param hasReply
     * @param command
     * @param params
     * @param agent
     * @param timeout
     * @return
     * @throws org.clever.Common.Exceptions.CleverException
     */
   
    @Override
    public Object forwardCommandToDomainWithTimeout(String domain, String agent, String command, Boolean hasReply, ArrayList params, Long timeout) throws CleverException {
        ArrayList<String> history = new ArrayList<String>();
        history.add(this.domain); //questo serve per evitare loop, in modo che non si possa rilanciare il metodo sul primo dominio che ne ha richiesto il lancio
        return this.forwardCommandToDomain(domain, agent, command, hasReply.booleanValue(), params, history, timeout.longValue());
    }
    
    @Override
    public Object forwardCommandToDomain(String domain, String agent, String command, Boolean hasReply, ArrayList params) throws CleverException {
        return this.forwardCommandToDomainWithTimeout(domain, agent, command, hasReply, params, new Long(this.defaultTimeout));
    }
    @Override
    public ArrayList<String> getFederatedDomains() throws CleverException {
        ArrayList<String> result=new ArrayList(this.cm_domain.values());
        return result;
    }
    
    @Override
    public String getLocalDomainName() {
        return this.domain;
    }

    @Override
    public void setDefaultTimeout(long t) {
        this.defaultTimeout = t;
    }

    @Override
    public void setAttempts(int n) {
        this.attempts = n;
    }
    
      //<editor-fold defaultstate="collapsed" desc="INITIALIZATION DB METHODS">
    /**
     * Function used to verify Database ManagerAgent State. 
     * The presence of this agent is mandatory for correct Federator life cicle. 
     * @return boolean
     * @throws CleverException 
     */
    private boolean checkDBAgent()throws CleverException{
        List params = new ArrayList();
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "isCorrectedStarted", true, params); 
    
    }
    /**
     * Function used to verify presence of VFS_FED Node in XML DB Structure. 
     * The presence of this agent is mandatory for correct Federator life cicle. 
     * @return
     * @throws CleverException 
     */
    private boolean checkVFS_FEDNode() throws CleverException{
       List params = new ArrayList();
        params.add(this.owner.getAgentName());
        params.add("/"+this.nodoSharedVFS); //XPath location with eventual predicate
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params); 
    }
    /**
     * Function used to verify presence of FEDERATED Operation Node in XML DB Structure. 
     * The presence of this agent is mandatory for correct Federator life cicle. 
     * @return
     * @throws CleverException 
     */
    private boolean checkFED_OpNode() throws CleverException{
       List params = new ArrayList();
        params.add(this.owner.getAgentName());
        params.add("/"+this.nodefederatedOperation); //XPath location with eventual predicate
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params); 
    }
    /**
     * Function used to verify presence of VM_Migration Node in XML DB Structure. 
     * The presence of this agent is mandatory for correct Federator life cicle.
     * @return
     * @throws CleverException 
     */
    private boolean checkVmMigratedNode() throws CleverException{
         
        List params = new ArrayList();
        params.add(this.owner.getAgentName());
        params.add("/"+this.nodoMigratedVm); //XPath location with eventual predicate
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params); 
    }
    /**
     * Function used to verify presence of FederationElement Node in XML DB Structure. 
     * The presence of this agent is mandatory for correct Federator life cicle.
     * @return
     * @throws CleverException 
     */
    private boolean checkMatchingFEDElemNode() throws CleverException{
        List params = new ArrayList();
        params.add(this.owner.getAgentName());
        params.add("/"+this.nodoFederatedpartner); //XPath location with eventual predicate
        try{
            logger.debug("verifica del nodo check fedHM");
            return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params);
        }catch(Exception e){
            logger.error(e.getMessage());
            return false;
        }
    }
    /**
     * Function used to create VFS_FED Node in XML DB Structure. 
     * The presence of this agent is mandatory for correct Federator life cicle.
     * @throws CleverException 
     */
    private void initVFS_FEDNode() throws CleverException{
        String node="<"+this.nodoSharedVFS+"/>";
        List params = new ArrayList();
        params.add(this.owner.getAgentName());
        params.add(node);
        params.add("into");
        params.add(""); //XPath location with eventual predicate
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
    /**
     * Function used to create Federated operation Node in XML DB Structure. 
     * The presence of this agent is mandatory for correct Federator life cicle.
     * @throws CleverException 
     */
    private void initFED_OPNode() throws CleverException{
        String node="<"+this.nodefederatedOperation+"/>";
        List params = new ArrayList();
        params.add(this.owner.getAgentName());
        params.add(node);
        params.add("into");
        params.add(""); //XPath location with eventual predicate
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
    /**
     * Function used to create VM_Migration Node in XML DB Structure. 
     * The presence of this agent is mandatory for correct Federator life cicle.
     * @throws CleverException
     */
    private void initVmMigratedNode() throws CleverException {
        String node = "<" + this.nodoMigratedVm + "/>";
        List params = new ArrayList();
        params.add(this.owner.getAgentName());
        params.add(node);
        params.add("into");
        params.add(""); //XPath location with eventual predicate
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
    /**
     * Function used to create FederationElement Node in XML DB Structure. 
     * The presence of this agent is mandatory for correct Federator life cicle.
     * @throws CleverException 
     */ 
    private void initMatchingFEDElemNode() throws CleverException{
        String node="<"+this.nodoFederatedpartner+"/>";
        List params = new ArrayList();
        params.add(this.owner.getAgentName());
        params.add(node);
        params.add("into");
        params.add(""); //XPath location with eventual predicate
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
        
    }
    /**
     * This function is used to manage the federation information.
     * @param fedid, in this moment this parameter is not needed, because always matchs with fedName parameter, but is added for future uses(case CLOUD connected to several federations)
     * @param cmname, name of the last active CM for domain 
     * @param domainname, name of domain 
     * @return
     * @throws CleverException 
     */
    private void insertFederationOnDBHigh(String fedid,String domainname,String cmname) throws CleverException{
        List params = new ArrayList();
        //TODO:NOTA RELATIVA ALLO SVILUPPO FUTURO CHE PREVEDE LA GESTIONE DELLA MULTIFEDERAZIONE
        // verificare il funzionamento in alternativa si faranno due chiamate prima questa params.add("cm/agent[@name=\""+this.agentName+"\"]/"+this.nodoFederatedHm+"/"+this.nodeFederateDomain+"[@name=\""+domainname+"\"]
        //e dopo quella ripotata sotto
        logger.debug("insertFederationOnDBHigh");
        params.add("/cm/agent[@name=\""+this.owner.getAgentName()+"\"]/"+this.nodoFederatedpartner+"/"+this.nodeFederateDomain+"[@name=\""+domainname+"\"]/"+this.nodefederatedCM+"/text()");
        String r = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);     
        logger.debug("query result is:"+r);
        if(r==null||r.equals("")){
            logger.debug("insert direct on Sedna federation");
            insertFederationOnDBLow(fedid,domainname,cmname);
        }
        else{
            logger.debug("insert after delete on Sedna federation");
            params.clear();
            params.add(this.owner.getAgentName());
            params.add("/"+this.nodoFederatedpartner+"/"+this.nodeFederateDomain+"[@name=\""+domainname+"\"]");
            this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
            insertFederationOnDBLow(fedid,domainname,cmname);
        }
        
    }
    /**
     * This function is used to write on XML DB the information passed by High Level Function.
     * @param fedid
     * @param domainname
     * @param cmname
     * @throws CleverException 
     */
    private void insertFederationOnDBLow(String fedid,String domainname,String cmname)throws CleverException{
        List params = new ArrayList();
        //TODO:NOTA RELATIVA ALLO SVILUPPO FUTURO CHE PREVEDE LA GESTIONE DELLA MULTIFEDERAZIONE
        // inserire la gestione per la multi federazione 
        //String node="<"+this.nodoFederatedpartner+" name=\""+fedid+"\" > "+
        String node="<"+this.nodeFederateDomain+" name=\""+domainname+"\"><"+this.nodefederatedCM+">"+cmname+"</"+this.nodefederatedCM+"></"+this.nodeFederateDomain+">";
        //</"+this.nodoFederatedpartner+">";
        params = new ArrayList();
        params.add(this.owner.getAgentName());
        params.add(node);
        params.add("into");
        params.add("/"+this.nodoFederatedpartner);
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
    /**
     * Function used to verify if the domain information is present on XMLDB.
     * @param domainname
     * @return
     * @throws CleverException 
     */
    private boolean existsDomain (String domainname) throws CleverException{
        List params = new ArrayList();
        boolean result=false;
        //TODO: NOTA RELATIVA ALLO SVILUPPO FUTURO CHE PREVEDE LA GESTIONE DELLA MULTIFEDERAZIONE
        //verificare il funzionamento in alternativa si faranno due chiamate prima questa params.add("cm/agent[@name=\""+this.agentName+"\"]/"+this.nodoFederatedHm+"/"+this.nodeFederateDomain+"[@name=\""+domainname+"\"]
        //e dopo quella ripotata sotto
        params.add(this.owner.getAgentName());
        params.add("/"+this.nodoFederatedpartner+"/"+this.nodeFederateDomain+"[@name=\""+domainname+"\"]");
        result = (Boolean)this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);     
        logger.debug("query result is:"+result);
        return result;
    }
    
    //</editor-fold>
    
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
    public String[] addAsActiveCMandReply (String domain, String nick) throws CleverException {
        this.insertFederationOnDBHigh(((FederationManagerAgent)this.owner).getFedName(), domain, nick);
        logger.info(nick+" cm added to federation for "+domain+" domain.");
        if(!this.domain_cm.containsKey(domain)){
            
            this.domain_cm.put(domain, nick);
            
            this.cm_domain.put(nick, domain);
        }
        else{
            this.domain_cm.remove(domain);
            this.domain_cm.put(domain, nick);
            this.cm_domain.remove(nick);
            this.cm_domain.put(nick, domain);
        }
        //logger.info("Actual Federation:\n"+this.scanFederation().toString());
        String[] reply = new String[2];
        reply[0] = ((FederationManagerAgent)this.owner).getDomain();
        reply[1] = ((FederationManagerAgent)this.owner).getConn().getUsername();
        logger.info("Nick and domain of this CM was communicated to CM "+nick);
        return reply;
    }
    
    /**
     * Returns CM active for selected domain
     * @param domain
     * @return
     * @throws CleverException 
     */
     private String getCM (String domain) throws CleverException {
        String location = "/"+this.nodoFederatedpartner+"/"+this.nodeFederateDomain+"[@name='"+domain+"']";
        ArrayList params = new ArrayList();
        params.add(this.owner.getAgentName());
        params.add(location);
        params.add("/"+this.nodefederatedCM+"/text()");
        String result = (String) this.owner.invoke("DatabaseManagerAgent", "getContentNodeXML", true, params);
        if (result.isEmpty())
            return null;
        else
            return result;
    }
    
    
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
    private Object forwardCommandToCM(String cm, String agent, String command, boolean hasReply, List params, long timeout) throws CleverException {
        //logger.debug("Timeout = "+timeout);
        if (timeout < 0)
            timeout = ((FederationManagerAgent)this.owner).getDefaultTimeout();
        logger.debug("Timeout = "+timeout);
        CleverMessage msg = new CleverMessage ();
        String src = ((FederationManagerAgent)this.owner).getConn().getUsername();
        //int id =org.clever.Common.UUIDProvider.UUIDProvider.getPositiveInteger();// new Integer(id);
        msg.fillMessageFields(CleverMessage.MessageType.REQUEST, src, cm, hasReply, params, new ExecOperation(command, params, agent), 0);
        
         int id=this.requestsManager.addSyncRequestPending(msg, Request.Type.EXTERNAL,0); //= new Request (msg.getId(), timeout);
            
        
        logger.debug("ID for this msg: "+id);
        msg.setId(id);
        Object result;
        
        
           msg.setId(id);
            //add request at requestPool
            ((FederationManagerAgent)this.owner).getRequestPool().put(id, this.requestsManager.getRequest(id));//this.requestPool.put(id,request );
            logger.debug("Launching "+command+" method of "+agent+" agent to CM="+cm+").");
            logger.debug ("Message type: "+msg.getType().toString()+" type value: "+msg.getType().ordinal()+" id: "+msg.getId());
            ((FederationManagerAgent)this.owner).sendMessage(msg);
            logger.info("Launched "+command+" method of "+agent+" agent to CM="+cm+").");
            logger.debug("Waiting and retrieving result for "+command+" method of "+agent+" agent, lauched to CM="+cm+").");
            try {
                result = this.requestsManager.getRequest(id).getReturnValue();//request.getReturnValue();
                logger.debug(command+" method of "+agent+" agent successfully launched to CM="+cm+" failed.");
                return result;
            } catch (CleverException ex) {
                if (ex instanceof RequestExpired) {
                    logger.error ("The launch of "+command+" method of "+agent+" to CM="+cm+" failed due to reached timeout ");
                } else
                    throw ex;
            }
        throw new org.clever.Common.Exceptions.RequestAborted ("The launch of "+command+" method of "+agent+" to CM="+cm+" failed due to reached timeout. Request aborted");
    }
    /**
     * This function is used to send a command on a specified domain setted throw param domain.
     * @param domain
     * @param agent
     * @param command
     * @param hasReply
     * @param params
     * @param history
     * @param timeout
     * @return
     * @throws CleverException 
     */
    private Object forwardCommandToDomain(String domain, String agent, String command, boolean hasReply, List params, List<String> history, long timeout) throws CleverException {
        history.add(domain); //questa riga deve essere la prima perchè deve essere eseguita prima di incorrere in qualsiasi eccezione
        logger.info(domain+" domain added to the history of the method.");
        if (!this.existsDomain(domain))
            throw new CleverException (domain+" domain doesn't exist in federation");
        String cm = this.getCM(domain);
        logger.info("Preparing to launch "+command+" method of "+agent+" agent to "+domain+" domain (CM="+cm+").");
        return this.forwardCommandToCM(cm, agent, command, hasReply, params, timeout);
    }
    
    
    
    
    
    
    
    //</editor-fold>

    @Override
    public void shutdownPluginInstance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private String getCmFromDomain(String domain){
        if(this.domain_cm.containsKey(domain))
            return (String)this.domain_cm.get(domain);
        else
            return "";
    }
    
    /**
     * Function called by HOMECLOUD To make a request.
     * @param fro
     * @return 
     */
    public String sendFedBroadcastRequest(FederatorDataContainer fro) {
        String response="aborted";
        Object result=false;
        ArrayList<String> CMsList = this.getFederatedCMs();
        ArrayList<String> DomainsList = this.getFederatedDomains(CMsList);
        Iterator domainsListIt = DomainsList.iterator();
        boolean stop = false;
        while (domainsListIt.hasNext() && (!stop)) {
        //modificare questa parte posso usare la forwardcommandtodomain
            String cm = this.getCmFromDomain((String) domainsListIt.next());
            ArrayList params = new ArrayList();
            
            params.add(fro);
            try {
                result = this.forwardCommandToCM(cm, ((FederationManagerAgent) this.owner).getName(), "request_resource", true, params, defaultTimeout);

            } catch (Exception e) {
                logger.error("Exception occurred in sending message to all federated member! Can't send message to" + cm);
                response = "aborted_requestResource";
            }
            if ((Boolean) result) {
                String fedid=this.generateUUID(this.domain);
                params.clear();
                fro.setOperationId(fedid);
                params.add(fro);
                try {
                    result = this.forwardCommandToCM(cm, ((FederationManagerAgent) this.owner).getName(), "lock_resource", true, params, defaultTimeout);

                } catch (Exception e) {
                    logger.error("Exception occurred in sending lock message to" + cm);
                    response = "aborted_lockResource";
                }
                if ((Boolean) result) {
                    stop = true;
                    this.operation_destination.put(fedid,this.cm_domain.get(cm));
                    return fedid;
                }
            }
        }
        return  response;
    }
    /**
     * This function search for federated resource where doing VM migration
     * @param fdc
     * @return 
     */
    public Boolean request_resource(FederatorDataContainer fdc){
        boolean response=false;
        List par=new ArrayList();
        
        par.add(fdc);
        par.add("request_resource");
        
        try{
            response=(Boolean)this.owner.invoke("DispatcherAgent", "sendRequestonPE", true, par);
        }
        catch(Exception e){
            return false;
        }
        return true;
    }
    /**
     * This function Lock finded resource before VM migration are starting
     * @param fdc
     * @return 
     */
    public Boolean lock_resource(FederatorDataContainer fdc){
        boolean response=false;
        ArrayList resp;
        ArrayList par=new ArrayList();
        par.add(fdc);
        par.add("lock_resource");
        try{
            
            resp=(ArrayList)this.owner.invoke("DispatcherAgent", "sendRequestonPE", true, par);
            response=(Boolean)resp.get(0);
        }
        catch(Exception e){
            return false;
        }
        try{
            //il terzo parametro è stato modificato per far inserire in SEDNA il dominio che richiede l'operazione
            //this.operationEXT_operationINT.put(fdc.getOperationId(),this.generateUUID());
            par.clear();
            par.add((String)resp.get(1));
            par.add(fdc.getFederatedEntityTenant());
            if(!this.storeinfoOnSystemDb(fdc.getOperationId(),par,"FED_operation"))
            {
                logger.error("Local ID generated for operation is not found in hashTable! ");
            }
        }catch(Exception e){
            logger.error("Error in storing information related to federated operation"+fdc.getOperationId(),e);
        }
        return true;
    }
    /**
     * This fuction have to forward the command for managing Migrated VM.
     * 
     * @param method_info, this list have the element necessary, to indicate method type and other info: element at index 0 is Agent, element at index 1 is method name
     * @param method_params, this list contain params for the function called
     * @param VMName, the VM Name
     * @return 
     */
    public Boolean forwardCommand4VMM(String VMName,ArrayList method_info,ArrayList method_params)throws CleverException{
        try{
        ArrayList params=new ArrayList();
        params.add("FederationManagerAgent");
        params.add(this.nodoMigratedVm+"/VM[@name=\""+VMName+"\"]/domain/text()");
        String result=(String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        Object response=this.forwardCommandToDomain(result, (String)method_info.get(0) , (String)method_info.get(1), Boolean.TRUE, method_params);
        }
        catch(Exception e){
            logger.error("error in forwarding command to remote VM",e);
            return false;
        }
        return true;
    }
    /***
     * this is used to create a vm in this cloud for permit migration operation.
     * @param fdc
     * @return 
     */
    @Override
    public Boolean createVM4Migration(FederatorDataContainer fdc) throws CleverException
    {
        logger.debug("createVM4Migration1");
        HashMap hMresult;
        org.clever.Common.VEInfo.VEDescription ved=fdc.getVED();
        ved.setName(ved.getName()+"_migrated_"+fdc.getOperationId());
        String sharingType=fdc.getTypefromCDI();
        org.clever.Common.Communicator.Utils.IstantiationParams param=new org.clever.Common.Communicator.Utils.IstantiationParams(org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.EX);
        ArrayList params=new ArrayList();
        params.add("FederationManagerAgent");
        params.add("/"+this.nodefederatedOperation+"/operation[@id=\""+fdc.getOperationId()+"\"]/hostchoosedAsExecutor/text()");
        String targetHM;
        logger.debug("createVM4Migration2");
        try{
           targetHM=(String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
           if(targetHM.contains("@@@"))
               targetHM=targetHM.split("@@@")[0];
        }
        catch(Exception e){
            throw new CleverException("Impossible find information needed for VM creation");
        }
        logger.debug("createVM4Migration3");
        params.clear();
        params.add(fdc);
        params.add(ved);
        params.add(targetHM);
        params.add(param);
        params.add(sharingType);
        logger.debug("createVM4Migration4");
        hMresult=(HashMap)this.owner.invoke("VirtualizationManagerAgent","createVM4Migration",true,params);
        logger.debug("createVM4Migration5");
        try{
            params.clear();
            params.add((String)hMresult.get("name"));
            params.add(fdc.getFederatedEntityTenant());
            params.add(fdc.getResource().get("VMName"));
            this.storeinfoOnSystemDb(fdc.getOperationId(), params, "CREATE_operation");
        }
        catch(Exception e){
            logger.error("Exception occurred in insert information on DB for operation "+fdc.getOperationId(),e );
            
        } 
        logger.debug("createVM4Migration6");
        return new Boolean(true);
       
        
    }
    
    private boolean storeinfoOnSystemDb(String id,ArrayList param,String typeOfInsert)throws CleverException{
        ArrayList params=new ArrayList();
        String node,son;
        String param1="",param2="",param3="";
        try{
            param1=(String)param.get(1);
            param2=(String)param.get(2);
            param3=(String)param.get(3);
        }
        catch(Exception e){
            //nothing to do here
        }
        params.add("FederationManagerAgent");
        
        if(typeOfInsert.equals("FED_operation")){
            if(param.size()<2)
                logger.error("the number of parameter passed for FED_operation is less of needed, it will be inserted the default value[\"\"] for parameter missed");
            node="<operation id=\""+id+"\"><hostchoosedAsExecutor>"+param1
                    +"</hostchoosedAsExecutor><domainanswering>"+param2+"</domainanswering></operation>";
            son="/"+this.nodefederatedOperation;

        } else if (typeOfInsert.equals("CREATE_operation")) {
            if(param.size()<3)
                logger.error("the number of parameter passed for FED_operation is less of needed, it will be inserted the default value[\"\"] for parameter missed");
            
            node = "<VM name=\"" + param1 + "\"><domain>" + param2 + "</domain><VM_originalName>"+param3+"<operation_ID>"+id+"</operation_ID></VM>";
            son = "/" + this.nodoMigratedVm;
        } else {
            throw new CleverException("Unsupported call");
        }
        params.add(node);
        params.add("into");
        params.add(son);
        try{
            this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
        }catch(Exception e){
            logger.error("Problem occurred insertion on DB of operation information",e);
            throw new CleverException(e.getMessage());
        }
        return true;

    }
    /**
     * Function used to propagate the migration command to remote Federation
     * that will be host for VM migrated.
     *
     * @param fdc
     * @return
     * @throws CleverException 
     */
    public Boolean called4Migration(FederatorDataContainer fdc)throws CleverException{
        Boolean result=false;
        fdc.setFederatedEntityTenant(this.domain);
        //fdc.setOperationId(fdc.getOperationId()+this.domain);
        fdc.setNameofCont(fdc.getOperationId()+"SHAREDNODE");
        String domainTarget=(String)this.operation_destination.get(fdc.getOperationId());
        ArrayList params=new ArrayList();
        params.add(fdc);
        try{
            result=(Boolean)this.forwardCommandToDomain(domainTarget,"FederationManagerAgent", "createVM4Migration", Boolean.TRUE,params );
            logger.debug("Valore ricevuto:"+result);
        }
        catch(Exception e){
            throw new CleverException("Exception occurred in Starting Migration");
        }
        return result;
    }
    
    private void testmigrate(){
        FederatorDataContainer fdc=new FederatorDataContainer();
        fdc.setCdi("giuseppe", "87487illidan", "VFS", "172.17.3.180", 21, "/","ftp");
        fdc.setHostdesignated4VM_Migration("giusimon");
        fdc.setOperationId("1dominioB");
        String locationVED="/org.clever.Common.VEInfo.VEDescription[./name/text()='UbuVirt2']";
        ArrayList params=new ArrayList();
        
        
        params.add("VirtualizationManagerAgent");
        params.add(locationVED);
        String pathxml="";
        try{
            pathxml=(String) this.owner.invoke("DatabaseManagerAgent", "query", true, params); 
        }catch(Exception e){
            logger.error("error migration", e);
        
        }
        
        VEDescription veD =(VEDescription) MessageFormatter.objectFromMessage(pathxml);
        fdc.setVED(veD);
        fdc.setNameofCont("domainBSHAREDNODE");
        this.operation_destination.put("1dominioB", "dominioA");
        try{
            this.called4Migration(fdc);
           /* ArrayList params=new ArrayList();
            params.add("1cecf36486404a0099cda8daafe5a8f2");
            params.add("1dominioB");
            this.owner.invoke("VirtualizationManagerAgent","migration",true,params);*/
        }
        catch(Exception e){
            logger.error(e.getMessage(),e);
        }
    }
    //response
    private void testanswer(){
        try{
            //this.operationEXT_operationINT.put(1, 1999367167);//this.generateUUID());
            ArrayList param=new ArrayList();
            param.add("giusimon");
            param.add("dominioB");
            this.storeinfoOnSystemDb("1dominioB",param,"FED_operation");
        }catch(Exception e){
            logger.error("Error in storing information related to federated operation____",e);
        }
    }
    /**
     * Utility used to generate UUID for localOperationID and to verify if this UUID is unique.
     * @return 
     */
    private String generateUUID(String dom){
        boolean find=false;
        do{
            Integer al=org.clever.Common.UUIDProvider.UUIDProvider.getPositiveInteger();
            ArrayList param=new ArrayList();
            param.add("FederationManagerAgent");
            param.add("/"+this.nodefederatedOperation+"/operation[@id='"+al.toString()+dom+"']");
            try{ 
                if(((String) this.owner.invoke("DatabaseManagerAgent", "query", true, param)).equals("")){
                    find=true;
                    return al.toString()+dom;
                }
            }
            catch(Exception e){
                logger.error("Error occurred in generation UUID for Federation operation",e);
            }
        }while(find);
        return null;
    }
    
//TODO: ---->inserire funzione che richiamata dalla deletevm (se si tratta di macchina migrata) elimina l'id dell'operazione
    /**
     * function invoked in the home cloud to bring back the migrated virtual machine. 
     * @param VM, VM designed to retrieve
     * @param HMdest, Host Manager designed to host VM
     * @return 
     */
    public boolean bringBackVM(String VM,String HMdest)throws CleverException{
//recupero dominio        
        ArrayList param=new ArrayList();
        param.add("FederationManagerAgent");
        param.add("/"+this.nodoMigratedVm+"/VM/domain/text()");
        String domainTarget="";
        try{ 
        domainTarget=(String) this.owner.invoke("DatabaseManagerAgent", "query", true, param);
        if(domainTarget.contains("@@@"))
            domainTarget=domainTarget.split("@@@")[0];
        }
        catch(Exception e){
            logger.error("error in retrieve the domain where the VM was migrated",e);
        }
//preparazione richiesta remota per recupero VM
//invio richiesta remota 
        FederatorDataContainer result;
        ArrayList params=new ArrayList();
        params.add(VM);
        String fedid=this.generateUUID(this.domain);
        if(fedid==null)
            throw new CleverException("It's impossible continue the federation operation");
        params.add(fedid);
        this.operation_destination.put(fedid, domainTarget);
        
        try{
            result=(FederatorDataContainer)this.forwardCommandToDomain(domainTarget,"FederatorManagerAgent", "returnVM", Boolean.TRUE,params );
            logger.debug("Valore ricevuto:"+result);
            if(result==null)
                throw new CleverException("Exception occurred in bring back vm phase, started for VM "+VM);
            else{
                params.clear();
                params.add(result);
                params.add(VM);
                params.add(HMdest);
                boolean completed=(Boolean)this.owner.invoke("VirtualizationManagerAgent","reCreateMigratedVM", true, params);
                if(!completed)
                    return false;
                else{
                    params.clear();
                    params.add(VM);
                    result=(FederatorDataContainer)this.forwardCommandToDomain(domainTarget,"FederatorManagerAgent", "deleteMigratedVM", Boolean.TRUE,params );
                }
            }
                
        }
        catch(Exception e){
            throw new CleverException("Exception occurred in bring back vm phase, started for VM "+VM);
        }
        this.operation_destination.remove(fedid);
        return true;
    }
    
    /**
     * Function invoked from home cloud to start return virtual machine process.
     * @param VM
     * @param fedid
     * @return 
     */
    @Override
    public FederatorDataContainer returnVM(String VM,String fedid)throws CleverException{
//TODO: verificare che non sia necessario comunicare con il Policy Engine
        ArrayList ar=new ArrayList();
        ar.add("FederationManagerAgent");
        ar.add("/"+this.nodoMigratedVm+"/VM");
        ar.add("VM_originalName/text()=\""+VM+"\"");
        ar.add("name");
        String localVMName="";
        try{ 
        localVMName= ((List<String>)this.owner.invoke("DatabaseManagerAgent", "getAttributeWithInternalCond", true, ar)).get(0);
        }
        catch(Exception e)
        {
            throw new CleverException("error occurred in retrieving VM local name for the vm:"+VM);
        }
        ar.clear();
        ar.add(localVMName);
        ar.add(fedid);
        try{
            FederatorDataContainer result=(FederatorDataContainer)this.owner.invoke("VirtualizationManagerAgent","retFromMigration", true, ar);
            if(result==null)
                throw new CleverException("Exception occurred in returnVM vm phase");
            else
                return result;
        }
        catch(CleverException ce){
            //----> Aggiungere gestione eccezioni tipizzate
        }
        catch(Exception e){
            logger.error("A generic Exception occurred in returnVM function for VM:"+VM);
            throw new CleverException(e.getMessage());
        }
//richiamare una funzione del Virtualization manager agent che faccia l'equivalente della migrazione 
//ma per restituire la vm gestendo opportunamente le info registrate sul db
//al termine di qst funzione viene marcata la vm come restituita ma il disco non viene eliminato
//sarà eliminato con un'altra funzione che viene invocata dalla home cloud non appena viene terminata questa prima parte del processo 
//di restituzione.   
        return null;
    }
    
    @Override
    public void deleteMigratedVM(String VMName){
//retrieve vm name in this cloud        
        ArrayList ar=new ArrayList();
        ar.add("FederationManagerAgent");
        ar.add("/"+this.nodoMigratedVm+"/VM");
        ar.add("VM_originalName/text()=\""+VMName+"\"");
        ar.add("name");
        String localVMName="";
        try{ 
        localVMName= ((List<String>)this.owner.invoke("DatabaseManagerAgent", "getAttributeWithInternalCond", true, ar)).get(0);
        ar.clear();
//deleting VM        
        ar.add(localVMName);
        this.owner.invoke("VirtualizationManagerAgent","deleteVm", true, ar);
        ar.clear();
//deleting VFS NODE: if node is not presence (for example: it is used SWIFT node for sharing) this operation don't delete nothing 
////retrieve operation federation id
        ar.add("FederationManagerAgent");
        ar.add("/"+this.nodoMigratedVm+"/VM[@name=\""+localVMName+"\"]/operation_ID/text()");
        String fedid_VFS=(String) this.owner.invoke("DatabaseManagerAgent", "query", true, ar);
        if(fedid_VFS.contains("@@@"))
            fedid_VFS=fedid_VFS.split("@@@")[0];
        ar.clear();
        ar.add("StorageManagerAgent");
        ar.add("/node[@name=\""+fedid_VFS+"\"]");
        this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, ar);
//deleting dB node for nodoMigratedVm
        ar.clear();
        ar.add("FederationManagerAgent");
        ar.add("/"+this.nodoMigratedVm+"/VM[@name=\""+localVMName+"\"]");
        this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, ar);
        ar.clear();
//deleting db node for nodefederatedOperation        
        ar.add("FederationManagerAgent");
        ar.add("/"+this.nodefederatedOperation+"/operation[@id=\""+fedid_VFS+"\"]");
        this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, ar);
        }
        catch(Exception e)
        {
            logger.error("error occurred in retieve VM NAME in deleteMigratedVM",e);
        }
        
    }
    
}

