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




public class GetStorageStatus extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "h", true, "The name of the HostManager/Probe." );
        options.addOption( "debug", false, "Displays debug information." );
        
        return options;
        
    }

    
    
    @Override
    public void exec(CommandLine commandLine) {
        
        try{
            String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);

            ArrayList params = new ArrayList();



            String hm=commandLine.getOptionValue("h");
            params.add(hm);
                
            
            ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "MonitorManagerAgent", "getStorageStatus", params, commandLine.hasOption( "xml" ) );

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
       System.out.println( "\n---------Storage----------" );
       System.out.println(response.toString());
       System.out.println( "\n---------------------------" );
    }

    @Override
    public void handleMessageError(CleverException e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
