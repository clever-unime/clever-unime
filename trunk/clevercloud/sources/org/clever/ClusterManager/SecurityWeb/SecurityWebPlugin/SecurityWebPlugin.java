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
package org.clever.ClusterManager.SecurityWeb.SecurityWebPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.SecurityWeb.ISecurityWebPlugin;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.OpenAm.Openam;
import org.jdom.Element;

/**
 *
 * @author clever
 */
public class SecurityWebPlugin implements ISecurityWebPlugin {

    private Agent owner;
    private Logger logger = Logger.getLogger("SecurityWebPlugin");
    private Openam mOpenAmClient;
    private Calendar mLastRelease;
    private String mCurrentToken;
    private final static int TOKEN_TIMEOUT = 30 * 60 * 1000; //30 min

    @Override
    public void setOwner(Agent owner) {
        this.owner = owner;
    }

    @Override
    public void init(Element params, Agent owner) throws CleverException {
        //fondamentale
        this.setOwner(owner);
        logger.debug("INIZIO init() di Openam");
        mCurrentToken = "";
        try {            
            mOpenAmClient = new org.clever.Common.OpenAm.Openam("", "", "");
            logger.debug("istanziato client openam");
        } catch (Exception ex) {
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
        //##################################    
        //this.owner.setPluginState(true);
        logger.debug("FINE init() di Openam");
        //##################################   

    }//init()

    public String authenticate(String username, String password) throws CleverException, IOException {

        logger.debug("call to OpenAM client authenticate");
        Calendar now = GregorianCalendar.getInstance();
        if (mLastRelease == null || (now.getTimeInMillis() - mLastRelease.getTimeInMillis()) >= TOKEN_TIMEOUT) {
            String token = mOpenAmClient.authenticate(username, password);
            if (token != null && !token.isEmpty()) {
                mLastRelease = GregorianCalendar.getInstance();
                mCurrentToken = token;
                logger.debug("Token updated!");
            }
        }
        return mCurrentToken;
    }

    public boolean authorize(String token, String moduleName, String methodName) throws CleverException {

        if (token == null || moduleName == null || methodName == null) {
            logger.error("One or more parameters in SecurityWebPlugin.authorize are null.\n"
                    + "token: " + token
                    + "\nmoduleName: " + moduleName
                    + "\nmethodName: " + methodName);
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
