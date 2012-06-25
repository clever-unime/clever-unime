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

package org.clever.HostManager.MonitorPlugins.Sigar;

import org.clever.HostManager.MonitorPlugins.Sigar.Database;
import java.util.Vector;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.SigarCommandBase;

public class StorageInfo extends SigarCommandBase {


    private float totalSpace = 0;
    private String dir = null;
    private String fileSystemType = null;
    private String name = null;
    private double maxFileDimension = 0;
    private boolean supportLink;
    private boolean supportFilePermission;

    private Database db;
    private Vector v;

    /**
     * Index of the FileSystem processed by the instance.
     */
    private int StorageIndex;

    /**
     * Object name this instance will give itself when being registered to an
     * MBeanServer.
     */


    public StorageInfo(int StorageIndex) throws IllegalArgumentException, Exception {
        this(new Sigar(), StorageIndex);
    }


    public StorageInfo(Sigar sigar, int StorageIndex) throws IllegalArgumentException, Exception {

            db= Database.istance();

            this.StorageIndex = StorageIndex;

            FileSystem fs=sigar.getFileSystemList()[this.StorageIndex];
            
            if(fs.getType()==FileSystem.TYPE_LOCAL_DISK){


                FileSystemUsage usage =this.sigar.getFileSystemUsage(fs.getDirName());
                usage = this.sigar.getFileSystemUsage(fs.getDirName());
                totalSpace=usage.getTotal();
                dir=sigar.getFileSystemList()[this.StorageIndex].getDirName();
                fileSystemType=sigar.getFileSystemList()[this.StorageIndex].getSysTypeName()+"/"+sigar.getFileSystemList()[this.StorageIndex].getTypeName();
                name=sigar.getFileSystemList()[this.StorageIndex].getDevName();

                v = db.executeQuery("select * from FileSystem where type = '"+  sigar.getFileSystemList()[this.StorageIndex].getSysTypeName() + "';");
                if(v.size()>0){
                                maxFileDimension = Double.valueOf( ((String[]) v.elementAt( 0 ))[2]);
                                if(((String[]) v.elementAt( 0 ))[4].equals("1"))
                                            supportLink=true;
                                else
                                            supportLink=false;
                                if(((String[]) v.elementAt( 0 ))[3].equals("1"))
                                            supportFilePermission=true;
                                else
                                            supportFilePermission=false;

                }else{
                                System.out.print("FileSystem sconosciuto!");
                                supportLink=false;
                                supportFilePermission=false;
                                maxFileDimension = 0;
                }

            }

    }


    /**
     * @return Get the FileSystem type
     */

    public String getFIleSystemType() {

            return fileSystemType;
    }

    /**
     * @return Get FileSystem name
     */
    public String getName() {

            return name;
    }


    /**
     * @return Get the directory where the FileSystem is mounted
     */
    public String getDir() {

            return dir;
    }

    /**
     * @return The total size of the FileSystem
     */

    public float getTotalSpace() {
        
            return totalSpace;

    }

     public double MaxFileDimension() {

         return maxFileDimension;

     }

     public boolean SupportLink() {

         return supportLink;

    }

     public boolean SupportFilePermission() {

         return supportFilePermission;

    }

    @Override
    public void output(String[] strings) throws SigarException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}


