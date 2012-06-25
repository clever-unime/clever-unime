/*
 * The MIT License
 *
 * Copyright 2011 Alessio Di Pietro.
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
package org.clever.HostManager.Test;


import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;

/**
 *
 * @author alessiodipietro
 */
public class TestAgent extends Agent implements Runnable {
    
    private String version = "0.0.1";
    private String description = "Agent for testing purpose";
    //public LoggerInstantiator loggerInstantiator2; 



   
    
   

    public TestAgent() throws CleverException
    {        
         super();
         logger=Logger.getLogger("TestAgent");
         logger.info("\n\nPROVA TEST AGENT HM\n");         
         
    }
    
     @Override
    public void initialization()
    {       
        if(super.getAgentName().equals("NoName"))
        {
            
            super.setAgentName("TestAgent");
        }
        try {
            
            super.start();
           
        } catch (CleverException ex) {
            logger.error(ex);
        }
    }
    
    
    
    public void startTest(){
        
        new Thread(this).start();
    }
    
    
    
    
    @Override
    public Class getPluginClass() {
        return this.getClass();
    }

    @Override
    public Object getPlugin() {
        return this;
    }

    @Override
    public void run() {
        
        Notification notification=new Notification();
        notification.setId("test/notificationtest");
       
        TestData data=new TestData();
        data.number=1;
        data.stringa="prova";
        notification.setBody(data);
        
       //MessageFormatter.objectFromMessage(msg.getBody())
        
        for(int i=0;i<1;i++){
            this.sendNotification(notification);
        }
        
        
    }
    
    @Override
   public void shutDown()
    {
        
    }
    
}
