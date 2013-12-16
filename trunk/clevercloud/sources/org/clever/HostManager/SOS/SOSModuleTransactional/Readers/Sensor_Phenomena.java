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
public class Sensor_Phenomena {

    private String phen_id;
    private String phen_descr;
    private String phen_uom;
    private String phen_uom_id;
    private String offering_id;

    public Sensor_Phenomena() {
        this.phen_descr = "";
        this.phen_id = "";
        this.phen_uom = "";
        this.phen_uom_id = "";
        this.offering_id = "";
    }

    public String getphen_descr() {
        return this.phen_descr;
    }

    public void setphen_descr(String line) {
        this.phen_descr = line;
    }

    public String getphen_id() {
        return this.phen_id;
    }

    public void setphen_id(String line) {
        this.phen_id = line;
    }

    public String getphen_uom() {
        return this.phen_uom;
    }

    public void setphen_uom(String line) {
        this.phen_uom = line;
    }

    public String getphen_uom_id() {
        return this.phen_uom_id;
    }

    public void setphen_uom_id(String line) {
        this.phen_uom_id = line;
    }

    public String getoffering_id() {
        return this.offering_id;
    }

    public void setoffering_id(String line) {
        this.offering_id = line;
    }
}