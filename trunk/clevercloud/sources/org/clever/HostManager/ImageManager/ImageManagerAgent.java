/*
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
 *
 */

package org.clever.HostManager.ImageManager;

import org.clever.HostManager.ImageManagerPlugins.ImageManagerClever.ImageManager;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.LoggingPlugins.Log4J.Log4J;

/**
 * 
 * @author Valerio Barbera & Luca Ciarniello
 */

/***QUESTO AGENTE VA RIVISTO*/

public class ImageManagerAgent extends Agent {
    
    

   /* @Override
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
        
         
    
        logger.info("\n\nImageManagerAgent started! \n\n"); 
         

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

    try
    {
      //Load configuration_imagemanager.XMl
        //logger.debug("\n\n||||||!!!========££$$$$$$$$ Provo se il logger funziona ImageManagerAgent di HM!!!!\n\n"); 
      ParserXML pars = super.getconfiguration("./cfg/configuration_ImageManager.xml","/org/clever/HostManager/ImageManager/configuration_ImageManager.xml");
      /*InputStream inxml=getClass().getResourceAsStream(
              "/org/clever/HostManager/ImageManager/configuration_ImageManager.xml");
      FileStreamer fs = new FileStreamer();
      ParserXML pars = new ParserXML(fs.xmlToString(inxml));

      
      super.setAgentName(pars.getElementContent("moduleName"));
      

      Element pp=pars.getRootElement().getChild("pluginsParams");
      
      
      //Setto ImageManager come ascoltatore
     //rob this.imgManager = new ImageManager();
       this.imgManager = new ImageManager(pp);
       
       this.imgManager.setOwner(this);
       //this.imgManager.registerHost();
       
     

      cl = Class.forName(pars.getElementContent("ImageManagerPlugin"));
       logger.info("ImageManager created!");

    }/* catch (IOException io_ex) {
      logger.error("ImageManager Error: " + io_ex);

    } 
    catch (Exception ex) {
      logger.error("ImageManage Error: " + ex);
    }
    }*/
  
  //private Class cl;
  private ImageManagerPlugin ip;
  private ImageManager imgManager;
  //rob
  
  
  //########
    //Dichiarazioni per meccanismo di logging
    Logger logger3 =null;
    private String pathLogConf="/sources/org/clever/HostManager/ImageManager/log_conf/";
    private String pathDirOut="/LOGS/HostManager/ImageManager";
    //########
  

  public ImageManagerAgent() throws CleverException 
  {     
        super();
      
      //############################################
      //Inizializzazione meccanismo di logging
      logger3 = Logger.getLogger("ImageManagerAgent");
      Log4J log =new Log4J();
      log.setLog4J(logger3, pathLogConf, pathDirOut);
      //#############################################
    }

 


  public void initialization() { 
    //&&logger = Logger.getLogger("ImageManagerAgent");
      
      
            
      
      
    logger3.info("\n\nImageManagerAgent Started!\n\n"); 
    if(super.getAgentName().equals("NoName"))
    {
        super.setAgentName("ImageManagerAgent");
    }
    try 
    {
        super.start();
    } catch (CleverException ex) 
    {
        logger3.error("Error in start procedure of Image Manager Agent. Message:"+ex.getMessage());
    }
    try
    {
        this.ip=(ImageManagerPlugin)super.startPlugin("./cfg/configuration_ImageManager.xml","/org/clever/HostManager/ImageManager/configuration_ImageManager.xml");
        this.ip.setOwner(this);
        logger3.info("ImageManager created!");

    }catch (Exception ex) {
        logger3.error("ImageManage Error: " + ex);
    }
  }


  public ImageManager getImgManager() {
    return this.imgManager;
  }



  @Override
  public Class getPluginClass() {
    return cl;
  }

  @Override
  public Object getPlugin() {
    return this.pluginInstantiation;
  }
  
   @Override
   public void shutDown()
    {
        
    }
   
   
}
