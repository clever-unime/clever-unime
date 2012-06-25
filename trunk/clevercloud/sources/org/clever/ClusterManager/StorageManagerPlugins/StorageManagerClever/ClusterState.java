/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
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
package org.clever.ClusterManager.StorageManagerPlugins.StorageManagerClever;

import org.clever.ClusterManager.StorageManagerPlugins.StorageManagerClever.HostLoad;
import org.clever.ClusterManager.StorageManagerPlugins.StorageManagerClever.HostInfo;
import org.clever.Common.VEInfo.VEInfo;
import java.util.ArrayList;
import java.util.List;



public class ClusterState
{

  private int totalHosts;
  private int clusterId;
  private ArrayList<HostInfo> hostInfo;



  public ClusterState( ArrayList<HostInfo> hostInfo )
  {

    this.hostInfo = hostInfo;
  }



  public int getTotalHosts()
  {
    return ( totalHosts );
  }



  protected void setTotalHosts( int totalHosts )
  {
    this.totalHosts = totalHosts;
  }



  public int getClusterId()
  {
    return ( clusterId );
  }



  protected void setClusterId( int clusterId )
  {
    this.clusterId = clusterId;
  }



  public String getMinLoadedHostID()
  {
    return ( "Not implemented yet" );
  }



  public String getMaxLoadedHostID()
  {
    return ( "Not implemented yet" );
  }



  public HostLoad getHostLoad( int hostId )
  {
    HostInfo hI = ( HostInfo ) hostInfo.get( hostId );
    HostLoad hl = hI.getCurrentLoad();
    return ( hl );
  }



  public String getHostToExec( VEInfo ve )
  {
    return ( "Not implemented yet" );
  }



  private List calculateLoads()
  {
    return ( hostInfo );
  }



  public ArrayList<HostInfo> getHostInfo()
  {
    return ( hostInfo );
  }
}
