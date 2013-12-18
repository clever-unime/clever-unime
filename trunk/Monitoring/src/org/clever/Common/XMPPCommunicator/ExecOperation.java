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

import org.clever.Common.XMLTools.MessageFormatter;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;



public class ExecOperation extends MessageBody
{

  //protected String operation;
  protected Dictionary<String, String> params;



  public ExecOperation( String name, List params, String module )
  {
    this.operation = name;
    this.params = new Hashtable<String, String>();
    this.module = module;
    int i;
    if( params != null )
    {
      for( i = 0; i < params.size(); i++ )
      {
        String st = MessageFormatter.messageFromObject( params.get( i ) );
        this.params.put( Integer.toString( i ), st );
      }
    }
  }



  public String getName()
  {
    return ( operation );
  }



  public void setName( String name )
  {
    this.operation = name;
  }



  public Dictionary getParams()
  {
    return ( params );
  }



  public void setParams( Dictionary params )
  {
    this.params = params;
  }



  public void setModule( String module )
  {
    this.module = module;
  }



    @Override
  public String generateXML()
  {
    int i;
    Element root = new Element( "exec" );
    Document doc = new Document( root );
    Element xmlOperation = new Element( "operation" );
    root.addContent( xmlOperation );
    xmlOperation.addContent( operation );
    Element mod = new Element( "module" );
    mod.addContent( module );
    root.addContent( mod );
    Element[] par = new Element[ params.size() ];
    for( i = 0; i < params.size(); i++ )
    {
      par[i] = new Element( "parameter" );
      par[i].setAttribute( "name", "p" + i );
      par[i].setAttribute( "useAttachementId", "true" );
      par[i].addContent( String.valueOf( i ) );
      root.addContent( par[i] );
    }
    XMLOutputter xout = new XMLOutputter();
    Format f = Format.getPrettyFormat();
    xout.setFormat( f );
    return ( xout.outputString( doc ) );
  }
}
