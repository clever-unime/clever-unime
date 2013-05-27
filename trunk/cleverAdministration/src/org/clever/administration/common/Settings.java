package org.clever.administration.common;

public class Settings {
    private CleverCommandClientProvider cleverCommandClientProvider;
    private String sessionFactoryName;
    
  /**
    * Restituisce il SessionFactory per avere le varie Session client (con thread separati);
   */
   public SessionFactory buildSessionFactory() {
       return null;
   };
              
                
    void setSessionFactoryName(String s) {
        sessionFactoryName = s;
    }

    
    /**
     * Imposta l'CleverCommandClientProvider
     * @param xmppconnections 
     */
    void setCleverCommandClientProvider(CleverCommandClientProvider xmppconnections) {
        cleverCommandClientProvider = xmppconnections;
    }
    
    /**
     * Restituisce l'CleverCommandClientProvider
     * @return 
     */
    public CleverCommandClientProvider getCleverCommandClientProvider()
    {
        return this.cleverCommandClientProvider;
    }
}
