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

import org.clever.HostManager.MonitorPlugins.Sigar.ProcessInfo;
import org.clever.HostManager.MonitorPlugins.Sigar.SigarMonitor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.jmx.AbstractMBean;
import org.hyperic.sigar.jmx.SigarInvokerJMX;

public class ProcessInfojmx extends AbstractMBean {

    private static final MBeanInfo MBEAN_INFO;

    private static final MBeanOperationInfo MBEAN_OPER_LISTPROCESSES;

    private static long[] ids;

    private List procinfo;

    static {

        MBEAN_OPER_LISTPROCESSES = new MBeanOperationInfo("listProcesses",
                "Executes a query returning the process IDs of all processes " +
                "found on the system.",
                new MBeanParameterInfo[0] ,
                String.class.getName(), MBeanOperationInfo.INFO);

        MBEAN_INFO = new MBeanInfo(
                ProcessInfojmx.class.getName(),
                "Sigar MBean registry. Provides a central point for creation "
                        + "and destruction of Sigar MBeans. Any Sigar MBean created via "
                        + "this instance will automatically be cleaned up when this "
                        + "instance is deregistered from the MBean server.",
                null /*new MBeanAttributeInfo[0]*/,
                null,//new MBeanConstructorInfo[] { MBEAN_CONSTR_DEFAULT },
                new MBeanOperationInfo[]{MBEAN_OPER_LISTPROCESSES},
                null /*new MBeanNotificationInfo[0]*/);
    }


    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */

    private final String objectName;


    public ProcessInfojmx(int procIndex) throws IllegalArgumentException, SigarException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        this(new Sigar());
    }


    public ProcessInfojmx(Sigar sigar) throws IllegalArgumentException, SigarException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        super(sigar, CACHED_500MS);
        ids =this.sigar.getProcList();

        SigarMonitor res = new SigarMonitor();
        procinfo=res.getProcessInfo();

        this.objectName = SigarInvokerJMX.DOMAIN_NAME + ":" + MBEAN_ATTR_TYPE
                + "=Process";
    }



    public String getObjectName() {
        return this.objectName;
    }

    /**
     * @return The index of the Process, typically starting at 0
     */

    public String listProcesses() {

            StringBuffer procNames = new StringBuffer();

            for (int i = 0; i < ids.length; i++) {
            try {
                int id = (int) ((ProcessInfo)procinfo.get(i)).getPID(i);
                procNames.append(id + 
                        " " + ((ProcessInfo) procinfo.get(i)).getUser(id) +
                        " " + ((ProcessInfo) procinfo.get(i)).getStartTime(id) +
                        " " + Sigar.formatSize((long) ((ProcessInfo)procinfo.get(i)).getMemoryUsage(id)) +
                        " " + Sigar.formatSize((long) ((ProcessInfo)procinfo.get(i)).getSwapDimension(id)) +
                        " " + ((ProcessInfo) procinfo.get(i)).getState(id) +
                        " " + ((ProcessInfo) procinfo.get(i)).getPriority(id) +
                        " " + ((ProcessInfo) procinfo.get(i)).getExecutionTime(id) +
                        " " + ((ProcessInfo) procinfo.get(i)).getCommand(id)).append("\n");
            } catch (SigarException ex) {
                Logger.getLogger(ProcessInfojmx.class.getName()).log(Level.SEVERE, null, ex);
            }
            }

            return procNames.toString();

    }


    public Object getAttribute(String attr) throws AttributeNotFoundException {
        throw new AttributeNotFoundException(attr);
    }


    public void setAttribute(Attribute attr) throws AttributeNotFoundException {
        throw new AttributeNotFoundException(attr.getName());
    }

     public Object invoke(String action, Object[] params, String[] signatures)
            throws MBeanException, ReflectionException {

      if (MBEAN_OPER_LISTPROCESSES.getName().equals(action))
            return listProcesses();

        else
            throw new ReflectionException(new NoSuchMethodException(action), action);
    }

    public MBeanInfo getMBeanInfo() {
        return MBEAN_INFO;
    }
}