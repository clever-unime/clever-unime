/*
 *  The MIT License
 *
 *  Copyright 2011 luca.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.clever.HostManager.ImageManager;

import java.util.HashMap;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Shared.ImageFileInfo;
import org.clever.Common.Storage.VFSDescription;

/**
 * This interface describes all the methods needed for the Image Manager
 * @author Valerio Barbera & Luca Ciarniello
 */

public interface ImageManagerPlugin {

  /**
   * Sets the mount point for the VM image files on the host
   * @param path - the absolute path on the current host
   */
  public void setMountPoint(String path);

  /**
   * Returns the mount point for the VM image files on the host
   * @return the mount directory on the host
   */
  public String getMountPoint();

  /**
   * Sets the directory where new VM image files will be downloaded
   * @param path - the absolute path on the current host
   */
  public void setSavePoint(String path);

  /**
   * Returns the directory where new VM image files will be downloaded
   * @return the absolute path on the current host
   */
  public String getSavePoint();

  /**
   *
   * @param sp
   */
  public void setSharedPath(HashMap sp);

  /**
   *
   * @return
   */
  public HashMap getSharedPath();

  /**
   *
   * @param host
   * @param path
   */
  public void addSharedPath(String host, String path);

  /**
   *
   * @param dict
   */
  public void setLocalVE(HashMap dict);

  /**
   *
   * @return
   */
  public HashMap getLocalVE();

  /**
   *
   * @param path
   */
  public void deleteFile(String path,VFSDescription vfsD);

  /**
   *
   * @param path
   * @return
   */
  public boolean isLocalPath(String path);

  /**
   *
   * @param path
   * @return
   */
  public boolean isRemotePath(String path);

  /**
   *
   * @param path
   * @return
   */
  public boolean isSharedPath(String path);

  /**
   *
   * @param path
   * @return
   */
  public boolean isAccessible(String path);

  /**
   *
   * @param veId
   * @param srcPath
   * @param dstPath
   * @return
   */
  public boolean saveVeToPath(String veId, String srcPath, String dstPath);

  /**
   *
   * @param veid
   * @param host
   * @return
   */
  public String getPath(String veid, String host);

  /**
   * This method retrieves the local path of a given VM from a temporary HashMap,
   * removes it, and sends it back
   * @param name - the logical name of the VM
   * @return the VM's local path on the host
   */
  public String getVMPath(String name);

  /**
   * This method, called by the Storage Manager, sends the file data to be saved
   * to the destination host according to the chosen network technology
   * @param name - the logical name of the VirtualMachine
   * @param filePath - the local path of the VirtualMachine image file on the source Host
   * @param destHost - the destination host's IP address
   * @param ftTech - the technology (TCP Socket, XMPP, ...) used for the actual file transfer
   * @return an object depending on the file transfer technology used
   */
  public Object sendFile(String name, String filePath, String destHost, Integer ftTech);

  /**
   * This method, called by the Storage Manager, will create and start a new thread. This thread
   * will open a ServerSocket and wait to receive a VM image file. Once the data is received,
   * a file will be created with the save point as its path.
   * @param name - the logical name of the VM whose image file should be received
   * @param fileName - the name of the image file
   * @return the host's IP address if the file transfer was successful, null otherwise
   */
  public String receiveFile(String name, String fileName);

  /**
   * This method, called by the Storage Manager, will open the file corresponding to filePath absolute
   * path on the host. It will then collect info on the file and return them in an ImageFileInfo object.
   * @param filePath - the image file's absolute path on the host
   * @return the image file info if the file exists, null otherwise
   */
  public ImageFileInfo getFileInfo(String filePath);
 
  public void setOwner(Agent owner);
}