/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.test;

import java.util.List;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Shared.HostEntityInfo;
import org.clever.administration.common.SessionFactory;

/**
 *
 * @author maurizio
 */
public class TestApi extends Thread{

    private SessionFactory sf;
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TestApi.class);
    
    private String host_target;
    
    public TestApi(SessionFactory s, String test_target)
    {
        super();
        sf = s;
        host_target = test_target;
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
            
         /*for (Object o : s.getSession().getVMAdministrationModule().listVMs_HOST(this.host_target, false))
         {
             System.out.println("VM: " + o);
         }*/
     
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
