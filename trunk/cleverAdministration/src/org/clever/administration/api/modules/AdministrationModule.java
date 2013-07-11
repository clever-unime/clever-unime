/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.api.modules;

import java.util.ArrayList;
import org.clever.Common.Communicator.InvocationCallback;
import org.clever.Common.Exceptions.CleverException;
import org.clever.administration.api.CleverCommandClient;
import org.clever.administration.api.CleverCommandClientProvider;
import org.clever.administration.api.Session;
import org.clever.administration.api.Settings;
import org.clever.administration.commands.CleverCommand;

/**
 * Contenitore di comandi dotato di identificatore di tipo stringa (per es. "Storage")
 * @author maurizio
 */
public class AdministrationModule {
    protected final Session session;
    final protected ArrayList emptyParams;
    
    public AdministrationModule(Session s)
    {
        this.session = s;
        emptyParams = new ArrayList();
    }
    
   /**
   * Esegue un comando clever generico.  in modalita' sincrona
   * Un semplice wrapper al metodo di CleverComnmandClient
   * @param agent: the entity which executes the command (e.g. ClusterManager)
   * @param command:  the target command
   * @param params:the params of the command
   * @param showXML:It sets if show the XML request/response messages.
   * @throws CleverException
   */
  public Object execSyncCommand( 
                                final String target,
                                final String agent,
                                final String command,
                                final ArrayList params,
                                final boolean showXML ) throws CleverException
  {
      Settings se = session.getSettings();
      CleverCommandClientProvider c = se.getCleverCommandClientProvider();
      CleverCommandClient client = c.getClient();
      //return session.getSettings().getCleverCommandClientProvider().getClient().execSyncAdminCommand(cleverCommand, target, agent, command, params, showXML);
      Object response = client.execSyncAdminCommand(target, agent, command, params, showXML);
      c.releaseClient();
      return response;
  }
  
   /**
   * Esegue un comando clever generico.
   * Un semplice wrapper al metodo di CleverComnmandClient
   * @param agent: the entity which executes the command (e.g. ClusterManager)
   * @param command:  the target command
   * @param params:the params of the command
   * @param showXML:It sets if show the XML request/response messages.
   * @throws CleverException
   */
  public void execASyncCommand( InvocationCallback cleverCommand,
                                final String target,
                                final String agent,
                                final String command,
                                final ArrayList params,
                                final boolean showXML ) throws CleverException
  {
      Settings se = session.getSettings();
      CleverCommandClientProvider c = se.getCleverCommandClientProvider();
      CleverCommandClient client = c.getClient();
      //return session.getSettings().getCleverCommandClientProvider().getClient().execSyncAdminCommand(cleverCommand, target, agent, command, params, showXML);
      client.execAdminCommand(cleverCommand, target, agent, command, params, showXML);
      c.releaseClient();
  }

   
}
