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
 * Upload operation
 * @author giancarloalteri
 */

public class UploadCommand extends CleverCommand{

    @Override
    public Options getOptions() {
        Options options = new Options();
         options.addOption( "src", true, "The local path." );
         options.addOption( "dest", true, "The remote path." );
         options.addOption( "debug", false, "Displays debug information." );
      //   options.addOption( "xml", false, "Displays the XML request/response Messages." );
         return options;
    }

    @Override
    public void exec(CommandLine commandLine) {
         try {
        String target = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getActiveCC(ConnectionXMPP.ROOM.SHELL);
        ArrayList params = new ArrayList();
        String path_src=commandLine.getOptionValue("src");
        String path_dest=commandLine.getOptionValue("dest");      
        File f = new File(path_src);
        if (!f.exists())
            throw new CleverException("Error. Local file not found");
        params.add(path_dest);
        params.add("");
        VFSDescription res=( VFSDescription) ClusterManagerAdministrationTools.instance().execSyncAdminCommand(this, target, "StorageManagerAgent", "discoveryNode", params, commandLine.hasOption( "xml" ) );
        VirtualFileSystem dest=new VirtualFileSystem();
        dest.setURI(res);
        FileObject file_d=dest.resolver(res, dest.getURI(),res.getPath1());
        FileSystemManager mgr = VFS.getManager();
        FileObject file_s = mgr.resolveFile(path_src);
        dest.cp(file_s, file_d);
        System.out.println("-------------------");
        System.out.println("File upload success");
        System.out.println("-------------------");
        }  catch (FileSystemException ex) {
            System.out.println(ex);
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleMessageError(CleverException response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
