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
 * Copyright 2011 Università di Messina.
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
package org.clever.HostManager.SAS;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author alessiodipietro
 */
public class SensorAlertMessage {
    private Integer requestId;
    private String publicationId;
    private String alertMessageStructure;
    private double latitude;
    private double longitude;
    private double altitude;
    private String timeOfAlert;
    private int alertExpires;
    private String value;

    
    
   
    /**
     * @return the alertMessageStructure
     */
    public String getAlertMessageStructure() {
        return alertMessageStructure;
    }

    /**
     * @param alertMessageStructure the alertMessageStructure to set
     */
    public void setAlertMessageStructure(String alertMessageStructure) {
        this.alertMessageStructure=alertMessageStructure;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the altitude
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * @param altitude the altitude to set
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    /**
     * @return the timeOfAlert
     */
    public String getTimeOfAlert() {
        return timeOfAlert;
    }

    /**
     * @param timeOfAlert the timeOfAlert to set
     */
    public void setTimeOfAlert(Date timeOfAlert) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        this.timeOfAlert = sdf.format( timeOfAlert );
    }

    /**
     * @return the alertExpires
     */
    public int getAlertExpires() {
        return alertExpires;
    }

    /**
     * @param alertExpires the alertExpires to set
     */
    public void setAlertExpires(int alertExpires) {
        this.alertExpires = alertExpires;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    
    @Override
    public String toString(){
        StringBuffer buffer=new StringBuffer();
        buffer.append(this.latitude);
        buffer.append(" ");
        buffer.append(this.longitude);
        buffer.append(" ");
        buffer.append(this.altitude);
        buffer.append(" ");
        buffer.append(this.timeOfAlert);
        buffer.append(" ");
        buffer.append(this.alertExpires);
        buffer.append(" ");
        buffer.append(this.value);
        return buffer.substring(0);
    }

    /**
     * @return the requestId
     */
    public Integer getRequestId() {
        return requestId;
    }

    /**
     * @param requestId the requestId to set
     */
    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    /**
     * @return the publicationId
     */
    public String getPublicationId() {
        return publicationId;
    }

    /**
     * @param publicationId the publicationId to set
     */
    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }
    
    
}
