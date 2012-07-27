/*
 * The MIT License
 *
 * Copyright 2012 s89.
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
package org.clever.ClusterManager.VisionManager;

import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMLTools.FileStreamer;
import org.clever.Common.XMLTools.ParserXML;

/**
 *
 * @author s89
 */
public class VisionManagerAgent extends CmAgent{

    private VisionManagerPlugin VsManagerPlugin;
    private Class cl;
    
    public VisionManagerAgent() 
    {
            super();
            logger = Logger.getLogger("VisionManagerAgent");  
       
    }
    
    
    @Override
    public void initialization(){
        try{
        if(super.getAgentName().equals("NoName"))
            super.setAgentName("VisionManagerAgent");
        
        super.start();
        FileStreamer fs = new FileStreamer();     
        InputStream inxml = getClass().getResourceAsStream( "/org/clever/ClusterManager/VisionManager/configuration_VisionManagerPlugin.xml" );
        ParserXML pXML = new ParserXML( fs.xmlToString( inxml ) );
        cl = Class.forName( pXML.getElementContent( "VisionManagerPlugin" ) );
        VsManagerPlugin = ( VisionManagerPlugin ) cl.newInstance();
        logger.debug( "called init of " + pXML.getElementContent( "VisionManagerPlugin" ) );
        logger.info( "VisionManagerPlugin created " );
        VsManagerPlugin.setOwner(this);
        //VsManagerPlugin.prova();
        }catch (ClassNotFoundException ex) {
            logger.error("Error: " + ex);
        } catch (IOException ex) {
            logger.error("Error: " + ex);
        } catch (InstantiationException ex) {
            logger.error("Error: " + ex);
        } catch (IllegalAccessException ex) {
            logger.error("Error: " + ex);
        } catch (Exception ex) {
            logger.error("VisionMangerPlugin creation failed: " + ex);
        }
    }
    
    @Override
    public Class getPluginClass() {
        return cl;
    }

    @Override
    public Object getPlugin() {
        return VsManagerPlugin;
    }

    @Override
    public void handleNotification(Notification notification) throws CleverException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void shutDown() {
    }
}