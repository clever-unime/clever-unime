/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
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

package org.clever.HostManager.MonitorPlugins.Sigar;

import org.clever.HostManager.Monitor.MonitorPlugin;
import java.util.List;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.HostManager.Monitor.MemoryInfo;
import org.clever.HostManager.Monitor.OSInfo;
import org.clever.HostManager.Monitor.ResourceState;


public class SigarMonitor implements MonitorPlugin{

    private HWMonitor hwMonitor = null;
    ModuleCommunicator mc;
    private Agent owner;


    //public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    public void init() {
        HwMonitorFactory HwMonitorFactory = new HwMonitorFactory();
        this.setHwMonitor(HwMonitorFactory.getHwMonitor());
    }

    private void setHwMonitor(HWMonitor hwMonitor) {
        this.hwMonitor =  hwMonitor;
    }

    public HWMonitor getHwMonitor() {
        return this.hwMonitor;
    }

    public List getCPUInfo() {
        return this.hwMonitor.getRawCPUInfo();
    }

    public List getCPUState() {
        return this.hwMonitor.getRawCPUState();
    }

    public MemoryInfo getMemoryInfo() {
        return this.hwMonitor.getRawMemoryInfo();
    }

    public List getStorageInfo() {
        return this.hwMonitor.getRawStorageInfo();
    }

    public List getProcessInfo() {
        return this.hwMonitor.getRawProcessInfo();
    }

    public OSInfo getOsInfo() {
        return this.hwMonitor.getRawOsInfo();
    }

    public void getNetworkInfo(boolean netflag){
        this.hwMonitor.getNetworkInfo(netflag);
    }

    public ResourceState getMemoryCacheState() {
        return this.hwMonitor.getRawMemoryCacheState();
    }

    public ResourceState getMemoryBufferState() {
        return this.hwMonitor.getRawMemoryBufferState();
    }

    public ResourceState getMemoryCurrentFree() {
        return this.hwMonitor.getRawMemoryCurrentFree();
    }

    public ResourceState getMemoryCurrentSwapFree() {
        return this.hwMonitor.getRawMemoryCurrentSwapFree();
    }

    public ResourceState getMemoryUsed() {
        return this.hwMonitor.getRawMemoryUsed();
    }

    public ResourceState getMemoryUsedSwap() {
        return this.hwMonitor.getRawMemoryUsedSwap();
    }

    public List getStorageCurrentFreeSpace() {
        return this.hwMonitor.getRawStorageCurrentFreeSpace();
    }

    public List getStorageUsed() {
        return this.hwMonitor.getRawStorageUsed();
    }

    public List getStorageUsedpc() {
        return this.hwMonitor.getRawStorageUsedpc();
    }

    public double getUpTime() {
        return this.hwMonitor.getRawUpTime();
    }

    public String getName() {
            String name = "Monitor";
            return name;
    }

    public String getVersion() {
            String version = "1.0";
            return version;
    }



  @Override
  public String getDescription()
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

    @Override
    public void setOwner(Agent owner) {
        this.owner=owner;
    }


}
