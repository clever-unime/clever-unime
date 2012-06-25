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

//import MonitorPlugin.SigarCommandBase;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.SigarCommandBase;


public class CPUInfoWin extends CPUInfo{//extends SigarCommandBase {


    /**
     * Index of the CPUInfo processed by the instance.
     */
    private int cpuIndex;

    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */


    private Map<String, String> env = System.getenv();
    private String pid = env.get("PROCESSOR_IDENTIFIER");

    private int deviceId;
    private int TotalSocket = 0;
    private int TotalCores = 0;
    private float Clock = 0;
    private String vendorID = null;
    private String CPUFamily = null;
    private String CPUModel = null;
    private String modelName = null;
    private String step = null;
    private int dataWidth = 0;
    private String CPUFlag = null;
    private int CPUCores = 0;

    private CpuInfo[] cpu;


    public CPUInfoWin()  {
        super();
        try {
             cpu = sigar.getCpuInfoList();


            for (int cpunum=0; cpunum<cpu.length; cpunum++){

                this.cpuIndex = cpunum;

                deviceId = getDeviceId();
                vendorID = sigar.getCpuInfoList()[this.cpuIndex].getVendor();
                modelName =sigar.getCpuInfoList()[this.cpuIndex].getModel();
                dataWidth = 0;
                CPUCores = sigar.getCpuInfoList()[this.cpuIndex].getTotalCores();
                CPUFamily = (pid.substring(pid.indexOf("Family ") + 7, pid.indexOf(" Model")).trim());
                CPUModel=(pid.substring(pid.indexOf("Model ") + 6, pid.indexOf(" Stepping")).trim());
                step=(pid.substring(pid.indexOf("Stepping ") + 9, pid.indexOf(",")).trim());
                CPUFlag="Unknown";

                if(getCPUCores()!=sigar.getCpuInfoList()[this.cpuIndex].getTotalSockets())
                        TotalSocket = sigar.getCpuInfoList()[this.cpuIndex].getTotalSockets();
                else
                        TotalSocket = 1;

                if(getCPUCores()!=sigar.getCpuInfoList()[this.cpuIndex].getTotalSockets())
                        TotalCores = sigar.getCpuInfoList()[this.cpuIndex].getCoresPerSocket();
                else
                        TotalCores = 1;

                Clock = sigar.getCpuInfoList()[this.cpuIndex].getMhz();




            }
        } catch (Exception ex) {
            Logger.getLogger(CPUInfoLinux.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  

    /**
     * @return The index of the CPUInfo, typically starting at 0
     */
    public int getDeviceId() {
        return this.cpuIndex;
    }

    public int getCPUCores() {
            return CPUCores;
    }
    public int getTotalSockets() {

            return TotalCores;
    }

    public int getTotalCores() {

            return TotalSocket;

    }

    /**
     * @return The clock speed of the CPUInfo, in [Clock]
     */
    public float getClock() {

            return Clock;
    }

    /**
     * @return The CPUInfo model reported
     */
    public String getModelName() {
            return modelName;
    }

    /**
     * @return The CPUInfo vendor reported
     */
    public String getVendorID() {
            return vendorID;
    }


     public String getCPUFamily() {

            return CPUFamily;

    }

    public String getCPUModel(){

            return CPUModel;
    }

    public String getStep(){

            return step;
    }

    public String getCPUFlag(){

            return CPUFlag;
     }

    public int getDataWidth(){

            return dataWidth;
     }

    @Override
    public void output(String[] strings) throws SigarException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}