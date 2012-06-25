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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 *
 * @authors Patrizio Filloramo & Salvatore Barbera
 */
public class AdapterInfo {

  private String name = "";
  private String macAddress;
  private IPAddress ipv4Address;
  private IPAddress ipv6Address;
  private IPAddress gateway;
  private IPAddress broadcast;
  private String mask;
  private float mtu;
  private String type;
  private Sigar sigar;
  private NetInterfaceConfig netInterfaceInfo;
  private long metric;
  private List ipv6Addresses;
  private boolean hasMorev6Addresses = false;

  AdapterInfo() {}

  public AdapterInfo(String name)
  {
    this.sigar = new Sigar();
    this.netInterfaceInfo= new NetInterfaceConfig();

    //Initializing the NetInterfaceConfig class...
    try 
    {
      this.netInterfaceInfo=sigar.getNetInterfaceConfig(name);
    }
    catch (SigarException ex)
    {
      Logger.getLogger(AdapterInfo.class.getName()).log(Level.SEVERE, null, ex);
    }

    this.setName(this.netInterfaceInfo.getName());
   this.setMacAddress(this.netInterfaceInfo.getHwaddr());
    this.setIPAddresses();
    this.setMask(this.netInterfaceInfo.getNetmask());
    this.setMtu(this.netInterfaceInfo.getMtu());
    this.setType(this.netInterfaceInfo.getType());
    this.setBroadcast(this.netInterfaceInfo.getBroadcast());
    this.setGateway(this.netInterfaceInfo.getDestination());
    this.setMetric(this.netInterfaceInfo.getMetric());
  }

  public String getName()
  {
    return this.name;
  }

  public String getMACAddress()
  {
    return this.macAddress;
  }

  public IPAddress getIPv4Address()
  {
    return this.ipv4Address;
  }

  public IPAddress getIPv6Address()
  {
    return this.ipv6Address;
  }

  public List getIPv6Addresses()
  {
    return this.ipv6Addresses;
  }

  public IPAddress getBroadcast()
  {
    return this.broadcast;
  }

  public String getMask()
  {
    return this.mask;
  }

  public float getMTU()
  {
    return this.mtu;
  }

  public String getType()
  {
    return this.type;
  }

  private void setName(String name)
  {
    this.name = name;
  }

 

  private void setMask(String netmask)
  {
    this.mask = netmask;
  }

  private void setMtu(long mtu)
  {
    this.mtu = mtu;
  }

  private void setType(String type)
  {
    this.type = type;
  }

  private void setIPAddresses()
  {
    this.ipv4Address = new IPAddress("0.0.0.0");
    this.ipv6Address = new IPAddress("0::0");
    this.ipv6Addresses = new ArrayList();
    Enumeration<NetworkInterface> interfaces = null;
    try
    {
      interfaces = NetworkInterface.getNetworkInterfaces(); //Enumeration of interfaces
    }
    catch (SocketException ex)
    {
      Logger.getLogger(AdapterInfo.class.getName()).log(Level.SEVERE, null, ex);
    }
    while (interfaces.hasMoreElements())
    {
      NetworkInterface nif = interfaces.nextElement();
      if (nif.getName().equals(getName()))
      {
        try
        {
          Enumeration<InetAddress> inetaddresses = NetworkInterface.getByName(getName()).getInetAddresses();
          while (inetaddresses.hasMoreElements())
          {
            InetAddress inetAddress = inetaddresses.nextElement();
            if (inetAddress instanceof Inet6Address) //has this adapter an IPV6 Address?
            {
              this.ipv6Addresses.add(new IPAddress(inetAddress.getHostAddress()));
              if (this.ipv6Addresses.size() > 1)
                this.hasMorev6Addresses = true;
              this.ipv6Address = (IPAddress) this.ipv6Addresses.get(0);
            }
            if (inetAddress instanceof Inet4Address)
            {
                    this.ipv4Address = new IPAddress(inetAddress.getHostAddress());
            }
          }
        }
        catch (SocketException ex)
        {
          Logger.getLogger(AdapterInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        break;
      }
    }
  }

	private void setBroadcast(String broadcast)
	{
		this.broadcast = new IPAddress(broadcast);
	}

	private void setGateway(String destination)
	{
		this.gateway = new IPAddress(destination);
	}

	public IPAddress getGateway()
	{
		return this.gateway;
	}

	private void setMetric(long metric)
	{
		this.metric = metric;
	}

	public long getMetric()
	{
		return this.metric;
	}
	
	public boolean hasMoreIPv6()
	{
		return this.hasMorev6Addresses;
	}
	@Override
	public String toString()
	{
			String str;
			str = "Interface : " + this.getName() + "\n";
			str +="MAC :	   " + this.getMACAddress() + "\n";
			str +="IPv4 :	  " + this.getIPv4Address().getAddress() + "\n";
			if(!this.getIPv4Address().getAddress().equals("0.0.0.0"))
				str +="IPv4 bin :  " + this.getIPv4Address().toBinary() + "\n";
			if(!this.getIPv6Address().getAddress().equals("0::0"))
				str +="IPv6 :	  " + this.getIPv6Address().getAddress() + "\n";
			str +="Netmask :   " + this.getMask() + "\n";
			str +="Broadcast : " + this.getBroadcast().getAddress() + "\n";
			str +="Gateway :   " + this.getGateway().getAddress() + "\n";
			str +="Metric :	" + this.getMetric() + "\n";
			str +="MTU :	   " + this.getMTU() + "\n";
			str +="Type :	  " + this.getType()+ "\n";
			return str;
	}

    /**
     * @param macAddress the macAddress to set
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * @return the netInterfaceInfo
     */
    public NetInterfaceConfig getNetInterfaceInfo() {
        return netInterfaceInfo;
    }
}