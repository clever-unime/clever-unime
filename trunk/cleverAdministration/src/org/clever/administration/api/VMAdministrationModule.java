/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.VEInfo.VEState;


/**
 * Modulo per gestire le VM
 * @author maurizio
 */
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
    public boolean stopVM_HOST(String host, String VMName, Boolean poweroff) throws CleverException
    {
        
        Boolean returnResponse;
        ArrayList params = new ArrayList();
        params.add(VMName);
        //params.add(poweroff);
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
    public boolean stopVMs_HOST(String host, String VMName[], Boolean poweroff) throws CleverException
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
    public List<VEState> listVMs_HOST(String host, Boolean onlyrunning) throws CleverException
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
    
    
            
        
    
    
    
}
