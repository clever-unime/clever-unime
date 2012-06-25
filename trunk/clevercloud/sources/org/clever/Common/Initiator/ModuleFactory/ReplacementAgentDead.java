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

import java.util.Map;
import java.util.Random;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;

/**This class is for recovery of the agents terminated prematurely. 
 * The only thing it does is call the createAgent of ModuleFactory
 * 
 * @author marco carbone
 */
public class ReplacementAgentDead implements Runnable
{
    private ModuleFactory mF;
    private  Process p; //quì istanzio il nuovo processo agente
    private String agentClassName; //quì memorizzo il nome della classe agente da istanziare
    private String agentName = ""; //quì memorizzo eventualmente il nome da dare all'agente, di default è vuoto
    private MonitorReplaceAgentDead monitor = null;
    private Map<String,MonitorReplaceAgentDead> monitorHash;
    
    ConnectionXMPP conn;  
    
    /**@deprecated
     * 
     * @param mF
     * @param p
     * @param monitorHash
     * @param agentClassName 
     */
    ReplacementAgentDead(ModuleFactory mF, Process p, Map<String,MonitorReplaceAgentDead> monitorHash, String agentClassName)   
    {
        this.mF = mF;
        this.p = p;
        this.agentClassName = agentClassName;     
        this.monitorHash = monitorHash;
        
    }
    
    ReplacementAgentDead(ModuleFactory mF, Process p,  Map<String,MonitorReplaceAgentDead> monitorHash, String agentClassName, String agentName)
    {
        this.mF = mF;
        this.p = p;
        this.agentClassName = agentClassName;
        this.agentName = agentName;        
        this.monitorHash = monitorHash;
        this.monitor = monitorHash.get(agentName);
    }
    
    
    @Override
    public void run()
    { 
        try 
        {
            p.waitFor(); 
         }
        catch (InterruptedException ex) 
        {
            //Logger.getLogger(ReplacementAgentDead.class.getName()).log(Level.SEVERE, null, ex);
        } 
        if(!Thread.currentThread().isInterrupted()) //dentro questo if si deve anche controllare che il cm esista ancora!
        {
            long milliseconds = Math.abs( ( new Random( System.currentTimeMillis() ) ).nextLong() % 10000 );
            
            try 
            {
                Thread.sleep( milliseconds ); //vado in attesa per un tempo casuale per veitare che tutti gli initiator del sistema lancino contemporanemente un CC andando così sopra la soglia!!
            } 
            catch (InterruptedException ex) 
            {
               // Logger.getLogger(ReplacementAgentDead.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(!Thread.currentThread().isInterrupted()) //dentro questo if si deve anche controllare che il cm esista ancora!
            {                 
                //se superiamo l'if il sistema è operativo!
                if(this.agentName.isEmpty())
                {
                    //prima di richiamare la create agent è necessario un controllo sugli errori riscontrati
                    //quì entra in gioco la classe MonitorReplaceAgentDead
                    monitor.setTime(System.currentTimeMillis()); //setto il tempo della chiamata 
                    if(monitor.check())
                    {
                        monitor.incrementNumLaunch(); //incremento
                        mF.createAgent(agentClassName);
                    }
                    else
                    {
                        monitor.reset();
                        //bisognerebbe però anche eliminare l'elemento dalla lista Hash map!!
                        monitorHash.remove(this.agentName);
                    }
                }
                else
                {
                    //prima di richiamare la create agent è necessario un controllo sugli errori riscontrati
                    //quì entra in gioco la classe MonitorReplaceAgentDead
                    monitor.setTime(System.currentTimeMillis()); //setto il tempo della chiamata 
                    if(monitor.check())
                    {
                        monitor.incrementNumLaunch();//incremento
                        mF.createAgent(agentClassName, agentName);
                    } 
                    else
                    {
                        monitor.reset();
                        //bisognerebbe però anche eliminare l'elemento dalla lista Hash map!!
                        monitorHash.remove(this.agentName);
                    }                                 
                }
            }
        } 
             
    }   
}
