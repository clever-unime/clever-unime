/*
 * Copyright [2014] [Università di Messina]
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

public class RouteInfo
{
	private IPAddress destination;
	private String mask;
	private IPAddress nextHop;
	private String adapterName;
	private int metric = 0;
	private boolean toHost = false;

	public RouteInfo(IPAddress destination, String adapterName, String mask, IPAddress nextHop, int metric, boolean toHost)
	{
		this.setDestination(destination);
		this.setMask(mask);
		this.setNextHop(nextHop);
		this.setAdapterName(adapterName);
		this.setMetric(metric);
		this.setToHost(toHost);
	}

	public RouteInfo(IPAddress destination, String adapterName, String mask, IPAddress nextHop)
	{
		this.setDestination(destination);
		this.setMask(mask);
		this.setNextHop(nextHop);
		this.setAdapterName(adapterName);
	}

	public RouteInfo(IPAddress destination, String adapterName)
	{
		this(destination, adapterName, "255.255.255.255", new IPAddress("0.0.0.0"));
		this.setToHost(true);
	}


	public IPAddress getDestination()
	{
		return this.destination;
	}

	public String getMask()
	{
		return this.mask;
	}

	public IPAddress getNextHop()
	{
		return this.nextHop;
	}

	public String getAdapterName()
	{
		return this.adapterName;
	}

	public int getMetric()
	{
		return this.metric;
	}

	public boolean getToHost()
	{
		return this.toHost;
	}

	public boolean getToNetwork()
	{
		return !this.toHost;
	}

	public boolean equals(RouteInfo route)
	{
		if(this.destination.getAddress().equals(route.getDestination().getAddress()) &&
				this.adapterName.equals(route.getAdapterName()) &&
				this.mask.equals(route.getMask()) &&
				this.metric == route.getMetric() &&
				this.nextHop.getAddress().equals(route.getNextHop().getAddress())
				)
		{
			return true;
		}
		return false;
	}

	private void setMask(String mask) {
		this.mask = mask;
	}

	private void setNextHop(IPAddress nh) {
		this.nextHop = nh;
	}

	private void setAdapterName(String name) {
		this.adapterName = name;
	}

	private void setDestination(IPAddress dst) {
		this.destination = dst;
	}

	private void setMetric(int metric) {
		this.metric = metric;
	}

	private void setToHost(boolean toHost)
	{
		this.toHost = toHost;
	}

	public String toString()
	{
		String str;
		str = "Interfaccia : " + this.getAdapterName() + "\n";
		str += "Destination : " + this.getDestination().getAddress().toString() + "\n";
		str += "Netmask :	 " + this.getMask() + "\n";
		str += "Gateway :	 " + this.getNextHop().getAddress().toString() + "\n";
		str += "To Host? :	" + this.getToHost() + "\n";
		str += "Metric :	  " + this.getMetric() + "\n";
		return str;
	}
}
