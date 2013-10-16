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
public class Sensor_Component {

    private String comp_id;
    private String comp_descr;
    private String comp_phenomena;
    private String comp_status;

    public Sensor_Component() {
        this.comp_id = "";
        this.comp_descr = "";
        this.comp_phenomena = "";
        this.comp_status = "";
    }

    public String getcomp_descr() {
        return this.comp_descr;
    }

    public void setcomp_descr(String line) {
        this.comp_descr = line;
    }

    public String getcomp_id() {
        return this.comp_id;
    }

    public void setcomp_id(String line) {
        this.comp_id = line;
    }

    public String getcomp_phenomena() {
        return this.comp_phenomena;
    }

    public void setcomp_phenomena(String line) {
        this.comp_phenomena = line;
    }

    public String getcomp_status() {
        return this.comp_status;
    }

    public void setcomp_status(String line) {
        this.comp_status = line;
    }
}