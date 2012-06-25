/*
 *  The MIT License
 * 
 *  Copyright 2010 francesco.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.clever.Common.Shared;

import java.util.HashMap;
import org.apache.log4j.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.CleverMessage;



public class CheckAndFormat
{

  private HashMap<Integer, CleverMessage> idMessage;
  private Logger logger = Logger.getLogger( "CheckAndFormat" );
  //such map is used only for Starting/Destroying VEs
  private HashMap<Integer, Integer> idUuid;



  public CheckAndFormat()
  {
    this.idMessage = new HashMap<Integer, CleverMessage>();
    this.idUuid = new HashMap<Integer, Integer>();
  }



  public void insertPendingOperation( CleverMessage msg )
  {
    logger.debug( "Inserting Message" );
    idMessage.put( msg.getId(), msg );
  }



  public void insertPendingOperation( CleverMessage msg, int veUuid )
  {
    logger.debug( "Inserting Message" );
    idMessage.put( msg.getId(), msg );
    idUuid.put( msg.getId(), veUuid );
  }



  public void verifyPendingOperation( CleverMessage msg ) throws CleverException
  {
    CleverMessage request = idMessage.get( msg.getId() );


    if ( ( Boolean ) request.getObjectFromMessage() )
    {
      if ( request.getBodyOperation().equals( "createAndStart" ) ) //an XML document should be created for recording information about VE
      ;
      else if ( request.getBodyOperation().equals( "destroyVE" ) )
      {
        idMessage.remove( msg.getId() );
      }
    }
  }
}
