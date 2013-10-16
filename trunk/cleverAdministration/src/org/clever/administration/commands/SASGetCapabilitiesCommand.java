/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.commands;

import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 *
 * @author alessiodipietro
 */
public class SASGetCapabilitiesCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption( "xml", false, "Displays the XML request/response Messages." );
        options.addOption( "debug", false, "Displays debug information." );
        options.addOption("s",true,"The Section");
        //options.addOption("s", "section",true, "The Section");
        //options.getOption("s").setArgs(4);
        return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
        String returnResponse;
        String[] sections;
        String sectionsXml="";
        String getCapabilitiesRequest;
        try {
            
            sections=commandLine.getOptionValues("s");
            logger.debug("sections= "+sections[0]);
            for(int i=0;i<sections.length;i++){
                sectionsXml+="<Section>"+sections[i]+"</Section>";
            }
            
            getCapabilitiesRequest="<GetCapabilities><Sections>"
                    + sectionsXml+"</Sections></GetCapabilities>";
            ArrayList params = new ArrayList();
            params.add(getCapabilitiesRequest);
            logger.info("getCapabilitier Request= "+getCapabilitiesRequest);
            String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
            if (!target.equals("")){
                logger.info("FRANCESCO: target="+target);
                //prova
                // params.add(true);
                returnResponse="prova";
                //fine prova
                
                
                
//                System.out.println("\n target="+target);
//                System.out.println("\n param="+params.get(0));
               // logger.debug(ClusterManagerAdministrationTools.instance().execSyncAdminCommand( this, target, "SASAgent", "test", params, commandLine.hasOption( "xml" ) ));
                returnResponse =(String)ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "SASAgent", "getCapabilities", params, commandLine.hasOption("xml"));
                System.out.println("\n---------GetCapabilities----------");
                System.out.println(returnResponse);
                System.out.println("\n-------------------------------");
            }
            else{
            System.out.println("\n GetCapabilities Error, target null !!!\n");
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
