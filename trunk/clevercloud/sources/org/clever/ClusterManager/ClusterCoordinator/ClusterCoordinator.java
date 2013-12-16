 /*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
 *  Copyright (c) 2012 Marco Carbone
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


package org.clever.ClusterManager.ClusterCoordinator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import java.io.IOException;
import org.apache.log4j.*;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.clever.Common.Shared.Support;
import org.clever.Common.XMPPCommunicator.CleverMessageHandler;
import org.clever.Common.XMPPCommunicator.RoomListener;
import org.clever.ClusterManager.Dispatcher.DispatcherAgent;
import org.clever.ClusterManager.Dispatcher.DispatcherPlugin;
import org.clever.ClusterManager.Info.InfoAgent;
import org.clever.ClusterManager.Brain.BrainInterface;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Initiator.ElectionThread;
import org.clever.Common.Initiator.Listener;
import org.clever.Common.Initiator.ModuleFactory.*;
import org.clever.Common.Shared.Support;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.Common.XMPPCommunicator.CleverMessage;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP.ROOM;
import org.clever.Common.XMPPCommunicator.RoomListener;
import org.jdom.Element;
import org.jivesoftware.smack.packet.Presence.Mode;



public class ClusterCoordinator implements CleverMessageHandler
{
  private ConnectionXMPP conn;
 
  private ParserXML pXML;
  private Logger logger;
  private final String cfgPath = "./cfg/configuration_clustercoordinator.xml";
  private String roomclients;
  private DispatcherAgent dispatcherAgent;
  private DispatcherPlugin dispatcherPlugin;
  private InfoAgent infoAgent;
  private BrainInterface brainInterface;
  
  private File cfgFile;
  
  InputStream inxml;
  
  private String server;
  private String room;
  private int port;
  private String username;
  private String password;
  private String nickname; 
  
  private int numReload; //memorizzo il numero di volte max x rilanciare un agente
  private int timeReload; //memorizzo il tempo max x rilanciare un agente 
  public boolean activeAgents;
  private boolean replaceAgents;
  
  private boolean tls;
    
  public ClusterCoordinator()
  {
      try
      {         
          Properties prop = new Properties();
          InputStream in = getClass().getResourceAsStream("/org/clever/Common/Shared/logger.properties");
          prop.load(in);             
          PropertyConfigurator.configure(prop);
      }
      catch (IOException ex) 
      {
          java.util.logging.Logger.getLogger(ClusterCoordinator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);       
      }
      
      logger = Logger.getLogger( "ClusterCoordinator" );     
      conn = null;

      pXML = null;      
      roomclients = "";      
      dispatcherAgent = null;
      dispatcherPlugin = null;
      infoAgent = null;
      brainInterface = null;      
      cfgFile = null;      
      inxml = null;      
      server = "";
      room = "";
      port = 0;
      username = "";
      password = "";
      nickname = ""; 
      this.numReload = 0;
      this.timeReload = 0;
      tls = false; //non viene attualmente usata la connessione su tls
  }
  
  public void init() 
  {
      cfgFile = new File( cfgPath );
      
      if( !cfgFile.exists() )
      {
          // Copy the content of file
          inxml = getClass().getResourceAsStream( "/org/clever/ClusterManager/ClusterCoordinator/configuration_template_clustercoordinator.xml" );
          
          try
          {
              Support.copy( inxml, cfgFile );
          }
          catch( IOException ex )
          {
              logger.error( "Copy file failed" + ex );
              System.exit( 1 );
          }
      }
      try
      {
          inxml = new FileInputStream( cfgPath );
      }
      catch( FileNotFoundException ex )
      {
          logger.error( "File not found: " + ex );
      }
      try
      {
          FileStreamer fs = new FileStreamer();
          pXML = new ParserXML( fs.xmlToString( inxml ) );
          logger.debug("Setting java.library.path ...");
          //adding to the java.library.path the path containing CLEVER specific dynamic libraries
          //such path is read from the configuration file of the initiator within the node <librariespath>
          System.setProperty( "java.library.path", pXML.getElementContent( "librariespath" ) + ":" + System.getProperty( "java.library.path" ));
          Field fieldSysPath = null;
            try {
                fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            } catch (NoSuchFieldException ex) {
                logger.error("Error setting java.library.path: "+ex);
            } catch (SecurityException ex) {
                logger.error("Error setting java.library.path: "+ex);
            }
          fieldSysPath.setAccessible(true);
            try {
                fieldSysPath.set(null, null);
            } catch (IllegalArgumentException ex) {
                logger.error("Error setting java.library.path: "+ex);
            } catch (IllegalAccessException ex) {
                logger.error("Error setting java.library.path: "+ex);
            }
          System.setProperty( "java.library.path", System.getProperty( "java.library.path" ) + ":" + pXML.getElementContent( "librariespath" ) );
          logger.debug( System.getProperty( "java.library.path" ) );
      }
      catch( IOException ex )
      {
          logger.error( "Error while parsing: " + ex );
      }
      
      
      try
       {  
      server = pXML.getElementContent( "server" );
      room = pXML.getElementContent( "room" );
      roomclients = pXML.getElementContent( "roomclients" );
      port = Integer.parseInt( pXML.getElementContent( "port" ) );
      username = pXML.getElementContent( "username" );
      password = pXML.getElementContent( "password" );
      nickname = pXML.getElementContent( "nickname" );
      this.activeAgents = Boolean.parseBoolean(pXML.getElementContent("activeAgents"));
      this.replaceAgents = Boolean.parseBoolean(pXML.getElementContent("replaceAgents"));
      this.numReload = Integer.parseInt(pXML.getElementContent("numReloadAgent"));
      this.timeReload = Integer.parseInt(pXML.getElementContent("timeReloadAgent"));
       }
      catch (Exception e)
          { 
              logger.error("Error parsing configuration: "+e);
              System.exit(1);
              
          }
      logger.info("\n\n&&&&& i nuovi valori caricati sono: " +numReload  +" "+timeReload);     
      logger.info("\n\nREPLACEAGENTS NEL CC: "+replaceAgents);
      tls = Boolean.parseBoolean( pXML.getElementContent( "tls" ) );  
      
      /*prova campi librarypath:*/
      //adding to the java.library.path the path containing CLEVER specific dynamic libraries
      //such path is read from the configuration file of the initiator within the node <librariespath>
      System.setProperty( "java.library.path", pXML.getElementContent( "librariespath" ) + ":" + System.getProperty( "java.library.path" ));
      Field fieldSysPath = null;
        try {
            fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        } catch (NoSuchFieldException ex) {
            java.util.logging.Logger.getLogger(ClusterCoordinator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            java.util.logging.Logger.getLogger(ClusterCoordinator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
      fieldSysPath.setAccessible(true);
        try {
            fieldSysPath.set(null, null);
        } catch (IllegalArgumentException ex) {
            java.util.logging.Logger.getLogger(ClusterCoordinator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClusterCoordinator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
      System.setProperty( "java.library.path", System.getProperty( "java.library.path" ) + ":" + pXML.getElementContent( "librariespath" ) );
      logger.debug( System.getProperty( "java.library.path" ) );
  }
  
  public boolean getActiveAgents()
  {
      return this.activeAgents;
  }
  
  /** This function handles the connection phase with the XMPP server. 
   *Will be loaded from the configuration file all the necessary parameters to do this.
   *If we are at the first connection, the function will generate a username, a password 
   * and a nickname, by which the registration will be performed on the server. 
   * These parameters are then saved in the xml configuration file for subsequent connections.
   */
   public void connectionManagement(ROOM roomtype, ClusterCoordinator cc) throws CleverException
  {
      conn = new ConnectionXMPP();
            
      if( tls )
      {
          //conn.connectTLS( username, username, password, port, cfgPath, password, password, password );
      }
      else
      {
          logger.info("Connection to server XMPP: "+server +"at port: "+port);
          conn.connect( server, port);
      }
 
    
    // Check if the username or password is blank
    // and try to use In-Band Registration
    if( username.isEmpty() || password.isEmpty() )
    {
      username = nickname = "cm" + conn.getHostName(); 
      password = Support.generatePassword( 7 );
      conn.inBandRegistration( username, password ); 
      pXML.modifyXML( "username", username );
      pXML.modifyXML( "password", password );
      pXML.modifyXML( "nickname", nickname ); 
      pXML.saveXML( cfgPath );
    }

    logger.info("authentication....");  
    conn.authenticate( username, password );
        
    conn.joinInRoom(room, ROOM.CLEVER_MAIN, conn.getUsername(), "CM_MONITOR");
    
    conn.addListener( roomtype, new Listener( conn, cc) );
    this.conn.addChatManagerListener( this ); 
    
    logger.info("DispatcherAgent created");
    dispatcherAgent = new DispatcherAgent(conn);
    dispatcherAgent.initialization();
    
    dispatcherPlugin = dispatcherAgent.getDispatcherPlugin();
    logger.info("Dispatcher Plugin created");
    
    infoAgent = new InfoAgent(conn);
    infoAgent.initialization();
    logger.info("InfoAgent for CM created");   
  } 
  
  /**This function try to set this Cluster Coordinator active, if the election was win
   * to this CC will be call the function SetAsActiveCC with the boolean parameter set true
   */
  public void tryActiveCC(ConnectionXMPP connect, ClusterCoordinator cc)
  {      
      logger.info("Start Election thread....");
      
      //This thread executes the procedure of election at the end of which a CC will be active
      Thread electionThread = new Thread( new ElectionThread(conn, this) );
      electionThread.start();
  }
  
    
  /**
   * @param ConnectionXMPP istance used by this CC to connect to server XMPP
   * @throws CleverException
   * 
   * This function launches the agents of the Cluster Coordinator. 
   * The application of agents can be done in different ways depending on which plug-in is loaded. 
   * Currently you can run them as mere objects (ModuleFactoryLocal), 
   * or in separate processes (ModuleFactoryMultiTasking). 
   * The list of agents to run the configuration file is loaded from xml.   * 
   */
  public void launchAgents(ConnectionXMPP connect) throws CleverException
  {      
      ModuleFactory moduleFactory= ModuleFactory.getInstance();
      
      ModuleFactory.setActiveReplaceAgent(this.replaceAgents);
      
       //setto i valori x il monitor sul rilancio!
      moduleFactory.setReplacementVariable(this.timeReload, this.numReload);
     
      Element agents = pXML.getRootElement().getChild("agents");
      
      if(agents == null)
      {
          logger.error("agents element not found in config");
      }
      else
      {
          List agentsList = agents.getChildren("agent");
          
          for(Object agentO : agentsList)
          {
              Element agent = (Element) agentO;
              String className = agent.getChildText("class");
              
              if(className == null)
              {
                  logger.error("Class element not found in config");
                  continue;
              }
              
              String moduleName = agent.getChildText("name");
              
              if(moduleName.equals(""))
              {
                  moduleFactory.createAgent(className);
              }
              else
              {
                  moduleFactory.createAgent(className,moduleName);
              }
              
              logger.debug("Agent created: "+agent.getChildText("class"));
          }
      }
      
      Element brain = pXML.getRootElement().getChild("brain");
      
      if (brain == null) 
      {
          logger.error("brain element not found in config");
      }
      else 
      {
          String brainClassName = brain.getChildText("class");
          
          if (brainClassName == null) 
          {
              logger.error("class element not found in config");
          }
          else 
          {
              try 
              {
                  Class[] stringArgsClass = new Class[] { DispatcherAgent.class};
                  Object[] stringArgs = new Object[] { dispatcherAgent };
                  Constructor brainConstructor;
                  Class brainClass=Class.forName(brainClassName);
                  brainConstructor=brainClass.getConstructor(stringArgsClass);
                  this.brainInterface= (BrainInterface)brainConstructor.newInstance(stringArgs);
                  //TODO passare dispatcher agent nel costruttore
                  //Class brainClass = Class.forName(brainClassName);
                  //this.brainInterface = (BrainInterface) brainClass.newInstance();
                  logger.debug("Brain created: " + brainClassName);
              }
              catch (InstantiationException ex) 
              {
                  logger.error("brain instantiation exception: " + ex);
              }
              catch (IllegalAccessException ex) 
              {
                  logger.error("brain illegal access exception: " + ex);
              }
              catch (IllegalArgumentException ex) 
              {
                  logger.error("brain illegal argument exception: " + ex);
              }
              catch (InvocationTargetException ex) 
              {
                  logger.error("brain invocation target exception: " + ex);
              }
              catch (NoSuchMethodException ex) 
              {
                  logger.error("brain constructor not found: " + ex);
              }
              catch (SecurityException ex) 
              {
                  logger.error("brain secutiry exception: " + ex);
              }
              catch (ClassNotFoundException ex) 
              {
                  logger.error("brain class not found: " + ex);
              }
          }
      }
      
      //    Agents.add(new DatabaseManagerAgent());
      //    //databasePlugin = (DatabaseManagerPlugin) databaseAgent.getPlugin();
      //
      //    Agents.add(new StorageManagerAgent());
      //    //storagePlugin = (StorageManagerPlugin) storageAgent.getPlugin();
      // Agents.add(new InfoAgent(conn));
      ////    Agents.add(new TestAgent());
      
      logger.info( "Cluster Coordinator created" );
        
      
      
      //start the procedure of shutdown
      if(!moduleFactory.getProcessList().isEmpty()) //if process list is empty no shutdown is needed
      {
          Runnable r = new ShutdownThread( moduleFactory.getProcessList(), moduleFactory.getReplaceAgent() ); //nel momento in cui distruggerò i processi bloccherò anche la procedura di rpristino!
          Thread hook = new Thread( r );
          Runtime.getRuntime().addShutdownHook( hook );  
          logger.info("Shutdown procedure stared");       
      }
  }  

  /** This function has the task to start the Cluster Coordinator, 
   * inside the functions are called in sequence: init(),
   * connectionManagement,  launchAgent() and tryActiveCC().
   * 
   * @throws CleverException 
   */
  public void start() throws CleverException
  {
      logger.info("\nSTART DI CC\n");
      
          this.init();
        try {
            this.connectionManagement(ROOM.CLEVER_MAIN, this);
        } catch (CleverException ex) {
            logger.error("Errore in start: "+ex);
            throw ex;
        }
      
      
      logger.info("Starting procedure launching Agents for CM");    
      
      //check taht this CC is enalble to launch its agents
      //Only CC active can launch its agent
      if(this.activeAgents) 
      {
          logger.info("This CM is FORCED to launch his Agents");
            try {
                this.launchAgents(conn); //lancio cmq gli agenti anche per i CM non attivi
            } catch (CleverException ex) {
                logger.error("Errore in start: "+ex);
                throw ex;
            }
      }
      this.tryActiveCC(conn, this);
  }
    
   
  /**
   * Set the status of the CC Active.
   * The possible states in which there may be a Cluster Manager are ACTIVE and MONITOR.
   *These are discriminated by the status of the XMPP connection.
   * A Cluster Coordinator will be in the active state if its status will be set as: CM_ACTIVE;
   * Similar a Cluster Coordinator will be in the monitor state if its status will be set as: CM_MONITOR
   * 
   * @param boolean active, boolean activeAgents
   * @throws CleverException
   */
  
  public synchronized void setAsActiveCC( final boolean active, boolean activeAgents)throws CleverException
  {
    if( active )
    {   
        logger.debug("devo settare il cm come active cc");
        conn.getMultiUserChat().changeAvailabilityStatus("CM_ACTIVE", Mode.chat ); //set the status of this Cluster Coordinator active
        conn.joinInRoom(roomclients, ROOM.SHELL, conn.getUsername(), "CM_ACTIVE"); //join in the room shell with statu "CM_ACTIVE"
        logger.debug("settato il cm come active cc");
        //If this cluster coordinator has enabled the launch of the agents only if active 
        //(this is dictated by the state of the Boolean variable to true activeAgents)
        //launch it!
        if(!activeAgents)
            this.launchAgents(this.conn);
        try
        {
            conn.getMultiUserChat( ROOM.SHELL ).addMessageListener( new RoomListener(this.dispatcherAgent) );
        }
        catch( CleverException ex )
        {
            logger.error( "Error while inserting message listener: " + ex );
        }
        
        int tmp = conn.getNum_CCsInRoom(ROOM.CLEVER_MAIN);
        logger.info("The number of CM in room CLEVER_MAIN is now: "+tmp);
    }
    else //this condition will never occur but for reasons of backward compatibility we let it
    {
      conn.getMultiUserChat().changeAvailabilityStatus( "CM_MONITOR", Mode.away );
    }
  }
  
  
  @Override
  public synchronized void handleCleverMessage( final CleverMessage msg )
  {
    logger.debug( "Message: " + msg.toXML() );

    dispatcherAgent.handleCleverMessage(msg);
    
    /*
    switch( msg.getType() )

    {

      case NOTIFY:
        this.handleNotification(msg);
        break;
      case ERROR:
      case REPLY:
        dispatcherPlugin.handleMessage( msg );
        break;
      case REQUEST:
        logger.debug("request received"+ msg.getBody());
        dispatcherPlugin.dispatch( msg );
        break;
    }
    *
    * */
  }

 /* public void handleNotification(CleverMessage msg){

      Notification notification=msg.getNotificationFromMessage();
      //Pass notification to dispatcher
      logger.debug("Passing notification to dispatcher");
      dispatcherPlugin.handleNotification(notification);

      //Pass notification to brain
      logger.debug("Passing notification to brain");
      //Notification notification=(Notification)MessageFormatter.objectFromMessage(msg.getBody());
      
      this.brainInterface.handleNotification(notification);
  }
*/
  
  public Iterator getHosts() 
  { //torna la lista di tutti gli host connessi alla stanza CLEVER_MAIN
    return conn.getMembers(ROOM.CLEVER_MAIN);
  }
  
  
}
