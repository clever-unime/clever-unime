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
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import org.clever.HostManager.Monitor.ResourceState;
import org.hyperic.sigar.FileSystem;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.jmx.AbstractMBean;
import org.hyperic.sigar.jmx.SigarInvokerJMX;


public class StorageInfojmx extends AbstractMBean {

    private static final String MBEAN_TYPE = "StorageList";

    private static final MBeanInfo MBEAN_INFO;

    private static final MBeanAttributeInfo MBEAN_ATTR_StorageIndex;

    private static final MBeanAttributeInfo MBEAN_ATTR_FS;

    private static final MBeanAttributeInfo MBEAN_ATTR_SIZE;

    private static final MBeanAttributeInfo MBEAN_ATTR_USED;

    private static final MBeanAttributeInfo MBEAN_ATTR_USEDPERC;

    private static final MBeanAttributeInfo MBEAN_ATTR_AVAIL;

    private static final MBeanAttributeInfo MBEAN_ATTR_DIR;

    private static final MBeanAttributeInfo MBEAN_ATTR_FSTYPE;

    private static final MBeanAttributeInfo MBEAN_ATTR_FILEDIM;

    private static final MBeanAttributeInfo MBEAN_ATTR_PERMISSION;

    private static final MBeanAttributeInfo MBEAN_ATTR_LINK;

    private static final MBeanConstructorInfo MBEAN_CONSTR_StorageIndex;

    private static final MBeanConstructorInfo MBEAN_CONSTR_StorageIndex_SIGAR;

    private static MBeanParameterInfo MBEAN_PARAM_StorageIndex;

    private static MBeanParameterInfo MBEAN_PARAM_SIGAR;

    private float currentFreeSpace = 0;
    private float totalSpace = 0;
    private float used = 0;
    private String usedpc = null;
    private String dir = null;
    private String fileSystemType = null;
    private String name = null;
    private double maxFileDimension = 0;
    private boolean supportLink;
    private boolean supportFilePermission;

    static {
        MBEAN_ATTR_StorageIndex = new MBeanAttributeInfo("StorageIndex", "int",
                "The index of the FileSystem, typically starting at 0", true, false,
                false);
        MBEAN_ATTR_FS = new MBeanAttributeInfo("FileSystem", "String",
                "FileSystem name", true, false, false);
        MBEAN_ATTR_SIZE = new MBeanAttributeInfo("Size", "String",
                "FileSystem total size", true,
                false, false);
        MBEAN_ATTR_USED = new MBeanAttributeInfo("Used", "String",
                "FileSystem used size", true, false,
                false);
        MBEAN_ATTR_AVAIL = new MBeanAttributeInfo("Avail", "String",
                "FileSystem avail size", true, false, false);
        MBEAN_ATTR_USEDPERC = new MBeanAttributeInfo("Used %", "String",
                "FileSystem used in percentage", true, false,
                false);
        MBEAN_ATTR_FILEDIM = new MBeanAttributeInfo("MaxFileDim", "String",
                "Maximum size of file storage", true, false, false);
        MBEAN_ATTR_PERMISSION = new MBeanAttributeInfo("SupportPermission", "Boolean",
                "Indicates if is possible to set access permissions to files", true, false, false);
        MBEAN_ATTR_LINK = new MBeanAttributeInfo("SupportLink", "Boolean",
                "Indicates if the filesystem supports hard links", true, false, false);
        MBEAN_ATTR_DIR = new MBeanAttributeInfo("Mounted on", "String",
                "Directory where the FileSystem is mounted", true,
                false, false);
        MBEAN_ATTR_FSTYPE = new MBeanAttributeInfo("Type", "String",
                "FileSystem type",
                true, false, false);
        MBEAN_PARAM_StorageIndex = new MBeanParameterInfo("StorageIndex", "int",
                "The index of the FileSystem to read data for. Must be >= 0 ");
        MBEAN_PARAM_SIGAR = new MBeanParameterInfo("sigar", Sigar.class
                .getName(), "The Sigar instance to use to fetch data from");
        MBEAN_CONSTR_StorageIndex = new MBeanConstructorInfo(StorageInfojmx.class
                .getName(),
                "Creates a new instance for the FileSystem index specified, "
                        + "using a new Sigar instance to fetch the data. "
                        + "Fails if the FileSystem index is out of range.",
                new MBeanParameterInfo[] { MBEAN_PARAM_StorageIndex });
        MBEAN_CONSTR_StorageIndex_SIGAR = new MBeanConstructorInfo(
                StorageInfojmx.class.getName(),
                "Creates a new instance for the FileSystem index specified, "
                        + "using the Sigar instance specified to fetch the data. "
                        + "Fails if the FileSystem index is out of range.",
                new MBeanParameterInfo[] { MBEAN_PARAM_SIGAR,
                        MBEAN_PARAM_StorageIndex });
        MBEAN_INFO = new MBeanInfo(
                StorageInfojmx.class.getName(),
                "Sigar FileSystem MBean.",
                new MBeanAttributeInfo[] { 
                        MBEAN_ATTR_FS, MBEAN_ATTR_SIZE, MBEAN_ATTR_USED,MBEAN_ATTR_USEDPERC,
                        MBEAN_ATTR_AVAIL, MBEAN_ATTR_DIR, MBEAN_ATTR_FSTYPE,
                        MBEAN_ATTR_FILEDIM, MBEAN_ATTR_PERMISSION, MBEAN_ATTR_LINK },
                new MBeanConstructorInfo[] { MBEAN_CONSTR_StorageIndex,
                        MBEAN_CONSTR_StorageIndex_SIGAR }, null, null);

    }

    /**
     * Index of the FileSystem processed by the instance.
     */
    private final int StorageIndex;

    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */

    private final String objectName;


    public StorageInfojmx(int StorageIndex) throws IllegalArgumentException, Exception {
        this(new Sigar(), StorageIndex);
    }


    public StorageInfojmx(Sigar sigar, int StorageIndex) throws IllegalArgumentException, Exception {
        super(sigar, CACHED_500MS);

        // check index
        if (StorageIndex < 0)
            throw new IllegalArgumentException(
                    "FileSystem index has to be non-negative: " + StorageIndex);
        try {
            int storageCount;
            if ((storageCount = sigar.getFileSystemList().length) < StorageIndex)
                throw new IllegalArgumentException(
                        "FileSystem index out of range (found " + storageCount
                                + " FileSystem(s)): " + StorageIndex);

        } catch (SigarException e) {
            throw unexpectedError(MBEAN_TYPE, e);
        }

        // all fine
        this.StorageIndex = StorageIndex;

        try {
                FileSystem fs=sigar.getFileSystemList()[this.StorageIndex];
                if(fs.getType()==FileSystem.TYPE_LOCAL_DISK){
                        this.objectName = SigarInvokerJMX.DOMAIN_NAME + ":" + MBEAN_ATTR_TYPE
                        + "=Storage,"
                        + MBEAN_ATTR_StorageIndex.getName().substring(0, 1).toLowerCase()
                        + MBEAN_ATTR_StorageIndex.getName().substring(1) + "=" + StorageIndex;
                }else  this.objectName = "";
        } catch (SigarException e) {
                throw unexpectedError(MBEAN_TYPE, e);
        }


        SigarMonitor res = new SigarMonitor();

        List storageinfo=res.getStorageInfo();
        List stocurfreespace=res.getStorageCurrentFreeSpace();
        List storused=res.getStorageUsed();
        List storusedpc=res.getStorageUsedpc();
        
        FileSystem fs=sigar.getFileSystemList()[this.StorageIndex];
        if(fs.getType()==FileSystem.TYPE_LOCAL_DISK){
            totalSpace=((StorageInfo)storageinfo.get(this.StorageIndex)).getTotalSpace();
            dir=((StorageInfo)storageinfo.get(this.StorageIndex)).getDir();
            fileSystemType=((StorageInfo)storageinfo.get(this.StorageIndex)).getFIleSystemType();
            name=((StorageInfo)storageinfo.get(this.StorageIndex)).getName();
            maxFileDimension=((StorageInfo)storageinfo.get(this.StorageIndex)).MaxFileDimension();
            supportLink=((StorageInfo)storageinfo.get(this.StorageIndex)).SupportLink();
            supportFilePermission=((StorageInfo)storageinfo.get(this.StorageIndex)).SupportFilePermission();

            currentFreeSpace=((ResourceState)stocurfreespace.get(this.StorageIndex)).getCurrentUsage();
            used=((ResourceState)storused.get(this.StorageIndex)).getCurrentUsage();
            usedpc=((ResourceState)storusedpc.get(this.StorageIndex)).getCurrentUsage()+"%";
        }
    }

    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */
    public String getObjectName() {
        return this.objectName;
    }

 

    public Object getAttribute(String attr) throws AttributeNotFoundException {

        if (MBEAN_ATTR_FS.getName().equals(attr)) {
            return name;

        } else if (MBEAN_ATTR_SIZE.getName().equals(attr)) {
            return totalSpace;

        } else if (MBEAN_ATTR_USED.getName().equals(attr)) {
            return used;

        } else if (MBEAN_ATTR_AVAIL.getName().equals(attr)) {
            return currentFreeSpace;

        } else if (MBEAN_ATTR_USEDPERC.getName().equals(attr)) {
            return usedpc;

        } else if (MBEAN_ATTR_DIR.getName().equals(attr)) {
            return dir;

        } else if (MBEAN_ATTR_FSTYPE.getName().equals(attr)) {
            return fileSystemType;

        } else if (MBEAN_ATTR_FILEDIM.getName().equals(attr)) {
            return maxFileDimension;

        } else if (MBEAN_ATTR_PERMISSION.getName().equals(attr)) {
            return supportFilePermission;

        } else if (MBEAN_ATTR_LINK.getName().equals(attr)) {
            return supportLink;

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


