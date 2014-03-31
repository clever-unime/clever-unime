/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
 *  Copyright (c) 2011 Marco Carbone
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
package org.clever.HostManager.HostCoordinator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.ModuleCommunicator;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Initiator.ModuleFactory.ModuleFactory;
import org.clever.Common.Initiator.ModuleFactory.ShutdownThread;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.Common.XMPPCommunicator.*;
import org.clever.HostManager.Dispatcher.DispatcherAgent;
import org.clever.HostManager.Info.InfoAgent;
import org.clever.HostManager.Test.TestAgent;
import org.jdom.Element;
import org.jivesoftware.smack.packet.Presence.Mode;

public class HostCoordinator implements CleverMessageHandler {

    private ConnectionXMPP conn;
    private ModuleFactory moduleFactory;
    private ModuleCommunicator mc;
    private String cfgPath = "./cfg/configuration_initiator.xml"; //28/11/2011: il file di configurazione ora coincide con quello dell'initiator!
    private ParserXML pXML;
    private InfoAgent infoAgent;
    private DispatcherAgent dispatcherAgent;
    InputStream inxml;
    File cfgFile;
    private int notificationsThreshold;
    private boolean replaceAgents;
    private int numReload; //memorizzo il numero di volte max x rilanciare un agente
    private int timeReload; //memorizzo il tempo max x rilanciare un agente
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/HostCoordinator/log_conf/";
    private String pathDirOut="/LOGS/HostManager/HostCoordinator";
    //########
    

    public HostCoordinator(ConnectionXMPP conn) throws CleverException {
        
      //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("HostCoordinatorHM");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //############################################# 
        
        this.conn = conn; //il costruttore accetta come parametro la connessione dell'initiator

        moduleFactory = null;
        mc = null;
        pXML = null;
        infoAgent = null;
        dispatcherAgent = null;
        inxml = null;
        cfgFile = null;
        notificationsThreshold = 0;
        this.numReload = 0;
        this.timeReload = 0;
    }

    public void init()  {
        try {
            logger.debug("CLASSPATH= " + System.getProperty("java.class.path", null));
            inxml = new FileInputStream(cfgPath);

            FileStreamer fs = new FileStreamer();
            pXML = new ParserXML(fs.xmlToString(inxml));
        } catch (IOException ex) {
            logger.error("Error while parsing: " + ex);
        }

        //adding to the java.library.path the path containing CLEVER specific dynamic libraries
        //such path is read from the configuration file of the initiator within the node <librariespath>
        System.setProperty("java.library.path", pXML.getElementContent("librariespath") + ":" + System.getProperty("java.library.path"));

        Field fieldSysPath = null;
        try {
            fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        } catch (NoSuchFieldException ex) {
           logger.error(ex);
        } catch (SecurityException ex) {
            logger.error(ex);
        }
        fieldSysPath.setAccessible(true);
        try {
            fieldSysPath.set(null, null);
        } catch (IllegalArgumentException ex) {
            logger.error(ex);
        } catch (IllegalAccessException ex) {
            logger.error(ex);
        }



        System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + pXML.getElementContent("librariespath"));
        logger.debug(System.getProperty("java.library.path"));

        logger.info("Initiator created");

        notificationsThreshold = Integer.parseInt(pXML.getElementContent("notificationsthreshold"));

        //======= TEMPORANEO ======
        replaceAgents = Boolean.parseBoolean(pXML.getElementContent("replaceAgents"));

        this.numReload = Integer.parseInt(pXML.getElementContent("numReloadAgent"));
        this.timeReload = Integer.parseInt(pXML.getElementContent("timeReloadAgent"));

        logger.info("\n\n valori nuovi caricati: " + numReload + " " + timeReload);
    }

    public void changeStatus() {
        this.conn.getMultiUserChat().changeAvailabilityStatus("HM", Mode.available); //se l'hm viene istanziato cambia lo status che torna ad essere HM!
    }

    // TODO add CC threshold checker!!!!
    /*
     * Aggiungere un meccanismo simile a quello di elezione per
     * l'HostCoordinato: all'uscita di un CC, si lancia un algoritmo che
     * controlla, DOPO UN TEMPO di sleep, che la soglia sia soddisfatta o meno,
     * e in caso negativo lancia un nuovo CC
     */
    public void launchAgents() throws CleverException {
        try {
            dispatcherAgent = new DispatcherAgent(conn, notificationsThreshold);
            dispatcherAgent.initialization();
            infoAgent = new InfoAgent(conn);
            infoAgent.initialization();

            moduleFactory = ModuleFactory.getInstance();

            //==== TEMPORANEO! =========
            ModuleFactory.setActiveReplaceAgent(this.replaceAgents);

            //setto i valori x il monitor sul rilancio!
            moduleFactory.setReplacementVariable(this.timeReload, this.numReload);




            Element agents = pXML.getRootElement().getChild("agents");

            if (agents == null) {
                logger.error("agents element not found in config");
            } else {
                List agentsList = agents.getChildren("agent");

                for (Object agentO : agentsList) {
                    Element agent = (Element) agentO;
                    String className = agent.getChildText("class");

                    if (className == null) {
                        logger.error("Class element not found in config");
                        continue;
                    }

                    String moduleName = agent.getChildText("name");

                    if (moduleName.equals("")) {
                        logger.info("# inizio lancio agente nell'HC");
                        moduleFactory.createAgent(className);
                        logger.info("#fine lancio agente nell'HC!");
                    } else {
                        logger.info("# inizio lancio agente nell'HC");
                        moduleFactory.createAgent(className, moduleName);
                        logger.info("#fine lancio agente nell'HC!");
                    }
                    logger.debug("Agent created: " + agent.getChildText("class"));
                }
            }

            //ThreadGroup tg = moduleFactory.getReplaceAgent();

            // logger.info("\n\n?0ì876yhhjkk======= Il thread Group contiene: " +tg.activeCount() +" elemneti");

            if (!moduleFactory.getProcessList().isEmpty()) {
                Runnable r = new ShutdownThread(moduleFactory.getProcessList(), moduleFactory.getReplaceAgent());
                // logger.info("*****///////66%%%%$$££ERfg  La lista di processi agenti attivi x hm contine elementi: " +moduleFactory.getProcessList().size());
                Thread hook = new Thread(r);
                Runtime.getRuntime().addShutdownHook(hook);
            }



            mc = new ModuleCommunicator("HostCoordinator","HM");
            logger.debug("Module Communicator instantiated");
            logger.info("HostCoordinator created");
        } catch (Exception e) {
            logger.error("Error on HostCoordinator creation: " + e);
        }

        this.conn.addChatManagerListener(this);

        //testAgent = new TestAgent(); //ma il testAgent viene cmq lanciato!! indipendentemente dal fatto di selezionarlo dentro il file di configurazione!!!
        //testAgent.startTest();
    }

    public void start() throws CleverException {
        this.init();
       
        this.changeStatus();
        this.launchAgents();
    }

    @Override
    public synchronized void handleCleverMessage(final CleverMessage message) {
        //TODO add check for messages: REQUEST, etc..
        logger.debug("Message: " + message.toXML());
        methodDispatcher(message);
    }

    /**
     * This method will handle CleverMessage whose body is of 'exec' type For
     * other types of CleverMessage's body we plain to use other methods such as
     * RequestInformationDispatcher, etc... MethodDispatcher must be called ONLY
     * with 'exec' body
     *
     * @param message
     */
    public void methodDispatcher(final CleverMessage message) {
        MethodConfiguration methodConf = new MethodConfiguration(message.getBody(), message.getAttachments());
        MethodInvoker mi = new MethodInvoker(methodConf.getModuleName(),
                methodConf.getMethodName(),
                message.needsForReply(),
                methodConf.getParams());

        logger.debug("INFO: " + methodConf.getMethodName() + methodConf.getModuleName());

        CleverMessage cleverMsg = new CleverMessage();
        cleverMsg.setDst(message.getSrc());
        cleverMsg.setSrc(message.getDst());
        cleverMsg.setReplyToMsg(message.getId());
        cleverMsg.setHasReply(false);

        try {
            Object obj = mc.invoke(mi);

            if (message.needsForReply()) {
                cleverMsg.setType(CleverMessage.MessageType.REPLY);
                cleverMsg.setBody(new OperationResult(Result.ResultType.OBJECT,
                        obj,
                        methodConf.getModuleName(),
                        methodConf.getMethodName()));

                cleverMsg.addAttachment(MessageFormatter.messageFromObject(obj));
            } else {
                // No other operations are required
                logger.info("No other operations are required, exiting from method");
                return;
            }
        } catch (CleverException ex) {
            logger.debug("Exception captured: " + ex.getMessage());

            cleverMsg.setType(CleverMessage.MessageType.ERROR);
            cleverMsg.setBody(new ErrorResult(Result.ResultType.ERROR,
                    ex.toString(),
                    methodConf.getModuleName(),
                    methodConf.getMethodName()));
            cleverMsg.addAttachment(MessageFormatter.messageFromObject(ex));
        } finally {
            this.conn.sendMessage(message.getSrc(), cleverMsg);
        }
    }
}