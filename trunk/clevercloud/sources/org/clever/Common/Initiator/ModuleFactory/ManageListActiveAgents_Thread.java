/*
 * The MIT License
 *
 * Copyright 2012 Marco Carbone.
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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The instance of this class will run in a separate thread.
 * Its purpose is to synchronize the list of active processes with the list of process names, 
 * callable in cleverAdministration using the command "listactiveagents".
 * Every time a new agent is launched, its name is added to the list of names and 
 * a thread of this type is launched. 
 * When the agent process will end, this thread will wake up and delete its name from the list.
 *
 * @author marco carbone
 */
public class ManageListActiveAgents_Thread implements Runnable
{
    private ArrayList<String> list = null; //Array name list of process agent activated
    private Process p = null;
    private String name = "";
    
    public ManageListActiveAgents_Thread(Process p, String name, ArrayList<String> list)
    {
        this.p = p;
        this.name = name;
        this.list = list;        
    }
    
    
    @Override
    public void run()
    {
        list.add(name);
        try 
        {
            p.waitFor(); //this thread wait until the process agent ends
            list.remove(name); //then delete it from the list of name           
        } 
        catch (InterruptedException ex) 
        {
            Logger.getLogger(ManageListActiveAgents_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
}
