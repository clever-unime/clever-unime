/*
 * Copyright [2014] [Università di Messina]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
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
import org.clever.Common.Plugins.PluginInformation;

/**
 *
 * @author alessiodipietro
 */
public class TestAgent extends Agent implements Runnable, PluginInformation {
    
    private String version = "0.0.1";
    private String description = "Agent for testing purpose";
    //public LoggerInstantiator loggerInstantiator2; 



   
    
   

    public TestAgent() throws CleverException
    {        
         super();
         
          
         
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

    @Override
    public String getName() {
        return "Test Agent Plugin";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getDescription() {
        return "Very simple Test Agent without plugin";
    }
    
}
