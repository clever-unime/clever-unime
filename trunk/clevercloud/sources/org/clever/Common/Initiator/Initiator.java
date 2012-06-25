/*
 * The MIT License
 *
 * Copyright 2012 Marco Carbone
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.clever.Common.Initiator;

/**
 *
 * @author Marco Carbone
 */

/**** UTILIZZO PATTERN SINGLETON PER SISTEMARE IL CODICE DI QUESTO FILE *******/
import java.io.*;
import java.lang.management.ManagementFactory;
import org.apache.log4j.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Shared.LoggerInstantiator;
import org.clever.Common.Shared.Support;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP.ROOM;
import org.clever.HostManager.HostCoordinator.HostCoordinator;

public class Initiator //questa classe deve istanziarsi una sola volta!!
{    
     LoggerInstantiator loggerInstantiator;  
     Logger logger;
     
     private String classpath;
     private final String cfgPath = "./cfg/configuration_initiator.xml";
     
     private final String cfgPath_CM = "./cfg/configuration_clustercoordinator.xml";

     private ConnectionXMPP conn; 
     
     private ParserXML pXML;
     
     private int threshold;
     
     private static Initiator istanza; 
     private HostCoordinator hc;
     
     private String pidInit; //in questa variabile stampo il nome del processo initiator!
     
     private File cfgFile;
     private InputStream inxml;
     
     private String server;
     private String room;     
     private int port;
     private String username;
     private String password;
     private String nickname;     
     
     private String nick_CM; 
     
     private boolean flag_abilityCM; //se true questo init è abilitato a lanciare CM e controllarlo!
     private boolean flag_abilityHM; //2 se true questo init è abilitato a lanciare HM e controllarlo!
     
     private boolean replaceAgents;
         
     private boolean tls;
     

     private Initiator()       
     {
         loggerInstantiator = new LoggerInstantiator(); 
         
         
         logger = Logger.getLogger( "Initiator" ); 
         classpath = System.getProperty( "java.class.path", null ); 
         conn = null;
         pXML = null;
         threshold = 0;
         pidInit = "";
         cfgFile = null; 
         inxml = null;
         server = "";
         room = "";        
         port = 0;
         username = "";
         password = "";
         nickname = "";
         //tls = 0;
         nick_CM = "";
         //nick_HM = "";
         flag_abilityCM = false; 
         flag_abilityHM = false;  
         replaceAgents = false;
     }
     
     public static Initiator getInstance() throws CleverException//questa funzione restituisce un unica istanza di initiator
     {
         if (istanza == null)
         {
            istanza = new Initiator();
         }
            
         return istanza;
     }
  
     public void init() //funzione di inizializzazione
     {     
         cfgFile = new File( cfgPath ); //apro il riferimento al file             
            
         if( !cfgFile.exists() ) //se il file di configurazione non esiste: siamo alla prima esecuzione dell'initiator!                          
         {                
             //devo quindi copiare il file configuration_template_initiator nella cartella della classe initiato.java, nella cartella al path cfg            		inxml = getClass().getResourceAsStream( "/org/clever/Common/Initiator/configuration_template_initiator.xml" ); //apro lo stream con il file configuration_template_initiator           	
             inxml = getClass().getResourceAsStream( "/org/clever/Common/Initiator/configuration_template_initiator.xml" );
                
             try                
             {                    
                 Support.copy( inxml, cfgFile ); //copio il file!!!                
             }                
             catch( IOException ex ) //se entriamo qui dentro significa che si è verificato un errore con la copia del file!
             {               
                 this.logger.error( "Copy file failed" + ex );                    
                 System.exit( 1 );            	
             }            
         }
         
         try//se invece il file di configurazione esiste, proviamo a:            
         {                
             inxml = new FileInputStream( cfgPath ); //aprire uno stream verso questo file            
         }            
         catch( FileNotFoundException ex )            
         {                
             this.logger.error( "File not found: " + ex );            
         }
         
         try //ad ogni modo adesso il file di configurazione dovrebbe essere pronto            
         {                
             FileStreamer fs = new FileStreamer();        	
             pXML = new ParserXML( fs.xmlToString( inxml ) );            
         }            
         catch( IOException ex )            
         {                
             this.logger.error( "Error while parsing: " + ex );            
         }
            
         server = pXML.getElementContent("server" );            
         room = pXML.getElementContent( "room" );           
         port = Integer.parseInt( pXML.getElementContent( "port" ) );            
         username = pXML.getElementContent( "username" );            
         password = pXML.getElementContent( "password" );
         nickname = pXML.getElementContent( "nickname" );            
         threshold = Integer.parseInt( pXML.getElementContent( "threshold" ) );
         flag_abilityCM = Boolean.parseBoolean(pXML.getElementContent("activatedCM"));
         flag_abilityHM = Boolean.parseBoolean(pXML.getElementContent("activatedHM")); 
         replaceAgents = Boolean.parseBoolean(pXML.getElementContent("replaceAgents"));
         tls = Boolean.parseBoolean( pXML.getElementContent( "tls" ) );        
     }
	
     public void connectionManagement()//questa funzione gestisce la connessione con il server XMPP	
     {               
         try //provo a creare un oggetto ConnectionXMPP con il quale gestirò la connessione als erver XMPP e l'accesso alla stanza!                
         {                   
             conn = new ConnectionXMPP();               
         }               
         catch(CleverException Cexec)               
         {                   
             Cexec.printStackTrace();               
         }               
               
         conn.connect(server, port);//effettuo una connessione al serverXMPP               
               
         if( username.isEmpty() || password.isEmpty() )               
         {   
             username = nickname = conn.getHostName(); //genero il nickname e la username con il quale l'initiator si collega al server XMPP                   
             password = Support.generatePassword( 7 );                   
             conn.inBandRegistration( username, password ); //questa funzione crea un account sul server XMPP, ovviamente tale account va creato una sola volta!                   
                   
             //Salvo username e password sul file di configurazione XML!! Attenzione a questo punto questo file non deve più essere cancellato!!! Perché altrimenti dovremmo rieffettuare una nuova registrazione!                  
             pXML.modifyXML( "username", username );                   
             pXML.modifyXML( "password", password );                   
             pXML.modifyXML( "nickname", nickname );                   
             pXML.saveXML( cfgPath );               
         }               
               
         //procedo con l'autenticazione sul server XMPP:                    
         conn.authenticate(username, password ); //mi autentico sul server XMPP               
         //conn.joinInRoom(room, ROOM.CLEVER_MAIN, conn.getUsername(),"INITIATOR"); //entro nella stanza CLEVER_MAIN con lo status INITIATOR!!!!        
        conn.joinInRoom(room, ROOM.CLEVER_MAIN, nickname); // 28//11/2011: ho cambiato il valore dello status, quando si connette solo l'initiator lo status è vuoto!
         
         conn.addListener(ROOM.CLEVER_MAIN, new Initiator_Listener(this.conn, this, this.flag_abilityCM, this.flag_abilityHM)); //collego il listener all'initiator!                 
     } 
     
     
     public boolean VerificaNecessitaCM(ConnectionXMPP connect)	//torna true se il numero di CM in clever Main è < della soglia!
     {        
         int tmp = connect.getNum_CCsInRoom(ROOM.CLEVER_MAIN); //questa nuova funzione di connection XMPP l'ho fatta io!
         
         if(tmp<threshold)
         {    
             logger.info("Number of CM istantiated is " +tmp);
             return true;		
         }        	
         else            		
             return false;	
     }
          
     
     public void launchClusterCoordinator(String cp) throws CleverException	
     {        	
         logger.info( "Launching cluster coordinator with classpath: " +cp);        	
         try        	
         {			
            Runtime runtime = Runtime.getRuntime();
            String[] command = new String[]{"java", "-cp", cp, "org.clever.ClusterManager.ClusterCoordinator.Main"};
             logger.info("ClusterCoordinato launching: "+command);
             Process ClusterCoordinator = runtime.exec(command);        
                         
             //quì bisognerebbe lanciare lo shutdwnThread di Initiator, passandogli il process Cluster coordinator sopra lanciato!!
            Runnable r = new ShutdownThread_ClusterCoordinator(ClusterCoordinator);
            Thread hook = new Thread( r );
            Runtime.getRuntime().addShutdownHook( hook );
         }       
         catch (IOException ex) 
        {
           logger.info("error launching CLUSTER COORDINATOR: "+ex); //non riesco ad usare il log!
           ex.printStackTrace();
        }
    }
     
     

    public void launchHostCoordinator(ConnectionXMPP conn)  throws CleverException
    {
        logger.info("Lancio host coordinator");
        
        hc = new HostCoordinator(conn); //istanzio l'host coordinator nello stesso processo dell'initiator
        hc.start();
    }
    
   
    public String getNick(String path) //funzione che mi restituisce il nickname del CM e HM istanziati da questo init prelevandoli dai relativi file di configurazione
    {
        FileInputStream fIs = null;
        
        try
        {   
            fIs = new FileInputStream(path);             
        }            
        catch( FileNotFoundException ex )             
        {                
            this.logger.error( "File not found: " + ex );            
        }
        try
        {                
            FileStreamer fs = new FileStreamer();        	
            pXML = new ParserXML( fs.xmlToString(fIs) );            
        }            
        catch( IOException ex )            
        {                
            this.logger.error( "Error while parsing: " + ex );            
        }
        
        return pXML.getElementContent( "nickname" );       
    }
    
    public String getNickCM()
    {
        return this.nick_CM;
    }
    
       
    public int getTH() //restituisce il valore della soglia
    {
        return this.threshold;
    }
    
    public String getCP() //restituisce il classpath
    {
        return this.classpath;
    }
    
          
    public void start() throws CleverException
    {
        this.init(); //si inizializza
        this.connectionManagement(); //si connette alla stanza Clever_main
        
        this.pidInit = ManagementFactory.getRuntimeMXBean().getName(); //ottengo il pid del processo Initiator
        
        logger.info("Lancio processo Initiator, pid: "+pidInit); //stampo questo pid sul logger
        
        if(this.flag_abilityHM) //se l'initiator è abilitato a lanciare HM:
            this.launchHostCoordinator(this.conn); //fa partire in un processo separato l'Host Coordinator
        
        if(this.flag_abilityCM) //se questo CM è abilitato a lanciare CM
        {
            boolean tmp = this.VerificaNecessitaCM(conn); //questo valore lo uso solo dentro il log!
            
            if(this.VerificaNecessitaCM(this.conn))            
            { //controlla il numero di Cm connessi nella stanza clever_main
               logger.info("Number of CM istantiated: " +tmp +"treshold is: " +this.getTH());
               logger.info("A new Cluster Manager is istantiated!");
                    
               this.launchClusterCoordinator(this.classpath); //se nn sono sufficienti ne lancia uno!
            }
        
                       
        //this.nick_CM = this.getNick(this.cfgPath_CM); //memorizzo il valore del pid del CM lanciato Mi SA CHE NN VIENE PIÙ USATO!
        }
    }
}
		
		
		