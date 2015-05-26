/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.administration.exceptions.CleverClientException;

/**
 * Per ora solo costanti utilizzate per i nomi delle properties (per es. "clever.session_factory_name")
 *
 * @author maurizio
 */
public class Environment {
    
    
    private static final Logger log = Logger.getLogger(Environment.class);
    
    //Le chiavi delle properties: forse final ?
    public static String SESSION_FACTORY_NAME = "clever.session_factory_name"; //nome del factory session name
    public static String COMMAND_PROVIDER = "clever.client_provider_class"; //la classe dell CleverCommandClientProvider
    public static String MAX_LENGHT_MESSAGES_QUEUE = "clever.client.max_message_in_queue"; //il max numer di clevermessage nella coda dei messaggi dei CleverCommandClient
    public static String MAX_NUMBER_MESSAGE_HANDLERS = "clever.client.max_message_handlers"; //il max numer di thread gestori dei messaggi clever
    
    
    
    public static String XMPP_SERVER = "xmpp_server"; //indirizzo o nome del server XMPP
    public static String XMPP_USERNAME = "xmpp_username"; //username da utilizzare per la connessione XMPP
    public static String XMPP_PASSWORD = "xmpp_password"; //password da utilizzare per la connessione XMPP
    public static String XMPP_PORT = "xmpp_port"; //porta tcp da utilizzare per la connessione XMPP
    public static String XMPP_ROOM = "xmpp_room"; //xmpp room per i client di Clever (CM + clients)
    public static String XMPP_NICKNAME = "xmpp_nickname"; //nickname con cui entrera' il client nella room
    public static String MESSAGE_MODE = "message_mode"; //modalità di trasmissione messaggi
    public static String OPEN_AM_HOST = "openAmHost"; //modalità di trasmissione messaggi
    public static String OPEN_AM_PORT = "openAmPort"; //modalità di trasmissione messaggi
    public static String OPEN_AM_DEPLOY_URL = "deployUrl"; //modalità di trasmissione messaggi
    
    public static final String MSG_MODE_PLAIN = "plain";
    public static final String MSG_MODE_ENCRYPTED = "encrypted";
    public static final String MSG_MODE_SIGNED = "signed";
    
    //VALORI di default
    public static String COMMAND_PROVIDER_DEFAULT = "org.clever.administration.api.SimpleCleverCommandClientProvider"; //provider di comandi clever di default
    public static String CONFIGURATION_FILE_NAME = "/clever_client.xml"; //file di configurazione
    
    public static Integer MAX_LENGHT_MESSAGES_QUEUE_DEFAULT = 500;
    public static Integer MAX_NUMBER_MESSAGE_HANDLERS_DEFAULT = 20;
    
    
    
    
    
    
    
    /**
     * Legge il file di configurazione XML di default e restituisce un Properties
     * @return 
     */
    public static Properties getPropertiesFromXML(InputStream inxml) throws CleverClientException {
        //TODO: validare l'xml
        FileStreamer fs;
        ParserXML pXML;
        try
        {
            fs = new FileStreamer();
            pXML = new ParserXML( fs.xmlToString( inxml ) );
        }
        catch( IOException ex )
        {
            final String error = "Parser error in configuration file";
            log.error(error);
            throw new CleverClientException(error);
        }
        
        Properties properties = new Properties();
        properties.setProperty(XMPP_SERVER,pXML.getElementContent( "server" ));
        properties.setProperty(XMPP_USERNAME,pXML.getElementContent( "username" ));
        properties.setProperty(XMPP_PASSWORD,pXML.getElementContent( "password" ));
        properties.setProperty(XMPP_PORT,pXML.getElementContent( "port" ));
        properties.setProperty(XMPP_ROOM,pXML.getElementContent( "room"));
        properties.setProperty(XMPP_NICKNAME,pXML.getElementContent( "nickname"));
        properties.setProperty(MESSAGE_MODE, pXML.getElementContent( "mode"));
        /** Added to support OpenAm authentication **/
        properties.setProperty(OPEN_AM_HOST, pXML.getElementContent( "openAmHost"));
        //System.out.println(pXML.getElementContent( "openAmHost"));
        properties.setProperty(OPEN_AM_PORT, pXML.getElementContent( "openAmPort"));
        properties.setProperty(OPEN_AM_DEPLOY_URL, pXML.getElementContent( "openAmDeployUrl"));
        
        String clientProvider = pXML.getElementContent( "CleverClientCommandProviderClass");
        if(clientProvider!=null)
        {
            properties.setProperty(COMMAND_PROVIDER , clientProvider);
        }
        
        
        
        return properties;
    }
    
    
}
