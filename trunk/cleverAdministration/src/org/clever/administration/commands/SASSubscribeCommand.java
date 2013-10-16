/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author alessiodipietro
 */
public class SASSubscribeCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "xml", false, "Displays the XML request/response Messages." );
        options.addOption( "debug", false, "Displays debug information." );
        options.addOption("f",true,"XML file that contains Subscribe Request");
        return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
        BufferedInputStream f = null;
        try {
            String returnResponse;
            String filePath=commandLine.getOptionValue("f");
            byte[] buffer = new byte[(int) new File(filePath).length()];
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
            String subscribeRequest=new String(buffer);
            
            try {
                
                
                
                
                ArrayList params = new ArrayList();
                params.add(subscribeRequest);
                
                
                String target =ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
                if (!target.equals("")) {
                    returnResponse=(String)ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "SASAgent", "subscribe", params, commandLine.hasOption("xml"));
                    System.out.println(returnResponse);
                }
            } catch (CleverException ex) {
                logger.error(ex);
                if(commandLine.hasOption("debug"))
                     ex.printStackTrace();
                else
                    System.out.println(ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(SASSubscribeCommand.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                f.close();
            } catch (IOException ex) {
                Logger.getLogger(SASSubscribeCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
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
