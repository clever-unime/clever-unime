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

public class TunnelInfo
{
	private String name;
	private IPAddress local;
	private IPAddress remote;
	private TunnelMode mode;
	private	String device = "";

	public TunnelInfo (String name, IPAddress local, IPAddress remote, TunnelMode mode)
	{
		this.setName(name);
		this.setLocal(local);
		this.setRemote(remote);
		this.setMode(mode);
	}

	public TunnelInfo (String name, IPAddress local, IPAddress remote, TunnelMode mode, String adapterName)
	{
		this(name, local, remote, mode);
		this.setDevice(adapterName);
	}

	private void setName(String name)
	{
		this.name = name;
	}

	private void setLocal(IPAddress local)
	{
		this.local = local;
	}

	private void setRemote(IPAddress remote)
	{
		this.remote = remote;
	}

	private void setDevice(String adapterName)
	{
		this.device = adapterName;
	}

	private void setMode(TunnelMode tunnelMode)
	{
		this.mode = tunnelMode;
	}

	public String getName()
	{
		return this.name;
	}

	public IPAddress getRemote()
	{
		return this.remote;
	}

	public IPAddress getLocal()
	{
		return this.local;
	}

	public TunnelMode getTunnelMode()
	{
		return this.mode;
	}

	public String getAdapterName()
	{
		return this.device;
	}

	public String toString()
	{
		String str;
		str = "TunnelName:	 " + this.getName() + "\n";
		str += "Interface :	 " + this.getAdapterName() + "\n";
		str += "LocalAddress :  " + this.getLocal().getAddress() + "\n";
		str += "RemoteAddress : " + this.getRemote().getAddress() + "\n";
		str += "TunnelMode:	 " + this.getTunnelMode() + "\n";
		return str;
	}
}
