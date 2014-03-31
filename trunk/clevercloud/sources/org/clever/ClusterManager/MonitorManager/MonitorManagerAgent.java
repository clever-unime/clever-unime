/*
 *  The MIT License
 * 
 *  Copyright (c) 2013 Nicola Peditto
 *  Copyright (c) 2013 Carmelo Romeo
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

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;


public class MonitorManagerAgent extends CmAgent
{
    private String version = "1.0";
    private String description = "Monitoring of the Clever resources about Cluster Manager";

    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger=null;
    private String pathLogConf="/sources/org/clever/ClusterManager/MonitorManager/log_conf/";
    private String pathDirOut="/LOGS/ClusterManager/MonitorManager";
    //########    
    
    
    public MonitorManagerAgent( ) throws CleverException {
        super();
              
       //#############################################
       //Inizializzazione meccanismo di logging
       logger=Logger.getLogger( "MonitorManagerAgent" );    
       Log4J log =new Log4J();
       log.setLog4J(logger, pathLogConf, pathDirOut);
       //#############################################  
        
        
        
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

    
    
    
   /**
   * Return CPU statistics to Clever shell
   * @param target name of HM or VM probe
   * @return String statistics of CPU human readable format
   * @throws CleverException
   */
    public String getCpuAll(String target) throws CleverException{ 
        
        List params = new ArrayList();
        
        String result = getMeasure(target, "getCpuStatus", params); //Risultato serializzato proveniente dal HM
        
        result = "Target: "+target+"\n"+result+"\n";
        
        //result = "<sourceHM name=\""+target+"\" >\n"+result+"\n</sourceHM>";
        
        List params1 = new ArrayList();
        params1.add(result);

        //this.invoke("DatabaseManagerAgent", "insertMeasure", true, params1);

        
        return result;
    }
    
    
   /**
   * Return process statistics from HM and VM probe
   * @param target name of HM or VM probe
   * @param procname name of the process e.g. "java", "skype", etc.
   * @return String statistics of the process in xml format
   * @throws CleverException
   */    
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
    
    
    
   /**
   * Return storage statistics to Clever shell of the main partition of the HM or VM probe
   * @param target name of HM or VM probe
   * @return String statistics of storage in human readable format
   * @throws CleverException
   */    
    public String getStorageStatus(String target) throws CleverException{ 
        
        List params = new ArrayList();
        
        String result = getMeasure(target, "getStorageStatus", params); 
        
        //result = "<sourceHM name=\""+target+"\" >\n"+result+"\n</sourceHM>";
        
        List params1 = new ArrayList();
        params1.add(result);

        //this.invoke("DatabaseManagerAgent", "insertMeasure", true, params1);
        
        return result;
    }
    
    
    
    
    
    
    
    
    
    
    
    
   /**
   * Method used by the above methods to invoke the Sigar API into "CloudMonitorAgent" plugin.
   * @param target name of HM or VM probe
   * @param method name of the method into "CloudMonitorAgent" plugin
   * @param params list of the parameters of the method into "CloudMonitorAgent" plugin
   * @return return a string of the result of the method invoked
   * @throws CleverException
   */     
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
