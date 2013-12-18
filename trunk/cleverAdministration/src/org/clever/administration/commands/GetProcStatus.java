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




public class GetProcStatus extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "h", true, "The name of the HostManager/Probe." );
        options.addOption( "p", true, "The name of the process (single process)." );
        options.addOption( "debug", false, "Displays debug information." );
        
        return options;
        
    }

    
    
    @Override
    public void exec(CommandLine commandLine) {
        
        try{
            String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);

            ArrayList params = new ArrayList();

            //if(commandLine.hasOption("h")){

                String hm=commandLine.getOptionValue("h");
                params.add(hm);
                
                String procname=commandLine.getOptionValue("p");
                params.add(procname);

            //}
            
            ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "MonitorManagerAgent", "getProcStatus", params, commandLine.hasOption( "xml" ) );

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
       System.out.println( "\n---------Measures----------" );
       System.out.println(response.toString());
       System.out.println( "\n---------------------------" );
    }

    @Override
    public void handleMessageError(CleverException e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
