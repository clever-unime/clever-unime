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
 * Creation of a virtual machine
 * @author giancarloalteri
 */
public class CreateVMCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
         options.addOption( "n", true, "The name of the Virtual Machine." );
         options.addOption( "h", true, "The name of the host manager." );
         //options.addOption( "lock", true, "[0-NL] [1-CR] [2-CW] [3-PR] [4-PW] [5-EX]." ); // tipi di lock
    //     options.addOption( "clonable", false, "Virtual Machine is cloned." );
   //      options.addOption( "exclusive", false, "Virtual Machine is exclusive." ); 
         options.addOption( "debug", false, "Displays debug information." );
      //   options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options; 
    }

    @Override
    public void exec(CommandLine commandLine) {
        try{
        
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        String targetHM=commandLine.getOptionValue("h");
        ArrayList params = new ArrayList();
        String id=commandLine.getOptionValue("n");
        int lock=5;
   /*       if(commandLine.hasOption("clonable")){
              lock=3;
              
          }
          if(commandLine.hasOption("exclusive")){
              lock=5;
          }*/
        
        //int lock=Integer.parseInt( commandLine.getOptionValue("lock"));
        params.add(id);
        params.add(targetHM);
        params.add(lock);
        ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "VirtualizationManagerAgent", "createVM", params, commandLine.hasOption( "xml" ) );
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
        System.err.println("\n------------------------------------");
        System.out.println("\n"+response.toString());
        System.err.println("\n------------------------------------");
    }

   
    public void handleMessageError(CleverException response) {
        System.out.println(response);  
    }
    
}
