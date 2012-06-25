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
import org.hyperic.sigar.Sigar;



public abstract class UdpState extends AdapterState
{
	private int endpoints=0;
	private long bytesSent=0;
	private long bytesRecv=0;
	private Sigar sigar;
	private String adapter;

	public UdpState(String adapter)
	{
		setName(adapter);
		//setEndpoints();
		//setBytesRecv();
		//setBytesSent();
	}

	public int getEndpoints( )
	{
		//???
		return this.endpoints;
	}

	public long getBytesSent()
	{
		return this.bytesSent;
	}

	public long getBytesRecv()
	{
		return this.bytesRecv;
	}

	private void setName(String adapter)
	{
		this.adapter=adapter;
	}

	private void setEndpoints()	{}

	private void setBytesRecv() {}

	private void setBytesSent() {}

	
}
