/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.commands;

import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 *
 * @author alessiodipietro
 */
public class SASCancelAdvertisementCommand extends CleverCommand {

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "xml", false, "Displays the XML request/response Messages." );
        options.addOption( "debug", false, "Displays debug information." );
        options.addOption("h",true,"The name of the Host Manager");
        options.addOption("r",true,"The request ID");
        return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
        String returnResponse;
        String agent;
        try {
            
            String requestId=commandLine.getOptionValue("r");
            ArrayList params = new ArrayList();
            params.add(requestId);
            String target =commandLine.getOptionValue("h");
            if (!target.equals("")) {
                ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "SASAgentHm", "cancelAdvertisement", params, commandLine.hasOption("xml"));
                
            }
        } catch (CleverException ex) {
            logger.error(ex);
            if(commandLine.hasOption("debug"))
                 ex.printStackTrace();
            else
                System.out.println(ex);
        }
    }

    @Override
    public void handleMessage(Object response) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void handleMessageError(CleverException e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
