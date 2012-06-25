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

import java.util.List;
import org.clever.HostManager.Monitor.MemoryInfo;
import org.clever.HostManager.Monitor.OSInfo;
import org.clever.HostManager.Monitor.ResourceState;

public abstract class HWMonitor{

        public String osType = getOsType();

        public abstract List getRawCPUInfo();
        public abstract List getRawCPUState();
        public abstract MemoryInfo getRawMemoryInfo();
        public abstract List getRawStorageInfo();
        public abstract List getRawProcessInfo();
        public abstract OSInfo getRawOsInfo();
        public abstract String getOsType();
        public abstract void getNetworkInfo(boolean netflag);
        public abstract ResourceState getRawMemoryCacheState();
        public abstract ResourceState getRawMemoryBufferState();
        public abstract ResourceState getRawMemoryCurrentFree();
        public abstract ResourceState getRawMemoryCurrentSwapFree();
        public abstract ResourceState getRawMemoryUsed();
        public abstract ResourceState getRawMemoryUsedSwap();
        public abstract List getRawStorageCurrentFreeSpace();
        public abstract List getRawStorageUsed();
        public abstract List getRawStorageUsedpc();
        public abstract double getRawUpTime();

}
