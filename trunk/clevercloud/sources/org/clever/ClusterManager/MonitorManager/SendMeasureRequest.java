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
package org.clever.ClusterManager.MonitorManager;

import java.util.ArrayList;
import java.util.List;
import static org.clever.Common.Communicator.Agent.logger;
import org.clever.Common.Exceptions.CleverException;

/**
 *
 * @author webwolf
 */
public class SendMeasureRequest implements Runnable{
    
    
    boolean done=false;
            
    private String measure = null;
    
    private MonitorManagerAgent MA = null;
    private String source = null;
    String AgentName = null; 
    String AgentMethod = null; 
    Boolean getReturn = null; 
    List params = null;
    
    public SendMeasureRequest(MonitorManagerAgent ma, String source, String AgentName, String AgentMethod, Boolean getReturn, List params)
    {
        this.MA = ma;
        this.AgentMethod= AgentMethod;
        this.AgentName=AgentName;
        this.getReturn=getReturn;
        this.source=source;
        this.params=params;
        
    }
    
    
    
    @Override
    public void run() {
        
        try{

                
                logger.debug("THREAD PARAMS: "+ this.source + " - " + this.AgentName + " - " + this.AgentMethod + " - " + this.getReturn + " - " + this.params);

                measure = "Thread --> " + (String) this.MA.remoteInvocation(this.source,this.AgentName,this.AgentMethod, this.getReturn, this.params);

                logger.debug("MEASURE: "+measure);
                
                /*
                try{
                    Thread.sleep(5);
                }catch (InterruptedException ex){}
                */
                return;
           
            
        }catch (CleverException ex){
            logger.error("Errore: " + ex);
        }
        
        
    }
    
    
    
    
    
}
