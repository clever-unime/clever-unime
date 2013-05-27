/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.common;

import java.util.ArrayList;
import java.util.List;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Shared.HostEntityInfo;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.commands.CommandCallback;

/**
 * Modulo per gestire le entita' clever a livello di hosts: HostManager e ClusterManager
 * @author maurizio
 */
public class HostAdministrationModule extends AdministrationModule{
    final ArrayList emptyParams;
    public HostAdministrationModule (Session s)
    {
        super(s);
        emptyParams = new ArrayList();
    }
    
    /**
     * Ritorna il ClusterManager attivo
     * @return ClusterManager attivo
     */
    public String getActiveCM()
    {
            return  this.
                        session.
                        getSettings().
                        getCleverCommandClientProvider().
                        getClient().
                        getConnectionXMPP().
                        getActiveCC(ConnectionXMPP.ROOM.SHELL);
    }
            
    /**
     * List degli host manager
     * @return List di Hostmanager
     * @throws CleverException 
     */
    public List<HostEntityInfo> listHostManagers() throws CleverException
    {
        String target = this.getActiveCM();
        List returnResponse;
        returnResponse = ( List<HostEntityInfo> ) 
                                this.execSyncCommand(
                                        target,
                                        "InfoAgent",
                                        "listHostManager",
                                        this.emptyParams,
                                        false);
        return returnResponse;
    }
    
    
    public void asyncListHostManagers(CommandCallback cc) throws CleverException
    {
        String target = this.getActiveCM();
        List returnResponse;
        this.execASyncCommand(
                    cc,
                    target,
                    "InfoAgent",
                    "listHostManager",
                    this.emptyParams,
                    false);

    }
    
    /**
     * List degli host manager
     * @return List di Hostmanager
     * @throws CleverException 
     */
    public List<HostEntityInfo> listClusterManagers() throws CleverException
    {
        String target = this.getActiveCM();
        List returnResponse;
        returnResponse = ( List<HostEntityInfo> ) 
                                this.execSyncCommand(
                                        target,
                                        "InfoAgent",
                                        "listClusterManager",
                                        this.emptyParams,
                                        false);
        return returnResponse;
    }
    
    
    public void asyncListClusterManagers(CommandCallback cc) throws CleverException
    {
        String target = this.getActiveCM();
        List returnResponse;
        this.execASyncCommand(
                    cc,
                    target,
                    "InfoAgent",
                    "listClusterManager",
                    this.emptyParams,
                    false);

    }
    
            
        
    
    
    
}
