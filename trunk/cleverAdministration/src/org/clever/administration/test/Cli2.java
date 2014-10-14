/*
 *  The MIT License
 *
 *  Copyright (c) 2012 Tricomi Giuseppe
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.clever.administration.test;

/**
 *
 * @author Giuseppe Tricomi <giu.tricomi@gmail.com>
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import jline.console.ConsoleReader;
import jline.console.history.History;
//import jlineSimpleCompletor;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.clever.Common.VEInfo.StorageSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.administration.ClusterManagerAdministrationTools;
import org.clever.administration.api.Configuration;
import org.clever.administration.commands.CleverCommand;


import org.clever.administration.exceptions.CleverClientException;
import org.clever.administration.test.TestApi;
import org.clever.administration.api.Session;
import org.clever.administration.api.SessionFactory;
import org.jdom2.Element;



public class Cli2 {
  private String prompt;
  private ConsoleReader cleverConsole;
  private History cleverConsoleHistory;
  private String cfgTemplatePath = "/org/clever/administration/config/config_template.xml";
  private String cfgCLIPath = "cfg";
  private HashMap<String, String> commands = null;
  private Logger logger = null;
  private String args[]=null;
  private String OpenStackhost=null;
   SessionFactory session=null;
  private Thread ta=null;

  public Cli2(String args[],String OpenStackhost)
  {
    this.args=args;
    this.OpenStackhost=OpenStackhost;
    this.init();
  }


  private void init()
    {
        
     
          
    File cliConfiguration = new File( cfgCLIPath + "/config_clever_cli.xml" );
    InputStream inxml = null;
    FileStreamer fs = null;
    ParserXML pXML = null;
    if( !cliConfiguration.exists() )
    {
      createConfigurationFile();
    }

    try
    {
      inxml = new FileInputStream( cfgCLIPath + "/config_clever_cli.xml" );
    }
    catch( FileNotFoundException e )
    {
      System.out.println( "Configuration file not found: " + e );
      System.exit( 1 );
    }

    try
    {
      fs = new FileStreamer();
      pXML = new ParserXML( fs.xmlToString( inxml ) );
    }
    catch( IOException ex )
    {
      System.out.println( "Error in parsig configuration file: " + ex );
      System.exit( 1 );
    }

    ClusterManagerAdministrationTools adminTools = ClusterManagerAdministrationTools.instance();
   
    
    if( !adminTools.connect( pXML.getElementContent( "server" ),
                             pXML.getElementContent( "username" ),
                             pXML.getElementContent( "password" ),
                             Integer.parseInt( pXML.getElementContent( "port" ) ),
                             pXML.getElementContent( "room" ),
                             pXML.getElementContent( "nickname" ) ) )
    {
      System.out.println( "Error in connecting the administrator. The server might be turned off or please verify the configuration file." );
      System.exit( 1 );
    }
    else
    {
      setPrompt( "" );
    }
    
    
    
    
    // Read the configuration from the file in the package
    Properties prop = new Properties();
    InputStream in = getClass().getResourceAsStream( "/org/clever/administration/config/logger.properties" );
    try
    {
      prop.load( in );
    }
    catch( IOException ex )
    {
      System.out.println( "Error while initializing logger: " + ex );
      System.exit( 1 );
    }

    PropertyConfigurator.configure( prop );
    logger = Logger.getLogger( "CLI" );
    
    try
    {



       collectCommands();


      cleverConsole = new ConsoleReader();
      cleverConsole.setBellEnabled( true );
      //cleverConsoleHistory = new History();

      File cleverHistoryFile = new File( "cleverHistory.txt" );
      if( !cleverHistoryFile.exists() )
      {
        System.out.println( "History file created." );
        cleverHistoryFile.createNewFile();
      }
    //  cleverConsoleHistory.setHistoryFile( cleverHistoryFile );
      cleverConsole.setHistory( cleverConsoleHistory );

      if(args!=null)
          showShell(args);
      else
            showShell();

    }
    catch( IOException e )
    {
      System.out.println( "Error history file creation. " + e );
    }
  }


  

  

  private void setPrompt( final String hostname )
  {
    String username = ClusterManagerAdministrationTools.instance().getConnectionXMPP().getUsername();
    prompt = "clever@" + username;
    if( !hostname.isEmpty() )
    {
      prompt += "/" + hostname;
    }
    prompt += "#";
  }



  public boolean createConfigurationFile()
  {
    InputStream inxml = null;
    FileStreamer fs = null;
    ParserXML pXML = null;
    System.out.println( "----------------------------------------" );
    System.out.println( "| Administration Console Configuration |" );
    System.out.println( "----------------------------------------" );
    System.out.println( "\n\nPlease insert the following data:\n\n" );
    try
    {
      inxml = getClass().getResourceAsStream( cfgTemplatePath );
    }
    catch( Exception e )
    {
      System.out.println( "Template file not found. " + e );
      System.exit( 1 );
    }
    try
    {
      fs = new FileStreamer();
      pXML = new ParserXML( fs.xmlToString( inxml ) );
    }
    catch( IOException e )
    {
      System.out.println( "Error while parsing: " + e );
      System.exit( 1 );
    }
    try
    {
      cleverConsole = new ConsoleReader();
      cleverConsole.setBellEnabled( false );
      String server = cleverConsole.readLine( "server XMPP: " );
      String port = cleverConsole.readLine( "port: " );
      String room = cleverConsole.readLine( "room: " );
      String username = cleverConsole.readLine( "username: " );
      String password = cleverConsole.readLine( "password: ", new Character( '*' ) );
      String nickname = cleverConsole.readLine( "nickname: " );
      pXML.modifyXML( "server", server );
      pXML.modifyXML( "port", port );
      pXML.modifyXML( "username", username );
      pXML.modifyXML( "password", password );
      pXML.modifyXML( "nickname", nickname );
      pXML.modifyXML( "room", room );
      File cliConfiguration = new File( cfgCLIPath );
      if( !new File( cfgCLIPath ).exists() )
      {
        cliConfiguration.mkdirs();
      }
      pXML.saveXML( cfgCLIPath + "/config_clever_cli.xml" );
      System.out.println( "Configuration file created." );
      return true;
    }
    catch( IOException e )
    {
      System.out.println( "Configuration file creation failed. " + e );
      return false;
    }
  }



  private void collectCommands() throws IOException
  {
    InputStream inxml = getClass().getResourceAsStream( "/org/clever/administration/commands/commands.xml" );
    FileStreamer fs = new FileStreamer();
    ParserXML pXML = new ParserXML( fs.xmlToString( inxml ) );
    commands = new HashMap<String, String>();
    List<Element> list = pXML.getRootElement().getChildren( "command" );
    String commandStrings[] = new String[ list.size() ];
    Element element = null;
    for( int i = 0; i < list.size(); i++ )
    {
      element = list.get( i );
      commands.put( element.getChildText( "string" ), element.getChildText( "class" ) );
      commandStrings[i] = element.getChildText( "string" );
    }


  }



  public void showCurrentConfig()
  {
    InputStream inxml = null;
    FileStreamer fs = null;
    ParserXML pXML = null;
    try
    {
      inxml = new FileInputStream( cfgCLIPath + "/config_clever_cli.xml" );
    }
    catch( FileNotFoundException e )
    {
      System.out.println( "Configuration file not found. " + e );
      return;
    }
    try
    {
      fs = new FileStreamer();
      pXML = new ParserXML( fs.xmlToString( inxml ) );
      System.out.println( "\n-------------------------" );
      System.out.println( "| Current Configuration |" );
      System.out.println( "-------------------------\n" );
      System.out.println( "Server: " + pXML.getElementContent( "server" ) );
      System.out.println( "Port: " + pXML.getElementContent( "port" ) );
      System.out.println( "room: " + pXML.getElementContent( "room" ) );
      System.out.println( "username: " + pXML.getElementContent( "username" ) );
      System.out.println( "password: " + pXML.getElementContent( "password" ) );
      System.out.println( "nickname: " + pXML.getElementContent( "nickname" ) );
      System.out.println();
    }
    catch( IOException e )
    {
      System.out.println( "Error while parsing: " + e );
    }

  }



  private Class classFromCommand( final String command ) throws ClassNotFoundException
  {
    String className = commands.get( command.split( " " )[0] );
    if(className==null)
        throw new ClassNotFoundException();
    Class cl = Class.forName( className );

    return cl;
  }




  public void showShell(String args[])
  {
        try
        {
            CommandLineParser parser = new PosixParser();
            CleverCommand cleverCommand = ( CleverCommand ) Class.forName(commands.get(args[0])).newInstance();
            CommandLine cmd = parser.parse( cleverCommand.getOptions(), args);
            cleverCommand.exec( cmd );
        }
        catch( ParseException ex )
        {
          logger.error( ex );
          System.out.println( "Command not found" );
        }
        catch( ClassNotFoundException ex )
        {
          logger.error( ex );
          System.out.println( "Command not found" );
        }
        catch( IllegalAccessException ex )
        {
          logger.error( ex );
          System.out.println( "Command error" );
        }
        catch( InstantiationException ex )
        {
          logger.error( ex );
          System.out.println( "Command error" );
        }
        //whit this row we can permits at the user to stop this process in all case kill this process.
        try{
            java.util.Scanner sc = new java.util.Scanner(System.in);
            System.out.println("Premere invio per terminare:");
            sc.nextLine();
            //System.exit(0);
        }
        catch(java.util.NoSuchElementException ex ){
            logger.error( ex );
            System.out.println( "No new line has finded. The process will be terminated!" );
            //System.exit(0);
        }
        catch(IllegalStateException ex){
            logger.error( ex );
            System.out.println( "The scanner for termination of shell process has been terminated. The process will be terminated!" );
            System.exit(0);
        }
  }
  

  public void showShell()
  {
    String command = "";
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;
    String a[] = {};
    //cleverConsole.addCompletor( new SimpleCompletor( commands.keySet().toArray(a) ) );
    try
    {
      do
      {
        command = cleverConsole.readLine( prompt );  
        if( command.isEmpty() )
        {
          continue;
        }

       // cleverConsoleHistory.addToHistory( command );

        CleverCommand cleverCommand;
        try
        {
          if(command.equals("exit"))
          {
              return;
          }
          /*String[] opts=command.split( " " );
          if(opts[opts.length-1].equals("openStack"))
          {
              if(command.split(" ")[0].equals("createvm"))
              {
                  
              }
              if(command.split(" ")[0].equals("deletevm"))
              {
                  
              }
          }*/
          
          cleverCommand = ( CleverCommand ) classFromCommand( command ).newInstance();
          cmd = parser.parse( cleverCommand.getOptions(), command.split( " " ) );
          cleverCommand.exec( cmd );
        }
        catch( ParseException ex )
        {
          logger.error( ex );
          System.out.println( "Command not found" );
        }
        catch( ClassNotFoundException ex )
        {
          logger.error( ex );
          System.out.println( "Command not found" );
        }
        catch( IllegalAccessException ex )
        {
          logger.error( ex );
          System.out.println( "Command error" );
        }
        catch( InstantiationException ex )
        {
          logger.error( ex );
          System.out.println( "Command error" );
        } 
      } while(true);
    }
    catch( IOException ex )
    {
      logger.error( "Shell IO Error: " + ex );
      //this.session.closeAllSessions();
      System.exit( 1 );
    }
  }

}
