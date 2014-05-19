/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api.modules;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.clever.administration.api.modules.AdministrationModule;
import java.util.List;
import org.clever.Common.Communicator.InvocationCallback;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Shared.HostEntityInfo;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.annotations.HasScripts;
import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.annotations.ShellParameter;
import org.clever.administration.api.Session;



/**
 * Modulo per gestire le entita' clever a livello di hosts: HostManager e ClusterManager
 * @author maurizio
 */
@HasScripts(value="HAM", script="scripts/ham.bsh", comment="Administration module for Clever Host")
public class HostAdministrationModule extends AdministrationModule{
    org.apache.log4j.Logger logger=org.apache.log4j.Logger.getLogger("HostAdministrationModule");
    public HostAdministrationModule (Session s)
    {
        super(s);
       
    }
    
    /**
     * Ritorna il ClusterManager attivo
     * @return active ClusterManager 
     */
    @ShellCommand
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
     @ShellCommand
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
    
    
    public void asyncListHostManagers(InvocationCallback cc) throws CleverException
    {
        String target = this.getActiveCM();
      
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
     @ShellCommand
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
    
    
    public void asyncListClusterManagers(InvocationCallback cc) throws CleverException
    {
        String target = this.getActiveCM();
        
        this.execASyncCommand(
                    cc,
                    target,
                    "InfoAgent",
                    "listClusterManager",
                    this.emptyParams,
                    false);

    }
    
     /**
     * List degli host manager
     * @return List di Hostmanager
     * @throws CleverException 
     */
    @ShellCommand(comment="List agents on a clever host (CM or HM)")
    public List<String> listActiveAgents(@ShellParameter(name="target", comment="the target host ") String target) throws CleverException
    {
        
        List returnResponse;
        returnResponse = ( List<String> ) 
                                this.execSyncCommand(
                                        target,
                                        "InfoAgent",
                                        "listActiveAgents",
                                        this.emptyParams,
                                        false);
        return returnResponse;
    }   
    
    /**
     * Ritorna il nome del plugin di un particolare agente
     * @param target
     * @param agent
     * @return
     * @throws CleverException 
     */
     @ShellCommand(comment="Retrieve the name of agent plugin")
    public String getPluginName(String target, String agent) throws CleverException
    {
        
        String returnResponse;
        returnResponse = ( String ) 
                                this.execSyncCommand(
                                        target,
                                        agent,
                                        "getName",
                                        this.emptyParams,
                                        false);
        return returnResponse;
    } 
    
    /**
     * Ritorna la descrizione del plugin di un particolare agente
     * @param target
     * @param agent
     * @return
     * @throws CleverException 
     */
    public String getPluginDescription(String target, String agent) throws CleverException
    {
        
        String returnResponse;
        returnResponse = ( String ) 
                                this.execSyncCommand(
                                        target,
                                        agent,
                                        "getDescription",
                                        this.emptyParams,
                                        false);
        return returnResponse;
    } 
    /**
     * Ritorna la versione del plugin di un particolare agente
     * @param target
     * @param agent
     * @return
     * @throws CleverException 
     */
    public String getPluginVersion(String target, String agent) throws CleverException
    {
        
        String returnResponse;
        returnResponse = ( String ) 
                                this.execSyncCommand(
                                        target,
                                        agent,
                                        "getVersion",
                                        this.emptyParams,
                                        false);
        return returnResponse;
    } 
    @ShellCommand
    public String restartAgent(@ShellParameter(name="target", comment="Cloud Entity target(HM/CM)") String target,
                                @ShellParameter(name="agent", comment="Agent selected for restart") String agent){
        String result="";
        try{
        result=(String)this.execSyncCommand(
                                        target,
                                        agent,
                                        "restartAgent",
                                        this.emptyParams,
                                        false);
        }
        catch(Exception e){
            result=" Error occurred in restart Agent function! the agent:"+agent+"hasn't restarted!";
            
        }
        return result;
    }
     @ShellCommand
     public String restartPlugin(@ShellParameter(name="target", comment="Cloud Entity target(HM/CM)") String target,
                                @ShellParameter(name="agent", comment="Agent selected for restart") String agent,
                                @ShellParameter(name="configurationFile", comment="Configuration File for the plugin") String configurationFile,
                                @ShellParameter(name="write", comment="If is true configuration file will be write on config folder ") boolean write){
        String result="";
        ArrayList al=new ArrayList();
        BufferedInputStream f = null;
        
        try {
            
            byte[] buffer = new byte[(int) new File(configurationFile).length()];
            f = new BufferedInputStream(new FileInputStream(configurationFile));
            f.read(buffer);
            String cfgfile=new String(buffer);


            al.add(cfgfile);
            al.add(write);
            try{
            result=(String)this.execSyncCommand(
                                            target,
                                            agent,
                                            "restartPlugin",
                                            this.emptyParams,
                                            false);
            }
            catch(Exception e){
                result=" Error occurred in restart Agent function! the agent:"+agent+"hasn't restarted!";

            }
        } catch (IOException ex) {
            result="Error is occurred in IO operation generated from Restart Plugin Function!";
            logger.error("Error is occurred in IO operation generated from Restart Plugin Function!",ex);
        } finally {
            try {
                f.close();
            } catch (IOException ex) {
                result=result+"Error is occurred in IO operation, it's generated from close operation of configuration file!";
                logger.error("Error is occurred in IO operation , it's generated from close operation of configuration file!",ex);
            }
        }
        return result;
    }   
    
    

    
    
    
}
