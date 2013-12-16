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

import org.clever.HostManager.SAS.SensorAlertMessage;

/**
 *
 * @author alessiodipietro
 */
public class MucSensorAlertMessage {
    
    private PublishDeliveryEntry publishDeliveryEntry;
    private SensorAlertMessage sensorAlertMessage;

    /**
     * @return the publishDeliveryEntry
     */
    public PublishDeliveryEntry getPublishDeliveryEntry() {
        return publishDeliveryEntry;
    }

    /**
     * @param publishDeliveryEntry the publishDeliveryEntry to set
     */
    public void setPublishDeliveryEntry(PublishDeliveryEntry publishDeliveryEntry) {
        this.publishDeliveryEntry = publishDeliveryEntry;
    }

    /**
     * @return the sensorAlertMessage
     */
    public SensorAlertMessage getSensorAlertMessage() {
        return sensorAlertMessage;
    }

    /**
     * @param sensorAlertMessage the sensorAlertMessage to set
     */
    public void setSensorAlertMessage(SensorAlertMessage sensorAlertMessage) {
        this.sensorAlertMessage = sensorAlertMessage;
    }
    
}
