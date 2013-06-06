/*
 * The MIT License
 *
 * Copyright 2011 Marco Carbone.
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

import org.apache.log4j.Logger;
import org.clever.ClusterManager.ClusterCoordinator.ClusterCoordinator;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP.ROOM;
import org.clever.HostManager.HostCoordinator.HostCoordinator;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

/**
 *
 * @author marco carbone
 */

/* Questa classe ha lo scopo di fare l'override del metodo left di PartecipantStatusListener
 * per dare la possibilità all'Initiator di monitorare la presenza del cluster manager e host manager da lui istanziati*/

public class Initiator_Listener implements ParticipantStatusListener
{
    
  private ConnectionXMPP conn = null;
  private Initiator init = null;
  private boolean flagCM;
  private boolean flagHM;
 
  private Logger logger;

  
  
  public Initiator_Listener(ConnectionXMPP conn, Initiator init, boolean flagCM, boolean flagHM)
  {
    logger = Logger.getLogger("Initiator_Listener");
    this.init = init;
    this.conn = conn;
    this.flagCM = flagCM;
    this.flagHM = flagHM;
  }

    @Override
    public void joined(String string) {
        //throw new UnsupportedOperationException("Not supported yet."); //l'ho commentata perché ogni volta ch si accedeva alla stanza veniva lanciata un eccezione!
    }

    /*METODO DI INTERESSE PER L'INITIATOR*/
    @Override
    public void left(String jid) 
    { //param jid: the jid of user left the room
        ThreadMonitoring initiator_th = new ThreadMonitoring("threadMonitor",this.conn, this.init, this.flagCM, this.flagHM);
        initiator_th.start();
    }

    @Override
    public void kicked(String string, String string1, String string2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void voiceGranted(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void voiceRevoked(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void banned(String string, String string1, String string2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void membershipGranted(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void membershipRevoked(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void moderatorGranted(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void moderatorRevoked(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void ownershipGranted(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void ownershipRevoked(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void adminGranted(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void adminRevoked(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void nicknameChanged(String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}