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
 *  Copyright (c) 2010 Antonio Nastasi
 *  Copyright (c) 2011 Marco Sturiale
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
package org.clever.ClusterManager.Dispatcher;

import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.CleverMessagesDispatcher;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Plugins.RunnerPlugin;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;


/**
 * Clever Messages dispatcher for Cluster Manager
 * @author maurizio
 */
public interface CLusterManagerDispatcherPlugin extends RunnerPlugin, CleverMessagesDispatcher
{
  
    
  /*
  public void handleNotification(final Notification notification);
  public void handleMessage( final CleverMessage message );
  public void dispatch(final CleverMessage message);
  */
  
  public Object dispatchToExtern(final MethodInvoker method, final String to) throws CleverException;
  public Object dispatchToIntern(final MethodInvoker method) throws CleverException;
  public void setConnectionXMMP( final ConnectionXMPP connectionXMPP );
  
  public void subscribeNotification(final String agentName, final String notificationId);
  //public void handleNotification(final CleverMessage msg);

 

  //  public void scheduleMsg(CleverMessage msg);

  public void setOwner(Agent owner);
  
  
  //NEWMONITOR
  public void handleMeasure(final CleverMessage message);
          
          
          
}