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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.SigarCommandBase;


public class CPUInfoLinux extends CPUInfo{//extends SigarCommandBase {


    /**
     * Index of the CPUInfo processed by the instance.
     */
    private int cpuIndex;

    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */


    private String[] command = {"cat","/proc/cpuinfo"};
    private ProcessBuilder pb = new ProcessBuilder(command);

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
   

    public CPUInfoLinux()  {
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


                if(getCPUCores()!=sigar.getCpuInfoList()[this.cpuIndex].getTotalSockets())
                        TotalSocket = sigar.getCpuInfoList()[this.cpuIndex].getTotalSockets();
                else
                        TotalSocket = 1;

                if(getCPUCores()!=sigar.getCpuInfoList()[this.cpuIndex].getTotalSockets())
                        TotalCores = sigar.getCpuInfoList()[this.cpuIndex].getCoresPerSocket();
                else
                        TotalCores = 1;

                Clock = sigar.getCpuInfoList()[this.cpuIndex].getMhz();


                try {
                    Process p = pb.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = br.readLine();
                    while (line != null) {
                        if (line.startsWith("cpu family")) {
                           CPUFamily = (line.substring(line.indexOf("cpu family\t: ") + 13).trim());
                        } else
                        if ((line.startsWith("model"))&&(line.indexOf("model name")==-1)) {
                            CPUModel= (line.substring(line.indexOf("model\t\t: ") + 9).trim());
                        } else
                        if (line.startsWith("stepping")) {
                            step = (line.substring(line.indexOf("stepping\t\t: ") + 12).trim());
                        } else
                        if (line.startsWith("flags")) {
                            CPUFlag=line.substring(line.indexOf("flags\t\t: ") + 9).trim();
                        }
                        line = br.readLine();
                    }

                }catch (IOException e) {
                    e.printStackTrace();
                }


                if((CPUFlag.indexOf("rm")!=-1))dataWidth=16;
                if((CPUFlag.indexOf("tm")!=-1))dataWidth=32;
                if((CPUFlag.indexOf("lm")!=-1))dataWidth=64;


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