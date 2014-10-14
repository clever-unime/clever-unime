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
 *  The MIT License
 *
 *  Copyright (c) 2012 Tricomi Giuseppe
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

package org.clever.ClusterManager.FederatorManager;

import com.google.common.collect.HashBiMap;
import org.clever.Common.VEInfo.VEDescription;
import java.util.HashMap;
/**
 *
 * @author Giuseppe Tricomi <giu.tricomi@gmail.com>
 */
public class FederatorDataContainer {
    //Name for resource requested 
    private HashMap<String,String> resource;
    //Create for future implementation: this indicates the domain owner
    private String federatedEntityTenant;
    //Create for future implementation
    private String federatedEntityLessor;
    //VEDescriptor passed for Virtualization request
    private VEDescription VED;
    //Operation requested, created for future implementation
    private HashBiMap featureRequested;
    //Operation requested 
    private String operationRequested;
    //federation operation id 
    private String operationId;
    //Object used to give at foreing cloud for share element
    private ContainerDataInfo cdi;
    //String used for give a name for container
    private String nameofCont;
    //
    private String hostdesignated4VM_Migration;

    public String getHostdesignated4VM_Migration() {
        return hostdesignated4VM_Migration;
    }

    public void setHostdesignated4VM_Migration(String hostdesignated4VM_Migration) {
        this.hostdesignated4VM_Migration = hostdesignated4VM_Migration;
    }
    
    public String getNameofCont() {
        return nameofCont;
    }

    public void setNameofCont(String nameofCont) {
        this.nameofCont = nameofCont;
    }
    public String getOperationId() {
        return operationId;
    }

    public ContainerDataInfo getCdi() {
        return cdi;
    }

    public void setCdi4Swift( String type, String path,String token) {
        this.cdi = new ContainerDataInfo("","",type,"",path,token);
    }
    
    public void setCdi(String user, String psw, String type, String host, int port, String path,String typeVFS) {
        this.cdi = new ContainerDataInfo(user,psw,type,host,port,path,typeVFS);
    }
    
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
    public String getOperationRequested() {
        return operationRequested;
    }

    public void setOperationRequested(String operationRequested) {
        this.operationRequested = operationRequested;
    }
    public HashBiMap getFeatureRequested() {
        return featureRequested;
    }

    public void setFeatureRequested(HashBiMap featureRequested) {
        this.featureRequested = featureRequested;
    }
    
    public HashMap getResource() {
        return resource;
    }

    public void setResource(HashMap resource) {
        this.resource = resource;
    }

    public void setFederatedEntityTenant(String federatedEntityTenant) {
        this.federatedEntityTenant = federatedEntityTenant;
    }

    public void setFederatedEntityLessor(String federatedEntityLessor) {
        this.federatedEntityLessor = federatedEntityLessor;
    }

    

    public void setVED(VEDescription VED) {
        this.VED = VED;
    }
    
    

    public String getFederatedEntityTenant() {
        return federatedEntityTenant;
    }

    public String getFederatedEntityLessor() {
        return federatedEntityLessor;
    }

    

    public VEDescription getVED() {
        return VED;
    }
    
    public void init_resource(){
        this.resource=new HashMap();
    }
    public void addElementToResource(String k,String V){
        this.resource.put(k, V);
    }
    
    public String getUserfromCDI() {
        return this.cdi.getUser();
    }

    public String getPswfromCDI() {
        return this.cdi.getPsw();
    }

    public String getTypefromCDI() {
        return this.cdi.getType();
    }

    public String getHostfromCDI() {
        return this.cdi.getHost();
    }

    public int getPortfromCDI() {
        return this.cdi.getPort();
    }

    public String getPathfromCDI() {
        return this.cdi.getPath();
    }
    public String getVFS_TypefromCDI() {
        return this.cdi.getTypeVFS();
    }
    public String gettokenfromCDI() {
        return this.cdi.getToken();
    }
}

class ContainerDataInfo{
    String user; //user for VFS NODE not used for SWIFT
    String psw;  //psw for VFS NODE or token for SWIFT
    String type; //VFS  or SWIFT 
    String host; //hostname for VFS NODE or hostname for connection with Swift
    int port;    //port for VFS NODE or port for connection with Swift
    String path; //path for VFS NODE or Path for file contained on Swift
    String typeVFS; //VFS Type
    String token; //token

    public ContainerDataInfo(String user, String psw, String type, String host, int port, String path, String typeVFS) {
        this.user = user;
        this.psw = psw;
        this.type = type;
        this.host = host;
        this.port = port;
        this.path = path;
        this.typeVFS = typeVFS;
        
    }
    
    
    public ContainerDataInfo(String user, String psw, String type, String host,  String path,String token) {
        this.user = user;
        this.psw = psw;
        this.type = type;
        this.host = host;
        this.path = path;
        this.token=token;
    }

    public String getUser() {
        return user;
    }

    public String getPsw() {
        return psw;
    }

    public String getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getTypeVFS() {
        return typeVFS;
    }

    public void setTypeVFS(String typeVFS) {
        this.typeVFS = typeVFS;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
}
