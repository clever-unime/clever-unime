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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.jms.JMSException;
import org.apache.log4j.Logger;
import org.clever.HostManager.Monitor.MemoryInfo;
import org.clever.HostManager.Monitor.OSInfo;
import org.clever.HostManager.Monitor.ResourceState;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.OperatingSystem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.cmd.Shell;



public class LinuxHWMonitor extends HWMonitor
{

  private MemoryInfo mem;
  private OSInfo osinfo;
  private ProcessInfo process;
  private List procinfo = new ArrayList();
  private ClientNet network;
  private List net = new ArrayList();
  private StorageInfo infostor;
  private List storinfo = new ArrayList();
  //private CPUInfoLinux cpu;
  private CPUInfo cpu;
  private List cpuinfo = new ArrayList();
  private FileSystem[] fslist;
  private FileSystem fs;
  private FileSystemUsage usage;
  private CpuPerc[] cpus;
  private CpuInfo[] cpuinfolist;
  private FileSystem[] fslistinfo;
  Sigar sigar;
  SigarProxy proxy;
  Shell shell;
  private Calendar cal = Calendar.getInstance();
  private SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT_NOW );
  public static final String DATE_FORMAT_NOW = "dd-MM-yyyy HH:mm:ss";



  public LinuxHWMonitor( Shell shell )
  {

    this.shell = shell;
    this.sigar = shell.getSigar();
    this.proxy = shell.getSigarProxy();


  }



  public LinuxHWMonitor()
  {

    this( new Shell() );
    Logger.getLogger( LinuxHWMonitor.class.getName() ).debug( "Creating Sigar LinuxHwMonitor " );
    //TODO: check the exception on the library load
  }



  @Override
  public List getRawCPUInfo()
  {
    try
    {
      cpu = new CPUInfoLinux();
      cpuinfolist = this.sigar.getCpuInfoList();
      for( int cpunum = 0; cpunum < cpuinfolist.length; cpunum++ )
      {
        cpuinfo.add( cpu );
      }
    }
    catch( Exception e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting RawCPUInfo" + e );
    }
    return cpuinfo;
  }



  @Override
  public List getRawCPUState()
  {
    List cpustate = new ArrayList();
    ResourceState cpures = null;
    try
    {
      cpus = this.sigar.getCpuPercList();
      for( int i = 0; i < cpus.length; i++ )
      {
        cpures = new ResourceState( ( float ) ( ( cpus[i].getUser() ) * 100 ), sdf.format( cal.getTime() ) );
        cpustate.add( cpures );
      }
    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting RawCPUState" + e );
    }
    return cpustate;
  }



  @Override
  public MemoryInfo getRawMemoryInfo()
  {
    try
    {
      MemoryInfo mem = new MemoryInfo();
      mem.setRam( sigar.getMem().getRam() );
      mem.setSwap( sigar.getSwap().getTotal() );
      mem.setTotal( sigar.getMem().getTotal() );
      return mem;
    }
    catch( SigarException ex )
    {
      return null;
    }
  }



  @Override
  public List getRawStorageInfo()
  {
    try
    {
      fslistinfo = this.sigar.getFileSystemList();
      for( int Stor = 0; Stor < fslistinfo.length; Stor++ )
      {
        infostor = new StorageInfo( Stor );
        FileSystem fsys = this.sigar.getFileSystemList()[Stor];
        if( fsys.getType() == FileSystem.TYPE_LOCAL_DISK )
        {
          storinfo.add( infostor );
        }
      }
    }
    catch( Exception e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting RawStorageInfo" + e );
    }
    return storinfo;
  }



  @Override
  public List getRawProcessInfo()
  {
    try
    {
      process = new ProcessInfo();
      long[] ids = this.proxy.getProcList();
      for( int i = 0; i < ids.length; i++ )
      {
        procinfo.add( process );
      }
    }
    catch( Exception e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting RawProcessInfo" + e );
    }
    return procinfo;
  }



  @Override
  public OSInfo getRawOsInfo()
  {
   OperatingSystem sys = OperatingSystem.getInstance();
    osinfo = new OSInfo(        sys.getName(),
        sys.getVendor(),
        sys.getVersion(),
        Integer.parseInt(sys.getDataModel()),
        sys.getDescription(),
        sys.getArch()
    );
    return osinfo;
  }



  @Override
  public String getOsType()
  {
    return HwMonitorFactory.getMonitor();
  }



  @Override
  public void getNetworkInfo( boolean netflag )
  {
    //boolean netflag=true;
    //boolean netflag=false;
    if( netflag )
    {
      System.out.println( "[totpkts, totpktsize, average, throughput, throughputpkts]" );
    }

    while( true )
    //TODO checking the following instructions
    {
      try
      {
        new ConsumerNetJms( netflag );
      }
      catch( JMSException e )
      {
        Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting NetworkInfo" + e );
      }
    }
  }



  @Override
  public ResourceState getRawMemoryCacheState()
  {
    ResourceState cache = null;
    try
    {
      cache = new ResourceState( ( float ) sigar.getMem().getActualFree(), sdf.format( cal.getTime() ) );
    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting RawMemoryCacheState" + e );
    }
    return cache;
  }



  @Override
  public ResourceState getRawMemoryBufferState()
  {
    ResourceState buffer = null;
    try
    {
      buffer = new ResourceState( ( float ) sigar.getMem().getActualUsed(), sdf.format( cal.getTime() ) );
    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting MemoryBufferState" + e );
    }
    return buffer;
  }



  @Override
  public ResourceState getRawMemoryCurrentFree()
  {
    ResourceState currentFree = null;
    try
    {
      currentFree = new ResourceState( ( float ) sigar.getMem().getFree(), sdf.format( cal.getTime() ) );
    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting Current free memory" + e );
    }
    return currentFree;
  }



  @Override
  public ResourceState getRawMemoryCurrentSwapFree()
  {
    ResourceState currentSwapFree = null;
    try
    {
      currentSwapFree = new ResourceState( ( float ) sigar.getSwap().getFree(), sdf.format( cal.getTime() ) );
    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting RawMemoryCurrentSwapFree" + e );
    }
    return currentSwapFree;
  }



  @Override
  public ResourceState getRawMemoryUsed()
  {
    ResourceState memoryUsed = null;
    try
    {
      memoryUsed = new ResourceState( ( float ) sigar.getMem().getUsed(), sdf.format( cal.getTime() ) );
    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting Raw Memory used" + e );
    }
    return memoryUsed;
  }



  @Override
  public ResourceState getRawMemoryUsedSwap()
  {
    ResourceState memoryUsedSwap = null;
    try
    {
      memoryUsedSwap = new ResourceState( ( float ) sigar.getSwap().getUsed(), sdf.format( cal.getTime() ) );
    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting memory used swap" + e );
    }
    return memoryUsedSwap;
  }



  @Override
  public List getRawStorageCurrentFreeSpace()
  {

    List storcurrentFreeSpace = new ArrayList();
    ResourceState currentFreeSpace = null;
    try
    {
      fslist = sigar.getFileSystemList();

      for( int Stor = 0; Stor < fslist.length; Stor++ )
      {

        fs = sigar.getFileSystemList()[Stor];

        if( fs.getType() == FileSystem.TYPE_LOCAL_DISK )
        {

          usage = this.sigar.getFileSystemUsage( fs.getDirName() );
          currentFreeSpace = new ResourceState( ( float ) usage.getAvail(), sdf.format( cal.getTime() ) );
          storcurrentFreeSpace.add( currentFreeSpace );
        }
      }


    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting the current free storage space" + e );
    }
    return storcurrentFreeSpace;
  }



  @Override
  public List getRawStorageUsed()
  {
    List storStorageUsed = new ArrayList();
    ResourceState storUsed = null;
    try
    {
      fslist = sigar.getFileSystemList();

      for( int Stor = 0; Stor < fslist.length; Stor++ )
      {

        fs = sigar.getFileSystemList()[Stor];

        if( fs.getType() == FileSystem.TYPE_LOCAL_DISK )
        {

          usage = this.sigar.getFileSystemUsage( fs.getDirName() );
          storUsed = new ResourceState( ( float ) usage.getUsed(), sdf.format( cal.getTime() ) );
          storStorageUsed.add( storUsed );
        }
      }


    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting Raw storage used" + e );
    }
    return storStorageUsed;
  }



  @Override
  public List getRawStorageUsedpc()
  {
    List storStorageUsedpc = new ArrayList();
    ResourceState storUsedpc = null;
    try
    {
      fslist = sigar.getFileSystemList();

      for( int Stor = 0; Stor < fslist.length; Stor++ )
      {

        fs = sigar.getFileSystemList()[Stor];

        if( fs.getType() == FileSystem.TYPE_LOCAL_DISK )
        {

          usage = this.sigar.getFileSystemUsage( fs.getDirName() );
          storUsedpc = new ResourceState( ( float ) ( usage.getUsePercent() * 100 ), sdf.format( cal.getTime() ) );
          storStorageUsedpc.add( storUsedpc );
        }
      }


    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting raw storage used pc" + e );
    }
    return storStorageUsedpc;
  }



  @Override
  public double getRawUpTime()
  {
    double uptime = 0;
    try
    {
      uptime = sigar.getUptime().getUptime();
    }
    catch( SigarException e )
    {
      Logger.getLogger( LinuxHWMonitor.class.getName() ).error( "Error while getting raw uptime" + e );
    }
    return uptime;
  }
}
