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
 * @author giancarloalteri
 */
public class StorageCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
         options.addOption( "p", true, "The logical path." ); // path cleveriano
         options.addOption( "n", true, "The name of the Virtual Machine." );
         options.addOption( "h", true, "The name of the host manager." );
         options.addOption( "lock", true, "[0-NL] [1-CR] [2-CW] [3-PR] [4-PW] [5-EX]." ); // tipi di lock
         options.addOption( "debug", false, "Displays debug information." );
        // options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options; 
    }

    @Override
    public void exec(CommandLine commandLine) {
       try{
        
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        String targetHM=commandLine.getOptionValue("h");
        ArrayList params = new ArrayList();
        String logicname=commandLine.getOptionValue("p");
        String id=commandLine.getOptionValue("n");
        int lock=Integer.parseInt( commandLine.getOptionValue("lock"));
        params.add(logicname);
        params.add(id);
        params.add(targetHM);
        params.add(lock);

        ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "TestAgent", "storage1", params, commandLine.hasOption( "xml" ) );
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
        System.err.println("\n------------------------------------");
        System.out.println("\n"+response.toString());
        System.err.println("\n------------------------------------");
         
    }

   
    public void handleMessageError(CleverException response) {
        System.out.println(response);  
    }
    
}
