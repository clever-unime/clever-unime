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
 *  The MIT License
 * 
 *  Copyright 2011 Maurizio Paone.
 *  Copyright 2013 Nicola Peditto
 *  Copyright 2013 Carmelo Romeo
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.clever.Common.Communicator;

import org.apache.log4j.Logger;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import static org.clever.Common.XMPPCommunicator.CleverMessage.MessageType.MEASURE;
import java.util.ArrayList;
/**
 *
 * @author Maurizio Paone
 */
class ThreadMessageHandler extends Thread{
    
    private static final Logger log = Logger.getLogger(ThreadMessageHandler.class);
    
    
    private CleverMessagesDispatcher dispatcher;
    private ThreadMessageDispatcher threadDispatcher;
    private CleverMessage message;
    private boolean running = true;
    private Agent sasAgent=null;
    ThreadMessageHandler(CleverMessagesDispatcher dispatcher, ThreadMessageDispatcher aThis, CleverMessage msg) {
        this.dispatcher = dispatcher;
        this.message=msg;
        this.threadDispatcher=aThis;
        this.setName("ThreadMessageHandler");
    }
    
    ThreadMessageHandler(Agent sasAgent, ThreadMessageDispatcher aThis, CleverMessage msg) {
        this.sasAgent = sasAgent;
        this.message=msg;
        this.threadDispatcher=aThis;
        this.setName("ThreadMessageHandler4sas");
    }
    /**
     * @return the message
     */
    public synchronized CleverMessage getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public synchronized void setMessage(CleverMessage message) {
        this.message = message;
    }
    public synchronized void manageMessage(CleverMessage message) {
        this.message = message;
        this.notifyAll();
    }
    
    @Override
    public synchronized void run()
    {
          while(running)
          {
             switch( this.message.getType() )
            {
                        
                      case NOTIFY:
                          
                          Notification notification=this.message.getNotificationFromMessage();
                          if((notification.getId().equals("SAS/Publish"))&&(this.sasAgent!=null))
                          {
                              ArrayList params=new ArrayList();
                              params.add(this.message);
                              MethodInvoker mi=new MethodInvoker("SASAgent","deliverPublish",false,params);
                              try{
                                dispatcher.dispatchToIntern(mi);
                              }
                              catch(Exception ce){
                                  log.error("error in dispatchToIntern invoke.Send SAS/Publish on dedicated MUC has Failed!",ce);
                              }
                          }
                          else{
                          //Pass notification to dispatcher
                          dispatcher.handleNotification(notification);
                          }
                        break;
                          
                      case ERROR:
                          
                      case REPLY:
                          log.debug("Reply received");
                        dispatcher.handleMessage( this.message );
                        break;
                          
                      case REQUEST:
                         log.debug("Request received");
                        dispatcher.dispatch( this.message );
                        break;
                          
                      //NEWMONITOR
                      case MEASURE:
                        dispatcher.handleMeasure( this.message );
                        break;
                          
            }
             
            this.threadDispatcher.threadTerminated(this);
            try {
                this.wait();
            } catch (InterruptedException ex) {
                //TODO: interrupt threadHandlers
                log.debug("Interrupt exiting ...");
               running = false;
            }
        }
    }

   

}
