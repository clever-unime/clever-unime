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
import org.clever.Common.XMLTools.ParserXML;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;



public class MethodConfiguration
{

  private ParserXML parser;
  private String body;
  private Map<Integer, String> attachments;
  private String moduleName;
  private List params;
  private String methodName;
  private Logger logger;



  public MethodConfiguration( String body, Map attachments )
  {
    logger = Logger.getLogger( "MethodConfiguration" );
    this.body = body;
    this.parser = new ParserXML( body );
    this.attachments = attachments;
    params = new ArrayList();
    parseParams();
    parseBasicInfo();
  }



  private void parseParams()
  {
    try
    {
      int j = parser.getElementNumber( "parameter" );
      if( j == 0 )
      {
        params = null;
      }
      else
      {
        for( int i = 0; i < j; i++ )
        {
          if( parser.getElementAttributeContent( "parameter", "useAttachementId", i ).compareTo( "true" ) == 0 )
          {
            params.add(
                    MessageFormatter.objectFromMessage(
                    attachments.get(
                    Integer.parseInt( parser.getElementContent( "parameter", i ) ) ) ) );
          }
          else
          {
            params.add( parser.getElementContent( "parameter", i ) );
          }
        }

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

  }



  private void parseBasicInfo()
  {
    this.moduleName = parser.getElementContent( "module" );
    this.methodName = parser.getElementContent( "operation" );
  }



  public String getModuleName()
  {
    return moduleName;
  }



  public String getMethodName()
  {
    return methodName;
  }



  public List getParams()
  {
    return params;
  }
}
