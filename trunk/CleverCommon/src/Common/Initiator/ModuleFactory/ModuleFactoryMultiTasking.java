/*
 * The MIT License
 *
 * Copyright 2012 Marco Carbone
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/** This class is a Plug-in that is istantiate by the method of the abstract class ModuleFactory.
 *  This plugin launches agents in separate processes. 
 *  Eventually enable the automatic recovery process agents terminated prematurely.
 *
 * @author marco carbone
 */
public class ModuleFactoryMultiTasking extends ModuleFactory 
{
    private String classpath = null;
    private Map<String,MonitorReplaceAgentDead> monitorH = new HashMap<String, MonitorReplaceAgentDead>(); //HashMap in cui memorizzo tutti gli oggetti monitor per il controllo dei thread di ripristino agenti.
    
    
    public void ModuleFactoryMultiTasking() //questo costruttore nn viene usato!
    {
        try 
        {
            Properties prop = new Properties();
            InputStream in = ModuleFactory.class.getResourceAsStream("/org/clever/Common/Shared/logger.properties");
            prop.load(in);
            PropertyConfigurator.configure(prop);
        } 
        catch (IOException ex) 
        {
            logger.error("logger.properties not found");
        }
         logger = Logger.getLogger("ModuleFactoryMultiTasking"); 
    }    
    
    /**This function returns the last piece of the string <class> inside the xml 
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
            classpath = this.getClasspath();            
            String librarypath = "-Djava.library.path="+System.getProperty( "java.library.path" );
            
            Runtime runtime = Runtime.getRuntime();       
            Process process = runtime.exec(new String[]{"java", "-cp", classpath, librarypath,  agentClassName, agentClassName}); //lancio il processo
            logger.info("New process launched!");
            
            String agentName2 = CreateAgentName(agentClassName); //estrapolo il nome della classe agente dal path relativo
            
            if((monitorH.isEmpty()) || (!monitorH.containsKey(agentName2))) //effettuo un controllo sulla chiave
            {//inizializzo l'oggetto monitor                
              monitorH.put(agentName2, new MonitorReplaceAgentDead(mf.getInterval_time(), mf.getNumLaunch(), System.currentTimeMillis()));                
            }
            this.AddProcess(process); // aggiungo questo processo in lista! Questa lista mi serve x chiudere tutti i processi lanciati! 
             
            Runnable r = new ManageListActiveAgents_Thread(process, agentName2, getAgents());            
              
            Thread th = new Thread(r); //tutti i thread x il controllo del nome in listactiveagents appartengono ad un gruppo di thread
            th.setDaemon(true); //questo è fondamentale affinché i thread vengano interrotti anche se in esecuzione durante la fase di shutdown
            th.start();           
            
            if(ModuleFactory.getActiveReplaceAgent())
            {
                logger.info("Thread ReplacementAgentDead abilitated");
                
                Runnable r2 = new ReplacementAgentDead(ModuleFactory.getInstance(), process, this.monitorH, agentClassName, agentName2);                 
                Thread th2 = new Thread(this.getIstanceReplaceAgent(), r2);
                th2.setDaemon(true); //questo è fondamentale affinché i thread vengano interrotti anche se in esecuzione durante la fase di shutdown
                th2.start(); //questo thread va interrotto nella procedura di shutdown!!!
                
                logger.info("Thread for replacement started");
             }
        }
        catch (IOException ex) 
        {
            
        }
    }   

    @Override
    public void createAgent(String agentClassName, String agentName) 
    {
        try 
        {
            classpath = this.getClasspath();
            String librarypath = "-Djava.library.path="+System.getProperty( "java.library.path" );
                                    
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[]{"java", "-cp", this.classpath/*,"-Djava.library.path="*/, librarypath, agentClassName, agentClassName, agentName}); //lancio l'agente in un processo separato e passo come parametro al main il nome della classe agente e il nome da dare all'agente lanciato!
            logger.info("New process launched!");
            
            if((monitorH.isEmpty()) || (!monitorH.containsKey(agentName))) //effettuo un controllo sulla chiave
            { //inizializzo l'oggetto monitor!
                monitorH.put(agentName, new MonitorReplaceAgentDead(mf.getInterval_time(), mf.getNumLaunch(), System.currentTimeMillis()));                       
            }
            this.AddProcess(process);            
            
            Runnable r = new ManageListActiveAgents_Thread(process, agentName, this.getAgents());            
            Thread th = new Thread(r);
            th.setDaemon(true);
            th.start();
            
            if(ModuleFactory.getActiveReplaceAgent())
            {
                logger.info("Thread ReplacementAgentDead abilitated");
                
                Runnable r2 = new ReplacementAgentDead(ModuleFactory.getInstance(), process, this.monitorH, agentClassName, agentName); //attenzione quì dentro va passato l'elemento corrispondente alla chiave nell'hashMap!
                Thread th2 = new Thread(this.getIstanceReplaceAgent(), r2);
                th2.setDaemon(true);
                th2.start(); 
                
                logger.info("Thread for replacement started");
            }
        }
        catch (IOException ex) 
        {
            
        }
    }
}