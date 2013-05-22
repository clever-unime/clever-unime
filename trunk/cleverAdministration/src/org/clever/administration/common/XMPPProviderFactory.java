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
class XMPPProviderFactory {

    
    private static final Logger log = Logger.getLogger(XMPPProviderFactory.class);
    
    static XMPPProvider newXMPPProvider(Properties properties) throws CleverClientException {
        XMPPProvider connections;
   	String providerClass = properties.getProperty(Environment.CONNECTION_PROVIDER);
   	if ( providerClass==null ) {
            providerClass = Environment.CONNECTION_PROVIDER_DEFAULT; //imposto il provider di default
        }
        try {
                 log.info("Initializing XMPP connection provider: " + providerClass);
                connections = (XMPPProvider) Class.forName(providerClass).newInstance();
        }
        catch ( Exception e ) {
                log.error( "Could not instantiate XMPP connection provider", e );
                throw new CleverClientException("Could not instantiate XMPP connection provider: " + providerClass);
        }
        connections.configure(properties);
        return connections;
    }
        
 }
    
