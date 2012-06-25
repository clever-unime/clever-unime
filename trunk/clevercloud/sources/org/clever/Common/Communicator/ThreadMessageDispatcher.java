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

package org.clever.Common.Communicator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.clever.ClusterManager.Dispatcher.DispatcherPlugin;
import org.clever.Common.XMPPCommunicator.CleverMessage;

/**
 *
 * @author sabayonuser
 */
public class ThreadMessageDispatcher extends Thread {


    LinkedBlockingQueue <CleverMessage> cleverMessages ;

    /**
     * Queue of not active thread for the CleverMessage managing
     */
    LinkedBlockingQueue<ThreadMessageHandler> poolMessageHandlers;
    /**
     * Queue for running CleverMessage handlers
     */
    ArrayBlockingQueue<ThreadMessageHandler> activeMessageHandlers;

    DispatcherPlugin dispatcher;

    public ThreadMessageDispatcher(DispatcherPlugin d,Integer maxMessagesInQueue, Integer maxActiveHandlers)
    {
        this.setName("messageDispatcher");
        this.dispatcher = d;
        this.cleverMessages = new LinkedBlockingQueue(maxMessagesInQueue);
        this.poolMessageHandlers = new LinkedBlockingQueue();
        this.activeMessageHandlers = new ArrayBlockingQueue(maxActiveHandlers,true);
    }

    /**
     * Method invoked for insert a CleverMessage in Queue
     */
    public  void pushMessage(CleverMessage msg)
    {
         
        if(!this.cleverMessages.offer(msg))
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
                CleverMessage msg = this.cleverMessages.take(); //retrieve message or wait for it
              
                if(this.poolMessageHandlers.isEmpty() && this.activeMessageHandlers.remainingCapacity()!=0)
                {
                    //no more thread in pool, but there is free space in active threads; create new one
              
                    ThreadMessageHandler mh = new ThreadMessageHandler(this.dispatcher,this,msg);
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
