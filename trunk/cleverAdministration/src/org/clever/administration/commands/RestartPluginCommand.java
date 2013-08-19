/*
 *  The MIT License
 *
 *  Copyright (c) 2013 Tricomi Giuseppe
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.clever.administration.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 *
 * @author Giuseppe Tricomi <giu.tricomi@gmail.com>
 */
public class RestartPluginCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "xml", false, "Displays the XML request/response Messages." );
        options.addOption( "debug", false, "Displays debug information." );
        options.addOption("f",true,"XML file that contains new plugin configuration");
        options.addOption("a",true,"name of the agent we want to restart the plugin");
        options.addOption("h",true,"name of the CLEVER Entity we want to restart the plugin");
        options.addOption("w",true,"yes/no if yes write config file on cfg folder");
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
            String cfgfile=new String(buffer);
            String agentTarget=commandLine.getOptionValue("a");
            try {
                
                
                
                
                ArrayList params = new ArrayList();
                params.add(cfgfile);
                if(((String)commandLine.getOptionValue("w")).equals("yes")){
                    params.add(true);
                }
                else{
                    params.add(false);
                }
                String target =commandLine.getOptionValue("h");
                //String target =ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
                if (!target.equals("")) {
                    ClusterManagerAdministrationTools.instance().execAdminCommand(this, target, agentTarget, "restartPlugin", params, commandLine.hasOption("xml"));
                    //System.out.println(returnResponse);
                }
            } catch (CleverException ex) {
                logger.error(ex);
                if(commandLine.hasOption("debug"))
                     ex.printStackTrace();
                else
                    System.out.println(ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(RestartPluginCommand.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                f.close();
            } catch (IOException ex) {
                Logger.getLogger(RestartPluginCommand.class.getName()).log(Level.SEVERE, null, ex);
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
