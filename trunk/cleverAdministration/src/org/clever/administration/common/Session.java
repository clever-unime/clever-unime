package org.clever.administration.common;


/**
 * Classe che permette di invocare le API
 * @author maurizio
 *
 */
public class Session {
    //per ora unico modulo
    AdministrationModule module;
    
    //per ora faccio un meccanismo molto spartano non considerando il modulo passato
    public Session()
    {
        module = new AdministrationModule(this);
    }
    
    /**
     * Ritorna l'administrationmodule per invocare le funzionalita' 
     * per ora ritorna un solo modulo ignorando il nome passato. Si potra' invocare un metodo generico di un agente di un'entita' (CM o HM), passandogli i parametri
     * @param moduleName
     * @return 
     */
    AdministrationModule getModule(String moduleName)
    {
        return module;
        
    }
}
