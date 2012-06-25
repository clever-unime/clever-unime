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


import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import org.clever.HostManager.Monitor.MemoryInfo;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.jmx.AbstractMBean;
import org.hyperic.sigar.jmx.SigarInvokerJMX;

public class MemoryInfojmx extends AbstractMBean {

    private static final MBeanInfo MBEAN_INFO;

    private static final MBeanAttributeInfo MBEAN_ATTR_ACTUAL_FREE;

    private static final MBeanAttributeInfo MBEAN_ATTR_ACTUAL_USED;

    private static final MBeanAttributeInfo MBEAN_ATTR_FREE;

    private static final MBeanAttributeInfo MBEAN_ATTR_RAM;

    private static final MBeanAttributeInfo MBEAN_ATTR_TOTAL;

    private static final MBeanAttributeInfo MBEAN_ATTR_USED;

    private static final MBeanAttributeInfo MBEAN_ATTR_SWAP;

    private static final MBeanAttributeInfo MBEAN_ATTR_SWAPFREE;

    private static final MBeanAttributeInfo MBEAN_ATTR_SWAPUSED;

    private static final MBeanConstructorInfo MBEAN_CONSTR_SIGAR;

    private static MBeanParameterInfo MBEAN_PARAM_SIGAR;


    private float total = 0;
    private float currentFree;
    private float swap = 0;
    private float currentSwapFree;
    private float cached;
    private float buffer;
    private float used;
    private float usedswap;
    private float ram = 0;


    static {
        MBEAN_ATTR_ACTUAL_FREE = new MBeanAttributeInfo("Cache", "long",
                "Cache dimension", true, false, false);
        MBEAN_ATTR_ACTUAL_USED = new MBeanAttributeInfo("Buffer", "long",
                "Buffer dimension", true, false, false);
        MBEAN_ATTR_FREE = new MBeanAttributeInfo("FreeMemory", "long",
                "Free memory", true, false, false);
        MBEAN_ATTR_TOTAL = new MBeanAttributeInfo("TotalMemory", "long",
                "Total memory", true, false, false);
        MBEAN_ATTR_USED = new MBeanAttributeInfo("UsedMemory", "long",
                "Used memory", true, false, false);
        MBEAN_ATTR_RAM = new MBeanAttributeInfo("Ram", "long",
                "Ram dimension", true, false, false);
        MBEAN_ATTR_SWAP = new MBeanAttributeInfo("TotalSwap", "long",
                "Total swap dimension", true, false, false);
        MBEAN_ATTR_SWAPFREE = new MBeanAttributeInfo("FreeSwap", "long",
                "Free swap dimension", true, false, false);
        MBEAN_ATTR_SWAPUSED = new MBeanAttributeInfo("UsedSwap", "long",
                "Used swap dimension", true, false, false);
        MBEAN_PARAM_SIGAR = new MBeanParameterInfo("sigar", Sigar.class
                .getName(), "The Sigar instance to use to fetch data from");
        MBEAN_CONSTR_SIGAR = new MBeanConstructorInfo(MemoryInfojmx.class.getName(),
                "Creates a new instance, using the Sigar instance "
                        + "specified to fetch the data.",
                new MBeanParameterInfo[] { MBEAN_PARAM_SIGAR });
        MBEAN_INFO = new MBeanInfo(
                MemoryInfojmx.class.getName(),
                "Sigar Memory MBean, provides raw data for the physical "
                        + "memory, the swap memory, the buffer and cache memory and"
                        + " the ram dimension installed on the system. ",
                new MBeanAttributeInfo[] { MBEAN_ATTR_ACTUAL_FREE,
                        MBEAN_ATTR_ACTUAL_USED, MBEAN_ATTR_RAM, 
                        MBEAN_ATTR_FREE, MBEAN_ATTR_TOTAL, MBEAN_ATTR_USED,
                        MBEAN_ATTR_SWAP, MBEAN_ATTR_SWAPFREE, MBEAN_ATTR_SWAPUSED},
                new MBeanConstructorInfo[] { MBEAN_CONSTR_SIGAR }, null, null);

    }

    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */
    private final String objectName;


    public MemoryInfojmx(int cpuIndex) throws IllegalArgumentException, SigarException, Exception {
            this(new Sigar());
    }


    public MemoryInfojmx(Sigar sigar) throws IllegalArgumentException, SigarException, Exception {
        super(sigar, CACHED_500MS);

        SigarMonitor res = new SigarMonitor();
        MemoryInfo mem = res.getMemoryInfo();


        total =mem.getTotal();
        currentFree = res.getMemoryCurrentFree().getCurrentUsage();
        swap = mem.getSwap();
        currentSwapFree = res.getMemoryCurrentSwapFree().getCurrentUsage();
        cached = res.getMemoryCacheState().getCurrentUsage();
        buffer = res.getMemoryBufferState().getCurrentUsage();
        used = res.getMemoryUsed().getCurrentUsage();
        usedswap = res.getMemoryUsedSwap().getCurrentUsage();
        ram = mem.getRam();

        this.objectName = SigarInvokerJMX.DOMAIN_NAME + ":" + MBEAN_ATTR_TYPE
                + "=Memory";


    }

    public String getObjectName() {
        return this.objectName;
    }



    public Object getAttribute(String attr) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (MBEAN_ATTR_ACTUAL_FREE.getName().equals(attr)) {
            return cached;

        } else if (MBEAN_ATTR_ACTUAL_USED.getName().equals(attr)) {
          return buffer;

        } else if (MBEAN_ATTR_FREE.getName().equals(attr)) {
            return currentFree;

        }   else if (MBEAN_ATTR_TOTAL.getName().equals(attr)) {
            return total;

        } else if (MBEAN_ATTR_USED.getName().equals(attr)) {
            return used;

        }  else if (MBEAN_ATTR_RAM.getName().equals(attr)) {
            return ram;

        } else if (MBEAN_ATTR_SWAPFREE.getName().equals(attr)) {
            return currentSwapFree;

        } else if (MBEAN_ATTR_SWAP.getName().equals(attr)) {
            return swap;

        } else if (MBEAN_ATTR_SWAPUSED.getName().equals(attr)) {
            return usedswap;

        }else {
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
