package org.clever.ClusterManager.ESOSAgent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.Shared.HostEntityInfo;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.jivesoftware.smackx.muc.Occupant;

public class ESOSAgent extends CmAgent
{
    private String version = "0.0.1";
    private String description = "Information about Cluster Manager";
    private ConnectionXMPP connectionXMPP = null;
    private  ArrayList <CmAgent> Agents = new ArrayList(3);
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger=null;
    private String pathLogConf="/sources/org/clever/ClusterManager/ESOSAgent/log_conf/";
    private String pathDirOut="/LOGS/ClusterManager/ESOSAgent";
    //########
    
    
    
    public ESOSAgent() throws CleverException
    {   super();
        
       //#############################################
       //Inizializzazione meccanismo di logging
       logger=Logger.getLogger("ESOSAgent");    
       Log4J log =new Log4J();
       log.setLog4J(logger, pathLogConf, pathDirOut);
       //#############################################
        
    }
    
    @Override
    public void initialization()throws CleverException
    {        
        super.setAgentName("ESOSAgent");
        super.start();
        
        
    }
  
  @Override
  public Class getPluginClass() {
        return org.clever.ClusterManager.ESOSAgent.ESOSAgent.class;
    }

    @Override
    public Object getPlugin() {
        return this;
    }




    public String getVersion() {
        return version;
    }


    public String getDescription() {
        return description;
    }

    public List listObservation(){
        
        ArrayList l2 = new ArrayList();
        logger.debug("Sono stato richiamato");
        return l2;
    }

    public List listClusterManager(){
        Collection <Occupant>  list_HC=connectionXMPP.getCCsInRoom(ConnectionXMPP.ROOM.CLEVER_MAIN);
        Occupant occupant = null;
        ArrayList l2 = new ArrayList();
        HostEntityInfo clusterManager;
        String CCActive = connectionXMPP.getActiveCC(ConnectionXMPP.ROOM.CLEVER_MAIN);

        for (Iterator i = list_HC.iterator(); i.hasNext();) {

            occupant=(Occupant) i.next();
            clusterManager= new HostEntityInfo();
            clusterManager.setNick(occupant.getNick());
            if( CCActive.equals(occupant.getNick()) )
                clusterManager.setActive(true);
            else
                clusterManager.setActive(false);
            l2.add(clusterManager);
        }
        return l2;
        
    }

    

    @Override
    public void handleNotification(Notification notification) throws CleverException {
        logger.debug("Received notification type: "+notification.getId());
    }

    
    @Override
   public void shutDown()
    {
        
    }
    
}