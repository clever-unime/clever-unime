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
/*
 *  Copyright (c) 2010 Universita' degli studi di Messina
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
package org.clever.Common.Communicator;

/**
 * @author 2010 Filippo Bua
 * @author 2010-2013 Maurizio Paone
 * @author 2010 Francesco Tusa
 * @author 2010 Massimo Villari
 * @author 2010 Antonio Celesti
 * @author 2010 Antonio Nastasi
 * @author 2012 Marco Carbone
 * @author 2013 Giuseppe Tricomi
 */

import org.clever.Common.Plugins.RunnerPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Shared.LoggerInstantiator;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.Common.Shared.Support;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

public abstract class Agent implements MethodInvokerHandler {
//TODO : aggiungere lo stato di errore
    private boolean pluginState;

  
    final public  Logger logger ;
    final protected ModuleCommunicator mc ;
    private String agentName = "";
    public static Runnable r; //this handle of kind Runnable is used to instantiate the thread for shutdown
    protected String group = null; //gruppo per le comunicazioni HM per hostmanager CM per Clustermanager
    public RunnerPlugin pluginInstantiation=null;
    protected Class cl;
    public String cfgfilepath=null;
    public String errorStr;
    
      public boolean isPluginState() {
        return pluginState;
    }

    public void setPluginState(boolean pluginState) {
       
        this.pluginState = pluginState;
        //this.notify();
    }
    
    public Agent() throws CleverException {
        logger = Logger.getLogger(this.getClass());
        logger.debug("start Agent constructor");

        try {
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream("/org/clever/Common/Shared/logger.properties");
            prop.load(in);
            PropertyConfigurator.configure(prop);
            group = "HM";
            
            
        } catch (IOException e) {

            logger.error("Missing logger.properties");
        }

        try {
            
            mc = new ModuleCommunicator(); // N.B. the module communicator was istantiated here!. 
                                                                       // Group is HM: this class is designed for a HM Agent
        } catch (InstantiationException ex) {
            
            logger.error("Error on Agent creation: " + ex);
           
            throw new CleverException(ex);
        } catch (IllegalAccessException ex) {
            logger.error("Error on Agent creation: " + ex);
           
            throw new CleverException(ex);
        } catch (ClassNotFoundException ex) {
            logger.error("Error on Agent creation: " + ex);
           
            throw new CleverException(ex);
        } catch (IOException ex) {
            logger.error("Error on Agent creation: " + ex);
           
            throw new CleverException(ex);
        }
        
        
        
        
        

    }

    /*
     * This function must implemented by all the agents children in this way: 1.
     * first must check if has already been assigned a name to the agent,
     * otherwise it must assigned with the function setAgentName 2. must perform
     * the necessary initialization actions 3. must call the function start(9 of
     * superclass Agent
     *
     */
    public abstract void initialization() throws Exception;
    
    //TODO: mettere stato di errore
    
     public RunnerPlugin startPlugin(String cfgFile,String classpath)throws CleverException, IOException{
        this.cfgfilepath=cfgFile;
        try
        {
            ParserXML pXML = getconfiguration(cfgFile,classpath);
            this.pluginInstantiation=this.startPlugin(pXML);
            //this.setPluginState(true);
        }
        catch( Exception e )
        {
            this.setPluginState(false);
            this.errorStr=e.getLocalizedMessage();
            throw new CleverException( "Start Plugin on Agent Throw: "+e.getLocalizedMessage());
        }
        return this.pluginInstantiation;
    }
    public RunnerPlugin startPlugin(ParserXML pXML)throws CleverException, IOException{
        try
        {
            pXML.printElementContentText();
            
            logger.debug("agent1");
            String al= pXML.getElementContent( "ClassPlugin" );
            if(al==null)
                logger.debug(pXML.getDocument().getContentSize());
            logger.debug("agent2");
            cl = Class.forName(al);
            
            this.pluginInstantiation= ( RunnerPlugin ) cl.newInstance();
           
            this.pluginInstantiation.setOwner(this);
           logger.debug("agent3");
            this.pluginInstantiation.init( pXML.getRootElement().getChild( "pluginParams" ),this );
            logger.debug("agent4");
            setAgentName(pXML.getElementContent("moduleName"));
        }
        catch( java.lang.NullPointerException e )
        { 
             logger.error(" error4:"+e.getMessage(),e);
            this.setPluginState(false);
            this.errorStr=e.getLocalizedMessage();
            throw new CleverException( e, "Missing logger.properties or bad configuration retrieved from configuration files" );       
            
        }
        catch( ClassNotFoundException e )      
        {
             logger.error(" error3:"+e.getMessage(),e);
            this.setPluginState(false);
            this.errorStr=e.getLocalizedMessage();
            throw new CleverException( e, "Plugin Class not found" );
        }
        catch( InstantiationException e )
        {
             logger.error(" error2:"+e.getMessage(),e);
            this.setPluginState(false);
            this.errorStr=e.getLocalizedMessage();
            throw new CleverException( e, "Plugin Instantiation error" );
        }
        catch( IllegalAccessException e )
        {
            logger.error(" error1:"+e.getMessage(),e);
            this.setPluginState(false);
            this.errorStr=e.getLocalizedMessage();
            throw new CleverException( e, "Error Access" );
        }
        catch( Exception e )
        {
            this.setPluginState(false);
            this.errorStr=e.getLocalizedMessage();
            logger.error("generic error:"+e.getMessage(),e);
            throw new CleverException( e,e.getLocalizedMessage() );
        }
        logger.debug("Plugin for agent:"+this.agentName+" has this state:"+this.isPluginState());
        return this.pluginInstantiation;
    }
     
    public ParserXML getconfiguration(String cfgFile,String classpath)throws CleverException{
        logger.debug("cfgfile:"+cfgFile);
        logger.debug("classpath:"+classpath);
        File cfgLocalFile = new File(cfgFile);
        ParserXML pXML=null;
        try {
            InputStream inxml = null;
            
            if(cfgLocalFile.exists())
            {
                //A configuration file exists on cfg/...
                inxml = new FileInputStream(cfgLocalFile);
                logger.debug("Configuration from localfile");
            }
            else
            {
                //retrieve configuration from classpath
                inxml = getClass().getResourceAsStream(classpath);
                InputStream inxmlTest=getClass().getResourceAsStream(classpath);
                logger.debug("Configuration from classpath");
                //TODO: verify if element "writeOnCfgFolder" have value "yes", in this case we can make a copy of this file on cfg folder
                pXML= new ParserXML(inxmlTest);
                boolean canCopy=((String)pXML.getElementContent("writeOnCfgFolder","yes").substring(0)).equals("yes");
                if(canCopy)
                {
                    try                
                    {   
                        
                        Support.copy( inxml, cfgLocalFile );                
                    }                
                    catch( IOException ex ) //se entriamo qui dentro significa che si è verificato un errore con la copia del file!
                    {               
                        this.logger.error( "The copy of the file 'configuration_template.xml' is failed; " + ex  );                    
                        throw new CleverException("The copy of the file 'configuration_template.xml' is failed; " + ex.getMessage() );     	
                    }
                    return pXML;
                }
            }
            if(inxml==null){
                logger.error("No configuration available");
                throw new CleverException("No configuration available");
            }
            else
            {
                pXML= new ParserXML(inxml);
            }    
        } catch (IOException ex) {
            logger.error("Error: " + ex);
        }
        
        return pXML;
    }
    //TODO: make test method for this function
    public void restartPlugin(String configfile,Boolean write) throws CleverException{
        ParserXML pXML=new ParserXML(configfile);
        logger.debug("restartplug0");
        if(write){
            try{
                if(this.cfgfilepath!=null)
                {
                    File f=new File(this.cfgfilepath);
                    f.delete();
                    InputStream is = new ByteArrayInputStream(configfile.getBytes());
                    f=new File(this.cfgfilepath);
                    Support.copy(is, f);
        
                }
            }catch(SecurityException e)
            {
                logger.error("Cannot delete configuration file for restart operation of the plugin");
            }catch(Exception e)
            {
                logger.error("An exception is occourred in delete configuration file phase in the plugin restart"+ this.getAgentName());
            }
        }
        try{
             logger.debug("restartplug3");
             this.pluginInstantiation=this.startPlugin(pXML);
             this.setPluginState(true);
        }
        catch( Exception e )
        {
            this.setPluginState(false);
            this.errorStr=e.getMessage();
            throw new CleverException( e );
        }
        
    }
    public void start() throws CleverException {
        try {
            logger.debug("\nistanzio ModuleCommunicator(agentName), dove agentName: " + this.getAgentName());
           
            mc.setMethodInvokerHandler(this);
            logger.debug("agentstart1 "+agentName+" "+ this.group);
            mc.init(agentName, this.group);
            logger.debug("agentstart2");
            r = new ShutdownThread(this);
            //logger.debug("agentstart3");
        } catch (java.lang.NullPointerException e) {
            logger.debug("agentstart3.1");
            throw CleverException.newCleverException(e, "Missing logger.properties or configuration not found");
        } 
        catch (Exception e) {
            logger.debug("agentstart3.2");
            throw CleverException.newCleverException(e, e.getMessage());
        }

    }

    @Override
    public Object handleInvocation(MethodInvoker method) throws CleverException {
        int i = 0;
        Method mthd;
        Class[] par;
        Object output = null;
        Object[] input = null;
        List params = method.getParams();
       
        if (params != null) {
            par = new Class[params.size()];
            input = new Object[params.size()];
            //logger.debug("%%%&handleinvocation1");
            for (i = 0; i < params.size(); i++) {
                par[i] = params.get(i).getClass();
                input[i] = params.get(i);
                //logger.debug("%%%&handleinvocation2f");
            }
        } else {
            par = null;
            
        }
        try {
            //logger.debug("%%%&handleinvocation"+"/n method inv:"+method.getMethodName()+" for agent :"+ this.agentName+"with params:"+(par==null?"null":"notnull"));
            
                
          
               // logger.debug("%%%2222"+Arrays.toString(this.getPluginClass().getMethods()));
                mthd = this.getClass().getMethod(method.getMethodName(), par);
                
           
           
            if (method.getHasReturn()) {
               
                output = (Object) mthd.invoke(this, input);
               
            } else {
                
                mthd.invoke(this, input);
                
                output = null;
            }
            return (output);
        } catch (IllegalAccessException ex) {
            logger.error("Error: " + ex);
            throw new CleverException("IllegalAccessException: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("Error: " + ex);
            throw new CleverException("Illegal Argument Exception: " + ex.getMessage());
        } catch (InvocationTargetException ex) {
            logger.error("Error: " + ex);
            throw new CleverException(ex.getTargetException(), "Error on plugin method invocation: " + ex.getTargetException().getMessage());
        } catch (NoSuchMethodException ex) {
            try {
                logger.debug("%%%&handleinvocationNoMethod:"+method.getMethodName()+ " "+this.isPluginState()+this.pluginState);
                //plugin method
                if(this.isPluginState()){
                    
                    mthd = getPluginClass().getMethod(method.getMethodName(), par);
                   
                    if (method.getHasReturn()) {
                        output = (Object) mthd.invoke(getPlugin(), input);
                      
                    } else {
                        mthd.invoke(getPlugin(), input);
                       
                        output = null;
                    }
                }
               else{
                    CleverException e=new CleverException("The Plugin is not corrected Started: An error is occurred in initialization process!\nThe error is: "+this.errorStr+"/n method inv:"+method.getMethodName()+" for agent :"+ this.agentName);
                    throw new CleverException(e.getMessage());
                }

            } catch (IllegalAccessException ex1) {
                logger.error("Error: " + ex1);
                throw new CleverException("Illegal Argument Exception: " + ex.getMessage());
            } catch (IllegalArgumentException ex1) {
                logger.error("%%%&handleinvocation IllegalArgumentException");
            } catch (InvocationTargetException ex1) {
                logger.error("Error: " + ex1);
                Throwable exception = ex1.getTargetException();
                
                throw CleverException.newCleverException(exception, "Error on plugin method invocation: " + ex1.getTargetException().getMessage());
            } catch (NoSuchMethodException ex1) {
                logger.error("Error: " + ex);
                throw new CleverException("method not found: " + ex.getMessage());
            } catch (SecurityException ex1) {
                logger.error("Error: " + ex);
                throw new CleverException("Security Exception: " + ex.getMessage());
            }
           logger.debug("Method invoked: " + method.getMethodName());
            return (output);


        } catch (SecurityException ex) {
            logger.error("Error: " + ex);
            throw new CleverException("Security Exception: " + ex.getMessage());
        }/*catch (Exception e){
            logger.error("genericError:",e);
            throw new CleverException("Generic Exception: " + e.getMessage());
        }*/

    }

    public void sendNotification(Notification notification) {
        try {
            List params = new ArrayList();
            params.add(notification);
            notification.setAgentId(agentName);
            //System.out.println("sendNotification invoke");
            MethodInvoker mi = new MethodInvoker("DispatcherAgentHm",
                    "sendNotification",
                    true,
                    params);

            mc.invoke(mi);
        } catch (CleverException ex) {
            logger.error("Invoke error: " + ex);
        } catch (SecurityException ex) {
            logger.error("Invoke error: " + ex);
        }

    }

    public abstract Class getPluginClass();

    public abstract Object getPlugin();

    /**
     * @autor marco carbone within this method, each agent son must manage its
     * closure procedure: releasing resources used and closing all the
     * connections opened
     */
    public abstract void shutDown();

    /**
     * @return the agentName
     */
    public String getAgentName() {
        return agentName;
    }

    /**
     * @param agentName the agentName to set
     */
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    /**
     * Invokes a method of an agent using the module communicator
     *
     * @param agentTarget the name of the target agent
     * @param methodTarget the method that has to be invoked
     * @param hasReply true if the method returns an object
     * @param params the method's parameters
     * @return the object returned by the method invoked or null if hasReply is
     * false
     * @throws CleverException
     */
    public Object invoke(String agentTarget, String methodTarget, boolean hasReply, List params) throws CleverException {

        MethodInvoker targetMethod = new MethodInvoker(agentTarget, methodTarget, hasReply, params);
        logger.debug(this.agentName+" invoke "+agentTarget+" on ");
        return mc.invoke(targetMethod);


    }
    
    /**
     * Invokes a method of an agent using the module communicator
     *
     * @param agentTarget the name of the target agent
     * @param methodTarget the method that has to be invoked
     * @param hasReply true if the method returns an object
     * @param params the method's parameters
     * @return the object returned by the method invoked or null if hasReply is
     * false
     * @throws CleverException
     */
    public Object invoke(MethodInvoker mi) throws CleverException {

        return mc.invoke(mi);


    }

    /**
     * @autor marco carbone
     *
     * This main function is inherited by all the agents that extend Agent
     *
     * @param args: String AgentClassName, String AgentName (optional) The param
     * AgentClassName is the path of the class Agent son The param AgentName is
     * the name to assign to Agent son before istantiate it.
     *
     *
     */
    public static void main(String[] args) throws NoSuchMethodException, IllegalArgumentException, InvocationTargetException, Exception {


        Agent agent = null;
        if (args.length == 1) {
            
            try {
                Class agentClass = Class.forName(args[0]);
                agent = (Agent) agentClass.newInstance();



                agent.setAgentName("NoName");
                agent.initialization();
            } catch (InstantiationException ex) {
                
                agent.logger.error("Error instantiating agent: " + ex);
            } catch (IllegalAccessException ex) {
                agent.logger.error("Error instantiating agent: " + ex);
            } catch (ClassNotFoundException ex) {
                agent.logger.error("Error instantiating agent: " + ex);
            }
        } else {
            try {
                Class agentClass = Class.forName(args[0]);
                agent = (Agent) agentClass.newInstance();

                agent.setAgentName(args[1]);


                agent.initialization();
            } catch (InstantiationException ex) {
                agent.logger.error("Error instantiating agent: " + ex);
            } catch (IllegalAccessException ex) {
                agent.logger.error("Error instantiating agent: " + ex);
            } catch (ClassNotFoundException ex) {
                agent.logger.error("Error instantiating agent: " + ex);
            }
        }

        //Thread for the shutdown
        Thread hook = new Thread(r); 
        Runtime.getRuntime().addShutdownHook(hook); 


        while (true) {
            try {
                Thread.sleep(1000000); //Inside, the main thread goes to sleep periodically 
            } catch (Exception ex) {
                agent.logger.error("Error during thread master sleep");
                System.exit(1);
            }
        }
    }
    
    public void restartAgent(){
        try
        {
            this.initialization();
        }catch(Exception e){
            logger.error("Problem is occurred in Restart agent:"+this.getAgentName(),e);
        }
            
    }
}