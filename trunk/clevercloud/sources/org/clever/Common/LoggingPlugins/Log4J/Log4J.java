

/*
 * The MIT License
 *
 * Copyright 2014 Riccardo Di Pietro.
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

package org.clever.Common.LoggingPlugins.Log4J;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.jdom.Element;

/**
 *
 * @author Riccardo Di Pietro
 */
public class Log4J {
    
    //###################################
    private Agent owner;
    private String version = "0.0.1";
    private String description = "Clever Logging with Log4J";
    private String name = "Log4J";
    private Logger logger2 = null;
    //###################################

    //variabili interne 
    private String radice;
    private String localPath;
    private String log4jConfigFile;
    private int n;
    private String[] vett;
    /////////////////////
   
    //costruttori
    
    public Log4J() {
        this.log4jConfigFile = "";
        this.vett = null;
        this.n = 0;
        logger2 = Logger.getLogger("LoggingAgent");
        logger2.info("LoggingAgent plugin inizializzato:  ");
    }
    
    /**
     * La versione del costruttore con il parametro radice serve per gestire la 
     * creazione dinamica dei frammenti di default qualora il normale processo
     * di creazione non possa essere avviato a causa della mancanza di qualche 
     * frammento.
     * Il parametro radice viene richiamato in assegnaFrammento() tramite un getRadice()
     * 
     * @param radice
     * @param log4jConfigFile
     * @param vett
     * @param n
     * @param logger 
     */
    public Log4J(String radice, String log4jConfigFile, String[] vett, int n, Logger logger) {
     this.log4jConfigFile = log4jConfigFile;
     this.vett = vett;
     this.n = n;
     this.logger2 = logger;
     this.radice = radice;
         
    }
    
    public Log4J( String log4jConfigFile, String[] vett, int n, Logger logger) {
     this.log4jConfigFile = log4jConfigFile;
     this.vett = vett;
     this.n = n;
     this.logger2 = logger;
            
    }
    
     
    

    private void init (){
       
           
      //logger2.info("SONO DENTRO init() di Log4J.java : ");
      //logger2.debug("Debug Message! su Log4J");
      //logger2.info("Info Message! su Log4J");
      //logger2.warn("Warn Message! su Log4J");
      //logger2.error("Error Message! su Log4J");
      //logger2.fatal("Fatal Message! su Log4J");
      
    }
    
    
    //Metodi setter e getter 
    
    
    public String getRadice() {
        return radice;
    }

    public void setRadice(String radice) {
        this.radice = radice;
    }

    public String getLog4jConfigFile() {
        return log4jConfigFile;
    }

    public void setLog4jConfigFile(String log4jConfigFile) {
        this.log4jConfigFile = log4jConfigFile;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String[] getVett() {
        return vett;
    }

    public void setVett(String[] vett) {
        this.vett = vett;
    }
    

    
    
/**
 * Funzione principale della classe Log4J 
 * @return 0 
 * @return 1 errore 
 */   
public int creaFileConfigurazioneLog(){

     //########################
     //Processo di validazione# 
     //########################
     //logger2.info("Sono entrato in creaFileConfigurazioneLog()"); 
     
     String[] vett_validato= new String[getN()];
     int flag=0;
     int j=0;//dim del vettore validato
     
     for(int i=0;i<getN();i++){
         flag=validaComponenteSW(getVett()[i]);
         if(flag==0){vett_validato[j]=getVett()[i];j++;}
         if(flag==1){vett_validato[j]=assegnaFrammento(getVett()[i],i);j++;}
     flag=0;
     }//
     
     //debug
     //for(int i=0;i<j;i++){logger2.info("Componente software validato: "+vett_validato[i]);}
     //

     //adesso vett_validato contiene l'input corretto da passare 
     //al processo di composizione del file di configurazione
     
     //#########################
     //Processo di composizione# 
     //#########################
     
     String log_finale = "";
     
     //stringa che contiene la configurazione finale
     log_finale=componiConfLog(vett_validato,j);
     
     //System.out.println(log_finale);
     
     int alert=0;
     
     //salvo la stringa contenente la configurazione finale  
     //sul file deputato ad essere il file di configurazione
     
     alert=stringToFile(getLog4jConfigFile(),log_finale);
     
    // logger2.info(getLog4jConfigFile());
     
     if(alert==1){logger2.error("\n\nERRORE nel processo di creazione del file di configurazione.\n\n");}
     //else {logger2.info("CREATO: "+getLog4jConfigFile());}
     
     //logger2.debug("Sono uscito da creaFileConfigurazioneLog().\n\n");
     return alert;
     }//creaFileConfigurazioneLog
   
/**
  * Questa funzione serve per validare un componente software 
  * rispetto al processo di creazione dinamica del file di 
  * configurazione di log4j. La "validazione" consiste nella 
  * verifica dell'esistenza dei 3 file "frammenti" richiesti come 
  * condizione necessaria.Non viene effettuato nessun controllo 
  * sul contenuto dei file.
  * 
  * appender.txt
  * logger.txt
  * rootLogger.txt
  * 
  * @param path path dove trovare i file per un dato componente software
  * @return 0 procedura corretta
  * @return 1 errore, almeno 1 file non è stato trovato
  */  
public int validaComponenteSW(String path){
     //flag restituito 
     int flag=0;
     //##################################
     //Verificare esistenza appender.xml#
     //##################################
     String appender=path+"/appender.xml";
     //apro il file 
     flag=verificaFile(appender);
     //################################
     //Verificare esistenza logger.xml#
     //################################
     if(flag==0){
     String logger=path+"/logger.xml";
     flag=verificaFile(logger);
     }
     //####################################
     //Verificare esistenza rootLogger.xml#
     //####################################
     if(flag==0){
     String rootLogger=path+"/rootLogger.xml";
     //apro il file 
      flag=verificaFile(rootLogger);
     }
return flag;
}//validaPath   
   
/**
 * Questa funzione serve a fornire dei frammenti di default per le componenti sw
 * che altrimenti non potrebbero essere validate (ossia mancano di un loro frammento)
 * I frammenti di default creati verranno creati in una directory interna al loro path. 
 * @param componente_sw non validato
 * @param n_c_sw serve per creare un nome diverso per ogni logger di ogni nuovo componente di default creato 
 * @return 
 */
public String assegnaFrammento(String componente_sw, int n_c_sw){
    String output="";  int flag=0; String nome_app="app_def_";
      
    //creo il nuovo path
    output=componente_sw+"/frammento_default/";
    //se già esiste la cancello
    deleteDir(output);
    //creo la directory
    creaDir(output);
    
    String path_log =fileToString(componente_sw+"/path_log.txt");
    //logger2.info("path_log: " +path_log);
    
    //##########################################
    //creo il contenuto di appender.xml
    String text_appender ="<appender name=\""+nome_app+n_c_sw+"\" class=\"org.apache.log4j.FileAppender\">\n" +
"   <param name=\"file\" value=\""+path_log+"DEBUG_default.txt"+"\"/>\n" +
"   <layout class=\"org.apache.log4j.PatternLayout\" >\n" +
"     <param name=\"ConversionPattern\" value=\"%d{yyyy-MM-dd HH:mm:ss} %p [%C:%L] - %m%n\"/>     \n" +
"   </layout>\n" +
"</appender>\n  "+
"<appender name=\""+nome_app+n_c_sw+1+"\" class=\"org.apache.log4j.FileAppender\">\n" +
"   <param name=\"file\" value=\""+"LOGS/DEBUG.txt\"/>\n" +
"   <layout class=\"org.apache.log4j.PatternLayout\" >\n" +
"        <param name=\"ConversionPattern\" value=\"%5p [%t] (%F:%L) - %m%n\"/>     \n" +
"   </layout>\n"+
"</appender>";
    
    //creo il file
    flag=stringToFile(output+"appender.xml",text_appender);
    if(flag==1){exit(1);logger2.error("ERRORE nella creazione del file appender.xml!!!");}
    //##########################################
    //bisogna valutare il numero di logger presenti sul file
       
        
           // apro il file che contiene i nomi dei logger
           ArrayList lista = new ArrayList();
           String file = null;
           file = fileToString(componente_sw+"/logger.txt");
           lista = stringToArrayList(file);
           
           //debug
           //System.out.println("il file contiene logger n°: "+lista.size()+"\n\n");
           //System.out.println(lista);
               
       
    //creo il contenuto di logger.xml
    String text_logger =""; 
    String comodo="";
    
    for(int i=0;i<lista.size();i++){
        comodo = "<logger name=\""+lista.get(i)+"\" additivity=\"false\">\n<level value=\"debug\"/>\n";
        text_logger= text_logger+ comodo+
                "<appender-ref ref=\""+nome_app+n_c_sw+"\" />\n"+
                "<appender-ref ref=\""+nome_app+n_c_sw+1+"\" />\n"+
                "\n</logger>\n\n";
        
    }//for
    
    //debug
    //System.out.println(text_logger);
        
    //creo il file
    flag=stringToFile(output+"logger.xml",text_logger);
    if(flag==1){exit(1);logger2.error("ERRORE nella creazione del file logger.xml!!!");}
    
    //##########################################
    //creo il contenuto di rootLogger.xml
    String text_rlogger = " <appender-ref ref=\""+nome_app+n_c_sw+"\"/>\n"+"<appender-ref ref=\""+nome_app+n_c_sw+1+"\"/>\n";
    //creo il file
    flag=stringToFile(output+"rootLogger.xml",text_rlogger);
    if(flag==1){exit(1);logger2.error("ERRORE nella creazione del file rootLogger.xml!!!");}
    //##########################################
    
 return output;    
}

/**
 * Questo metodo compone dinamicamente il file di configurazione di log4j a partire
 * dai frammenti presenti su percorsi specifici dei componenti software.
 * @param vett_ok vettore contenente i path dove prendere i file dei componenti software validati
 * @param n dim fisica del vettore vett_ok
 * @return stringa contenente il file di configurazione di log finale
 */
public String componiConfLog(String[] vett_ok,int n){
     String Log_Finale ="";
    
              
     String TESTA_configuration ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE log4j:configuration SYSTEM \"log4j.dtd\">\n<log4j:configuration>\n";
     String CODA_configuration ="</log4j:configuration>";
     
     String TESTA_rootLogger = "<logger name=\"log4j.rootLogger\" additivity=\"false\">\n<level value=\"DEBUG\"/>\n";
     String CODA_rootLogger ="</logger>\n";
     
     
       
     //stringhe di comodo 
     String Appenders ="";
     String Loggers ="";
     String RootLoggers ="";
     
     
    //funzione per la fusione di tutti gli appender
    Appenders=componiAppConf(vett_ok,n);
    //System.out.println("Appenders"+Appenders);
    //funzione per la fusione di tutti logger
    Loggers=componiLogConf(vett_ok,n);
    //System.out.println("Loggers"+Loggers);
    //funzione per la fusione di tutti i rootLogger
    RootLoggers=componirootLogConf(vett_ok,n);
    //System.out.println("RootLoggers"+RootLoggers);
    
    Log_Finale=TESTA_configuration+Appenders+Loggers+TESTA_rootLogger+RootLoggers+CODA_rootLogger+CODA_configuration;
    
 return Log_Finale;
}//componiConfLog 

/**
 * Questo metodo preleva tutti i frammenti di tipo appender da tutti i componenti 
 * software e li fonde in un unica stringa.
 * @param path vettore contenente i path dove prendere i file dei componenti software
 * @param n dim fisica di path
 * @return stringa contenente tutti i frammenti appender delle componenti presnti nel vettore path 
 */
public String componiAppConf(String [] path,int n){
   // logger2.debug("Sono entrato in componiAppConf(), routine di creazione del frammento appender globale.\n\n");
    String com1 ="",com2 ="";
   // logger2.info("Elaboro: ");
    for(int i=0;i<n;i++){
       // logger2.debug(path[i]+"appender.xml  "+i);
        com1=fileToString(path[i]+"appender.xml");
        com2=com2+com1;
       
    }
 
 //logger2.debug("Sono uscito da componiAppConf().\n\n");
 return com2;   
}//componiAppConf

/**
 * Questo metodo preleva tutti i frammenti di tipo logger da tutti i componenti 
 * software e li fonde in un unica stringa.
 * @param path vettore contenente i path dove prendere i file dei componenti software
 * @param n dim fisica di path
 * @return stringa contenente tutti i frammenti logger delle componenti presenti nel vettore path
 */
public String componiLogConf(String[] path,int n){
  //  logger2.debug("Sono entrato in componiLogConf(), routine di creazione del frammento logger globale.\n\n");
    String com1 ="",com2 ="";
  //  logger2.debug("Elaboro: ");
    for(int i=0;i<n;i++){
   //     logger2.debug(path[i]+"logger.xml  "+i);
        com1=fileToString(path[i]+"logger.xml");
        com2=com2+com1;
    }
    
 //logger2.debug("Sono uscito da componiLogConf().\n\n");
 return com2;   
}//componiLogConf

/**
 * Questo metodo preleva tutti i frammenti di tipo rootLogger da tutti i componenti 
 * software e li fonde in un unica stringa.
 * @param path vettore contenente i path dove prendere i file dei componenti software
 * @param n dim fisica di path
 * @return stringa contenente tutti i frammenti rootLogger delle componenti presenti nel vettore path
 */
public String componirootLogConf(String[] path,int n){
  //  logger2.debug("Sono entrato in componirootLogConf(), routine di creazione del frammento logger globale.\n\n");
    String com1 ="",com2 ="";
  //  logger2.debug("Elaboro: ");
    for(int i=0;i<n;i++){
  //      logger2.debug(path[i]+"rootLogger.xml  "+i);
        com1=fileToString(path[i]+"rootLogger.xml");
        com2=com2+com1;
    }
 // logger2.debug("Sono uscito da componirootLogConf().\n\n");
 return com2;   
}//componirootLogConf

/**
 * Questa funzione serve per verificare l'esistenza di un file
 * @param path percorso del file da verificare 
 * @return 0 il file esiste
 * @return 1 i1 filenon esiste
 */
 public int verificaFile(String path){
  //   logger2.debug("Entro in verificaFile()\n, eseguo il controllo sull'esistenza del file: "+path);
    int flag=0;
     //apro il file 
     File  file1 =new File(path);
     boolean existsFile1 = file1.isFile();
     if (!existsFile1){
         flag=1;
    // logger2.info("Il file: "+path+" non esiste!!!");
     }
     //if(flag==0){logger2.debug("Il file"+path+" esiste. Esco da verificaFile()");}
return flag;
}//verificaFile

/**
 * Questo metodo scrive in modalità append il contenuto_da_appenere (stringa), dentro il file_contenitore
 * (indicato col suo path)
 * @param contenitore path del file da riempire in modalità append
 * @param contenuto contenuto da mettere nel file contenitore
 */
public void componiFile(String contenitore, String contenuto){
 //  logger2.debug("Entro in componiFile()");
   
    BufferedWriter bw = null;
   String file;
try {
    bw = new BufferedWriter(new FileWriter(contenitore, true));
    
    file=fileToString(contenuto);
    
    bw.write(file);
    bw.newLine();
    bw.flush();
} catch (IOException ioe) {
    ioe.printStackTrace();
} finally { // chiudo sempre il file
    if (bw != null) {
        try {
            bw.close();
        } catch (IOException ioe2) {
            // nn la gestisco
        }
    }
}
//logger2.debug("Esco da componiFile()");
}//componiFile

/**
 * Questo metodo restituisce una stringa che contiene il contenuto del file 
 * indicato col suo "path".
 * Funzione usata all'interno di componiFile()
 * @param path percorso del file
 * @return 
 */
public String fileToString( String path ){
    BufferedReader reader = null;
        try {
            reader = new BufferedReader( new FileReader (path));
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(Log4J.class.getName()).log(Level.SEVERE, null, ex);
        }
    String         line = null;
    StringBuilder  stringBuilder = new StringBuilder();
    String         ls = System.getProperty("line.separator");
        try {
            while( ( line = reader.readLine() ) != null ) {
                stringBuilder.append( line );
                stringBuilder.append( ls );
            }   } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Log4J.class.getName()).log(Level.SEVERE, null, ex);
        }
   //System.out.println(stringBuilder.toString());
    return stringBuilder.toString();
}

/**
 * Questo metodo crea un nuovo file al "path" definito e gli assegna "text" 
 * come contenuto.
 * @param path dove crea il file
 * @param text testo che assegna al file
 * @return 0 funzionamento regolare
 * @return 1 errore
 */
public int stringToFile(String path, String text){
  //  logger2.debug("Entro in stringToFile()");
    int flag =0;
    PrintWriter out = null;
       try {
           out = new PrintWriter(path);
           out.println(text);
       } //try
       catch (FileNotFoundException ex) {
           logger2.error("Errore: "+ex);
           flag=1;
       } finally {
           out.close();
       }//finally
 // if(flag==0 ){logger2.debug("operazione completata con successo");}     
//  logger2.debug("Esco da stringToFile()");     
 return flag;
} //strinToFile

/**
 * Questo metodo crea una directory dato il path
 * @param path 
 */
  public void creaDir(String path)
  {
    
    boolean success = (new File(path)).mkdir();

    if (success)
    {
      logger2.debug("Ho creato: " + path);
    }else{
      logger2.error("Impossibile creare: " + path);
    }
}//creaDir

/**
 * cancella un file
 * @param file 
 */
public void deleteFile(String file){
   // Creo un oggetto file
    File f = new File(file);
    f.delete();
    
}//deleteFile

/**
 * Cancella una direcotry, anche piena. E' ricorsiva.
 * @param path
 * @return 
 */
public static boolean deleteDirectory(File path) {
             if(path.exists()) {
              File[] files = path.listFiles();
              for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
              }
      }
      return(path.delete());
}//deleteDirectory

/**
 * Cancella una directory, anche piena. E' ricorsiva.
 * @param path percorso della directory
 * @return 
 */
public static void deleteDir(String path) {
    
    File file = new File(path);
      
             if(file .exists()) {
              File[] files = file.listFiles();
              for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
              }
      }
      
}//deleteDir

/**
 * Questo metodo trasforma una stringa di testo in un array list, dove ciscun 
 * campo è una parola della stringa.
 * Viene usato per ricavare i logger presenti in logger.txt, durante la procedura
 * assegnaFrammento() quando deve creare una configurazione di default.
 * @param stringa
 * @return 
 */
public ArrayList stringToArrayList (String stringa){
     //imposto il separatore
     String[] strValues = stringa.split(" "); 
     
     //ad ogni elemento del vettore levo lo spazio prima e dopo del contenuto
     for(int i=0; i<strValues.length; i++) {
      strValues[i]=strValues[i].trim();
      }
     
     ArrayList<String> lista = new ArrayList<String>(Arrays.asList(strValues));
     return lista;
}// stringToArrayList

/**
 * Metodo che aggiorna la configurazione di log4j
 */
public void aggiornaConfToLog4j(){
     //faccio un reset di eventuali precedenti configurazione log4j
     LogManager.resetConfiguration();
     //setto il file di configurazione in log4j
     DOMConfigurator.configure(getLog4jConfigFile());    
    
}//assegnaConfToLog4j

/**
 * Metodo che assegna la configurazione di log4j
 */
public void assegnaConfToLog4j(String file){
    // File com = new File(file);
    //faccio un reset di eventuali precedenti configurazione log4j
     LogManager.resetConfiguration();
     //setto il file di configurazione in log4j
     DOMConfigurator.configure(file);    
    
}//assegnaConfToLog4j


/**
 * 
 * @param logger
 * @param pathLogConf
 * @param pathDirOut 
 */

public void setLog4J(Logger logger, String pathLogConf, String pathDirOut){
      //
      String radice =  System.getProperty("user.dir"); 
      String path = radice +pathLogConf; 
      String log4jConfigFile= path+"/conf.xml";
      String vett[]={path};
      new File(radice+pathDirOut).mkdirs();
      Log4J log =new Log4J();
      //log.creaDir(radice+pathDirOut);
      log=new Log4J(radice,log4jConfigFile,vett,1,logger);
      log.creaFileConfigurazioneLog();
      log.assegnaConfToLog4j(log4jConfigFile);
      //
    }









  
    
}//Log4J
