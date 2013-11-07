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

package org.clever.HostManager.CloudMonitorPlugins.Sigar;

import java.io.IOException;
import org.clever.HostManager.CloudMonitor.CloudMonitorPlugin;
import java.util.List;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Exceptions.CleverException;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.jdom.Element;

import org.clever.Common.Measure.*;
import org.clever.Common.XMLTools.MessageFormatter;
import org.hyperic.sigar.Cpu;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.clever.Common.Communicator.Agent.logger;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.MultiProcCpu;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.ProcCredName;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.ProcTime;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarProxyCache;
import org.hyperic.sigar.Uptime;

public class SigarCloudMonitor implements CloudMonitorPlugin{

    ModuleCommunicator mc;
    private Agent owner;
    private Sigar sigar;
    

    public SigarCloudMonitor() throws IOException
    {
        this.sigar = new Sigar();
        
    }
    

    public void init(Element params, Agent owner) throws CleverException{
        
    }

    
    public String getName() {
            String name = "CloudMonitor";
            return name;
    }

    public String getVersion() {
            String version = "1.0";
            return version;
    }

    public String getDescription()
    {
      throw new UnsupportedOperationException( "Not supported yet." );
    }
    
    
    public void setOwner(Agent owner) {
        this.owner=owner;
    }
    
    
    
    
    
    
    
    public String getProcStatus(String procname){
        
        
        MultiProcCpu mproc=null;
        ProcCredName pcred=null;
        ProcMem pmem=null;
        ProcState pstate=null;
        ProcTime ptime=null;

        
        String xmlobj=null;
        
        ProcessM obj = null;
        
        String query="State.Name.eq="+procname;
        
        try {
            
            mproc = this.sigar.getMultiProcCpu(query); 
            obj = new ProcessM(ProcessM.SubType_m.cpu, ProcessM.Unit_m.percent);
            obj.setValue(CpuPerc.format(mproc.getPercent()));
            xmlobj=MessageFormatter.messageFromObject(obj)+"\n";
            
            pcred = this.sigar.getProcCredName(query);
            obj = new ProcessM(ProcessM.SubType_m.user, ProcessM.Unit_m.text);
            obj.setValue(pcred.getUser());
            xmlobj=xmlobj+MessageFormatter.messageFromObject(obj)+"\n";
            obj = new ProcessM(ProcessM.SubType_m.group, ProcessM.Unit_m.text);
            obj.setValue(pcred.getGroup());
            xmlobj=xmlobj+MessageFormatter.messageFromObject(obj)+"\n";
            
            pmem= this.sigar.getProcMem(query);
            obj = new ProcessM(ProcessM.SubType_m.mem, ProcessM.Unit_m.MB);
            obj.setValue(pmem.getResident()/1024/1024);
            xmlobj=xmlobj+MessageFormatter.messageFromObject(obj)+"\n";
            
            pstate= this.sigar.getProcState(query);
            obj = new ProcessM(ProcessM.SubType_m.state, ProcessM.Unit_m.text);
            obj.setValue(pstate.getState());
            xmlobj=xmlobj+MessageFormatter.messageFromObject(obj)+"\n";
            
            ptime= this.sigar.getProcTime(query);
            DateFormat formatter = new SimpleDateFormat("E dd.MM.yyyy hh:mm:ss a z");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(ptime.getStartTime());
            obj = new ProcessM(ProcessM.SubType_m.state, ProcessM.Unit_m.text);
            obj.setValue(formatter.format(calendar.getTime()));
            xmlobj=xmlobj+MessageFormatter.messageFromObject(obj)+"\n";
            
            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        

        

        
        return xmlobj;
    }   
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //----------------------------------------
    //CPU MONITOR
    //----------------------------------------
    public String getCpuIdle(){
        
        
        //Cpu cpu=null;
        CpuPerc cpuperc=null;
        try {
            //cpu = this.sigar.getCpu();
            cpuperc = this.sigar.getCpuPerc();
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        String xmlobj=null;
        
        CpuM obj = new CpuM(CpuM.SubType_m.idle, CpuM.Unit_m.percent);
        obj.setValue(CpuPerc.format(cpuperc.getIdle()));
        
        
        //FORMAT obj TO xml
        xmlobj=MessageFormatter.messageFromObject(obj);
        
        
        /*
        // serialize the object
        String serializedObject = "";
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(obj);
            so.flush();
            serializedObject = bo.toString();
            
            logger.debug("AAA"+serializedObject);
            
            
        } catch (Exception e) {
            System.out.println(e);
        }
        
        */
 
        
        
        return xmlobj;
    }
    
    public String getCpuSys(){
        
        
        CpuPerc cpuperc=null;
        String xmlobj=null;
        CpuM obj = null;
        
        try {
            
            cpuperc = this.sigar.getCpuPerc();
            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        obj = new CpuM(CpuM.SubType_m.sys, CpuM.Unit_m.percent);
        obj.setValue(CpuPerc.format(cpuperc.getSys()));
        
        
        //FORMAT obj TO xml
        xmlobj=MessageFormatter.messageFromObject(obj);
        

        
        return xmlobj;
    }

    
    public String getCpuUser(){
        
        
        
        CpuPerc cpuperc=null;
        String xmlobj=null;
        CpuM obj = null;
        
        try {
            
            cpuperc = this.sigar.getCpuPerc();
            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        obj = new CpuM(CpuM.SubType_m.usr, CpuM.Unit_m.percent);
        obj.setValue(CpuPerc.format(cpuperc.getUser()));
        
        
        //FORMAT obj TO xml
        xmlobj=MessageFormatter.messageFromObject(obj);
        

        
        return xmlobj;
    }
    
    //----------------------------------------
    //STORAGE MONITOR
    //----------------------------------------     
    public String getTotalStorage(){
        
        String xmlobj=null;
        StorageM obj = null;
            
            
        try {

            
            SigarProxy proxy = SigarProxyCache.newInstance(this.sigar);
        
            FileSystem[] fileSystemList = proxy.getFileSystemList();
            
            for (int i = 0; i < fileSystemList.length; i++) {
                
                FileSystem fs = fileSystemList[i];
                
                if (fs.getType() == FileSystem.TYPE_LOCAL_DISK){
                    
                    FileSystemUsage usage = this.sigar.getFileSystemUsage(fs.getDirName());
                    
                    System.out.println(" total: " + usage.getTotal()/1024/1024);
                    //System.out.println(" avail: " + usage.getAvail()/1024/1024);
                    //System.out.println(" used: " + usage.getUsed()/1024/1024);
                    //System.out.println(" use %: " + usage.getUsePercent()*100);
                    //System.out.println(" read: " + usage.getDiskReadBytes());
                    //System.out.println(" write: " + usage.getDiskWriteBytes());
                    
                    obj = new StorageM(StorageM.SubType_m.total, StorageM.Unit_m.GB, fs.getDevName(), fs.toString());
            
                    obj.setValue(usage.getTotal()/1024/1024);

                    //FORMAT obj TO xml
                    xmlobj=MessageFormatter.messageFromObject(obj);


                }

            }

            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return xmlobj;
        
    }
  
    
    
    public String getAvailStorage(){
        
        String xmlobj=null;
        StorageM obj = null;
            
            
        try {

            
            SigarProxy proxy = SigarProxyCache.newInstance(this.sigar);
        
            FileSystem[] fileSystemList = proxy.getFileSystemList();
            
            for (int i = 0; i < fileSystemList.length; i++) {
                
                FileSystem fs = fileSystemList[i];
                
                if (fs.getType() == FileSystem.TYPE_LOCAL_DISK){
                    
                    FileSystemUsage usage = this.sigar.getFileSystemUsage(fs.getDirName());
                   
                    
                    obj = new StorageM(StorageM.SubType_m.free, StorageM.Unit_m.GB, fs.getDevName(), fs.toString());
            
                    obj.setValue(usage.getAvail()/1024/1024);

                    //FORMAT obj TO xml
                    xmlobj=MessageFormatter.messageFromObject(obj);


                }

            }

            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return xmlobj;
        
    }    
    
    
    public String getUsedStorage(){
        
        String xmlobj=null;
        StorageM obj = null;
            
            
        try {

            
            SigarProxy proxy = SigarProxyCache.newInstance(this.sigar);
        
            FileSystem[] fileSystemList = proxy.getFileSystemList();
            
            for (int i = 0; i < fileSystemList.length; i++) {
                
                FileSystem fs = fileSystemList[i];
                
                if (fs.getType() == FileSystem.TYPE_LOCAL_DISK){
                    
                    FileSystemUsage usage = this.sigar.getFileSystemUsage(fs.getDirName());
                   
                    
                    obj = new StorageM(StorageM.SubType_m.used, StorageM.Unit_m.GB, fs.getDevName(), fs.toString());
            
                    obj.setValue(usage.getUsed()/1024/1024);

                    //FORMAT obj TO xml
                    xmlobj=MessageFormatter.messageFromObject(obj);


                }

            }

            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return xmlobj;
        
    }       
    
    
    
    
    public String getUsedPercentStorage(){
        
        String xmlobj=null;
        StorageM obj = null;
            
            
        try {

            
            SigarProxy proxy = SigarProxyCache.newInstance(this.sigar);
        
            FileSystem[] fileSystemList = proxy.getFileSystemList();
            
            for (int i = 0; i < fileSystemList.length; i++) {
                
                FileSystem fs = fileSystemList[i];
                
                if (fs.getType() == FileSystem.TYPE_LOCAL_DISK){
                    
                    FileSystemUsage usage = this.sigar.getFileSystemUsage(fs.getDirName());
                   
                    
                    obj = new StorageM(StorageM.SubType_m.used, StorageM.Unit_m.percent, fs.getDevName(), fs.toString());
            
                    obj.setValue(usage.getUsePercent()*100);

                    //FORMAT obj TO xml
                    xmlobj=MessageFormatter.messageFromObject(obj);


                }

            }

            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return xmlobj;
        
    }       

    
    
    
       
    public String getReadBytesStorage(){
        
        String xmlobj=null;
        StorageM obj = null;
            
            
        try {

            
            SigarProxy proxy = SigarProxyCache.newInstance(this.sigar);
        
            FileSystem[] fileSystemList = proxy.getFileSystemList();
            
            for (int i = 0; i < fileSystemList.length; i++) {
                
                FileSystem fs = fileSystemList[i];
                
                if (fs.getType() == FileSystem.TYPE_LOCAL_DISK){
                    
                    FileSystemUsage usage = this.sigar.getFileSystemUsage(fs.getDirName());
                   
                    
                    obj = new StorageM(StorageM.SubType_m.read, StorageM.Unit_m.B, fs.getDevName(), fs.toString());
            
                    obj.setValue(usage.getDiskReadBytes());

                    //FORMAT obj TO xml
                    xmlobj=MessageFormatter.messageFromObject(obj);


                }

            }

            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return xmlobj;
        
    }   
    
 
    
    
    
    public String getWriteBytesStorage(){
        
        String xmlobj=null;
        StorageM obj = null;
            
            
        try {

            
            SigarProxy proxy = SigarProxyCache.newInstance(this.sigar);
        
            FileSystem[] fileSystemList = proxy.getFileSystemList();
            
            for (int i = 0; i < fileSystemList.length; i++) {
                
                FileSystem fs = fileSystemList[i];
                
                if (fs.getType() == FileSystem.TYPE_LOCAL_DISK){
                    
                    FileSystemUsage usage = this.sigar.getFileSystemUsage(fs.getDirName());
                   
                    
                    obj = new StorageM(StorageM.SubType_m.write, StorageM.Unit_m.B, fs.getDevName(), fs.toString());
            
                    obj.setValue(usage.getDiskWriteBytes());

                    //FORMAT obj TO xml
                    xmlobj=MessageFormatter.messageFromObject(obj);


                }

            }

            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return xmlobj;
        
    }      
    
    
    
    
    
    
    
    
    //----------------------------------------
    //NETWORKING MONITOR
    //----------------------------------------   
    public String getInterfaceRX() {
        
        NetInterfaceConfig netinterfaceconfig = null;
        NetInterfaceStat netinterfacestat = null;
        
        String xmlobj=null;
        NetworkM obj = null;
        
        try {
            
            netinterfaceconfig = this.sigar.getNetInterfaceConfig();
            netinterfacestat = this.sigar.getNetInterfaceStat(netinterfaceconfig.getName());
            
            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        obj = new NetworkM(NetworkM.SubType_m.rx, NetworkM.Unit_m.B);
        obj.setValue(netinterfacestat.getRxBytes());
        
        //FORMAT obj TO xml
        xmlobj=MessageFormatter.messageFromObject(obj);
       
        return xmlobj; 
        
    }  
    
 
    public String getInterfaceTX() {
        
        NetInterfaceConfig netinterfaceconfig = null;
        NetInterfaceStat netinterfacestat = null;
        
        String xmlobj=null;
        NetworkM obj = null;
        
        try {
            
            netinterfaceconfig = this.sigar.getNetInterfaceConfig();
            netinterfacestat = this.sigar.getNetInterfaceStat(netinterfaceconfig.getName());
            
            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        obj = new NetworkM(NetworkM.SubType_m.tx, NetworkM.Unit_m.B);
        obj.setValue(netinterfacestat.getTxBytes());
        
        //FORMAT obj TO xml
        xmlobj=MessageFormatter.messageFromObject(obj);
       
        return xmlobj; 
        
    } 
   
    
    public String getInterfacePktTX() {
        
        NetInterfaceConfig netinterfaceconfig = null;
        NetInterfaceStat netinterfacestat = null;
        
        String xmlobj=null;
        NetworkM obj = null;
        
        try {
            
            netinterfaceconfig = this.sigar.getNetInterfaceConfig();
            netinterfacestat = this.sigar.getNetInterfaceStat(netinterfaceconfig.getName());
            
            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        obj = new NetworkM(NetworkM.SubType_m.pkt_tx, NetworkM.Unit_m.packet);
        obj.setValue(netinterfacestat.getTxPackets());
        
        //FORMAT obj TO xml
        xmlobj=MessageFormatter.messageFromObject(obj);
       
        return xmlobj; 
        
    }  
    
    
    public String getInterfacePktRX() {
        
        NetInterfaceConfig netinterfaceconfig = null;
        NetInterfaceStat netinterfacestat = null;
        
        String xmlobj=null;
        NetworkM obj = null;
        
        try {
            
            netinterfaceconfig = this.sigar.getNetInterfaceConfig();
            netinterfacestat = this.sigar.getNetInterfaceStat(netinterfaceconfig.getName());
            
            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        obj = new NetworkM(NetworkM.SubType_m.pkt_rx, NetworkM.Unit_m.packet);
        obj.setValue(netinterfacestat.getRxPackets());
        
        //FORMAT obj TO xml
        xmlobj=MessageFormatter.messageFromObject(obj);
       
        return xmlobj; 
        
    }     
    
    
    //----------------------------------------
    //MEMORY MONITOR
    //----------------------------------------

    public String getTotalUsedMemory() {
        
        Mem mem = null;
        
        String xmlobj=null;
        MemoryM obj = null;
        
        try {
            
            mem = this.sigar.getMem();
            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        obj = new MemoryM(MemoryM.SubType_m.used, MemoryM.Unit_m.MB);
        obj.setValue(mem.getUsed() / 1024 / 1024);
        
        
        //FORMAT obj TO xml
        xmlobj=MessageFormatter.messageFromObject(obj);
        

        
        return xmlobj;       
       
        
    }

    public String getTotalMemory() {
        
        Mem mem = null;
        
        String xmlobj=null;
        MemoryM obj = null;
        
        try {
            
            mem = this.sigar.getMem();
            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        obj = new MemoryM(MemoryM.SubType_m.total, MemoryM.Unit_m.MB);
        obj.setValue(mem.getTotal() / 1024 / 1024);
        
        
        //FORMAT obj TO xml
        xmlobj=MessageFormatter.messageFromObject(obj);
        

        
        return xmlobj;       
       
        
    }
 
    
    
     public String getTotalFreeMemory() {
        
        Mem mem = null;
        
        String xmlobj=null;
        MemoryM obj = null;
        
        try {
            
            mem = this.sigar.getMem();
            
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        obj = new MemoryM(MemoryM.SubType_m.free, MemoryM.Unit_m.MB);
        obj.setValue(mem.getFree() / 1024 / 1024);
        
        
        //FORMAT obj TO xml
        xmlobj=MessageFormatter.messageFromObject(obj);
        

        
        return xmlobj;       
       
        
    }   
    
    
    
    
    /*    
    public void getInformationsAboutMemory() {
        
        
        System.out.println("**************************************");
        System.out.println("*** Informations about the Memory: ***");
        System.out.println("**************************************\n");

        Mem mem = null;
        try {
            
            mem = this.sigar.getMem();
            
            
            
        } catch (SigarException se) {
            se.printStackTrace();
        }

        System.out.println("Actual total free system memory: "
                + mem.getActualFree() / 1024 / 1024+ " MB");
        System.out.println("Actual total used system memory: "
                + mem.getActualUsed() / 1024 / 1024 + " MB");
        System.out.println("Total free system memory ......: " + mem.getFree()
                / 1024 / 1024+ " MB");
        System.out.println("System Random Access Memory....: " + mem.getRam()
                + " MB");
        System.out.println("Total system memory............: " + mem.getTotal() / 1024 / 1024+ " MB");
        System.out.println("Total used system memory.......: " + mem.getUsed() / 1024 / 1024+ " MB");

        System.out.println("\n**************************************\n");

        
    }

    */

     private static String formatUptime(double uptime) {
        String retval = "";
        int days = (int)uptime / (60*60*24);
        int minutes, hours;
        if (days != 0) {
            retval += days + " " + ((days > 1) ? "days" : "day") + ", ";
        }
        minutes = (int)uptime / 60;
        hours = minutes / 60;
        hours %= 24;
        minutes %= 60;
        if (hours != 0) {
            retval += hours + ":" + minutes + " [hh:mm]";
        }
        else {
            retval += minutes + " min";
        }
        return retval;
    }
     
     
    public String handShaking(){
        
       
        String xmlobj=null;
       
        Uptime uptime = null;
        
        try {
            uptime=this.sigar.getUptime();
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }

        xmlobj="Chat created! VM uptime: " + formatUptime(uptime.getUptime());
            
  
        
        return xmlobj;
        
    }

}
