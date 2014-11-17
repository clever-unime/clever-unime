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
 * Copyright 2011 Alessio Di Pietro
 * Copyright 2012 Marco Carbone
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
package org.clever.Common.Communicator;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.clever.Common.Exceptions.CleverException;

/**
 *
 * @author alessiodipietro
 */
public abstract class CmAgent extends Agent implements NotificationHandler  {
    public CmAgent() throws CleverException
    {  
       super();
       this.group = "CM";
       
    }

    public CmAgent(String name) throws CleverException
    {   
        //TODO: controllare perche' chiama sempre super() senza parametro
        super();
        this.group = "CM";
    }
    
    
   
    
    
    
    /**
     * Invokes a method of a remote agent on a particular HM
     * @param hostTarget the name of the target HM
     * @param agentTarget the name of the target agent
     * @param methodTarget the method that has to be invoked
     * @param hasReply true if the method returns an object
     * @param params the method's parameters
     * @return the object returned by the method invoked or null if hasReply is false
     * @throws CleverException
     */
    public Object remoteInvocation( String hostTarget, String agentTarget, String methodTarget, boolean hasReply , List params) throws CleverException
    {

        MethodInvoker targetMethod= new MethodInvoker(agentTarget, methodTarget, hasReply, params);
        List dispatchParams = new ArrayList();
        dispatchParams.add(targetMethod);
        dispatchParams.add(hostTarget);
        MethodInvoker dispatchMethod= new MethodInvoker("DispatcherAgent", "dispatchToExtern", true, dispatchParams);
        return mc.invoke(dispatchMethod);
            
       
    }
    
    
    
}
