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
class SimpleCleverCommandClientProvider implements CleverCommandClientProvider {
    
    private  String username;
    private  String password;
    private  String servername;
    private  Integer port;
    private  String nickname;
    private  String room;
    //private final ConnectionXMPP conn;
    private final CleverCommandClient admintools;
    
    private static final Logger log = Logger.getLogger(SimpleCleverCommandClientProvider.class);
    
    
    //costruttore a cui si passano delle properties con i parametri di configurazione (servername, password,ecc.)
    public SimpleCleverCommandClientProvider(Properties properties) {
        //TODO: validate the properties
        username = properties.getProperty(Environment.XMPP_USERNAME);
        password = properties.getProperty(Environment.XMPP_PASSWORD);
        servername = properties.getProperty(Environment.XMPP_SERVER);
        port = Integer.parseInt(properties.getProperty(Environment.XMPP_PORT));
        room = properties.getProperty(Environment.XMPP_ROOM);
        nickname = properties.getProperty(Environment.XMPP_NICKNAME);
        //conn = new ConnectionXMPP();
        admintools = new CleverCommandClient();
        
    }

    
    /**
     * Semplicissima politica: si connette alla prima invocazione 
     * e mantiene una sola connessione.
     * 
     * @return
     * Il client se va a buon fine o null in caso di errore
     */
    
    
    @Override
    public CleverCommandClient getClient() {
        if(admintools.isActive())
        {
            return admintools;
        }
        
        //mi connetto
        admintools.connect(servername, username, password, port, room, nickname);
        //controllo se la connessione e' andata a buon fine
        if(admintools.isActive())
        {
            log.error("XMPP connection error on server: " + servername + " with username " + username);
            return null;
        }
        return admintools;
    }
    
    
    /**
     * Chiamato per rilasciare il Client. In questa implementazione non fa nulla
     */
    @Override
    public void releaseClient() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void configure(Properties properties) {
        //TODO: validate the properties
        username = properties.getProperty(Environment.XMPP_USERNAME);
        password = properties.getProperty(Environment.XMPP_PASSWORD);
        servername = properties.getProperty(Environment.XMPP_SERVER);
        port = Integer.parseInt(properties.getProperty(Environment.XMPP_PORT));
        room = properties.getProperty(Environment.XMPP_ROOM);
        nickname = properties.getProperty(Environment.XMPP_NICKNAME);
    }
    
}
