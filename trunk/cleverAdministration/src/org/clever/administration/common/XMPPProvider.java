package org.clever.administration.common;

import java.util.Properties;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;

/**
 * Interfaccia per un provider di oggetti ConnectionXMPP
 * Per ora esiste solo un'implementazione: SimpleXMPPProvider 
 * @author maurizio
 */
public interface XMPPProvider {
	
    
    
	public ConnectionXMPP getConnection();
	public void closeConnection();
	
        
        
        
	/**
	 * inizializza il provider con la configurazione XMPP (server name , password, ecc.)
	 * @param p
	 */
	public void configure(Properties p); 
	
}
