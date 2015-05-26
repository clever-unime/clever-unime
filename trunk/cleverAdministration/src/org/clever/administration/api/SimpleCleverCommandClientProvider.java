/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.openam.OpenAmSessionClient;

/**
 *
 * @author maurizio
 */
class SimpleCleverCommandClientProvider extends CleverCommandClientProviderImpl {
  
    
     private CleverCommandClient client = null;
     /***
      * Added for OpenAM
      */
     private OpenAmSessionClient mOpenAmClient = null;
     //private Properties mProperties;
    
   
    
    protected static final Logger log = Logger.getLogger(SimpleCleverCommandClientProvider.class);
    
    
    
    public SimpleCleverCommandClientProvider() {
        super();
    }
    
    //costruttore a cui si passano delle properties con i parametri di configurazione (servername, password,ecc.)
    public SimpleCleverCommandClientProvider(Properties properties) {
       super(properties);
       // mProperties = properties;
        
        
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
        
        /***
         * Added for OpenAM
        */
        if(mOpenAmClient == null){
            mOpenAmClient = OpenAmSessionClient.getInstance();//mProperties);
            mOpenAmClient.authenticate(username, password);
        }
        
        if(client==null)
        {
            client = new CleverCommandClient(this.maxMessages,this.maxHandlers);
        }
        
        if(client.isActive())
        {
            return client;
        }
        
        //mi connetto
        
        client.connect(servername, username, password, port, room, nickname, mMode);
        //TODO: lanciare eccezione se la connessione non e' andata a buon fine
        //controllo se la connessione e' andata a buon fine
        //if(admintools.isActive())
        //{
        //    log.error("XMPP connection error on server: " + servername + " with username " + username);
        //    return null;
        //}
        return client;
        
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
        if(client!=null)
        {
            client.close();
        }
    }

    
}
