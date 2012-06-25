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
package org.clever.HostManager.NetworkManager;

import java.util.Calendar;



public class AdapterState
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
  private boolean active = false;
  private long speed = 100000000;



  public AdapterState( String adapterName )
  {
    this.setName( adapterName );
    this.udpState = new UdpState( getName() )
    {
    };
    this.tcpState = new TcpState( getName() )
    {
    };
    this.setTimestamp();
    this.setTotalBytesRecv();
    this.setTotalBytesSent();
    this.setTotalEndpoints();
  }



  public AdapterState()
  {
  }



  private void setName( String name )
  {
    this.adapterName = name;
  }



  public void setBandUsageRX( float bandUsageRX )
  {
    this.bandUsageRX = bandUsageRX;
  }



  public void setBandUsageTX( float bandUsageTX )
  {
    this.bandUsageTX = bandUsageTX;
  }



  private void setTotalBytesRecv()
  {
    this.totalByteRecv = this.tcpState.getByteRecv() + this.udpState.getBytesRecv();
  }



  private void setTotalBytesSent()
  {
    this.totalByteSent = this.tcpState.getByteSent() + this.udpState.getBytesSent();
  }



  private void setTotalEndpoints()
  {
    this.totalEndpoints = udpState.getEndpoints() + tcpState.getEndpoints();
  }



  public void setTimestamp()
  {
    this.timestamp = Calendar.getInstance();
  }



  public String getName()
  {
    return this.adapterName;
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



  public long getTotalBytesSent()
  {
    return this.totalByteSent;
  }



  public long getTotalBytesRecv()
  {
    return this.totalByteRecv;
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



  public long getSpeed()
  {
    return this.speed;
  }

//public set methods we need in AdapterStateUpdater


  public void setTimestamp( Calendar time )
  {
    this.timestamp = time;
  }



  public void setTotalBytesRecv( long totalBytesRecv )
  {
    this.totalByteRecv = totalBytesRecv;
  }



  public void setTotalBytesSent( long totalBytesSent )
  {
    this.totalByteSent = totalBytesSent;
  }



  public void setTotalEndpoints( int totalEndpoints )
  {
    this.totalEndpoints = totalEndpoints;
  }



  public void setIsActive( boolean isActive )
  {
    this.active = isActive;
  }



  public void setSpeed( long speed )
  {
    this.speed = speed;
  }



  @Override
  public String toString()
  {
    String str;
    str = "Interfaccia	   : " + this.getName() + "\n";
    str += "Active			: " + this.isActive() + "\n";
    str += "Band usage TX	 : " + this.getBandUsageTX() + "%" + "\n";
    str += "Band usage RX	 : " + this.getBandUsageRX() + "%" + "\n";
    str += "Total bytes Recv  : " + this.getTotalBytesRecv() + "\n";
    str += "Total bytes Sent  : " + this.getTotalBytesSent() + "\n";
    str += "TIMESTAMP		 : " + this.getTimestamp().getTime() + "\n";
    return str;
  }
  //protected abstract void getData();
}
