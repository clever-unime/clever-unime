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
 * @author Francesco Antonino Manera
 */
public class SASServiceCommand extends CleverCommand {

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "xml", false, "Displays the XML request/response Messages." );
        options.addOption( "debug", false, "Displays debug information." );
        options.addOption("c",true,"The Command");
        //options.addOption("s", "section",true, "The Section");
        //options.getOption("s").setArgs(4);
        return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
               String returnResponse;
        String[] sections;

        try {
            
            sections=commandLine.getOptionValues("c");

            ArrayList params = new ArrayList();
            params.add(sections[0]);
            String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
            if (!target.equals("")){

                
                
                
//              
                returnResponse =(String)ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "SASAgent", "servicecommand", params, commandLine.hasOption("xml"));
                System.out.println("\n---------ServiceCommand----------");
                System.out.println(returnResponse);
                System.out.println("\n-------------------------------");
            }
            else{
            System.out.println("\n ServiceCommand Error, target null !!!\n");
            }
        } catch (CleverException ex) {
            logger.error("CleverException"+ex);
            if(commandLine.hasOption("debug"))
                 ex.printStackTrace();
            else
                System.out.println(ex);
        }
    }

    @Override
    public void handleMessage(Object response) {
        System.out.println(response);
    }

    @Override
    public void handleMessageError(CleverException e) {
        System.out.println("\n Error:"+e.getMessage());
    }
    
}
