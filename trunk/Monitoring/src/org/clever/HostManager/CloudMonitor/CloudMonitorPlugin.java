/*
 *  The MIT License
 * 
 *  Copyright (c) 2013 Nicola Peditto
 *  Copyright (c) 2013 Carmelo Romeo
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
package org.clever.HostManager.CloudMonitor;

import org.clever.Common.Communicator.Agent;
import org.clever.Common.Plugins.RunnerPlugin;

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
