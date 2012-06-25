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


import java.util.logging.Level;
import java.util.logging.Logger;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public abstract class TcpState extends AdapterState
{
	private int endpoints=0;
	private int listeners=0;
	private int outboundConnections=0;
	private int inboundConnections=0;
	private long bytesSent=0;
	private long bytesRecv=0;
	private String adapter = "";
	private Sigar sigar;

	public TcpState(String adapter)
	{
		this.setName(adapter);
		this.setEndpoints();
		this.setListeners();
		this.setOutboundConnections();
		this.setInboundConnections();
		this.setBytesSent();
		this.setBytesRecv();
	}

	private void setName(String adapter)
	{
		this.adapter=adapter;
	}

	private void setEndpoints()
	{
		sigar = new Sigar();
		try
		{
			this.endpoints=sigar.getNetStat().getTcpEstablished();
		}
		catch (SigarException ex)
		{
			Logger.getLogger(TcpState.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void setListeners()
	{
		sigar = new Sigar();
		try
		{
			this.listeners=sigar.getNetStat().getTcpListen();
		}
		catch (SigarException ex)
		{
			Logger.getLogger(TcpState.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void setOutboundConnections()
	{
		sigar = new Sigar();
		try 
		{
			this.outboundConnections = sigar.getNetStat().getTcpOutboundTotal();
		}
		catch (SigarException ex)
		{
			Logger.getLogger(TcpState.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void setInboundConnections()
	{
		sigar = new Sigar();
		try
		{
			this.inboundConnections = sigar.getNetStat().getTcpInboundTotal();
		}
		catch (SigarException ex)
		{
			Logger.getLogger(TcpState.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void setBytesSent()
	{
		sigar = new Sigar();
		try
		{
			this.bytesSent=sigar.getNetInterfaceStat(this.adapter).getTxBytes();
		}
		catch (SigarException ex)
		{
			Logger.getLogger(AdapterState.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void setBytesRecv()
	{
		sigar = new Sigar();
		try
		{
			this.bytesRecv=sigar.getNetInterfaceStat(this.adapter).getRxBytes();
		}
		catch (SigarException ex)
		{
			Logger.getLogger(AdapterState.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public String getName()
	{
		return this.adapter;
	}
	public long getByteRecv()
	{
		return this.bytesRecv;
	}
	public long getByteSent()
	{
		return this.bytesSent;
	}
	public int getEndpoints()
	{
		return this.endpoints;
	}
	public int getListeners()
	{
		return this.listeners;
	}
	public int getOutboundConnections()
	{
		return this.outboundConnections;
	}
	public int getInboundConnections()
	{
		return this.inboundConnections;
	}
	protected void getData() {}

}
