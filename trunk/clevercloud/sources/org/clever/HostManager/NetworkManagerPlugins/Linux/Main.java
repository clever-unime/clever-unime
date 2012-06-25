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


import org.hyperic.sigar.SigarException;

/**
 *
 * @authors Patrizio Filloramo & Salvatore Barbera
 */
 class Main {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws SigarException, InterruptedException {
		//Questo serve per specificare la posizione delle librerie
		//System.setProperty("java.library.path", "/home/salvo/NetBeansProjects/NetworkManagerPlugin/lib");
		//NetworkManagerLinux nm = new NetworkManagerLinux();

/*_*
		HostModuleFactory hmf = new HostModuleFactory();
		NetworkManagerPlugin nm = hmf.getNetworkManager();
		List bridges = nm.getBridges();
		for( Object tmp : bridges)
		{
			System.out.println(((BridgeInfo)tmp).toString());
		}
		/*
		int i = bridges.size() - 1;

		for( i = bridges.size() - 1 ; i == 0 ; i--)
		{
			System.out.println("Elimino il bridge " + ((BridgeInfo)bridges.get(i)).getName() + ": " + nm.delBridge(((BridgeInfo)bridges.get(i))));
		}

		for( Object tmp : nm.getBridges())
		{
			System.out.println(((BridgeInfo)tmp).toString());
		}

/*_*
		System.out.println(nm.getRouteRules());
		//RouteInfo route1 = new RouteInfo(new IPAddress("192.168.1.3"), "eth0");
		RouteInfo route2 = new RouteInfo(new IPAddress("192.168.1.0"), "wlan0", "255.255.255.0", new IPAddress("0.0.0.0"));
		//System.out.println("Aggiungo una regola " + nm.createRoute(route1));
		System.out.println("Aggiungo una regola " + nm.createRoute(route2));
		System.out.println(nm.getRouteRules());
		//System.out.println("Rimuovo una regola " + nm.delRoute(route1));
		System.out.println("Rimuovo una regola " + nm.delRoute(route2));
		System.out.println(nm.getRouteRules());
/*_*/
		
		//System.out.println("=== INTERFACCIA INFO ===");
		//System.out.println(nm.getAdapterInfo("eth4").toString());
		
		//System.out.println("=== INTERFACCIA STATE ===");
		//System.out.println(nm.getAdaptersState().toString());
		/*
		for( Object tmp : nm.getAdaptersInfo())
		{
			System.out.println(nm.getAdapterState(((AdapterInfo)tmp).getName()).toString());
		}
		*/
		//Thread.sleep(2000);

		//System.out.println(nm.getAdapterState("eth4").toString());
/*_*
		System.out.println("=== INTERFACCE ADAPTERINFO ===");
		for( Object tmp : nm.getAdaptersInfo())
		{
			System.out.println(((AdapterInfo)(tmp)).toString());
		}

/*_*
		System.out.println("=== INTERFACCE ADAPTERSTATE ===");
		for (Object tmp : nm.getAdaptersState())
		{
			System.out.println(((AdapterState)(tmp)).toString());
		}

 
/*_*
		System.out.println("=== REGOLE DI FIREWALLING ===");
		FlowType chain = null;
		Policy policy = null;

		Endpoint a = new Endpoint("172.17.70.90", 123);
		Endpoint b = new Endpoint("192.168.10.10", 124);
		FirewallRule rule = new FirewallRule(a, b, "tcp", "eth0", chain.INPUT, policy.ACCEPT);
		System.out.println("Aggiunta regola di firewalling: " + nm.appendFirewallRule(rule));

		Endpoint c = new Endpoint("172.17.70.90", 234);
		Endpoint d = new Endpoint("192.168.58.2", 12);
		FirewallRule rule1 = new FirewallRule(c, d, "tcp", "eth0", chain.INPUT, policy.ACCEPT);
		System.out.println("Aggiunta regola di firewalling: " + nm.addFirewallRule(rule1,0));

		System.out.println("=== STAMPA REGOLE DI FIREWALLING ===");
		for(Object tmp : nm.getFirewallRules())
		{
			System.out.println(((FirewallRule)(tmp)).toString());
		}

		System.out.println("Cancellazione regola di firewalling: " + nm.removeFirewallRule(rule));
		System.out.println("Cancellazione regola di firewalling: " + nm.removeFirewallRule(rule1));

		System.out.println("=== STAMPA REGOLE DI FIREWALLING ===");
		for(Object tmp : nm.getFirewallRules())
		{
			System.out.println(((FirewallRule)(tmp)).toString());
		}
/*_*
		System.out.println("=== CREO  UN BRIDGE ===");
		BridgeInfo bridge = new BridgeInfo("br0");
		bridge.addAdapter("eth0");
		bridge.addAdapter("wlan0");
		//bridge.addAdapter("wlan1"); // decommentando questo fa il controllo e salta la creazione del bridge restituendo false in quanto non trova l'adapter
		System.out.println("Creazione bridge: " + nm.createBridge(bridge));

		System.out.println("=== LISTA DEI BRIDGE ===");
		for(Object tmp : nm.getBridges())
		{
			System.out.println(((BridgeInfo)tmp).toString());
		}

		System.out.println("=== INTERFACCE ADAPTERINFO PRIMA DELLA CANCELLAZIONE DEL BRIDGE ===");
		for( Object tmp : nm.getAdaptersInfo())
		{
			System.out.println(((AdapterInfo)(tmp)).toString());
		}

		System.out.println("Cancellazione bridge: " + nm.delBridge(bridge));
		System.out.println("=== INTERFACCE ADAPTERINFO DOPO LA CANCELLAZIONE DEL BRIDGE ===");
		for( Object tmp : nm.getAdaptersInfo())
		{
			System.out.println(((AdapterInfo)(tmp)).toString());
		}
/*_*
		System.out.println("=== REGOLE DI ROUTING ===");
		for(Object tmp : nm.getRouteRules())
		{
			System.out.println(((RouteInfo)(tmp)).toString());
		}

		RouteInfo routeToHost = new RouteInfo(new IPAddress("192.168.1.3"), "eth0");
		RouteInfo routeToNet = new RouteInfo(new IPAddress("192.168.1.0"), "eth0", "255.255.255.0", new IPAddress("192.168.1.3") );
		System.out.println("Aggiungo regola di routing:" + nm.createRoute(routeToHost));
		System.out.println("Aggiungo regola di routing:" + nm.createRoute(routeToNet));
		
		System.out.println("=== REGOLE DI ROUTING ===");
		for(Object tmp : nm.getRouteRules())
		{
			System.out.println(((RouteInfo)(tmp)).toString());
		}
		System.out.println("Rimuovo regola di routing:" + nm.delRoute(routeToHost));
		System.out.println("Rimuovo regola di routing:" + nm.delRoute(routeToNet));
		System.out.println("=== REGOLE DI ROUTING ===");
		for(Object tmp : nm.getRouteRules())
		{
			System.out.println(((RouteInfo)(tmp)).toString());
		}
/*_*
		TunnelInfo tunnel = new TunnelInfo("provatunnel", new IPAddress("102.168.1.5"), new IPAddress("192.168.1.6"), TunnelMode.sit, "eth0");
		System.out.println("Creo il tunnel: " + nm.createTunnel(tunnel));
		System.out.println("=== LISTA TUNNEL PRIMA DELLA CANCELLAZIONE DEL TUNNEL ===");
		for( Object tmp : nm.getTunnels())
		{
			System.out.println(((TunnelInfo)(tmp)).toString());
		}
		System.out.println("=== AdapterInfo del tunnel ===");
		System.out.println(nm.getAdapterInfo(tunnel.getName()).toString());
		System.out.println("Rimuovo il tunnel: " + nm.delTunnel(tunnel));

		System.out.println("=== LISTA TUNNEL DOPO DELLA CANCELLAZIONE DEL TUNNEL ===");
		for( Object tmp : nm.getTunnels())
		{
			System.out.println(((TunnelInfo)(tmp)).toString());
		}
/*_*/
	}

}
