/*
 * Copyright [2014] [Università di Messina]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
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
package org.clever.Common.XMPPCommunicator;

import org.apache.log4j.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;



public class RoomListener implements PacketListener,Runnable
{

  private CleverMessageHandler receiver;
  private CleverMessage cleverMessage;
  // private ClusterCoordinator.ROOMS roomId;
  private Logger logger;



  public RoomListener(/*ClusterCoordinator.ROOMS id,*/ CleverMessageHandler cmh ) throws CleverException
  {
    receiver = cmh;
    //  roomId=id;
    logger = Logger.getLogger( "RoomListener" );
    logger.debug("roomlistener added: ");
  }



  @Override
  public void processPacket( Packet arg0 )
  {
    Message msg = ( Message ) arg0;
    logger.debug("messaggio xmpp: "+msg);
    this.cleverMessage=new CleverMessage(msg.getBody());
    //new Thread(this,"handleCleverMessage").start();
    receiver.handleCleverMessage(this.cleverMessage);
  }

    @Override
    public void run() {
        logger.debug("nel run");
        receiver.handleCleverMessage(this.cleverMessage);
        
    }
}
