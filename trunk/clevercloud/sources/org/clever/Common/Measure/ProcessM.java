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

import java.util.ArrayList;
import java.util.List;

/*
class Credential{
    
    String user=null;
    String group=null;
    
    public Credential (String user, String group){
        this.user=user;
        this.group=group;
    }
    
}   
*/

public class ProcessM extends Measure{
    
    public enum SubType_m {cpu, mem, user, group, state, time};
    private SubType_m stype_m=null;
   
    
    //private List cpuperc =new ArrayList();
    //private Credential cred =null;
    
    public ProcessM(ProcessM.SubType_m stype_m, Measure.Unit_m unit_m){
        
        super();

        super.setUnit_m(unit_m);
        
        super.setType_m(Measure.Type_m.proc);
       
        this.stype_m=stype_m;
        

        /*
        this.cpuperc.add("100");
        this.cpuperc.add(unit_m); 
        this.cred=new Credential("web","webgroup");  
        * */
       
    }
    
    @Override
    public String toString(){
        String str=this.stype_m + " " + this.getValue().toString();
        return str;
    }    
}
