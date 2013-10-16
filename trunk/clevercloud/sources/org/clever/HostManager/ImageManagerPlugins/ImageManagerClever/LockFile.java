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
    
private static String risposta="";    
/*    
public interface Mode {
public static final int NL = 0;
public static final int CR = 1;
public static final int CW = 2;
public static final int PR = 3;
public static final int PW = 4;
public static final int EX = 5;
}
*/
public enum lockMode {NL,CR,CW,PR,PW,EX};
/**
 * 
 * @param currentLock the current lock on the replication (0 to 5)
 * @param newLock new lock that you want to apply (0 to 5)
 * @return 
 */    
public String checkLock(lockMode currentLock,lockMode newLock){

switch (currentLock) {
case NL: 
                risposta="SI";
                break;
case CR: 
                if(newLock==lockMode.EX)
                risposta="NO";
                else
                risposta="SI";    
                break; 
case CW: 
                if(newLock==lockMode.EX || newLock==lockMode.PW || newLock==lockMode.PR)
                risposta="NO";
                else
                risposta="SI";
                break; 
case PR: 
                if(newLock==lockMode.EX || newLock==lockMode.PW || newLock==lockMode.CW)
                risposta="NO";
                else
                risposta="SI";
                break;
case PW: 
                if(newLock==lockMode.NL || newLock==lockMode.CR)
                risposta="SI";
                else
                risposta="NO";
                break;    
case EX: 
                if(newLock==lockMode.NL) 
                    risposta="SI";
                else
                    risposta="NO";
                break;     
    }
org.apache.log4j.Logger.getLogger("LockFile").debug("item \"risposta\" is:"+risposta);
return risposta;
        
}
/**
 * This function returns an appropiate string representation of lockMode l
 * @param l
 * @return 
 */
    public static String getLockType(lockMode l){
        String risposta="";
        switch(l){
           case NL:{
                risposta="Null Lock";
                return risposta;
            }
            case CR:{
                risposta="Concurrent Read";
                return risposta;
            }
            case CW:{
                risposta="Concurrent Write";
                return risposta;
            }
            case PR:{
                risposta="Protected Read";
                return risposta;
            }
            case PW:{
                risposta="Protected Write";
                return risposta;
            }
            case EX:{
                risposta="Exclusive";
                return risposta;
            }
        }
        return risposta;
    }
    /**
     * This function returns the apropiate lockmode indicate by string representation,
     * or null if representation don't match with a lockmode available.  
     * @param representation
     * @return 
     */
    public static lockMode getLockEnumType(String representation){
        if(representation.isEmpty())
            return null;
        else if(representation.equals("Null Lock"))
            return lockMode.NL;
        else if(representation.equals("Concurrent Read"))
            return lockMode.CR;
        else if(representation.equals("Concurrent Write"))
            return lockMode.CW;
        else if(representation.equals("Protected Read"))
            return lockMode.PR;
        else if(representation.equals("Protected Write"))
            return lockMode.PW;
        else if(representation.equals("Exclusive"))
            return lockMode.EX;
        else
            return null;
    }
}
