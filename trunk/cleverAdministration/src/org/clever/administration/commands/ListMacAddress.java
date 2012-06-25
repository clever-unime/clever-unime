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
public class ListMacAddress extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "n", true, "The name of the Virtual Machine." );
        return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        ArrayList params = new ArrayList();
        String id=commandLine.getOptionValue("n");
        params.add(id);
        try{
         ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "VirtualizationManagerAgent", "listMac_address", params, commandLine.hasOption( "xml" ) );
        //ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "TestAgent", "simVirtualizationManager", params, commandLine.hasOption( "xml" ) );
           }
        catch (CleverException ex) {
            logger.error(ex);
            ex.printStackTrace();
          }
        
    }

    @Override
    public void handleMessage(Object response) {
       System.out.println( "\n---------MacAddresses----------(sync)" );
       System.out.println(response.toString());
       System.out.println( "\n-------------------------------(sync)" );
    }

    @Override
    public void handleMessageError(CleverException e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
