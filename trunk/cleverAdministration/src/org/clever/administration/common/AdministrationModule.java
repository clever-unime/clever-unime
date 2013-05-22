/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.common;

/**
 * Contenitore di comandi dotato di identificatore di tipo stringa (per es. "Storage")
 * @author maurizio
 */
class AdministrationModule {
    private final Session session;
    public AdministrationModule(Session s)
    {
        this.session = s;
    }

   
}
