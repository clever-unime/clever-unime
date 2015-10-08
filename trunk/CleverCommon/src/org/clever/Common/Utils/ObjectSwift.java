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
 *  Copyright (c) 2010 Universita' degli studi di Messina
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
package org.clever.Common.Utils;

import java.util.HashMap;


/**
 *
 * @author agalletta
 */
public class ObjectSwift {
    
    private String pathObject,userSrc,tenantSrc,userDst,tenantDst,token;
    private HashMap<String,String> metadata;

    public ObjectSwift( String pathObject, String user, String tenant, String password,String originETAG,String manipulation) {
        
        this.pathObject = pathObject;
        this.userSrc = user;
        this.tenantSrc = tenant;
        this.metadata=new HashMap();
        this.metadata.put("manipulation", manipulation);
        this.metadata.put("originETAG", originETAG);
        
        }
    
     public ObjectSwift( String pathObject, String user, String tenant) {
        this.pathObject = pathObject;
        this.userSrc = user;
        this.tenantSrc = tenant;
        this.metadata=new HashMap();
        this.metadata.put("manipulation", "origin");
    }

    public ObjectSwift() {
        this.metadata=new HashMap();
        this.metadata.put("manipulation", "origin");
         }

    public String getPathObject() {
        return pathObject;
    }

    public void setPathObject(String pathObject) {
        this.pathObject = pathObject;
    }

    public String getUserSrc() {
        return userSrc;
    }

    public void setUserSrc(String user) {
        this.userSrc = user;
    }

    public String getTenantSrc() {
        return tenantSrc;
    }

    public void setTenantSrc(String tenant) {
        this.tenantSrc = tenant;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String password) {
        this.token = password;
    }

    public void addMetadata(String key,String value){
        this.metadata.put(key, value);
    }
    
    public HashMap getMetadata(){
        return metadata;
    }
 
    public void setMetadata(HashMap metadata){
        this.metadata=metadata;
    }

    public String getUserDst() {
        return userDst;
    }

    public void setUserDst(String userDst) {
        this.userDst = userDst;
    }

    public String getTenantDst() {
        return tenantDst;
    }

    public void setTenantDst(String tenantDst) {
        this.tenantDst = tenantDst;
    }
    
    
    
}
