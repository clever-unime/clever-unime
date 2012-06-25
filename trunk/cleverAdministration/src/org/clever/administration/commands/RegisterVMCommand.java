package org.clever.administration.commands;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 * Registration of a virtual environment
 * @author giancarloalteri
 */
public class RegisterVMCommand extends CleverCommand {
    private String localPath=System.getProperty("user.dir");
    private String cfgMountPath = "/src/org/clever/administration/config/VEDescription.xml";
  @Override
  public Options getOptions()
  {
    Options options = new Options();
   // options.addOption("xml", false, "Displays the XML request/response Messages.");
    options.addOption("debug", false, "Displays debug information.");
    return options;
  }

  @Override
  public void exec(final CommandLine commandLine)
  {
    try
    {     
     String info="owners"; 
     ArrayList params = new ArrayList();
     params.add(info);
     String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
     String VE="";         
     FileInputStream fstream = null;
                fstream = new FileInputStream(localPath+cfgMountPath);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                br.readLine();
                while (((strLine = br.readLine()) != null))
                          VE=VE+strLine; 
      VEDescription veD =(VEDescription) MessageFormatter.objectFromMessage(VE);
    
      
      params.add(veD);

      ClusterManagerAdministrationTools.instance().execAdminCommand(
              this, target, "VirtualizationManagerAgent", "register", params, commandLine.hasOption("xml"));
      
    }   catch (FileNotFoundException ex) {
            Logger.getLogger(RegisterVMCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(RegisterVMCommand.class.getName()).log(Level.SEVERE, null, ex);
        }    catch (CleverException ex) {
       if(commandLine.hasOption("debug"))
                 ex.printStackTrace();
       else
                System.err.println(ex);
      logger.error(ex);
    }
  }



  @Override
  public void handleMessage(Object response){
        System.out.println("VM registration done");
  }

   
    public void handleMessageError(CleverException response) {
        System.out.println("VM registration failed"); 
        System.out.println(response);
    }
}
