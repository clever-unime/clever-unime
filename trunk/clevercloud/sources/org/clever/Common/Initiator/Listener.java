/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
 *  Copyright (c) 2011 Marco Carbone
 * 

 */
package org.clever.Common.Initiator;

import org.apache.log4j.Logger;
import org.clever.ClusterManager.ClusterCoordinator.ClusterCoordinator;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP.ROOM;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;



public class Listener implements ParticipantStatusListener
{

  private ConnectionXMPP conn = null;
  private ClusterCoordinator clusterCoordinator = null;
  private Logger logger;



  public Listener( final ConnectionXMPP conn, final ClusterCoordinator clusterCoordinator )
  {
    logger = Logger.getLogger( "Listener" );
    this.conn = conn;
    this.clusterCoordinator = clusterCoordinator;
  }



  private String getNick( String nickName )
  {
    return ( nickName.substring( nickName.indexOf( "/" ) + 1 ) );
  }



  @Override
  public void nicknameChanged( String oldNick, String newNick )
  {
  }



  /**
   * Callback called when an user left the room
   * @param nickName
   */
  @Override
  public void left( final String nickName )
  {
    // Check if there is an active CM otherwise launch the Election Thread
     
    if ( conn.getActiveCC( ROOM.CLEVER_MAIN ).isEmpty() )
    {
      Thread electionThread = new Thread( new ElectionThread( conn, clusterCoordinator ) );
      electionThread.start();
    }
  }



  /**
   * Callback called when an user join in the room
   * @param nickName
   */
  @Override
  public void joined( final String nickName ) //questa stringa stando al metodo joined dell'interfaccia Partecipant STatus Listener è il Jid!
  {
    String name = getNick( nickName );//tanto è vero che lo tagliamo!
    logger.debug( "Joined:" + nickName );
    try
    {
      // Check if a cluster manager entered in the room
      // and assign the owner privileges
      if ( name.startsWith( "cm" ) ) //qui nn deve controllare il nome bensì lo status!
      {
        String jid = conn.getMultiUserChat().getOccupant( nickName ).getJid();
        conn.getMultiUserChat().grantOwnership( jid ); //quindi far diventare moderatore il CM!
      }
    }
    catch ( XMPPException ex )
    {
      logger.error( "Error while assign permission to:" + nickName );
    }
  }




  @Override
  public void kicked( String arg0, String arg1, String arg2 )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  @Override
  public void voiceGranted( String arg0 )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  @Override
  public void voiceRevoked( String arg0 )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  @Override
  public void banned( String arg0, String arg1, String arg2 )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  @Override
  public void membershipGranted( String arg0 )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  public void membershipRevoked( String arg0 )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  @Override
  public void moderatorGranted( String string )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  @Override
  public void moderatorRevoked( String string )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  @Override
  public void ownershipGranted( String string )
  {
     
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  @Override
  public void ownershipRevoked( String string )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  @Override
  public void adminGranted( String string )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }



  @Override
  public void adminRevoked( String string )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }
}