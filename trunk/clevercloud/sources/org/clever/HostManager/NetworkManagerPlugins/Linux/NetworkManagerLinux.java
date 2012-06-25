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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.HostManager.NetworkManager.*;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.jdom.Element;



public class NetworkManagerLinux implements NetworkManagerPlugin
{
  private Agent owner;
  private static String errorStr;
  private List adaptersInfo;
  private List adaptersState;
  private List firewallRules = null;
  private List bridges = null;
  private List routeRules = null;
  private List tunnels = null;
  private Sigar sigar;
  private String iptablesPath;
  private String bridgePath;
  private String routePath;
  private String ipPath;
  private String iptablesSavePath;
  private String ethtoolPath;
  protected static boolean loading = true;
  protected int runningThread = 0;
  private boolean isRoot = false;
  private boolean hasBrctl = false;
  private boolean hasEthtool = false;
  private PluginDescription pluginDescription;
  private Logger logger = null;
	


  public NetworkManagerLinux() //cmq i log non funzionano nemmeno qui!!!
  {
      logger = Logger.getLogger( "NetworkManagerLinux" );
      this.logger.info("Network Manager plugin created: ");
  }

    @Override
    public void init(Element params, Agent owner) throws CleverException { 
        if(params!=null){
            //Read param from configuration_networkManager.xml
        }
        this.init(owner);        
    }

    private void init(Agent owner) throws CleverException {
        this.owner=owner;
        this.adaptersInfo = new ArrayList();
        this.adaptersState = new ArrayList();
        //this.adaptersState=Collections.synchronizedList(this.adaptersState);
        this.bridges = new ArrayList();
        //this.bridges = Collections.synchronizedList(this.bridges);

        
        this.isRoot();
        if( this.checkRoot() )
        {
            if( this.setPaths() )
            {
                this.setAdaptersInfo();
                this.setAdaptersState();
                this.setFirewallRules();
                logger.info("1");
                this.setBridges();
                logger.info("1");
                this.setRouteRules();
                logger.info("1");
                this.setTunnels();
                logger.info("1");
            }
            else
            {
                System.out.println( "Cannot start Network Manager" );
            }
        }
        else
        {
            this.setAdaptersInfo();
            System.out.println( "Your're not root -> need root privileges" );
        }

        pluginDescription = new PluginDescription( "Network Manager", "0.9", "Network management tool", "java+bridge-utils+ethtool", "" );
        logger.info( "NetworkManager plugin initialized");
    }

  private void setAdaptersInfo()
  {
    this.sigar = new Sigar();
    String list[] = null;
    try
    { 
      list = this.sigar.getNetInterfaceList();
    }
    catch( SigarException ex )
    {
      logger.error( ex );

    }

    for( String tmp : list )
    {
      if( !( ( ( String ) tmp ).contains( ":" ) ) )
      {
        this.adaptersInfo.add( new AdapterInfo( tmp ) );
      }
    }
  }



  public AdapterInfo getAdapterInfo( String adapterName )
  {
    AdapterInfo adapterInfo = null;
    for( Object tmp : this.adaptersInfo )
    {
      if( ( ( AdapterInfo ) tmp ).getName().equals( adapterName ) )
      {
        adapterInfo = ( ( AdapterInfo ) tmp );
        break;
      }
    }
    return adapterInfo;
  }



  public List getAdaptersInfo()
  {
    return this.adaptersInfo;
  }



  private void setAdaptersState()
  {
    this.sigar = new Sigar();
    String list[] = null;
    AdapterState adapterState;
    try
    {
      list = this.sigar.getNetInterfaceList();
    }
    catch( SigarException ex )
    {
      logger.error( ex );
    }

    for( String tmp : list )
    {
      if( !tmp.contains( ":" ) ) //not IPv6
      {
        adapterState = new AdapterState( tmp );
        this.addAdapaterState( adapterState );
      }
    }
  }



  public AdapterState getAdapterState( String name )
  {
    updateAdaptersState();
    return getAdapterStateByName( name );
  }



  public List getAdaptersState()
  {
    updateAdaptersState();
    return this.adaptersState;
  }



  private void updateAdaptersState()
  {
    AdapterStateUpdater.updateAll( this );
    while( this.isLoading() )
    {
      try
      {
        Thread.sleep( 300 );
      }
      catch( InterruptedException ex )
      {
        logger.error( ex );
      }
    }
  }



  private AdapterState getAdapterStateByName( String adapter )
  {
    for( Object tmp : adaptersState )
    {
      if( ( ( AdapterState ) tmp ).getName().equals( adapter ) )
      {
        return ( ( AdapterState ) tmp );
      }
    }
    return null;
  }



  private void setRouteRules()
  {
    this.routeRules = new ArrayList();
    //this.routeRules = Collections.synchronizedList(this.routeRules);
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      List response = new ArrayList();
      response = Cmd.executeCommand( "cat /proc/net/route" );
      for( Object line : response )
      {
        StringTokenizer commandArgs = new StringTokenizer( ( ( String ) line ), "\t" );

        while( commandArgs.hasMoreTokens() )
        {
          String temp = commandArgs.nextToken();
          if( this.existAdapter( temp ) )
          {
            String dst = IPAddress.hexToIp( commandArgs.nextToken() );
            String nh = IPAddress.hexToIp( commandArgs.nextToken() );
            String flags = commandArgs.nextToken();

            commandArgs.nextToken();
            commandArgs.nextToken();

            int metric = Integer.parseInt( commandArgs.nextToken() );
            String mask = new String( IPAddress.hexToIp( commandArgs.nextToken() ) );

            commandArgs.nextToken();
            commandArgs.nextToken();
            commandArgs.nextToken();
            boolean toHost = false;

            if( flags.equals( "0005" ) )  // 0005 means that there's a route to host
            {
              toHost = true;
            }

            routeRules.add( new RouteInfo( new IPAddress( dst ), temp, mask, new IPAddress( nh ), metric, toHost ) );
          }
        }
      }
    }
  }



  public boolean createRoute( RouteInfo route )
  {
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      if( this.existAdapter( route.getAdapterName() ) && !existRoute( route ) )
      {
        String routeCommand = new String( this.routePath );
        routeCommand += " add";
        routeCommand += this.getRouteCommand( route );
        if( Cmd.executeCommand( routeCommand ).isEmpty() )
        {
          this.routeRules.add( route );
          return true;
        }
      }
    }
    return false;
  }



  public boolean delRoute( RouteInfo route )
  {
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      if( this.existAdapter( route.getAdapterName() ) && existRoute( route ) )
      {
        String routeCommand = new String( this.routePath );
        routeCommand += " del";
        routeCommand += this.getRouteCommand( route );
        if( Cmd.executeCommand( routeCommand ).isEmpty() )
        {
          this.routeRules.remove( this.getRouteRule( route ) );
          return true;
        }
      }
    }
    return false;
  }



  private void setFirewallRules()
  {
    this.firewallRules = new ArrayList();
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      List response = new ArrayList();
      response = Cmd.executeCommand( this.iptablesSavePath );


      for( Object line : response )
      {
        try
        {
          if( ( ( String ) line ).charAt( 0 ) == '-' )
          {
            FirewallRule rule = new FirewallRule();
            int port = -1;
            IPAddress srcAddress = new IPAddress( "0.0.0.0" );
            IPAddress dstAddress = new IPAddress( "0.0.0.0" );
            StringTokenizer commandArgs = new StringTokenizer( ( ( String ) line ) );
            while( commandArgs.hasMoreTokens() )
            {
              String temp = new String( commandArgs.nextToken() );
              if( temp.equals( "-A" ) )
              {
                rule.setChain( FlowType.valueOf( commandArgs.nextToken() ) );
              }
              else if( temp.equals( "-s" ) )
              {
                srcAddress = new IPAddress( commandArgs.nextToken() );
              }
              else if( temp.equals( "-d" ) )
              {
                dstAddress = new IPAddress( commandArgs.nextToken() );
              }
              else if( temp.equals( "-i" ) )
              {
                rule.setAdapter( commandArgs.nextToken() );
              }
              else if( temp.equals( "-p" ) )
              {
                rule.setProtocol( commandArgs.nextToken() );
              }
              else if( temp.equals( "--dport" ) )
              {
                port = Integer.parseInt( commandArgs.nextToken() );
              }
              else if( temp.equals( "-j" ) )
              {
                rule.setPolicy( Policy.valueOf( commandArgs.nextToken() ) );
              }
            }
            rule.setSrc( new Endpoint( srcAddress.getAddress(), -1 ) );
            rule.setDst( new Endpoint( dstAddress.getAddress(), port ) );
            this.firewallRules.add( rule );
          }

        }
        catch( IllegalArgumentException ex )
        {
          logger.error( "Firewall rule mismatch: " + line + ex.toString() );
        }
      }
    }
  }



  public List getFirewallRules()
  {
    return this.firewallRules;
  }



  public boolean addFirewallRule( FirewallRule rule, int priority )
  {
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      if( this.existAdapter( rule.getAdapter() ) && !existFirewallRule( rule ) )
      {
        String iptables = new String( this.iptablesPath );
        iptables += " -A ";
        iptables += this.getIptablesCommand( rule );
        if( Cmd.executeCommand( iptables ).isEmpty() )
        {
          this.firewallRules.add( priority, rule );
          return true;
        }
      }
    }
    return false;
  }



  public boolean removeFirewallRule( FirewallRule rule )
  {
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      if( this.existAdapter( rule.getAdapter() ) && existFirewallRule( rule ) )
      {
        String iptables = new String( this.iptablesPath );
        iptables += " -D ";
        iptables += this.getIptablesCommand( rule );
        if( Cmd.executeCommand( iptables ).isEmpty() )
        {
          this.firewallRules.remove( this.getFirewallRule( rule ) );
          return true;
        }
      }
    }
    return false;
  }



  public boolean appendFirewallRule( FirewallRule rule )
  {
    return this.addFirewallRule( rule, this.firewallRules.size() );
  }



  public boolean createTunnel( TunnelInfo tunnel )
  {
    if( OSValidator.isUnix() && this.checkRoot() && !this.existTunnel( tunnel ) )
    {
      String tunnelCommand = new String( this.ipPath );
      tunnelCommand += " tunnel add ";
      tunnelCommand += tunnel.getName();
      tunnelCommand += " mode " + tunnel.getTunnelMode();
      tunnelCommand += " remote " + tunnel.getRemote().getAddress();
      tunnelCommand += " local " + tunnel.getLocal().getAddress();
      if( existAdapter( tunnel.getAdapterName() ) )
      {
        tunnelCommand += " dev " + tunnel.getAdapterName();
      }

      if( Cmd.executeCommand( tunnelCommand ).isEmpty() )
      {

        this.tunnels.add( tunnel );
        this.adaptersInfo.add( new AdapterInfo( tunnel.getName() ) );
        return true;
      }
    }
    return false;
  }



  public boolean delTunnel( TunnelInfo tunnel )
  {
    if( existAdapter( tunnel.getName() ) )
    {
      String tunnelCommand = new String( this.ipPath );
      tunnelCommand += " tunnel del " + tunnel.getName();
      if( Cmd.executeCommand( tunnelCommand ).isEmpty() )
      {
        this.tunnels.remove( tunnel );
        this.adaptersInfo.remove( this.getAdapterInfo( tunnel.getName() ) );
        this.adaptersState.remove( this.getAdapterStateByName( tunnel.getName() ) );
        return true;
      }
    }
    return false;
  }



  public List getTunnels()
  {
    return this.tunnels;
  }



  public boolean existTunnel( TunnelInfo tunnel )
  {
    for( Object tmp : this.tunnels )
    {
      if( ( ( TunnelInfo ) tmp ).getName().equals( tunnel.getName() ) )
      {
        return true;
      }
    }
    return false;
  }



  public boolean createBridge( BridgeInfo bridge )
  {
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      int adaptersExist = 0;
      for( Object tmp1 : adaptersInfo )
      {
        for( Object tmp2 : bridge.getAdapters() )
        {
          if( ( ( AdapterInfo ) tmp1 ).getName().equals( ( String ) tmp2 ) )
          {
            adaptersExist++; // check if all bridge's adapters exist
          }
        }
      }

      if( adaptersExist == bridge.getAdapters().size() && !existBridge( bridge ) )
      {
        List bridgeCommands = new ArrayList();
        bridgeCommands.add( this.bridgePath + " addbr " + bridge.getName() );
        for( Object tmp : bridge.getAdapters() )
        {
          bridgeCommands.add( this.bridgePath + " addif " + bridge.getName() + " " + ( ( String ) tmp ) );
        }

        if( bridge.getSTP() )
        {
          bridgeCommands.add( this.bridgePath + " stp " + bridge.getName() + " on" );
        }
        else
        {
          bridgeCommands.add( this.bridgePath + " stp " + bridge.getName() + " off" );
        }

        for( Object tmp : bridgeCommands )
        {
          if( Cmd.executeCommand( ( String ) tmp ).size() == 0 )
          {
            continue;
          }
          else
          {
            if( !existBridge( bridge ) )
            {
              this.bridges.add( bridge );
              this.delBridge( bridge );
            }
            return false;
          }
        }
        this.bridges.add( bridge );
        this.adaptersInfo.add( new AdapterInfo( bridge.getName() ) );
        return true;
      }
    }
    return false;
  }



  public boolean delBridge( BridgeInfo bridge )
  {
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      if( existBridge( bridge ) )
      {
        if( Cmd.executeCommand( this.bridgePath + " delbr " + bridge.getName() ).isEmpty() )
        {
          this.bridges.remove( this.getBridgeInfo( bridge ) );
          this.adaptersInfo.remove( this.getAdapterInfo( bridge.getName() ) );
          this.adaptersState.remove( this.getAdapterStateByName( bridge.getName() ) );
          return true;
        }
        else
        {
          return false;
        }
      }
    }
    return false;
  }



  public boolean updateBridge( BridgeInfo bridge )
  {
    if( bridge.exist( bridge ) )
    {
      if( delBridge( bridge ) && createBridge( bridge ) )
      {
        return true;
      }
    }
    return false;
  }



  public List getBridges()
  {
    return this.bridges;
  }



  private boolean checkRoot()
  {
    return this.isRoot;
  }



  private boolean isRoot()
  {
    if( OSValidator.isUnix() )
    {
      String execStr = "";
      execStr = ( ( String ) Cmd.executeCommand( "whoami" ).get( 0 ) );

      if( execStr.equals( "root" ) )
      {
        this.isRoot = true;
      }
      else
      {
        this.isRoot = false;
      }

      return isRoot;
    }
    else
    {
      return false;
    }
  }



  private boolean setPaths()
  {
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      this.iptablesPath = new String( ( ( String ) Cmd.executeCommand( "which iptables" ).get( 0 ) ) );
      if( ( Cmd.executeCommand( "which brctl" ).isEmpty() ) )
      {
        System.out.println( "brctl not found" );
        if( !Cmd.executeCommand( "cat /etc/debian_version" ).contains( "sid" ) )
        {
          this.hasBrctl = true;
          System.out.println( "Debian's like system found. -> Trying to install bridge-utils" );
          System.out.println( "Check internet access to install packages." );
          Cmd.executeCommand( "apt-get install -y -qq bridge-utils" );
          if( !( Cmd.executeCommand( "which brctl" ).isEmpty() ) )
          {
            this.bridgePath = new String( ( ( String ) Cmd.executeCommand( "which brctl" ).get( 0 ) ) );
            this.hasBrctl = true;
          }
          else
          {
            System.out.println( "Check your Internet connection" );
            return false;
          }
        }
      }
      else
      {
        this.bridgePath = Cmd.executeCommand( "which brctl" ).get( 0 ).toString();
        this.hasBrctl = true;
      }

      this.routePath = new String( ( ( String ) Cmd.executeCommand( "which route" ).get( 0 ) ) );
      this.ipPath = new String( ( ( String ) Cmd.executeCommand( "which ip" ).get( 0 ) ) );
      this.iptablesSavePath = new String( ( ( String ) Cmd.executeCommand( "which iptables-save" ).get( 0 ) ) );

      if( ( Cmd.executeCommand( "which ethtool" ).isEmpty() ) )
      {
        System.out.println( "ethtool not found" );
        if( !Cmd.executeCommand( "cat /etc/debian_version" ).contains( "sid" ) )
        {
          this.hasEthtool = true;
          System.out.println( "Debian's like system found. -> Trying to install ethtool" );
          System.out.println( "Check internet access to install packages." );
          Cmd.executeCommand( "apt-get install -y -qq ethtool" );
          if( !( Cmd.executeCommand( "which ethtool" ).isEmpty() ) )
          {
            this.ethtoolPath = new String( ( ( String ) Cmd.executeCommand( "which ethtool" ).get( 0 ) ) );
            this.hasEthtool = true;
          }
          else
          {
            System.out.println( "Check your Internet connection" );
            return false;
          }
        }
      }
      else
      {
        this.ethtoolPath = Cmd.executeCommand( "which ethtool" ).get( 0 ).toString();
        this.hasEthtool = true;
      }
    }
    return true;
  }



  private String getIptablesCommand( FirewallRule rule )
  {
    String command = new String();
    String temp = new String();
    if( !rule.getChain().toString().isEmpty() && !rule.getPolicy().toString().isEmpty() )
    {
      command += rule.getChain().toString();
      command += " -j " + rule.getPolicy().toString();

      if( !( temp = rule.getAdapter() ).isEmpty() )
      {
        command += " -i " + temp;
      }

      if( !( temp = rule.getSrc().getAddress() ).isEmpty() )
      {
        command += " -s " + temp;
      }

      if( !( temp = rule.getDst().getAddress() ).isEmpty() )
      {
        command += " -d " + temp;
      }

      if( !( temp = rule.getProtocol() ).isEmpty() )
      {
        command += " -p " + temp;
      }

      if( rule.getDst().getPort() > 0 && rule.getDst().getPort() < 65535 )
      {
        command += " --dport " + rule.getDst().getPort();
      }

    }
    return command;
  }



  private String getRouteCommand( RouteInfo route )
  {
    String command = new String();
    String temp = new String();

    if( !( temp = route.getDestination().getAddress() ).isEmpty() )
    {
      if( route.getToHost() )
      {
        command += " -host " + temp;
      }
      else
      {
        command += " -net " + temp;
        if( !( temp = route.getMask() ).isEmpty() )
        {
          command += " netmask " + temp;
        }

        if( !( temp = route.getNextHop().getAddress() ).isEmpty() && !temp.equals( "0.0.0.0" ) )
        {
          command += " gw " + temp;
        }
      }

    }

    if( !( temp = route.getAdapterName() ).isEmpty() )
    {
      command += " dev " + temp;
    }
    return command;
  }



  private boolean existAdapter( String name )
  {
    for( Object tmp : this.adaptersInfo )
    {
      if( ( ( AdapterInfo ) tmp ).getName().equals( name ) )
      {
        return true;
      }
    }
    return false;
  }



  private int existAdapterState( String adapterName )
  {
    int i = 0;
    for( Object tmp : this.adaptersState )
    {
      i++;
      if( ( ( AdapterState ) tmp ).getName().equals( adapterName ) )
      {
        return i;
      }
    }
    return -1;
  }



  protected synchronized void addAdapaterState( AdapterState name )
  {
    int i = existAdapterState( name.getName() );
    if( i == -1 )
    {
      this.adaptersState.add( name );
    }
    else
    {
      this.adaptersState.remove( i - 1 );
      this.adaptersState.add( i - 1, name );
    }
    if( this.runningThread == 0 )
    {
      NetworkManagerLinux.loading = false;
    }
  }



  protected boolean isLoading()
  {
    if( this.runningThread == 0 )
    {
      return false;
    }
    else
    {
      return true;
    }
  }



  public boolean existFirewallRule( FirewallRule rule )
  {
    for( Object tmp : this.firewallRules )
    {
      if( ( ( FirewallRule ) tmp ).equals( rule ) )
      {
        return true;
      }
    }
    return false;
  }



  public boolean existBridge( BridgeInfo bridge )
  {
    for( Object tmp : this.bridges )
    {
      if( ( ( BridgeInfo ) tmp ).equals( bridge ) )
      {
        return true;
      }
    }
    return false;
  }



  public BridgeInfo getBridgeByName( String bridgeName )
  {

    for( Object tmp : this.bridges )
    {
      if( ( ( BridgeInfo ) tmp ).getName().equals( bridgeName ) )
      {
        return ( BridgeInfo ) tmp;
      }
    }
    return null;
  }



  public TunnelInfo getTunnelByName( String tunnelName )
  {
    for( Object tmp : this.tunnels )
    {
      if( ( ( TunnelInfo ) tmp ).getName().equals( tunnelName ) )
      {
        return ( ( TunnelInfo ) tmp );
      }
    }
    return null;
  }



  public boolean existRoute( RouteInfo route )
  {
    for( Object tmp : routeRules )
    {
      if( ( ( RouteInfo ) tmp ).equals( route ) )
      {
        return true;
      }
    }
    return false;
  }



  public List getRouteRules()
  {
    return this.routeRules;
  }



  protected String getethtoolPath()
  {
    return this.ethtoolPath;
  }



  private void setBridges()
  {
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      List response = new ArrayList();
      response = Cmd.executeCommand( this.bridgePath + " show" );

      String bridgeName = null;
      List bridgedAdapters = new ArrayList();
      boolean stp = false;
      for( Object line : response )
      {
        StringTokenizer tabTok = new StringTokenizer( ( ( String ) line ), "\t" );
        bridgedAdapters = new ArrayList();
        if( tabTok.countTokens() > 2 )
        {
          while( tabTok.hasMoreTokens() )
          {
            String temp = ( String ) tabTok.nextToken();
            if( this.existAdapter( temp ) )
            {
              bridgeName = temp;
              tabTok.nextToken();
              if( ( ( String ) tabTok.nextToken() ).equals( "no" ) )
              {
                stp = false;
              }
              else
              {
                stp = true;
              }
              if( tabTok.hasMoreTokens() )
              {
                bridgedAdapters.add( ( String ) tabTok.nextToken() );
              }
              this.bridges.add( new BridgeInfo( bridgeName, bridgedAdapters, stp ) );
            }
          }
        }
        else if( tabTok.countTokens() == 1 )
        {
          String temp;
          if( this.existAdapter( temp = ( String ) tabTok.nextToken() ) )
          {
            ( this.getBridgeByName( bridgeName ) ).addAdapter( temp );
          }
        }
      }

    }

  }



  private void setTunnels()
  {
    this.tunnels = new ArrayList();
    //this.tunnels = Collections.synchronizedList(this.tunnels);
    if( OSValidator.isUnix() && this.checkRoot() )
    {
      List response = new ArrayList();
      response = Cmd.executeCommand( this.ipPath + " tunnel show" );
      for( Object line : response )
      {
        try
        {
        StringTokenizer commandArgs = new StringTokenizer( ( ( String ) line ), " " );

        String mode = null;
        String remote = null;
        String local = null;
        String dev = null;
        String adapter = null;
        while( commandArgs.hasMoreTokens() )
        {
          String temp = commandArgs.nextToken();

          if( temp.contains( ":" ) && this.existAdapter( temp.substring( 0, temp.length() - 1 ) ) )
          {
            adapter = temp.substring( 0, temp.length() - 1 );
          }
          else if( temp.contains( "ipv6/ip" ) )
          {
            mode = "sit";
          }
          else if( temp.contains( "remot" ) )
          {
            remote = commandArgs.nextToken();
          }
          else if( temp.contains( "local" ) )
          {
            local = commandArgs.nextToken();
          }
          else if( temp.contains( "dev" ) )
          {
            dev = commandArgs.nextToken();
          }
        }

        if( dev != null )
        {
          this.tunnels.add( new TunnelInfo( adapter, new IPAddress( local ), new IPAddress( remote ), TunnelMode.valueOf( mode ), dev ) );
        }
        else
        {
          this.tunnels.add( new TunnelInfo( adapter, new IPAddress( local ), new IPAddress( remote ), TunnelMode.valueOf( mode ) ) );
        }
        }catch(NullPointerException ex)
        {
          logger.error(ex +" on "  + line);
        }
      }
    }

  }



  public List getBridgedAdapters()
  {
    List brAd = new ArrayList();
    for( Object tmp : this.bridges )
    {
      for( Object tmp2 : ( ( ( BridgeInfo ) tmp ).getAdapters() ) )
      {
        brAd.add( ( String ) tmp2 );
      }
    }
    return brAd;
  }



  public boolean hasBrctl()
  {
    return this.hasBrctl;
  }



  public boolean hasEthtool()
  {
    return this.hasEthtool;
  }



  private RouteInfo getRouteRule( RouteInfo route )
  {
    RouteInfo existingRoute = null;
    for( Object tmp : this.routeRules )
    {
      if( ( ( RouteInfo ) tmp ).equals( route ) )
      {
        existingRoute = ( RouteInfo ) tmp;
      }
    }
    return existingRoute;
  }



  private FirewallRule getFirewallRule( FirewallRule rule )
  {
    FirewallRule existingRule = null;
    for( Object tmp : this.firewallRules )
    {
      if( ( ( FirewallRule ) tmp ).equals( rule ) )
      {
        existingRule = ( FirewallRule ) tmp;
      }
    }
    return existingRule;
  }



  private BridgeInfo getBridgeInfo( BridgeInfo bridge )
  {
    BridgeInfo existingBridge = null;
    for( Object tmp : this.bridges )
    {
      if( ( ( BridgeInfo ) tmp ).equals( bridge ) )
      {
        existingBridge = ( BridgeInfo ) tmp;
      }
    }
    return existingBridge;
  }



  public static void setErrorStr( String errorStr )
  {
    NetworkManagerLinux.errorStr = errorStr;
  }



  public String getErrorStr()
  {
    return NetworkManagerLinux.errorStr;
  }



  public PluginDescription getPluginDescription()
  {
    return this.pluginDescription;
  }

    @Override
    /**
     * Allocate a resource port and return the number. Invoke a script bask that use sockets programming
     * @return Return the number of port allocated here. Return -1 value
     * if there is no port free
     */
    public Integer getPort(Integer inf_port, Integer sup_port, String ip){
        int port;
        
    	for(port=inf_port; port<=sup_port; port++){
            if(this.queryBash(ip,port).equals("free"))
                break;
    	}
        if(port==(sup_port+1))
            port=-1;
        
        return port;
    }
    
    @Override
    /**
     * List all busy ports. Invoke a script bask that use sockets programming
     * @return Return the numbers of busy ports
     */
    public List listBusyPorts(Integer inf_port, Integer sup_port, String ip){
        int port;
        ArrayList l = new ArrayList();
    	for(port=inf_port; port<=sup_port; port++){
            if(this.queryBash(ip,port).equals("busy"))
                l.add(Integer.toString(port));
    	}
        return l;
    }
    
    
    @Override
    /**
     * List all free ports. Invoke a script bask that use sockets programming
     * @return Return the numbers of free ports
     */
    public List listFreePorts(Integer inf_port, Integer sup_port, String ip){
        int port;
        ArrayList l = new ArrayList();
    	for(port=inf_port; port<=sup_port; port++){
            if(this.queryBash(ip,port).equals("free"))
                l.add(Integer.toString(port));
    	}
        return l;
    }
    
    @Override
    /**
     * Query to known if a port is busy or free. Invoke a script bask that use sockets programming
     * @param porta Number of port
     * @return True if busy, false is free
     */
    public Boolean isPortBusy(Integer port, String ip){
        boolean status = false;
        if(this.queryBash(ip,port).equals("busy"))
            status=true;
        else
            status=false;
        return status;
    }

    private String queryBash(String ip, int port){
        String s;
        String ret=null;
	try {
            String query=" ( ( echo >/dev/tcp/"+ip+"/"+port+" ) 2>/dev/null && echo \"busy\" || echo \"free\" ) > statusPort";
            String[] cmd = {"/bin/bash", "-c", query};
            Process proc = Runtime.getRuntime().exec(cmd);
            try {
                proc.waitFor();
            }
            catch (InterruptedException e) {
                this.logger.error("E' stata rilevata una InterruptedException: "+e.getMessage());
            }

            BufferedReader reader = new BufferedReader(new FileReader("statusPort"));
            while( (s = reader.readLine()) != null ){
                ret=s;
            }
            Runtime.getRuntime().exec("rm statusPort");
        }
        catch(IOException e){
            this.logger.error(e);
        }
        return ret;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOwner(Agent owner) {
        this.owner=owner;
    }
}
