/*
 * The MIT License
 *
 * Copyright 2011 Marco Carbone
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
package org.clever.Common.Initiator;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP.ROOM;

/**
 *
 * @author Marco Carbone
 */

/*
 * Questa classe genera un thread che viene lanciato dall'initiator ed ha il compito di
 * controllare la presenza nella room del CM lanciato dall'initiator ed eventualmente
 * rilanciarlo!
 */
public class ThreadMonitoring extends Thread
{
  private ConnectionXMPP connection = null;
  private Initiator init = null;
  private boolean flagCM;
  private boolean flagHM;
  
  private Logger logger;
    
  public ThreadMonitoring(String nomeTh, ConnectionXMPP conn, Initiator init, boolean flagCM, boolean flagHM)
  {
      super(nomeTh);//do un nome al thread!
      this.connection = conn;
      this.init = init;
      this.flagCM = flagCM;
      this.flagHM = flagHM;
      
      logger = Logger.getLogger( "Thread Monitoring for: " + connection.getUsername() );
      
  }
  
  private void MonitorCM(boolean flag) throws InterruptedException
  {
      if(flag) //se l'initiator è abilitato al lancio dei CM:
        {
             if(!this.connection.SearchCM_InRoom(init.getNickCM(), ROOM.CLEVER_MAIN))
            {
                
                if(this.connection.getNum_CCsInRoom(ROOM.CLEVER_MAIN) < init.getTH()) //check about number of CM is < threshold
                {
                    long milliseconds = Math.abs( ( new Random( System.currentTimeMillis() ) ).nextLong() % 10000 );
                    Thread.sleep( milliseconds ); //vado in attesa per un tempo casuale per veitare che tutti gli initiator del sistema lancino contemporanemente un CC andando così sopra la soglia!!
                    
                    if(this.connection.getNum_CCsInRoom(ROOM.CLEVER_MAIN) < init.getTH()) //al suo risveglio ogni init  rifà il controllo sulla soglia!
                    {
                        try 
                        {
                            init.launchClusterCoordinator(init.getCP());
                        } 
                        catch (CleverException ex) 
                        {
                            Logger.getLogger(ThreadMonitoring.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }        
            }
        }
  }
  
  //ALLO STESSO MODO ANDREBBE PREVISTA UNA FUNZIONE MONITOR hm!!!

    @Override
    public void run() 
    {
        logger.info("Start Thread Monitor about Cluster manager alive in room CLEVER_MAIN");
        try 
        {
            this.MonitorCM(flagCM);
        }
        catch (InterruptedException ex) 
        {
           //eccezione non gestita!
        }
    }
}