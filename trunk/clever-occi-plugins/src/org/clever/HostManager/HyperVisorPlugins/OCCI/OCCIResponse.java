/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI;

import com.google.common.collect.Maps;
import java.util.Map;
import static org.clever.HostManager.HyperVisorPlugins.OCCI.OCCIStructureTypes.Category;

/**
 *
 * @author maurizio
 */
public class OCCIResponse {
    final private Map<String, OCCIStructure> categories = Maps.newHashMap();
    final private Map<String, OCCIStructure> links = Maps.newHashMap();
    final private Map<String, String> attributes = Maps.newHashMap();
    final private Map<String, String> locations = Maps.newHashMap();
    
    public void addOCCIStructure(String toparse)
    {
        OCCIStructure structure = new OCCIStructure(toparse);
        switch(structure.getType())
        {
            case Category:
                categories.put(structure.getName(), structure);
                break;
            case Link:
                links.put(structure.getName(), structure);
                break;
            case Attribute:
                attributes.put(structure.getName(), structure.get(toparse));
                break;
        }
    }
    
    
    
}
