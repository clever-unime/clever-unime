/*
 * Copyright [2014] [Università di Messina]
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
package org.clever.HostManager.ImageManagerPlugins.ImageManagerClever;

import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;

/*
 * @author Valerio Barbera
 */

public class StoragePluginFactory {
    //Istanze per il Singleton
    private static StoragePluginFactory instance;
    private DistributedStoragePlugin distributedStorage;
    private Logger logger;
    private Class cl;

    //  ------COSTRUTORE------
    private StoragePluginFactory() {
        logger = Logger.getLogger("StoragePluginFactory");

        try
        {
            //Load configuration_imagmanager.XMl
            InputStream inxml=getClass().getResourceAsStream("/org/clever/Common/Shared/configuration_imagemanager.xml");
            FileStreamer fs = new FileStreamer();
            ParserXML pars = new ParserXML(fs.xmlToString(inxml));

            //Instantiate DistributedStoragePlugin
            //Read correct name to XML file configuration
            cl = Class.forName(pars.getElementContent("PluginName1"));
            distributedStorage = (DistributedStoragePlugin) cl.newInstance();
            logger.info("DistributedStoragePlugin created!");

        } catch (IOException io_ex) {
            logger.error("Error: " + io_ex);
        }  catch (Exception ex) {
            logger.error("DistributedStoragePlugin creation failed: " + ex);
        }
    }

    public static StoragePluginFactory getInstance() {
        return instance == null ? new StoragePluginFactory() : instance;
    }

    public DistributedStoragePlugin getDistributedStoragePlugin() {
        return distributedStorage;
    }
}