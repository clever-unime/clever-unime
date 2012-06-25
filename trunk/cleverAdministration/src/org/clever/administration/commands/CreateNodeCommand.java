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
 * Creation of a clever node
 * @author giancarloalteri
 */
public class CreateNodeCommand extends CleverCommand{

    @Override
    public Options getOptions() {
         Options options = new Options();
         options.addOption( "p", true, "The logical path." );
         options.addOption( "debug", false, "Displays debug information." );
     //    options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
    
        try {
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        ArrayList params = new ArrayList();
        String logicname=commandLine.getOptionValue("p");
        params.add(logicname);
        params.add("dir");
        VFSDescription vfsD = new VFSDescription();
        params.add(vfsD);
        params.add("");
        
        ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "StorageManagerAgent", "createNode", params, commandLine.hasOption( "xml" ) );
      
        } catch (CleverException ex) {
            logger.error(ex);
            if(commandLine.hasOption("debug"))
                 ex.printStackTrace();
            else
                System.out.println(ex);
        }

    }

    @Override
    public void handleMessage(Object response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

 
   
    public void handleMessageError(CleverException response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
