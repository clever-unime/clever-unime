/*
 *  Copyright (c) 2011 Marco Sturiale
 *  Copyright (c) 2011 Alessio Di Pietro
 *  Copyright (c) 2012 Marco Carbone
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
package org.clever.ClusterManager.MonitorManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import static org.clever.Common.Communicator.Agent.logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.LogicalCatalogException;
import org.clever.Common.Initiator.ModuleFactory.ModuleFactory;
import org.clever.Common.Shared.HostEntityInfo;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.jivesoftware.smackx.muc.Occupant;


import org.clever.ClusterManager.MonitorManager.SendMeasureRequest;
import org.clever.Common.Measure.*;


public class MonitorManagerAgent extends CmAgent
{
    private String version = "1.0";
    private String description = "Monitoring of the Clever resources about Cluster Manager";

    
    
    
    public MonitorManagerAgent( ) throws CleverException {
        super();
        logger = Logger.getLogger( "MonitorManagerAgent" );
        
    }
    

    
    @Override
    public void initialization() throws CleverException {       
        
        boolean checkdb=true;
        List params = null;

        
        if(super.getAgentName().equals("NoName"))
            super.setAgentName("MonitorManagerAgent");
        
        
        super.start();
        
        
        //check "measure" node into sedna db
        checkdb=(Boolean)this.invoke("DatabaseManagerAgent", "checkMeasure", true, params);

        if(!checkdb){
        
            this.invoke("DatabaseManagerAgent", "addMeasure", true, params);
            logger.debug("MonitorManagerAgent: Measure DB node created!");
        }
        else
            logger.error("MonitorManagerAgent: Measure DB node exist!");
        
        

        
    }
    
    
  
    @Override
    public Class getPluginClass() {
        return MonitorManagerAgent.class;
    }

    @Override
    public Object getPlugin() {

        return this;
    }


    @Override
    public void handleNotification(Notification notification) throws CleverException {
        logger.debug("Received notification type: "+notification.getId());
        
    }

    
    @Override
    public void shutDown()
    {
        
    }
    
    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    
    
    
    
    public String getCpuAll(String target) throws CleverException{ 
        
        List params = new ArrayList();
        
        String result = getMeasure(target, "getCpuSys", params); //Risultato serializzato proveniente dal HM
        
        result = "<sourceHM name=\""+target+"\" >\n"+result+"\n</sourceHM>";
        
        List params1 = new ArrayList();
        params1.add(result);

        this.invoke("DatabaseManagerAgent", "insertMeasure", true, params1);

        
        return result;
    }
    
    
    public String getProcStatus(String target, String procname) throws CleverException{ 
        
        List params = new ArrayList();
        params.add(procname);
        
        String result = getMeasure(target, "getProcStatus", params); //Risultato serializzato proveniente dal HM
        
        result = "<sourceHM name=\""+target+"\" >\n"+result+"\n</sourceHM>";
        
        List params1 = new ArrayList();
        params1.add(result);

        this.invoke("DatabaseManagerAgent", "insertMeasure", true, params1);

        
        return result;
    } 
    
    
    
    
    public String getStorageStatus(String target) throws CleverException{ 
        
        List params = new ArrayList();
        
        String result = getMeasure(target, "getStorageStatus", params); 
        
        //result = "<sourceHM name=\""+target+"\" >\n"+result+"\n</sourceHM>";
        
        List params1 = new ArrayList();
        params1.add(result);

        //this.invoke("DatabaseManagerAgent", "insertMeasure", true, params1);
        
        return result;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    public String getMeasure(String target, String method, List params) throws CleverException{                        

        String measure = null;
       
        try{

            
            //Thread t = new Thread(new SendMeasureRequest(this, "webuntu", "CloudMonitorAgent", "getTotalUsedMemory", true, params));
            //t.start();
            
            measure = this.remoteInvocation(target,"CloudMonitorAgent",method, true, params).toString(); 

            //measure = (String) this.invoke("CloudMonitorAgent","getTotalUsedMemory", true, params);
        
            /*
            while(t.isAlive()){
                logger.debug("Thread is working...");
            }
            t.interrupt();
            logger.debug("Thread successfully stopped.");
            */
            

        
        } catch (Exception ex) {
            logger.error("Errore: " + ex);
        }
        
        return measure;
        
        
     }

  
    
    
    
}
