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
package org.clever.HostManager.SOS.SOSModuleCore;

/**
 *
 * @author user
 */
public class IdentifierDescription {

    private String identifier_id;
    private String identifier_value;
    private String identifier_description;

    IdentifierDescription() {
        this.identifier_description = "";
        this.identifier_id = "";
        this.identifier_value = "";


    }

    public void setidentifier_id(String str) {
        this.identifier_id = str;
    }

    public void setidentifier_description(String str) {
        this.identifier_description = str;

    }

    public void setidentifier_value(String str) {
        this.identifier_value = str;
    }

    public String getidentifier_id() {
        return this.identifier_id;
    }

    public String getidentifier_description() {
        return this.identifier_description;

    }

    public String getidentifier_value() {
        return this.identifier_value;
    }
}
