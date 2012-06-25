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

import org.clever.HostManager.MonitorPlugins.Sigar.CPUInfojmx;
import org.clever.HostManager.MonitorPlugins.Sigar.MemoryInfojmx;
import org.clever.HostManager.MonitorPlugins.Sigar.NetworkInfojmx;
import org.clever.HostManager.MonitorPlugins.Sigar.OSInfojmx;
import org.clever.HostManager.MonitorPlugins.Sigar.ProcessInfojmx;
import org.clever.HostManager.MonitorPlugins.Sigar.StorageInfojmx;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.jmx.AbstractMBean;
import org.hyperic.sigar.jmx.SigarInvokerJMX;

public class JmxRegister extends AbstractMBean{

    private MemoryInfojmx mem;
    private OSInfojmx os;
    private StorageInfojmx stor;
    private CPUInfojmx cpu;
    private ProcessInfojmx proc;
    private NetworkInfojmx net;

    private final String osType = System.getProperty("os.name");

    private final String objectName;
    private final ArrayList managedBeans;

    private static final String MBEAN_TYPE = "MonitorPlugin";

    private static final MBeanInfo MBEAN_INFO;


    static {
                MBEAN_INFO = new MBeanInfo(
                        JmxRegister.class.getName(),
                        "Sigar MBean registry. Provides a central point for creation "
                                + "and destruction of Sigar MBeans. Any Sigar MBean created via "
                                + "this instance will automatically be cleaned up when this "
                                + "instance is deregistered from the MBean server.",
                                null, null, null, null );
    }

    public void getCPUInfo() {

        ObjectInstance nextRegistered = null;


        try {

            final int cpuCount = sigar.getCpuInfoList().length;

            for (int i = 0; i < cpuCount; i++) {
                try {
                    // add CPUInfojmx bean
                    cpu = new CPUInfojmx(sigarImpl, i);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    if (!mbeanServer.isRegistered(new ObjectName(cpu
                            .getObjectName())))
                        nextRegistered = mbeanServer.registerMBean(cpu,
                                null);
                } catch (Exception e) { // ignore
                }
                // add MBean to set of managed beans
                if (nextRegistered != null)
                    managedBeans.add(nextRegistered.getObjectName());
                nextRegistered = null;

            }

        } catch (SigarException e) {
            throw unexpectedError("CpuInfoList", e);
        }

    }

    public void getMemoryInfo() throws IllegalArgumentException, SigarException  {

        ObjectInstance nextRegistered = null;
        try {
            // add physical memory bean
            mem = new MemoryInfojmx(sigarImpl);
        } catch (Exception ex) {
            Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            if (!mbeanServer.isRegistered(new ObjectName(mem.getObjectName())))
                nextRegistered = mbeanServer.registerMBean(mem, null);
        } catch (Exception e) {}

        // add MBean to set of managed beans
        if (nextRegistered != null)
            managedBeans.add(nextRegistered.getObjectName());
        nextRegistered = null;


    }


    public void getStorageInfo() {

        ObjectInstance nextRegistered = null;

        try {
            final int storageCount = sigar.getFileSystemList().length;
            for (int i = 0; i < storageCount; i++) {
                try {
                    // add storage bean
                    stor = new StorageInfojmx(sigarImpl, i);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    if (!mbeanServer.isRegistered(new ObjectName(stor
                            .getObjectName())))
                        nextRegistered = mbeanServer.registerMBean(stor,
                                null);
                } catch (Exception e) { // ignore
                }
                // add MBean to set of managed beans
                if (nextRegistered != null)
                    managedBeans.add(nextRegistered.getObjectName());
                nextRegistered = null;

            }

        } catch (SigarException e) {
            throw unexpectedError("StorageInfoList", e);
        }

    }

    public void getProcessInfo() throws IllegalArgumentException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        
        ObjectInstance nextRegistered = null;

        try {
            final int procCount = sigar.getProcList().length;
            for (int i = 0; i < procCount; i++) {
                // add Process bean
                proc = new ProcessInfojmx(sigarImpl);
                try {
                    if (!mbeanServer.isRegistered(new ObjectName(proc
                            .getObjectName())))
                        nextRegistered = mbeanServer.registerMBean(proc,
                                null);
                } catch (Exception e) { // ignore
                }
                // add MBean to set of managed beans
                if (nextRegistered != null)
                    managedBeans.add(nextRegistered.getObjectName());
                nextRegistered = null;

        }
        } catch (SigarException e) {
            throw unexpectedError("ProcessInfoList", e);
        }

    }


    public void getNetworkInfo() throws IllegalArgumentException, SigarException  {

        ObjectInstance nextRegistered = null;
        try {
            // add physical network bean
            net = new NetworkInfojmx(sigarImpl);
        } catch (Exception ex) {
            Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            if (!mbeanServer.isRegistered(new ObjectName(net.getObjectName())))
                nextRegistered = mbeanServer.registerMBean(net, null);
        } catch (Exception e) {}

        // add MBean to set of managed beans
        if (nextRegistered != null)
            managedBeans.add(nextRegistered.getObjectName());
        nextRegistered = null;

    }

    public void getOsInfo() throws IllegalArgumentException, InstantiationException, IllegalAccessException, ClassNotFoundException {

        ObjectInstance nextRegistered = null;

        // add os memory bean
        os = new OSInfojmx(sigarImpl);

        try {
            if (!mbeanServer.isRegistered(new ObjectName(os.getObjectName())))
                nextRegistered = mbeanServer.registerMBean(os, null);
        } catch (Exception e) { // ignore
        }

        // add MBean to set of managed beans
        if (nextRegistered != null)
            managedBeans.add(nextRegistered.getObjectName());
        nextRegistered = null;

    }

    /**
     * Creates a new instance of this class. Will create the Sigar instance this
     * class uses when constructing other MBeans.
     */

    public JmxRegister() {
        super(new Sigar(), CACHELESS);
        this.objectName = SigarInvokerJMX.DOMAIN_NAME + ":" + MBEAN_ATTR_TYPE + "=" + MBEAN_TYPE;
        this.managedBeans = new ArrayList();
    }


    public String getObjectName() {
        return this.objectName;
    }


    public MBeanInfo getMBeanInfo() {
        return MBEAN_INFO;
    }


    @Override
    public void postRegister(Boolean success) {

        super.postRegister(success);

        if (!success.booleanValue())
            return;

        // get CPUs
        getCPUInfo();
        
        // get memory
        try {
                getMemoryInfo();
            }
                catch (IllegalArgumentException ex) {}
                catch (SigarException ex) {}

        // get storage
        getStorageInfo();

         // get OSInfo_iniziale
        try {
            try {
                getOsInfo();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
        }


        // get PROCESS
        try {
            try {
                getProcessInfo();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(JmxRegister.class.getName()).log(Level.SEVERE, null, ex);
        }

        // get NETWORK
        try {
            getNetworkInfo();
        }
        catch (IllegalArgumentException ex) {}
        catch (SigarException ex) {}

    }




    @Override
    public void preDeregister() throws Exception {

        // count backwards to remove ONs immediately
        for (int i = managedBeans.size() - 1; i >= 0; i--) {
            ObjectName next = (ObjectName) managedBeans.remove(i);
            if (mbeanServer.isRegistered(next)) {
                try {
                    mbeanServer.unregisterMBean(next);
                } catch (Exception e) { // ignore
                }
            }
        }
        // do the super call
        super.preDeregister();
    }


    public Object invoke(String string, Object[] os, String[] strings) throws MBeanException, ReflectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public Object getAttribute(String attr) throws AttributeNotFoundException {
        throw new AttributeNotFoundException(attr);
    }


    public void setAttribute(Attribute attr) throws AttributeNotFoundException {
        throw new AttributeNotFoundException(attr.getName());
    }
}
