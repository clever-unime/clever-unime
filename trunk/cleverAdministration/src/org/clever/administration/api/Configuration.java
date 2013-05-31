package org.clever.administration.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.administration.exceptions.CleverClientException;

import org.w3c.dom.Document;


/**
 * Un'istanza di questa classe rappresenta una particolare configurazione del sistema client di Clever.
 * Per esempio server XMPP, sistema di autenticazione, stanza XMPP, ecc.
 * Da un'istanza di questa classe si puo' ottenere un gestore di sessioni clever (SessionFactory) dal quale si otterra' le sessioni
 * separate in threads (Session), che permetteranno di invocare le varie API client. 
 * @author maurizio
 *
 */
public class Configuration {
    
    
        private static final Logger log = Logger.getLogger(Configuration.class);
    
        private SettingsFactory settingsFactory;
        
        private Settings settings = null;

   
    
	/**
	 * Costruttore di default che configura i provider come default: per ora tramite SettingsFactory setta XMPPProvider a SimpleXMPPProvider. (per utilizzare l'oggetto occorre invocare configure).
	 */
	public Configuration() {
            this(new SettingsFactory());
            
            };
	/**
	 * Costruttore che prende un oggetto SettingsFactory .
	 * 
	 * @param sf
	 */
	protected Configuration(SettingsFactory sf) {
            this.settingsFactory = sf;
        };
	
	/**
	 * Restituisce un oggetto settings con valori di default
         * 
	 * Usa il file di default (cleverAdmin)
	 */
	public Settings configure() throws CleverClientException{
            
            InputStream inxml = getClass().getResourceAsStream(Environment.CONFIGURATION_FILE_NAME); //apro il file di conf: default : "clever_client.xml"
            log.debug("Configuration from classpath resource: " + Environment.CONFIGURATION_FILE_NAME);
            if(inxml==null)
            {
                final String error = "Configuration file not found in classpath: " + Environment.CONFIGURATION_FILE_NAME;
                log.error("Configuration file not found in classpath: " + Environment.CONFIGURATION_FILE_NAME);
                throw new CleverClientException(error);
            }
            
            settings = settingsFactory.buildSettings(Environment.getPropertiesFromXML(inxml));
            
            return settings;
        
        };
	
	/**
	 * Restituisce un oggetto settings
	 * Usa il file passato come parametro (file XML)
	 */
	public Settings configure(File f) throws CleverClientException{
            
            if(f==null)
            {
                final String error = "Configuration file not found " ;
                log.error(error);
                throw new CleverClientException(error);
            }
            log.debug("Configuration from file : " + f.getPath());
           
            try {
                FileInputStream fis = new FileInputStream(f);
                settings = settingsFactory.buildSettings(Environment.getPropertiesFromXML(fis));
                return settings;
            } catch (FileNotFoundException ex) {
                 final String error = " Configuration file not found : " + f.getPath();
                log.error(error);
                throw new CleverClientException(error);
            }
        };
	
	
	/**
	 * Restituisce un oggetto settings 
	 * Usa il  parametro come fonte della configurazione: si aspetta , per ora, un file XML
	 */
	public Settings configure(Document d){
             throw new UnsupportedOperationException("Not supported yet.");
        };
	
        public SessionFactory buildSessionFactory() throws CleverClientException
        {
            if(settings==null)
            {
                log.debug("Settings from default");
                this.configure();
            }
            return new SessionFactoryImpl(settings);
        }
        
        
        //getters and setters
        
         public Settings getSettings() {
            return settings;
         }

        public void setSettings(Settings settings) {
            this.settings = settings;
        }
        
        public void setSettings(Properties properties) throws CleverClientException
        {
            this.settings = settingsFactory.buildSettings(properties);
        }
        
}
