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
package org.clever.Common.XMLTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ParserXML
{
  private Element rootElement;
  Document document;
  private Logger logger;

    public Element getRootElement() {
        return rootElement;
    }


  public void modifyXML( final String tag, final String value )
  {
    rootElement.getChild( tag ).setText(  value );
  }

  public void saveXML( final String filename )
  {
    try
    {
      OutputStream out = new FileOutputStream( filename );
      XMLOutputter xmlOutput=new XMLOutputter();
      xmlOutput.setFormat(Format.getPrettyFormat());
      xmlOutput.output(document, out);
      //new XMLOutputter().output( document, out );
    }
    catch ( FileNotFoundException ex )
    {
      logger.error( "Error while saving the file xml: " + ex );
    }
    catch( IOException ex )
    {
      logger.error( "Error while saving the file xml: " + ex );
    }
  }
  
    public ParserXML(File file){
        try {
            logger = Logger.getLogger( "ParserXML" );
            SAXBuilder builder = new SAXBuilder();
            document = (Document) builder.build( file );
            rootElement = document.getRootElement();
        } catch (JDOMException ex) {
            logger.error( "Error while opening the file xml: " + ex );
        } catch (IOException ex) {
            logger.error( "Error while opening the file xml: " + ex );
        }
  }

  public ParserXML( String XMLString )
  {
    logger = Logger.getLogger( "ParserXML" );
    // TODO put here the InputStream and the call xmlToString
    SAXBuilder builder = new SAXBuilder();

    try
    {
      document = builder.build( new StringReader( XMLString ) );
      rootElement = document.getRootElement();
    }
    catch ( JDOMException ex )
    {
     logger.error( "Error while opening the file xml: " + ex );
    }
    catch ( IOException ex )
    {
     logger.error( "Error while opening the file xml: " + ex );
    }
    
  }

  public String getElementContent( String element )
  {
    return rootElement.getChildText( element );

  }

  public String getElementContent( String element, int istanceNumber )
  {
    List listOfTags = rootElement.getChildren( element );
    Element target = ( Element ) listOfTags.get( istanceNumber );
    return target.getText();

  }

  public String getElementAttributeContent( String element, String attribute )
  {
    if ( element.compareTo( rootElement.getName() ) == 0 )
    {
      return rootElement.getAttributeValue( attribute );
    }
    else
    {
      Element elem = rootElement.getChild( element );
      if(elem==null)
          return null;
      return elem.getAttributeValue( attribute );
    }
  }

  public String getStringedSubTree( String element ) throws JDOMException, IOException
  {
    String s = "";
    Element elem = rootElement.getChild( element );
    Iterator lst = elem.getDescendants();
    int i;
    Element e = document.detachRootElement();
    e.removeNamespaceDeclaration( Namespace.NO_NAMESPACE );
    Element e2 = e.getChild( element );
    XMLOutputter xout = new XMLOutputter();
    Format f = Format.getPrettyFormat();
    xout.setFormat( f );
    return ( ( xout.outputString( e2 ).replaceAll( "<" + element + ">", "" ) ).replaceAll( "</" + element + ">", "" ) );
  }

  public String getElementAttributeContent( String element, String attribute, int istanceNumber )
  {
    List listOfTags = rootElement.getChildren( element );
    Element target = ( Element ) listOfTags.get( istanceNumber );
    return target.getAttributeValue( attribute );

  }

  public int getElementNumber( String element ) throws JDOMException, IOException
  {
    return rootElement.getChildren( element ).size();
  }

  /*public boolean exist(String element){
  Element elem = null;
  elem = rootElement.getChild(element);
  if(elem == null){
  return(false);
  }
  else
  return(true);
  }*/
  public boolean exist( String element )
  {
    if ( rootElement.getName().compareTo( element ) == 0 || rootElement.getChildren( element ).size() != 0 )
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  public Document getDocument(){
      return this.document;
  }
}
