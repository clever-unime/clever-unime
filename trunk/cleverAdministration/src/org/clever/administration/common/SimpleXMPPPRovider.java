/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.common;

import java.util.Properties;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;

/**
 *
 * @author maurizio
 */
class SimpleXMPPPRovider implements XMPPProvider {
    
    private final String username;
    private final String password;
    private final String servername;
    private final String port;
    private final String nickname;
    private final String room;
    private final ConnectionXMPP conn;
    
    //costruttore a cui si passano delle properties con i parametri di configurazione (servername, password,ecc.)
    public SimpleXMPPPRovider(Properties properties) {
        //TODO: validate the properties
        username = properties.getProperty(Environment.XMPP_USERNAME);
        password = properties.getProperty(Environment.XMPP_PASSWORD);
        servername = properties.getProperty(Environment.XMPP_SERVER);
        port = properties.getProperty(Environment.XMPP_PORT);
        room = properties.getProperty(Environment.XMPP_ROOM);
        nickname = properties.getProperty(Environment.XMPP_NICKNAME);
        conn = new ConnectionXMPP();
        
    }

    @Override
    public ConnectionXMPP getConnection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeConnection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void configure(Properties p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
