/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api;

import org.clever.administration.exceptions.CleverClientException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Classe di comodo (si puo' usare per adottare un pattern Factory: dubbio se utile):
 * prende delle Properties , tra le quali c'e' la classe del Provider dei comandi da utilizzare: se assente ne usa uno di default
 * @author maurizio
 */
class CleverCommandClientProviderFactory {

    
    private static final Logger log = Logger.getLogger(CleverCommandClientProviderFactory.class);
    
    static CleverCommandClientProvider newCleverCommandClientProvider(Properties properties) throws CleverClientException {
        CleverCommandClientProvider clients;
   	String providerClass = properties.getProperty(Environment.COMMAND_PROVIDER);
   	if ( providerClass==null ) {
            log.info("No providerClass ... using default class : " + Environment.COMMAND_PROVIDER_DEFAULT); 
            providerClass = Environment.COMMAND_PROVIDER_DEFAULT; //imposto il provider di default
        }
        try {
                 log.info("Initializing Clever command client  provider: " + providerClass);
                clients = (CleverCommandClientProvider) Class.forName(providerClass).newInstance();
        }
        catch ( Exception e ) {
                log.error( "Could not instantiate  Clever command client", e );
                throw new CleverClientException(e, "Could not instantiate  Clever command client: " + providerClass);
        }
        clients.configure(properties);
        return clients;
    }
        
 }
    
