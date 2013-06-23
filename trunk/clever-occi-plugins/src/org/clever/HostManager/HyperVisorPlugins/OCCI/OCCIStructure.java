/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;





enum OCCIStructureTypes {
    Category,
    Link,
    Attribute,
    Location
}


/** Class representing a OCCI category or link
 *
 * @author maurizio
 */
public class OCCIStructure  {
    
     Logger logger = Logger.getLogger(OCCIStructure.class);
    
    
    
    static public boolean putKeyValue(Map<String,String> target, String toparse)
    {
        final Pattern p = Pattern.compile(" *(.*) *= *\"(.*)\"");
        Matcher m = p.matcher(toparse);
        boolean r;
        if(r=m.find())
        {
            target.put(m.group(1), m.group(2));
        }
        return r;
        
        
    }
    
    final private Map<String,String> contents;
    final private String name ;
    final private String value ;
    final private OCCIStructureTypes type;
    
    
    public OCCIStructure(String toparse)
    {
        contents = Maps.newHashMap();
        
        Pattern p = Pattern.compile("^(.*?): *");
        Matcher m = p.matcher(toparse);
        
        
        if(m.find())
        {
                String tipo = m.group(1);
                logger.debug("Tipo: " + tipo);

                if(tipo.equals("Category"))
                {
                    type = OCCIStructureTypes.Category;
                }
                else if(tipo.equals("Link"))
                {
                    type = OCCIStructureTypes.Link;
                }
                else if(tipo.equals("X-OCCI-Attribute"))
                {
                    type = OCCIStructureTypes.Attribute;
                }
                else if(tipo.equals("X-OCCI-Location"))
                {
                    type = OCCIStructureTypes.Location;
                }
                else
                {
                    type = null;
                    name = null;
                    value=null;
                    return; //TODO: manage parse error
                }
                
                
                
                String parsed = m.replaceAll("");
                //toparse = toparse.replaceAll("^.*: ", ""); //TODO: improve it
                
                
                switch (type)
                {
                    case Category:
                    case Link:
                        p = Pattern.compile("^(.*?);(.*)");
//                        ^(.*): (.*?);
                        
                        m = p.matcher(parsed);
                        m.find();
                        name = m.group(1);
                        value = m.group(2);
                        for (String token : Splitter.
                                            on(";").
                                            trimResults().
                                            split(value))

                            {

                                putKeyValue(contents, token);
                            }
                        break;
                    case Attribute:
                    case Location:
                        p = Pattern.compile(" *(.*) *= *\"?(.*)\"?");
                        m = p.matcher(parsed);
                        m.find();
                        name = m.group(1);
                        value = m.group(2);
                        break;
                    default:
                        name=null;
                        value=null;
                        
                }
                

       

              
        } //TODO: manage error parsing
        else
        {
            name=null;
            value=null;
            type=null;
        }
        
    }
    
    
    public String get(String cat)
    {
        return contents.get(cat);
    }

    public String getName() {
        return name;
    }

    public OCCIStructureTypes getType() {
        return type;
    }
    
    
    
    
    
    @Override
    public String toString()
    {
        final Joiner.MapJoiner mapJoiner = Joiner.on('&').withKeyValueSeparator("=");
        return  type + " " + name + " ---- " + mapJoiner.join(contents);
    }
    
            
    
    
}
