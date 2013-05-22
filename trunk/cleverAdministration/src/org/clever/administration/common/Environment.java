/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.administration.ClusterManagerAdministrationTools;
import org.clever.administration.exceptions.CleverClientException;

/**
 * Per ora solo costanti utilizzate per i nomi delle properties (per es. "clever.session_factory_name")
 *
 * @author maurizio
 */
class Environment {
    
    
    private static final Logger log = Logger.getLogger(Environment.class);
    
    //Le chiavi delle properties: forse final ?
    static String SESSION_FACTORY_NAME = "clever.session_factory_name"; //nome del factory session name
    static String CONNECTION_PROVIDER = "clever.xmpp_provider_class"; //la classe dell XMPPProvider
    static String XMPP_SERVER = "xmpp_server"; //indirizzo o nome del server XMPP
    static String XMPP_USERNAME = "xmpp_username"; //username da utilizzare per la connessione XMPP
    static String XMPP_PASSWORD = "xmpp_password"; //password da utilizzare per la connessione XMPP
    static String XMPP_PORT = "xmpp_port"; //porta tcp da utilizzare per la connessione XMPP
    static String XMPP_ROOM = "xmpp_room"; //xmpp room per i client di Clever (CM + clients)
    static String XMPP_NICKNAME = "xmpp_nickname"; //nickname con cui entrera' il client nella room
    
    //VALORI di default
    static String CONNECTION_PROVIDER_DEFAULT = "org.clever.administration.common.SimpleXMPPProvider"; //provider di connessioni xmpp di default
    static String CONFIGURATION_FILE_NAME = "clever_client.xml"; //provider di connessioni xmpp di default
    
    
    
    
    
    
    
    
    /**
     * Legge il file di configurazione XML di default e restituisce un Properties
     * @return 
     */
    static Properties getPropertiesFromXML(InputStream inxml) throws CleverClientException {
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
        return properties;
    }
    
    
}
