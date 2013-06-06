/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
 *  Copyright (c) 2010 Marco Carbone
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
package org.clever.Common.Initiator;

import java.util.logging.Level;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import java.util.Random;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.ClusterCoordinator.ClusterCoordinator;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP.ROOM;



public class ElectionThread implements Runnable
{

  private ConnectionXMPP connection = null;
  private ClusterCoordinator clusterCoordinator = null;
  private Logger logger;
 


  public ElectionThread( final ConnectionXMPP connection, ClusterCoordinator clusterCoordinator)
  {
    this.connection = connection;
    this.clusterCoordinator = clusterCoordinator;
    logger = Logger.getLogger( "Election Thread for: " + connection.getUsername() );
  }
  
  @Override
  public void run()
  {
      logger.info("Switching a cluster manager active");
      
      try 
      {
          long milliseconds = Math.abs( ( new Random( System.currentTimeMillis() ) ).nextLong() % 10000 );
          Thread.sleep( milliseconds );
          
          Boolean cmActiveNotPresent = connection.getActiveCC(ROOM.CLEVER_MAIN).isEmpty(); //Verify if there is a CM active after the sleep of thread
          
          clusterCoordinator.setAsActiveCC(cmActiveNotPresent, clusterCoordinator.getActiveAgents()); //if there isn't a cm active this entry set it active!
      }
      catch (InterruptedException ex) 
      {
          java.util.logging.Logger.getLogger(ElectionThread.class.getName()).log(Level.SEVERE, null, ex);
      }     
      catch (CleverException ex) 
      {
          java.util.logging.Logger.getLogger(ElectionThread.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
}