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




public class GetMeasureCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "h", false, "The name of the HostManager." );
        
        
        return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
        
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        
        ArrayList params = new ArrayList();
        
        if(commandLine.hasOption("h")){
            
            String hm=commandLine.getOptionValue("h");
            params.add(hm);
            
        }

        
        try{
            
            ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "MonitorManagerAgent", "getCpuIdle", params, commandLine.hasOption( "xml" ) );

        }
        catch (CleverException ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        
    }

    @Override
    public void handleMessage(Object response) {
       System.out.println( "\n---------Measures----------" );
       System.out.println(response.toString());
       System.out.println( "\n---------------------------" );
    }

    @Override
    public void handleMessageError(CleverException e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
