package org.clever.Common.SecureXMPPCommunicator;

import org.jivesoftware.smack.packet.PacketExtension;

/*
 * The MIT License
 *
 * Copyright 2012 Alessandro La Bella.
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

/**
 *
 * @author Alessandro La Bella
 */
public class SecureExtension implements PacketExtension {
    
    private String Type;
    private String Data;
    
    public SecureExtension(){
        this.Type = "encrypted";
    }
    
    public SecureExtension(String type){
        this.Type = type;
    }
    
    public void setType(String Type) {
        this.Type = Type;
    }
    
    public String getType(){
        return this.Type;
    }
    
    public void setData(String Data){
        this.Data = Data;
    }
    
    public String getData(){
        return this.Data;
    }
    
    public String toXML(){
        return "<x xmlns=\"jabber:x:" + this.Type + "\">" + this.Data + "</x>";
    }
    
    public String getElementName() {
        return "x";
    }
    
    public String getNamespace() {
        return "jabber:x:" + this.Type;
    }
    
}
