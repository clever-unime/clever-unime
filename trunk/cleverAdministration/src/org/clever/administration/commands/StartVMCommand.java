 /*
 *  Copyright (c) 2011 Antonio Nastasi
 *  Copyright (c) 2012 Giancarlo Alteri
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */
package org.clever.administration.commands;

import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;

import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 * Starting a virtual machine
 * @author giancarloalteri
 */
public class StartVMCommand extends CleverCommand
{

  @Override
  public Options getOptions()
  {
    Options options = new Options();
    options.addOption( "n", true, "The name of the Virtual Environment." );
   // options.addOption( "xml", false, "Displays the XML request/response Messages." );
    options.addOption( "debug", false, "Displays debug information." );

    return options;
  }

  @Override
  public void exec( final CommandLine commandLine )
  {
    try
    {
      ArrayList params = new ArrayList();
      String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
      params.add(commandLine.getOptionValue("n"));
           
      ClusterManagerAdministrationTools.instance().execAdminCommand( this, target, "VirtualizationManagerAgent", "startVm", params, commandLine.hasOption( "xml" ) );
      //ClusterManagerAdministrationTools.instance().execAdminCommand( this, target, "HyperVisorAgent", "startVm", params, commandLine.hasOption( "xml" ) );
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
        System.out.println("VM successfully started");
  }

   
  
    public void handleMessageError(CleverException response) {
        System.out.println("Failed to start VM"); 
        System.out.println(response);
    }  
}