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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;



public class ErrorResult extends Result
{

  private String code;



  public ErrorResult( String code )
  {
    super( ResultType.ERROR );
    this.code = code;
  }

  //wrong constructor Error result must have ERROR type


  public ErrorResult( ResultType type, String code, String module, String method )
  {
    super( type );
    this.code = code;
    this.module = module;
    this.operation = method;
  }



  public String getCode()
  {
    return ( code );
  }



  @Override
  public String generateXML()
  {
    Element root = new Element( "result" );
    root.setAttribute( "type", Result.ResultType.ERROR.name() );
    Document doc = new Document( root );
    root.setAttribute( "useAttachementId", "true" );

    Element moduleElement = new Element( "module" );
    moduleElement.addContent( module );

    Element methodElement = new Element( "operation" );
    methodElement.addContent( operation );

    root.addContent( moduleElement );
    root.addContent( methodElement );

    XMLOutputter xout = new XMLOutputter();
    Format f = Format.getPrettyFormat();
    xout.setFormat( f );
    return ( xout.outputString( doc ) );
  }
}
