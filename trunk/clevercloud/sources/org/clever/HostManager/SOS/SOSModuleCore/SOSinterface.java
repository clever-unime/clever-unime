/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleCore;

import java.util.ArrayList;

/**
 *
 * @author user
 */
public interface SOSinterface {

    public void init();

    public ArrayList<String> SOSservice(String filename_input);

    public void SOSservice(String filename_input, String filename_output);
}
