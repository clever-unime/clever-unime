/*
 * Copyright [2014] [Università di Messina]
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
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;
import org.jdom.Element;

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
  

  public ImageManagerAgent() throws CleverException 
  {     
        super();
      
   
    
    }

 


  public void initialization() { 
    //&&logger = Logger.getLogger("ImageManagerAgent");
    logger.info("\n\nImageManagerAgent Started!\n\n"); 
    if(super.getAgentName().equals("NoName"))
    {
        super.setAgentName("ImageManagerAgent");
    }
    try 
    {
        super.start();
    } catch (CleverException ex) 
    {
        logger.error("Error in start procedure of Image Manager Agent. Message:"+ex.getMessage());
    }
    try
    {
        this.ip=(ImageManagerPlugin)super.startPlugin("./cfg/configuration_ImageManager.xml","/org/clever/HostManager/ImageManager/configuration_ImageManager.xml");
        this.ip.setOwner(this);
        logger.info("ImageManager created!");

    }catch (Exception ex) {
        logger.error("ImageManage Error: " + ex);
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
