/*
 * The MIT License
 *
 * Copyright 2011 giovalenti.
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
package org.clever.ClusterManager.VirtualizationManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.VEInfo.DesktopVirtualization;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;

/**
 *
 * @author giovalenti
 */
public class VirtualizationManagerAgent extends CmAgent {
    
    private Class cl;
    private VirtualizationManagerPlugin VirtualizationManager;
    private String agentName;
    private String notificationIdRegisterVirtualDeskHTML5 = "Virtualization/RegisterVirtualDesktopHTML5";
    private String notificationIdUnRegisterVirtualDeskHTML5 = "Virtualization/UnRegisterVirtualDesktopHTML5";
    private String notificationStartedVm = "Virtualization/VmStarted";
    private String notificationCreatedVm = "Virtualization/VmCreated";
    private String notificationImportedVm = "Virtualization/VmImported";
    private String notificatioPresenceHM = "PRESENCE/HM";
 //Modifico il virtualizationManagerAgent 05/24/2012 Rob
  
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger=null;
    private String pathLogConf="/sources/org/clever/ClusterManager/VirtualizationManager/log_conf/";
    private String pathDirOut="/LOGS/ClusterManager/VirtualizationManager";
    //########
    
    
    
   //05/24/2012 
    public VirtualizationManagerAgent() {
      super();
      
      //#############################################
      //Inizializzazione meccanismo di logging
       logger=Logger.getLogger("VirtualizationManager");    
       Log4J log =new Log4J();
       log.setLog4J(logger, pathLogConf, pathDirOut);
    //#############################################
    }
    
    
    public void initialization()throws CleverException,IOException {
        try {
            List params = null;
           //Load properties from XML file
            if(super.getAgentName().equals("NoName"))
               super.setAgentName("VirtualizationManagerAgent");
            
            super.start();
            
            try {
                this.logger.info("Read Configuration VirtualManager!");
                FileStreamer fs = new FileStreamer();
                InputStream inxml = getClass().getResourceAsStream("/org/clever/ClusterManager/VirtualizationManager/configuration_VirtualizationManager.xml");
                ParserXML pXML = new ParserXML(fs.xmlToString(inxml));
        
                this.cl = Class.forName(pXML.getElementContent("VirtualizationManager"));
                VirtualizationManager=(VirtualizationManagerPlugin)this.cl.newInstance();
                this.agentName=pXML.getElementContent( "moduleName" );
                this.VirtualizationManager.setModuleCommunicator(mc);
                this.VirtualizationManager.setOwner(this);
                
                VirtualizationManager.init(pXML.getRootElement().getChild("pluginParams"), this);   
                 
              
                logger.debug("called init of " + pXML.getElementContent("VirtualizationManager"));
        
                 
                params= new ArrayList();
                params.add(this.agentName);
                params.add(this.notificatioPresenceHM);
                this.invoke("DispatcherAgent", "subscribeNotification", true, params);
                
                
                params = new ArrayList();
                params.add(this.agentName);
                params.add(this.notificationIdRegisterVirtualDeskHTML5);  
               // mi = new MethodInvoker("DispatcherAgent","subscribeNotification", true, params);
              //  this.mc.invoke(mi);
                this.invoke("DispatcherAgent", "subscribeNotification", true, params);
       
                params = new ArrayList();
                params.add(this.agentName);
                params.add(this.notificationIdUnRegisterVirtualDeskHTML5);
           //     mi = new MethodInvoker("DispatcherAgent","subscribeNotification", true, params);
             //   this.mc.invoke(mi);
                 this.invoke("DispatcherAgent", "subscribeNotification", true, params);
                
                params = new ArrayList();
                params.add(this.agentName);
                params.add(this.notificationStartedVm);
            //    mi = new MethodInvoker("DispatcherAgent","subscribeNotification", true, params);
            //    this.mc.invoke(mi);
                 this.invoke("DispatcherAgent", "subscribeNotification", true, params);
                
                params = new ArrayList();
                params.add(this.agentName);
                params.add(this.notificationCreatedVm);
             //   mi = new MethodInvoker("DispatcherAgent","subscribeNotification", true, params);
             //   this.mc.invoke(mi);
                 this.invoke("DispatcherAgent", "subscribeNotification", true, params);
                
                params = new ArrayList();
                params.add(this.agentName);
                params.add(this.notificationImportedVm);
              //  mi = new MethodInvoker("DispatcherAgent","subscribeNotification", true, params);
             //   this.mc.invoke(mi);
                this.invoke("DispatcherAgent", "subscribeNotification", true, params);
               
                logger.info("VirtualizationManager Agent created ");
                
            } catch (ClassNotFoundException ex) {
                logger.error("Error: " + ex);
            } catch (IOException ex) {
                logger.error("Error: " + ex);
            } catch (InstantiationException ex) {
                logger.error("Error: " + ex);
            } catch (IllegalAccessException ex) {
                logger.error("Error: " + ex);
            } catch (Exception ex) {
                logger.error("VirtualizationManager creation failed: " + ex);
            }
            
        } catch (CleverException ex) {
            java.util.logging.Logger.getLogger(VirtualizationManagerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Override
    public Class getPluginClass() {
        return this.cl;
    }
    
    @Override
    public Object getPlugin() {
        return this.VirtualizationManager;
    }
    
    @Override
    public void handleNotification(Notification notification) throws CleverException {
        logger.debug("Received notification type: " + notification.getId());
        
        if (notification.getId().equals(this.notificationIdRegisterVirtualDeskHTML5)) {
            DesktopVirtualization desktop = (DesktopVirtualization) notification.getBody();
            try {
                this.VirtualizationManager.RegisterVirtualizationDesktopHTML5(desktop);
            } catch (Exception ex) {
                throw new CleverException("Registration DesktopVirtualization " + desktop.getUsername() + " into Guacamole failed");
            }
        }
        if (notification.getId().equals(this.notificationIdUnRegisterVirtualDeskHTML5)) {
            String body = null;
            logger.debug("Received notification type: " + notification.getId());
            try {
                body = (String) notification.getBody();
                this.VirtualizationManager.UnRegisterVirtualizationDesktopHTML5(body);                
            } catch (Exception ex) {
                throw new CleverException("UnRegistration DesktopVirtualization " + body + " into Guacamole failed");
            }
        }
        if (notification.getId().equals(this.notificationStartedVm)) {
            String nameVM = "";
            logger.debug("Received notification type: " + notification.getId());
            try {
                nameVM = (String) notification.getBody();
                
                List params1 = new ArrayList();
                params1.add("VirtualizationManagerAgent");
                params1.add(" attribute {'started'}{'" + new Date().toString() + "'}");
                params1.add("with");
                params1.add("/VMs_Running/VM[@name='" + nameVM + "']/@started");
                
                this.invoke("DatabaseManagerAgent", "updateNode", true, params1);
                
            } catch (Exception ex) {
                throw new CleverException("Timestamp startvm into DB failed!");
            }
        }
        if (notification.getId().equals(this.notificationCreatedVm)) {
            String nameVM = "";
            logger.debug("Received notification type: " + notification.getId());
            try {
                nameVM = (String) notification.getBody();
                List params1 = new ArrayList();
                params1.add("VirtualizationManagerAgent");
                params1.add(" attribute {'created'}{'" + new Date().toString() + "'}");
                params1.add("into");
                params1.add("/Matching_VM_HM/VM[@name='" + nameVM + "']");
                
                this.invoke("DatabaseManagerAgent", "insertNode", true, params1);
                
            } catch (Exception ex) {
                throw new CleverException("Timestamp createvm into DB failed!");
            }
        }
        if (notification.getId().equals(this.notificationImportedVm)) {
            String nameVM = "";
            logger.debug("Received notification type: " + notification.getId());
            try {
                nameVM = (String) notification.getBody();
                List params1 = new ArrayList();
                params1.add("VirtualizationManagerAgent");
                params1.add(" attribute {'imported'}{'" + new Date().toString() + "'}");
                params1.add("into");
                params1.add("/Matching_VM_HM/VM[@name='" + nameVM + "']");
                
                this.invoke("DatabaseManagerAgent", "insertNode", true, params1);
                
            } catch (Exception ex) {
                throw new CleverException("Timestamp importedvm into DB failed!");
            }
        }
        if (notification.getId().equals(this.notificatioPresenceHM)) {
            String nameHM = "";
            
            logger.debug("Received notification type: " + notification.getId());
            try {
                nameHM = (String) notification.getHostId();
                /**/
                this.VirtualizationManager.manageReUpHost(nameHM);
                        //vmsClever.lastIndexOf(r))
            } 
            catch (Exception ex) {
                throw new CleverException("Sincornization with host's "+nameHM+" hypervisor is failed!"+ex.getMessage());
            }
        }
    }
    
  
    @Override
    public void shutDown() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
