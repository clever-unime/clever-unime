/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clever.Common.VEInfo.VEState;
import org.clever.Common.XMLTools.ParserXML;

/**
 *
 * @author maurizio
 */
public class TestPlugin {
    
    static public void main (String [] argv) throws Exception
    {
            TestPlugin me = new TestPlugin();
            me.start(argv[0]);
    
        
    }
    protected void start(String filename) throws Exception
            {
                    HvOCCI plugin = new HvOCCI();
                    File cfgLocalFile = new File(filename);
                    InputStream inxml = null;
                    try {
                        inxml = new FileInputStream(cfgLocalFile);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(TestPlugin.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ParserXML pXML = new ParserXML(inxml);
                    
                    plugin.init(pXML.getRootElement().getChild("pluginParams"), null);
                   
                    
                    for(VEState ve :  plugin.listVms())
                    {
                        if(ve!=null)
                            System.out.println(ve);
                    }
             
//                    for(VEState ve :  plugin.listRunningVms())
//                    {
//                        if(ve!=null)
//                        System.out.println(ve);
//                    }
                    
                   /* if(plugin.destroyVm("from-curl"))
                    {
                        System.out.println("Tutto ok ");
                    }
                    else
                    {
                        System.err.println("Errore");
                    }*/
                    //plugin.startVm("from-curl");
                    
                    
            }
}
