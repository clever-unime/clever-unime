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
 * @author roberto
 */
public class ResumeVMcommand extends CleverCommand {
    
    public Options getOptions()
  {
    Options options = new Options();
    options.addOption( "n", true, "The name of the Virtual Environment." );
   // options.addOption( "xml", false, "Dispalys the XML request/response Messages." );
    options.addOption( "debug", false, "Displays debug information." );
    return options;
  }
 public void exec( final CommandLine commandLine )
  { 
    try
    {
      ArrayList params = new ArrayList();
      String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
      params.add(commandLine.getOptionValue("n"));
           
      ClusterManagerAdministrationTools.instance().execAdminCommand( this, target, "VirtualizationManagerAgent", "resumeVm", params, commandLine.hasOption( "xml" ) );
       
    }
    catch( CleverException ex )
    {
     if(commandLine.hasOption("debug"))
     {
                 ex.printStackTrace();          
     }
            else
                System.out.println(ex);
      logger.error( ex );
    }
  }

   @Override
  public void handleMessage(Object response){
    System.out.println("VM successfully resumed");
  }

   
    public void handleMessageError(CleverException response) {
        System.out.println("Failed to resume VM"); 
        System.out.println(response);
    } 
}


