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
 * @author alessiodipietro
 */
public class SASRenewSubscriptionCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "xml", false, "Displays the XML request/response Messages." );
        options.addOption( "debug", false, "Displays debug information." );
        options.addOption("s",true,"The subscription id");
        return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
        String returnResponse;
        String agent;
        String subscriptionId=commandLine.getOptionValue("s");
        String subscribeRequest="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<RenewSubscription>"
                + "<SubscriptionID>"+subscriptionId+"</SubscriptionID>"              
                + "</RenewSubscription>";
        try {
            
            
            
            
            ArrayList params = new ArrayList();
            params.add(subscribeRequest);
            
            
            String target =ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
            if (!target.equals("")) {
                returnResponse=(String)ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "SASAgent", "renewSubscription", params, commandLine.hasOption("xml"));
                System.out.println(returnResponse);
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
