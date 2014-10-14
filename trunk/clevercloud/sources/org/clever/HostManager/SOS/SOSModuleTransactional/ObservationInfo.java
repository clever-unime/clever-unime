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
 * Copyright 2012 Università di Messina.
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
package org.clever.HostManager.SOS.SOSModuleTransactional;

import java.util.Vector;

/**
 *
 * @author user
 */
public class ObservationInfo {

    private String sensor_id;
    private String time_stamp;
    private float latitude;
    private String lat_uom;
    private float longitude;
    private String long_uom;
    private String time_definition;
    private String lat_definition;
    private String long_definition;
    private String phenomenon_composite;
    private Vector<ObsPhenomenaValue> ObsPhenomena;

    public ObservationInfo() {
        this.sensor_id = "";
        this.time_stamp = "";
        this.phenomenon_composite = "";
        this.longitude = (float) -1.0;
        this.latitude = (float) -1.0;
        this.lat_uom = "";
        this.long_uom = "";
        this.time_definition = "";
        this.lat_definition = "";
        this.long_definition = "";
        this.ObsPhenomena = new Vector<ObsPhenomenaValue>(1);


    }

    String getSensor_id() {
        return this.sensor_id;
    }

    String getTime_stamp() {
        return this.time_stamp;
    }

    String getPhenomenon_composite() {
        return this.phenomenon_composite;
    }

    String getlat_uom() {
        return this.lat_uom;
    }

    String gettime_definition() {
        return this.time_definition;
    }

    String getlat_definition() {
        return this.lat_definition;
    }

    String getlong_definition() {
        return this.long_definition;
    }

    String getlong_uom() {
        return this.long_uom;
    }

    Vector<ObsPhenomenaValue> getObsPhenomena() {
        return this.ObsPhenomena;
    }

    float getLatitude() {
        return this.latitude;
    }

    float getLongitude() {
        return this.longitude;
    }

    void setSensor_id(String stringa) {
        this.sensor_id = stringa;
    }

    void setTime_stamp(String stringa) {
        this.time_stamp = stringa;
    }

    void setPhenomenon_composite(String stringa) {
        this.phenomenon_composite = stringa;
    }

    void setLat_uom(String stringa) {
        this.lat_uom = stringa;
    }

    void setLong_uom(String stringa) {
        this.long_uom = stringa;
    }

    void settime_definition(String stringa) {
        this.time_definition = stringa;
    }

    void setlat_definition(String stringa) {
        this.lat_definition = stringa;
    }

    void setlong_definition(String stringa) {
        this.long_definition = stringa;
    }

    void setLongitude(float stringa) {
        this.longitude = stringa;
    }

    void setLatitude(float stringa) {
        this.latitude = stringa;
    }

    void setObsPhenomena(Vector<ObsPhenomenaValue> stringa) {
        this.ObsPhenomena = stringa;
    }
}
