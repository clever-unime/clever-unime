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

import org.clever.Common.Plugins.RunnerPlugin;
import java.util.List;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Communicator.ModuleCommunicator;


/**
 *
 * @author Patrizio Filloramo & Salvatore Barbera
 */
public interface NetworkManagerPlugin extends RunnerPlugin
{
	public AdapterInfo getAdapterInfo(String name);
	public List getAdaptersInfo();
	public AdapterState getAdapterState(String name);
	public List getAdaptersState();
	public boolean createRoute(RouteInfo route);
	public boolean delRoute(RouteInfo route);
	public boolean existRoute(RouteInfo route);
	public List getRouteRules();
	public boolean addFirewallRule(FirewallRule rule, int priority);
	public boolean appendFirewallRule(FirewallRule rule);
	public boolean removeFirewallRule(FirewallRule rule);
	public boolean existFirewallRule(FirewallRule rule);
	public List getFirewallRules();
	public boolean createBridge(BridgeInfo bridge);
	public boolean delBridge(BridgeInfo bridge);
	public boolean existBridge(BridgeInfo bridge);
	public List getBridges();
	public boolean createTunnel(TunnelInfo tunnel);
	public boolean delTunnel(TunnelInfo tunnel);
	public boolean existTunnel(TunnelInfo tunnel);
	public List getTunnels();
        
        public Integer getPort(Integer inf_port, Integer sup_port, String ip);
        public List listBusyPorts(Integer inf_port, Integer sup_port, String ip);
        public List listFreePorts(Integer inf_port, Integer sup_port, String ip);
        public Boolean isPortBusy(Integer port, String ip);
        public void setOwner(Agent owner);
}
