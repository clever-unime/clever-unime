/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.commands;

import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Storage.VFSDescription;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 * Creation of a link node
 * @author giancarloalteri
 */
public class CreateLinkCommand extends CleverCommand{

    @Override
    public Options getOptions() {
         Options options = new Options();
         options.addOption( "n", true, "The link name." );
         options.addOption( "p", true, "The logical path." );
         options.addOption( "debug", false, "Displays debug information." );
      //   options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options;    }

    @Override
    public void exec(CommandLine commandLine) { 
        try {
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        ArrayList params = new ArrayList();
        String linkname=commandLine.getOptionValue("n");
        String logicname=commandLine.getOptionValue("p"); 
        params.add(linkname);
        params.add("link");
        VFSDescription vfsD = new VFSDescription();
        params.add(vfsD);
        params.add(logicname);
        ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "StorageManagerAgent", "createNode", params, commandLine.hasOption( "xml" ) );
      
        } catch (CleverException ex) {
            logger.error(ex);
            if(commandLine.hasOption("debug"))
                 ex.printStackTrace();
            else
                System.out.println(ex);
        }    }

    @Override
    public void handleMessage(Object response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

 
    public void handleMessageError(CleverException response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
