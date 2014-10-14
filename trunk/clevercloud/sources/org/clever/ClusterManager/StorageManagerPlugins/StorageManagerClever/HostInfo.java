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

package org.clever.ClusterManager.StorageManagerPlugins.StorageManagerClever;

import java.util.List;

public class HostInfo {
    private HostLoad current;
    private HostLoad estimated;
    private long freeStorageSpace;
    private String hostname;
    private boolean publicAdapter;
    private List hypervisors;
    private List veId;

    public HostInfo(String hostname){
        this.hostname = hostname;
    }


    public String getHostname(){
        return this.hostname;
    }

    public HostLoad getCurrentLoad(){
        return this.current;
    }

    public HostLoad getEstimatedLoad(){
        return this.estimated;
    }

    public float getFreeStorageSpace(){
        return this.freeStorageSpace;
    }

    public void setFreeStorageSpace(long space){

        this.freeStorageSpace = space;
    }

    public boolean hasPublicAdapter(){

        return this.publicAdapter;
    }

    public void setPublicAdapter(boolean value){

        this.publicAdapter = value;
    }

    public List getHypervisorsInfo(){

        return this.getHypervisorsInfo();
    }

    public void setHypervisorsInfo(List hypervisors){

        this.hypervisors = hypervisors;
    }

    public List getVEId(){

        return this.veId;
    }

    public void setVEId(List ve){

        this.veId = ve;
    }

    public void setCurrentLoad(HostLoad current){

        this.current = current;
    }

     public void setEstimatedLoad(HostLoad estimated){

        this.estimated = estimated;
    }


}
