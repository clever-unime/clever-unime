/*
 * The MIT License
 *
 * Copyright 2014 agalletta.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.clever.ClusterManager.BigData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Utils.BigDataParameterContainer;
import org.clever.Common.Utils.TypeOfElement;

/**
 *
 * @author agalletta
 */
public class BigDataAgent extends CmAgent {
    
    private BigDataPlugin bigDataPlugin;
    private Logger logger;
    private String configPath="./cfg/configuration_bigDataPlugin.xml";
    
    public BigDataAgent() throws CleverException {
        super();
        logger=Logger.getLogger("BigDataPlugin");
        
    }
    
      @Override
    public void initialization() throws CleverException, IOException
    {
        if(super.getAgentName().equals("NoName")){
            super.setAgentName("BigDataAgent");
        }
        super.start();
        
        try
        {
            this.startPlugin();
            
            logger.info( "BigDataAgent created " );
           
            List params = new ArrayList();
            params.add(super.getAgentName());
           //TO DO: modificare la notifica
            params.add("SENSING/NOTIFICATION");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);
            params = new ArrayList();
            params.add(super.getAgentName());
           //TO DO: modificare la notifica
            params.add("VMLOG/NOTIFICATION");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);
             params = new ArrayList();
            params.add(super.getAgentName());
           //TO DO: modificare la notifica
            params.add("VMSTATE/NOTIFICATION");
            this.invoke("DispatcherAgent", "subscribeNotification", true, params);
           
        }
        catch( java.lang.NullPointerException e )
        { 
            throw new CleverException( e, "Missing logger.properties or configuration not found" );       
        }
        catch( java.io.IOException e )
        {
            throw new CleverException( e, "Error on reading logger.properties" );
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
        return this.bigDataPlugin;
    }
    
      @Override
    public void handleNotification(Notification notification) throws CleverException {
        
        logger.debug("Received notification type: "+notification.getId());
        BigDataParameterContainer container= new BigDataParameterContainer();
        container.setElemToInsert(notification.getBody());
        container.setType(TypeOfElement.STRINGXML);
        if(notification.getId().equalsIgnoreCase("SENSING/NOTIFICATION")){
            
            this.bigDataPlugin.insertSensing(container);
            }
        else
            if(notification.getId().equalsIgnoreCase("VMLOG/NOTIFICATION")){
            this.bigDataPlugin.insertVMLog(container);
            }
        else
            if(notification.getId().equalsIgnoreCase("VMSTATE/NOTIFICATION")){
            this.bigDataPlugin.insertHostState(container);
            }
        else
                logger.error("notification type unknown");
       
    }
    
    public void startPlugin()throws CleverException, IOException{
        try
        {
           
            this.bigDataPlugin = ( BigDataPlugin )super.startPlugin("./cfg/configuration_bigDataPlugin.xml","/org/clever/ClusterManager/BigData/configuration_bigDataPlugin.xml");
             this.bigDataPlugin.setOwner(this);
            logger.info( "BigDataPlugin created " );
             this.setPluginState(true);
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
