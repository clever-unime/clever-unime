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
package org.clever.Common.XMPPCommunicator;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.apache.log4j.Logger;
import org.clever.Common.XMLTools.MessageFormatter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author alessiodipietro
 */
public class NotificationOperation extends MessageBody {

    private String hostId;
    private String agentId;
    private String notificationId;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    

    public NotificationOperation(String hostId, String agentId, String notificationId,String type) {
        this.hostId = hostId;
        this.agentId = agentId;
        this.notificationId = notificationId;
        this.type=type;
        
        
    }

    @Override
    public String generateXML() {
            //hostId agentId notificationId List params

        int i;
        Element root = new Element("notification");
        root.setAttribute("useAttachementId", "true");
        Document doc = new Document(root);
        Element xmlHostId = new Element("hostId");
        root.addContent(xmlHostId);
        xmlHostId.addContent(hostId);
        Element xmlAgentId = new Element("agentId");
        xmlAgentId.addContent(agentId);
        root.addContent(xmlAgentId);
        Element xmlNotificationId = new Element("notificationId");
        xmlNotificationId.addContent(notificationId);
        root.addContent(xmlNotificationId);
        Element xmltype = new Element("type");
        xmltype.addContent(type);
        root.addContent(xmltype);
        
        XMLOutputter xout = new XMLOutputter();
        Format f = Format.getPrettyFormat();
        xout.setFormat(f);
        

        return (xout.outputString(doc));
    }

/**
 * @return the hostId
 */
public String getHostId() {
        return hostId;
    }

    /**
     * @param hostId the hostId to set
     */
    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    /**
     * @return the agentId
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * @param agentId the agentId to set
     */
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * @return the notificationId
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * @param notificationId the notificationId to set
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
}
