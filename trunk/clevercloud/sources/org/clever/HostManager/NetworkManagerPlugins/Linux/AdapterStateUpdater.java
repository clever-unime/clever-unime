/*
 *  Copyright (c) 2010 Patrizio Filloramo
 *  Copyright (c) 2010 Salvatore Barbera
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
package org.clever.HostManager.NetworkManagerPlugins.Linux;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clever.HostManager.NetworkManager.AdapterState;
import org.clever.HostManager.NetworkManager.TcpState;
import org.clever.HostManager.NetworkManager.UdpState;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;



/**
 *
 * @author Patrizio Filloramo & Salvatore Barbera
 */
class AdapterStateUpdater extends Thread
{

  private String adapterName;
  private Calendar timestamp;
  private float bandUsageRX;
  private float bandUsageTX;
  private long totalByteSent;
  private long totalByteRecv;
  private int totalEndpoints;
  public TcpState tcpState;
  public UdpState udpState;
  private boolean active;
  private long speed = 1000000;
  private NetworkManagerLinux manager;
  private AdapterState adapterState;



  public AdapterStateUpdater( String name, NetworkManagerLinux manager )
  {
    this.adapterName = name;
    this.manager = manager;
  }



  public void run()
  {
    this.udpState = new UdpState( this.adapterName )
    {
    };
    this.tcpState = new TcpState( this.adapterName )
    {
    };
    this.updateTimestamp();
    this.updateSpeed();
    this.updateTotalBytesSent();
    this.updateTotalBytesRecv();
    this.updateTotalEndpoints();
    this.updateBandUsage();
    this.linkIsActive();
    this.manager.addAdapaterState( getAdapterStateUpdated() );
    this.manager.runningThread--;
  }



  private void setAdapterName( String adapterName )
  {
    this.adapterName = adapterName;
  }



  private void updateTimestamp()
  {
    this.timestamp = Calendar.getInstance();
  }



  private void updateTotalBytesSent()
  {
    this.totalByteSent = this.tcpState.getByteSent() + this.udpState.getBytesSent();
  }



  private void updateTotalBytesRecv()
  {
    this.totalByteRecv = this.tcpState.getByteRecv() + this.udpState.getBytesRecv();
  }



  private void updateTotalEndpoints()
  {
    this.totalEndpoints = udpState.getEndpoints() + tcpState.getEndpoints();
  }



  private void updateBandUsage()
  {
    float bytesRcv = 0;
    float bytesRcv1SecLater = 0;
    float bytesSent = 0;
    float bytesSent1SecLater = 0;

    bytesSent = this.totalByteSent;
    bytesRcv = this.totalByteRecv;

    try
    {
      Thread.sleep( 1000 );
    }
    catch( InterruptedException ex )
    {
      Logger.getLogger( AdapterState.class.getName() ).log( Level.SEVERE, null, ex );
    }
    this.udpState = new UdpState( this.adapterName )
    {
    };
    this.tcpState = new TcpState( this.adapterName )
    {
    };
    this.updateTotalBytesRecv();
    bytesRcv1SecLater = this.getTotalBytesRecv();
    this.updateTotalBytesSent();
    bytesSent1SecLater = this.getTotalBytesSent();

    this.bandUsageRX = ( ( bytesRcv1SecLater - bytesRcv ) / this.getSpeed() ) * 100;
    this.bandUsageTX = ( ( bytesSent1SecLater - bytesSent ) / this.getSpeed() ) * 100;
  }



  private void linkIsActive()
  {
    this.active = false;
    if( OSValidator.isUnix() )
    {
      List response = new ArrayList();
      response = Cmd.executeCommand( "/usr/sbin/ethtool " + this.adapterName );
      if( this.adapterName.contains( "wlan" ) )
      {
        this.wlanIsActive();
      }
      else
      {
        for( Object line : response )
        {
          StringTokenizer commandArgs = new StringTokenizer( ( ( String ) line ) );
          while( commandArgs.hasMoreTokens() )
          {

            String temp = new String( commandArgs.nextToken() );
            if( temp.contains( "Link" ) )
            {
              StringTokenizer commandArgs2 = new StringTokenizer( ( ( String ) temp ) );
              String temp2 = commandArgs.nextToken();
              if( temp2.contains( "detected" ) )
              {
                String status = commandArgs.nextToken();
                if( status.equals( "yes" ) )
                {
                  this.active = true;
                }
                else
                {
                  this.active = false;
                }
              }
            }

          }
        }
      }
    }
  }



  private void wlanIsActive()
  {
    if( this.bandUsageRX > 0.0 || this.bandUsageTX > 0.0 )
    {
      this.active = true;
    }
    else
    {
      this.active = false;
    }
  }



  private void updateSpeed()
  {
    Sigar sigar = new Sigar();
    String tmpSpeed = new String();
    if( OSValidator.isUnix() )
    {
      List response = new ArrayList();
      response = Cmd.executeCommand( this.manager.getethtoolPath() + " " + adapterName );
      for( Object line : response )
      {
        StringTokenizer commandArgs = new StringTokenizer( ( ( String ) line ) );
        if( commandArgs.nextToken().equals( "Speed:" ) )
        {
          tmpSpeed = commandArgs.nextToken();
          break;
        }
      }
      if( tmpSpeed.equals( "10Mb/s" ) )
      {
        this.speed = 10000000;
      }
      else if( tmpSpeed.equals( "100Mb/s" ) )
      {
        this.speed = 100000000;
      }
      // I know it's Ugly but... it works!!
      else if( tmpSpeed.equals( "1000Mb/s" ) || tmpSpeed.equals( "1Gb/s" ) )
      {
        this.speed = 1000000000;
      }
    }
    else if( OSValidator.isWindows() )
    {
      try
      {
        this.speed = sigar.getNetInterfaceStat( this.adapterName ).getSpeed();
      }
      catch( SigarException ex )
      {
        Logger.getLogger( AdapterState.class.getName() ).log( Level.SEVERE, null, ex );
      }
    }
  }



  public long getTotalBytesSent()
  {
    return this.totalByteSent;
  }



  private long getSpeed()
  {
    return this.speed;
  }



  private long getTotalBytesRecv()
  {
    return this.totalByteRecv;
  }

  //to obtain the Date format you've to add .getTime() to the Calendar object


  public Calendar getTimestamp()
  {
    return this.timestamp;
  }



  public int getTotalEndpoints()
  {
    return this.totalEndpoints;
  }



  public float getBandUsageRX()
  {
    return this.bandUsageRX;
  }



  public float getBandUsageTX()
  {
    return this.bandUsageTX;
  }



  public boolean isActive()
  {
    return this.active;
  }



  protected AdapterState getAdapterStateUpdated()
  {
    adapterState = new AdapterState( this.adapterName );
    {
      adapterState.setTimestamp( getTimestamp() );
      adapterState.setSpeed( getSpeed() );
      adapterState.setTotalBytesSent( getTotalBytesSent() );
      adapterState.setTotalBytesRecv( getTotalBytesRecv() );
      adapterState.setTotalEndpoints( getTotalEndpoints() );
      adapterState.setBandUsageRX( getBandUsageRX() );
      adapterState.setBandUsageTX( getBandUsageTX() );
      adapterState.setIsActive( isActive() );
    }
    return this.adapterState;
  }



  protected static void updateAll( NetworkManagerLinux manager )
  {
    Sigar sigar = null;
    sigar = new Sigar();
    String list[] = null;
    AdapterStateUpdater adapterStateUpdater;
    try
    {
      list = sigar.getNetInterfaceList();
    }
    catch( SigarException ex )
    {
      Logger.getLogger( NetworkManagerLinux.class.getName() ).log( Level.SEVERE, null, ex );
    }

    for( String tmp : list )
    {
      if( !tmp.contains( ":" ) ) //not IPv6
      {
        adapterStateUpdater = new AdapterStateUpdater( tmp, manager );
        adapterStateUpdater.start();
        manager.runningThread++;			// this trace active threads' number
      }
    }
  }
}
