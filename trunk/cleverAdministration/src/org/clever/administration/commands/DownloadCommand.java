/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Storage.VFSDescription;
import org.clever.Common.Storage.VirtualFileSystem;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;

/**
 * Download operation
 * @author giancarloalteri
 */
public class DownloadCommand extends CleverCommand{

    @Override
    public Options getOptions() {
         Options options = new Options();
         options.addOption( "src", true, "The remote path." );
         options.addOption( "dest", true, "The local path." );
         options.addOption( "debug", false, "Displays debug information." );
      //   options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
        try {
            String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
            ArrayList params = new ArrayList();
            String remotePath=commandLine.getOptionValue("src");
            String localPath=commandLine.getOptionValue("dest");
            params.add(remotePath);
            params.add("");
            VFSDescription res=(VFSDescription) ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "StorageManagerAgent", "discoveryNode", params, commandLine.hasOption( "xml" ) );
            VirtualFileSystem vfs=new VirtualFileSystem();
            vfs.setURI(res);
            FileObject remoteFile=vfs.resolver(res, vfs.getURI(), res.getPath1());
            FileSystemManager mgr = VFS.getManager();
            File f = new File(localPath);
            if (!f.exists() || !f.isDirectory())
            throw new CleverException("Error. Local path not found");
            FileObject localFile = mgr.resolveFile(localPath);
            vfs.cp(remoteFile, localFile);
            System.out.println("-------------------");
            System.out.println("File download success");
            System.out.println("-------------------");
          } 
          catch (CleverException ex) {
            logger.error(ex);
            if(commandLine.hasOption("debug"))
                 ex.printStackTrace();
            else
                System.out.println(ex);
        } catch (FileSystemException ex) {
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
