package org.clever.administration.common;

import java.util.Properties;
import org.clever.administration.exceptions.CleverClientException;

/**
 * Legge le configurazioni da file properties e restituisce oggetti settings
 * @author maurizio
 *
 */
public class SettingsFactory {
	
    
             
        /**
         * Crea l'oggetto settings 
         * Usato Dalla classe Configuration per creare il settings all'interno del metodo configure()
         * @param props
         * @return 
         */
	public Settings buildSettings(Properties props) throws CleverClientException {
      		Settings settings = new Settings();
      
   
      
      		String sessionFactoryName = props.getProperty(Environment.SESSION_FACTORY_NAME); //per ora non utilizzata
                
      		settings.setSessionFactoryName(sessionFactoryName);
      
      		//Configurazione XMPP
      
      		XMPPProvider xmppconnections = createXMPPProvider(props);
      		settings.setXMPPProvider(xmppconnections);
                
                
                //qui andranno altre configurazioni relative, per es. al client vero e proprio, tipo numero di tentativi delle richieste, timeout, ecc.
                
                return settings;
        }	
	/**
	 * CreateXMPPProvider. Crea un oggetto per ottenere una connessione XMPP.
	 * Usato per implementare  diverse strategie di ottenimento delle connessioni (pooling, per es.) 
         * Nelle properties passo servername, password,ecc.
         * Per ora implementa SimpleXMPPProvider
	 */
	
	public XMPPProvider createXMPPProvider(Properties properties) throws CleverClientException{
            //TODO: creare altri XMPPPROvider a seconda dei properties
      
            return XMPPProviderFactory.newXMPPProvider(properties);
        }
        
        
        
}
	
	
	

