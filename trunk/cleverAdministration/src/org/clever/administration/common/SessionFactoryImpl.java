/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.common;

/**Implementazione di SessionFactory. 
 * E' stata progettata come interfaccia - implementazione pensando all'uso di strumenti come spring
 *
 * @author maurizio
 */
public class SessionFactoryImpl implements SessionFactory {

    private Settings settings;
    
    
    
    
    public SessionFactoryImpl(Settings settings) {
        this.settings = settings;
        
    }

    @Override
    public Session getSession() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
