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
package org.clever.Common.Communicator;

/**
 *
 * @author alessiodipietro
 */
public class Notification {
    private String id;
    private Object body;
    private String hostId;
    private String agentId;
    //todo timestamp

    /**
     * @return the notifyId
     */
    public String getId() {
        return id;

    }

    /**
     * @param notifyId the notifyId to set
     */
    public void setId(String notificationId) {
        this.id = notificationId;
    }

    /**
     * @return the body
     */
    public Object getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(Object body) {
        this.body = body;
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


}
