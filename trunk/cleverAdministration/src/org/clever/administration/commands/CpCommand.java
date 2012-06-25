/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 * Copy a file or folder between two mount nodes
 * @author giancarloalteri
 */
public class CpCommand extends CleverCommand{

    @Override
       public Options getOptions(){
          Options options = new Options();
          options.addOption( "src", true, "Source path." );
          options.addOption("dest", true, "Destination path");
      //    options.addOption( "xml", false, "Displays the XML request/response Messages." );
          return options;
       }

    @Override
    public void exec(CommandLine commandLine){
        {
            FileReader f = null;
            {
                FileReader fr = null;
                try {
                    ArrayList params = new ArrayList();
                    String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
                    String path_src = commandLine.getOptionValue("src");
                    String path_dest = commandLine.getOptionValue("dest");
                    params.add(path_src);
                    params.add("");
                    params.add(path_dest);
                    params.add("");
                    for (int i = 1; i <=30 ; i++) {
                        long t0 = new Date().getTime();
                        long tcp = (Long) ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "StorageManagerAgent", "cp", params, commandLine.hasOption( "xml" ) );
                        System.out.println("\n The copy was completed successfully");
                        long t3 = new Date().getTime();
                        fr = new FileReader("/home/davide/Scrivania/cmd.txt");
                        BufferedReader in = new BufferedReader(fr);
                        String line = in.readLine();
                        String[] a = line.split("\t\t");
                        System.out.println(a[0]);
                        System.out.println(a[1]);
                        long t1=Long.valueOf(a[0]);
                        long t2=Long.valueOf(a[1]);

                        long tstartup=t1-t0;
                        long tresponse=t3-t2;

                        fr.close();
                        writeFile(i,tstartup,tcp,tresponse);
                    }
                }
                 catch (FileNotFoundException ex) {
                    Logger.getLogger(CpCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (IOException ex) {
                    Logger.getLogger(CpCommand.class.getName()).log(Level.SEVERE, null, ex);
                } catch (CleverException ex) {
                    logger.error(ex);
                    if (commandLine.hasOption("debug")) {
                        ex.printStackTrace();
                    } else {
                        System.out.println(ex);
                    }
                } finally {
                    try {
                        fr.close();
                    } catch (IOException ex) {
                        Logger.getLogger(CpCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    }




    public static void writeFile(int k,long tstartup,long tcp,long tresponse) {
    String path = "/home/davide/Scrivania/size01GB_ftp.txt";
    try {
      File file = new File(path);
      FileWriter fw = new FileWriter(file,true);
      fw.write(k+"\t\t"+tstartup+"\t\t"+tcp+"\t\t"+tresponse+"\n");
      fw.flush();
      fw.close();
    }
    catch(IOException e) { 
      e.printStackTrace();
    }
  }

    @Override
    public void handleMessage(Object response) {
        
        System.err.println("\n------------------------------------");
        System.out.println("\n The copy was completed successfully");
        System.err.println("\n------------------------------------");
    }

    
    public void handleMessageError(CleverException e) {
        System.out.println(e);
    }
    
}

