 /*
 *  Copyright (c) 2011 Antonio Nastasi
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
package org.clever.administration.commands;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.VEInfo.CpuSettings;
import org.clever.Common.VEInfo.MemorySettings;
import org.clever.Common.VEInfo.StorageSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP.ROOM;
import org.clever.administration.ClusterManagerAdministrationTools;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.safehaus.uuid.UUIDGenerator;



public class GetNotifyPresence extends CleverCommand implements PacketListener
{
  private boolean notified=false;
  @Override
  public Options getOptions()
  {
    
    Options options = new Options();
    options.addOption( "timeout", true, "Timeout on waiting for notification." );
   
    return options;
  }



  @Override
  public synchronized void  exec( final CommandLine commandLine )
  {
        try {
            notified=false;
            ClusterManagerAdministrationTools.instance().getConnectionXMPP().addPresenceListener(ROOM.SHELL, this);
            while(!notified)
                wait();
        } catch (InterruptedException ex) {
            Logger.getLogger(GetNotifyPresence.class.getName()).log(Level.SEVERE, null, ex);
        }
   

  }



  @Override
  public void handleMessage( Object response )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

    @Override
    public synchronized void processPacket(Packet packet) {
        if (((Presence)packet).isAvailable())
        {
            System.out.println(packet.getFrom());
            ClusterManagerAdministrationTools.instance().getConnectionXMPP().removePresenceListener(ROOM.SHELL, this);
            notified=true;
            this.notifyAll();
        }
        
    }
     public void handleMessageError(CleverException e) {
        System.out.println(e);
    }
    
}