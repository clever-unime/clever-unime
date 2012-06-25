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
import org.hyperic.sigar.jmx.AbstractMBean;
import org.hyperic.sigar.jmx.SigarInvokerJMX;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import org.clever.HostManager.Monitor.ResourceState;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;


public class CPUInfojmx extends AbstractMBean {

    private static final String MBEAN_TYPE = "CpuInfoList";

    private static final MBeanInfo MBEAN_INFO;

    private static final MBeanAttributeInfo MBEAN_ATTR_CPUINDEX;

    private static final MBeanAttributeInfo MBEAN_ATTR_CORE;

    private static final MBeanAttributeInfo MBEAN_ATTR_PHYCORE;

    private static final MBeanAttributeInfo MBEAN_ATTR_CORECPU;

    private static final MBeanAttributeInfo MBEAN_ATTR_Clock;

    private static final MBeanAttributeInfo MBEAN_ATTR_MODEL;

    private static final MBeanAttributeInfo MBEAN_ATTR_VENDOR;

    private static final MBeanAttributeInfo MBEAN_ATTR_FAMILY;

    private static final MBeanAttributeInfo MBEAN_ATTR_TIME;

    private static final MBeanAttributeInfo MBEAN_ATTR_MODELCPU;

    private static final MBeanAttributeInfo MBEAN_ATTR_STEP;

    private static final MBeanAttributeInfo MBEAN_ATTR_USAGE;

    private static final MBeanAttributeInfo MBEAN_ATTR_FLAGS;

    private static final MBeanAttributeInfo MBEAN_ATTR_DATAWIDTH;

    private static final MBeanConstructorInfo MBEAN_CONSTR_CPUINDEX;

    private static final MBeanConstructorInfo MBEAN_CONSTR_CPUINDEX_SIGAR;

    private static MBeanParameterInfo MBEAN_PARAM_CPUINDEX;

    private static MBeanParameterInfo MBEAN_PARAM_SIGAR;

    static {
        MBEAN_ATTR_CPUINDEX = new MBeanAttributeInfo("CpuIndex", "int",
                "The index of the CPU, typically starting at 0", true, false,
                false);
        MBEAN_ATTR_CORE = new MBeanAttributeInfo("Core", "long",
                "Total CPUs", true, false, false);
        MBEAN_ATTR_Clock = new MBeanAttributeInfo("Clock", "int",
                "The clock speed of the CPU, in [Clock]", true, false, false);
        MBEAN_ATTR_MODEL = new MBeanAttributeInfo("Model", "java.lang.String",
                "The CPU model reported", true, false, false);
        MBEAN_ATTR_VENDOR = new MBeanAttributeInfo("Vendor",
                "java.lang.String", "The CPU vendor reported", true, false,
                false);
        MBEAN_ATTR_FAMILY = new MBeanAttributeInfo("Family", "int",
                "Identification code of the processor family", true, false, false);
        MBEAN_ATTR_MODELCPU = new MBeanAttributeInfo("ModelCPU",
                "int", "Identification code of the processor model", true, false,
                false);
        MBEAN_ATTR_STEP = new MBeanAttributeInfo("Step",
                "int", "Revision number of the cpu model", true, false,
                false);
        MBEAN_ATTR_PHYCORE = new MBeanAttributeInfo("PhysicalCPU",
                "int", "Number of physical CPUs", true, false,
                false);
        MBEAN_ATTR_CORECPU = new MBeanAttributeInfo("CoreCPU",
                "int", "Number of cores per CPU", true, false,
                false);
        MBEAN_ATTR_FLAGS = new MBeanAttributeInfo("Flags", "String",
                "CPU features", true, false, false);
        MBEAN_ATTR_DATAWIDTH = new MBeanAttributeInfo("DataWidth", "int",
                "Process architecture", true,
                false, false);
        MBEAN_ATTR_USAGE = new MBeanAttributeInfo("CurrentUsage", "float",
                "Cpu current usage", true, false, false);
        MBEAN_ATTR_TIME = new MBeanAttributeInfo("LastNotification", "String",
                "Cpu current usage last notification", true, false, false);
        MBEAN_PARAM_CPUINDEX = new MBeanParameterInfo("cpuIndex", "int",
                "The index of the CPU to read data for. Must be >= 0 "
                        + "and not exceed the CPU count of the system");
        MBEAN_PARAM_SIGAR = new MBeanParameterInfo("sigar", Sigar.class
                .getName(), "The Sigar instance to use to fetch data from");
        MBEAN_CONSTR_CPUINDEX = new MBeanConstructorInfo(CPUInfojmx.class
                .getName(),
                "Creates a new instance for the CPU index specified, "
                        + "using a new Sigar instance to fetch the data. "
                        + "Fails if the CPU index is out of range.",
                new MBeanParameterInfo[] { MBEAN_PARAM_CPUINDEX });
        MBEAN_CONSTR_CPUINDEX_SIGAR = new MBeanConstructorInfo(
                CPUInfojmx.class.getName(),
                "Creates a new instance for the CPU index specified, "
                        + "using the Sigar instance specified to fetch the data. "
                        + "Fails if the CPU index is out of range.",
                new MBeanParameterInfo[] { MBEAN_PARAM_SIGAR,
                        MBEAN_PARAM_CPUINDEX });
        MBEAN_INFO = new MBeanInfo(
                CPUInfojmx.class.getName(),
                "Sigar CPU MBean. Provides raw timing data for a single "
                        + "CPU. The data is cached for 500ms, meaning each request "
                        + "(and as a result each block request to all parameters) "
                        + "within half a second is satisfied from the same dataset.",
                new MBeanAttributeInfo[] { MBEAN_ATTR_CPUINDEX,
                        MBEAN_ATTR_CORE, MBEAN_ATTR_Clock, MBEAN_ATTR_MODEL,
                        MBEAN_ATTR_VENDOR, MBEAN_ATTR_USAGE, MBEAN_ATTR_TIME,
                        MBEAN_ATTR_FAMILY, MBEAN_ATTR_MODELCPU, MBEAN_ATTR_STEP,
                        MBEAN_ATTR_FLAGS,MBEAN_ATTR_DATAWIDTH, MBEAN_ATTR_PHYCORE, MBEAN_ATTR_CORECPU},
                new MBeanConstructorInfo[] { MBEAN_CONSTR_CPUINDEX,
                        MBEAN_CONSTR_CPUINDEX_SIGAR }, null, null);

    }

    /**
     * Index of the CPUInfoLinuxjmx processed by the instance.
     */
    private final int cpuIndex;

    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */
    private final String objectName;

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
    private float currentusage = 0;
    private String time = null;


    public CPUInfojmx(int cpuIndex) throws IllegalArgumentException, SigarException, Exception {
        this(new Sigar(), cpuIndex);
    }


    public CPUInfojmx(Sigar sigar, int cpuIndex) throws IllegalArgumentException, SigarException, InstantiationException, IllegalAccessException, ClassNotFoundException{
        super(sigar, CACHED_500MS);

        // check index
        if (cpuIndex < 0)
            throw new IllegalArgumentException(
                    "CPU index has to be non-negative: " + cpuIndex);
        try {
            int cpuCount;
            if ((cpuCount = sigar.getCpuList().length) < cpuIndex)
                throw new IllegalArgumentException(
                        "CPU index out of range (found " + cpuCount
                                + " CPU(s)): " + cpuIndex);

        } catch (SigarException e) {
            throw unexpectedError(MBEAN_TYPE, e);
        }


        // all fine
        this.cpuIndex = cpuIndex;
        this.objectName = SigarInvokerJMX.DOMAIN_NAME + ":" + MBEAN_ATTR_TYPE
                + "=Cpu,"
                + MBEAN_ATTR_CPUINDEX.getName().substring(0, 1).toLowerCase()
                + MBEAN_ATTR_CPUINDEX.getName().substring(1) + "=" + cpuIndex;


        SigarMonitor res = new SigarMonitor();
        List cpuinfo=res.getCPUInfo();
        List cpustate=res.getCPUState();

        //for(int i=0;i<cpuinfo.size();i++){

            deviceId = this.cpuIndex;
            vendorID = ((CPUInfo)cpuinfo.get(this.cpuIndex)).getVendorID();
            modelName =((CPUInfo)cpuinfo.get(this.cpuIndex)).getModelName();
            dataWidth = ((CPUInfo)cpuinfo.get(this.cpuIndex)).getDataWidth();
            CPUCores = ((CPUInfo)cpuinfo.get(this.cpuIndex)).getCPUCores();
            CPUFamily = ((CPUInfo)cpuinfo.get(this.cpuIndex)).getCPUFamily();
            CPUModel = ((CPUInfo)cpuinfo.get(this.cpuIndex)).getCPUModel();
            step = ((CPUInfo)cpuinfo.get(this.cpuIndex)).getStep();
            CPUFlag = ((CPUInfo)cpuinfo.get(this.cpuIndex)).getCPUFlag();
            TotalSocket = ((CPUInfo)cpuinfo.get(this.cpuIndex)).getTotalSockets();
            TotalCores = ((CPUInfo)cpuinfo.get(this.cpuIndex)).getTotalCores();
            Clock=((CPUInfo)cpuinfo.get(this.cpuIndex)).getClock();
        //}

        //for(int i=0;i<cpustate.size();i++){
            currentusage=((ResourceState)cpustate.get(this.cpuIndex)).getCurrentUsage();
            time=((ResourceState)cpustate.get(this.cpuIndex)).getLastNotification();
        //}
    }

    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */
    public String getObjectName() {
        return this.objectName;
    }

/*    public int getDeviceId() {
        deviceId = this.cpuIndex;
        return deviceId;
    }
*/
    // -------
    // Implementation of the DynamicMBean interface
    // -------


    public Object getAttribute(String attr) throws AttributeNotFoundException {

        if (MBEAN_ATTR_CPUINDEX.getName().equals(attr)) {
            return deviceId;

        } else if (MBEAN_ATTR_CORE.getName().equals(attr)) {
            return CPUCores;

        } else if (MBEAN_ATTR_Clock.getName().equals(attr)) {
            return Clock;

        } else if (MBEAN_ATTR_MODEL.getName().equals(attr)) {
            return modelName;

        } else if (MBEAN_ATTR_PHYCORE.getName().equals(attr)) {
            return TotalSocket;

        } else if (MBEAN_ATTR_CORECPU.getName().equals(attr)) {
            return TotalCores;

        } else if (MBEAN_ATTR_VENDOR.getName().equals(attr)) {
            return vendorID;

        } else if (MBEAN_ATTR_FAMILY.getName().equals(attr)) {
            return CPUFamily;

        } else if (MBEAN_ATTR_MODELCPU.getName().equals(attr)) {
            return CPUModel;

        } else if (MBEAN_ATTR_STEP.getName().equals(attr)) {
            return step;

        } else if (MBEAN_ATTR_FLAGS.getName().equals(attr)) {
            return CPUFlag;

        } else if (MBEAN_ATTR_DATAWIDTH.getName().equals(attr)) {
            return dataWidth;

        } else if (MBEAN_ATTR_USAGE.getName().equals(attr)) {
            return currentusage;

        } else if (MBEAN_ATTR_TIME.getName().equals(attr)) {
            return time;

        } else {
            throw new AttributeNotFoundException(attr);
        }
    }


    public void setAttribute(Attribute attr) throws AttributeNotFoundException {
        throw new AttributeNotFoundException(attr.getName());
    }

    public Object invoke(String actionName, Object[] params, String[] signature)
            throws ReflectionException {
        throw new ReflectionException(new NoSuchMethodException(actionName),
                actionName);
    }


    public MBeanInfo getMBeanInfo() {
        return MBEAN_INFO;
    }
}