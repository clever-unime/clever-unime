
package org.clever.HostManager.ObjectStorage;

//import java.util.logging.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;

public class ObjectStorageAgent extends Agent {

    private ObjectStoragePlugin objectStorage;
   
    
    public ObjectStorageAgent() throws CleverException  {
        super();
        
      
    }

    @Override
    public void initialization() throws CleverException {
        if (super.getAgentName().equals("NoName")) {
            super.setAgentName("ObjectStorageAgent");
        }
        super.start();
        try 
        {
            
            objectStorage = (ObjectStoragePlugin) super.startPlugin("./cfg/configuration_objectStorage.xml","/org/clever/HostManager/ObjectStorage/configuration_objectStorage.xml");        
            objectStorage.setOwner(this);
            logger.info("ObjectStoragePlugin created ");
            this.setPluginState(true);
        } catch (Exception ex) {
            logger.error("ObjectStoragePlugin creation failed: " + ex.getMessage());
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

    
  
}
