/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api;

import org.apache.log4j.Logger;

/**
 *
 * @author maurizio
 */
class PerThreadCleverCommandClientProvider extends CleverCommandClientProviderImpl {
    
    
    
    
     private final ThreadLocal<CleverCommandClient> clientHolder = new ThreadLocal<CleverCommandClient>() {

        /*
         * initialValue() is called
         */
        @Override
        protected CleverCommandClient initialValue() {
            log.debug("Creating CleverCommandClient for thread : " + Thread.currentThread().getName()); 
            return new CleverCommandClient(PerThreadCleverCommandClientProvider.this.maxMessages, 
                                           PerThreadCleverCommandClientProvider.this.maxHandlers);
        }
    };
    
    
    
    //private final CleverCommandClient admintools;
    
    private static final Logger log = Logger.getLogger(PerThreadCleverCommandClientProvider.class);
    
    
    
   

    
    /**
     * Semplicissima politica: crea una connessione per ogni thread
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
    public void closeAllClients() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    
}
