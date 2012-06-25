/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.commands;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Storage.VFSDescription;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 * Creation of a mount node
 * @author giancarloalteri
 */
public class CreateMountCommand extends CleverCommand{
    private String localPath=System.getProperty("user.dir");
    private String cfgMountPath = "/src/org/clever/administration/config/VFSDescription.xml";

    @Override
    public Options getOptions() {
         Options options = new Options();
         options.addOption( "p", true, "The logical path." );
         options.addOption( "debug", false, "Displays debug information." );
         options.addOption("wizard", false, "wizard.");
      //   options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options;    }

    @Override
    public void exec(CommandLine commandLine) {
        try {
            String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
            String a="";
            
            if(commandLine.hasOption("wizard")){
            String type=JOptionPane.showInputDialog("inserisci il tipo di VFS"); 
            String usr=JOptionPane.showInputDialog("inserisci username");
            String pwd=JOptionPane.showInputDialog("inserisci password");
            String host=JOptionPane.showInputDialog("inserisci hostname");
            String port=JOptionPane.showInputDialog("inserisci la porta");
            String path=JOptionPane.showInputDialog("inserisci il path");
            a="<org.clever.Common.Storage.VFSDescription>"
                +"<auth>"
                    +"<username>"+usr+"</username>"
                    +"<password>"+pwd+"</password>"
                +"</auth>"
                +"<typevfs>"+type+"</typevfs>"
                +"<hostname>"+host+"</hostname>"
                +"<port>"+port+"</port>"
                +"<path>"+path+"</path>"
                +"<path1></path1>"
            +"</org.clever.Common.Storage.VFSDescription>";
            }
            else{
                    FileInputStream fstream = null;
                    fstream = new FileInputStream(localPath+cfgMountPath);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;
                    br.readLine();
                    while (((strLine = br.readLine()) != null))
                          a=a+strLine; 
                }
        
            ArrayList params = new ArrayList();
            String logicname=commandLine.getOptionValue("p");
            params.add(logicname);
            params.add("mount");
            VFSDescription vfsD =(VFSDescription) MessageFormatter.objectFromMessage(a);
            params.add(vfsD);
            params.add("");
        
            ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "StorageManagerAgent", "createNode", params, commandLine.hasOption( "xml" ) );
       
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CreateMountCommand.class.getName()).log(Level.SEVERE, null, ex);
        } 
         catch (IOException ex) {
            Logger.getLogger(CreateMountCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (CleverException ex) {
            logger.error(ex);
            if(commandLine.hasOption("debug"))
                 ex.printStackTrace();
            else
                System.out.println(ex);
        }
        }

    @Override
    public void handleMessage(Object response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public void handleMessageError(CleverException response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
