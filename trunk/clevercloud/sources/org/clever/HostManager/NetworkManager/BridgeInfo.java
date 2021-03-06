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

import java.util.ArrayList;
import java.util.List;

public class BridgeInfo
{
	private String name;
	private boolean STP = false;
	private List adapters;

	public BridgeInfo (String name)
	{
		this.setName(name);
		this.adapters = new ArrayList();
		return;
	}

	public BridgeInfo (String name, List adapters)
	{
		this(name);
		this.setAdapters(adapters);
		return;
	}

	public BridgeInfo (String name, List adapters, boolean stp)
	{
		this(name, adapters);
		this.STP = stp;
		return;
	}

	private void setName(String name)
	{
		this.name = name;
		return;
	}

	public String getName()
	{
		return this.name;
	}

	public Boolean addAdapter(String adapter)
	{
		this.adapters.add(adapter);
		return true;
	}

	public Boolean delAdapter(String adapter)
	{
		this.adapters.remove(adapter);
		return true;
	}
	
	private void setAdapters(List adapters)
	{
		this.adapters = adapters;
	}

	public List getAdapters()
	{
		return this.adapters;
	}

	protected void setSTP(boolean stp)
	{
		this.STP = stp;
	}

	public boolean getSTP()
	{
		return this.STP;
	}

	public boolean exist(BridgeInfo bridge)
	{
		if(	this.name.equals(bridge.getName() ))
		{
			return true;
		}
		else return false;
	}

	public String toString()
	{
		String str;
		str =  "Nome del Bridge : "+ this.getName() + "\n";
		str += "Interfacce connesse a " + this.getName() + " :";
		for(Object tmp : this.getAdapters() )
		{
			str += " " + (String)tmp;
		}
		str += "\n";
		str += "STP :	  " + this.getSTP() + "\n";
		return str;
	}
}
