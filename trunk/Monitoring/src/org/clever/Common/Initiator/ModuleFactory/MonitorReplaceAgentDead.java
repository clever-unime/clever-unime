/*
 * The MIT License
 *
 * Copyright 2012 Marco Carbone
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
package org.clever.Common.Initiator.ModuleFactory;

/**
 *
 * @author marco carbone
 */
public class MonitorReplaceAgentDead 
{
    private int IntervalTime; //tempo massimo x il rilancio da specificare in minuti
    private int numLaunchMAX; //numero di rilanci da effettuare in IntervalTime
    
    private long Interval; //tempo massimo x il rilancio specificato in millisecondi
    private long startTime; //tempo in cui viene eseguito il primo lancio
    private long launchTime; //tempo in cui vengono eseguiti i successivi lanci
    private int numLaunch;
     
    
    
    public MonitorReplaceAgentDead(int IntervalTime, int numLaunch, long startTime)
    {
        this.IntervalTime = IntervalTime;
        this.numLaunchMAX = numLaunch;        
        this.startTime = startTime;
        
        this.launchTime = 0;  
        this.numLaunch = 0;
        this.Interval = (this.IntervalTime)*60000; //1 min = 60000 millisec
    }
    
        
    public void incrementNumLaunch()
    {
        this.numLaunch++;
    }
    
    public long getNumLaunch()
    {
        return this.numLaunch;
    }
    
    public void setTime(long time)
    {
        this.launchTime = time;
    }
    
    public void reset()
    {
        this.startTime = 0;
        this.launchTime = 0;
        this.numLaunch = 0;
    }
    
    public boolean check()
    {
        long tmp = this.launchTime - this.startTime;
        if((tmp < this.Interval)&&(this.numLaunch < this.numLaunchMAX))
            return true; //OK
        else
        {
            reset();
            return false; //siamo fuori tempo
        }            
    }   
}
