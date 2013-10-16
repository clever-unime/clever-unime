/*
 * The MIT License
 *
 * Copyright 2012 giancarloalteri.
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
package org.clever.ClusterManager.StorageManager;

import org.apache.commons.vfs2.FileSystemException;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Plugins.RunnerPlugin;
import org.clever.Common.Storage.VFSDescription;
import org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.LockFile;
/**
 *
 * @author giancarloalteri
 */

public interface StorageManagerPlugin extends RunnerPlugin{

  /**
   * Setter allowing the Agent to pass its ModuleCommunicator to the plugin
   * @param mc - the Agent's ModuleCommunicator
   */
  public void setModuleCommunicator(ModuleCommunicator mc);

  /**
   * Getter for the ModuleCommunicator
   * @return the ModuleCommunicator
   */
  public ModuleCommunicator getModuleCommunicator();
  
  /**
   * This method runs through the hierarchical tree until you reach a mount mode
   * @param path_dest
   * @param cont_dest
   * @return
   * @throws Exception 
   */
  public VFSDescription discoveryNode(String path_dest,String cont_dest) throws Exception;  
  
  /**
   * This method makes a copy of data between two mount nodes 
   * @param path_src
   * @param cont_src
   * @param path_dest
   * @param cont_dest
   * @return
   * @throws CleverException
   * @throws FileSystemException
   * @throws Exception 
   */
  public boolean cp(String path_src,String cont_src,String path_dest,String cont_dest) throws CleverException, FileSystemException, Exception;

  /**
   * This method displays the contents of a clever node
   * @param path
   * @return
   * @throws CleverException
   * @throws FileSystemException
   * @throws Exception 
   */
  public String ls(String path) throws CleverException, FileSystemException, Exception;
  
  /**
   * This method creates a new node in the catalog
   * @param namefolder
   * @param tipo
   * @param contenuto
   * @param contenuto1
   * @return
   * @throws CleverException
   * @throws Exception 
   */
  public boolean createNode(String namefolder,String tipo,VFSDescription vfsD,String contenuto1) throws CleverException,Exception;

  /**
   * This method checks if a directory has child nodes (VirtualizationManager)
   * Is necessary to differentiate the two cases: creatVM and registerVM (HYP)
   * @param path
   * @return
   * @throws Exception 
   */
  public boolean check(String path) throws Exception;

  /**
    * This method handles the locks on the replicas of data
    * @param path
    * @param targetHM
    * @param lock
    * @return
    * @throws CleverException
    * @throws Exception 
    */
   public String lockManager(String path,String targetHM,LockFile.lockMode lock) throws CleverException,Exception;
  
   /**
    * This method checks if the path set by the user during registration is valid
    * @param cleverPath
    * @throws CleverException
    * @throws FileSystemException
    * @throws Exception 
    */
    public void registerVeNew(String cleverPath) throws CleverException, FileSystemException, Exception;
    
    
    public void setOwner(Agent owner);
    
    public boolean deleteFile(String path,String id,String HMTarget);
}
