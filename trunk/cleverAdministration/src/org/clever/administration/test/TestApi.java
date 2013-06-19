/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.test;

import java.util.ArrayList;
import java.util.List;
import org.clever.Common.Communicator.InvocationCallback;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Shared.HostEntityInfo;
import org.clever.Common.VEInfo.StorageSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.VEInfo.VEState;
import org.clever.Common.XMPPCommunicator.UUIDProvider;

import org.clever.administration.api.SessionFactory;
import org.clever.administration.api.VMAdministrationModule;

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
      
    
        VMAdministrationModule vmm = s.getSession().getVMAdministrationModule();
      
      
//         this.printListHost(s.getSession().getHostAdministrationModule().listHostManagers()); 
//         s.getSession().getHostAdministrationModule().asyncListHostManagers(new InvocationCallback() 
//                        {
//                     
//
//                                @Override
//                                public void handleMessage(Object response) {
//                                    printListHost(( List<HostEntityInfo> )response);
//                                }
//
//                                @Override
//                                public void handleMessageError(CleverException e) {
//                                    System.out.println(e);
//                                    e.printStackTrace();
//                                }
//                         }); 
//          
//          
//          this.printListHost(s.getSession().getHostAdministrationModule().listClusterManagers()); 
//          
//          
//      
//          
//          
//          List<String> response = s.getSession().getHostAdministrationModule().listActiveAgents(host_target);
//          for (String agent : response)
//          {
//              System.out.println("Agente: " + agent);
//              System.out.println("Plugin: " + s.getSession().getHostAdministrationModule().getPluginName(host_target, agent));
//          }
          
        
          List<VEState> vms = s.getSession().getVMAdministrationModule().listVMs_HOST(host_target, false);
        
          for (VEState stato : vms)
          {
              System.out.println(stato);
          }
          
          
          
          List<StorageSettings> st = new ArrayList<StorageSettings>();
//          String img = "cirros-0.3.0-x86_64-uec";
//          String template = "m1.tiny";
          
          
          String img = "cirros-0-3-0";
          String template = "m1-tiny";
          
          
          st.add(new StorageSettings(0, null, null, "C", img));
          VEDescription ved = new VEDescription( st, null, template, null, null, null);
         
            if(s.getSession().getVMAdministrationModule().createVM_HOST(host_target, "VM-" + UUIDProvider.getPositiveInteger(), ved, Boolean.TRUE))
            {
                System.out.println("Creazione effettuata");
            }
          
          
          
          
//          String vmstop = "vm-1253730391";
        for(VEState stato : vms)
        {
            
          String vmstop = stato.getName();
          if(vmstop==null)
              continue;
          if(!vmm.isRunningVM_HOST(host_target, vmstop))
                  {
                      try{
                              System.out.println("Esito stop VM: " + s.getSession().getVMAdministrationModule().startVM_HOST(host_target, vmstop));
                      }
                      catch (CleverException e)
                      {
                          e.printStackTrace();
                      }
                  }
          else
          {
              System.out.println("VM "+ vmstop + " already started");
          }
        }
//          
//          try
//          {
//              if(vmm.destroyVM_HOST(host_target, "from-curl"))
//                  System.out.println("from-curl cancellata");
//              
//          }
//          catch(CleverException e)
//          {
//              e.printStackTrace();
//          }
          
          
          
          
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
