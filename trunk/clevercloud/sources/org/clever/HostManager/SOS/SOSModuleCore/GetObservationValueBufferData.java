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
 */package org.clever.HostManager.SOS.SOSModuleCore;

import java.util.Vector;

/**
 *
 * @author user
 */
public class GetObservationValueBufferData {

    private String phen_id;
    private String sens_id;
    private String value;
    private String time;
    private String lat;
    private String longit;
    private String mintime;
    private String maxtime;
    private Vector<GetObservationFieldBufferData> field;

    GetObservationValueBufferData() {
        this.phen_id = "";
        this.sens_id = "";
        this.value = "";
        this.time = "";
        this.lat = "";
        this.longit = "";
        this.maxtime = "";
        this.mintime = "";
        this.field = new Vector<GetObservationFieldBufferData>(1);

    }

    Vector<GetObservationFieldBufferData> getfield() {
        return this.field;
    }

    void setfield(Vector<GetObservationFieldBufferData> elem) {
        this.field = elem;
    }

    void setphen_id(String elem) {
        this.phen_id = elem;
    }

    void setsens_id(String elem) {
        this.sens_id = elem;
    }

    void setmintime(String elem) {
        this.mintime = elem;
    }

    void setmaxtime(String elem) {
        this.maxtime = elem;
    }

    void settime(String elem) {
        this.time = elem;
    }

    void setlat(String elem) {
        this.lat = elem;
    }

    void setlong(String elem) {
        this.longit = elem;
    }

    void setvalue(String elem) {
        this.value = elem;
    }

    String getphen_id() {
        return this.phen_id;

    }

    String getmintime() {
        return this.mintime;

    }

    String getmaxtime() {
        return this.maxtime;

    }

    String getsens_id() {
        return this.sens_id;

    }

    String gettime() {
        return this.time;

    }

    String getlat() {
        return this.lat;

    }

    String getlong() {
        return this.longit;

    }

    String getvalue() {
        return this.value;

    }
}
