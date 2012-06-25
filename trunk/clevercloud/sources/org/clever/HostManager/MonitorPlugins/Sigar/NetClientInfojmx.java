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

import org.clever.HostManager.MonitorPlugins.Sigar.ClientNet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import java.io.*;
import java.net.*;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.jmx.AbstractMBean;
import org.hyperic.sigar.jmx.SigarInvokerJMX;

public class NetClientInfojmx extends AbstractMBean {

    private static final MBeanInfo MBEAN_INFO;

    private static final MBeanAttributeInfo MBEAN_ATTR_PACKET;

    private static final MBeanAttributeInfo MBEAN_ATTR_PACKETSIZE;

    private static final MBeanAttributeInfo MBEAN_ATTR_AVERAGE;

    private static final MBeanAttributeInfo MBEAN_ATTR_THROUGHPUT;

    private static final MBeanAttributeInfo MBEAN_ATTR_PKTS_S;

    private static final MBeanConstructorInfo MBEAN_CONSTR_SIGAR;

    private static MBeanParameterInfo MBEAN_PARAM_SIGAR;


    private Object pkts = 0;
    private Object pktsize = 0;
    private Object average = 0;
    private Object throughput = 0;
    private Object pktsec = 0;

    BufferedReader in = null;
    PrintStream out = null;
    Socket socket = null;

    static {
        MBEAN_ATTR_PACKET = new MBeanAttributeInfo("Packets", "Object",
                "Total Packets", true, false, false);
        MBEAN_ATTR_PACKETSIZE = new MBeanAttributeInfo("PacketsSize", "Object",
                "Total Packets size", true, false, false);
        MBEAN_ATTR_AVERAGE = new MBeanAttributeInfo("PacketsSizeAverage", "Object",
                "Average packet size", true, false, false);
        MBEAN_ATTR_THROUGHPUT = new MBeanAttributeInfo("Throughput", "Object",
                "Total bits per seconds", true, false, false);
        MBEAN_ATTR_PKTS_S = new MBeanAttributeInfo("ThroughputPkts", "Object",
                "Total packets per seconds", true, false, false);
        MBEAN_PARAM_SIGAR = new MBeanParameterInfo("sigar", Sigar.class
                .getName(), "The Sigar instance to use to fetch data from");
        MBEAN_CONSTR_SIGAR = new MBeanConstructorInfo(NetClientInfojmx.class.getName(),
                "Creates a new instance, using the Sigar instance "
                        + "specified to fetch the data.",
                new MBeanParameterInfo[] { MBEAN_PARAM_SIGAR });
        MBEAN_INFO = new MBeanInfo(
                NetClientInfojmx.class.getName(),
                "Sigar Network MBean, provides raw data for the traffic of the network. ",
                new MBeanAttributeInfo[] { MBEAN_ATTR_PACKET,
                        MBEAN_ATTR_PACKETSIZE, MBEAN_ATTR_AVERAGE,
                        MBEAN_ATTR_THROUGHPUT, MBEAN_ATTR_PKTS_S},
                new MBeanConstructorInfo[] { MBEAN_CONSTR_SIGAR }, null, null);

    }

    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */
    private final String objectName;


    public NetClientInfojmx() throws IllegalArgumentException, SigarException, Exception {
            this(new Sigar());
    }


    public NetClientInfojmx(Sigar sigar) throws IllegalArgumentException, SigarException, Exception {
        super(sigar, CACHED_500MS);

        try {
            // open a socket connection
            InetAddress host = InetAddress.getLocalHost();
            socket = new Socket(host, 4000);

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream(), true);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        this.objectName = SigarInvokerJMX.DOMAIN_NAME + ":" + MBEAN_ATTR_TYPE
                + "=Network";


    }

    public String getObjectName() {
        return this.objectName;
    }



    public Object getAttribute(String attr) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (MBEAN_ATTR_PACKET.getName().equals(attr)) {
            try {
                pkts=in.readLine();
            } catch (IOException ex) {
                Logger.getLogger(NetClientInfojmx.class.getName()).log(Level.SEVERE, null, ex);
            }
            return pkts;

        }  else if (MBEAN_ATTR_PACKETSIZE.getName().equals(attr)) {
            try {
                pktsize=in.readLine();
            } catch (IOException ex) {
                Logger.getLogger(NetClientInfojmx.class.getName()).log(Level.SEVERE, null, ex);
            }
            return pktsize;

        } else if (MBEAN_ATTR_AVERAGE.getName().equals(attr)) {
            try {
                average=in.readLine();
            } catch (IOException ex) {
                Logger.getLogger(NetClientInfojmx.class.getName()).log(Level.SEVERE, null, ex);
            }
            return average;

        } else if (MBEAN_ATTR_THROUGHPUT.getName().equals(attr)) {
            try {
                throughput=in.readLine();
            } catch (IOException ex) {
                Logger.getLogger(NetClientInfojmx.class.getName()).log(Level.SEVERE, null, ex);
            }
            return throughput;

        } else if (MBEAN_ATTR_PKTS_S.getName().equals(attr)) {
            try {
                pktsec=in.readLine();
            } catch (IOException ex) {
                Logger.getLogger(NetClientInfojmx.class.getName()).log(Level.SEVERE, null, ex);
            }
            return pktsec;

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
