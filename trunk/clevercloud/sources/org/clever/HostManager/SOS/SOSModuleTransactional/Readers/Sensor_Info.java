/*
 * The MIT License
 *
 * Copyright 2012 alessiodipietro.
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
package org.clever.HostManager.SOS.SOSModuleTransactional.Readers;

/**
 *
 * @author alessiodipietro
 */
public class Sensor_Info {

    private String id;
    private String type_id;
    private String product_description;
    private String manufacturer;
    private String model;
    private String operator_area;
    private String class_application;
    private String measures_interval;
    private String measures_interval_uom;
    private String alt_uom;
    private String alt_val;
    private String lat_uom;
    private String lat_val;
    private String long_uom;
    private String long_val;
    private String ref;
    private String active;
    private String mobile;
    private String packet;

    public Sensor_Info() {
        this.active = "";
        this.type_id = "";
        this.id = "";
        this.mobile = "";
        this.alt_uom = "";
        this.alt_val = "";
        this.class_application = "";
        this.lat_uom = "";
        this.lat_val = "";
        this.long_uom = "";
        this.long_uom = "";
        this.manufacturer = "";
        this.measures_interval = "";
        this.measures_interval_uom = "";
        this.model = "";
        this.product_description = "";
        this.ref = "";
        this.operator_area = "";
        this.packet = "";
    }

    public String getactive() {
        return this.active;
    }

    public void setactive(String line) {
        this.active = line;
    }

    public String getalt_uom() {
        return this.alt_uom;
    }

    public void setalt_uom(String line) {
        this.alt_uom = line;
    }

    public String getalt_val() {
        return this.alt_val;
    }

    public void setoperator_area(String line) {
        this.operator_area = line;
    }

    public String getoperator_area() {
        return this.operator_area;
    }

    public void setalt_val(String line) {
        this.alt_val = line;
    }

    public String getclass_application() {
        return this.class_application;
    }

    public void setclass_application(String line) {
        this.class_application = line;
    }

    public String getid() {
        return this.id;
    }

    public void setid(String line) {
        this.id = line;
    }

    public String getlat_uom() {
        return this.lat_uom;
    }

    public void setlat_uom(String line) {
        this.lat_uom = line;
    }

    public String getlat_val() {
        return this.lat_val;
    }

    public void setlat_val(String line) {
        this.lat_val = line;
    }

    public String getlong_uom() {
        return this.long_uom;
    }

    public void setlong_uom(String line) {
        this.long_uom = line;
    }

    public String getlong_val() {
        return this.long_val;
    }

    public void setlong_val(String line) {
        this.long_val = line;
    }

    public String getmanufacturer() {
        return this.manufacturer;
    }

    public void setmanufacturer(String line) {
        this.manufacturer = line;
    }

    public String getmeasures_interval() {
        return this.measures_interval;
    }

    public void setmeasures_interval(String line) {
        this.measures_interval = line;
    }

    public String getmeasures_interval_uom() {
        return this.measures_interval_uom;
    }

    public void setmeasures_interval_uom(String line) {
        this.measures_interval_uom = line;
    }

    public String getmobile() {
        return this.mobile;
    }

    public void setmobile(String line) {
        this.mobile = line;
    }

    public String getmodel() {
        return this.model;
    }

    public void setmodel(String line) {
        this.model = line;
    }

    public String getproduct_description() {
        return this.product_description;
    }

    public void setproduct_description(String line) {
        this.product_description = line;
    }

    public String getref() {
        return this.ref;
    }

    public void setref(String line) {
        this.ref = line;
    }

    public String gettype_id() {
        return this.type_id;
    }

    public void settype_id(String line) {
        this.type_id = line;
    }

    public String getpacket() {
        return this.packet;
    }

    public void setpacket(String line) {
        this.packet = line;
    }
}