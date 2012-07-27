/*
 * The MIT License
 *
 * Copyright 2012 s89.
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
package org.clever.ClusterManager.VisionManager;

import org.clever.ClusterManager.VisionManagerPlugin.VisionManagerClever.VisionVmInfo;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.VisionException;
import org.clever.Common.Plugins.RunnerPlugin;
import org.jdom.Document;

/**
 *
 * @author s89
 */
public interface VisionManagerPlugin extends RunnerPlugin{
    public void setOwner(Agent agent);
    public boolean clusterBuild(Document vision_cfg,Document net_info,Boolean ipstatic)throws VisionException, CleverException;
    public boolean startVisionVm(String id)throws CleverException;
    public boolean startVisionCluster(String cl)throws CleverException;
    public boolean startVisionClusters()throws CleverException;
    public boolean stopVisionVm(String id,Boolean poweroff)throws CleverException;
    public boolean stopVisionCluster(String cl,Boolean poweroff)throws CleverException;
    public boolean stopVisionClusters(Boolean poweroff)throws CleverException;
     public boolean deleteVisionVm(String id)throws CleverException;
    public boolean deleteVisionCluster(String cl)throws CleverException;
    public boolean deleteVisionClusters()throws CleverException;
    public String listVisionClusterVm(String id) throws CleverException;
    public String listVisionClustersVm() throws CleverException;
    public boolean addVmRunning(String vmname) throws CleverException;
    public boolean addVmDb(String nameC,VisionVmInfo vmi) throws CleverException;
    public boolean addClusterDb(String nameC,String image) throws CleverException;
    public boolean delVmRunning(String vmname) throws CleverException;
    public boolean delAllDb() throws CleverException;
    
}
