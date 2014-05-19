/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api;

import java.util.*;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Request;
import org.clever.Common.Communicator.CleverMessagesDispatcher;
import org.clever.Common.Communicator.InvocationCallback;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Communicator.RequestsManager;
import org.clever.Common.Communicator.ThreadMessageDispatcher;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.CleverMessage.MessageType;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.CleverMessageHandler;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ExecOperation;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;


/**
 * Usata per mandare comandi e smistare le risposte ai vari client
 * Normalmente viene instanziato e gestito da CleverCommandClientProvider
 * @author maurizio
 */
public class CleverCommandClient implements CleverMessageHandler, CleverMessagesDispatcher
{

  private static final Logger log = Logger.getLogger(CleverCommandClient.class);
    
    
  private String adminHostName;
  private ConnectionXMPP connectionXMPP;
 
  
  private HashMap<Integer, InvocationCallback> commandsSent = null;
  
  private RequestsManager requestManager;
  
  
  private ThreadMessageDispatcher dispatcher;


  public CleverCommandClient(Integer maxMessages, Integer maxThreadHandlers)
  {
    
    commandsSent = new HashMap<Integer, InvocationCallback>();
    requestManager = new RequestsManager();
    connectionXMPP = new ConnectionXMPP();
    dispatcher = new ThreadMessageDispatcher(this, maxMessages, maxThreadHandlers);
    dispatcher.start();
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
  

  public synchronized boolean connect( String XMPPServer,
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


/**
 * Send messag to MUC . Destination active ClusterManager
 * @param msg 
 */

  private synchronized void sendRequest( final CleverMessage msg ) throws CleverException
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
  public  void execAdminCommand( final InvocationCallback cleverCommand,
                                final String target,
                                final String agent,
                                final String command,
                                final ArrayList params,
                                final boolean showXML ) throws CleverException
  {

    CleverMessage requestMsg = new CleverMessage();

    requestMsg.fillMessageFields( MessageType.REQUEST, adminHostName, target, true, params, new ExecOperation( command, params, agent ), 0 );

    int id = requestManager.addRequestPending(requestMsg, Request.Type.INTERNAL);
    Request request = requestManager.getRequest(id);
    //request.setAsync(true);
    request.setCallback(cleverCommand); //store invoker to async reply
    requestMsg.setId(id);
    sendRequest( requestMsg );
    log.debug( "Clever Request Message (aSync): \n" + requestMsg.toXML() );
    
    
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

    int id = requestManager.addSyncRequestPending(requestMsg, Request.Type.INTERNAL, 1800000); //TODO: retrieve from configuration
    Request request = requestManager.getRequest(id);
    
    
    requestMsg.setId(id);
    sendRequest( requestMsg );
    log.debug( "Clever Request Message: \n" + requestMsg.toXML() );
    //wait for response (sync invocation)
    return request.getReturnValue();
    
  }

  /**
   * Close client command and release all resources
   */
  public void close()
  {
      //TODO: call ClientCommandProvider to choose the correct action
      this.connectionXMPP.closeConnection();
      this.dispatcher.close();
      
  }
  
  
  //invocato direttamente dalla coda XMPP
  
  @Override
  public synchronized void handleCleverMessage( final CleverMessage cleverMessage )
  {
      try {
          log.debug( "Received:\n" + cleverMessage.toXML() );
      } catch (CleverException ex) {
          java.util.logging.Logger.getLogger(CleverCommandClient.class.getName()).log(Level.SEVERE, null, ex);
      }
    dispatcher.pushMessage(cleverMessage); //insert in message queue
    
  }

  
  
   //invocati dai thread gestori dei messaggi
  
    @Override
    public void handleNotification(Notification notification) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleMessage(CleverMessage message) {
        Request request = requestManager.getRequest(message.getReplyToMsg()); // retrieve request using replytomsg  field
        if(request == null)
        {
            //no request for this reply or error
            log.error("No request for message : " + message.getReplyToMsg());
            return;
        }
        try {
                if (request.isAsync())
                {
                        log.debug("Async request serving ... " );
                        request.getCallback().handleMessage(message.getObjectFromMessage()); //TODO: handle error message
                }
                else
                {
                        //sync invocation
                        //TODO : do an overloaded method setReturnValue(CleverMessage c)
                        log.debug("Sync request serving ... " );
                        request.setReturnValue(message.getObjectFromMessage()); 

                }
        } catch (CleverException ex) {
                 if(ex.getInternalException()!=null)
                    log.error( "from CleverCommandClient: "+ex );
                  else
                      log.error( "from CleverCommandClient: "+ex.getInternalException() );
                  if(!request.isAsync())
                      request.setReturnValue(ex); //the ex CleverException will be raised by getReturnValue method
                  else
                    request.getCallback().handleMessageError(ex);  
        }
    }

    @Override
    public void dispatch(CleverMessage message) {
        log.warn("Dispatch not implemented : received an unexpected REQUEST message ?");
    }
  
    @Override
    public Object dispatchToIntern(MethodInvoker mi) throws CleverException{
        throw new CleverException("Unsupported Operation");
    }
  
    @Override
    public void handleMeasure(final CleverMessage message){
        
    }
  
  
}
