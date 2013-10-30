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
    
    public void getInformationsAboutMemory();
    public String getTotalUsedMemory();
    
    public String getCpuIdle();
    
    
}
