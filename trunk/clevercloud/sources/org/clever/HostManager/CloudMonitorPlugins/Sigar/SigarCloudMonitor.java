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
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.clever.Common.Communicator.Agent.logger;
import org.hyperic.sigar.CpuPerc;


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
    
    
    
    //----------------------------------------
    //CPU MONITOR
    //----------------------------------------
    public String getCpuIdle(){
        
        
        Cpu cpu=null;
        CpuPerc cpuperc=null;
        try {
            //cpu = this.sigar.getCpu();
            cpuperc = this.sigar.getCpuPerc();
        } catch (SigarException ex) {
            Logger.getLogger(SigarCloudMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        String xmlobj=null;
        
        CpuM obj = new CpuM(CpuM.SubType_m.idle, "%");
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
    

    //----------------------------------------
    //NETWORKING MONITOR
    //----------------------------------------   
    
    
    
    //----------------------------------------
    //MEMORY MONITOR
    //----------------------------------------

    public String getTotalUsedMemory() {
        
        
        System.out.println( "sono dentro getTotalUsedMemory" ); 
        
        String totmem=null;
        Mem mem = null;
        
        try {
            
            mem = this.sigar.getMem();
            
            
            
        } catch (SigarException se) {
            se.printStackTrace();
        }
        
        totmem="Total used system memory.......: " + mem.getUsed() / 1024 / 1024+ " MB";
        
        //System.out.println(totmem);
        
        
        return totmem;
        
    }

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




}
