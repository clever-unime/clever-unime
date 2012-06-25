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

import java.util.logging.Logger;
import org.clever.ClusterManager.Dispatcher.DispatcherPlugin;
import org.clever.Common.XMPPCommunicator.CleverMessage;

/**
 *
 * @author Maurizio Paone
 */
class ThreadMessageHandler extends Thread{
    private DispatcherPlugin dispatcher;
    private ThreadMessageDispatcher threadDispatcher;
    private CleverMessage message;
    
    ThreadMessageHandler(DispatcherPlugin dispatcher, ThreadMessageDispatcher aThis, CleverMessage msg) {
        this.dispatcher = dispatcher;
        this.message=msg;
        this.threadDispatcher=aThis;
        this.setName("ThreadMessageHandler");
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
          while(true)
          {
             switch( this.message.getType() )

            {

                      case NOTIFY:
                          Notification notification=this.message.getNotificationFromMessage();


                          //Pass notification to dispatcher
                          dispatcher.handleNotification(notification);

                        break;
                      case ERROR:
                      case REPLY:
                        dispatcher.handleMessage( this.message );
                        break;
                      case REQUEST:

                        dispatcher.dispatch( this.message );
                        break;
            }
            this.threadDispatcher.threadTerminated(this);
            try {
                this.wait();
            } catch (InterruptedException ex) {
               //TODO: manage exception
            }
        }
    }

   

}
