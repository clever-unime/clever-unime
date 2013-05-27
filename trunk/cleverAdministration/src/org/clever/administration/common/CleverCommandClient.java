/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.common;

import java.util.*;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.DispatcherPlugins.DispatcherClever.Request;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.CleverMessage.MessageType;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.CleverMessageHandler;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ExecOperation;
import org.clever.administration.commands.CleverCommand;
import org.clever.administration.commands.CommandCallback;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;


/**
 * Usata per mandare comandi e smistare le risposte ai vari client
 * Normalmente viene instanziato e gestito da CleverCommandClientProvider
 * @author maurizio
 */
public class CleverCommandClient implements CleverMessageHandler
{

   private static final Logger log = Logger.getLogger(CleverCommandClient.class);
    
    
  private String adminHostName;
  private ConnectionXMPP connectionXMPP;
  private Request request;
  private HashMap<Integer, CommandCallback> commandsSent = null;
  



  public CleverCommandClient()
  {
    commandsSent = new HashMap<Integer, CommandCallback>();
    connectionXMPP = new ConnectionXMPP();
    
  }

  
  
 /**
  * Il client e' attivo (connessone XMPP e autenticato)
  * @return 
  */
 public boolean isActive()
 {
     XMPPConnection c = connectionXMPP.getXMPP();
     return (c==null ?  false : c.isAuthenticated());
 }

    public ConnectionXMPP getConnectionXMPP() {
        return connectionXMPP;
    }

    public void setConnectionXMPP(ConnectionXMPP connectionXMPP) {
        this.connectionXMPP = connectionXMPP;
    }
  

  public boolean connect( String XMPPServer,
                          String username,
                          String passwd,
                          int port,
                          String room,
                          String nickname )
  {
    try
    {

      adminHostName = username;

      
      connectionXMPP.connect(XMPPServer, port);
      connectionXMPP.authenticate(username, passwd);

      connectionXMPP.joinInRoom( room, ConnectionXMPP.ROOM.SHELL, nickname );
      connectionXMPP.addChatManagerListener( this );
      return true;
    }
    catch( CleverException e )
    {
      log.error("Error on connect : " + e); 
      return false;

    }

  }



  private void sendRequest( final CleverMessage msg )
  {
    try
    {
      connectionXMPP.getMultiUserChat( ConnectionXMPP.ROOM.SHELL ).sendMessage( msg.toXML() );
    }
    catch( XMPPException ex )
    {
      log.error("Error in sending Clever Message. " + ex );
    }
  }

  /**
   *
   * @param agent: the entity which executes the command (e.g. ClusterManager)
   * @param command:  the target command
   * @param params:the params of the command
   * @param showXML:It sets if show the XML request/response messages.
   * @throws CleverException
   */
  public void execAdminCommand( final CommandCallback cleverCommand,
                                final String target,
                                final String agent,
                                final String command,
                                final ArrayList params,
                                final boolean showXML ) throws CleverException
  {

    request =null; //this set the command as async command (see handleCleverMessage)
    CleverMessage requestMsg = new CleverMessage();

    requestMsg.fillMessageFields( MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation( command, params, agent ), 0 );


    commandsSent.put( Integer.valueOf( requestMsg.getId() ), cleverCommand );
    sendRequest( requestMsg );
    
    log.debug( "Clever Request Message: \n" + requestMsg.toXML() );
    
  }


  /**
   *
   * @param agent: the entity which executes the command (e.g. ClusterManager)
   * @param command:  the target command
   * @param params:the params of the command
   * @param showXML:It sets if show the XML request/response messages.
   * @throws CleverException
   */
  public Object execSyncAdminCommand(
                                final String target,
                                final String agent,
                                final String command,
                                final ArrayList params,
                                final boolean showXML ) throws CleverException
  {
    CleverMessage requestMsg = new CleverMessage();

    requestMsg.fillMessageFields( MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation( command, params, agent ), 0 );

    request = new Request(requestMsg.getId(),0); //this set the command as sync command (see handleCleverMessage)
    
    sendRequest( requestMsg );
    log.debug( "Clever Request Message: \n" + requestMsg.toXML() );
    
    return request.getReturnValue();
    
  }

  @Override
  public void handleCleverMessage( final CleverMessage cleverMessage )
  {
    log.debug( "Received:\n" + cleverMessage.toXML() );
    CommandCallback cleverCommand = null;
    try
    {
      if(request!=null) //this is a reply to a sync command
      {
         
          request.setReturnValue(cleverMessage.getObjectFromMessage()); //TODO : do an overloaded method setReturnValue(CleverMessage c)
         
      }
      else //this is a reply to an async command
      {
        cleverCommand = commandsSent.get( Integer.valueOf( cleverMessage.getReplyToMsg() ) );
        if(cleverMessage.getType()==MessageType.REPLY || cleverMessage.getType()==MessageType.ERROR)
            cleverCommand.handleMessage( cleverMessage.getObjectFromMessage() );
      
      }
    }
    
    catch( CleverException ex )
    {
      
      if(ex.getInternalException()!=null)
        log.error( "from ClusterManagerAdministrationTools: "+ex );
      else
          log.error( "from ClusterManagerAdministrationTools: "+ex.getInternalException() );
      if(request!=null)
          request.setReturnValue(ex); //the ex CleverException will be raised by getReturnValue method
      else
        cleverCommand.handleMessageError(ex);  
    }
  }
}
