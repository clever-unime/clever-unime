/**
 * The MIT License
 * 
 * @author dott. Riccardo Di Pietro - 2014
 * MDSLab Messina
 * dipcisco@hotmail.com
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


package org.clever.ClusterManager.SecurityWeb;

import org.clever.ClusterManager.SecurityWeb.SecurityWebPlugin.SecurityWebPlugin;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;

public class SecurityWebAgent extends CmAgent {

    private SecurityWebPlugin securityWeb;
    //private Class cl;
   
    
    public SecurityWebAgent() throws CleverException  {
        super();
        
      
    }

    @Override
    public void initialization() throws CleverException {
        if (super.getAgentName().equals("NoName")) {
            super.setAgentName("SecurityWebAgent");
        }
        super.start();
        try 
        {
            
            securityWeb = (SecurityWebPlugin) super.startPlugin("./cfg/configuration_securityWeb.xml","/org/clever/ClusterManager/SecurityWeb/configuration_securityWeb.xml");        
            securityWeb.setOwner(this);
            logger.info("SecurityWebPlugin created ");
            this.setPluginState(true);
        } catch (Exception ex) {
            logger.error("SecurityWebPlugin creation failed: " + ex.getMessage());
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
