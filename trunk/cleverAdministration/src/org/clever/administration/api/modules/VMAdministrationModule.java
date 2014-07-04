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
 * Copyright (c) 2013 Universita' degli studi di Messina
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

package org.clever.administration.api.modules;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.VEInfo.VEState;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.administration.annotations.HasScripts;
import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.annotations.ShellParameter;
import org.clever.administration.api.Session;


/**
 * Modulo per gestire le VM
 * @author maurizio
 * @author Giuseppe Tricomi 2014
 */
@HasScripts(value="VMM", script="scripts/vmm.bsh", comment="Administration module for VMs")
public class VMAdministrationModule extends AdministrationModule{
    
    public VMAdministrationModule (Session s)
    {
        super(s);
       
    }
    
    //<editor-fold defaultstate="collapsed" desc="Virtualization function">
    /**
     * This Function create a  VEDescriptor Template on SEDNA.
     * @param absolutePath Name of the host where the guest will be deploy
     * @return true on success
     */
    @ShellCommand(comment="Create a VirtualMachine on a specified host")
    public void registervm(@ShellParameter(name="absolutePath", comment="input VEDescriptor, insert absolute path or default to use default VEDescriptor") String absolutePath) throws CleverException
    {
        ArrayList params = new ArrayList();
        String path="";
        if(absolutePath.equals("default")){
            String localPath=System.getProperty("user.dir");
            String cfgMountPath = "/src/org/clever/administration/config/VEDescription.xml";
            path=localPath+cfgMountPath;
        }
        else
            path=absolutePath;
        String VE="";
        try{
            FileInputStream fstream = null;
            fstream = new FileInputStream(path);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            br.readLine();
            while (((strLine = br.readLine()) != null))
                VE=VE+strLine;
            VEDescription veD =(VEDescription) MessageFormatter.objectFromMessage(VE);
            params.add("owners");
            params.add(veD);
            this.execSyncCommand(
                    this.session.getHostAdministrationModule().getActiveCM(),
                    "VirtualizationManagerAgent",
                    "register",
                    params,
                    false);
        }catch(Exception ex){
            throw new CleverException(ex,ex.getMessage());
        }
        
    }
    
    /**
     * This Function create a Virtual Machine.
     * @param host Name of the host where the guest will be deploy
     * @param veID Name of the template where the guest will be deploy
     * @param lock Type of Lock for VirtualMachine Disk.
     * @return true on success
     */
    @ShellCommand(comment="Create a VirtualMachine on a specified host")
    public String createVM(@ShellParameter(name="host", comment="HOST target") String host,
    @ShellParameter(name="veID", comment="VEDescriptor Template name") String veId,
    @ShellParameter(name="lock", comment="type of disk lock, PR or EX ") String lock) throws CleverException
    {
        
        String returnResponse;
        ArrayList params = new ArrayList();
        params.add(veId);
        params.add(host);
        params.add(lock);
        HashMap res=(HashMap)this.execSyncCommand(
                this.session.getHostAdministrationModule().getActiveCM(),
                "VirtualizationManagerAgent",
                "createVM",
                params,
                false);
        if((Boolean)res.get("result"))
            returnResponse = ( String )res.get("name");
        else
            returnResponse = "Creation of VM:"+( String )res.get("name")+"is Failed!";
        
        
        return returnResponse;
    }
    
    
    
    /**
     * Start a Virtual Machine
     * @return true on success
     */
    @ShellCommand(comment="Start VM ")
    public boolean startVM(@ShellParameter(name="VMname", comment="Name of VM that has to be started ") String VMName) throws CleverException
    {
        
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(VMName);
        returnResponse = ( Boolean )
                this.execSyncCommand(
                this.session.getHostAdministrationModule().getActiveCM(),
                "VirtualizationManagerAgent",
                "startVm",
                params,
                false);
        return returnResponse;
    }
    /**
     * Unregister a Virtual Machine Template
     *
     * @return true on success
     */
    @ShellCommand(comment="Unregister Virtual Machine template ")
    public boolean unregisterVM(@ShellParameter(name="templatename", comment="Name of the template that it will be deleted") String templatename) throws CleverException
    {
        
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(templatename);
        returnResponse = ( Boolean )
                this.execSyncCommand(
                this.session.getHostAdministrationModule().getActiveCM(),
                "VirtualizationManagerAgent",
                "unregisterVm",
                params,
                false);
        return returnResponse;
    }
    
    
    /**
     * Ferma una VM
     * @return true on success
     */
    @ShellCommand(comment="Stop VM ")
    public boolean stopVM(@ShellParameter(name="VMName", comment="VM name") String VMName,
    @ShellParameter(name="poweroff", comment="Force poweroff") Boolean poweroff) throws CleverException
    {
        
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(VMName);
        params.add(poweroff);
        returnResponse = ( Boolean )
                this.execSyncCommand(
                this.session.getHostAdministrationModule().getActiveCM(),
                "VirtualizationManagerAgent",
                "stopVm",
                params,
                false);
        return returnResponse;
    }
    
    
    /**
     * Ferma una VM direttamente dando il comando ad un HOST
     * @return true on success
     */
    @ShellCommand(comment="Destroy VM directly invoking an HOST")
    public boolean deleteVM(@ShellParameter(name="VMName", comment="VM to be deleted") String VMName) throws CleverException
    {
        
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(VMName);
        
        returnResponse = ( Boolean )
                this.execSyncCommand(
                this.session.getHostAdministrationModule().getActiveCM(),
                "VirtualizationManagerAgent",
                "deleteVm",
                params,
                false);
        return returnResponse;
    }
    
    /**
     * Return the list of VM probe connected
     * @return
     */
    @ShellCommand
    public String ListProbe () throws CleverException
    {
        ArrayList params = new ArrayList();
        
        try{
            List res=(List)this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),
                    "InfoAgent",
                    "listProbe",
                    params,
                    false);
            Iterator ir=res.iterator();
            String result="\n--------List Probe--------\n";
            while(ir.hasNext())
                result=result+((org.clever.Common.Shared.HostEntityInfo)ir.next()).getNick()+"\n";
            result=result+"\n-------------------------------";
            return result;
        }catch(Exception e){
            throw new CleverException(e);
        }
        
    }
    
    /**
     * Return the list of VM probe connected
     * @return
     */
    @ShellCommand
    public String ListTemplate () throws CleverException
    {
        
        
        try{
            List res=(List)this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),
                    "DatabaseManagerAgent",
                    "querytemplate",
                    this.emptyParams,
                    false);
            Iterator ir=res.iterator();
            String result="\n--------List Probe--------\n";
            while(ir.hasNext())
                result=result+(String)ir.next()+"\n";
            result=result+"\n-------------------------------";
            return result;
        }catch(Exception e){
            throw new CleverException(e);
        }
        
    }
    /**
     * This Function attach a Network interface to Virtual Machine
     * @param VMName String, Name for Virtual Machine
     * @param interfaceName String, Name for Interface, [VirtualBox case:] referred to TypeName for Interface you must be indicate the correct type of interface: NAT->empty field,Bridge->physical Interface Name where Virtual Interface Network will be connected,Internal-> intnet,HostOnly-> Virtual Network where the Interface will be connected.
     * @param TypeName String, [VirtualBox Case:]Network Interface Type used for VM
     * @param Mac String, MAC ADDRESS fo Virtual Machine network interface, [VirtualBox case:] if this field is \"\" then VBOX create automatically the MAC Address
     * @return true on success
     */
    @ShellCommand(comment="Attach an interface on selected Virtual Machine")
    public boolean AttachInterface(@ShellParameter(name="VMName", comment="VM name") String VMName,
    @ShellParameter(name="interfaceName", comment="Name of Interface") String interfaceName,
    @ShellParameter(name="TypeName", comment="[VirtualBox Case:]Network Interface Type used for VM") String TypeName,
    @ShellParameter(name="Mac", comment="MAC Address that will be assigned to Network Interface,[VirtualBox case:] if this field is \"\" then VBOX create automatically the MAC Address") String Mac) throws CleverException
    {
        String regex = "[a-fA-F0-9]{12}";
        if (!Pattern.matches(regex, Mac) && !Mac.equals(""))
        {
            System.err.println("Mac Address is wrong! Check the Mac Address passed to function");
            return false;
        }
        else{
            Boolean returnResponse;
            ArrayList params = new ArrayList();
            params.add(VMName);
            if (TypeName.equals("NAT")) {
                interfaceName="";
            } else if (TypeName.equals("")) {
                TypeName="NAT";
                interfaceName="";
            }
            params.add(interfaceName);
            params.add(Mac);
            params.add(TypeName);
            returnResponse = ( Boolean )
                    this.execSyncCommand(
                    this.session.getHostAdministrationModule().getActiveCM(),
                    "VirtualizationManagerAgent",
                    "attachInterface",
                    params,
                    false);
            return returnResponse;
        }
    }
    
    
    /**
     * Lista delle VM
     * @return true on success
     */
    @ShellCommand(comment="Lista delle VM direttamente")
    public String listVMs(@ShellParameter(name="host", comment="Name of the host manager selected") String host,
    @ShellParameter(name="hypvr", comment="If this parameter is true is returned the list of all Virtual Machine registrated on Host Manager's hypervisor") boolean hypvr,
    @ShellParameter(name="onlyrunning", comment="List only runing VM")  Boolean onlyrunning) throws CleverException
    {
        ArrayList al=new ArrayList();
        String returnResponse="------Virtual Machine list for VM "+host+"--------\n";
        List response;
        al.add(host);
        al.add(onlyrunning);
        al.add(hypvr);
        response = ( List<String> )
                this.execSyncCommand(
                this.session.getHostAdministrationModule().getActiveCM(),
                "VirtualizationManagerAgent",
                "listVm",
                al,
                false);
        Iterator i=response.iterator();
        while(i.hasNext()){
            returnResponse=returnResponse+(String)i.next()+"\n";
        }
        return returnResponse;
    }
    //</editor-fold>
    
    
    
    
    //<editor-fold defaultstate="collapsed" desc="OCCI">
    /********************* Usate per OCCI : da rivedere e eventualmente, rifattorizzando, metterle per tutti ************************/
    
    /**
     *
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    public Map<String,Object> getVMDetails_HOST(String host, String name) throws CleverException
    {
        
        Map<String,Object> returnResponse;
        String method;
        
        method="getVMDetails";
        ArrayList p = new ArrayList();
        p.add(name);
        returnResponse = ( Map<String,Object> )
                this.execSyncCommand(
                host,
                "HyperVisorAgent",
                method,
                p,
                false);
        return returnResponse;
    }
    
    
    
    
    
    
    //    /**
    //     *
    //     * Da evitare: usare i metodi del CM
    //     * @return true on success
    //     */
    //     @ShellCommand(comment="Lista dei template per VM (solo OCCI)")
    //    public List<String> listTemplates_HOST(@ShellParameter(name="host", comment="HOST target") String host) throws CleverException
    //    {
    //
    //        List<String> returnResponse;
    //        String method;
    //
    //        method="listTemplates";
    //
    //        returnResponse = ( List<String> )
    //                                this.execSyncCommand(
    //                                        host,
    //                                        "HyperVisorAgent",
    //                                        method,
    //                                        this.emptyParams,
    //                                        false);
    //        return returnResponse;
    //    }
    //
    //    /**
    //     *
    //     * Da evitare: usare i metodi del CM
    //     * @return true on success
    //     */
    //     @ShellCommand(comment="Lista dei template per le immagini (solo OCCI)")
    //    public List<String> listImageTemplates_HOST(@ShellParameter(name="host", comment="HOST target") String host) throws CleverException
    //    {
    //
    //        List<String> returnResponse;
    //        String method;
    //
    //        method="listImageTemplates";
    //
    //        returnResponse = ( List<String> )
    //                                this.execSyncCommand(
    //                                        host,
    //                                        "HyperVisorAgent",
    //                                        method,
    //                                        this.emptyParams,
    //                                        false);
    //        return returnResponse;
    //    }
    //</editor-fold>
      
    //<editor-fold defaultstate="collapsed" desc="Hypervisor function">
    /**
     * Crea delle VM direttamente dndo il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    /*    public boolean createVMs_HOST(String host, Map<String, VEDescription> ves) throws CleverException
      {
      
      Boolean returnResponse;
      ArrayList params = new ArrayList();
      params.add(ves);
      returnResponse = ( Boolean )
      this.execSyncCommand(
      host,
      "HyperVisorAgent",
      "createVm",
      params,
      false);
      return returnResponse;
      }
      
     */
    /**
     * Lancia delle VMs direttamente dando il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    /*
      public boolean startVMs_HOST(String host, String[] VMNames) throws CleverException
      {
      
      Boolean returnResponse;
     ArrayList params = new ArrayList();
     params.add(VMNames);
      returnResponse = ( Boolean )
      this.execSyncCommand(
      host,
      "HyperVisorAgent",
      "startVm",
      params,
      false);
      return returnResponse;
      }
      
      
     */
    /**
     * Test se VM e' running
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    public boolean isRunningVM_HOST(String host, String VMName) throws CleverException
    {
        
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(VMName);
        returnResponse = ( Boolean )
                this.execSyncCommand(
                host,
                "HyperVisorAgent",
                "isRunning",
                params,
                false);
        return returnResponse;
    }
    /**
     * Test se VM e' running
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    @ShellCommand(comment="Lista MAC Address della VM direttamente")
    public String ListMAConVM(String host, String VMName) throws CleverException
    {
        String returnResponse="------MAC Address list for VM "+VMName+"--------\n";
        List response;
        ArrayList params = new ArrayList();
        params.add(VMName);
        response = ( ArrayList )
                this.execSyncCommand(
                host,
                "HyperVisorAgent",
                "listVM_MAC",
                params,
                false);
        Iterator i=response.iterator();
        while(i.hasNext()){
            returnResponse=returnResponse+(String)i.next()+"\n";
        }
        return returnResponse;
    }
    /**
     * Ferma una VM direttamente dando il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    /*  public boolean destroyVMs_HOST(String host, String VMNames) throws CleverException
     * {
     * 
     * Boolean returnResponse;
     * ArrayList params = new ArrayList();
     * params.add(VMNames);
     * //params.add(poweroff);
     * returnResponse = ( Boolean )
     * this.execSyncCommand(
     * host,
     * "HyperVisorAgent",
     * "destroyVm",
     * params,
     * false);
     * return returnResponse;
     * }
     * 
     */
      //TODO: manage destroy domain action based on parameter
      
     /**
     * Ferma piu' VM direttamente dando il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    /*    @ShellCommand(comment="Stop VMs directly invoking an HOST")
      public boolean stopVMs_HOST(@ShellParameter(name="host", comment="HOST target") String host,
      @ShellParameter(name="VMs", comment="An String array containing the names of VM to stop") String VMName[],
      @ShellParameter(name="poweroff", comment="Force poweroff") Boolean poweroff) throws CleverException
      {
      
      Boolean returnResponse;
      ArrayList params = new ArrayList();
      params.add(VMName);
      params.add(poweroff);
      returnResponse = ( Boolean )
      this.execSyncCommand(
      host,
      "HyperVisorAgent",
      "shutDownVm",
      params,
      false);
      return returnResponse;
      }*/
      //</editor-fold>
     
    
   /*
    public boolean attachDevice(String host, String VMName,String diskPath) throws CleverException
    {
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(VMName);
        params.add(diskPath);
        params.add(false);
        returnResponse = ( Boolean )
                this.execSyncCommand(
                host,
                "HyperVisorAgent",
                "attachDevice",
                params,
                false);
        return returnResponse;
    }
    */
    
}
