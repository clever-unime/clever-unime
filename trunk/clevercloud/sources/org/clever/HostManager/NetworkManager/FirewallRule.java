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

import org.clever.HostManager.NetworkManager.Policy;



public class FirewallRule
{

  private Endpoint src;
  private Endpoint dst;
  private String protocol;
  private String adapter;
  private FlowType chain;
  private Policy policy;



  public FirewallRule( Endpoint src, Endpoint dst, String protocol, String adapter, FlowType chain, Policy policy )
  {
    this.setSrc( src );
    this.setDst( dst );
    this.setProtocol( protocol );
    this.setAdapter( adapter );
    this.setChain( chain );
    this.setPolicy( policy );
  }



  public FirewallRule()
  {
  }



  public Endpoint getSrc()
  {
    return this.src;
  }



  public Endpoint getDst()
  {
    return this.dst;
  }



  public String getProtocol()
  {
    return this.protocol;
  }



  public String getAdapter()
  {
    return this.adapter;
  }



  public FlowType getChain()
  {
    return this.chain;
  }



  public Policy getPolicy()
  {
    return this.policy;
  }



  public void setSrc( Endpoint src )
  {
    this.src = src;
  }



  public void setDst( Endpoint dst )
  {
    this.dst = dst;
  }



  public void setProtocol( String protocol )
  {
    this.protocol = protocol;
  }



  public void setAdapter( String adapter )
  {
    this.adapter = adapter;
  }



  public void setChain( FlowType chain )
  {
    this.chain = chain;
  }



  public void setPolicy( Policy policy )
  {
    this.policy = policy;
  }



  public boolean equals( FirewallRule rule )
  {
    if( this.src.getAddress().equals( rule.getSrc().getAddress() )
        && this.dst.getAddress().equals( rule.getDst().getAddress() )
        && this.dst.getPort() == rule.getDst().getPort()
        && this.protocol.equals( rule.getProtocol() )
        && this.adapter.equals( rule.getAdapter() )
        && this.chain == rule.getChain()
        && this.policy == rule.getPolicy() )
    {
      return true;
    }
    else
    {
      return false;
    }
  }



  public String toString()
  {
    String str;
    str = "Interfaccia :			" + this.getAdapter() + "\n";
    str += "Catena :				 " + this.getChain() + "\n";
    str += "Indirizzo Sorgente :	 " + this.getSrc().getAddress() + "\n";
    str += "Indirizzo Destinazione : " + this.getDst().getAddress() + "\n";
    str += "PortaDestinazione :	  " + this.getDst().getPort() + "\n";
    str += "Protocollo :			 " + this.getProtocol() + "\n";
    str += "Policy :				 " + this.getPolicy() + "\n" + "\n";
    return str;
  }
}
