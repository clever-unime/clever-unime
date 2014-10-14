/*
 * Copyright 2014 Università di Messina
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
package org.clever.ClusterManager.StorageManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;

/*
 * @author Valerio Barbera & Luca Ciarniello
 */
public class StorageManagerAgent extends CmAgent {
    private Logger loger;
    //private Class cl;
    private StorageManagerPlugin StoragePlugin;

    public StorageManagerAgent() throws CleverException {
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
       
    }

    @Override
    public void initialization() throws CleverException {
        if (super.getAgentName().equals("NoName")) {
            super.setAgentName("StorageManagerAgent");
        }

        super.start();

        try {
            logger.info("Read Configuration StorageManager!");
            this.StoragePlugin = (StorageManagerPlugin) super.startPlugin("./cfg/configuration_StorageManager.xml","/org/clever/ClusterManager/StorageManager/configuration_StorageManager.xml");
            
            
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
        return this.pluginInstantiation;
    }

    @Override
    public void handleNotification(Notification notification) throws CleverException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void shutDown() {
    }
}