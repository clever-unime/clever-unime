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

import org.clever.Common.XMLTools.ParserXML;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.MessageFormatter;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.safehaus.uuid.UUIDGenerator;
import org.xml.sax.SAXException;



public class CleverMessage
{
  public enum MessageType
  {
    REQUEST,
    NOTIFY,
    REPLY,
    ERROR,
    UNKNOWN
  }

  private Schema xsd = null;
  private Map<Integer, String> attachments = new HashMap<Integer, String>();
  private String src = "";
  private String dst = "";
  private String body = "";
  private MessageType type = MessageType.UNKNOWN;

  // TODO remove it and use attachements.size()
  private int attachCounter = 0;
  // Message Id, must be a unique integer
  private int id = 0;
  // Contains the id of request message
  private int replyToMsg = 0;
  private UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
  // Specify if the message require an operation answer
  private boolean hasReply = false;
  private Logger logger = Logger.getLogger( "CleverMessage" );



  public CleverMessage()
  {
    id = Math.abs( uuidGenerator.generateTimeBasedUUID().hashCode() );
  }



  public CleverMessage( final String xml )
  {
    ParserXML pars = new ParserXML( xml );

    dst = pars.getElementContent( "destination" );
    src =  pars.getElementContent( "source" );
    id =  Integer.parseInt( pars.getElementAttributeContent( "message", "id" ) );
    replyToMsg = Integer.parseInt( pars.getElementContent( "replyToMsg" ) );
    hasReply = ( pars.getElementContent( "hasReply" ).equalsIgnoreCase( "true" ) ? true : false );

   

    int i = 0;
    try
    {
      i = pars.getElementNumber( "attachement" );
      body = pars.getStringedSubTree( "body" );
      for( int j = 0; j < i; j++ )
      {
        addAttachment( pars.getElementContent( "attachement", j ) );
      }
    }
    catch( JDOMException ex )
    {
      logger.error( ex.toString() );
    }
    catch( IOException ex )
    {
      logger.error( ex.toString() );
    }

    if( pars.getElementAttributeContent( "message", "type" ).equalsIgnoreCase( "Request" ) )
    {
      type = CleverMessage.MessageType.REQUEST;
    }
    else if( pars.getElementAttributeContent( "message", "type" ).equalsIgnoreCase( "Reply" ) )
    {
      type = CleverMessage.MessageType.REPLY;
    }
    else if( pars.getElementAttributeContent( "message", "type" ).equalsIgnoreCase( "Notify" ) )
    {
      type = CleverMessage.MessageType.NOTIFY;
    }
    else if( pars.getElementAttributeContent( "message", "type" ).equalsIgnoreCase( "Error" ) )
    {
      type = CleverMessage.MessageType.ERROR;
    }
    else
    {
      type = CleverMessage.MessageType.UNKNOWN;
    }
  }



  public Object getObjectFromMessage() throws CleverException
  {
    Object result = MessageFormatter.objectFromMessage( this.getAttachment( 0 ) );

    //maybe could be checked the type of CleverMessage
    if( result instanceof CleverException )
    {
      throw ( CleverException ) result;
    }
    else if( result instanceof Exception )
    {
      throw new CleverException( ( Exception ) result );
    }
    return result;
  }



  public int getReplyToMsg()
  {
    return replyToMsg;
  }



  public String getSrc()
  {
    return ( src );
  }



  public void setSrc( final String src )
  {
    this.src = src;
  }



  public String getDst()
  {
    return ( dst );
  }



  public void setDst( final String dst )
  {
    this.dst = dst;
  }



  public boolean needsForReply()
  {
    return ( hasReply );
  }



  public void setHasReply( final boolean hasReply )
  {
    this.hasReply = hasReply;
  }



  public MessageType getType()
  {
    return ( type );
  }



  public void setType( MessageType type )
  {
    this.type = type;
  }



  public String getBody()
  {
    return ( body );
  }



  public void setBody( final MessageBody body )
  {
    this.body = body.generateXML();
  }



  public void setBody( final String body )
  {
    this.body = body;
  }


  
  public String getAttachment( final int id )
  {
    return ( attachments.get( Integer.valueOf( id ) ) );
  }



  public Map getAttachments()
  {
    return ( attachments );
  }



  public void addAttachment( final String attach )
  {
    attachments.put( Integer.valueOf( attachCounter ), attach );
    attachCounter++;
  }



  public void setAttachments( Map attachments )
  {
    this.attachments = new HashMap<Integer, String>( attachments );
  }



  public void setBodyAndAttachments( MessageBody body, List attachments )
  {
    setBody( body );
    attachCounter = attachments.size() - 1;
    for( int i = 0; i < attachments.size(); i++ )
    {
      this.attachments.put( Integer.valueOf( i ),
                            MessageFormatter.messageFromObject( attachments.get( i ) ) );
    }
  }



  public void setReplyToMsg( int id )
  {
    this.replyToMsg = id;
  }



  public Schema getSchema()
  {
    return ( xsd );
  }



  public int getId()
  {
    return this.id;
  }



  public void setId( int id )
  {
    this.id = id;
  }



  public void setSchema( final Schema xsd )
  {
    this.xsd = xsd;
  }



  public Schema readSchemaFromFile( final String path ) throws SAXException
  {
    SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
    File f = new File( path );
    return ( sf.newSchema( f ) );
  }



  public boolean validateMessage()
  {
    if( xsd == null )
    {
      System.out.println( "Validation Error\nSchema not setted" );
      return ( false );
    }
    else
    {
      try
      {
        String schemaLang = "http://www.w3.org/2001/XMLSchema";
        SchemaFactory factory = SchemaFactory.newInstance( schemaLang );
        System.out.println( "Starting to read XSchema..." );
        Validator validator = xsd.newValidator();
        validator.validate( new StreamSource( new StringReader( toXML() ) ) );
        System.out.println( "Validation successfull!" );
        return ( true );

      }
      catch( SAXException ex )
      {
        System.out.println( "Cannot validate Message  " + ex.getMessage() );
        return ( false );
      }
      catch( Exception ex )
      {
        ex.printStackTrace();
        return ( false );
      }
    }
  }

 



  public String toXML()
  {
    Element root = new Element( "message" );
    

    root.setAttribute( "id", String.valueOf( id ) );
    root.setAttribute( "type", type.name() );

    Element source = new Element( "source" );

    source.addContent( src );
    root.addContent( source );
    Element destination = new Element( "destination" );
    destination.addContent( dst );

    root.addContent( destination );
    Element timestamp = new Element( "timestamp" );
    timestamp.addContent( new Date().toString() );
    root.addContent( timestamp );
    Element reply = new Element( "replyToMsg" );
    reply.addContent( String.valueOf( replyToMsg ) );
    root.addContent( reply );
    if( hasReply )
    {
      Element hreply = new Element( "hasReply" );
      hreply.addContent( "true" );
      root.addContent( hreply );
    }
    else
    {
      Element hreply = new Element( "hasReply" );
      hreply.addContent( "false" );
      root.addContent( hreply );
    }
    Element b = new Element( "body" );
    SAXBuilder builder = new SAXBuilder();
    Document document = null;
    try
    {
      document = builder.build( new StringReader( this.getBody() ) );
    }
    catch( JDOMException ex )
    {
        /*
        for(StackTraceElement s : ex.getStackTrace()){
            logger.error(s.getClassName()+"."+s.getMethodName()+" -- line: "+s.getLineNumber());
        }
        */
      logger.error( ex.toString() );
    }
    catch( IOException ex )
    {
      logger.error( ex.toString() );
    }
    Element body_root = document.detachRootElement();
    body_root.removeNamespaceDeclaration( Namespace.NO_NAMESPACE );
    b.addContent( body_root );
    root.addContent( b );
    int i;
    for( i = 0; i < attachments.size(); i++ )
    {
      Element elem = new Element( "attachement" );
      elem.setAttribute( "id", String.valueOf( i ) );
      CDATA attach = new CDATA( attachments.get( Integer.valueOf( i ) ) );
      elem.addContent( attach );
      root.addContent( elem );
    }

    XMLOutputter xout = new XMLOutputter();
    Format f = Format.getPrettyFormat();
    xout.setFormat( f );

    Document doc = new Document( root );
    return ( xout.outputString( doc ) );
  }



  public void fillMessageFields( CleverMessage.MessageType type,
                                 String source,
                                 String destination,
                                 boolean hasReply,
                                 List attachments,
                                 MessageBody body,
                                 int id )
  {
    this.setType( type );

    this.setSrc( source );
    this.setDst( destination );
    this.setHasReply( hasReply );
    this.setBodyAndAttachments( body, attachments );
    this.setReplyToMsg( id );

  }
  
  /*
   * Used for notifications
   *
   */
  public void fillMessageFields(CleverMessage.MessageType type,
                                 String source,
                                 List attachments,
                                 MessageBody body){
    this.setType( type );
    this.setSrc( source );
    this.setHasReply( false);
    this.setBodyAndAttachments( body, attachments );
  }

  
  
  public Notification getNotificationFromMessage(){
      Notification notification=new Notification();
      ParserXML pXML = new ParserXML(this.getBody());
      notification.setAgentId(pXML.getElementContent("agentId"));
      notification.setHostId(pXML.getElementContent("hostId"));
      notification.setId(pXML.getElementContent("notificationId"));
      notification.setBody(MessageFormatter.objectFromMessage(this.getAttachment(0))); //todo getObjectFromMessage
      
      return notification;
      
  }
  


  public String getBodyOperation()
  {
    ParserXML pXML = new ParserXML( this.getBody() );
    return pXML.getElementContent( "operation" );
  }



  public String getBodyModule()
  {
    ParserXML pXML = new ParserXML( this.getBody() );
    return pXML.getElementContent( "module" );
  }
}
