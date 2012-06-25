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
 * @author s89
 */
public class AttackInterfaceCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
         options.addOption( "n", true, "The name of the Virtual Machine." );
         options.addOption( "i", true, "The name of interface." );
         options.addOption("m",true,"mac-address.");
         options.addOption( "t", true, "The type of interface." );
         options.addOption( "debug", false, "Displays debug information." );
      //   options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options; 
    }

    @Override
    public void exec(CommandLine commandLine) {
        try{
        
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        String inf=commandLine.getOptionValue("i");
        ArrayList params = new ArrayList();
        String id=commandLine.getOptionValue("n");
        String type=commandLine.getOptionValue("t");
        params.add(id);
        params.add(inf);
        if(commandLine.hasOption("m")){
            String descr=commandLine.getOptionValue("m");
            params.add(descr);
        }
        else params.add("");
        params.add(type);
        ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "VirtualizationManagerAgent", "attackInterface", params, commandLine.hasOption( "xml" ));
        
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
        System.out.println("\n Interface attached");
        System.err.println("\n------------------------------------");
    }

   
    public void handleMessageError(CleverException response) {
        System.out.println(response);  
    }
    
}