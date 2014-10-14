/*
 * Copyright 2014 Università di Messina
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.HostManager.SOS.SOSModuleTransactional.Readers;

import org.clever.HostManager.SOS.SOSModuleTransactional.SOSmodule;
import org.w3c.dom.Node;

/**
 *
 * @author user
 */
public interface ReaderInterface {
    public void init(Node Params, SOSmodule sosModule);
    //method for collecting information needed for RegisterSensor.xml
    public void RegisterInfo();
    //method for collecting and parsing data into InsertObservation.xml
    public void parseIncomingLine(String line,String cmd);
    
    public void parseIncomingLine(String identifierObservation,String identfiercomponent,String t,Object ob);
}
