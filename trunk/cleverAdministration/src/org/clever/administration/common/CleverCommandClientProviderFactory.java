/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.common;

import org.clever.administration.exceptions.CleverClientException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author maurizio
 */
class CleverCommandClientProviderFactory {

    
    private static final Logger log = Logger.getLogger(CleverCommandClientProviderFactory.class);
    
    static CleverCommandClientProvider newCleverCommandClientProvider(Properties properties) throws CleverClientException {
        CleverCommandClientProvider clients;
   	String providerClass = properties.getProperty(Environment.CONNECTION_PROVIDER);
   	if ( providerClass==null ) {
            providerClass = Environment.CONNECTION_PROVIDER_DEFAULT; //imposto il provider di default
        }
        try {
                 log.info("Initializing Clever command client  provider: " + providerClass);
                clients = (CleverCommandClientProvider) Class.forName(providerClass).newInstance();
        }
        catch ( Exception e ) {
                log.error( "Could not instantiate  Clever command client", e );
                throw new CleverClientException("Could not instantiate  Clever command client: " + providerClass);
        }
        clients.configure(properties);
        return clients;
    }
        
 }
    
