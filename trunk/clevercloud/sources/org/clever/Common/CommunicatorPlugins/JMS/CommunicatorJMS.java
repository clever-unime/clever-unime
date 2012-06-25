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
package org.clever.Common.CommunicatorPlugins.JMS;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CommunicationPlugin;
import org.clever.Common.Communicator.MessageHandler;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.MessageFormatter;



public class CommunicatorJMS implements CommunicationPlugin, MessageListener
{

  private MessageHandler mh;
  private ActiveMQConnectionFactory connectionFactory;
  private Connection connection;
  private Session session;
  private MessageProducer producer;
  private MessageConsumer consumer;
  private boolean transacted = false;
  private Destination destination;
  String clientId = null, text = null;
  private Logger logger;



  @Override
  //TODO: update plugin to manage "group"
  public void init( String topic , String group)
  {
    try
    {
      logger = Logger.getLogger( "JMSCommunicationPlugin" );
      logger.info( "Initializing the JMS Communication plugin " );
      connectionFactory = new ActiveMQConnectionFactory( ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, ActiveMQConnection.DEFAULT_BROKER_URL );
      logger.debug( "Connection factory to ActiveMQ using " + ActiveMQConnection.DEFAULT_USER
                    + " as username and " + ActiveMQConnection.DEFAULT_BROKER_URL + " as url" );
      connection = connectionFactory.createConnection();
      logger.debug( "Creating the connection to the ActiveMQ and waiting for start... " );
      connection.start();
      logger.debug( "Connection to the ActiveMQ started" );
      session = connection.createSession( transacted, Session.AUTO_ACKNOWLEDGE );
      destination = session.createTopic( topic );
      clientId = connection.getClientID();
      //Create producer
      producer = session.createProducer( destination );
      producer.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
      //Create consumer
      consumer = session.createConsumer( destination, null, true );
      consumer.setMessageListener( this );
    }
    catch( JMSException ex )
    {
      logger.error( "Error while initializing the JMS Communication plugin: ", ex );
    }
  }



  public String sendRecv( String to, String msg ) throws CleverException
  {
    try
    {
      // Create link Session and Topic
      Session linkSession = connection.createSession( transacted, Session.AUTO_ACKNOWLEDGE );
      Destination linkDestination = linkSession.createTopic( to );
      MessageProducer linkProducer = linkSession.createProducer( linkDestination );
      linkProducer.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
      // create Temporaty Topic
      Session tmpSession = connection.createSession( transacted, Session.AUTO_ACKNOWLEDGE );
      TemporaryTopic tmp = tmpSession.createTemporaryTopic();
      //Create Message
      TextMessage message = linkSession.createTextMessage( msg );
      message.setJMSReplyTo( tmp );
      logger.debug( "Sending message to : " + to + " : " + msg );
      sendAMessage( message, linkProducer );
      //Waiting for response
      MessageConsumer c = tmpSession.createConsumer( tmp, null, true );
      logger.debug( "Waiting response ..." );
      TextMessage txt = ( TextMessage ) c.receive();
      logger.debug( "Response received : " + txt.getText() );
      return ( txt.getText() );
    }
    catch( JMSException ex )
    {
      logger.error( "Error while sending and receiving msg: ", ex );
      throw new CleverException( ex.getMessage() );
    }
  }



  @Override
  public void asyncSend( String to, String msg )
  {
    try
    {
      Session linkSession = connection.createSession( transacted, Session.AUTO_ACKNOWLEDGE );
      //Destination linkDestination = session.createTopic(to);
      MessageProducer linkProducer = session.createProducer( destination );
      linkProducer.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
      TextMessage message = linkSession.createTextMessage( msg );
      sendAMessage( message, linkProducer );
    }
    catch( JMSException ex )
    {
      logger.error( "Error while performing asyncSend ", ex );
    }
  }



  private void sendAMessage( TextMessage message, MessageProducer producer ) throws JMSException
  {
    producer.send( message );
  }



  @Override
  public String getName()
  {
    return ( "JMSCommunicationPlugin" );
  }



  @Override
  public String getVersion()
  {
    return ( "3.0" );
  }



  @Override
  public String getDescription()
  {
    return ( "Low-level communication plugin" );
  }



  private void stopConnection() throws JMSException
  {
    session.close();
  }



  @Override
  public void onMessage( Message message )
  {
    String msg = null;
    TextMessage textMessage = null;
    if( message instanceof TextMessage )
    {
      textMessage = ( TextMessage ) message;
      try
      {
        msg = textMessage.getText();
      }
      catch( JMSException ex )
      {
        logger.error( "Error while receving message: ", ex );
      }
    }
    String st = null;
    try
    {
      logger.debug( "Invoking handleMessage of : " + mh );
      st = mh.handleMessage( msg );
    }
    catch( CleverException ex )
    {

      st = MessageFormatter.messageFromObject( ex );
      logger.error( "Error CleverException , sending as Msg: " + ex + " \nINVIERO: " + st );
    }



    if( st != null )
    {
      try
      {
        logger.debug( "Sending reply: " + st );
        sendAMessage( session.createTextMessage( st ), session.createProducer( textMessage.getJMSReplyTo() ) );
      }
      catch( JMSException ex )
      {
        logger.error( "Error while receiving message ", ex );
      }
    }
    else
    {
      logger.error( "ERROR: reply null" + st );
    }
  }



  @Override
  public void setMessageHandler( MessageHandler handler )
  {
    this.mh = handler;
  }
}
