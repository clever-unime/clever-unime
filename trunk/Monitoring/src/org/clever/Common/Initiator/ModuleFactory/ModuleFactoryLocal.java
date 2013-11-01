/*
 * The MIT License
 *
 * Copyright 2011 Alessio Di Pietro.
 * Copyright 2011 Marco Carbone
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
package org.clever.Common.Initiator.ModuleFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.clever.Common.Communicator.Agent;

/**
 *
 * @author alessiodipietro
 */
public class ModuleFactoryLocal extends ModuleFactory 
{
    public void ModuleFactoryLocal()
    {
        logger = Logger.getLogger( "ModuleFactoryLocal" );
        
        try 
        {
            Properties prop = new Properties();
            InputStream in = ModuleFactory.class.getResourceAsStream( "/org/clever/Common/Shared/logger.properties" );
            prop.load(in);
            PropertyConfigurator.configure(prop);
        } 
        catch (IOException ex) 
        {
            logger.error("logger.properties not found");
        }
    }

    /**@author marco carbone
     * 
     * This function returns the last piece of the string <class> inside the xml 
     * configuration file and use it to name the agent, if not provided
     * 
     * @param agentClassName the path of class agent to istantiate
     * @return the last part of string AgentClassName (name of Class agent)
     */
    public String CreateAgentName(String agentClassName)
    {       
        int index = agentClassName.lastIndexOf("."); 
        String agentName = agentClassName.substring(index+1, agentClassName.length()); 
        return agentName;
    }   
    
    @Override
    public void createAgent(String agentClassName) 
    {
        try 
        {
            Class agentClass = Class.forName(agentClassName);
            //addActiveAgents(agent.getModuleName)
            Agent agent=(Agent)agentClass.newInstance();
            this.addActiveAgents(this.CreateAgentName(agentClassName));
            agent.setAgentName("NoName"); //delego la funzione initialization dell'agente ad assegnare il nome di default
            try
            {
                agent.initialization();
            } 
            catch (Exception ex) 
            {
                //java.util.logging.Logger.getLogger(ModuleFactoryLocal.class.getName()).log(Level.SEVERE, null, ex);
                logger.error("Problemi nell'istanza dell'agente "+agentClassName +" " +ex);
            }
            
        } catch (InstantiationException ex) {
            logger.error("Instantiation exception: " + ex);
        } catch (IllegalAccessException ex) {
            logger.error("Illegal access: "+ex);
        } catch (ClassNotFoundException ex) {
            logger.error("Class not found: "+ ex);
        }
    }

    @Override
    public void createAgent(String agentClassName, String agentName) 
    {
        try 
        {
            /*Class[] stringArgsClass = new Class[] { String.class};
            Object[] stringArgs = new Object[] { agentName };
            Constructor agentConstructor;
            Class agentClass=Class.forName(agentClassName);
            agentConstructor=agentClass.getConstructor(stringArgsClass);
            agentConstructor.newInstance(stringArgs);
            this.addActiveAgents(agentName);*/
            Class agentClass = Class.forName(agentClassName);
            Agent agent=(Agent)agentClass.newInstance();
            agent.setAgentName(agentName); //setto subito il nome dell'agente
            this.addActiveAgents(agent.getAgentName());
           
            try
            {
                agent.initialization();
            } 
            catch (Exception ex) 
            {
                //java.util.logging.Logger.getLogger(ModuleFactoryLocal.class.getName()).log(Level.SEVERE, null, ex);
                logger.error("Problemi nell'istanza dell'agente "+agentClassName +" " +ex);
            }
            
        } catch (InstantiationException ex) {
            logger.error("Error instantiating agent: "+ex);
        } catch (IllegalAccessException ex) {
            logger.error("Error instantiating agent: "+ex);
        } catch (IllegalArgumentException ex) {
            logger.error("Error instantiating agent: "+ex);
        /*} catch (InvocationTargetException ex) {
            logger.error("Error instantiating agent: "+ex);
        } catch (NoSuchMethodException ex) {
            logger.debug("Only default constructor for this agent: "+ex);
            this.createAgent(agentClassName);*/
        
        } catch (SecurityException ex) {
            logger.error("Error instantiating agent: "+ex);
        } catch (ClassNotFoundException ex) {
            logger.error("Error instantiating agent: "+ex);
        }
        

    }
    
    
}
