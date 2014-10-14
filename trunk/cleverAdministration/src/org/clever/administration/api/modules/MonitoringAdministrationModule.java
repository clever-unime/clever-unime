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
 *  Copyright (c) 2014 Giuseppe Tricomi
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

import java.util.ArrayList;
import org.clever.Common.Exceptions.CleverException;
import org.clever.administration.annotations.HasScripts;
import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.annotations.ShellParameter;
import org.clever.administration.api.Session;

/**
 *
 * @author Giuseppe Tricomi 
 */
@HasScripts(value="MAM", script="scripts/mam.bsh", comment="Monitoring Administration module for Clever Host")
public class MonitoringAdministrationModule extends AdministrationModule{
    public MonitoringAdministrationModule (Session s)
    {
        super(s);
        
    }
    /**
     * Return CPU status of the Cloud Element selected
     * @return 
     */
    @ShellCommand
    public String GetMeasureCpu (@ShellParameter(name="name", comment="Name of the Clever Element HostManager/Probe") String name) throws CleverException
    {
        ArrayList params = new ArrayList();
        params.add(name  );
        try{
        String result="\n--------Measures--------\n"+(String)this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),
                                        "MonitorManagerAgent",
                                        "getCpuAll",
                                        params,
                                        false)+"\n-------------------------------";
        return result;
        }catch(Exception e){
            throw new CleverException(e);
        }
        
    }
    /**
     * Return the statistics of the process specified
     * @return 
     */
    @ShellCommand
    public String GetProcStatus (@ShellParameter(name="CEname", comment="Name of the Clever Element HostManager/Probe") String name,
                                @ShellParameter(name="Procname", comment="Name of the Clever Element HostManager/Probe") String Procname,
                                @ShellParameter(name="type", comment="Typer of parameter measured") String type) throws CleverException
    {
        ArrayList params = new ArrayList();
        params.add(name);
        params.add(Procname);
        params.add(type);
        try{
        String result="\n--------Measures--------\n"+(String)this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),
                                        "MonitorManagerAgent",
                                        "getProcStatus",
                                        params,
                                        false)+"\n-------------------------------";
        return result;
        }catch(Exception e){
            throw new CleverException(e);
        }
        
    }
    /**
     * Return storage statistics to Clever shell of the main partition of the HM or VM probe
     * @return 
     */
    @ShellCommand
    public String GetStorageStatus (@ShellParameter(name="CEname", comment="Name of the Clever Element HostManager/Probe") String name) throws CleverException
    {
        ArrayList params = new ArrayList();
        params.add(name);
        try{
        String result="\n--------Storage--------\n"+(String)this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),
                                        "MonitorManagerAgent",
                                        "getStorageStatus",
                                        params,
                                        false)+"\n-------------------------------";
        return result;
        }catch(Exception e){
            throw new CleverException(e);
        }
        
    }
    
}
