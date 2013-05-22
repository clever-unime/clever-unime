package org.clever.administration.common;

public class Settings {
    private XMPPProvider xmppProvider;
  /**
    * Restituisce il SessionFactory per avere le varie Session client (con thread separati);
   */
   public SessionFactory buildSessionFactory() {
       return null;
   };
              
                
    void setSessionFactoryName(String sessionFactoryName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void setXMPPProvider(XMPPProvider xmppconnections) {
        xmppProvider = xmppconnections;
    }
    
    XMPPProvider getXmppProvider()
    {
        return this.xmppProvider;
    }
}
