/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.VEInfo.VEState;
import org.clever.administration.annotations.HasScripts;
import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.annotations.ShellParameter;
import org.clever.administration.api.Session;


/**
 * Modulo per gestire le VM
 * @author maurizio
 */
@HasScripts(value="VMM", script="scripts/vmm.bsh", comment="Administration module for VMs")
public class VMAdministrationModule extends AdministrationModule{
    
    public VMAdministrationModule (Session s)
    {
        super(s);
       
    }
    
    
    
    /**
     * Crea una VM direttamente dndo il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    public boolean createVM_HOST(String host, String veId, VEDescription ved, Boolean notExclusive) throws CleverException
    {
        
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(veId);
        params.add(ved);
        params.add(notExclusive);
        returnResponse = ( Boolean ) 
                                this.execSyncCommand(
                                        host,
                                        "HyperVisorAgent",
                                        "createVm",
                                        params,
                                        false);
        return returnResponse;
    }
    
    
    /**
     * Crea delle VM direttamente dndo il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    public boolean createVMs_HOST(String host, Map<String, VEDescription> ves) throws CleverException
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
     * Lancia una VM direttamente dndo il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    public boolean startVM_HOST(String host, String VMName) throws CleverException
    {
        
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(VMName);
        returnResponse = ( Boolean ) 
                                this.execSyncCommand(
                                        host,
                                        "HyperVisorAgent",
                                        "startVm",
                                        params,
                                        false);
        return returnResponse;
    }
    
    /**
     * Lancia delle VMs direttamente dando il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
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
    
     
     /**
     * Ferma una VM direttamente dando il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    @ShellCommand(comment="Stop VM directly invoking an HOST")
    public boolean stopVM_HOST(@ShellParameter(name="host", comment="HOST target") String host,
                               @ShellParameter(name="VMName", comment="VM name") String VMName, 
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
    }
    
    
    /**
     * Ferma una VM direttamente dando il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    public boolean destroyVM_HOST(String host, String VMName) throws CleverException
    {
        
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(VMName);
        //params.add(poweroff);
        returnResponse = ( Boolean ) 
                                this.execSyncCommand(
                                        host,
                                        "HyperVisorAgent",
                                        "destroyVm",
                                        params,
                                        false);
        return returnResponse;
    }
    
    /**
     * Ferma una VM direttamente dando il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    public boolean destroyVMs_HOST(String host, String VMNames) throws CleverException
    {
        
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(VMNames);
        //params.add(poweroff);
        returnResponse = ( Boolean ) 
                                this.execSyncCommand(
                                        host,
                                        "HyperVisorAgent",
                                        "destroyVm",
                                        params,
                                        false);
        return returnResponse;
    }
    
    
    //TODO: manage destroy domain action based on parameter
    
    /**
     * Ferma piu' VM direttamente dando il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
     @ShellCommand(comment="Stop VMs directly invoking an HOST")
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
    }
    
    /**
     * Lista delle VM direttamente dando il comando ad un HOST
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    @ShellCommand(comment="Lista delle VM direttamente dando il comando ad un HOST")
    public List<VEState> listVMs_HOST(@ShellParameter(name="host", comment="HOST target") String host,
                                      @ShellParameter(name="onlyrunning", comment="List only runing VM")  Boolean onlyrunning) throws CleverException
    {
        
        List returnResponse;
        String method;
        if(onlyrunning)
        {
            method="listRunningVms";
        }
        else
        {
            method="listVms";
        }
        returnResponse = ( List<VEState> ) 
                                this.execSyncCommand(
                                        host,
                                        "HyperVisorAgent",
                                        method,
                                        this.emptyParams,
                                        false);
        return returnResponse;
    }
    
    
    
    
    
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
    
    /**
     * 
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    public List<String> listTemplates_HOST(String host) throws CleverException
    {
        
        List<String> returnResponse;
        String method;
        
        method="listTemplates";
        
        returnResponse = ( List<String> ) 
                                this.execSyncCommand(
                                        host,
                                        "HyperVisorAgent",
                                        method,
                                        this.emptyParams,
                                        false);
        return returnResponse;
    }
    
    /**
     * 
     * Da evitare: usare i metodi del CM
     * @return true on success
     */
    public List<String> listImageTemplates_HOST(String host) throws CleverException
    {
        
        List<String> returnResponse;
        String method;
        
        method="listImageTemplates";
        
        returnResponse = ( List<String> ) 
                                this.execSyncCommand(
                                        host,
                                        "HyperVisorAgent",
                                        method,
                                        this.emptyParams,
                                        false);
        return returnResponse;
    }
    
            
        
    
    
    
}
