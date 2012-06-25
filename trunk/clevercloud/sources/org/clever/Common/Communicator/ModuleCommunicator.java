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
package org.clever.Common.Communicator;

import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.MessageFormatter;
import org.clever.Common.XMLTools.ParserXML;
import java.io.IOException;
import javax.management.Notification;
import org.apache.log4j.*;
import java.util.Properties;
import java.io.InputStream;

public class ModuleCommunicator implements MessageHandler {

    private CommunicationPlugin cp;
    private Agent invokerHandler;
    //private NotifyEventHandler eventHandler;
    private Logger logger;

    public ModuleCommunicator(String moduleName, String group) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        logger = Logger.getLogger("ModuleCommunicator");
        try {
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream("/org/clever/Common/Shared/logger.properties");
            prop.load(in);
            PropertyConfigurator.configure(prop);
        } catch (java.lang.NullPointerException e) {
            throw new java.lang.NullPointerException("Missing logger.properties");
        }

        try {
            FileStreamer fs = new FileStreamer();
            InputStream inxml = getClass().getResourceAsStream("/org/clever/Common/Communicator/configuration_communicator.xml");
            ParserXML pXML = new ParserXML(fs.xmlToString(inxml));
            //Class c = Class.forName(pXML.getElementContent("communicationPlugin")+"."+pXML.getElementContent("communicationPlugin"));
            //Attenzione ho dovuto integrare il nome del package all'interno del file
            //di configurazione del plugin
            Class c = Class.forName(pXML.getElementContent("communicationPlugin"));
            cp = (CommunicationPlugin) c.newInstance();
            cp.init(moduleName, group);
        } catch (IOException ex) {
            logger.error("Error while initializing Module Communicator", ex);
        }
        cp.setMessageHandler(this);
    }

    @Override
    public String handleMessage(String msg) throws CleverException {
        Object rcvd = MessageFormatter.objectFromMessage(msg);
        Object obj;
        if (rcvd instanceof MethodInvoker) {
            try {
                obj = invokerHandler.handleInvocation((MethodInvoker) rcvd);
            } catch (CleverException ex) {
                //TODO throw CleverException
                logger.error("Error on method invocation: " + ex.getMessage());
                throw ex;
            }

            return (MessageFormatter.messageFromObject(obj));

        } else {
            //eventHandler.handleNotification( ( Notification ) rcvd );
            return (null);
        }
    }

    public Object invoke(MethodInvoker method) throws CleverException {

        if (method.getHasReturn()) {
            logger.debug("Before sendRecv with has return: " + method.getModule());


            String s = cp.sendRecv(method.getModule(), MessageFormatter.messageFromObject(method));



            logger.debug("After sendRecv with has return :" + s);

            Object result = null;
            if (s != null) {
                result = MessageFormatter.objectFromMessage(s);
            }



            logger.debug("Result :" + result);
            if (result instanceof CleverException) {
                logger.debug("Exception received :");
                throw (CleverException) result;
            }
            return (result);
        } else {
            logger.debug("SendRecv without has return ");
            cp.asyncSend(method.getModule(), MessageFormatter.messageFromObject(method));

            return (null);
        }


    }

    /*
     * public void notifyEvent( String module, Notification notify ) throws
     * CleverException { cp.asyncSend( module,
     * MessageFormatter.messageFromObject( notify ) );
  }
     */
    public Agent getMethodInvokerHandler() {
        return (invokerHandler);
    }

    public void setMethodInvokerHandler(Agent invokerHandler) {
        this.invokerHandler = invokerHandler;
    }
    /*
     * public NotifyEventHandler getNotifyEventHandler() { return ( eventHandler
     * );
  }
     */
    /*
     * public void setNotifyEventHandler( NotifyEventHandler eventHandler ) {
     * this.eventHandler = eventHandler;
  }
     */
}
