/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
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
package org.clever.Common.Communicator;

import java.io.File;
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
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.Shared.LoggerInstantiator;

/**
 * @autor marco carbone
 *
 */
public abstract class Agent implements MethodInvokerHandler {

    public LoggerInstantiator loggerInstantiator;
    public static Logger logger = null;
    protected ModuleCommunicator mc = null;
    private String agentName = "";
    public static Runnable r; //this handle of kind Runnable is used to instantiate the thread for shutdown

    public Agent() {
        try {
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream("/org/clever/Common/Shared/logger.properties");
            prop.load(in);
            PropertyConfigurator.configure(prop);
            
        } catch (IOException e) {

            logger.error("Missing logger.properties");
        }

        loggerInstantiator = new LoggerInstantiator();
        logger = Logger.getLogger("Agent");
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

    public void start() throws CleverException {
        try {
            logger.info("\nistanzio ModuleCommunicator(agentName), dove agentName: " + this.getAgentName());
            mc = new ModuleCommunicator(this.getAgentName(), "HM"); //N.B. the module communicator was istantiated here!. Group is HM: this class is designed for a HM Agent
            logger.info("\nnfine istanza ModuleCommunicator");
            mc.setMethodInvokerHandler(this);

            r = new ShutdownThread(this);
        } catch (java.lang.NullPointerException e) {
            throw new CleverException(e, "Missing logger.properties or configuration not found");
        } catch (java.io.IOException e) {
            throw new CleverException(e, "Error on reading logger.properties");
        } catch (InstantiationException e) {
            throw new CleverException(e, "MC Plugin Instantiation error");
        } catch (IllegalAccessException e) {
            throw new CleverException(e, "Error Access");
        } catch (Exception e) {
            throw new CleverException(e);
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

            for (i = 0; i < params.size(); i++) {
                par[i] = params.get(i).getClass();
                input[i] = params.get(i);
            }
        } else {
            par = null;
        }
        try {
            //agent method
            mthd = this.getClass().getMethod(method.getMethodName(), par);

            if (method.getHasReturn()) {
                output = (Object) mthd.invoke(this, input);


            } else {
                mthd.invoke(this, input);
                output = null;
            }
            logger.debug("Method invoked: " + method.getMethodName());
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
                //plugin method
                mthd = getPluginClass().getMethod(method.getMethodName(), par);
                if (method.getHasReturn()) {

                    output = (Object) mthd.invoke(getPlugin(), input);
                } else {
                    mthd.invoke(getPlugin(), input);
                    output = null;
                }

            } catch (IllegalAccessException ex1) {
                logger.error("Error: " + ex);
                throw new CleverException("Illegal Argument Exception: " + ex.getMessage());
            } catch (IllegalArgumentException ex1) {
            } catch (InvocationTargetException ex1) {
                logger.error("Error: " + ex);
                throw new CleverException(ex1.getTargetException(), "Error on plugin method invocation: " + ex1.getTargetException().getMessage());
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
        }

    }

    public void sendNotification(Notification notification) {
        try {
            List params = new ArrayList();
            params.add(notification);
            notification.setAgentId(agentName);
            MethodInvoker mi = new MethodInvoker("DispatcherAgent",
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
        return mc.invoke(targetMethod);


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



        if (args.length == 1) {
            try {
                Class agentClass = Class.forName(args[0]);
                Agent agent = (Agent) agentClass.newInstance();



                agent.setAgentName("NoName");
                agent.initialization();
            } catch (InstantiationException ex) {
                logger.error("Error instantiating agent: " + ex);
            } catch (IllegalAccessException ex) {
                logger.error("Error instantiating agent: " + ex);
            } catch (ClassNotFoundException ex) {
                logger.error("Error instantiating agent: " + ex);
            }
        } else {
            try {
                Class agentClass = Class.forName(args[0]);
                Agent agent = (Agent) agentClass.newInstance();

                agent.setAgentName(args[1]);


                agent.initialization();
            } catch (InstantiationException ex) {
                logger.error("Error instantiating agent: " + ex);
            } catch (IllegalAccessException ex) {
                logger.error("Error instantiating agent: " + ex);
            } catch (ClassNotFoundException ex) {
                logger.error("Error instantiating agent: " + ex);
            }
        }

        //Thread for the shutdown
        Thread hook = new Thread(r); 
        Runtime.getRuntime().addShutdownHook(hook); 


        while (true) {
            try {
                Thread.sleep(1000000); //Inside, the main thread goes to sleep periodically 
            } catch (Exception ex) {
                logger.error("Error during thread master sleep");
                System.exit(1);
            }
        }
    }
    
    /**
     * 
     * @param logger
     * @param pathLogConf dir dove andare a prendere i frammenti xml per comporre il file di conf
     * @param pathDirOut dir dove salvare i file di log
     * 
     * es:
     * pathLogConf -> "/sources/org/clever/HostManager/Info/log_conf/"
     * pathDirOut -> "/LOGS/HostManager/Info"
     */
    public void setLog4J(Logger logger, String pathLogConf, String pathDirOut){
      //
      String radice =  System.getProperty("user.dir"); 
      String path = radice +pathLogConf; 
      String log4jConfigFile= path+"/conf.xml";
      String vett[]={path};
      Log4J log =new Log4J();
      new File(radice+pathDirOut).mkdirs();
    //  log.creaDir(radice+pathDirOut);
      log=new Log4J(radice,log4jConfigFile,vett,1,logger);
      log.creaFileConfigurazioneLog();
      log.assegnaConfToLog4j(log4jConfigFile);
      //
    }
    
    
    
}