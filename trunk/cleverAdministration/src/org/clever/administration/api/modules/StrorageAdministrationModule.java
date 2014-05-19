/*
 * Copyright [2014] [Università di Messina]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
/*
 * Copyright (c) 2013 Universita' degli studi di Messina
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

package org.clever.administration.api.modules;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Storage.VFSDescription;
import org.clever.Common.Storage.VirtualFileSystem;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.administration.annotations.HasScripts;

import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.annotations.ShellParameter;
import org.clever.administration.api.Session;
import org.clever.administration.commands.CreateMountCommand;


/**
 * Modulo per gestire lo Storage Logico
 * @author Giuseppe Tricomi
 */
@HasScripts(value="SAM", script="scripts/sam.bsh", comment="Storage Administration module for Clever")
public class StrorageAdministrationModule extends AdministrationModule{
    
    
    
    public StrorageAdministrationModule (Session s)
    {
        super(s);
        
    }
    
    /**
     * Mount a VFS NODE
     * @return 
     */
    @ShellCommand
    public String Mount(@ShellParameter(name="name", comment="Logica Path Name node") String name,
                                 @ShellParameter(name="pathVFS", comment="if has selected unwizard, this parameter set the path of vfs") String pathVFS,
                                 @ShellParameter(name="Type", comment="type of creation(wizard: for guided creation,unwizard: creation by vfsDescriptor") String type) throws CleverException
    {
        String a="";
        
        if(type.equals("wizard")){
            String typevfs=JOptionPane.showInputDialog("inserisci il tipo di VFS"); 
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
                +"<typevfs>"+typevfs+"</typevfs>"
                +"<hostname>"+host+"</hostname>"
                +"<port>"+port+"</port>"
                +"<path>"+path+"</path>"
                +"<path1></path1>"
            +"</org.clever.Common.Storage.VFSDescription>";
            }
        else{
            try{
                    FileInputStream fstream = null;
                    fstream = new FileInputStream(pathVFS);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;
                    br.readLine();
                    while (((strLine = br.readLine()) != null))
                          a=a+strLine; 
            }
            catch (FileNotFoundException ex) {
                
                throw new CleverException(ex,"VFS Descriptor selected not Found!"+"\n"+ex.getMessage());
            } 
             catch (IOException ex) {
                throw new CleverException(ex,"IOException in VFS Descriptor manipulation!"+"\n"+ex.getMessage());
            }
            catch (Exception ex) {
                throw new CleverException(ex,"a generic Exception has occurred in VFS manipulation!"+"\n"+ex.getMessage());
            }
        }
        ArrayList params = new ArrayList();
        params.add(name);
        params.add("mount");
        VFSDescription vfsD =(VFSDescription) MessageFormatter.objectFromMessage(a);
        params.add(vfsD);
        params.add("");
        this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),
                                        "StorageManagerAgent",
                                        "createNode",
                                        params,
                                        false);
         return "Node created!";
    }
    
    
    
    
    /**
     * Unmount a VFS NODE
     * @return 
     */
    @ShellCommand
    public void Unmount(@ShellParameter(name="name", comment="Logica Path Name node") String name) throws CleverException
    {
        ArrayList params = new ArrayList();
        params.add("StorageManagerAgent");
        params.add("/node[@name='" +name  + "']");
        try{
        this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),
                                        "DatabaseManagerAgent",
                                        "deleteNode",
                                        params,
                                        false);
        }catch(Exception e){
            throw new CleverException(e);
        }
    }
            
    /**
     * List element stored in a VFS NODE
     * @return 
     */
    @ShellCommand
    public String ls (@ShellParameter(name="name", comment="Logical Path Name node") String name) throws CleverException
    {
        ArrayList params = new ArrayList();
        params.add(name  );
        try{
        String result="\n--------Logical Catalog--------\n"+(String)this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),
                                        "StorageManagerAgent",
                                        "ls",
                                        params,
                                        false)+"\n-------------------------------";
        return result;
        }catch(Exception e){
            throw new CleverException(e);
        }
        
    }    
    
    /**
     * Upload an element stored in a VFS NODE.
     * This function is deprecated.
     * @return 
     */
    @ShellCommand
    public String upload (@ShellParameter(name="src", comment="Local Logical Path") String src,
                          @ShellParameter(name="dst", comment="Remote Logical Path") String dst  ) throws CleverException
    {
        ArrayList params = new ArrayList();
        File f = new File(src);
        if (!f.exists())
            throw new CleverException("Error. Local file not found");
        params.add(dst);
        params.add("");
        try{
            VFSDescription res=( VFSDescription) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),
                                            "StorageManagerAgent",
                                            "discoveryNode",
                                            params,
                                            false);
            VirtualFileSystem dest=new VirtualFileSystem();
            dest.setURI(res);
            FileObject file_d=dest.resolver(res, dest.getURI(),res.getPath1());
            FileSystemManager mgr = VFS.getManager();
            FileObject file_s = mgr.resolveFile(src);
            dest.cp(file_s, file_d);
        }  catch (FileSystemException ex) {
            throw new CleverException(ex,"Error in file upload!");
        }
        
        String result="\n--------Logical Catalog--------\n"+"File upload success"+"\n-------------------------------";
        return result;
    }
    
}
