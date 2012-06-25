/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.commands;

import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 * Lists the contents of a node of the catalog
 * @author giancarloalteri
 */
public class LsCommand extends CleverCommand{

    @Override
    public Options getOptions() {
         Options options = new Options();
         options.addOption( "p", true, "The logical path." );
         options.addOption( "debug", false, "Displays debug information." );
        // options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options;    }

    @Override
    public void exec(CommandLine commandLine) {
      try {
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        ArrayList params = new ArrayList();
        String logicname=commandLine.getOptionValue("p");
        params.add(logicname);
        String result=(String) ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "StorageManagerAgent", "ls", params, commandLine.hasOption( "xml" ) );
        System.err.println("\n--------Logical Catalog--------");
        System.out.println(result);  
        System.err.println("-------------------------------");   
        } 

      catch (CleverException ex) {
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
