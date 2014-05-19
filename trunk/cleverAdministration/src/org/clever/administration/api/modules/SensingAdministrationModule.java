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
 * Copyright (c) 2013 Universita' degli studi di Messina
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


package org.clever.administration.api.modules;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;
import org.clever.administration.ClusterManagerAdministrationTools;
import org.clever.administration.annotations.HasScripts;
import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.annotations.ShellParameter;
import org.clever.administration.api.Session;
import org.clever.administration.commands.SASSubscribeCommand;

/**
 * Modulo per gestire il Sensing, SAS e SOS Management
 * @author Giuseppe Tricomi 2014
 */
@HasScripts(value="SEAM", script="scripts/seam.bsh", comment="Sensing Administration Module for Clever")
public class SensingAdministrationModule extends AdministrationModule{
    Logger logger=Logger.getLogger("SensingAdministrationModule");
    public SensingAdministrationModule (Session s)
    {
        super(s);
        
    }
    /**
     * Command used to make a subscribtion for a sensor 
     * @param pathfile
     * @return
     * @throws CleverException 
     */
    //@ShellCommand
    public String SASsubscribe(@ShellParameter(name="pathfile", comment="Physical path for file that contain subscription parameter") String pathfile) throws CleverException
    {
       BufferedInputStream f = null;
       String returnResponse="";
        try {
            
            byte[] buffer = new byte[(int) new File(pathfile).length()];
            f = new BufferedInputStream(new FileInputStream(pathfile));
            f.read(buffer);
            String subscribeRequest=new String(buffer);
            
            try {
                ArrayList params = new ArrayList();
                params.add(subscribeRequest);
                returnResponse=(String)this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),
                                        "SASAgent",
                                        "subscribe",
                                        params,
                                        false);
                
            } catch (CleverException ex) {
                returnResponse="Error is occurred in subscribe process!";
                logger.error(ex.getMessage()+ ex.getInternalException().getMessage(),ex);
            }
        } catch (IOException ex) {
            returnResponse="Error is occurred in IO process!";
            logger.error(ex.getMessage(),ex);
        } finally {
            try {
                f.close();
            } catch (IOException ex) {
                returnResponse=returnResponse+"But an error is occurred in IO closing file process!";
                logger.error(ex.getMessage(),ex);
            }
        } 
        return returnResponse;
    }
    
    /**
     * Command used to make get Observation from SOSAgent
     * @param pathfile String, Physical path for file that contain subscription parameter
     * @param hostName String, Name of the HostManager connected with SOSAGENT that returns Observation requested
     * @return
     * @throws CleverException 
     */
    @ShellCommand
    public String SOSGetObservation(@ShellParameter(name="pathfile", comment="Physical path for file that contain subscription parameter") String pathfile,
                                    @ShellParameter(name="hostName", comment="Name of the HostManager connected with SOSAGENT that returns Observation requested") String hostName) throws CleverException
    {
       BufferedInputStream f = null;
       String returnResponse="";
        try {
            
            byte[] buffer = new byte[(int) new File(pathfile).length()];
            f = new BufferedInputStream(new FileInputStream(pathfile));
            f.read(buffer);
            String subscribeRequest=new String(buffer);
            
            try {
                ArrayList params = new ArrayList();
                params.add(subscribeRequest);
                returnResponse=(String)this.execSyncCommand(hostName,
                                        "SOSAgent",
                                        "getObservation",
                                        params,
                                        false);
                
            } catch (CleverException ex) {
                returnResponse="Error is occurred in getObservation process from SOS AGENT !";
                logger.error(ex.getMessage()+ ex.getInternalException().getMessage(),ex);
            }
        } catch (IOException ex) {
            returnResponse="Error is occurred in IO process!";
            logger.error(ex.getMessage(),ex);
        } finally {
            try {
                f.close();
            } catch (IOException ex) {
                returnResponse=returnResponse+"But an error is occurred in IO closing file process!";
                logger.error(ex.getMessage(),ex);
            }
        } 
        return returnResponse;
    }
    
    
}
