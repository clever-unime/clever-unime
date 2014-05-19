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

/*
 *  Copyright (c) 2010 Antonino Longo
 *  Copyright (c) 2012 Marco Carbone
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */
package org.clever.ClusterManager.DatabaseManager;

import java.io.IOException;
import org.clever.Common.Communicator.Notification;
import org.apache.log4j.*;
import java.util.ArrayList;
import java.util.List;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Exceptions.CleverException;
//import java.io.File;
//import java.io.FileInputStream;
//import org.clever.Common.XMLTools.FileStreamer;
//import org.clever.Common.XMLTools.ParserXML;
//import java.util.Properties;
//import java.io.InputStream;
//import org.clever.Common.Communicator.MethodInvoker;




public class DatabaseManagerAgent extends CmAgent
{
    private DatabaseManagerPlugin DbManagerPlugin;

    //private String configPath="./cfg/configuration_dbManagerPlugin.xml";

    public DatabaseManagerAgent() throws CleverException 

    {
            super();
              
    }
    
    
    @Override
    public void initialization() throws CleverException, IOException
    {
        if(super.getAgentName().equals("NoName"))
            super.setAgentName("DatabaseManagerAgent");
        super.start();
        try
        {

            this.startPlugin();

            
            List params = new ArrayList();
            params.add(super.getAgentName());
            params.add("PRESENCE");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);

        }

        catch( java.lang.NullPointerException e )
        { 
            throw new CleverException( e, "Missing logger.properties or configuration not found" );       
        }
        catch( Exception e )
        {
            throw new CleverException( e );
        }
    }
    
    @Override
    public Class getPluginClass()
    {
        return cl;
    }
    
    @Override
    public Object getPlugin()
    {
        return this.pluginInstantiation;
    }
    
    @Override
    public void handleNotification(Notification notification) throws CleverException {
        if(notification.getId().equals("PRESENCE")){            
            logger.debug("Received notification type "+notification.getId());
            if(!DbManagerPlugin.checkHm(notification.getHostId())){
                DbManagerPlugin.addHm(notification.getHostId());
            }
        }
    }
   
    public void startPlugin()throws CleverException, IOException{
        try
        {
         //   Properties prop = new Properties();
         //   InputStream in = getClass().getResourceAsStream( "/org/clever/Common/Shared/logger.properties" );
         //   prop.load( in );
         //   PropertyConfigurator.configure( prop );
            
            DbManagerPlugin = ( DatabaseManagerPlugin )super.startPlugin("./cfg/configuration_dbManagerPlugin.xml","/org/clever/ClusterManager/DatabaseManager/configuration_dbManagerPlugin.xml");
            /*InputStream inxml = getClass().getResourceAsStream( "/org/clever/ClusterManager/DatabaseManager/configuration_dbManagerPlugin.xml" );
            ParserXML pXML = new ParserXML( fs.xmlToString( inxml ) );
            
            cl = Class.forName( pXML.getElementContent( "DbManagerPlugin" ) );
             cl.newInstance();
            
            logger.debug( "called init of " + pXML.getElementContent( "DbManagerPlugin" ) );
            
            DbManagerPlugin.init( pXML.getRootElement().getChild( "pluginParams" ),this );
            
            //agentName=pXML.getElementContent( "moduleName" );
            DbManagerPlugin.setOwner(this);*/
           this.DbManagerPlugin.setOwner(this);
            logger.info( "DbManagerPlugin created " );
            
        }
        catch( java.lang.NullPointerException e )
        { 
            throw new CleverException( e, "Missing logger.properties or configuration not found" );       
        }
        catch( Exception e )
        {
            throw new CleverException( e );
        }
    }
    
    @Override
    public void shutDown()
    {
        
    }
}
