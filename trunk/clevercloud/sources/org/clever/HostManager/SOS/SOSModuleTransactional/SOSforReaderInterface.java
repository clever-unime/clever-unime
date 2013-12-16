/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleTransactional;

import org.w3c.dom.Node;

/**
 *
 * @author user
 */
public interface SOSforReaderInterface {

    public void init();

    public void SOSservice(String filename_input);
    //public void SOSservice(String filename_input,String filename_output);

    //method for parsing the Configuration file with readers setting informations.
    public void parseConfiguration(Node currentNode);
    //method for parsing the Configuration's information for the readers and allocating them.

    public void parseReader(Node currentNode);
}
