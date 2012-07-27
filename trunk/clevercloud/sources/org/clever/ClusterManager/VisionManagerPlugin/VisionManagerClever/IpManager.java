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

import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.VisionException;




/**
 *
 * @author s89
 */
public class IpManager {
    
    private static Long ipmin;
    private static Long ipmax;
    private static Long currentip;
    
    
    public IpManager(String min,String max){
        
        IpManager.ipmin=ipToInt(min);
        IpManager.ipmax=ipToInt(max);
        IpManager.currentip=ipToInt(min);
     }
    
    public static void setCurrentIp(String ip){
        IpManager.currentip=ipToInt(ip);
        
    }
    
    public static void setMaxIp(String ipmax){
        IpManager.ipmax=ipToInt(ipmax);
    }
    
    public static void setMinIp(String ipmin){
        IpManager.ipmin=ipToInt(ipmin);
    }
    
    public static String getMaxIp(){
        return intToIp(ipmax);
    }
    
    public static String getMinIP(){
        return intToIp(ipmin);
    }
    
    public static String getCurrentIp(){
        return intToIp(currentip);
    }
    
    public static String getBlockIp(int number) throws VisionException{
        if((IpManager.currentip+number)>IpManager.ipmax)
            throw new VisionException("out of ip range");
            IpManager.currentip=IpManager.currentip+number;
            return intToIp(IpManager.currentip-number);
            
        
    }
    
    public static String intToIp(Long i) {
             return ((i >> 24 ) & 0xFF) + "." +
             ((i >> 16 ) & 0xFF) + "." +
             ((i >> 8 ) & 0xFF) + "." +
             ( i & 0xFF);
             }
 
    
    public static Long ipToInt(String addr) {
        String[] addrArray = addr.split("\\.");
        long num = 0;
        for (int i=0;i<addrArray.length;i++) {
             int power = 3-i;
             num += ((Integer.parseInt(addrArray[i])%256 * Math.pow(256,power)));
            }
        return num;
       }
    
public static String getNextIp(String ip){
    Long c_ip=ipToInt(ip);
    c_ip++;
    return intToIp(c_ip);
}
public static String macCalculator(int cl, int host,int inter){
    
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%s%s",52,":"));
    sb.append(String.format("%s%s",54,":"));
    sb.append(String.format("%02x%s",inter,":"));
    sb.append(String.format("%01x",cl));
    sb.append(String.format("%01x%s",0,":"));
    sb.append(String.format("%02x%s",0,":"));
    sb.append(String.format("%02x",host));
    return sb.toString();
    }
    


     
     
    
    
             
    


public static void main(String args[]) throws CleverException{
        String mac = IpManager.macCalculator(8,15,2);
       
    System.out.format(mac);
        
}
    
}
