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
         options.addOption( "tn", true, "The template name of the Virtual Machine." );
         options.addOption( "h", true, "The name of the host manager." );
         //options.addOption( "n", true, "The name of the Virtual Machine." );
         //options.addOption( "lock", true, "[0-NL] [1-CR] [2-CW] [3-PR] [4-PW] [5-EX]." ); // tipi di lock
         options.addOption( "clonable", false, "Virtual Machine is cloned." );
         options.addOption( "exclusive", false, "Virtual Machine is exclusive." ); 
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
        String id=commandLine.getOptionValue("tn");
        String istanceName=commandLine.getOptionValue("n");
        org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode lock=org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.EX;
          if(commandLine.hasOption("clonable")){
              lock=org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.PR;
              
          }
          if(commandLine.hasOption("exclusive")){
              lock=org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile.lockMode.EX;
          }
        
        //int lock=Integer.parseInt( commandLine.getOptionValue("lock"));
        params.add(id);
        //params.add(istanceName);
        params.add(targetHM);
        org.clever.Common.Communicator.Utils.IstantiationParams containerParams=new org.clever.Common.Communicator.Utils.IstantiationParams(lock);
        params.add(containerParams);
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
