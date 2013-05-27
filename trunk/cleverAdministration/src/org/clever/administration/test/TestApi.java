/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Shared.HostEntityInfo;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;
import org.clever.administration.commands.CommandCallback;
import org.clever.administration.common.Configuration;
import org.clever.administration.common.SessionFactory;
import org.clever.administration.exceptions.CleverClientException;

/**
 *
 * @author maurizio
 */
public class TestApi extends Thread{

    private SessionFactory sf;
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TestApi.class);
    
    
    public TestApi(SessionFactory s)
    {
        super();
        sf = s;
    }
    
    
    
    private synchronized void printListHost(List<HostEntityInfo> lista)
    {
        System.err.println("\n---------List----------");

                for( int i = 0; i < lista.size(); i++ )
                {
                    HostEntityInfo hostEntityInfo=(HostEntityInfo)lista.get( i );
                    if(hostEntityInfo.isActive())
                        System.out.println( hostEntityInfo.getNick() +" (*)");
                    else
                        System.out.println( hostEntityInfo.getNick());
                    
                }

                System.err.println("\n-------------------------");
    }
    
    
  private void testAPI(SessionFactory s) throws CleverException
  {
      
    
      
      
      
         this.printListHost(s.getSession().getHostAdministrationModule().listHostManagers()); 
         /*s.getSession().getHostAdministrationModule().asyncListHostManagers(new CommandCallback() 
                        {
                     

                                @Override
                                public void handleMessage(Object response) {
                                    printListHost(( List<HostEntityInfo> )response);
                                }

                                @Override
                                public void handleMessageError(CleverException e) {
                                    System.out.println(e);
                                    e.printStackTrace();
                                }
                         }); 
          
          */
          this.printListHost(s.getSession().getHostAdministrationModule().listClusterManagers()); 
            
         
     
  }
    
    
    
    @Override
    public void run() {
        try {
            testAPI(sf);
        } catch (CleverException ex) {
            log.error("error in test");
            ex.printStackTrace();
        }
    }
    
}
