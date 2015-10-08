package org.clever.ClusterManager.IdentityService;

//import java.util.logging.Logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;

public class IdentityServiceAgent extends CmAgent {

    private IdentityServicePlugin identityService;
    //private Class cl;
   
    
    public IdentityServiceAgent() throws CleverException  {
        super();
        
      
    }

    @Override
    public void initialization() throws CleverException {
        if (super.getAgentName().equals("NoName")) {
            super.setAgentName("IdentityServiceAgent");
        }
        super.start();
        try 
        {
            
            identityService = (IdentityServicePlugin) super.startPlugin("./cfg/configuration_identityService.xml","/org/clever/ClusterManager/IdentityService/configuration_identityService.xml");        
            identityService.setOwner(this);
            logger.info("IdentityServicePlugin created ");
            this.setPluginState(true);
        } catch (Exception ex) {
            logger.error("IdentityServicePlugin creation failed: " + ex.getMessage());
            this.errorStr=ex.getMessage();
        }
    }

    @Override
    public Class getPluginClass() {
        return cl;
    }

    @Override
    public Object getPlugin() {
         
        return this.pluginInstantiation;
    }

    @Override
    public void shutDown() {
    }

    @Override
    public void handleNotification(Notification notification) throws CleverException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
  
}
