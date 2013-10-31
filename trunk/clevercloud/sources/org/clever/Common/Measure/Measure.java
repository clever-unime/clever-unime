/*
 * The MIT License
 *
 * Copyright 2013 webwolf.
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
package org.clever.Common.Measure;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 *
 * @author webwolf
 */
public abstract class Measure {
    
    private long date=0;

    private String source=null;
    
    public enum Type_m {cpu, mem, sto, net};
    private Type_m type_m=null;
    
    public enum Unit_m {percent, B, MB, GB, s, ns, packet, unknown};
    private Unit_m unit_m=null;
    
    private Object value=null;
    
    
    
    
    public Measure(){
        
    }
    
   
    

    public Unit_m getUnit_m() {
        return unit_m;
    }

    public void setUnit_m(Unit_m unit_m) {
        this.unit_m = unit_m;
    }


    public Type_m getType_m() {
        return type_m;
    }

    public void setType_m(Type_m type_m) {
        this.type_m = type_m;
    }
    

    public String getFormatDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return dateFormat.format(date);
    }
    
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.setDate(System.currentTimeMillis());
        this.value = value;
    }
    
    
            
    
}
