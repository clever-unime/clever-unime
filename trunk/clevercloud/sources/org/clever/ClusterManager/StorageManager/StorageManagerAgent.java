package org.clever.ClusterManager.StorageManager;

import java.io.InputStream;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;

/*
 * @author FValerio Barbera & Luca Ciarniello
 */
public class StorageManagerAgent extends CmAgent {
    
    private Class cl;
    private StorageManagerPlugin StoragePlugin;
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger=null;
    private String pathLogConf="/sources/org/clever/ClusterManager/StorageManager/log_conf/";
    private String pathDirOut="/LOGS/ClusterManager/StorageManager";
    //########

    public StorageManagerAgent() {
       /* logger = Logger.getLogger("StorageManagerAgent");
        //Load properties from XML file
        try {
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream("/org/clever/Common/Shared/logger.properties");
            prop.load(in);
            PropertyConfigurator.configure(prop);
        } catch (IOException e) {
            logger.error("Missing logger.properties");
        }*/
       super();
       
       //#############################################
       //Inizializzazione meccanismo di logging
       logger=Logger.getLogger("StorageManager");    
       Log4J log =new Log4J();
       log.setLog4J(logger, pathLogConf, pathDirOut);
       //#############################################  
        
        
    }

    @Override
    public void initialization() throws CleverException {
        if (super.getAgentName().equals("NoName")) {
            super.setAgentName("StorageManagerAgent");
        }

        super.start();

        try {
            logger.info("Read Configuration StorageManager!");
            InputStream inxml = getClass().getResourceAsStream("/org/clever/ClusterManager/StorageManager/configuration_StorageManager.xml");
            FileStreamer fs = new FileStreamer();
            ParserXML pars = new ParserXML(fs.xmlToString(inxml));

            //Instantiate ModulCommunicator //the module communicator is istantiated in superclass Agent into function start()!
            //this.mc = new ModuleCommunicator(pars.getElementContent("moduleName"));

            //Instantiate StorageManager
            this.cl = Class.forName(pars.getElementContent("StorageManagerPlugin"));
            this.StoragePlugin = (StorageManagerPlugin) this.cl.newInstance();
            this.StoragePlugin.setModuleCommunicator(mc);
           // this.mc.setMethodInvokerHandler(this);
            this.StoragePlugin.setOwner(this);
            
            
            
            logger.info("StorageManager Plugin instantiated !");
        } catch (Exception e) {
            logger.error("Error initializing StorageManager : " + e.getMessage());
        }
    }

    @Override
    public Class getPluginClass() {
        return cl;
    }

    @Override
    public Object getPlugin() {
        return StoragePlugin;
    }

    @Override
    public void handleNotification(Notification notification) throws CleverException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void shutDown() {
    }
}