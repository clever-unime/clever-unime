 /*
 *  Copyright (c) 2011 Antonio Nastasi
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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.VEInfo.CpuSettings;
import org.clever.Common.VEInfo.MemorySettings;
import org.clever.Common.VEInfo.StorageSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.administration.ClusterManagerAdministrationTools;
import org.safehaus.uuid.UUIDGenerator;



public class TakeSnapshot extends CleverCommand
{

  @Override
  public Options getOptions()
  {
    

    Options options = new Options();
   
    
    
    options.addOption( "h", true, "The name of the host manager." );
    options.addOption( "n", true, "The name of the Virtual Environment." );
    options.addOption( "s", true, "The name of the Snapshot." );
    options.addOption( "d", true, "The description of snapshot." );
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
            String target = commandLine.getOptionValue( "h" );

      
       
           String id = commandLine.getOptionValue( "n" );
           String name = commandLine.getOptionValue( "s" );
           String description = null;
           if(commandLine.hasOption("d"))
               description = commandLine.getOptionValue( "d" );
           else
               description = "";


            // Create the list of the parameter for the method that will be invoked
            params.add( id );
            params.add( name );
            params.add( description );


            System.out.println("return: "+ClusterManagerAdministrationTools.instance().execSyncAdminCommand( this,
                                target,
                                "HyperVisorAgent",
                                "takeSnapshot",
                                params,
                                commandLine.hasOption( "xml" ) )
                                );
            
     
           
        
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
  public void handleMessage( Object response )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }
   public void handleMessageError(CleverException e) {
        System.out.println(e);
    }
}