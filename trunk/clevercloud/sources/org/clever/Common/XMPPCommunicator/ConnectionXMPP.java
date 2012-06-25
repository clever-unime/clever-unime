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
package org.clever.Common.XMPPCommunicator;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.apache.log4j.*;
import org.clever.Common.Exceptions.CleverException;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.Occupant;

class myTransferListener implements FileTransferListener{
    private Logger logger;
    private String path;
    
    public myTransferListener(Logger l, String p){
        
        logger = l;
        logger.debug("Copying the logger");
        
        path = p;
        logger.debug("Copying the path " + path);
    }
        
    @Override
    public void fileTransferRequest(FileTransferRequest request) {
                // Check to see if the request should be accepted
                IncomingFileTransfer transfer = request.accept();
                try {
                    logger.debug( "------Receiving file in " + path + "------");
                    transfer.recieveFile(new File(path));
                    logger.debug( "------File received------");
                } catch (XMPPException ex) {
                    logger.error( "------Error while receiving file.------");
                    logger.error(ex.toString());
                }
    }
}


public class ConnectionXMPP implements javax.security.auth.callback.CallbackHandler
{

  @Override
  public void handle( Callback[] callbacks ) throws IOException, UnsupportedCallbackException
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }
  public enum ROOM
  {
    CLEVER_MAIN,
    SHELL
  };

  private XMPPConnection connection = null;
  private String servername = "";
  private String username = "";
  private String password = "";
  private HashMap<ROOM, MultiUserChat> mucs = new HashMap<ROOM, MultiUserChat>( 0 );
  private Integer port;
  private Logger logger;
  private CleverChatManagerListener cleverChatManagerListener = null;
  private CleverMessageHandler msgHandler = null;

  private boolean isTLS = false;


  public ConnectionXMPP() throws CleverException
  {
    logger = Logger.getLogger( "XMPPCommunicator" );
  }



  public void connect( final String servername, final Integer port )
  {
    this.servername = servername;
    this.port = port;
    
    try
    {
      ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration( servername, port );
      connection = new XMPPConnection( connectionConfiguration );
      //connection.DEBUG_ENABLED=true;
      connection.connect();
      logger.info( "XMPP connected plain mode" );
    }
    catch( XMPPException ex )
    {
      logger.error( "Error during the XMPP connection: " + ex.toString() );
      System.exit( 1 );
    }
  }

  public void connectTLS( final String servername, final Integer port,
                          final String keystorePath, final String keystorePassword,
                          final String truststorePath, final String truststorePassword )
  {
    this.servername = servername;
    this.port = port;
    this.isTLS = true;
    
    try
    {
      ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration( servername, port );
      connectionConfiguration.setSecurityMode( SecurityMode.required );
      connectionConfiguration.setVerifyRootCAEnabled( true );
      connectionConfiguration.setSASLAuthenticationEnabled( true );
      connectionConfiguration.setKeystoreType( "pkcs12" );
      connectionConfiguration.setKeystorePath( keystorePath );
      connectionConfiguration.setTruststoreType( "jks" );
      connectionConfiguration.setTruststorePath( truststorePath );
      connectionConfiguration.setTruststorePassword( truststorePassword );
      connection = new XMPPConnection( connectionConfiguration, ( CallbackHandler ) this );
      SASLAuthentication.supportSASLMechanism( "PLAIN", 0 );
      connection.connect();
      logger.info( "XMPP connected in TLS mode" );
    }
    catch( XMPPException ex )
    {
      logger.error( "Error during the XMPP connection: " + ex.toString() );
      System.exit( 1 );
    }
  }

/**
   * Join in a specific room
   * @param room
   * @param nickName
   */
/*
  public void joinInRoom( final String roomName, final ROOM roomType, final String nickName , Boolean createIt)
  {
    if(createIt)
    {
        this.joinInRoom( roomName, roomType, nickName);
    }
    else
    {


                DiscussionHistory history = new DiscussionHistory();
                history.setMaxStanzas( 0 );

                MultiUserChat mucTemp ;
                
                if(mucTemp = MultiUserChat.getRoomInfo(connection, roomName))
                {

                }
                 
                logger.info( "Creating room: " + roomName + " with nickname: " + nickName );
                try
                {
                  mucTemp.join( nickName, "", history, 5000 );
                  logger.info( "Created room: " + roomName + " with nickname: " + nickName );
                  mucs.put( roomType, mucTemp );
                }
                catch( XMPPException ex )
                {
                  logger.error( "Error while joing room: " + roomName + " " + ex );
                }
      }
  }
*/


  /**
   * Join in a specific room with a empty status creating it
   * @param room
   * @param nickName
   */
  public void joinInRoom( final String roomName, final ROOM roomType, final String nickName )
  {
     this.joinInRoom(roomName, roomType, nickName,  "");
  }
 
  /**
   * Join in a specific room with a specific status creating it
   * @param room
   * @param nickName
   */
  public void joinInRoom( final String roomName, final ROOM roomType, final String nickName , final String status) //provo a fare sta funzione come sincronizzata!
  {
    DiscussionHistory history = new DiscussionHistory();
    history.setMaxStanzas( 0 );
    MultiUserChat mucTemp = new MultiUserChat( connection, roomName );
    

    logger.info( "Creating room: " + roomName + " with nickname: " + nickName );
    
    try
    {
      mucTemp.join( nickName, "", history, 5000 );
      mucTemp.changeAvailabilityStatus(status, Presence.Mode.chat); 
      logger.info( "Created room: " + roomName + " with nickname: " + nickName );
      mucs.put( roomType, mucTemp );
    }
    catch( XMPPException ex )
    {
      logger.error( "Error while joing room: " + roomName + " " + ex );
    }
  }
  
  /**
   * Add Presence listener to connection. Method for testing and debugging purposes
   */
   public void addPresenceListener(ROOM roomType , PacketListener listener)
   {
       getMultiUserChat( roomType ).addParticipantListener(listener) ;
   }

   /**
   * Delete Presence listener to connection. Method for testing and debugging purposes
   */
   public void removePresenceListener(ROOM roomType , PacketListener listener)
   {
       getMultiUserChat( roomType ).removeParticipantListener(listener) ;
   }
   

  /**
   * Add Chat Manager Listener to connection
   * @param msgHandler
   */
  public void addChatManagerListener( final CleverMessageHandler msgHandler )
  {
    this.msgHandler = msgHandler;
    cleverChatManagerListener = new CleverChatManagerListener( msgHandler );
    connection.getChatManager().addChatListener( cleverChatManagerListener );
  }



  public void authenticate( final String username, final String password )
  {
    this.username = username;
    this.password = password;
    for( int i = 0; i < 4; i++ )
    {
      try
      {

        if( isTLS )
        {
          logger.debug("trying TLS authentication");
          connection.getSASLAuthentication().authenticate( this.username, this.servername,
                                                           new SecurityCallback( this.username, this.password ) );

        }
        else
        {
          connection.login( username, password );
        }

        logger.debug( "XMPP connection established with username: " + this.username + " password: " + this.password + " server: "
                      + this.servername + " port: " + this.port );

      }
      catch( XMPPException e )
      {
        logger.error( "XMPP login failed with username (try # " + i + ": " + this.username + " password: " + this.password + " server: "
                      + this.servername + " port: " + this.port + "Exception:" + e );
      }

      if( connection.isAuthenticated() )
      {
        return;
      }
      else
      {
        try
        {
          Thread.sleep( 2000 );
        }
        catch( InterruptedException ex )
        {
          logger.error( "Interrupted Thread error: " + ex.toString() );
          System.exit( 1 );
        }
      }
    }
    System.exit( 1 );
  }



  public String getServer()
  {
    return servername;
  }



  public String getUsername()
  {
    return ( username );
  }



  public Iterator getMembers( ROOM roomType )
  {
    return getMultiUserChat( roomType ).getOccupants(); //restituisce tutti gli occupanti della stanza specificata
  }



  public MultiUserChat getMultiUserChat( ROOM roomType )
  {
    return mucs.get( roomType );
  }



  public MultiUserChat getMultiUserChat()
  {
    return getMultiUserChat( ROOM.CLEVER_MAIN );
  }



  public void closeConnection()
  {
    Iterator it = mucs.values().iterator();
    MultiUserChat mucTemp = null;
    while( it.hasNext() )
    {
      mucTemp = ( MultiUserChat ) it.next();
      mucTemp.leave();
    }

    connection.disconnect();
  }



  /**
   * Send private message to an user
   * @param jid
   * @param message
   */
  public void sendMessage( String jid, final CleverMessage message )
  {
    logger.debug( "Sending message: " + message.toXML() );
    jid += "@" + this.getServer();
    // See if there is already a chat open
    Chat chat = cleverChatManagerListener.getChat( jid.toLowerCase() );
    if( chat == null )
    {
        logger.debug("Chat toward "+jid +" not found");
      chat = connection.getChatManager().createChat( jid, new CleverChatListener( msgHandler ) );
    }

    // Send a message
    try
    {
      logger.debug("sending message");
      chat.sendMessage( message.toXML() );
      logger.debug("message sent");
    }
    catch( XMPPException ex )
    {
      logger.error( "Error while sending message: " + message.toXML() + " " + ex.getMessage() );
    }
  }



  public void addListener( ROOM roomType, ParticipantStatusListener list )
  {
    getMultiUserChat( roomType ).addParticipantStatusListener( list );
  }



  public String getHostName()
  {
    String hostname = "";

    try
    {
      InetAddress localmachine = InetAddress.getLocalHost();
      hostname = localmachine.getHostName();
      logger.debug( "Host name: " + hostname );
    }
    catch( Exception ex )
    {
      logger.error( "Error while getting the hostname: " + ex.getMessage() );
    }
    finally
    {
      return hostname;
    }
  }



  /**
   * Procedure for In-Band Registration ( XEP - 0077 )
   * @param server server to connect for registration
   * @param port numeric value for port where is binded the server
   * @param username the name used for the client
   * @param password the password used for the client
   */
  public void inBandRegistration( final String username, final String password )
  {
    if( ( connection == null || !connection.isConnected() )  )
    {
      logger.debug( "Not connected" );
      System.exit( 1 );
    }
    if( username.isEmpty() || password.isEmpty() )
    {
      logger.debug( "Invalid username or password" );
      System.exit( 1 );

    }
    try
    {
      // Connect and try to register the new account
      logger.debug( "Trying in-band registration with username: " + username + " and password: " + password );
      AccountManager accountManager = new AccountManager( connection );
      accountManager.createAccount( username, password );
    }
    catch( XMPPException ex )
    {
      logger.error( "Error while using In-Band Registration: " + ex );
      System.exit( 1 );
    }
  }



  /**
   * Procedure for retrieving the number of CMs in a specific room
   * @param room
   * @return
   */
  
  public Collection<Occupant> getCCsInRoom( final ROOM roomType ) 
  {
    MultiUserChat mucTemp = getMultiUserChat( roomType );

    Collection<Occupant> collection = new LinkedList<Occupant>(); //collezione di uscita!
    Iterator<String> it = mucTemp.getOccupants(); 
    Occupant occupant = null;
    Presence presence = null; 
    String occupantJid = ""; 
    String tmp = "";

    /*devo effettuare ora una ricerca x status*/
    while( it.hasNext() )
    {
      occupantJid = it.next();
      presence = mucTemp.getOccupantPresence(occupantJid);
      occupant = mucTemp.getOccupant(occupantJid);
      tmp = presence.getStatus();
      
      if((tmp!=null && (tmp.equals("CM_MONITOR") || (tmp.equals("CM_ACTIVE"))))) //controlla sempre tmp a null! che è importante
          collection.add(occupant);
    }
    return collection;
  }
  
  /*autore: Marco Carbone*/
  /* QUESTA FUNZIONE CERCA UN PARTICOLARE CM (per nickname) DENTRO LA STANZA specificata da roomtype E TORNA TRUE SE LO TROVA!*/
  public boolean SearchCM_InRoom(String nickname, ROOM roomtype)
  {//return true if exist
      Collection<Occupant> cm_inRoom = getCCsInRoom(roomtype); //prendo tutta la lista dei cluster manager presenti nella stanza roomtype
      Iterator it = cm_inRoom.iterator();
      Occupant cm = null;
      String nick = "";
      
      while(it.hasNext())
      {
          cm = (Occupant) it.next(); //cerco quello che ha come nick quello desiderato!
          nick = cm.getNick();
          
          if(nick.equals(nickname))
              return true;           
      }
      return false;
  }
  
  /*autore: Marco Carbone*/
  /*Questa funzione effettua una ricerca per status, fra tutti gli occupanti della stanza
   * specificata (roomType), restituendo il numero di ClusterManager presenti (sia nello stato
   * active che monitor)
   */
  public int getNum_CCsInRoom( final ROOM roomType)
  {
      
    MultiUserChat mucTemp = getMultiUserChat( roomType );

    Collection<Occupant> collection = new LinkedList<Occupant>(); //creo una lista concatenata di occupant
    Iterator<String> it = mucTemp.getOccupants(); //prendo tutti gli occupanti della room, devo invece selezionare solo quelli che osno moderatori della room!
    Occupant occupant = null;
    Presence presence = null; //mercoledì 16 Novembre 2011
    String occupantJid = ""; //qui memorizzo ad ogni iterazione un valore della collection Occupants
    
    while( it.hasNext() )
    {
        occupantJid = it.next(); 
        presence = mucTemp.getOccupantPresence(occupantJid);
        occupant = mucTemp.getOccupant(occupantJid);
        
        if(presence.getStatus() != null)
        {
            if(presence.getStatus().equals("CM_MONITOR")) 
                collection.add( occupant );
            if(presence.getStatus().equals("CM_ACTIVE"))
                collection.add(occupant);
        }
    }
    
    return collection.size();
  }
  
  /**
   * Procedure for retrieving the name of CM ACTIVE in a specific room
   * @param room
   * @return
   */
  public String getActiveCC(final ROOM roomType) //questa funzione da problemi nel restituire il CC attivo cercandolo dentro la room SHELL!!!
  {
      /*Ho modificato quesat funzione di ricerca del CC attivo perché adesso la 
       * modalità di ricerca si deve basare sullo status, lo status ci viene dato dalla funzione getOccupants di MultiUserChat
       * la vecchia funzione getActiveCC, invece ricercava il CC attivo o tra la lista dei
       * CC di CleverMain (che restituisce una lista di Occupant) o tra tutti gli utenti di
       * SHELL, che rioestituisce cmq una lista di Occupant e da Occupant non vediamo lo status!
       * 
       */
      
      MultiUserChat mucTemp = getMultiUserChat(roomType);
      Iterator<String> it = mucTemp.getOccupants(); //ho tutti i Jid degli utenti di roomType
      
      Occupant occupant = null;
      Presence presence = null;
      String occupantJid = "";
      String tmp = "";
      String nick = "";
      
      
      while(it.hasNext())
      {
          occupantJid = it.next();
          presence = mucTemp.getOccupantPresence(occupantJid);
          occupant = mucTemp.getOccupant(occupantJid);
          tmp = presence.getStatus();
          
          if(tmp == null)
              continue;
          if((!tmp.isEmpty()) && (tmp.equals("CM_ACTIVE"))) //in qualsiasi stanza il Cm ha come status CM_ACTIVE!
              nick = occupant.getNick();         
      }
      return nick;      
  }

  /*questa funzione restituisce la lista degli HM presenti sulla stanza
   * specificata da roomType, attualmente questi HostCoordinator sono dei
   * moderatori per quella stanza (credo SHELL), forse questo controllo va
   * modificato e fatto sulla presence.MODe!
   */
  
  /*public Collection<Occupant> getHCsInRoom( final ROOM roomType )
  {
    MultiUserChat mucTemp = getMultiUserChat( roomType );

    Collection<Occupant> collection = new LinkedList<Occupant>();
    Iterator<String> it = mucTemp.getOccupants();
    Occupant occupant = null;
    Presence presence = null;

    while( it.hasNext() )
    {
      String occupantJid = it.next();
      presence = mucTemp.getOccupantPresence(occupantJid);
      occupant = mucTemp.getOccupant(occupantJid);

      String tmp = presence.getStatus();
      
      if(tmp == null)
          continue;
      
      if( (!tmp.isEmpty()) && (tmp.equals("HM")) )
      {
        collection.add(occupant);
      }
    }
    return collection;
  }*/
  
  public Collection<Occupant> getHCsInRoom( final ROOM roomType ) 
  {
    MultiUserChat mucTemp = getMultiUserChat( roomType );

    Collection<Occupant> collection = new LinkedList<Occupant>(); //collezione di uscita!
    Iterator<String> it = mucTemp.getOccupants(); 
    Occupant occupant = null;
    Presence presence = null; 
    String occupantJid = ""; 
    String tmp = "";

    /*devo effettuare ora una ricerca x status*/
    while( it.hasNext() )
    {
      occupantJid = it.next();
      presence = mucTemp.getOccupantPresence(occupantJid);
      occupant = mucTemp.getOccupant(occupantJid);
      tmp = presence.getStatus();
      
      if(tmp == null)
          continue;
      
      if( (!tmp.isEmpty()) && (tmp.equals("HM")) )
      
      //if( (tmp!=null) && (tmp.equals("HM")) ) //controlla sempre tmp a null! che è importante
          collection.add(occupant);
    }
    return collection;
  }
  
  /*QUESTA FUNZIONE CERCA UN PARTICOLARE HOSTNAME (PER NICK) ALL'INTERNO DELLA STANZA roomtype
   per il momento nessuno la usa!*/
  
  public boolean SearchHM_InRoom(String nickname, ROOM roomtype)//questa funzione cerca l'HM istanziato da questo initiator nella stanza CLEVER_MAIN
  {//return true if exist
      
      Collection<Occupant> hm_inRoom = getHCsInRoom(roomtype);
      Iterator it = hm_inRoom.iterator();
      Occupant hm = null;
      String nick = "";
      
      while(it.hasNext())
      {
          hm = (Occupant) it.next();
          nick = hm.getNick();
          
          if(nick.equals(nickname))
              return true;            
      }
      return false;
  }



  public Collection<Occupant> getUsersInRoom( final ROOM roomType )
  {
    MultiUserChat mucTemp = getMultiUserChat( roomType );

    Collection<Occupant> collection = new LinkedList<Occupant>();
    Iterator<String> it = mucTemp.getOccupants();
    Occupant occupant = null;

    while( it.hasNext() )
    {

      occupant = mucTemp.getOccupant( it.next() );
      collection.add( occupant );

    }
    return collection;
  }



  public XMPPConnection getXMPP()
  {
    return this.connection;
  }
  
  /**
   * Send file to an user
   * @param jid
   * @param message
   */
  public void sendFile( String jid, String file_path ) 
  {
    logger.debug( "Sending file: " + file_path + " to " + jid);
    // See if there is already a chat open
    Chat chat = cleverChatManagerListener.getChat( jid.toLowerCase() );
    if( chat == null )
    {
      logger.debug("Chat toward " + jid + " not found... creating it");
      chat = connection.getChatManager().createChat( jid, new CleverChatListener( msgHandler ) );
      logger.debug("Chat toward " + jid + " created");
    }

      // Create the file transfer manager
      logger.debug("Creating file transfer manager");
      FileTransferManager manager = new FileTransferManager(connection);
		
      // Create the outgoing file transfer
      logger.debug("Creating outgoing file transfer");
      OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(jid);
        try {
            // Send the file
            logger.debug("Sending file " + file_path + " to " + jid);
            File file = new File(file_path);
            
            if (file.exists()){
                transfer.sendFile(file, "You won't believe this!");
                int progress_int = 0;
                int now = 0;
                System.out.print("|");
                    while(!transfer.isDone()) {
                        if(transfer.getStatus().equals(FileTransfer.Status.error)) {
                            logger.debug("Error starting transfering " + transfer.getError());
                        } else if (transfer.getStatus().equals(FileTransfer.Status.in_progress)) {
                            double progress = transfer.getProgress();
                            progress_int = (int)(progress*10);
                            //System.out.println("Progress_int = " + progress_int);
                            int how_many = progress_int - now;
                            //System.out.println("How_many = " + how_many);
                            for (int i=1; i<=how_many;i++ )
                                System.out.print("==");
                            now = progress_int;
                            //System.out.println("Now = " + now);
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {
                            logger.error(ex.toString());
                        }
                    }
                    if (now < 10)
                        for (int i=now+1; i<=10;i++ )
                                System.out.print("==");
                    System.out.println(">|");
                logger.debug("File sent");
            }
            else{
                logger.debug("File does not exist");
            }
          
        } catch (XMPPException ex) {
            logger.error( "Error while sending file: " + file_path + " to " + jid );
      
        }
  }

    /**
   * Receive file from an user
   * @param path
   */

  public String receiveFile(String path){
      // Create the file transfer manager
      logger.debug("Creating file transfer manager");
      final FileTransferManager manager = new FileTransferManager(connection);
      
      // Create the listener
      logger.debug("Creating transfer listener - I will save file in " + path);
      myTransferListener m = new myTransferListener(logger, path);
      
      // Adding listener to manager
      logger.debug("Adding listener to manager");
      manager.addFileTransferListener(m);
      
      logger.debug("Getting my user");
      String myUser = this.connection.getUser();
      
      logger.debug( "My user is: "+ myUser);
      
      return myUser;
  }
  
}