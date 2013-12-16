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
