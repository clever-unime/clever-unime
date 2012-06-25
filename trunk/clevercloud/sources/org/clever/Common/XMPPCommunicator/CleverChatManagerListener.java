/*
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

package org.clever.Common.XMPPCommunicator;

import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;


public class CleverChatManagerListener implements ChatManagerListener
{

  private Logger logger = null;
  private CleverMessageHandler msgHandler = null;
  private HashMap<String, Chat> chats = new HashMap<String, Chat>();



  CleverChatManagerListener( CleverMessageHandler msgHandler )
  {
    logger = Logger.getLogger( "XMPPCommunicator" );
    this.msgHandler = msgHandler;
  }



  /**
   * Get Chat Object with an user
   * @param jid
   * @return
   */
  public Chat getChat( final String jid )
  {
    return chats.get( jid );
  }



  /**
   * Close one chat
   * @param jid
   */
  public void closeChat( final String jid )
  {
    // TODO investigate if the memory is released
    Chat chat = chats.get( jid );
    if ( chat != null )
    {
      Iterator it = chat.getListeners().iterator();
      while ( it.hasNext() )
      {
        CleverChatListener listener = ( CleverChatListener ) it.next();
        chat.removeMessageListener( listener );
      }
    }
    chats.remove( jid );
  }



  @Override
  public void chatCreated( Chat chat, boolean createdLocally )
  {
      String p = chat.getParticipant().replace("/Smack", "");
    logger.info( "Chat created with: " + p + " createdLocally: " + createdLocally );
    chats.put( p, chat );


    // Check if the chat was created locally or not
    // If was created locally, the listener was already created by ConnectionXMPP.sendMessage()
    // Otherwhise one CleverChatListener is associated to received chat
    if ( ! createdLocally )
    {
      chat.addMessageListener( new CleverChatListener( msgHandler ) );
    }
  }
}
