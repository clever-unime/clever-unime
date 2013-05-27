/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.common;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;

/**
 *
 * @author maurizio
 */
class PerThreadCleverCommandClientProvider implements CleverCommandClientProvider {
    
    private  String username;
    private  String password;
    private  String servername;
    private  Integer port;
    private  String nickname;
    private  String room;
    
    
     private final ThreadLocal<CleverCommandClient> clientHolder = new ThreadLocal<CleverCommandClient>() {

        /*
         * initialValue() is called
         */
        @Override
        protected CleverCommandClient initialValue() {
            log.debug("Creating CleverCommandClient for thread : " + Thread.currentThread().getName()); 
            return new CleverCommandClient();
        }
    };
    
    
    
    //private final CleverCommandClient admintools;
    
    private static final Logger log = Logger.getLogger(PerThreadCleverCommandClientProvider.class);
    
    
    
    public PerThreadCleverCommandClientProvider() {
        //admintools = new CleverCommandClient();
    }
    
    //costruttore a cui si passano delle properties con i parametri di configurazione (servername, password,ecc.)
    public PerThreadCleverCommandClientProvider(Properties properties) {
        this();
        //TODO: validate the properties
       this.configure(properties);
        
        //admintools = new CleverCommandClient();
        
    }

    
    /**
     * Semplicissima politica: si connette alla prima invocazione 
     * e mantiene una sola connessione.
     * 
     * @return
     * Il client se va a buon fine o null in caso di errore
     */
    
    
    @Override
    public synchronized CleverCommandClient getClient() {
        //cosi' funzionicchia controllare il cleverhandlemessage
        
        CleverCommandClient ccc = clientHolder.get();
        
        
        if(ccc.isActive())
        {
            return ccc;
        }
        
        //mi connetto
        
        ccc.connect(servername, username, password, port, room, nickname);
        //controllo se la connessione e' andata a buon fine
        //if(admintools.isActive())
        //{
        //    log.error("XMPP connection error on server: " + servername + " with username " + username);
        //    return null;
        //}
        return ccc;
        
    }
    
    
    /**
     * Chiamato per rilasciare il Client. In questa implementazione non fa nulla
     */
    @Override
    public synchronized void releaseClient() {
        //TODO: manage connection
        return;
    }

    @Override
    final synchronized public void configure(Properties properties) {
        //TODO: validate the properties
        username = properties.getProperty(Environment.XMPP_USERNAME);
        password = properties.getProperty(Environment.XMPP_PASSWORD);
        servername = properties.getProperty(Environment.XMPP_SERVER);
        port = Integer.parseInt(properties.getProperty(Environment.XMPP_PORT));
        room = properties.getProperty(Environment.XMPP_ROOM);
        nickname = properties.getProperty(Environment.XMPP_NICKNAME);
    }
    
}
