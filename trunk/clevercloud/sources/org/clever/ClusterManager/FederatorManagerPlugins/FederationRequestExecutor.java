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
 * The MIT License
 *
 * Copyright (c) 2014 Giovanni Volpintesta
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

package org.clever.ClusterManager.FederatorManagerPlugins;

import java.util.List;
import org.apache.log4j.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.ErrorResult;
import org.clever.Common.XMPPCommunicator.MethodConfiguration;
import org.clever.Common.XMPPCommunicator.OperationResult;
import org.clever.Common.XMPPCommunicator.Result;
import org.clever.ClusterManager.FederatorManager.FederationManagerAgent;

/**
 * @author Giuseppe Tricomi
 * @author Giovanni Volpintesta
 */
public class FederationRequestExecutor extends Thread {
    FederationManagerAgent federationListenerAgent;
    CleverMessage msg;
    FederationMessagePoolThread poolThread;
    Logger logger;
    
    public FederationRequestExecutor(FederationManagerAgent federationListenerAgent, CleverMessage msg, Logger logger) {
        this.msg = msg;
        this.federationListenerAgent = federationListenerAgent;
        this.poolThread = this.federationListenerAgent.getFederationMessagePoolThread();
        this.logger = logger;
    }
    
    @Override
    public void run() {
        this.poolThread.putRequestInPool(this.msg, this.msg.getId());
        MethodConfiguration methodConf = new MethodConfiguration(msg.getBody(), msg.getAttachments());
        String agent = methodConf.getModuleName();
        String method = methodConf.getMethodName();
        boolean hasReply = msg.needsForReply();
        List params = methodConf.getParams();
        this.logger.debug ("moduleName: "+agent+" methodName: "+method+" hasReply: "+hasReply+" params: "+params+"\nid: "+msg.getId());
        CleverMessage replyMsg = new CleverMessage();
        replyMsg.setReplyToMsg(this.msg.getId());
        replyMsg.setId(this.msg.getId()); //important to retrieve the request message
        replyMsg.setHasReply(false);
         try {
            this.logger.debug("Launching federated method on CM..."+msg.getBodyOperation());
             this.logger.debug("Launching federated method on CM..."+agent);
             this.logger.debug("Launching federated method on CM..."+method);
            Object replyObject;
            switch(msg.getBodyOperation()){
                case "addAsActiveCMandReply":{
                    replyObject= this.federationListenerAgent.getFederationManagerPlugin().addAsActiveCMandReply((String)params.get(0),(String)params.get(1));
                    break;
                }
                case "createVM4Migration":{
                    replyObject=(Boolean)this.federationListenerAgent.getFederationManagerPlugin().createVM4Migration((org.clever.ClusterManager.FederatorManager.FederatorDataContainer)params.get(0));
                    break;
                }
                case "returnVM":{
                    replyObject=(org.clever.ClusterManager.FederatorManager.FederatorDataContainer)this.federationListenerAgent.getFederationManagerPlugin().returnVM((String)params.get(0),(String)params.get(1));
                    break;
                }
                case "deleteMigratedVM":{
                    replyObject=new Boolean(true);
                    this.federationListenerAgent.getFederationManagerPlugin().deleteMigratedVM((String)params.get(0));
                    break;
                }
                default:{
                    replyObject = this.federationListenerAgent.invoke(agent, method,hasReply, params);
                    break;
                }
            }
            /*if(msg.getBodyOperation().equals("addAsActiveCMandReply"))
                replyObject= this.federationListenerAgent.getFederationManagerPlugin().addAsActiveCMandReply((String)params.get(0),(String)params.get(1));
            else
                if(msg.getBodyOperation().equals("createVM4Migration"))
                    replyObject=(Boolean)this.federationListenerAgent.getFederationManagerPlugin().createVM4Migration((org.clever.ClusterManager.FederatorManager.FederatorDataContainer)params.get(0));
                else
                {
                    replyObject = this.federationListenerAgent.invoke(agent, method,hasReply, params);
                }*/
            this.logger.debug("object returned: "+replyObject);
            replyMsg.setType(CleverMessage.MessageType.REPLY);
            if (hasReply) {
                //this.logger.debug("Sto riempendo i campi relativi a body e attachment");
                replyMsg.setBody (new OperationResult (Result.ResultType.OBJECT, replyObject, methodConf.getModuleName(), methodConf.getMethodName()));
                replyMsg.addAttachment (MessageFormatter.messageFromObject (replyObject));
                //this.logger.debug("Sto riempendo i campi relativi a body e attachment:"+replyMsg.getBody());
            }
            //src e dst will be setted when the reply will be retrieved from stack
        } catch (CleverException ex) {
            replyMsg.setType(CleverMessage.MessageType.ERROR);
            replyMsg.setBody(new ErrorResult(Result.ResultType.ERROR, (new CleverException(ex)).toString(), methodConf.getModuleName(), methodConf.getMethodName()));
            replyMsg.addAttachment(MessageFormatter.messageFromObject(ex));
        } catch(Exception e) {
            logger.error("errore sconosciuto",e);
        }
        finally {
            try{
                this.logger.debug("Reply created: "+replyMsg.toXML());
            }
            catch(Exception e){//nothing to do
                
            }
            this.poolThread.manageReply(replyMsg);
        }
    }
}
