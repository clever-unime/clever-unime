/*
 * The MIT License
 *
 * Copyright 2011 alessiodipietro.
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
package org.clever.ClusterManager.SAS;

import java.util.TimerTask;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author alessiodipietro
 */
public class SubscriptionExpirationTask extends TimerTask {
    private SASAgent sasAgent=null;
    private String subscriptionId;
    private XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    
    public SubscriptionExpirationTask(SASAgent sasAgent, String subscriptionId){
        this.sasAgent=sasAgent;
        this.subscriptionId=subscriptionId;
    }

    @Override
    public void run() {
        
        //remove subscription from publishDelivery and subscriptions hashtable
        sasAgent.removeSubscriptionFromTables(subscriptionId);
        
        //subscriptions remains on db because it can be renewed
        
    }
    
}
