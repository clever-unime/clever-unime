/*
 * The MIT License
 *
 * Copyright (c) 2014 Giovanni Volpintesta
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
package org.clever.ClusterManager.FederatorManager;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Plugins.RunnerPlugin;
import org.clever.Common.XMPPCommunicator.ConnectionXMPP;

/**
 *
 * @author Giovanni Volpintesta
 */
public interface FederationManagerPlugin extends RunnerPlugin{
    public void setOwner (Agent owner);
    public void setLogger (Logger logger);
    public void setConnection (ConnectionXMPP conn);
    public void setDomain (String domain);
    public void setDefaultTimeout (long t);
    public void setAttempts (int n);
    
    public String[] addAsActiveCMandReply (String domain, String nick) throws CleverException;
    public void initAsActive ();

    //public FederationReply forwardCommand (final String agent, final String command, final Boolean hasReply, final ArrayList params) throws CleverException;
    //public FederationReply forwardCommandWithTimeout (final String agent, final String command, final Boolean hasReply, final ArrayList params, Long timeout) throws CleverException;
    public Object forwardCommandToDomain (final String domain, final String agent, final String command, final Boolean hasReply, final ArrayList params) throws CleverException;
    public Object forwardCommandToDomainWithTimeout (final String domain, final String agent, final String command, final Boolean hasReply, final ArrayList params, Long timeout) throws CleverException;
    public ArrayList<String> getFederatedDomains() throws CleverException;
    public String getLocalDomainName();
    
    public FederatorDataContainer returnVM(String VM,String fedid)throws CleverException;
    public void deleteMigratedVM(String VMName);
    public Boolean createVM4Migration(FederatorDataContainer fdc) throws CleverException;
}
