/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Shared.HostEntityInfo;
import org.clever.Common.VEInfo.StorageSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.VEInfo.VEState;
import org.clever.Common.XMPPCommunicator.UUIDProvider;

import org.clever.administration.api.SessionFactory;
import org.clever.administration.api.modules.VMAdministrationModule;

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
          
        
           String img = "cirros-0.3.0-x86_64-uec";
         // String img = "cirros-0-3-0"; //unime openstacks
          //String img = "631ba9f5-3ea6-4a06-b36c-ec30770a61aa"; //sito spagnolo openstack 
          //String img = "ttylinux"; //sito infn nebula e es
          String template = "m1.small";
          //String template = "small"; //nebula es
          //String template = "m1-small"; //unime openstack

        
        
        
//         System.out.println("Templates:");
//         for (String t : vmm.listTemplates_HOST(host_target))
//         {
//             System.out.println(t);
//         }
//        
//        
//         System.out.println("Images:");
//         for (String t : vmm.listImageTemplates_HOST(host_target))
//         {
//             System.out.println(t);
//         }
        
          List<String> running = new ArrayList<String>();
          List<String> stopped = new ArrayList<String>();
          List<VEState> vms = s.getSession().getVMAdministrationModule().listVMs_HOST(host_target, false);
        
          for (VEState stato : vms)
          {
              System.out.println(stato);
              switch(stato.getState())
              {
                  case RUNNING:
                      running.add(stato.getName());
                      break;
                  case STOPPED:
                      stopped.add(stato.getName());
                      
                              
              }
             Map<String,Object> details = vmm.getVMDetails_HOST(host_target, stato.getName());
             List<Map<String,String>> net = ((List<Map<String,String>>)details.get("network"));
             for(Map<String,String> i : net)
             {
                 
             
              System.out.println("IP: " + i.get("ip"));
         
              System.out.println("MAC: " + i.get("mac"));
              System.out.println("STATE: " + i.get("state"));
             }
              System.out.println("DISPLAY: " + details.get("display"));
              System.out.println("MEMORY: " + details.get("memory"));
              System.out.println("CORES: " + details.get("cores"));
             
          }
          
          
          
          List<StorageSettings> st = new ArrayList<StorageSettings>();
      
          
          st.add(new StorageSettings(0, null, null, "C", img));
          VEDescription ved = new VEDescription( st, null, template, null, null, null);
         
            /*if(s.getSession().getVMAdministrationModule().createVM_HOST(host_target, "VMPLUGIN-" + UUIDProvider.getPositiveInteger(), ved, Boolean.TRUE))
            {
                System.out.println("Creazione effettuata");
            }
          */
          
          
          

//        for(VEState stato : vms)
//        {
//            
//          String vm = stato.getName();
//          if(vm==null)
//              continue;
//          if(vmm.isRunningVM_HOST(host_target, vm))
//                  {
//                      try{
//                              System.out.println("Esito stop VM: " + s.getSession().getVMAdministrationModule().stopVM_HOST(host_target, vm, false));
//                      }
//                      catch (CleverException e)
//                      {
//                          e.printStackTrace();
//                      }
//                  }
//          else
//          {
//              try{
//                       System.out.println("Esito start VM: " + s.getSession().getVMAdministrationModule().startVM_HOST(host_target, vm));
//                  }
//              catch (CleverException e)
//              {
//                  e.printStackTrace();
//              }
//          }
//        }
//        
          
          /*
         try
          {
	      //vmm.stopVM_HOST(host_target, "vmplugin-638497822", false);
              if(vmm.destroyVM_HOST(host_target, "vmplugin-1505692512"))
                  System.out.println("vmplugin-1505692512 cancellata");
              
          }
          catch(CleverException e)
          {
              e.printStackTrace();
          }
  */
          
          
          
            //System.out.println("Stop le macchine attive: " + vmm.stopVMs_HOST(host_target, running.toArray(new String[running.size()]), Boolean.TRUE));
            //System.out.println("start le macchine ferme: " + vmm.startVMs_HOST(host_target, stopped.toArray(new String[stopped.size()])));
          
          
          
          
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
