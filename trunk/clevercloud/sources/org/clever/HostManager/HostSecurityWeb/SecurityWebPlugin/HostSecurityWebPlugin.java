/*
 * The MIT License
 *
 * Copyright 2015 clever.
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
package org.clever.HostManager.HostSecurityWeb.SecurityWebPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.OpenAm.httpResp;
import org.jdom.Element;
import org.clever.HostManager.HostSecurityWeb.IHostSecurityWebPlugin;
import org.json.JSONException;

/**
 *
 * @author clever
 */
public class HostSecurityWebPlugin implements IHostSecurityWebPlugin {

    private Agent owner;
    private Logger logger = Logger.getLogger("HostSecurityWebPlugin");
    private org.clever.Common.OpenAm.Openam mOpenAmClient;
    
    @Override
    public void setOwner(Agent owner) {
        this.owner = owner;
    }

    @Override
    public void init(Element params, Agent owner) throws CleverException {
        //fondamentale
        this.setOwner(owner);
        logger.debug("INIZIO init() di Openam");
        try{
        
            mOpenAmClient = new org.clever.Common.OpenAm.Openam("","","");
            logger.debug("istanziato client openam");
        }
        catch(Exception ex){
            logger.error(ex);
        }

        //ricavo queste variabili dal file configuration_secutiryWeb.xml 
        mOpenAmClient.setOpenamHost(params.getChildText("openamHost"));
        mOpenAmClient.setPort(params.getChildText("port"));
        mOpenAmClient.setDeployUrl(params.getChildText("deployUrl"));

        //setto la variabile AUTH_URL con l'indirizzo completo di Openam
        mOpenAmClient.setAUTH_URL(mOpenAmClient.getOpenamHost(), mOpenAmClient.getPort(), mOpenAmClient.getDeployUrl());
        logger.debug("\n Openam AUTH_URL : " + mOpenAmClient.getAUTH_URL());

        //ricavo queste variabili dal file configuration_secutiryWeb.xml 
        //ed inizializzo le variabili locali
        mOpenAmClient.setUsername(params.getChildText("user"));
        mOpenAmClient.setPassword(params.getChildText("password"));
        
        logger.debug("Ho acquisito le seguenti credenziali: \n");
        logger.debug("user:  " + mOpenAmClient.getUsername());
        logger.debug(" pass:  " + mOpenAmClient.getPassword());

        //###############################
        Element listCmds = params.getChild("listCmds");

        if (listCmds == null) {
            logger.error("listCmds element not found in config");
        } else {
            List commandsList = listCmds.getChildren("cmd");
            ArrayList<String> commands = new ArrayList<String>();

            for (Object commandO : commandsList) {
                Element command = (Element) commandO;
                String commandName = command.getText();
                logger.debug("commandName " + commandName);

                if (commandName == null) {
                    logger.error("Command name element not found in config");
                    continue;
                }

                commands.add(commandName);

            }
            mOpenAmClient.setCmdAutho(commands);
        }

        logger.debug("Ho una lista di comandi con n°: " + mOpenAmClient.getCmdAutho().size());

        //###############################
       /*
        httpResp risp=null;
         try {
            
         //effettuo un test sul servizio di autenticazione di Openam
            
         risp= mOpenAmClient.simpleAuthentication(mOpenAmClient.getUsername(), mOpenAmClient.getPassword());
            
                 
         } catch (JSONException ex) {
         logger.error("Errore in simpleAuthentication()" + ex);
         } catch (IOException ex) {
            logger.error("Errore in simpleAuthentication()" + ex);
        }

         if (risp.getTokenId() != "") {
         //se ho ricevuto un token
         this.owner.setPluginState(true);
         logger.debug("Openam Service is OK !!!");
         logger.debug("Token is: "+risp.getTokenId());
         } else {
         //se non ho ricevuto un token
         this.owner.setPluginState(false);
         logger.error("Openam Service is KO !!!");
         }

         //setto il token nell'oggetto client
         mOpenAmClient.setTokenID(risp.getTokenId());
    
        
         //#####
         //esperimento
        
         List paramss = null;
         String moduleName = null;
        
         Boolean prova = mOpenAmClient.authorizeUser(risp.getTokenId(), "listhm", moduleName, paramss);
         logger.debug("cazzo "+prova);
        
         /*
         //###################################################################
         // faccio un test sulla validazione del token appena ricevuto
        
         httpResp risp2= new httpResp();
        
         try {
         risp2 = client.tokenValidation(client.getTokenID());
         } catch (IOException ex) {
         logger.error("Errore in tokenValidation() "+ex);
         }
        
         logger.debug("tokenValidation: " + risp2.getTokenValidity());
        
         //###################################################################
         // disabilito il token appena ricevuto 
       
         httpResp risp3= new httpResp();
         try {
         risp3=client.tokenLogout(client.getTokenID());
         } catch (IOException ex) {
         logger.error("Errore in tokenLogout() "+ex);
         }
        
         //###################################################################
         // ri faccio un test sulla validazione del token appena disabilitato
         httpResp risp4= new httpResp();
        
         try {
         risp4 = client.tokenValidation(client.getTokenID() );
         } catch (IOException ex) {
         logger.error("Errore in tokenValidation() "+ex);
         }
        
         logger.debug("tokenValidation: " + risp4.getTokenValidity());
       
         //###################################################################
           
         //faccio un test sulla simple Authorization
         httpResp risp5= new httpResp();
         String uri = "listhm";
         String verb[] ={"POST","GET"};
        
         try {
             
         risp5 = client.simpleAuthorization(uri, verb[1]);
         } catch (IOException ex) {
         logger.error("Errore in simpleAuthorization() "+ex);
         }
        
         logger.debug("simpleAuthorization: " + risp5.getUriAutho());
        
         */
     //##################################    
        //this.owner.setPluginState(true);
        logger.debug("FINE init() di Openam");
     //##################################   

    }//init()

    public boolean authorize(String token, String moduleName, String methodName) throws CleverException {

        if (token == null || moduleName == null || methodName == null) {
            logger.error("One or more parameters in HostSecurityWebPlugin.authorize are null.\n" +
                    "token: " + token +
                    "\nmoduleName: " + moduleName + 
                    "\nmethodName: " + methodName);
            return false;
        }
        logger.debug("call to OpenAM client authorize");
        return mOpenAmClient.authorizeUser(token, moduleName, methodName, null);
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDescription() {
        return ("This plugin provides an integration with OpenStack Object Storage Swift ");
    }

    @Override
    public void shutdownPluginInstance() {

    }
}
