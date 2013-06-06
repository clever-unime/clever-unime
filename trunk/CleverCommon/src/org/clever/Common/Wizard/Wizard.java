/*
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
package org.clever.Common.Wizard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.log4j.*;

public class Wizard {

  String server = "127.0.0.1";
  int port = 5222;
  String username = "user";
  String password = "password";


  final InputStreamReader inputStream = new InputStreamReader( System.in );
  final BufferedReader reader = new BufferedReader( inputStream );
  private Logger logger;

  public Wizard() {
    logger = Logger.getLogger( "Wizard" );
    initConfiguration();
  }

  private void initConfiguration() {    
    server = getStringParameter( "Insert server (" + server + ")" );
    port = getIntParameter( "Insert port (" +  port + ")" );
    username = getStringParameter( "Insert username (" + username + ")" );
    password = getStringParameter( "Insert password (" + password + ")" );
  }


  private String getStringParameter( final String information )
  {
    String parameter = null;
    while( parameter == null || parameter.isEmpty() )
    {
      System.out.print( information + ": " );
      try {
        parameter = reader.readLine();
      } catch (IOException ex) {
        logger.warn( "Error while retrieving string parameter: "  + ex );
        parameter = null;
      }
    }

    return parameter;
  }


  private int getIntParameter( final String information )
  {
    int parameter = 0;
    while( parameter == 0 )
    {
      System.out.print( information + ": " );
      try {
        parameter = Integer.parseInt(reader.readLine());
      } catch (IOException ex) {
        logger.warn( "Error while retrieving int parameter: "  + ex );
        parameter = 0;
      } catch ( NumberFormatException ex )
      {
        logger.warn( "Error while parsing int parameter: "  + ex );
        parameter = 0;
      }
    }

    return parameter;
  }


}
