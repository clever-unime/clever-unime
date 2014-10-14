/*
 * Copyright 2014 Università di Messina
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

package org.clever.ClusterManager.SAS;

//import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.clever.ClusterManager.Dispatcher.DispatcherPlugin;
//import org.clever.Common.XMPPCommunicator.CleverMessage;
//TODO: Modificare questa parte per renderla conforme al sistema di dispatch standard di Common
/**
 * THIS CLASS IS DEPRECATED
 * @author sabayonuser
 */
public class ThreadMessageDispatcher extends Thread {


    LinkedBlockingQueue <MucSensorAlertMessage> mucSensorAlertMessages ;

    /**
     * Queue of not active thread for the CleverMessage managing
     */
    LinkedBlockingQueue<ThreadMessageHandler> poolMessageHandlers;
    /**
     * Queue for running CleverMessage handlers
     */
    ArrayBlockingQueue<ThreadMessageHandler> activeMessageHandlers;

    SASAgent sasAgent;

    public ThreadMessageDispatcher(SASAgent sasAgent,Integer maxMessagesInQueue, Integer maxActiveHandlers)
    {
        this.setName("messageDispatcher");
        this.sasAgent = sasAgent;
        this.mucSensorAlertMessages = new LinkedBlockingQueue(maxMessagesInQueue);
        this.poolMessageHandlers = new LinkedBlockingQueue();
        this.activeMessageHandlers = new ArrayBlockingQueue(maxActiveHandlers,true);
    }

    /**
     * Method invoked for insert a CleverMessage in Queue
     */
    public  void pushMessage(MucSensorAlertMessage msg)
    {
         
        if(!this.mucSensorAlertMessages.offer(msg))
        {
             //TODO: manage log message
        }
    }

    void threadTerminated(ThreadMessageHandler th)
    {
        this.activeMessageHandlers.remove(th);
        this.poolMessageHandlers.offer(th); //TODO: check result values
    }


    @Override
    public synchronized void run() {
        while(true) //TODO insert condition to exit
        {
            
            try {
                MucSensorAlertMessage msg = this.mucSensorAlertMessages.take(); //retrieve message or wait for it
              
                if(this.poolMessageHandlers.isEmpty() && this.activeMessageHandlers.remainingCapacity()!=0)
                {
                    //no more thread in pool, but there is free space in active threads; create new one
              
                    ThreadMessageHandler mh = new ThreadMessageHandler(sasAgent,this,msg);
                    this.activeMessageHandlers.put(mh);
                    mh.start();

                }
                else
                {
                    //threads pool is not empty or active threads queue is full : retrieve a thread from pool and put it in active threads (or wait)
                     ThreadMessageHandler mh = this.poolMessageHandlers.take(); //retrieve thread or wait if the queue is empty
                     
                     this.activeMessageHandlers.put(mh); //put it in activethreads queue or wait for free space
                     
                     mh.manageMessage(msg);
                }

            } catch (InterruptedException ex) {
                 
                //TODO: manage thread death
            }
        }
    }

}
