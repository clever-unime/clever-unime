/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api;

import java.util.Properties;
import org.apache.log4j.Logger;

/** Implementazione di SessionFactory. 
 * E' stata progettata come interfaccia - implementazione pensando all'uso di strumenti come spring
 *
 * @author maurizio
 */
public class SessionFactoryImpl implements SessionFactory {

    private Settings settings;
    
    private static final Logger log = Logger.getLogger(Configuration.class);
    
    private final ThreadLocal<Session> sessionHolder = new ThreadLocal<Session>() {

        /*
         * initialValue() is called
         */
        @Override
        protected Session initialValue() {
            log.debug("Creating Session for thread : " + Thread.currentThread().getName()); 
            return new Session(settings);
        }
    };


    /**
     * Le properties passate per creare le settings: per consultarle dall'esterno
     * @return 
     */
    public Properties getProperties()
    {
        return settings.getProperties();
    }
    
    
    
    public SessionFactoryImpl(Settings settings) {
        this.settings = settings;
        
    }

    @Override
    public Session getSession() {
       
        return this.sessionHolder.get();
    }

    @Override
    public void closeAllSessions() {
        //TODO: release session resources
        this.settings.getCleverCommandClientProvider().closeAllClients();
    }
    
}
