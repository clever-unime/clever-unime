/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.CloudMonitor;

import org.clever.Common.Communicator.Agent;
import org.clever.Common.Plugins.RunnerPlugin;

/**
 *
 * @author webwolf
 */
public interface CloudMonitorPlugin extends RunnerPlugin{
    

    public void setOwner(Agent owner);
    
    public String handShaking();
    
    //public void getInformationsAboutMemory();
    public String getTotalUsedMemory();
    public String getTotalMemory();
    public String getTotalFreeMemory();
    
    public String getCpuIdle();
    public String getCpuSys();
    public String getCpuUser();
    
    
    public String getInterfaceRX();
    public String getInterfaceTX();
    public String getInterfacePktRX();
    public String getInterfacePktTX();
    
    
    public String getTotalStorage();
    public String getAvailStorage();
    public String getUsedStorage();
    public String getUsedPercentStorage();
    public String getReadBytesStorage();
    public String getWriteBytesStorage();
    
    public String getProcStatus(String procname);
    public String getStorageStatus();    
    
}
