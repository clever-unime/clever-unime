/*
 * The MIT License
 *
 * Copyright 2012 s89.
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
package org.clever.ClusterManager.VisionManagerPlugin.VisionManagerClever;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author s89
 */
public class VisionVmInfo {
private String name;
private List mac;
private HashMap mac_ip;
private List ip;


public VisionVmInfo(){
    mac=new ArrayList();
    mac_ip=new HashMap();
    ip=new ArrayList();
}

public VisionVmInfo(String name){
this();
this.name=name;
}


public void setName(String name){
    this.name=name;
}

public void addNetInterface(String ip,String mac){
    this.mac.add(mac);
    mac_ip.put(mac,ip);
    this.ip.add(ip);
}

public String getName(){
    return name;
}

public List getMac(){
    return mac;
}  

public HashMap getMac_Ip(){
    return mac_ip;
}
 
public List getIp(){
    return ip;
}

public String toXml(){
    String temp;
    Iterator it=mac.iterator();
        String st= "<vm>"
                  +"\n<name>"+name+"</name>";
        while(it.hasNext()){
            temp=(String) it.next();
            st=st+"\n <interface>"
                 +"\n"+"<mac>"+temp+"</mac>"
                 +"\n"+"<ip>"+(String)mac_ip.get(temp)+"</ip>"
                 +"\n </interface>";
        }
        st=st+"\n </vm>";
        return st;
    }
 
}
