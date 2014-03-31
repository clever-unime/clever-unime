/*
 * The MIT License
 *
 * Copyright 2011 giovalenti.
 * Copyright 2012 giancarloalteri
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
package org.clever.ClusterManager.VirtualizationManagerPlugin;

import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.VirtualizationManager.VirtualizationManagerPlugin;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.LogicalCatalogException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.VEInfo.DesktopVirtualization;
import org.clever.Common.VEInfo.StorageSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile;
import org.clever.HostManager.ServiceManager.ServiceObject;
import org.jdom.Element;


public class VirtualizationManagerClever implements VirtualizationManagerPlugin {
    private ModuleCommunicator mc;
    private Agent owner;
    private String version = "0.0.1";
    private String description = "Plugin per HTML5 remote desktop";
    private String name = "Virtualization Desktop Plugin";
    private String HostManagerServiceGuacaTarget;
    private String agent;
    private String OS_service;

    private String RegisterVirtualDeskHTML5 = "Virtualization/RegisterVirtualDesktopHTML5";
    private String UnRegisterVirtualDeskHTML5 = "Virtualization/UnRegisterVirtualDesktopHTML5";
    private ParserXML pXML;
    
    // variabile d'appoggio per lo startup di un VM scritta dallo startvm e letta dal RegisterVirtualizationDesktopHTML5
    private String vm_tmp="";

    private String nodoVEDescriptorVm="Matching_VED_VM";
    private String nodoMatchingVmHM="Matching_VM_HM";
    private String nodoVmRunning="VMs_Running";
    
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger=null;
    private String pathLogConf="/sources/org/clever/ClusterManager/VirtualizationManager/log_conf/";
    private String pathDirOut="/LOGS/ClusterManager/VirtualizationManager";
    //########


    public VirtualizationManagerClever() throws Exception{
        
       //#############################################
       //Inizializzazione meccanismo di logging
       logger=Logger.getLogger("VirtualizationManager");    
       Log4J log =new Log4J();
       log.setLog4J(logger, pathLogConf, pathDirOut);
    //#############################################
                
        this.logger.info("VirtualizationManager plugin created: ");
    }


    @Override
    public void init(Element params, Agent owner) throws CleverException {
        if(params!=null){
            //Read param from configuration_networkManager.xml
            this.HostManagerServiceGuacaTarget = params.getChildText("HostManagerServiceGuacaTarget");
        }
        //this.owner = owner;
        //If the data struct, for matching between VM and HM, isen't into DB then init it.
       if(!this.checkMatchingVmHMNode())
            this.initVmHMNodeDB();
         if(!this.checkVmRunningNode())
            this.initVmRunningDB();
       if(!this.checkVEDVMNode())
            this.initVEDVMDB(); 

    }
    private boolean checkVEDVMNode() throws CleverException{
       List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/"+this.nodoVEDescriptorVm); //XPath location with eventual predicate
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params); 
    }
    
    private boolean checkVmRunningNode() throws CleverException{
       List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/"+this.nodoVmRunning); //XPath location with eventual predicate
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params); 
    }

    private boolean checkMatchingVmHMNode() throws CleverException{
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/"+this.nodoMatchingVmHM); //XPath location with eventual predicate
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params);
    }
    
    private void initVEDVMDB() throws CleverException{
        String node="<"+this.nodoVEDescriptorVm+"/>";
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(node);
        params.add("into");
        params.add(""); //XPath location with eventual predicate
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
    
     private void initVmRunningDB() throws CleverException{
        String node="<"+this.nodoVmRunning+"/>";
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(node);
        params.add("into");
        params.add(""); //XPath location with eventual predicate
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
         
     }
    private void initVmHMNodeDB() throws CleverException{
        String node="<"+this.nodoMatchingVmHM+"/>";
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(node);
        params.add("into");
        params.add(""); //XPath location with eventual predicate
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }

    private void dispatchToExtern(String host_manager, String agent, String OS_service, ServiceObject serviceobject) throws Exception{
          List params = new ArrayList();
          params.add(OS_service);
          params.add(serviceobject);

          if(!(Boolean)((CmAgent)this.owner).remoteInvocation(host_manager, agent, "ServiceUpdate", true, params))
              throw new CleverException("Host Manager sul proxy HTML5/VNC non raggiungibile");
    }

    @Override
    public void RegisterVirtualizationDesktopHTML5(DesktopVirtualization desktop) throws Exception {
        this.agent="ServiceManagerAgent";
        this.OS_service="guacamole";
        if(desktop.getUsername().isEmpty()){      // se non definito imposto un username=VM
            desktop.setUsername(this.vm_tmp);
        }
        desktop.setUserPassword(MD5.getMD5("vmware"));//la pass dovrebbe essere settata mediante qualche politica (DA VEDERE)
        ServiceObject serviceobject = new ServiceObject(desktop,this.RegisterVirtualDeskHTML5);
        this.dispatchToExtern(this.HostManagerServiceGuacaTarget, this.agent, this.OS_service, serviceobject); //send object to OS_service Manager of Tiny Clever
        this.updateDesktop(desktop,this.vm_tmp);
    }

    @Override
    /**
     * @param id virtual machine name identification: vmname|host
     */
    public void UnRegisterVirtualizationDesktopHTML5(String id) throws Exception {
        String vmname = id;
        this.agent="ServiceManagerAgent";
        this.OS_service="guacamole";
        ServiceObject serviceobject = new ServiceObject(vmname,this.UnRegisterVirtualDeskHTML5);
        this.dispatchToExtern(this.HostManagerServiceGuacaTarget, this.agent, this.OS_service, serviceobject); //send object to OS_service Manager of Tiny Clever
        this.updateDesktop(vmname);
    }
    
    /**
     * Update Sedna DB for insert new item
     * @param desktop Contains all informations for a new desktop virtualized. name VM, host, port, vnc password and virtualization password
     * @return
     * @throws CleverException
     * @throws Exception 
     */
    private void updateDesktop(DesktopVirtualization desktop, String id) throws CleverException {
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("<desktop>"
                + "<username>" + desktop.getUsername() + "</username>"
                + "<password_user>" + desktop.getUserPassword() + "</password_user>"
                + "<password_vnc_vm>" + desktop.getVmVNCPassword() + "</password_vnc_vm>"
                + "<ip_vnc>" + desktop.getIpVNC() + "</ip_vnc>"
                + "<port>" + desktop.getPort() + "</port>"
                + "</desktop>");
        params.add("with");
        params.add("/org.clever.Common.VEInfo.VEDescription[./name/text()=\"" + id + "\"]/desktop");
        this.owner.invoke("DatabaseManagerAgent", "updateNode", true, params);

        /* 
         String node="<VMHTML5 name=\""+desktop.getUsername() +"\" host=\""+desktop.getIpVNC()+"\"><port>"+desktop.getPort()+"</port><pass_vnc>"+desktop.getVmVNCPassword()+"</pass_vnc><pass>"+desktop.getUserPassword()+"</pass></VMHTML5>";
         List params = new ArrayList();
         params.add("VirtualizationManagerAgent");
         params.add(node);
         params.add("into");
         params.add("/org.clever.Common.VEDescription[./name/text()='"+desktop.getUsername()+"']"); //XPath location with eventual predicate. Username coincide con il nome della vm
         this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
         
         */
    }
    
    /**
     * Update Sedna DB for delete an item
     * @param name name of virtual machine
     * @param host name or ip host
     * @return
     * @throws CleverException
     * @throws Exception 
     */
    private void updateDesktop(String name) throws CleverException{ 
        
        List params = new ArrayList();
        
        
           params.add("VirtualizationManagerAgent");
           params.add("<port>-1</port>");
           params.add("with");
           params.add("/org.clever.Common.VEInfo.VEDescription[./name/text()=\""+name+"\"]/desktop/port");
        
           this.owner.invoke("DatabaseManagerAgent", "updateNode", true, params);
        
        
        /*
        String location="/org.clever.Common.VEDescription[./name/text()=\""+name+"\"]/VMHTML5[(@name=\""+name+"\" and @host=\""+host+"\")]";
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(location); //XPath location with eventual predicate     
        this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
         */
    }


   
    
 /**
  * This method registers a virtual environment and write everything into database Sedna
  * @param info
  * @param veD
  * @throws CleverException
  */
 @Override
 public void register(String info, VEDescription veD) throws CleverException{
         
        List params = new ArrayList();
        params.add(((StorageSettings)veD.getStorage().get(0)).getDiskPath());
        this.owner.invoke("StorageManagerAgent", "registerVeNew", true, params);   
        params.clear(); 
        
        // the name of the virtual machine is required
        if(veD.getName().isEmpty()){
                            throw new LogicalCatalogException("VM name is required");
        }
        // check if there is a virtual machine with the same name
        params.add("VirtualizationManagerAgent");
        params.add(("/org.clever.Common.VEInfo.VEDescription[./name/text()='"+veD.getName()+"']/name/text()"));
        boolean r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);     
        if(r==true){  
                throw new LogicalCatalogException("VM name already exist");
                }
        params.clear();

        String prova=MessageFormatter.messageFromObject(veD);
        params.add("VirtualizationManagerAgent");
        params.add(prova);
        params.add("into");
        params.add("");           
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }

/**
  * Creation and/or registration of a virtual machine
  * @param id
  * @param targetHM
  * @param param , is an object used to define the parameter necessary for creation of VM
  * @return
  * @throws CleverException
  */
    @Override
    public String createVM(String id,String targetHM,org.clever.Common.Communicator.Utils.IstantiationParams param) throws CleverException{
        boolean result=false; 
        String vmname=java.util.UUID.randomUUID().toString();
         vmname=vmname.replace("-", "");//this replacement is necessary because if we don't eliminate the "-" the VirtuaslBox "findmachine" function don't work finely 
         if((param.getLock().equals(LockFile.lockMode.PR))&&((CmAgent)this.owner).remoteInvocation(targetHM,"HyperVisorAgent", "getHYPVRName", true, new ArrayList()).equals("LibVirt")){
             this.TakeEasySnapshot(id, vmname, "Snapshot created by CLEVER for a clonable image.", targetHM);
             result=true;
         }
         else{
         
         MethodInvoker mi=null;
         // check if into db Sedna exist name of the VM
         List params = new ArrayList();
         params.add("VirtualizationManagerAgent");
         params.add(("/org.clever.Common.VEInfo.VEDescription[./name/text()='"+id+"']/name/text()"));
         boolean r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);     
            if(r==false){  
                throw new LogicalCatalogException("Template name not valid");
            }
         params.clear();
         params.add("VirtualizationManagerAgent");
         String location="/Matching_VM_HM/VM[@name='"+vmname+"']";
         params.add(location);
         r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);
         //Insert into DB: mappigetAttributeNodeng VM - HM
         if(r==true){  
                throw new LogicalCatalogException("VM already exist");
         }
         params.clear();
         //Insert into DB: mapping VM - HM
         this.InsertItemIntoMatchingVmHM(vmname, targetHM);
         
         List params1 = new ArrayList();
         params1.add("VirtualizationManagerAgent");
         location="/org.clever.Common.VEInfo.VEDescription[./name/text()='"+id+"']";
         params1.add(location);
         String pathxml=(String) this.owner.invoke("DatabaseManagerAgent", "query", true, params1); 
         VEDescription veD =(VEDescription) MessageFormatter.objectFromMessage(pathxml);
         String diskPath=((StorageSettings)veD.getStorage().get(0)).getDiskPath();
         params.add(diskPath);
         params.add(targetHM);
         params.add(param.getLock());
         // physical path
         String res=(String) this.owner.invoke("StorageManagerAgent", "lockManager", true, params); 
         
         List params2 = new ArrayList();
         params2.add(diskPath);
         Boolean check=(Boolean) this.owner.invoke("StorageManagerAgent", "check", true, params2);
        
         ((StorageSettings)veD.getStorage().get(0)).setDiskPath(res);
         
         params2 = new ArrayList();
         params2.add(vmname);
         params2.add( veD );
         if(param.getLock().ordinal()<=4)
            params2.add(true);
         else
            params2.add(false);
         
         if(!check){
             result=(Boolean)((CmAgent)this.owner).remoteInvocation(targetHM,"HyperVisorAgent", "registerVm", true, params2);
         }
         else{
              result=(Boolean)((CmAgent)this.owner).remoteInvocation(targetHM,"HyperVisorAgent", "createVm", true, params2);
         }
         this.InsertItemIntoMatchingVEDVm(id,vmname,param.getLock(),res,diskPath);
         }
         /*
         String a="";
           a = (String) ((CmAgent) this.owner).remoteInvocation(targetHM,"TestAgentHm","fittizioHM", true, params2);
          */
         if(!result){
             return "creating VM "+vmname+" failed!";
         }
         return "VM " +vmname+ " created!";
     }

    /**
     * Starting VM
     * @param id
     * @return
     * @throws CleverException
     */
    @Override
    public boolean startVm(String id) throws CleverException{


        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        String location="/Matching_VM_HM/VM[@name='"+id+"']/host/text()";
        params.add(location);
       
        String HMTarget=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        if(HMTarget.isEmpty())
            throw new LogicalCatalogException("VM name not exist");
        
        // insert intoDB
        String node="<VM name=\""+id+"\" request=\""+new Date().toString()+"\" started=\"\">"+HMTarget+"</VM>";
        params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(node);
        params.add("into");
        params.add("/"+this.nodoVmRunning);
       
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
        
        params = new ArrayList();
        params.add(id);
        this.vm_tmp=id;
        boolean result = (Boolean) ((CmAgent) this.owner).remoteInvocation(HMTarget,"HyperVisorAgent","startVm", true, params);
        return result;
    }

    /**
     * Stopping VM
     * @param id
     * @return
     * @throws CleverException
     */
    @Override
    public boolean stopVm(String id) throws CleverException{
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        String location="/Matching_VM_HM/VM[@name='"+id+"']/host/text()";
        params.add(location);

        String HMTarget=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);

        if(HMTarget.isEmpty())
            throw new LogicalCatalogException("VM name not exist");
        params = new ArrayList();
        params.add(id);
        boolean result = (Boolean) ((CmAgent) this.owner).remoteInvocation(HMTarget,"HyperVisorAgent","shutDownVm", true, params);
        
        if(result){
            params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/VMs_Running/VM[@name='"+id+"']");
  
        this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
        }

        return result;
    }  


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setModuleCommunicator(ModuleCommunicator mc) {
        this.mc = mc;
    }

    @Override
    public ModuleCommunicator getModuleCommunicator() {
        return this.mc;
    }

    @Override
    public void setOwner(Agent owner) {
        this.owner=owner;
    }
    
 

 /**   salvo
     * Stopping VM
     * @param id
     * @return
     * @throws CleverException
     */
    @Override
    public boolean unregisterVm(String id) throws CleverException{
        String response=null;
        
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(("/org.clever.Common.VEInfo.VEDescription[./name/text()='"+id+"']"));
        Boolean x=(Boolean)this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);
        if(!x){
            response="Template is not registered";
            throw new CleverException(response);
            
        }
      
        params.clear();
        params.add("VirtualizationManagerAgent");
        params.add("/Matching_VM_HM/VM[@parent='"+id+"']/@name");
        String r=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        if(r.isEmpty()==false){
            response="Have to delete first these snapshot:"+r;
            throw new CleverException(response);
        }
        params.clear();
        params.add("VirtualizationManagerAgent");
        params.add("/Matching_VED_VM/VED[@name='"+id+"']");
        r=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        if(r.isEmpty()==false){
            response="Have to do first deletevm -n "+id;
            throw new CleverException(response);
        }
      
        params.clear();
        params.add("VirtualizationManagerAgent");
        params.add(("/org.clever.Common.VEInfo.VEDescription[./name/text()='"+id+"']"));
        this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
        params.clear();
     
        return true;
        
        
        
        
        
    }

    @Override
    public boolean resumeVm(String id) throws CleverException {
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        String location="/Matching_VM_HM/VM[@name='"+id+"']/host/text()";
        params.add(location);
        String HMTarget=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        if(HMTarget.isEmpty())
            throw new LogicalCatalogException("VM name not exist");
        params.clear();
        params.add("VirtualizationManagerAgent");
        params.add("/VMs_Running/VM[@name='"+id+"']");
        Boolean r=(Boolean)this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);
        if(r){
            throw new CleverException("Vm is already running");
        }
        
        String node="<VM name=\""+id+"\" request=\""+new Date().toString()+"\" started=\"\">"+HMTarget+"</VM>";
        params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(node);
        params.add("into");
        params.add("/"+this.nodoVmRunning);
 
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
        
        params = new ArrayList();
        params.add(id);
        this.vm_tmp=id;
        boolean result=(Boolean)((CmAgent) this.owner).remoteInvocation(HMTarget,"HyperVisorAgent","resume", true, params);
      
        return result;
    }

    @Override
    public boolean stopVm(String id, Boolean poweroff) throws CleverException {
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        String location="/Matching_VM_HM/VM[@name='"+id+"']/host/text()";
        params.add(location);

        String HMTarget=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);

        if(HMTarget.isEmpty())
            throw new LogicalCatalogException("VM name not exist");
        params = new ArrayList();
        params.add(id);
        params.add(true);
        boolean result = (Boolean) ((CmAgent) this.owner).remoteInvocation(HMTarget,"HyperVisorAgent","shutDownVm", true, params);
        if(result){
            params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/VMs_Running/VM[@name='"+id+"']");
  
        this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
        }

        return result;
    }

    @Override
    public boolean suspendVm(String id) throws CleverException {
          List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        String location="/Matching_VM_HM/VM[@name='"+id+"']/host/text()";
        params.add(location);

        String HMTarget=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);

        if(HMTarget.isEmpty())
            throw new LogicalCatalogException("VM name not exist");
        params = new ArrayList();
        params.add(id);
        boolean result = (Boolean) ((CmAgent) this.owner).remoteInvocation(HMTarget,"HyperVisorAgent","suspend", true, params);
        
        if(result){
            params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/VMs_Running/VM[@name='"+id+"']");
  
        this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
        }

        return result;
    }
    @Override
public boolean TakeEasySnapshot(String id,String nameS,String description,String targetHM) throws CleverException{
         boolean result=false;
         MethodInvoker mi=null;
         // check if into db Sedna exist name of the template
         List params = new ArrayList();
         params.add("VirtualizationManagerAgent");
         params.add(("/org.clever.Common.VEInfo.VEDescription[./name/text()='"+id+"']/name/text()"));
         boolean r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);     
            if(r==false){  
                throw new LogicalCatalogException("Template name not valid");
                }
         params.clear();
         params.add("VirtualizationManagerAgent");
         String location="/Matching_VM_HM/VM[@name='"+nameS+"']";
         params.add(location);
         r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);
         //Insert into DB: mappigetAttributeNodeng VM - HM
         if(r==true){  
                throw new LogicalCatalogException("SN already exist");
                }
         this.InsertItemIntoMatchingSnHM(id,nameS,targetHM);
         LockFile.lockMode lock=LockFile.lockMode.CR;
         List params1 = new ArrayList();
         params1.add("VirtualizationManagerAgent");
         location="/org.clever.Common.VEInfo.VEDescription[./name/text()='"+id+"']";
         params1.add(location);
         String pathxml=(String) this.owner.invoke("DatabaseManagerAgent", "query", true, params1); 
         VEDescription veD =(VEDescription) MessageFormatter.objectFromMessage(pathxml);
         params.clear();
         params.add(((StorageSettings)veD.getStorage().get(0)).getDiskPath());
         params.add(targetHM);
         params.add(lock);
         // physical path
         String localpath=(String)this.owner.invoke("StorageManagerAgent","lockManager", true, params);
         params.clear();
         params.add(localpath);
         params.add(((StorageSettings)veD.getStorage().get(0)).getDiskPath());
         params.add(targetHM);
         params.add(lock);
         String snapshotLocalPath=(String)this.owner.invoke("StorageManagerAgent","SnapshotImageCreate", true, params);
         this.InsertItemIntoMatchingVEDVm(id,nameS,lock,snapshotLocalPath,localpath);
        
        
         
         ((StorageSettings)veD.getStorage().get(0)).setDiskPath(snapshotLocalPath);
         veD.setName(nameS);
         params.clear();
         params.add(nameS);
         params.add( veD );
         params.add(true);
         
        result=(Boolean)((CmAgent)this.owner).remoteInvocation(targetHM,"HyperVisorAgent", "createVm", true, params);
            
       
         if(!result){
             throw new CleverException("creating SN "+nameS+" failed!");
         }
         return true;
     }
    @Override
 public boolean deleteVm(String id) throws CleverException{
        List params = new ArrayList();
        String nomeVED="";
        params.add("VirtualizationManagerAgent");
        params.add("/Matching_VM_HM/VM[@name='"+id+"']/host/text()");
        String HMTarget=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        
        params.clear(); 
        if(HMTarget.isEmpty())
            throw new LogicalCatalogException("VM name not exist");
         
        params.add("VirtualizationManagerAgent");
        params.add("/Matching_VED_VM/VED[@VM_name='"+id+"']/locklevel/text()");
        String lock=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        params.clear();
        /*
        params.add("VirtualizationManagerAgent");
        params.add("/Matching_VM_HM/VM[@name='"+id+"'][@snapshot='"+true+"']");
        Boolean r=(Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);
        params.clear();
         
         if(r){
            params.add("VirtualizationManagerAgent");
            params.add("/Matching_VM_HM/VM[@name='"+id+"']");
            params.add("parent");
            String parent=(String)this.owner.invoke("DatabaseManagerAgent", "getAttributeNode", true, params);
            
            params.clear();
           
            params.add("VirtualizationManagerAgent");
            String location="/org.clever.Common.VEInfo.VEDescription[./name/text()='"+parent+"']";
            params.add(location);
         }*/
         params.add("VirtualizationManagerAgent");
         String location="/Matching_VED_VM/VED[@VM_name='"+id+"']";
         params.add(location);
         String tipo="name";
         params.add(tipo);
         nomeVED=(String) this.owner.invoke("DatabaseManagerAgent", "getAttributeNode", true, params); 
         params.clear();
       
         params.add("VirtualizationManagerAgent");
         params.add("/Matching_VED_VM/VED[@VM_name='"+id+"']/ParentHDD/text()");
         String pathHDD=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
         //this.logger.debug("path:"+pathHDD);
         params.clear();
         params.add("VirtualizationManagerAgent");
         params.add("/Matching_VED_VM/VED[@VM_name='"+id+"']/HDD/text()");
         String HDD=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
         //this.logger.debug("path:"+pathHDD);
         params.clear();
         //inserire un controllo del tipo di lock realizzato sul disco della macchina
         //se  lock di tipo ex richiamre la funzione attuale altrimenti richiamare la funzione corretta
         //questa funzione deve essere modificata per eliminare il file corretto correlato alla vm
         //attualmente cancella la golden image
         if(lock.equals("Null Lock")){
             //si deve decidere cosa fare in questo caso, 
             //temporarily Nothing to do, the VM's Disk must not be deleted
         }
         else if((lock.equals("Concurrent Read"))||(lock.equals("Protected Read"))){
             params.add("VirtualizationManagerAgent");
             params.add("/Matching_VED_VM/VED/HDD/text()");
             String pathsHDDs=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
             boolean testValue=testUsageHDD(pathsHDDs,HDD);
             params.clear();
             if(testValue)
             {
                params.add(HDD);
                params.add(id);
                params.add(HMTarget);
                this.owner.invoke("StorageManagerAgent","deleteFile", true, params);
                params.clear();
             }
         }
         else if(lock.equals("Concurrent Write")){
             //Nothing to do, the VM's Disk must not be deleted
         }
         else if(lock.equals("Protected Write")){
             //qui è necessario verificare se ci sono altre vm che usano quel disco se non ce ne sono allora si può eliminare,
             //anche se momentaneamente questa modalità non viene usata
         }
         else if(lock.equals("Exclusive")){
             
             params.add("VirtualizationManagerAgent");
             /*//essenzialmente questa parte andrà rivista per bene non appena deciderò come trattare gli altri lock per adesso copmmento questa parte
             String location="/org.clever.Common.VEInfo.VEDescription[./name/text()='"+nomeVED+"']";
             params.add(location);        
             String pathxml=(String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
             VEDescription veD =(VEDescription) MessageFormatter.objectFromMessage(pathxml);
             params.clear();
             params.add(((StorageSettings)veD.getStorage().get(0)).getDiskPath());*/
             
             params.clear();
             params.add(HDD);
             params.add(id);
             params.add(HMTarget);
             this.owner.invoke("StorageManagerAgent","deleteFile", true, params);
             params.clear();
         }
         params.add("VirtualizationManagerAgent");
         params.add(("/Matching_VED_VM/VED[@VM_name='"+id+"']"));
         this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
         params.clear();
         params.add("VirtualizationManagerAgent");
         params.add(("/Matching_VM_HM/VM[@name='"+id+"']"));
         this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
         params.clear();
         params.add(id);
         ((CmAgent)this.owner).remoteInvocation(HMTarget,"HyperVisorAgent", "unregisterVm", true, params);
         return true;
   }
 private boolean testUsageHDD(String pathHDD,String element2verfiy){
     int counter=0;
     String[] alp=pathHDD.split(".vdi");
             for(int i =0;i<alp.length;i++){
                 //String[] alp2=alp[i].split("/");
                 if(element2verfiy.equals(alp[i]+".vdi")){
                     counter++;
                     logger.debug("$$confronto: "+element2verfiy+" "+alp[i]+".vdi"+" "+counter);
                     if(counter>1)
                         return false;
                 }
             }
     return true;
 }
 
public boolean attackInterface(String id,String inf,String mac,String type) throws CleverException{
  List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        String location="/Matching_VM_HM/VM[@name='"+id+"']/host/text()";
        params.add(location);
        String HMTarget=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        if(HMTarget.isEmpty())
            throw new LogicalCatalogException("VM name not exist");
        params.clear();
        params.add(id);
        params.add(inf);
        params.add(mac);
        params.add(type);
        ((CmAgent)this.owner).remoteInvocation(HMTarget,"HyperVisorAgent","attackInterface", true, params); 
        boolean result=insertNetInterfaceIntoDb(id,inf,mac,type);
        return result;
 }
      
 
 public boolean insertNetInterfaceIntoDb(String id,String inf,String mac,String type) throws CleverException{
           String iface="<interface>"
                             + "<name>"+inf+"</name>"
                             + "<type>"+type+"</type>"
                             + "<mac_address>"+mac+"</mac_address>"
                       + "</interface>";
           List params=new ArrayList();
           params.add("VirtualizationManagerAgent");
           params.add(iface);
           params.add("into");
           params.add("/Matching_VM_HM/VM[@name='"+id+"']");
           this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
        return  true;
           
       }
public String listMac_address(String id) throws CleverException{
        List params=new ArrayList();
        
        params.add("VirtualizationManagerAgent");
        params.add("/Matching_VM_HM/VM[@name='"+id+"']/interface/mac_address/text()");
        String result=(String)this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
        return result;
        
    } 

    /**
     * List VM.This function take the list of Virtual Machine.
     * 
     * @param targetHM - is a string object that represents Host Manager target. 
     * @param running - is a boolean object that indicates to list only running VirtualMachine
     * @param hypvr - is a boolean object that indicates to list Virtual Machine registered on hypervisor
     * @return
     * @throws CleverException
     */
    public List listVm(String targetHM,Boolean running,Boolean hypvr) throws CleverException{
        List result;
        String location="";
        String condition="";
        List params = new ArrayList();
        if(!hypvr)
        {
            params.add("VirtualizationManagerAgent");
            if(!running){
                location="/Matching_VM_HM/VM";
                condition="host/text()=\""+targetHM+"\"";
            }
            else{
                location="/VMs_Running/VM";
                condition="text()=\""+targetHM+"\"";
            }
            params.add(location);
            params.add(condition);
            result=(List<String>)this.owner.invoke("DatabaseManagerAgent", "getNameAttributes", true, params);
        }
        else{
            if(running){
                location="listRunningHVms";
            }
            else{
                location="listHVms";
            }
            try{
                result = ( List<String> ) ((CmAgent) this.owner).remoteInvocation(targetHM,"HyperVisorAgent",location, true, new ArrayList());
            }
            catch(Exception e){
                logger.error("An error has occurred in invocation of the Hypervisor Plugin Method:"+e.getMessage());
                return null;
            }
        }
        return result;
       }
    
    /*HANDLE RETURN ONLINE HOST*/
    public void manageReUpHost(String HmName){
        //TODO: This function need some fixes. It doesn't work properly.
        try{
            List vmsHost=(List)this.listVm(HmName, Boolean.FALSE, Boolean.TRUE);
            List vmsClever=(List)this.listVm(HmName, Boolean.FALSE, Boolean.FALSE);
            Iterator IvmsHost=vmsHost.iterator();
            Iterator IvmsClever=vmsClever.iterator();
            while(IvmsClever.hasNext()){
                 String vmName=(String)IvmsClever.next();
                 if(vmsHost.lastIndexOf(vmName)==-1){
                    //is it necessary create a log for this operation ?
                    this.deleteDBNode(vmName,false);
                 }
            }
        }catch(Exception e){
           // logger.error("An exception has verified in function <manageReUpHost> step 1:"+e.getMessage());
        }
        try{
            //Step 2:Verify VMs running
            List vmsClever=(List<String>)this.listVm(HmName, Boolean.TRUE, Boolean.FALSE);
            List vmsHost=(List<String>)this.listVm(HmName, Boolean.TRUE, Boolean.TRUE);
            //if(vmsHost.isEmpty())
            //    logger.debug("X?X an empty list are returned");
            Iterator IvmsHost=vmsHost.iterator();
            Iterator IvmsClever=vmsClever.iterator();
            while(IvmsClever.hasNext()){
               String vmName=(String)IvmsClever.next();
                 if(vmsHost.lastIndexOf(vmName)==-1){
                    //is it necessary create a log for this operation ?
                    this.deleteDBNode(vmName,true);
                 }
            }
        }
        catch(Exception e){
            //logger.error("An exception has verified in function <manageReUpHost> :"+e.getMessage());
        }
    }
/*FUNCTION FOR INTERATION WITH DB*/
    private void deleteDBNode(String id, boolean run){
       try
       {
            List params=new ArrayList();
            if(run){
                
                params.add("VirtualizationManagerAgent");
                params.add(("/VMs_Running/VM[@name='"+id+"']"));
                this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
                this.logger.debug("Sedna entry relative at VM running "+id+" are deleted!");
            }
            else{
                params.clear();
                params.add("VirtualizationManagerAgent");
                params.add(("/Matching_VM_HM/VM[@name='"+id+"']"));
                this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
                params.clear();
                params.add("VirtualizationManagerAgent");
                params.add(("/Matching_VED_VM/VED[@VM_name='"+id+"']"));
                this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
                this.logger.debug("Sedna entry relative at VM "+id+" are deleted!");
            }
            
       }
       catch(Exception e){
           this.logger.error("Error in execution of function 'deleteDBNode':"+e.getMessage());
       }
    }
     /**
     * Insert new item for matching between Virtual Machine and Host Manager
     * @param id
     * @param vmname 
     * @param lock
     * @param HostManagerTarget
     * @throws CleverException
     */
    public void InsertItemIntoMatchingVEDVm(String id,String vmname,LockFile.lockMode lock,String HDDpath,String ParentHDD) throws CleverException {
        logger.debug("sto inserendo il VED element");
        String node="<VED name=\""+id+"\" VM_name=\""+vmname+"\"> <locklevel>"+new LockFile().getLockType(lock)
                    +"</locklevel><HDD>"+HDDpath+"</HDD> <ParentHDD>"+ParentHDD+"</ParentHDD></VED>";
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(node);
        params.add("into");
        params.add("/"+this.nodoVEDescriptorVm);
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
    /**
     * Insert new item for matching between Virtual Machine and Host Manager
     * @param id
     * @param HostManagerTarget
     * @throws CleverException
     */
    public void InsertItemIntoMatchingVmHM(String id, String HostManagerTarget) throws CleverException {
         String node="<VM name=\""+id+"\" request=\""+new Date().toString()+"\""+">"
                       +"<host>"+HostManagerTarget+"</host>"
                    +"</VM>";
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(node);
        params.add("into");
        params.add("/"+this.nodoMatchingVmHM);
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
    /**
     * Insert new item for matching between Virtual Machine Snapshot and Host Manager
     * @param id
     * @param nameS
     * @param HostManagerTarget
     * @throws CleverException
     */
    public void InsertItemIntoMatchingSnHM(String id,String nameS,String HostManagerTarget) throws CleverException{
   String node="<VM name=\""+nameS+"\" request=\""+new Date().toString()+"\" snapshot=\""+true+"\" parent=\""+id+"\">"
                     +"<host>"+HostManagerTarget+"</host>"
              +"</VM>";
    List params = new ArrayList();
    params.add("VirtualizationManagerAgent");
    params.add(node);
    params.add("into");
    params.add("/"+this.nodoMatchingVmHM);
    this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
    
}

 
  

