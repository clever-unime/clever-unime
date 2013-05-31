package org.clever.administration.api;

import java.util.Properties;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;

/**
 * Interfaccia per un provider di oggetti CleverCommandClient (ognuno di questi ha un ConnectionXmpp)
 * Per ottenere un riferimento a un CleverCommandClient si usa getClient: a seconda
 * della politica della particolare implementazione viene fornito un client per la gestione dei comandi clever
 * (per es. possibilita' di pooling)
 * Per ora esiste solo un'implementazione: SimpleXMPPProvider 
 * @author maurizio
 */
public interface CleverCommandClientProvider {
	
    
    
	public CleverCommandClient getClient();
	public void releaseClient();
	
        
        
        
	/**
	 * inizializza il provider con la configurazione XMPP (server name , password, ecc.)
         * e , eventualmente , parametri per i comandi (timeout, retry, ecc.)
	 * @param p
	 */
	public void configure(Properties p); 
	
}
