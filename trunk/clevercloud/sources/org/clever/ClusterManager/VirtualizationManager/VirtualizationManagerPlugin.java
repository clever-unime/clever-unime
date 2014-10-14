/*
 * Copyright 2014 Università di Messina
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
 * The MIT License
 *
 * Copyright 2011 giovalenti.
 * Copyright 2012 giancarloalteri
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
package org.clever.ClusterManager.VirtualizationManager;

import java.util.HashMap;
import java.util.List;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Plugins.RunnerPlugin;
import org.clever.Common.VEInfo.DesktopVirtualization;
import org.clever.Common.VEInfo.VEDescription;
/**
 *
 * @author giovalenti
 */
public interface VirtualizationManagerPlugin extends RunnerPlugin{
    
    public void RegisterVirtualizationDesktopHTML5(DesktopVirtualization desktop) throws Exception;
    public void UnRegisterVirtualizationDesktopHTML5(String id) throws Exception;
    
    /** 
     * @author giancarloalteri
     * 
     * This method registers a virtual environment and write everything into database Sedna     * @param info
     * @param id
     * @param veD
     * @param st
     * @throws CleverException 
     */
    public void register(String info, VEDescription veD) throws CleverException;
 
    /**
     * Creation and/or registration of a virtual machine
     * @param id
     * @param targetHM
     * @param lock
     * @return
     * @throws CleverException
     */
    public HashMap createVM(String id,String targetHM,org.clever.Common.Communicator.Utils.IstantiationParams param) throws CleverException;
    
    /**
     * Starting VM
     * @param id
     * @return
     * @throws CleverException
     */
    public boolean startVm(String id) throws CleverException;
    
    /**
     * Stopping VM
     * @param id
     * @return
     * @throws CleverException
     */
    public boolean stopVm(String id) throws CleverException;
    
  
  
  public boolean stopVm(String id,Boolean poweroff) throws CleverException;
  
  public void setOwner(Agent owner);
  
  public boolean suspendVm(String id) throws CleverException;
   
  public boolean unregisterVm(String id)throws CleverException;
  
  public boolean resumeVm(String id )throws CleverException;
  
  public boolean TakeEasySnapshot(String id,String nameS,String description,String targetHM) throws CleverException;
  
  public boolean deleteVm(String id) throws CleverException;
  
  public boolean attachInterface(String id,String inf,String mac,String type) throws CleverException;
  
  public List listVm(String targetHM,Boolean running,Boolean hypvr)throws CleverException;
  
  public void manageReUpHost(String HmName);
  
}
