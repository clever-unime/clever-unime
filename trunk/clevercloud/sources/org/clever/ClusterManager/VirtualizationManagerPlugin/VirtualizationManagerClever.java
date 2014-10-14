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
 * Copyright 2011 giovalenti.
 * Copyright 2012 giancarloalteri
 * Copyright 2013-14 Giuseppe Tricomi
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

import java.io.File;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.clever.Common.Storage.VFSDescription;
import org.clever.Common.VEInfo.DesktopVirtualization;
import org.clever.Common.VEInfo.StorageSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile;
import org.clever.HostManager.ObjectStorage.ObjectStoragePlugin;
import org.clever.HostManager.ServiceManager.ServiceObject;
import org.jdom2.Element;
import org.clever.Common.Communicator.Utils.IstantiationParams;

public class VirtualizationManagerClever implements VirtualizationManagerPlugin {
    private Agent owner;
    private String version = "0.0.1";
    private String description = "Plugin per HTML5 remote desktop";
    private String name = "Virtualization Desktop Plugin";
    private Logger logger = null;
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
    private String shareTecnologyAdopted="VFS";
    private String nodeVFSShared="";
    public VirtualizationManagerClever() throws Exception{
        this.logger = Logger.getLogger( "VirtualizationManager plugin" );
        this.logger.info("VirtualizationManager plugin created: ");
    }


    @Override
    public void init(Element params, Agent owner) throws CleverException {
        if(params!=null){
            this.HostManagerServiceGuacaTarget = params.getChildText("HostManagerServiceGuacaTarget");
            this.shareTecnologyAdopted = params.getChildText("ShareTecnologyAdopted");
            this.nodeVFSShared=params.getChildText("VFSNODESHARED");
        }
        
        //this.owner = owner;
        while(!this.owner.isPluginState())
        {
            //If the data struct, for matching between VM and HM, isen't into DB then init it.
            this.owner.setPluginState(true);
            try {
            //If the data struct, for matching between VM and HM, isen't into DB then init it.
                if (this.checkDBAgent()){
                    if (!this.checkMatchingVmHMNode()) {
                        this.initVmHMNodeDB();
                    }
                    if (!this.checkVmRunningNode()) {
                        this.initVmRunningDB();
                    }
                    if(!this.checkVEDVMNode()){
                        this.initVEDVMDB();
                    }
                    this.owner.setPluginState(true);
                    
                }
                    
                }
            catch (Exception e) {
                logger.error(e.getMessage(),e);
                this.owner.setPluginState(false);

            }
              //  this.addormenta();
                    
           //   this.shareTecnologyAdopted="SWIFT";
             //     this.migration("1cecf36486404a0099cda8daafe5a8f2", "1dominioB");
        }
        //this.createVM4Migration("prova731","portatileA", new IstantiationParams(LockFile.lockMode.EX));
       
//this.migration(name, Integer.SIZE);
       /* String dst="";
        ArrayList paramss=new ArrayList();
        
                paramss.add("StorageManagerAgent");
                paramss.add("/node");
                paramss.add("name");
                
                paramss.add("port");
                try{
                    dst=(String)this.owner.invoke("DatabaseManagerAgent", "getAttributeNode", true, paramss);
                    logger.debug("$$$$$$$££££££: "+dst);
                }catch(Exception e){
                    logger.error("error migration", e);
                    //return false;
                }*/
    }
    
    private boolean checkDBAgent()throws CleverException{
        List params = new ArrayList();
        return (Boolean)this.owner.invoke("DatabaseManagerAgent", "isCorrectedStarted", true, params); 
    
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
        try{
            logger.debug("verifica del nodo check VMHM");
            return (Boolean)this.owner.invoke("DatabaseManagerAgent", "checkAgentNode", true, params);
        }catch(Exception e){
            logger.error(e.getMessage());
            return false;
        }
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
        
        // the name of the template virtual machine is required
        if(veD.getName().isEmpty()){
                            throw new LogicalCatalogException("VM name is required");
        }
        // check if there is a template for virtual machine with the same name
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
 
 public void registerVEDWithoutDisk(String info, VEDescription veD) throws CleverException{
         
        List params = new ArrayList();
        // the name of the template virtual machine is required
        if(veD.getName().isEmpty()){
                            throw new LogicalCatalogException("VM name is required");
        }
        // check if there is a template for virtual machine with the same name
        params.add("VirtualizationManagerAgent");
        params.add(("/org.clever.Common.VEInfo.VEDescription[./name/text()='"+veD.getName()+"']/name/text()"));
        boolean r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);     
        if(r==true){  
                throw new LogicalCatalogException("VM name already exist");
                }
        params.clear();
        ArrayList<StorageSettings> a_ss=new ArrayList<StorageSettings>();
        a_ss.add(new StorageSettings(0,"","",""));
        veD.setStorage(a_ss);
        String prova=MessageFormatter.messageFromObject(veD);
        params.add("VirtualizationManagerAgent");
        params.add(prova);
        params.add("into");
        params.add("");           
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
    }
 
public HashMap createVM(String id,String targetHM,String lock) throws CleverException{
     IstantiationParams param=new IstantiationParams(org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.EX);
        if(lock.equals("PR"))
            param=new IstantiationParams(org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.PR);
        else if(lock.equals("CR"))
                param=new IstantiationParams(org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.CR);
            else if(lock.equals("CW"))
                param=new IstantiationParams(org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.CW);
                else if(lock.equals("PW"))
                    param=new IstantiationParams(org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.PW);
                    else if(lock.equals("NL"))
                        param=new IstantiationParams(org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.NL);
                        else if(lock.equals("EX"))
                            param=new IstantiationParams(org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.EX);
        return this.createVM(id, targetHM, param);
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
    public HashMap createVM(String id, String targetHM, IstantiationParams param) throws CleverException {
        boolean result = false;
        //TODO: incapsulate this phase in a cicle to have a better management of the choose VM name 
        String vmname = java.util.UUID.randomUUID().toString();
        vmname = vmname.replace("-", "");//this replacement is necessary because if we don't eliminate the "-" the VirtuaslBox "findmachine" function don't work finely 
        if ((param.getLock().equals(LockFile.lockMode.PR)) && ((CmAgent) this.owner).remoteInvocation(targetHM, "HyperVisorAgent", "getHYPVRName", true, new ArrayList()).equals("LibVirt")) {
            this.TakeEasySnapshot(id, vmname, "Snapshot created by CLEVER for a clonable image.", targetHM);
            result = true;
        } else {

            MethodInvoker mi = null;
            // check if into db Sedna exist name of the VM
            List params = new ArrayList();
            params.add("VirtualizationManagerAgent");
            params.add(("/org.clever.Common.VEInfo.VEDescription[./name/text()='" + id + "']/name/text()"));
            boolean r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);
            //this exception is very difficult to appears
            if (r == false) {
                throw new LogicalCatalogException("Template name not valid");
            }
            params.clear();
            params.add("VirtualizationManagerAgent");
            String location = "/Matching_VM_HM/VM[@name='" + vmname + "']";
            params.add(location);
            r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);
            //Insert into DB: mappigetAttributeNodeng VM - HM
            if (r == true) {
                throw new LogicalCatalogException("VM already exist");
            }
            params.clear();
            //Insert into DB: mapping VM - HM
            this.InsertItemIntoMatchingVmHM(vmname, targetHM);

            List params1 = new ArrayList();
            params1.add("VirtualizationManagerAgent");
            location = "/org.clever.Common.VEInfo.VEDescription[./name/text()='" + id + "']";
            params1.add(location);
            String pathxml = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params1);
            VEDescription veD = (VEDescription) MessageFormatter.objectFromMessage(pathxml);
            String diskPath = ((StorageSettings) veD.getStorage().get(0)).getDiskPath();
            params.add(diskPath);
            params.add(targetHM);
            params.add(param.getLock());
            // physical path
            String res = (String) this.owner.invoke("StorageManagerAgent", "lockManager", true, params);

            List params2 = new ArrayList();
            params2.add(diskPath);
            Boolean check = (Boolean) this.owner.invoke("StorageManagerAgent", "check", true, params2);

            ((StorageSettings) veD.getStorage().get(0)).setDiskPath(res);

            params2 = new ArrayList();
            params2.add(vmname);
            params2.add(veD);
            if (param.getLock().ordinal() <= 4) {
                params2.add(true);
            } else {
                params2.add(false);
            }

            if (!check) {
                result = (Boolean) ((CmAgent) this.owner).remoteInvocation(targetHM, "HyperVisorAgent", "registerVm", true, params2);
            } else {
                result = (Boolean) ((CmAgent) this.owner).remoteInvocation(targetHM, "HyperVisorAgent", "createVm", true, params2);
            }
            this.InsertItemIntoMatchingVEDVm(id, vmname, param.getLock(), res, diskPath);
        }
        if (!result) {
            HashMap res = new HashMap();
            res.put("result", new Boolean(false));
            res.put("name", vmname);
            return res;
        } else {
            HashMap res = new HashMap();
            res.put("result", new Boolean(true));
            res.put("name", vmname);
            return res;
        }
    }

    /**
     * Starting VM
     * @param id
     * @return
     * @throws CleverException
     */
    @Override
    public boolean startVm(String id) throws CleverException{
        if(!this.checkVMisMigrated(id))
        {
            List params = new ArrayList();
            String HMTarget=this.getVMHostOWNER(id);
            if(HMTarget==null)
                throw new LogicalCatalogException("VM name not exist");

            // insert intoDB
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            String node="<VM name=\""+id+"\" request=\""+new Date().toString()+"\" started=\"\">"+HMTarget+"<date>"+new Long(calendar.getTimeInMillis()).toString()+"</date></VM>";
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
        else{
            ArrayList params=new ArrayList();
            params.add(id);
            ArrayList info_params=new ArrayList();
            info_params.add("VirtualizzationManagerAgent");
            info_params.add("startVM");
            params.add(info_params);
            ArrayList method_params=new ArrayList();
            method_params.add(id);
            params.add(method_params);
            if((Boolean)this.owner.invoke("FederationManagerAgent", "forwardCommand4VMM", true, params))
                return true;
            else 
                return false;
        }
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
        String HMTarget=this.getVMHostOWNER(id);
        if(HMTarget==null)
            throw new LogicalCatalogException("VM name not exist");
           
        if(!this.checkVMisMigrated(id))
        {

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
        else{
            params.clear();
            params.add(id);
            ArrayList info_params=new ArrayList();
            info_params.add("VirtualizzationManagerAgent");
            info_params.add("stopVM");
            params.add(info_params);
            ArrayList method_params=new ArrayList();
            method_params.add(id);
            params.add(method_params);
            if((Boolean)this.owner.invoke("FederationManagerAgent", "forwardCommand4VMM", true, params))
                return true;
            else 
                return false;
        }
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
        params.add("/"+this.nodoMatchingVmHM+"/VM[@parent='"+id+"']/@name");
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
        String HMTarget=this.getVMHostOWNER(id);
        if(HMTarget==null)
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
        String HMTarget=this.getVMHostOWNER(id);

        if(HMTarget==null)
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
        String HMTarget = this.getVMHostOWNER(id);

        if (HMTarget == null) {
            throw new LogicalCatalogException("VM name not exist");
        }
        params = new ArrayList();
        params.add(id);
        boolean result = (Boolean) ((CmAgent) this.owner).remoteInvocation(HMTarget, "HyperVisorAgent", "suspend", true, params);

        if (result) {
            params = new ArrayList();
            params.add("VirtualizationManagerAgent");
            params.add("/VMs_Running/VM[@name='" + id + "']");

            this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
        }

        return result;
    }
    
    
    @Override
    public boolean TakeEasySnapshot(String id, String nameS, String description, String targetHM) throws CleverException {
        boolean result = false;
        MethodInvoker mi = null;
        // check if into db Sedna exist name of the template
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(("/org.clever.Common.VEInfo.VEDescription[./name/text()='" + id + "']/name/text()"));
        boolean r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);
        if (r == false) {
            throw new LogicalCatalogException("Template name not valid");
        }
        params.clear();
        params.add("VirtualizationManagerAgent");
        String location = "/Matching_VM_HM/VM[@name='" + nameS + "']";
        params.add(location);
        r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);
        //Insert into DB: mappigetAttributeNodeng VM - HM
        if (r == true) {
            throw new LogicalCatalogException("SN already exist");
        }
        this.InsertItemIntoMatchingSnHM(id, nameS, targetHM);
        LockFile.lockMode lock = LockFile.lockMode.CR;
        List params1 = new ArrayList();
        params1.add("VirtualizationManagerAgent");
        location = "/org.clever.Common.VEInfo.VEDescription[./name/text()='" + id + "']";
        params1.add(location);
        String pathxml = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params1);
        VEDescription veD = (VEDescription) MessageFormatter.objectFromMessage(pathxml);
        params.clear();
        params.add(((StorageSettings) veD.getStorage().get(0)).getDiskPath());
        params.add(targetHM);
        params.add(lock);
        // physical path
        String localpath = (String) this.owner.invoke("StorageManagerAgent", "lockManager", true, params);
        params.clear();
        params.add(localpath);
        params.add(((StorageSettings) veD.getStorage().get(0)).getDiskPath());
        params.add(targetHM);
        params.add(lock);
        String snapshotLocalPath = (String) this.owner.invoke("StorageManagerAgent", "SnapshotImageCreate", true, params);
        this.InsertItemIntoMatchingVEDVm(id, nameS, lock, snapshotLocalPath, localpath);

        ((StorageSettings) veD.getStorage().get(0)).setDiskPath(snapshotLocalPath);
        veD.setName(nameS);
        params.clear();
        params.add(nameS);
        params.add(veD);
        params.add(true);

        result = (Boolean) ((CmAgent) this.owner).remoteInvocation(targetHM, "HyperVisorAgent", "createVm", true, params);

        if (!result) {
            throw new CleverException("creating SN " + nameS + " failed!");
        }
        return true;
    }
    
    
    @Override
    public boolean deleteVm(String id) throws CleverException {
        List params = new ArrayList();
        String nomeVED = "";
        String HMTarget = this.getVMHostOWNER(id);

        if (HMTarget == null) {
            throw new LogicalCatalogException("VM name not exist");
        }

        if (!this.checkVMisMigrated(id)) {
            params.clear();
            params.add("VirtualizationManagerAgent");
            params.add("/Matching_VED_VM/VED[@VM_name='" + id + "']/locklevel/text()");
            String lock = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
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
            //VM INFORMATION MANAGEMENT BLOCK
            params.add("VirtualizationManagerAgent");
            String location = "/Matching_VED_VM/VED[@VM_name='" + id + "']";
            params.add(location);
            String tipo = "name";
            params.add(tipo);
            nomeVED = (String) this.owner.invoke("DatabaseManagerAgent", "getAttributeNode", true, params);
            params.clear();
            params.add("VirtualizationManagerAgent");
            params.add("/Matching_VED_VM/VED[@VM_name='" + id + "']/ParentHDD/text()");
            String pathHDD = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
            params.clear();
            params.add("VirtualizationManagerAgent");
            params.add("/Matching_VED_VM/VED[@VM_name='" + id + "']/HDD/text()");
            String HDD = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
            params.clear();
            //inserire un controllo del tipo di lock realizzato sul disco della macchina
            //se  lock di tipo ex richiamre la funzione attuale altrimenti richiamare la funzione corretta
            //questa funzione deve essere modificata per eliminare il file corretto correlato alla vm
            //attualmente cancella la golden image
            if (lock.equals("Null Lock")) {
                //temporarily Nothing to do, in this case the type of lock indicates that the VM's Disk must not be deleted
            } else if ((lock.equals("Concurrent Read")) || (lock.equals("Protected Read"))) {
                params.add("VirtualizationManagerAgent");
                params.add("/Matching_VED_VM/VED/HDD/text()");
                String pathsHDDs = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
                boolean testValue = testUsageHDD(pathsHDDs, HDD);
                params.clear();
                if (testValue) {
                    params.add(HDD);
                    params.add(id);
                    params.add(HMTarget);
                    this.owner.invoke("StorageManagerAgent", "deleteFile", true, params);
                    params.clear();
                }
            } else if (lock.equals("Concurrent Write")) {
                //Nothing to do, in this case the type of lock indicates that the VM's Disk must not be deleted
            } else if (lock.equals("Protected Write")) {
                //qui è necessario verificare se ci sono altre vm che usano quel disco se non ce ne sono allora si può eliminare,
                //anche se momentaneamente questa modalità non viene usata
            } else if (lock.equals("Exclusive")) {

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
                this.owner.invoke("StorageManagerAgent", "deleteFile", true, params);
                params.clear();
            }
            params.add("VirtualizationManagerAgent");
            params.add(("/Matching_VED_VM/VED[@VM_name='" + id + "']"));
            this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
            params.clear();
            params.add("VirtualizationManagerAgent");
            params.add(("/Matching_VM_HM/VM[@name='" + id + "']"));
            this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
            params.clear();
            params.add(id);
            ((CmAgent) this.owner).remoteInvocation(HMTarget, "HyperVisorAgent", "unregisterVm", true, params);
            return true;
        } else {
            params.clear();
            params.add(id);
            ArrayList info_params = new ArrayList();
            info_params.add("VirtualizzationManagerAgent");
            info_params.add("deleteVM");
            params.add(info_params);
            ArrayList method_params = new ArrayList();
            method_params.add(id);
            params.add(method_params);
            if ((Boolean) this.owner.invoke("FederationManagerAgent", "forwardCommand4VMM", true, params)) {
                params.clear();
                //if all is completed without error we can delete info on Database Manager Agent
                params.add("VirtualizationManagerAgent");
                params.add(("/Matching_VED_VM/VED[@VM_name='" + id + "']"));
                this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
                params.clear();
                params.add("VirtualizationManagerAgent");
                params.add(("/Matching_VM_HM/VM[@name='" + id + "']"));
                this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
                return true;
            } else {
                return false;
            }
        }
    }
    
    
    private boolean testUsageHDD(String pathHDD, String element2verfiy) {
        int counter = 0;
        String[] alp = pathHDD.split(".vdi");
        for (int i = 0; i < alp.length; i++) {
            //String[] alp2=alp[i].split("/");
            if (element2verfiy.equals(alp[i] + ".vdi")) {
                counter++;
                logger.debug("$$confronto: " + element2verfiy + " " + alp[i] + ".vdi" + " " + counter);
                if (counter > 1) {
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * Method used to add an interface to Virtual environment.
     * This method is optimized to work with VirtualBox.
     * @param id
     * @param inf
     * @param mac
     * @param type
     * @return
     * @throws CleverException 
     */
    public boolean attachInterface(String id, String inf, String mac, String type) throws CleverException {
        List params = new ArrayList();
        String HMTarget = this.getVMHostOWNER(id);
        if (HMTarget == null) {
            throw new LogicalCatalogException("VM name not exist");
        }
        params.clear();
        params.add(id);
        params.add(inf);
        params.add(mac);
        params.add(type);
        ((CmAgent) this.owner).remoteInvocation(HMTarget, "HyperVisorAgent", "attachInterface", true, params);
        boolean result = insertNetInterfaceIntoDb(id, inf, mac, type);
        return result;
    }
      
 
    /**
     * Method used to register new VM network interface in the database 
     * @param id
     * @param inf
     * @param mac
     * @param type
     * @return
     * @throws CleverException 
     */
    public boolean insertNetInterfaceIntoDb(String id, String inf, String mac, String type) throws CleverException {
        String iface = "<interface>"
                + "<name>" + inf + "</name>"
                + "<type>" + type + "</type>"
                + "<mac_address>" + mac + "</mac_address>"
                + "</interface>";
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add(iface);
        params.add("into");
        params.add("/Matching_VM_HM/VM[@name='" + id + "']");
        this.owner.invoke("DatabaseManagerAgent", "insertNode", true, params);
        return true;
    }
    
    /**
     * This function is called to get a list of all MacAddress 
     * @param id
     * @return
     * @throws CleverException 
     */
    public String listMac_address(String id) throws CleverException {
        List params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/Matching_VM_HM/VM[@name='" + id + "']/interface/mac_address/text()");
        String result = (String) this.owner.invoke("DatabaseManagerAgent", "querytab", true, params);
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
            params.add("name");
            result=(List<String>)this.owner.invoke("DatabaseManagerAgent", "getAttributeWithInternalCond", true, params);
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
                 logger.debug("manageReUpHost clever:"+vmName+ " hypvr:"+ vmsHost.lastIndexOf(vmName));
                        
                 if(vmsHost.lastIndexOf(vmName)==-1){
                    //is it necessary create a log for this operation ?
                    this.markAsInconsistentVM(vmName,false);
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
                    this.markAsInconsistentVM(vmName,true);
                 }
            }
        }
        catch(Exception e){
            //logger.error("An exception has verified in function <manageReUpHost> :"+e.getMessage());
        }
    }
/*FUNCTION FOR INTERATION WITH DB*/
    /**
     * Function used to retrieve the VM hostowner.
     * @param id
     * @return
     * @throws CleverException 
     */
    private String getVMHostOWNER(String id)throws CleverException{
        ArrayList params=new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/"+this.nodoMatchingVmHM+"/VM[@name=\""+id+"\"]/host/text()");
        String hostowner="";
        try{
            hostowner=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
            return hostowner;
        }catch(Exception e){
            logger.error("error in retreive host owner of VM:"+id, e);
            return null;//creare un'eccezione relativa al non funzionamento del DB
        }
        
    }
    /**
     * This function is used to replace the hdd info for the VM identified by vm_id.
     * @param vm_id
     * @param hddpath
     * @throws CleverException 
     */
    private void associateHddToVM(String vm_id,String hddpath)throws CleverException{
        ArrayList params=new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/"+this.nodoVEDescriptorVm+"/VED[@VM_name=\""+vm_id+"\"]/HDD");
        params.add("<HDD>"+hddpath+"</HDD>");
        String hostowner="";
        try{
            this.owner.invoke("DatabaseManagerAgent", "replaceNode", true, params);
            this.logger.debug("Sedna entry relative at HDD in VED_VM NODE for VM: "+vm_id+" is modified!");
        }catch(Exception e){
            logger.error("error in retreive host owner of VM:"+vm_id, e);
            //creare un'eccezione relativa al non funzionamento del DB
        }
        
    }
    /**
     * Method used to mark a VM as inconsistent
     * @param id
     * @param run 
     */
    private void markAsInconsistentVM(String id, boolean run){
       try
       {
            List params=new ArrayList();
            if(run){
                
                params.add("VirtualizationManagerAgent");
                params.add(("/VMs_Running/VM[@name='"+id+"']/state"));
                params.add("<state>inconsistent</state>");
                this.owner.invoke("DatabaseManagerAgent", "replaceNode", true, params);
                this.logger.debug("Sedna entry relative at VM running "+id+" is modified!");
            }
            else{
                params.clear();
                params.add("VirtualizationManagerAgent");
                params.add(("/Matching_VM_HM/VM[@name='"+id+"']/state"));
                params.add("<state>inconsistent</state>");
                this.owner.invoke("DatabaseManagerAgent", "replaceNode", true, params);
                /*
                params.clear();
                params.add("VirtualizationManagerAgent");
                params.add(("/Matching_VED_VM/VED[@VM_name='"+id+"']"));
                this.owner.invoke("DatabaseManagerAgent", "deleteNode", true, params);
                this.logger.debug("Sedna entry relative at VM "+id+" are deleted!");
                */
            }
            
       }
       catch(Exception e){
           this.logger.error("Error in execution of function 'markAsInconsistentVM':"+e.getMessage());
       }
    }
    /**
     * Used to mark the VM as migrated.
     * @param id, Virtual Machine name
     */
    private void markAsMigratedVM(String id){
       try
       {
           List params = new ArrayList();
           params.add("VirtualizationManagerAgent");
           params.add(("/Matching_VM_HM/VM[@name='" + id + "']/migrated"));
           params.add("<migrated>true</migrated>");
           this.owner.invoke("DatabaseManagerAgent", "replaceNode", true, params);
           }
       catch(Exception e){
           this.logger.error("Error in execution of function 'markAsMigratedVM':"+e.getMessage());
       }
    }
    /**
     * Used to mark the VM as own.
     * @param id, Virtual Machine name
     */
    private void markAsReturnedVM(String id){
       try
       {
           List params = new ArrayList();
           params.add("VirtualizationManagerAgent");
           params.add(("/Matching_VM_HM/VM[@name='" + id + "']/migrated"));
           params.add("<migrated>false</migrated>");
           this.owner.invoke("DatabaseManagerAgent", "replaceNode", true, params);
           }
       catch(Exception e){
           this.logger.error("Error in execution of function 'markAsReturnedVM':"+e.getMessage());
       }
    }
     /**
     * Insert new item for matching between Virtual Machine and Host Manager.
     * When it is inserted migrated VM's informations HDD e ParentHDD arethe same.  
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
        this.InsertItemIntoMatchingVmHM(id,HostManagerTarget,"consistent","false");
    }
    /**
     * Insert new item for matching between Virtual Machine and Host Manager
     * @param id
     * @param HostManagerTarget
     * @param state
     * @throws CleverException 
     */    
    private void InsertItemIntoMatchingVmHM(String id, String HostManagerTarget,String state,String migrated) throws CleverException {
        String node="<VM name=\""+id+"\" request=\""+new Date().toString()+"\""+">"
                       +"<host>"+HostManagerTarget+"</host>"
                       +"<state>"+state+"</state>"
                       +"<migrated>"+migrated+"</migrated>"
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
    
     
    public void shutdownPluginInstance(){
        
    }
    
    //<editor-fold defaultstate="collapsed" desc="Function used for VM Migration">
    /**
     * Function called to start migration operation from home cloud. 
     * @param VMName
     * @param idfedOp
     * @return
     * @throws CleverException 
     */
    public boolean migration(String VMName, String idfedOp) throws CleverException {
        org.clever.ClusterManager.FederatorManager.FederatorDataContainer fdc = new org.clever.ClusterManager.FederatorManager.FederatorDataContainer();
        //retrieve VED phase
        String vedName, pathxml;
        vedName = "";
        pathxml = "";
        ArrayList params = new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/Matching_VED_VM/VED");
        params.add("VM_name=\"" + VMName + "\"");
        params.add("@name");
        try {
            vedName = (String) this.owner.invoke("DatabaseManagerAgent", "getAttributeNode", true, params);
        } catch (Exception e) {
            logger.error("error migration", e);
            return false;
        }
        params.clear();
        params.add("VirtualizationManagerAgent");
        String locationVED = "/org.clever.Common.VEInfo.VEDescription[./name/text()='" + vedName + "']";
        params.add(locationVED);
        try {
            pathxml = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        } catch (Exception e) {
            logger.error("error migration", e);
            return false;
        }
        VEDescription veD = (VEDescription) MessageFormatter.objectFromMessage(pathxml);
        fdc.setVED(veD);
        fdc.init_resource();
        fdc.addElementToResource("VMName", VMName);
        fdc.addElementToResource("vedName", vedName);
        //GOLDENIMAGE name
        fdc.addElementToResource("DiskName", veD.getStorage().get(0).getDiskPath());
        //TODO: future works : Modify to make possibile the management of multiple disk
        fdc.setOperationId(idfedOp);
        //TODO: future works(actually we transfert all disk) : add name/url for disk/goldenImage on VFS shared/SWIFT
        ////find host where is hosted VM ready for migration.
        String hostowner = "";
        try {
            hostowner = this.getVMHostOWNER(VMName);
        } catch (Exception e) {
            logger.error("error in retreive host owner phase of migration VM:" + VMName, e);
            return false;
        }
////stop vm
        try {
            params.clear();
            params.add(VMName);
            if ((Boolean) ((CmAgent) this.owner).remoteInvocation(hostowner, "HyperVisorAgent", "isRunning", true, params)) {
                this.stopVm(VMName);
            }
        } catch (Exception e) {

            logger.error("error in stopping VM phaseof migration VM:" + VMName, e);
        }
////take disk name
//////the name can be taken from fdc.addElementToResource("DiskName" if you want retrieve the golden image name
        String pathHDD;
        try {
            params.clear();
            params.add("VirtualizationManagerAgent");
            params.add("/" + this.nodoVEDescriptorVm + "/VED[@VM_name=\"" + VMName + "\"]/HDD/text()");
            pathHDD = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
////start store disk name
//////check if we take image from SWIFT OR FROM VFS
            if (this.shareTecnologyAdopted.equalsIgnoreCase("SWIFT")) {
////////SWIFT
//////////1°: we create container to store Object
                org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput spo = null;
                try {
                    params.clear();
////////////create object parameter for swift method
                    org.clever.HostManager.ObjectStoragePlugins.Swift.InsertContainer spi = new org.clever.HostManager.ObjectStoragePlugins.Swift.InsertContainer();
                    org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput spiC = new org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput();
                    spiC.type = org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput.tipoObjectInput.InsertContainer;
                    String nomeContainer = VMName + hostowner;
                    spi.setContainer(nomeContainer);
////////////create token parameter for all interaction on Swift here and on foreing cloud
                    org.clever.ClusterManager.IdentityServicePlugins.Keystone.Token token = new org.clever.ClusterManager.IdentityServicePlugins.Keystone.Token();
                    try {
                        token = (org.clever.ClusterManager.IdentityServicePlugins.Keystone.Token) this.owner.invoke("IdentityServiceAgent", "getInfo4interactonSWIFT", true, new ArrayList());
                    } catch (Exception e) {
                        throw new CleverException(e.getMessage());
                    }
////////////end token creation phase
                    spi.setTokenId(token.getId());
                    spi.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
                    spi.elaboraInfo();
                    spiC.ogg = spi;
                    params.add(spiC);
                    spo = (org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostowner, "ObjectStorageAgent", "createContainer", true, params);
////////////end creation container phase                    
////////////copy on container the object                     
                    try {
                        params.clear();
////////////create object parameter for swift method                       
                        org.clever.HostManager.ObjectStoragePlugins.Swift.InsertObject spi2 = new org.clever.HostManager.ObjectStoragePlugins.Swift.InsertObject();
                        spiC.type = org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput.tipoObjectInput.InsertObject;
                        spi2.setContainer(nomeContainer);
                        spi2.setTokenId(token.getId());
                        spi2.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
                        spi2.setPathObject(pathHDD);
                        spi2.elaboraInfo();
                        spiC.ogg = spi2;
                        params.add(spiC);
                        spo = (org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostowner, "ObjectStorageAgent", "createObject", true, params);
                        fdc.setCdi4Swift("SWIFT", ((org.clever.HostManager.ObjectStoragePlugins.Swift.InfoCreateObjectForMongoDb) spo).getUrl(), token.getId());
                    } catch (Exception e) {
                        logger.error("Error occurred in copy Object phase.", e);
                        return false;
                    }
////////////end copy object phase                    
                } catch (Exception e) {
                    logger.error("Error occurred in creation Container phase.", e);
                    return false;
                }
            } else {
////////VFS
//////////1°: Identificate VFS node used for migration  
                //TODO: improve this system to implement a choose algoritm related to
                //Viene usato il primo nodo recuperato dal DB di Sistema
                String dst = this.nodeVFSShared;

                if (dst.equals("")) {
                    params.add("StorageManagerAgent");
                    params.add("/node");
                    params.add("name");
                    try {
                        dst = (String) this.owner.invoke("DatabaseManagerAgent", "getAttributeNode", true, params);
                    } catch (Exception e) {
                        logger.error("Error in identification VFS NODE phase, is impossibile insert disk image in VFS node.", e);
                        return false;
                    }
                }

                //dst deve essere inserito come nuovo nodo per il VFS di CLEVER, si prende il primo nodo disponibile ed inserire una nuova cartella nel quale mettere il nodo logico
                //nodo che verrà eliminato al termine dell'operazione oppure
                //inserirlo semplicemente in un nodo e poi eliminare il disco dal nodo VFS momentaneamente viene fatto così
                params.clear();
//////////verify if NODE exist and if it works            
                params.add(dst);
                params.add("");
                VFSDescription res;
                try {
                    res = (VFSDescription) this.owner.invoke("StorageManagerAgent", "discoveryNode", true, params);
//////////Now we invoke ImageManager for upload the disk on VFS Node with function uploadDiskonVFS
//////////this function need VFS NODE container and the path of the src disk as parameter
                    try {
                        params.clear();
                        params.add(res);
                        params.add(pathHDD);
                        if (!(Boolean) ((CmAgent) this.owner).remoteInvocation(hostowner, "ImageManagerAgent", "uploadDiskonVFS", true, params)) {
                            return false;
                        }

                    } catch (CleverException e) {
                        logger.error(e.getMessage(), e);
                        return false;
                    }
//////////end copy of the disk on node
//////////Insert info into federatorDataContainer to make able foreing cloud to access at shared node                     
                    params.clear();
                    params.add("StorageManagerAgent");
                    params.add("/node[@name=\"" + this.nodeVFSShared + "\"]/org.clever.Common.Storage.VFSDescription");
                    String VFSNODE = "";
                    try {
                        VFSNODE = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
                    } catch (Exception e) {
                        logger.error("error in retrieve VFSSHARED NODE", e);
                        return false;
                    }
                    ParserXML pxml = new ParserXML(VFSNODE);
                    Element e = pxml.getRootElement();
                    Element authpar = e.getChild("auth");
                    String type = e.getChildText("typevfs");
                    String host = e.getChildText("hostname");
                    String port = e.getChildText("port");
                    String path = e.getChildText("path");
                    String[] aRR = pathHDD.split("/");
                    path = path + aRR[aRR.length - 1];
                    fdc.setCdi((String) authpar.getChildText("username"), (String) authpar.getChildText("password"), "VFS", host, Integer.parseInt(port), path, type);

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }

            }
        } catch (Exception e) {
            logger.error("Error in acquisition of phisical path of " + VMName + " hd", e);
        }
//////////end federatorDataContainer preparation phase        
//////////this forward the migration operation to Federator: the federator say at foreing cloud how it have to create VM
        try {
            params.clear();
            params.add(fdc);
            if ((Boolean) this.owner.invoke("FederationManagerAgent", "called4Migration", true, params)) {
                this.markAsMigratedVM(VMName);
            }
        } catch (Exception e) {
            logger.error("error occurred in starting phase of migration process", e);
            throw new CleverException("error occurred in starting phase of migration process");
        }

//////////End
        return true;
    }
    
    /**
     * Method used to verify if the VM is migrated. 
     * This function is called when an operation on VM is performed
     * @param VMName
     * @return 
     */     
    private boolean checkVMisMigrated(String VMName){
        //TODO: verificare se è compliant contutti i casi        
        try{
        ArrayList params=new ArrayList();
        params.add("VirtualizationManagerAgent");
        String location="/Matching_VM_HM/VM[@name=\""+VMName+"\"]/migrated";
        params.add(location);
        String result=(String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
        if(result.equals("true")){
            return true;
        }
        else
            return false;
        }catch(Exception e){
            logger.error("error",e);
        }
        return false;
    }
    
    /**
     * This Function have to create the Virtual Machine in foreing cloud to migration request.
     * All VM Migrated are used with exclusive DISK for security reason.
     * @param ved
     * @param targetHM
     * @param param
     * @return
     * @throws CleverException 
     */
    public HashMap createVM4Migration(org.clever.ClusterManager.FederatorManager.FederatorDataContainer fdc,VEDescription ved,String targetHM,IstantiationParams param,String sharingType) throws CleverException{
        boolean result=false;
        boolean r,check;
        String vmname,res="",diskPath="";
        int counter=15;
        
        List params = new ArrayList();
        do{
            vmname=java.util.UUID.randomUUID().toString();
            vmname=vmname.replace("-", "");
            MethodInvoker mi = null;
            // check if into db Sedna exist name of the VM
            params.add("VirtualizationManagerAgent");
            String location = "/Matching_VM_HM/VM[@name='" + vmname + "']";
            params.add(location);
            r = (Boolean) this.owner.invoke("DatabaseManagerAgent", "existNode", true, params);
            //Insert into DB: mappinggetAttributeNode VM - HM???
            counter--;
        }
        while((r == true)&&(counter>0));
            params.clear();
 //gestione del download del disco condiviso
            

            if(sharingType.equals("SWIFT")){
////SWIFT                
//////Create object parameter for swift method
                //dal CDI dovrò prendere il token e l'url del file che è lo stesso usato per swift
                org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput spo = null;
                try {
                    params.clear();
                    org.clever.HostManager.ObjectStoragePlugins.Swift.InsertObject spi = new org.clever.HostManager.ObjectStoragePlugins.Swift.InsertObject();
                    org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput spiC= new org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput();
                    spiC.type = org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput.tipoObjectInput.InsertObject;
                    spi.setTokenId(fdc.gettokenfromCDI());
                    spi.setUrlMigration(fdc.getPathfromCDI());
                    spi.elaboraInfoFromUrlComplete();
                    
//TODO: aggiungere funzione che crei il percorso sul quale salvare il file scaricato
//momentaneamente li salva nello stesso path dei dischi gestiti dal sistema, ovvero nella cartella repository                   
                    spi.setPathObject(System.getProperty("user.dir")+"/repository/");
                    spiC.ogg=spi;
                    params.add(spiC);
                    spo = (org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(targetHM, "ObjectStorageAgent", "downloadObject", true, params);
                    res=((org.clever.HostManager.ObjectStoragePlugins.Swift.InfoGetObjectForMongoDb)spo).getPathObjec()+((org.clever.HostManager.ObjectStoragePlugins.Swift.InfoGetObjectForMongoDb)spo).getObject();
                    diskPath=res;
                    params.clear();
                    params.add(res);
                    //check= (Boolean) this.owner.invoke("StorageManagerAgent", "check", true, params);
                    check=true;
                }catch(Exception e){
                    logger.error("Problem occurred in download disk phase. Migration operation can't continue!");
                    throw new CleverException(e.getMessage());
                }
                
                
                
////register VED for this VM
                try{
                    this.registerVEDWithoutDisk("", ved);//the first parameter is not used
                }
                catch(Exception le){
                    logger.error("The operation: VED register for Migration operation indicated by: "+fdc.getOperationId()+" can't be completed. The VEDescriptor is already registered!",le);
                }
            }
            else{
 //VFS Management
////creation of VFS NODE for retrieving disk passed from home cloud                
                try
                {
                this.createVFSNODE(fdc);
                }
                catch(Exception e){
                    logger.error("It's impossible create a VFS node!");
                }
////end creation of VFS NODE
////start elaboration storage info taken from VED: we must do a substitution of the logical VFS node name                 
                diskPath = ((StorageSettings) ved.getStorage().get(0)).getDiskPath();
                String disk=diskPath;
                int i=diskPath.indexOf("/");
                if(i==0)
                    disk=disk.substring(i+1);
                disk=disk.substring(disk.indexOf("/")+1);
                disk=fdc.getNameofCont()+"/"+disk;
////end elaboration disk info
////start preparation disk for VM                
                params.add(disk);
                params.add(targetHM);
                params.add(param.getLock());
                // physical path
                res = (String) this.owner.invoke("StorageManagerAgent", "lockManager", true, params);
                List params2 = new ArrayList();
                params2.add(disk);
                check= (Boolean) this.owner.invoke("StorageManagerAgent", "check", true, params2);
                List<StorageSettings> ss=ved.getStorage();
                ss.get(0).setDiskPath(disk);
                ved.setStorage(ss);
////register VED for this VM
                try{
                     this.register("", ved);//the first parameter is not used
                }
                catch(LogicalCatalogException le){
                    logger.error("The operation: VED register for Migration operation indicated by: "+fdc.getOperationId()+" can't be completed. The VEDescriptor is already registered!",le);
                }
            }

            
////end preparation of disk

////Insert into DB: mapping VM - HM
            //TODO: pensare a come gestire il rientro della VM: nuova funzione o usare qst con un parametro per la corretta gestione delle info?
            this.InsertItemIntoMatchingVmHM(vmname, targetHM,"consistent","migrated_here");
////creation VM phase
//////here we set where is the new VM disk for vm migrated           
            List<StorageSettings> ss=ved.getStorage();
            ss.get(0).setDiskPath(res);//res=$PATHFORNEWDISKMIGRATED$
            ved.setStorage(ss);
//////end setting
            List params2 = new ArrayList();
            
            params2.add(vmname);
            params2.add(ved);
            if (param.getLock().ordinal() <= 4) {
                params2.add(true);
            } else {
                params2.add(false);
            }

            if (!check) {
                result = (Boolean) ((CmAgent) this.owner).remoteInvocation(targetHM, "HyperVisorAgent", "registerVm", true, params2);
            } else {
                result = (Boolean) ((CmAgent) this.owner).remoteInvocation(targetHM, "HyperVisorAgent", "createVm", true, params2);
            }
////end VM creation phase
            this.InsertItemIntoMatchingVEDVm(ved.getName(), vmname, param.getLock(), res, diskPath);
        
        if (!result) {
            HashMap res1 = new HashMap();
            res1.put("result", new Boolean(false));
            res1.put("name", vmname);
            return res1;
        } else {
            HashMap res1 = new HashMap();
            res1.put("result", new Boolean(true));
            res1.put("name", vmname);
            return res1;
        }
    }
    /**
     * This create VFS NODE. that node is used to add VFS shared node to logical catalogue.
     * @param fdc
     * @throws Exception 
     */
    private void createVFSNODE(org.clever.ClusterManager.FederatorManager.FederatorDataContainer fdc) throws Exception{
        
        
        String node="<org.clever.Common.Storage.VFSDescription>"
                +"<auth>"
                    +"<username>"+fdc.getUserfromCDI()+"</username>"
                    +"<password>"+fdc.getPswfromCDI()+"</password>"
                +"</auth>"
                +"<typevfs>"+fdc.getVFS_TypefromCDI()+"</typevfs>"
                +"<hostname>"+fdc.getHostfromCDI()+"</hostname>"
                +"<port>"+fdc.getPortfromCDI()+"</port>"
                +"<path>"+fdc.getPathfromCDI()+"</path>"
                +"<path1></path1>"
            +"</org.clever.Common.Storage.VFSDescription>";
        ArrayList params = new ArrayList();
        params.add(fdc.getNameofCont());
        params.add("mount");
        VFSDescription vfsD =(VFSDescription) MessageFormatter.objectFromMessage(node);
        params.add(vfsD);
        params.add("");
        try{
            this.owner.invoke("StorageManagerAgent","createNode",true, params);
        }catch(Exception e)
        {
            throw e;
        }
    }
/**
 * This function is called to return VM to its owner cloud. 
 * @param idfedOp
 * @param VMName
 * @return FederatorDataContainer which contains the information for recreate VM in remote cloud.
 * @throws CleverException 
 */    
    public org.clever.ClusterManager.FederatorManager.FederatorDataContainer retFromMigration(String idfedOp,String VMName)throws CleverException{
        org.clever.ClusterManager.FederatorManager.FederatorDataContainer fdc =new org.clever.ClusterManager.FederatorManager.FederatorDataContainer();
//retrieve VED phase
        String vedName,pathxml;
        vedName="";
        pathxml="";
        ArrayList params=new ArrayList();
        params.add("VirtualizationManagerAgent");
        params.add("/Matching_VED_VM/VED");
        params.add("VM_name=\""+VMName+"\"");
        params.add("@name");
        try{
            vedName=(String)this.owner.invoke("DatabaseManagerAgent", "getAttributeNode", true, params);
        }catch(Exception e){
            logger.error("error migration", e);
            return null;//----> creare un eccezione apposita per il mancato ritrovamento del VED nel foreign cloud
        }
        params.clear();
        params.add("VirtualizationManagerAgent");
        String locationVED="/org.clever.Common.VEInfo.VEDescription[./name/text()='"+vedName+"']";
        params.add(locationVED);
        try{
            pathxml=(String) this.owner.invoke("DatabaseManagerAgent", "query", true, params); 
        }catch(Exception e){
            logger.error("error migration", e);
            return null;//----> creare un eccezione apposita per il mancato ritrovamento del VED nel foreign cloud
        }
        VEDescription veD =(VEDescription) MessageFormatter.objectFromMessage(pathxml);
//end retrieve VED phase         
        fdc.setVED(veD);
        fdc.init_resource();
        fdc.addElementToResource("VMName",VMName);
        fdc.addElementToResource("vedName", vedName);
//--->probabilmente questa parte non deve essere inserita         
        //nome della GOLDENIMAGE
        //fdc.addElementToResource("Origin_DiskName", veD.getStorage().get(0).getDiskPath());
        //TODO: future works : Modify to make possibile the management of multiple disk
        fdc.setOperationId(idfedOp);
        //TODO: future works(actually we transfert all disk) : add name/url for disk/goldenImage on VFS shared/SWIFT
////find host where is hosted VM ready for migration.
        params.clear();
        
        String hostowner="";
        try{
            hostowner=this.getVMHostOWNER(VMName);
        }catch(Exception e){
            logger.error("error in retreive host owner phase of migration VM:"+VMName, e);
            return null;//----> creare un eccezione apposita per il mancato ritrovamento del VED nel foreign cloud
        }
////stop vm
        try{
            params.clear();
            params.add(VMName);
            if((Boolean)((CmAgent) this.owner).remoteInvocation(hostowner,"HyperVisorAgent","isRunning", true, params))
                this.stopVm(VMName);
        }catch(Exception e){
            
            logger.error("error in stopping VM phaseof migration VM:"+VMName, e);
        }
////take disk name
//////the name can be taken from fdc.addElementToResource("DiskName" if you want retrieve the golden image name
        String pathHDD;
        try {
            params.clear();
            params.add("VirtualizationManagerAgent");
            params.add("/"+this.nodoVEDescriptorVm+"/VED[@VM_name=\""+VMName+"\"]/HDD/text()");
            pathHDD = (String) this.owner.invoke("DatabaseManagerAgent", "query", true, params);
////start store disk name
//////check if we take image from SWIFT OR FROM VFS
            if (this.shareTecnologyAdopted.equalsIgnoreCase("SWIFT")) {
////////SWIFT
//////////1°: we create container to store Object
                org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput spo = null;
                try {
                    params.clear();
////////////create object parameter for swift method
                    org.clever.HostManager.ObjectStoragePlugins.Swift.InsertContainer spi = new org.clever.HostManager.ObjectStoragePlugins.Swift.InsertContainer();
                    org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput spiC= new org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput();
                    spiC.type = org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput.tipoObjectInput.InsertContainer;
                  
                    String nomeContainer=VMName+hostowner;
                    (spi).setContainer(nomeContainer);
////////////create token parameter for all interaction on Swift here and on foreing cloud
                    org.clever.ClusterManager.IdentityServicePlugins.Keystone.Token token=new org.clever.ClusterManager.IdentityServicePlugins.Keystone.Token();
                    try{
                        token=(org.clever.ClusterManager.IdentityServicePlugins.Keystone.Token)this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
                    }catch(Exception e){
                        throw new CleverException(e.getMessage());
                    }
////////////end token creation phase
                    (spi).setTokenId(token.getId());
                    (spi).setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
                    (spi).elaboraInfo();
                    spiC.ogg=spi;
                    params.add(spiC);
                    spo = (org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostowner, "ObjectStorageAgent", "createContainer", true, params);
////////////end creation container phase                    
////////////copy on container the object                     
                    try {
                        params.clear();
////////////create object parameter for swift method                       
                        org.clever.HostManager.ObjectStoragePlugins.Swift.InsertObject spi2 = new org.clever.HostManager.ObjectStoragePlugins.Swift.InsertObject();
                        spiC.type = org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput.tipoObjectInput.InsertObject;
                        spi2.setContainer(nomeContainer);
                        spi2.setTokenId(token.getId());
                        spi2.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
                        spi2.setPathObject(pathHDD);
                        spi2.elaboraInfo();
                        spiC.ogg=spi2;
                        params.add(spiC);
                        spo = (org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostowner, "ObjectStorageAgent", "createObject", true, params);
                        fdc.setCdi4Swift( "SWIFT",  ((org.clever.HostManager.ObjectStoragePlugins.Swift.InfoCreateObjectForMongoDb)spo).getUrl(),token.getId());
                    } catch (Exception e) {
                        logger.error("Error occurred in copy Object phase.", e);
                        return null;//----> creare un eccezione apposita per il mancato uploading del disco su SWIFT foreign cloud
                    }
////////////end copy object phase                    
                } catch (Exception e) {
                    logger.error("Error occurred in creation Container phase.", e);
                    return null;//----> creare un eccezione apposita per il mancato uploading del disco su SWIFT foreign cloud
                }
            }
            else {
////////VFS
//////////1°: Identificate VFS node used for migration              
                String dst=this.nodeVFSShared;
                
                if(dst.equals("")){
                    params.add("StorageManagerAgent");
                    params.add("/node");
                    params.add("name");
                    try{
                        dst=(String)this.owner.invoke("DatabaseManagerAgent", "getAttributeNode", true, params);
                    }catch(Exception e){
                        logger.error("Error in identification VFS NODE phase, is impossibile insert disk image in VFS node.", e);
                        return null;//----> creare un eccezione apposita per il mancato uploading del disco su SWIFT foreign cloud
                    }
                }
                //dst deve essere inserito come nuovo nodo per il VFS di CLEVER, si prende il primo nodo disponibile ed inserire una nuova cartella nel quale mettere il nodo logico
                //nodo che verrà eliminato al termine dell'operazione oppure
                //inserirlo semplicemente in un nodo e poi eliminare il disco dal nodo VFS momentaneamente viene fatto così
                params.clear();
//////////verify if NODE exist and if it works            
                params.add(dst);
                params.add("");
                VFSDescription res;
                try{
                    res=(VFSDescription)this.owner.invoke("StorageManagerAgent","discoveryNode", true, params);
//////////Now we invoke ImageManager for upload the disk on VFS Node with function uploadDiskonVFS
//////////this function need VFS NODE container and the path of the src disk as parameter
                    try{
                        params.clear();
                        params.add(res);
                        params.add(pathHDD);
                        if(!(Boolean)((CmAgent)this.owner).remoteInvocation(hostowner,"ImageManagerAgent","uploadDiskonVFS", true, params))
                            return null;//----> creare un eccezione apposita per il mancato uploading del disco su VFS foreign cloud

                    }catch(CleverException e){
                        logger.error(e.getMessage(),e);
                        return null;//----> creare un eccezione apposita per il mancato uploading del disco su VFS foreign cloud
                    }
//////////end copy of the disk on node
//////////Insert info into federatorDataContainer to make able foreing cloud to access at shared node                     
                    params.clear();
                    params.add("StorageManagerAgent");
                    params.add("/node[@name=\""+this.nodeVFSShared+"\"]/org.clever.Common.Storage.VFSDescription");
                    String VFSNODE="";
                    try{
                        VFSNODE=(String)this.owner.invoke("DatabaseManagerAgent", "query", true, params);
                    }catch(Exception e){
                        logger.error("error in retrieve VFSSHARED NODE",e);
                        return null;//----> creare un eccezione apposita per il mancato completamento dell'inserimento delle info nel VFS
                    }
                    ParserXML pxml=new ParserXML(VFSNODE);
                    Element e=pxml.getRootElement();
                    Element authpar=e.getChild("auth");
                    String type=e.getChildText("typevfs");
                    String host=e.getChildText("hostname");
                    String port=e.getChildText("port");
                    String path=e.getChildText("path");
                    String[] aRR=pathHDD.split("/");
                    path=path+aRR[aRR.length-1];
                    fdc.setCdi((String)authpar.getChildText("username"), (String)authpar.getChildText("password"), "VFS", host, Integer.parseInt(port), path,type);
                    
                }catch(Exception e){
                    logger.error(e.getMessage(),e);
                    return null;//----> creare un eccezione apposita per il mancato completamento dell'inserimento delle info nel VFS
                }
                
            }
        } catch (Exception e) {
            logger.error("Error in acquisition of phisical path of " + VMName + " hd", e);
        }
//////////end federatorDataContainer preparation phase        
//////////this forward the migration operation to Federator: the federator say at foreing cloud how it have to create VM
        return fdc;
    }
    /**
     * Method used to create the VM recovered from the migration site to VM home cloud.  
     * @param fdc, Federation Data Container object
     * @param VMName, original UUID of the VM for CLEVER 
     * @param targetHM, Host Manager Entity that will be instantiate the VM
     * @return
     * @throws CleverException 
     */
    public Boolean reCreateMigratedVM(org.clever.ClusterManager.FederatorManager.FederatorDataContainer fdc,String VMName,String targetHM )throws CleverException{
        boolean result=false;
        boolean check=false;
        String res="",diskPath="";
        VEDescription ved=fdc.getVED();;
        List params = new ArrayList();
        IstantiationParams param=new IstantiationParams(org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.EX);
        if(targetHM.equals(""))
        {    
            try{
                targetHM=this.getVMHostOWNER(VMName);
                if(targetHM==null){
                    return false;
                }
            }catch(Exception e){
                logger.error("error in retreive host owner phase in function reCreateMigratedVM for the VM:"+VMName, e);
            }
        }
        
//gestione del download del disco condiviso
            

            if(fdc.getTypefromCDI().equals("SWIFT")){
////SWIFT                
//////Create object parameter for swift method
                //dal CDI dovrò prendere il token e l'url del file che è lo stesso usato per swift
                org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput spo = null;
                try {
                    params.clear();
                    org.clever.HostManager.ObjectStoragePlugins.Swift.InsertObject spi = new org.clever.HostManager.ObjectStoragePlugins.Swift.InsertObject();
                    org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput spiC= new org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput();
                    spiC.type = org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput.tipoObjectInput.InsertObject;
                    spi.setTokenId(fdc.gettokenfromCDI());
                    spi.setUrlMigration(fdc.getPathfromCDI());
                    spi.elaboraInfoFromUrlComplete();
                    
//TODO: aggiungere funzione che crei il percorso sul quale salvare il file scaricato
//momentaneamente li salva nello stesso path dei dischi gestiti dal sistema, ovvero nella cartella repository                   
                    spi.setPathObject(System.getProperty("user.dir")+"/repository/");
                    spiC.ogg=spi;
                    params.add(spiC);
                    spo = (org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(targetHM, "ObjectStorageAgent", "downloadObject", true, params);
                    res=((org.clever.HostManager.ObjectStoragePlugins.Swift.InfoGetObjectForMongoDb)spo).getPathObjec()+((org.clever.HostManager.ObjectStoragePlugins.Swift.InfoGetObjectForMongoDb)spo).getObject();
                    //diskPath=res;
                    
                    this.associateHddToVM(VMName, res);
                    check=true;
                }catch(Exception e){
                    logger.error("Problem occurred in associateHddToVM function. the new hdd can't be associated correctly to VM:"+VMName);
                    //throw new CleverException(e.getMessage());
                    //TODO: prepare a specific Exception for this.
                }
           }
            else{
//VFS Management
////creation of VFS NODE for retrieving disk passed from home cloud                
                try
                {
                    this.createVFSNODE(fdc);
                }
                catch(Exception e){
                    logger.error("It's impossible create a VFS node! Function 'reCreateMigratedVM' can be continue!");
                }
////end creation of VFS NODE
////start elaboration storage info taken from VED: we must do a substitution of the logical VFS node name                 
                diskPath = ((StorageSettings) fdc.getVED().getStorage().get(0)).getDiskPath();
                String disk=diskPath;
                int i=diskPath.indexOf("/");
                if(i==0)
                    disk=disk.substring(i+1);
                disk=disk.substring(disk.indexOf("/")+1);
                disk=fdc.getNameofCont()+"/"+disk;
////end elaboration disk info
////start preparation disk for VM                
                params.add(disk);
                params.add(targetHM);
                params.add(param.getLock());
                // physical path
                try{
                    res = (String) this.owner.invoke("StorageManagerAgent", "lockManager", true, params);
                    List params2 = new ArrayList();
                    params2.add(disk);
                    check= (Boolean) this.owner.invoke("StorageManagerAgent", "check", true, params2);
                    List<StorageSettings> ss=ved.getStorage();
                    ss.get(0).setDiskPath(disk);
                    ved.setStorage(ss);
                    this.associateHddToVM(VMName, res);
                }catch(Exception e){
                    logger.error("Problem occurred in associateHddToVM function. the new hdd can't be associated correctly to VM:"+VMName);
                }
            
            }
////end preparation of disk
////creation VM phase
//////here we set where is the new VM disk for vm migrated           
            List<StorageSettings> ss=ved.getStorage();
            ss.get(0).setDiskPath(res);
            ved.setStorage(ss);
//////end setting
            List params2 = new ArrayList();
            
            params2.add(VMName);
            params2.add(ved);
            if (param.getLock().ordinal() <= 4) {
                params2.add(true);
            } else {
                params2.add(false);
            }
            try{
                if (!check) {
                    result = (Boolean) ((CmAgent) this.owner).remoteInvocation(targetHM, "HyperVisorAgent", "registerVm", true, params2);
                } else {
                    result = (Boolean) ((CmAgent) this.owner).remoteInvocation(targetHM, "HyperVisorAgent", "createVm", true, params2);
                }
            }catch(Exception e){
                logger.error("Exception occurred in vm creation on hypervisor",e);
                throw new CleverException(e.getMessage());
            }
////end VM creation phase      
        this.markAsReturnedVM(VMName);
        return result;
    }
    //</editor-fold>
    
    
    
    
    
    
    
    
  /**
  * ACTUALLY NOT USED.
  * This function is used to replace VED descriptor in DB sturcture.
  * @param info
  * @param veD
  * @throws CleverException 
  */
/* public void replaceVEDinDB(String info, VEDescription veD) throws CleverException{
         
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
    }*/
}

 
  

