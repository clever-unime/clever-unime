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

/**
 *
 * @author webwolf
 */
    

public class StorageM extends Measure{
    
    public enum SubType_m {total, used, free, read, write};
    private SubType_m stype_m=null;
    
    private String partition=null;
    private String filesystem=null;
    
    public StorageM(StorageM.SubType_m stype_m, Measure.Unit_m unit_m, String partition, String filesystem){
        
        super();

        super.setUnit_m(unit_m);
        
        super.setType_m(Measure.Type_m.sto);
        
        this.filesystem=filesystem;
        
        this.stype_m=stype_m;
        
        this.partition=partition;
        
        this.setF_date(super.getFormatDate(super.getDate()));
            
       
    }
    
    @Override
    public String toString(){
        String str=this.stype_m + " " + this.getValue().toString();
        return str;
    }    
}
