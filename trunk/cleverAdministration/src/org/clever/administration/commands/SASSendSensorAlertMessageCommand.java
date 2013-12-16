/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 *
 * @author alessiodipietro
 */
public class SASSendSensorAlertMessageCommand extends CleverCommand {

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "xml", false, "Displays the XML request/response Messages." );
        options.addOption( "debug", false, "Displays debug information." );
        options.addOption("h",true,"The name of the Host Manager");
        options.addOption("latitude",true,"The latitude");
        options.addOption("altitude",true,"The altitude");
        options.addOption("longitude",true,"The longitude");
        options.addOption("f",true,"The file that contains AlertMessageStructure");
        return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
        String returnResponse;
        String agent;
        BufferedInputStream f = null;
        try {
            String filePath=commandLine.getOptionValue("f");
            byte[] buffer = new byte[(int) new File(filePath).length()];
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
            String alertMessageStructure=new String(buffer);
            
            Double latitude=Double.parseDouble(commandLine.getOptionValue("latitude"));
            Double altitude=Double.parseDouble(commandLine.getOptionValue("altitude"));
            Double longitude=Double.parseDouble(commandLine.getOptionValue("longitude"));
            Random generator = new Random();
            String value=Double.toString(generator.nextDouble() * 200);
            
            ArrayList params = new ArrayList();
            params.add(latitude);
            params.add(longitude);
            params.add(altitude);
            params.add(value);
            params.add(alertMessageStructure);
            
            String target =commandLine.getOptionValue("h");
            if (!target.equals("")) {
                ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "SASAgentHm", "sendSensorAlertMessage", params, commandLine.hasOption("xml"));
                
            }
        } catch (IOException ex) {
            Logger.getLogger(SASSendSensorAlertMessageCommand.class.getName()).log(Level.SEVERE, null, ex);
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
