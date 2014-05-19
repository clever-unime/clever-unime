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
 *  The MIT License
 * 
 *  Copyright (c) 2013 Nicola Peditto
 *  Copyright (c) 2013 Carmelo Romeo
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package org.clever.Common.Measure;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



public abstract class Measure {
    
    private long date=0;
    
    private String timestamp = null;

    private String source=null;
    
    public enum Type_m {cpu, mem, sto, net, proc};
    private Type_m type_m=null;
    
    public enum Unit_m {percent, B, MB, GB, s, ns, timestamp, packet, text};
    private Unit_m unit_m=null;
    
    private Object value=null;
    
    
    
    
    public Measure(){
        
    }
    
   /**
   * Return CPU statistics to Clever shell
   * @param data timestamp in milliseconds (long)
   * @return return data in human readable format
   */    
    public String data_converter(long data){
        DateFormat formatter = new SimpleDateFormat("E MM dd hh:mm:ss a z yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(data);
        
        return formatter.format(calendar.getTime());
    }
    
    
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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
        
        this.setTimestamp(new Date().toString());
        
        this.value = value;
    }
    
    
            
    
}
