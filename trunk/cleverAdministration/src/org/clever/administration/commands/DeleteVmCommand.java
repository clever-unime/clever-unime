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
 *
 * @author roberto
 */
public class DeleteVmCommand extends CleverCommand {
      @Override
    public Options getOptions() {
        Options options = new Options();
         options.addOption( "n", true, "The name of the Virtual Machine." );
         options.addOption( "debug", false, "Displays debug information." );
       //  options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options; 
    }

    @Override
    public void exec(CommandLine commandLine) {
        try{
        
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        ArrayList params = new ArrayList();
        String id=commandLine.getOptionValue("n");

        params.add(id);
        
        ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "VirtualizationManagerAgent", "deleteVm", params, commandLine.hasOption( "xml" ) );
        //ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "TestAgent", "simVirtualizationManager", params, commandLine.hasOption( "xml" ) );
           }
        catch (CleverException ex) {
            logger.error(ex);
            if(commandLine.hasOption("debug"))
                 ex.printStackTrace();
            else
                System.out.println(ex);
        }    }

    @Override
    public void handleMessage(Object response) {
       System.out.println("succesfully deleted");
    }

   
    public void handleMessageError(CleverException response) {
        System.out.println("failed to delete VM");
        System.out.println(response);  
    }
    
}
