/*
 * The MIT License
 *
 * Copyright 2011 giancarloalteri.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.clever.Common.Storage;

import java.text.DateFormat;
import java.util.Date;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;

/**
 *
 * @author giancarloalteri
 */

    
public class VirtualFileSystem {

    private String uri;
    
    public VirtualFileSystem(){
        
    }
            
    public VirtualFileSystem(String uri){
        this.uri=uri;
    } 

    /**
     * This method lists contents of a file or folder
     * @param file
     * @return
     * @throws FileSystemException 
     */
    public String ls(FileObject file) throws FileSystemException{
        String str="Contents of " + file.getName() +"\n";
        if (file.exists()){
            if (file.getType().equals(FileType.FILE)){
                str=str+"Size: " + file.getContent().getSize() + " bytes\n"
                        +"Last modified: " +DateFormat.getInstance().format(new Date(file.getContent().getLastModifiedTime()))+"\n"
                        +"Readable: " + file.isReadable()+"\n"
                        +"Writeable: " + file.isWriteable()+"\n";
                return str;
            } 
            else if (file.getType().equals(FileType.FOLDER) && file.isReadable()){
                 FileObject[] children = file.getChildren(); 
                 str=str="Directory with " + children.length + " files \n"
                         +"Readable: " + file.isReadable()+"\n"
                         +"Writeable: " + file.isWriteable()+"\n\n";
                 //str=str+"Last modified: " +DateFormat.getInstance().format(new Date(file.getContent().getLastModifiedTime()))+"\n" ;
                for ( int i = 0; i < children.length; i++ ){
                    str=str+children[ i ].getName().getBaseName()+"\n";    
            }
            }
           } 
         else{
              str=str+"The file does not exist"; 
             }
      return str;
    }
    /**
     * This method copies a file or folder
     * @param file_s
     * @param file_d
     * @throws FileSystemException 
     */
    public void cp(FileObject file_s,FileObject file_d) throws FileSystemException{
      if (file_d.exists() && file_d.getType() == FileType.FOLDER){
          file_d = file_d.resolveFile(file_s.getName().getBaseName());
         }
      file_d.copyFrom(file_s, Selectors.SELECT_ALL);
   }

    /**
     * Simply changed the last modification time of the given file
     * @param fo
     * @throws FileSystemException 
     */
    public void ChangeLastModificationTime(FileObject fo) throws FileSystemException{
        long setTo = System.currentTimeMillis();
        System.err.println("set to: " + setTo);
        fo.getContent().setLastModifiedTime(setTo);
        System.err.println("after set: " + fo.getContent().getLastModifiedTime());
    }

    /**
     * This method returns a URI format for that particular virtual file system
     * @return 
     */
    public String getURI(){
    return uri;
    }

    /**
     * This method creates a URI format for a particular virtual file file
     * @param vfsD 
     */
    public void setURI( VFSDescription vfsD ){
    if(vfsD.getPort().isEmpty()){
        vfsD.setPort("");
        
    }
    else{
        vfsD.setPort(":"+vfsD.getPort());
    }
    if(vfsD.getHostname().isEmpty()){
        vfsD.setHostname("");
    }
    if(vfsD.getPath().isEmpty()){
        vfsD.setPath("");
    }  
   //
    this.uri=vfsD.getType().name().replace("_", ":")+"://"+vfsD.getHostname()+vfsD.getPort()+"/"+vfsD.getPath();

  }
    /**
     * This method makes the URI resolver
     * @param vfsD
     * @param uri
     * @param content
     * @return
     * @throws FileSystemException 
     */
    public FileObject resolver(VFSDescription vfsD,String uri,String content) throws FileSystemException {
      StaticUserAuthenticator auth = new StaticUserAuthenticator(null,vfsD.getAuth().getUsername(),vfsD.getAuth().getPassword());
       FileSystemOptions opts = new FileSystemOptions(); 
       DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth); 
       FileSystemManager fsManager = VFS.getManager();  
       FileObject file = fsManager.resolveFile(uri,opts);
       FileObject file1 = fsManager.resolveFile(file,content) ;
       return file1;
    }


}