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
package org.clever.Common.Communicator;

import com.fasterxml.uuid.Generators;

import java.security.SecureRandom;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.UUIDProvider.UUIDProvider;




public class RequestsManager
{

  private HashMap<Integer, Request> idNAT = null;
  private Logger logger = null;
  UUIDProvider gen = null;



  public RequestsManager()
  {
    logger = Logger.getLogger( RequestsManager.class );
    idNAT = new HashMap<Integer, Request>();
    SecureRandom r = new SecureRandom();
    
    
  }

   /**
    * Add a new Sync request invocation
    * @param msg
    * @param type
    * @param timeout
    * @return request id
    */ 
   public int addSyncRequestPending( final CleverMessage msg, Request.Type type ,long timeout)
  {
    Integer tempId = UUIDProvider.getPositiveInteger();
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

    request.setAsync(false);
    
    synchronized(this)
    {
        idNAT.put( tempId, request );
    }
    logger.debug( "Sync Request added from: " + msg.getSrc() + " id: " + msg.getId()
                  + " new id (differ for EXTERNAL): " + tempId );
    return tempId;
  }

   
  /**
    * Add a new aSync request invocation
    * @param msg
    * @param type
    * @param timeout
    * @return request id
    */ 
  public int addRequestPending( final CleverMessage msg, Request.Type type )
  {
    Integer tempId = Math.abs( UUIDProvider.getPositiveInteger() );
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
    request.setAsync(true);
     synchronized(this)
    {
        idNAT.put( tempId, request );
    }
    
    logger.debug( "aSync Request added from: " + msg.getSrc() + " id: " + msg.getId()
                  + " id toward HostCoordinator: " + tempId );
    return tempId;
  }



  public synchronized boolean deleteRequestPending( int id )
  {
    Integer newId = Integer.valueOf( id );
    return ( idNAT.remove( newId ) != null );
  }



  public synchronized Request getRequest( final int id )
  {
    return idNAT.get( Integer.valueOf( id ) );
  }



  public synchronized int getRequestPendingId( int id )
  {
    Integer newId = Integer.valueOf( id );
    Request request = idNAT.get( newId );
    return request.getId();
  }



  public synchronized String getRequestPendingSrc( int id )
  {
    Integer newId = Integer.valueOf( id );
    Request request = idNAT.get( newId );
    return request.getSrc();
  }
}
