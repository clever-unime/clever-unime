 /*
 *  Copyright (c) 2011 MAurizio Paone
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
package org.clever.ClusterManager.Test;


import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;


public class TestAgent extends CmAgent implements Runnable
{
    private String version = "0.0.1";
    private String description = "Agent for testing purpose";
    private long timeInterval;
    private String HMTarget =null;
    private String agentTarget =null;
    private String methodTarget =null;
    private int thread=1;
    
    public TestAgent() throws CleverException, IOException
    {   super();
        logger = Logger.getLogger("TestAgent");
    }
    
    @Override
    public void initialization() throws CleverException
    {
        if(super.getAgentName().equals("NoName"))
            super.setAgentName("TestAgent");
        
        super.start();        
    }
         

    @Override
    public Class getPluginClass() 
    {
        return TestAgent.class;
    }

    @Override
    public Object getPlugin() 
    {
        return this;
    }    


    public String getVersion() 
    {
        return version;
    }


    public String getDescription() 
    {
        return description;
    }

    /**
     * Invokes a method (specified by method param) of a particular agent, in the same CM (specified by localAgent)
     * @param localAgent
     * @param method
     * @return
     * @throws CleverException 
     */
    public Object localInvoke(String localAgent,String method) throws CleverException
    {
        return this.invoke(localAgent, method, true, null);
    }


    public Object startTest(String HMTarget, String agentTarget , String methodTarget)
    {
        this.timeInterval= 4000;
        this.HMTarget = HMTarget;
        this.agentTarget = agentTarget;
        this.methodTarget = methodTarget;
        new Thread(this,"Test Thread: "+thread++).start();
        return true;
    }

    public Object startTest(String HMTarget, String agentTarget , String methodTarget, Long timeInterval)
    {
        this.timeInterval = timeInterval;
        this.HMTarget = HMTarget;
        this.agentTarget = agentTarget;
        this.methodTarget = methodTarget;
        Object result = "Errore nell'invocazione del metodo sull'hm:";
        try {
            result = this.remoteInvocation(this.HMTarget, agentTarget, methodTarget,true, new ArrayList());
            System.out.println("TestAgent: "+result);
        } catch (CleverException ex) {
            System.out.println("TestAgent error: "+ex.getMessage());
        }


//        try {
//            result = (String) mc.invoke(dispatchMethod);
//            System.out.println("TestAgent: "+result);
//        } catch (CleverException ex) {
//            System.out.println("TestAgent: "+ex.getMessage());
//        }


        new Thread(this,"Test Thread: "+thread++).start();
        return result;
    }


    @Override
    public void run() {
        
        
        String target = this.HMTarget;
         ArrayList params = new ArrayList();
      while(true)
      {
            try {
                Thread.sleep(this.timeInterval);
            } catch (InterruptedException ex) {
               System.out.println("Test thread interrupted!");
               return;
            }
       
        try {
            String result = (String) this.remoteInvocation(target, this.agentTarget, this.methodTarget, true, params);
            System.out.println("TestAgent: "+result);
        } catch (CleverException ex) {
            System.out.println("TestAgent error: "+ex.getMessage());
        }
      }
    }

    @Override
    public void handleNotification(Notification notification) throws CleverException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
@Override
   public void shutDown()
    {
        
    }


}
