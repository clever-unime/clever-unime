/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
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
package org.clever.HostManager.Monitor;

import java.util.logging.Level;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import java.io.InputStream;
import org.apache.log4j.Logger;

import org.clever.Common.Communicator.Agent;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;





public class MonitorAgent extends Agent
{
    private MonitorPlugin monitorPlugin;
    private Class cl;
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/Monitor/log_conf/";
    private String pathDirOut="/LOGS/HostManager/Monitor";
    //########
   

  public MonitorAgent()
  {   
      super();
      //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("MonitorAgent");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
  }
  
   @Override
    public void initialization()
    {
        if(super.getAgentName().equals("NoName"))
            super.setAgentName("MonitorAgent");
        
        try 
        {
            super.start();
            
            InputStream inxml = getClass().getResourceAsStream( "/org/clever/HostManager/Monitor/configuration_monitor.xml" );
            FileStreamer fs = new FileStreamer();
            ParserXML pars = new ParserXML( fs.xmlToString( inxml ) );
            
            cl = Class.forName( pars.getElementContent( "PluginName" ) );
            monitorPlugin = ( MonitorPlugin ) cl.newInstance();
            
            monitorPlugin.init();
         //   mc.setMethodInvokerHandler( this );
            
            monitorPlugin.setOwner(this);
            logger.info( "MonitorPlugin Created" );
            
            //agentName=pars.getElementContent( "moduleName" );
        }
        catch (CleverException ex) 
        {
            java.util.logging.Logger.getLogger(MonitorAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch( Exception e )
        {
            logger.error( "MonitorPlugin creation failed: " + e );
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
    return monitorPlugin;
  }
  
  @Override
   public void shutDown()
    {
        
    }
}
