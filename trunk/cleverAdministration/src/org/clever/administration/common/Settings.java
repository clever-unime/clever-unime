package org.clever.administration.common;

public class Settings {
    private CleverCommandClientProvider xmppProvider;
  /**
    * Restituisce il SessionFactory per avere le varie Session client (con thread separati);
   */
   public SessionFactory buildSessionFactory() {
       return null;
   };
              
                
    void setSessionFactoryName(String sessionFactoryName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    /**
     * Imposta l'XMPPProvider
     * @param xmppconnections 
     */
    void setXMPPProvider(CleverCommandClientProvider xmppconnections) {
        xmppProvider = xmppconnections;
    }
    
    /**
     * Restituisce l'XMPPProvider
     * @return 
     */
    CleverCommandClientProvider getXmppProvider()
    {
        return this.xmppProvider;
    }
}
