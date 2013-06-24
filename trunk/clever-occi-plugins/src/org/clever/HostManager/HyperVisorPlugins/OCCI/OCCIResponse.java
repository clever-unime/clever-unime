/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
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
    
    
    
    
    
    public OCCIResponse(String responses [])
    {
        for (String response : responses)
        {
            this.addOCCIStructure(response);
        }
    }
    
    
    public OCCIResponse(Iterable<String> responses )
    {
        for (String response : responses)
        {
            this.addOCCIStructure(response);
        }
    }
    
    
    
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
                attributes.put(structure.getName(), structure.getValue());
                break;
            case Location:
                locations.put(structure.getName(), structure.getValue());
                break;
        }
    }

    public Map<String, OCCIStructure> getCategories() {
        return categories;
    }

    public Map<String, OCCIStructure> getLinks() {
        return links;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Map<String, String> getLocations() {
        return locations;
    }
    
    
    
    
    
    
    
    
    
    
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Categories: \n");
        for (OCCIStructure cat : categories.values())
        {
            sb.append(cat.toString()).append("\n");
        }
        sb.append("Links: \n");
        for (OCCIStructure cat : links.values())
        {
            sb.append(cat.toString()).append("\n");
        }
        sb.append("Locations: \n");
        for (String cat : locations.values())
        {
            sb.append(cat).append("\n");
        }
        sb.append("Attributes: \n");
        for (Entry<String, String> cat : attributes.entrySet())
        {
            sb.append(cat.getKey()).append(" === ").append(cat.getValue()).append("\n");
        }
        return sb.toString();
    }
    
    
    
}
