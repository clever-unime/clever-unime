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
package org.clever.Common.Initiator.ModuleFactory;

import java.io.IOException;
import java.io.InputStream;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.*;
import org.jdom.Element;

public abstract class ModuleFactory 
{
    static ModuleFactory mf = null;
    private static ParserXML pXML;
    private static InputStream inxml;
    private static FileStreamer fs;
    private List<Process> processList = new LinkedList<Process>();
    private ThreadGroup replaceAgent = null;
    protected static Logger logger;
    private static Class moduleFactoryClass;
    private ArrayList<String> Agents = new ArrayList(); //TODO nomi dei moduli 
    
    /*variabili di settaggio per il ReplacementAgentDead*/
    private int interval_time;
    private int numLaunch;
    
    private static boolean activeReplaceAgent;
    
    /**
     * 
     * @return boolhan activeReplaceAgent: if this variable is set true the mechanism of replace agent is a active
     */
    public static boolean getActiveReplaceAgent()
    {
        return activeReplaceAgent;
    }
    
    /**
     * 
     * @param flag the value boolean to set the variable activeReplaceAgent
     */
    public static void setActiveReplaceAgent(boolean flag)
    {
        activeReplaceAgent = flag;
    }
    
    public void setReplacementVariable(int interval_time, int numLaunch) //questa variabile va richiamata dentro ClusterCoordinator e HostCoordinator, subito dopo listanza di MF!
    {
        this.interval_time = interval_time;
        this.numLaunch = numLaunch;
    }
    
    public int getInterval_time()
    {
        return this.interval_time;
    }
    
    public int getNumLaunch()
    {
        return this.numLaunch;
    }
    
    
    public String getClasspath() 
    {
        return System.getProperty("java.class.path", null);
    }

 
    /**This function instantiates once the thread group to which is added to 
     * the thread that will replace the agent terminated prematurely
     * 
     * @author marco carbone
     * @return ThreadGroup the group of the thread that replace the agent dead.
     */
    public ThreadGroup getIstanceReplaceAgent() 
    {
        if (this.replaceAgent == null) 
        {
            this.replaceAgent = new ThreadGroup("Gruppo di rimpiazzo agenti");
            return replaceAgent;
        } 
        else 
        {
            return replaceAgent;
        }
    }

    /*
     * @return  The ThreadGroup replaceAgent for the replace of Process Agent dead
     * @author marco carbone
     * 
     */
    public ThreadGroup getReplaceAgent() 
    {
        return replaceAgent;
    }

    public static ModuleFactory getInstance() 
    {
        logger = Logger.getLogger("ModuleFactory");
        
        try 
        {
            Properties prop = new Properties();
            InputStream in = ModuleFactory.class.getResourceAsStream("/org/clever/Common/Shared/logger.properties");
            prop.load(in);
            PropertyConfigurator.configure(prop);

            if (mf == null) 
            {
                inxml = ModuleFactory.class.getClass().getResourceAsStream("/org/clever/Common/Initiator/ModuleFactory/configuration_module_factory.xml");

                if (inxml == null) 
                {
                    logger.error("configuration file not found");
                } 
                else 
                {
                    fs = new FileStreamer();
                    pXML = new ParserXML(fs.xmlToString(inxml));
                    Element moduleFactories = pXML.getRootElement().getChild("modulefactory");

                    if (moduleFactories == null) 
                    {
                        logger.error("modulefactory element not found in config");
                    } 
                    else 
                    {
                        String moduleFactoryClassName = moduleFactories.getChildText("class");
                        moduleFactoryClass = Class.forName(moduleFactoryClassName);
                        mf = (ModuleFactory) moduleFactoryClass.newInstance();
                        
                        //classpath = System.getProperty("java.class.path", null);
                        logger.debug("ModuleFactory created: " + moduleFactoryClassName);
                    }
                }
            }
            else 
            {
                return mf;
            }
        }
        catch (InstantiationException ex) 
        {
            logger.error("Instantiation Error: " + ex);
        } 
        catch (IllegalAccessException ex) 
        {
            logger.error("Illegal access: " + ex);
        }
        catch (ClassNotFoundException ex) 
        {
            logger.error("Class not found: " + ex);
        }
        catch (IOException ex) 
        {
            logger.error("Error while parsing: " + ex);
        } 
        catch (NullPointerException ex) 
        {
            logger.error("Null pointer " + ex);
        }

        return mf;
    }

    public abstract void createAgent(String agentClassName);

    public abstract void createAgent(String agentClassName, String moduleName);

    public List listActiveAgents() 
    {
        //TODO Agent list must be a list of strings containing only Agents name
        ArrayList agentsNames = new ArrayList();

        for (Iterator i = Agents.listIterator(); i.hasNext();) 
        {
            agentsNames.add(i.next());
        }

        return agentsNames;
    }

    /**This function return the list of name of agents istantiate
     * 
     * @author marco carbone
     * 
     */
    public ArrayList<String> getAgents() //questa funzione mi restituisce la lista di agenti!!
    {
        return this.Agents;
    }

    public void addActiveAgents(String agentName) 
    {
        Agents.add(agentName);
    }

    /**This function return the process list of agents created
     * 
     * @author marco carbone
     * @param p  the process to add to list of process agent 
     */
    public void AddProcess(Process p) //autore: marco carbone!
    {
        processList.add(p);
    }

    
    /**
     * 
     * @return the handle to processList of agents
     */
    public List<Process> getProcessList() 
    { 
        return this.processList;
    }
}