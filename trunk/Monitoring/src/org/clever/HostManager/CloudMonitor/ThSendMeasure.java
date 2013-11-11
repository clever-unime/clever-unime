/*
 * The MIT License
 *
 * Copyright 2013 webwolf.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.clever.HostManager.CloudMonitor;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.clever.Common.Communicator.Agent.logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMPPCommunicator.CleverMessage;


import org.clever.HostManager.CloudMonitorPlugins.Sigar.*;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ErrorResult;
import org.clever.Common.XMPPCommunicator.OperationResult;
import org.clever.Common.XMPPCommunicator.Result;

/**
 *conn
 * @author webwolf
 */
public class ThSendMeasure implements Runnable{
    
    
    //boolean done=false;
            
    private int frequency = 0;
    private CloudMonitorPlugin monitorPlugin= null;
    private ConnectionXMPP conn=null;
    private CloudMonitorAgent ma=null;

    private List params = new ArrayList();
    
   
    //List params = null;
    
    public ThSendMeasure(CloudMonitorAgent ma, CloudMonitorPlugin monitorPlugin, int frequency)
    {
        this.frequency = frequency;
        this.monitorPlugin=monitorPlugin;
        this.ma=ma;
        
    }
    
    
    public void dispatchMeasure(String measure){
        
        try {
                this.params.add(measure);
                this.ma.invoke("DispatcherAgent","sendMeasure", false, this.params);
                this.params.clear();

        }catch (CleverException ex) {
                logger.error("Error: "+ ex );
        }
        
    }
    
    public void dispatchHandshake(String hello){
        
        try {
                this.params.add(hello);
                this.ma.invoke("DispatcherAgent","sendHandshake", false, this.params);
                this.params.clear();

        }catch (CleverException ex) {
                logger.error("Error: "+ ex );
        }
        
    }    
    
    
    @Override
    public void run() {
        
        
        logger.debug("ThSendMeasure start!");
        
        //Inizializzazione
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            logger.debug("ThSendMeasure sleep failed: "+ex);
        }
        
        logger.debug("Start sending measure...");
        System.out.println("Start sending measure...");
        
        
        
        dispatchHandshake(this.monitorPlugin.handShaking());

        while(true){
            
            
        //for(int i=0; i<1; i++){
            
            /*
            //CPU monitor
            dispatchMeasure(this.monitorPlugin.getCpuIdle());
            dispatchMeasure(this.monitorPlugin.getCpuSys());
            dispatchMeasure(this.monitorPlugin.getCpuUser());
            
            
            //Memory monitor
            dispatchMeasure(this.monitorPlugin.getTotalUsedMemory());
            dispatchMeasure(this.monitorPlugin.getTotalFreeMemory());
            dispatchMeasure(this.monitorPlugin.getTotalMemory());
            
            
            //Network monitor
            dispatchMeasure(this.monitorPlugin.getInterfaceRX());
            dispatchMeasure(this.monitorPlugin.getInterfaceTX());
            dispatchMeasure(this.monitorPlugin.getInterfacePktRX());
            dispatchMeasure(this.monitorPlugin.getInterfacePktTX());
            
            
            
            //Storage monitor
            dispatchMeasure(this.monitorPlugin.getTotalStorage());
            dispatchMeasure(this.monitorPlugin.getAvailStorage());
            dispatchMeasure(this.monitorPlugin.getUsedStorage());
            dispatchMeasure(this.monitorPlugin.getUsedPercentStorage());
            dispatchMeasure(this.monitorPlugin.getReadBytesStorage());
            */
            dispatchMeasure(this.monitorPlugin.getWriteBytesStorage());
            
            
            
            try {
                Thread.sleep(frequency*1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ThSendMeasure.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            
        }
    
    
        //logger.debug("End sending measure!");
        //System.out.println("End sending measure!");
        
        
    }
    
    
    
    
    
}
