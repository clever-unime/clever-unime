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

package org.clever.HostManager.ImageManager;

import org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.ImageManager;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.jdom.Element;

/**
 * 
 * @author Valerio Barbera & Luca Ciarniello
 */

/***QUESTO AGENTE VA RIVISTO, A CHE SERVE IL COSTRUTTORE CON PARAMETRO?? QUALE DEVE ESSERE USATO??*/

public class ImageManagerAgent extends Agent {
    
    //########
    //Dichiarazioni per meccanismo di logging
    Logger logger =null;
    private String pathLogConf="/sources/org/clever/HostManager/ImageManager/log_conf/";
    private String pathDirOut="/LOGS/HostManager/ImageManager";
    //########
    
     private Class cl;
  //private ModuleCommunicator mc;
  private ImageManager imgManager;
  //rob

    @Override
    public void initialization()
    {
        if(super.getAgentName().equals("NoName"))
        {
            
            super.setAgentName("ImageManagerAgent");
        }
        try 
        {
            super.start();
        } catch (CleverException ex) 
        {
            java.util.logging.Logger.getLogger(ImageManagerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         
    
        logger.info("\n\n||||||!!!========££$$$$$$$$ Provo se il logger funziona ImageManagerAgent di HM!!!!\n\n"); //OK FUNONZIAaaaa!!!
         

    //Load properties from XML file
 /*  try
    {
      Properties prop = new Properties();
      InputStream in = getClass().getResourceAsStream("/org/clever/Common/Shared/logger.properties");
      prop.load(in);
      PropertyConfigurator.configure(prop);

    } catch (IOException e) {
      logger.error("Missing logger.properties");
    }
*/
    try
    {
      //Load configuration_imagemanager.XMl
        logger.info("\n\n||||||!!!========££$$$$$$$$ Provo se il logger funziona ImageManagerAgent di HM!!!!\n\n"); //OK FUNONZIAaaaa!!!
     
      InputStream inxml=getClass().getResourceAsStream(
              "/org/clever/HostManager/ImageManager/configuration_ImageManager.xml");
      FileStreamer fs = new FileStreamer();
      ParserXML pars = new ParserXML(fs.xmlToString(inxml));

      //Instantiate ModulCommunicator
      //agentName=pars.getElementContent("moduleName");
      super.setAgentName(pars.getElementContent("moduleName"));
      

      Element pp=pars.getRootElement().getChild("pluginsParams");
      
      
      //Setto ImageManager come ascoltatore
     //rob this.imgManager = new ImageManager();
       this.imgManager = new ImageManager(pp);
       this.imgManager.setMC(mc);
       this.imgManager.setOwner(this);
       //this.imgManager.registerHost();
       
     

      cl = Class.forName(pars.getElementContent("ImageManagerPlugin"));
       logger.info("ImageManager created!");

    } catch (IOException io_ex) {
      logger.error("ImageManager Error: " + io_ex);

    } catch (Exception ex) {
      logger.error("ImageManage Error: " + ex);
    }
    }
  
 
  

  public ImageManagerAgent() 
  {     
        super();
        
     // init();
      //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("ImageManagerAgent");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
        
    
    }

  public ImageManagerAgent(ConnectionXMPP conn) {
    super();
    //init();
    this.imgManager.setXMPP(conn);
    
    //############################################
      //Inizializzazione meccanismo di logging
      logger = Logger.getLogger("ImageManagerAgent");
      Log4J log =new Log4J();
      log.setLog4J(logger, pathLogConf, pathDirOut);
      //#############################################
    
  }

  private void init() { //va tolto l'underscore dopo la prova
       //loggerInstantiator2 = new LoggerInstantiator();
    logger = Logger.getLogger("ImageManagerAgent");
    
    logger.info("\n\n||||||!!!========££$$$$$$$$ Provo se il logger funziona ImageManagerAgent di HM!!!!\n\n"); //OK FUNONZIAaaaa!!!
         

    //Load properties from XML file
 /*  try
    {
      Properties prop = new Properties();
      InputStream in = getClass().getResourceAsStream("/org/clever/Common/Shared/logger.properties");
      prop.load(in);
      PropertyConfigurator.configure(prop);

    } catch (IOException e) {
      logger.error("Missing logger.properties");
    }
*/
    try
    {
      //Load configuration_imagemanager.XMl
        logger.info("\n\n||||||!!!========££$$$$$$$$ Provo se il logger funziona ImageManagerAgent di HM!!!!\n\n"); //OK FUNONZIAaaaa!!!
     
      InputStream inxml=getClass().getResourceAsStream(
              "/org/clever/HostManager/ImageManager/configuration_ImageManager.xml");
      FileStreamer fs = new FileStreamer();
      ParserXML pars = new ParserXML(fs.xmlToString(inxml));

      //Instantiate ModulCommunicator
      //agentName=pars.getElementContent("moduleName");
      super.setAgentName(pars.getElementContent("moduleName"));
      

      Element pp=pars.getRootElement().getChild("pluginsParams");
      
      
      //Setto ImageManager come ascoltatore
     //rob this.imgManager = new ImageManager();
       this.imgManager = new ImageManager(pp);
       this.imgManager.setMC(mc);
       //this.imgManager.registerHost();
       cl = Class.forName(pars.getElementContent("ImageManagerPlugin"));
      
      logger.info("ImageManager created!");

    } catch (IOException io_ex) {
      logger.error("ImageManager Error: " + io_ex);

    } catch (Exception ex) {
      logger.error("ImageManage Error: " + ex);
    }
  }

  public ImageManager getImgManager() {
    return this.imgManager;
  }


  @Override
  public Object handleInvocation(MethodInvoker method) {
    int i = 0;
    Method mthd;
    Class[] par;
    Object output;
    Object[] input = null;
    List params = method.getParams();
    if (params != null)
    {
      par = new Class[params.size()];
      input = new Object[params.size()];

      for (i = 0; i < params.size(); i++)
      {
        par[i] = params.get(i).getClass();
        input[i] = params.get(i);
      }
    }
    else
      par = null;

    try
    {
      mthd = cl.getMethod(method.getMethodName(), par);
      if (method.getHasReturn())
      {
        logger.debug("getHasReturn TRUE");
        output = (Object) mthd.invoke(this.imgManager, input);
      }
      else
      {
        logger.debug("getHasReturn FALSE");
        mthd.invoke(this.imgManager, input);
        output = null;
      }

      if (output != null)
      {
        logger.debug("ImageManagerAgent " + method.getMethodName() + "Result: " + output.toString());
        return output;
      }

      logger.debug("Out NULL");
      return output;

    } catch (IllegalAccessException ex) {
      logger.debug(ex.getMessage());
      return false;

    } catch (IllegalArgumentException ex) {
      logger.debug(ex.getMessage());
      return false;

    } catch (InvocationTargetException ex) {
      logger.debug(ex.getMessage());
      return false;

    } catch (NoSuchMethodException ex) {
      logger.debug(ex.getMessage());
      return false;

    } catch (SecurityException ex) {
      logger.debug(ex.getMessage());
      return false;
    }
  }

  @Override
  public Class getPluginClass() {
    return cl;
  }

  @Override
  public Object getPlugin() {
    return imgManager;
  }
  
   @Override
   public void shutDown()
    {
        
    }
}
