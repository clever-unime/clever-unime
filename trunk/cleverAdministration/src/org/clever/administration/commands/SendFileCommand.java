/*
 *  Copyright (c) 2011 Francesco Longo
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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 *
 * @author mizar
 */

public class SendFileCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "f", true, "The local path of the file to be sent" );
        options.addOption( "r", true, "The remote path where the file has to be saved" );
        return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
            ArrayList params = new ArrayList();
            params.add( commandLine.getOptionValue( "r" ) );
            String destination_jid;
            String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);

            if (!target.equals("")) {
                    try {
                        destination_jid = (String) ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "DispatcherAgent", "receiveFile", params, commandLine.hasOption("xml"));
                        String local_path = commandLine.getOptionValue( "f" );
                        ClusterManagerAdministrationTools.instance().getConnectionXMPP().sendFile( destination_jid, local_path );
                    } catch (CleverException ex) {
                        System.out.println("Eccezione:" + ex);
                    }
            }
    }

    @Override
    public void handleMessage(Object response) {
        System.out.println(response);
    }
    
     public void handleMessageError(CleverException e) {
        System.out.println(e);
    }

}

