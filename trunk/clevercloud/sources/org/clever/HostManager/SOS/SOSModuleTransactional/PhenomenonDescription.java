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

/**
 *
 * @author user
 */
public class PhenomenonDescription {

    private String phenomenon_id;
    private String phenomenon_description;
    private String phenomenon_unit;
    private String phenomenon_valuetype;
    private String offering_id;
    private String offering_name;

    PhenomenonDescription() {
        this.offering_id = "";
        this.offering_name = "";
        this.phenomenon_description = "";
        this.phenomenon_id = "";
        this.phenomenon_unit = "";
        this.phenomenon_valuetype = "";


    }

    public void setPhenomenon_id(String str) {
        this.phenomenon_id = str;
    }

    public void setPhenomenon_description(String str) {
        this.phenomenon_description = str;

    }

    public void setPhenomenon_unit(String str) {
        this.phenomenon_unit = str;
    }

    public void setPhenomenon_valuetype(String str) {
        this.phenomenon_valuetype = str;
    }

    public void setOffering_id(String str) {
        this.offering_id = str;
    }

    public void setOffering_name(String str) {
        this.offering_name = str;
    }

    public String getPhenomenon_id() {
        return this.phenomenon_id;
    }

    public String getPhenomenon_description() {
        return this.phenomenon_description;

    }

    public String getPhenomenon_unit() {
        return this.phenomenon_unit;
    }

    public String getPhenomenon_valuetype() {
        return this.phenomenon_valuetype;
    }

    public String getOffering_id() {
        return this.offering_id;
    }

    public String getOffering_name() {
        return this.offering_name;
    }
}
