/*
 *  The MIT License
 * 
 *  Copyright 2011 brady.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.clever.HostManager.ServiceManagerPlugins.Linux;

import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.HostManager.ServiceManager.ServiceManagerPlugin;
import org.clever.HostManager.ServiceManager.ServiceObject;

import org.jdom.Element;


/**
 *
 * @author giovalenti
 */
public class ServiceManagerLinux implements ServiceManagerPlugin{

    private Agent owner;
    private Logger logger;

    private String version = "0.0.1";
    private String description = "Plugin per Hypervisor VMWare";
    private String name = "VMWare Plugin";

    private Guacamole guaca;
    private Class cl;

    public ServiceManagerLinux()throws Exception{
        this.logger = Logger.getLogger( "Hypervisor plugin" );
        this.logger.info("VMWare plugin created: ");
    }


    @Override
    public void init(Element params, Agent owner) throws CleverException {
        if(params!=null){
            //Read param from configuration_networkManager.xml
        }
        this.owner = owner;
    }

   
    @Override
    public Boolean ServiceUpdate(String name, ServiceObject object) throws Exception {        
        boolean status = false;
                
        if(name.equals("guacamole")){
            this.guaca = new Guacamole(object);
            this.guaca.update();
            status = true;
        }
        return status;
    }

    @Override
    public Boolean ServiceStart(String id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Boolean ServiceStop(String id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Boolean ServiceRestart(String id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Boolean ServiceStatus(String id) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setOwner(Agent owner) {
        this.owner=owner;
    }

    

}
