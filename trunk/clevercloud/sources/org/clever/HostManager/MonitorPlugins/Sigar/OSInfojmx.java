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
import org.clever.HostManager.Monitor.OSInfo;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.jmx.AbstractMBean;
import org.hyperic.sigar.jmx.SigarInvokerJMX;

public class OSInfojmx extends AbstractMBean {

    private static final MBeanInfo MBEAN_INFO;

    private static final MBeanAttributeInfo MBEAN_ATTR_OSNAME;

    private static final MBeanAttributeInfo MBEAN_ATTR_OSKERNEL;

    private static final MBeanAttributeInfo MBEAN_ATTR_OSARCHITECTURE;

    private static final MBeanAttributeInfo MBEAN_ATTR_OSVERSION;

    private static final MBeanAttributeInfo MBEAN_ATTR_OSDISTRIBUTION;

    private static final MBeanAttributeInfo MBEAN_ATTR_OSDATAMODEL;

    private static final MBeanConstructorInfo MBEAN_CONSTR_SIGAR;

    private static MBeanParameterInfo MBEAN_PARAM_SIGAR;

    private OSInfo os;

    static {
        MBEAN_ATTR_OSNAME = new MBeanAttributeInfo("OSname", "String",
                "User friendly name", true, false, false);
        MBEAN_ATTR_OSKERNEL = new MBeanAttributeInfo("Kernel", "String",
                "Operating System Kernel", true, false, false);
        MBEAN_ATTR_OSARCHITECTURE = new MBeanAttributeInfo("Architecture", "String",
                "Operating System architecture", true, false, false);
        MBEAN_ATTR_OSVERSION = new MBeanAttributeInfo("Version", "String",
                "Kernel's version", true, false, false);
        MBEAN_ATTR_OSDISTRIBUTION = new MBeanAttributeInfo("Distribution", "String",
                "Operating System distributin", true, false, false);
        MBEAN_ATTR_OSDATAMODEL = new MBeanAttributeInfo("Family", "int",
                "Specifies whether the operating system is 32-bit or 64-bit", true, false, false);
        MBEAN_PARAM_SIGAR = new MBeanParameterInfo("sigar", Sigar.class
                .getName(), "The Sigar instance to use to fetch data from");
        MBEAN_CONSTR_SIGAR = new MBeanConstructorInfo(OSInfojmx.class.getName(),
                "Creates a new instance, using the Sigar instance "
                        + "specified to fetch the data.",
                new MBeanParameterInfo[] { MBEAN_PARAM_SIGAR });
        MBEAN_INFO = new MBeanInfo(
                OSInfojmx.class.getName(),
                "Sigar Memory MBean, provides raw data for the physical "
                        + "memory installed on the system. Uses an internal cache "
                        + "that invalidates within 500ms, allowing for bulk request "
                        + "being satisfied with a single dataset fetch.",
                new MBeanAttributeInfo[] { MBEAN_ATTR_OSNAME, MBEAN_ATTR_OSKERNEL,
                        MBEAN_ATTR_OSARCHITECTURE, MBEAN_ATTR_OSVERSION,
                        MBEAN_ATTR_OSDISTRIBUTION, MBEAN_ATTR_OSDATAMODEL},
                new MBeanConstructorInfo[] { MBEAN_CONSTR_SIGAR }, null, null);

    }

    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */
    
    private final String objectName;

    public OSInfojmx(Sigar sigar) throws IllegalArgumentException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        super(sigar, CACHED_500MS);

        SigarMonitor res = new SigarMonitor();
        os=res.getOsInfo();

        this.objectName = SigarInvokerJMX.DOMAIN_NAME + ":" + MBEAN_ATTR_TYPE
                + "=OSinfo";
    }

    public String getObjectName() {
        return this.objectName;
    }




    public Object getAttribute(String attr) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (MBEAN_ATTR_OSNAME.getName().equals(attr)) {
            return new String(os.getName());

        } else if ( MBEAN_ATTR_OSKERNEL.getName().equals(attr)) {
          return new String(os.getOs());

        } else if (MBEAN_ATTR_OSARCHITECTURE.getName().equals(attr)) {
            return new Integer(os.getArchitecture());

        }   else if (MBEAN_ATTR_OSVERSION.getName().equals(attr)) {
            return new String(os.getVersion());

        } else if (MBEAN_ATTR_OSDISTRIBUTION.getName().equals(attr)) {
            return new String(os.getDistribution());

        }  else if (MBEAN_ATTR_OSDATAMODEL.getName().equals(attr)) {
            return new String(os.getFamily());

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