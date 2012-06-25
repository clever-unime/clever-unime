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
package org.clever.ClusterManager.DispatcherPlugins.DispatcherClever;

import java.util.HashMap;
import org.apache.log4j.Logger;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.safehaus.uuid.UUIDGenerator;



public class RequestsManager
{

  private HashMap<Integer, Request> idNAT = null;
  private Logger logger = null;
  UUIDGenerator gen = null;



  public RequestsManager()
  {
    logger = Logger.getLogger( "ClusterCoordinator" );
    idNAT = new HashMap<Integer, Request>();
    gen = UUIDGenerator.getInstance();
  }

 //method with request timeout. TODO: modify the current invokations of this method
   public int addRequestPending( final CleverMessage msg, Request.Type type ,long timeout)
  {
    Integer tempId = Math.abs( gen.generateTimeBasedUUID().hashCode() );
    Request request = null;
    switch( type )
    {
      case EXTERNAL:
        request = new Request( msg.getId(), msg.getSrc() );
        break;
      case INTERNAL:
        request = new Request( msg.getId() ,timeout);
        break;
    }

    idNAT.put( tempId, request );
    logger.debug( "Request added from: " + msg.getSrc() + " id: " + msg.getId()
                  + " id toward HostCoordinator: " + tempId );
    return tempId;
  }

  public int addRequestPending( final CleverMessage msg, Request.Type type )
  {
    Integer tempId = Math.abs( gen.generateTimeBasedUUID().hashCode() );
    Request request = null;
    switch( type )
    {
      case EXTERNAL:
        request = new Request( msg.getId(), msg.getSrc() );
        break;
      case INTERNAL:
        request = new Request( msg.getId());
        break;
    }

    idNAT.put( tempId, request );
    logger.debug( "Request added from: " + msg.getSrc() + " id: " + msg.getId()
                  + " id toward HostCoordinator: " + tempId );
    return tempId;
  }



  public boolean deleteRequestPending( int id )
  {
    Integer newId = Integer.valueOf( id );
    return ( idNAT.remove( newId ) != null );
  }



  public Request getRequest( final int id )
  {
    return idNAT.get( Integer.valueOf( id ) );
  }



  public int getRequestPendingId( int id )
  {
    Integer newId = Integer.valueOf( id );
    Request request = idNAT.get( newId );
    return request.getId();
  }



  public String getRequestPendingSrc( int id )
  {
    Integer newId = Integer.valueOf( id );
    Request request = idNAT.get( newId );
    return request.getSrc();
  }
}
