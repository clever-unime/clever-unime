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

package org.clever.Common.VEInfo;

public class StorageSettings {
    private long capacity;
    private long free;
    private String type;
    private String name;
    private String diskPath;
    
    //Per VMWare
    private String datastoreName;
    private String datacenterName;
    public static enum DiskMode {persistent, independent_persistent, independent_nonpersistent};
    private DiskMode diskMode;
    
    //Per VMWare Type: persistent|independent_persistent
    public StorageSettings(long capacity, String type, DiskMode diskMode, String name, String diskPath){
        this.capacity = capacity;
        this.type = type; 
        this.diskPath = diskPath;
        this.name = name;
        this.diskMode = diskMode;
    }
    
    public StorageSettings(long capacity, String type, DiskMode diskMode, String name, String datacenterName, String datastoreName, String diskPath){
        this.capacity = capacity;
        this.type = type; 
        this.diskPath = diskPath;
        this.name = name;
        this.datacenterName = datacenterName;
        this.datastoreName = datastoreName;
        this.diskMode = diskMode;
    }
    
    public StorageSettings(long capacity, String type, String name, String diskPath){
        this.capacity = capacity;
        this.diskPath = diskPath;
        this.name = name;
        this.type = type;       
    }
       

    public long getCapacity(){
        return capacity;
    }

    public void setCapacity(long value){
        this.capacity = value;

    }

    public long getFree(){
        return free;
    }

    public String getType(){
        return type;
    }

    public void setType(String value){
        this.type = value;
    }

    public String getName(){
        return name;
    }

    public void setName(String value){
        this.name = value;
    }

    public String getDiskPath(){
        return diskPath;
    }

    public void setDiskPath(String value){
        this.diskPath = value;
    }
    
    
    //Per VMWare
    public void setDatacenterName(String datacenterName){
        this.datacenterName = datacenterName;
    }    
    public String getDatacenterName(){
        return this.datacenterName;
    }
    public void setDatastoreName(String datasoreName){
        this.datastoreName = datasoreName;
    }    
    public String getDatastoreName(){
        return this.datastoreName;
    }
    public void setDiskMode(DiskMode diskMode){
        this.diskMode = diskMode;
    }
    public DiskMode getDiskMode(){
        return this.diskMode;
    }
}
