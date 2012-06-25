/*
 * The MIT License
 *
 * Copyright 2012 giancarloalteri.
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
package org.clever.HostManager.ImageManagerPlugins.ImageManagerClever;

/**
 * This class handles the mechanics of the lock
 * @author giancarloalteri
 */
public class LockFile {
    
public static String risposta="";    
    
public interface Mode {
public static final int NL = 0;
public static final int CR = 1;
public static final int CW = 2;
public static final int PR = 3;
public static final int PW = 4;
public static final int EX = 5;
}
/**
 * 
 * @param currentLock the current lock on the replication (0 to 5)
 * @param newLock new lock that you want to apply (0 to 5)
 * @return 
 */    
public String checkLock(int currentLock,int newLock){

switch (currentLock) {
case Mode.NL: 
                risposta="SI";
                break;
case Mode.CR: 
                if(newLock==Mode.EX)
                risposta="NO";
                else
                risposta="SI";    
                break; 
case Mode.CW: 
                if(newLock==Mode.EX || newLock==Mode.PW || newLock==Mode.PR)
                risposta="NO";
                else
                risposta="SI";
                break; 
case Mode.PR: 
                if(newLock==Mode.EX || newLock==Mode.PW || newLock==Mode.CW)
                risposta="NO";
                else
                risposta="SI";
                break;
case Mode.PW: 
                if(newLock==Mode.NL || newLock==Mode.CR)
                risposta="SI";
                else
                risposta="NO";
                break;    
case Mode.EX: 
                if(newLock==Mode.NL) 
                    risposta="SI";
                else
                    risposta="NO";
                break;     
    }
return risposta;
        
}
}
