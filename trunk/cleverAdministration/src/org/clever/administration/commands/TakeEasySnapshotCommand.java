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
 * @author salvatore sabato
 */
public class TakeEasySnapshotCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
         options.addOption( "n", true, "The name of the Virtual Machine." );
         options.addOption( "s", true, "The name of snapshot." );
         options.addOption("h",true,"Taget host.");
         options.addOption( "descr", false, "Snapshot Description." );
         options.addOption( "debug", false, "Displays debug information." );
       //  options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options; 
    }

    @Override
    public void exec(CommandLine commandLine) {
        try{
        
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        String names=commandLine.getOptionValue("s");
        ArrayList params = new ArrayList();
        String id=commandLine.getOptionValue("n");
        String HMTarget=commandLine.getOptionValue("h");
        params.add(id);
        params.add(names);
        if(commandLine.hasOption("descr")){
            String descr=commandLine.getOptionValue("descr");
            params.add(descr);
        }
        else params.add("");
        params.add(HMTarget);
        ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, "VirtualizationManagerAgent", "TakeEasySnapshot", params, commandLine.hasOption( "xml" ) );
        
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
        System.out.println("snapshot succesfully created ");
    }

   
    public void handleMessageError(CleverException response) {
        System.out.println("failed to take snapshot");
        System.out.println(response);  
    }
    
}