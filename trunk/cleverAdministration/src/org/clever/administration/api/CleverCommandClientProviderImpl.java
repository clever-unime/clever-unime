/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api;

import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author maurizio
 */
public abstract class CleverCommandClientProviderImpl implements CleverCommandClientProvider{

    
    protected static final Logger log = Logger.getLogger(CleverCommandClientProviderImpl.class);
    
    protected  String username;
    protected  String password;
    protected  String servername;
    protected  Integer port;
    protected  String nickname;
    protected  String room;
    protected int maxMessages;
    protected int maxHandlers;

    
    public CleverCommandClientProviderImpl() {
        super();
    }
    
    //costruttore a cui si passano delle properties con i parametri di configurazione (servername, password,ecc.)
    public CleverCommandClientProviderImpl(Properties properties) {
        this();
        //TODO: validate the properties
       this.configure(properties);
        
        
        
    }
    
    
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
        
        if(properties.getProperty(Environment.MAX_LENGHT_MESSAGES_QUEUE)==null)
        {
            int m = Environment.MAX_LENGHT_MESSAGES_QUEUE_DEFAULT;
            log.info("Set max number of clever messages in queue to default:" + m);
            this.maxMessages = m;
        }
        if(properties.getProperty(Environment.MAX_NUMBER_MESSAGE_HANDLERS)==null)
        {
            int m = Environment.MAX_NUMBER_MESSAGE_HANDLERS_DEFAULT;
            log.info("Set max number of clever messages handlers to default:" + m);
            this.maxHandlers = m;
        }
    }
    
}
