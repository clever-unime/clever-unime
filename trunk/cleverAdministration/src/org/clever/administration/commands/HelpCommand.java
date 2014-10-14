 /*
 *  Copyright (c) 2011 Antonio Nastasi
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
package org.clever.administration.commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.jdom2.Element;



public class HelpCommand extends CleverCommand
{

  @Override
  public Options getOptions()
  {
    return ( new Options() );
  }



  @Override
  public void exec( CommandLine commandLine )
  {
    HelpFormatter formatter = new HelpFormatter();
    InputStream inxml = getClass().getResourceAsStream( "/org/clever/administration/commands/commands.xml" );
    FileStreamer fs = new FileStreamer();
    ParserXML pXML;
    try
    {
      pXML = new ParserXML( fs.xmlToString( inxml ) );
      List<Element> list = pXML.getRootElement().getChildren( "command" );
      Element element = null;
      CleverCommand cleverCommand = null;
      for( int i = 0; i < list.size(); i++ )
      {
        element = list.get( i );

        String className = element.getChildText( "class" );
        String command = element.getChildText( "string" );
        Class cl;
        try
        {
          cl = Class.forName( className );

          cleverCommand = ( CleverCommand ) cl.newInstance();
          formatter.printHelp( command, cleverCommand.getOptions() );
        }
        catch( ClassNotFoundException ex )
        {
          logger.error( ex );
        }
        catch( IllegalAccessException ex )
        {
          logger.error( ex );
        }
        catch( InstantiationException ex )
        {
          logger.error( ex );
        }
      }
    }
    catch( IOException ex )
    {
      logger.error( ex );
    }

  }



  @Override
  public void handleMessage( Object response )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  
    public void handleMessageError(CleverException response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
