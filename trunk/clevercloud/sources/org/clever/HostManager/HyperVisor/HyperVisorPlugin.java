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
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
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

package org.clever.HostManager.HyperVisor;

import java.util.List;
import java.util.Map;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Plugins.RunnerPlugin;
import org.clever.Common.VEInfo.NetworkSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.VEInfo.VEState;

public interface HyperVisorPlugin extends RunnerPlugin {

    boolean destroyVm(String id) throws Exception;
    boolean destroyVm(String ids[]) throws Exception;
    
    boolean shutDownVm(String id) throws Exception;
    boolean shutDownVm(String ids[]) throws Exception;
     boolean shutDownVm(String id, Boolean poweroff) throws Exception;
    boolean shutDownVm(String ids[], Boolean poweroff) throws Exception;
    
    boolean resume(String id) throws Exception;
    boolean startVm(String id) throws Exception;
   
    /**
     * Start n VMs
     * @param ids: IDs of VMs that have to be started
     * @return successful
     * @throws Exception 
     */
    boolean startVm(String[] ids) throws Exception;
    
   
    boolean createVm(String veId, VEDescription parameters,Boolean notExclusive) throws Exception;
    //TODO: include notExclusive in parameter
    boolean createVm(Map<String, VEDescription> ves) throws Exception;
    
    boolean saveState(String id, String path) throws Exception;
    boolean resumeState(String id, String path) throws Exception;
    boolean createAndStart(String veId, VEDescription parameters,Boolean notExclusive) throws Exception;
    boolean addAdapter(String id, NetworkSettings settings) throws Exception;
    boolean isRunning(String id)throws Exception;
    public List<VEState> listVms() throws Exception;
    public List<VEState> listRunningVms() throws Exception;
    public List getOSTypes();
    public boolean suspend (String id) throws Exception;
    public boolean cloneVM (String id, String clone, String description) throws Exception;
    public boolean takeSnapshot (String id, String nameS, String description) throws Exception;
    public boolean restoreSnapshot (String id, String nameS)throws Exception;
    public boolean deleteSnapshot (String id, String nameS) throws Exception;
    public String currentSnapshot (String id)throws Exception;   
    public long snapshotCount(String id)throws Exception;
    public String getHYPVRName();
   
    
    
    
    //Aggiunti da Giovanni (VMWare)
    public boolean deleteAllSnapshot(String id) throws Exception;
    public boolean renameVM(String id, String new_id) throws Exception;
    public boolean resetVM(String id) throws Exception;
    public List listSnapshot(String id) throws Exception;
    public boolean renameSnapshot(String id, String snapName, String newSnapName, String description) throws Exception;
    public boolean attachPortRemoteAccessVm(String id) throws Exception; 
    public void releasePortRemoteAccessVm(String id) throws Exception;
    //public boolean registerVm(String id, String path) throws Exception;
    //public boolean unregisterVm(String id) throws Exception;
    public boolean ExportOvfToLocal(String id, String TargetPhysicalPath) throws Exception;
    public boolean ImportLocalOvf(String id, String OVF_physicalPath) throws Exception;
    public void setOwner(Agent owner);
    //public String getLocalPath(String id)throws HyperVisorException;
    public boolean attachInterface(String id, String inf,String mac,String type) ;
}
