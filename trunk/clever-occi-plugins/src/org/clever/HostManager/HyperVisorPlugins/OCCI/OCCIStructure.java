/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.HyperVisorPlugins.OCCI;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import static org.clever.HostManager.HyperVisorPlugins.OCCI.OCCIStructureTypes.Category;





enum OCCIStructureTypes {
    Category,
    Link,
    Attribute,
    Location,
    UKNOWN
}


/** Class representing a OCCI category or link
 *
 * @author maurizio
 */
public class OCCIStructure  {
    
     Logger logger = Logger.getLogger(OCCIStructure.class);
    
    
    
    static public boolean putKeyValue(Map<String,String> target, String toparse)
    {
        Entry<String,String> e = getKeyValue(toparse);
        if(e!=null)
        {
            target.put(e.getKey(), e.getValue());
            return true;
        }
        
        return false;
        
        
    }
    
    
    static public Entry<String,String> getKeyValue(String toparse)
    {
        final Pattern p = Pattern.compile(" *(.*?) *= *\"?([^\"]*)");
        Matcher m = p.matcher(toparse);
        
        if(m.find())
        {
            return new AbstractMap.SimpleEntry<String,String>(m.group(1), m.group(2));
            
        }
        else
        {
            return null;
        }
    }
    
    
    
    
    
    final private Map<String,String> contents;
    final private String name ;
    final private String value ;
    final private OCCIStructureTypes type;
    
    
    public OCCIStructure(String toparse)
    {
        contents = Maps.newHashMap();
        
        
        
        
        /*
         * Gruppo1: tipo (Category , Link, X-OCCI-ATTRIBUTE, X-OCCI-LOCATION)
         * Gruppo2: per Category e Link nome (network, compute, ...), null per altri
         * Gruppo3: per Link (o comunque dove il nome e' nella forma </nome/idxxxxxx>) l'idxxxxxx ; null per gli altri
         * Gruppo4: tutto quello che c'e' dopo il ';' (per Location e Category) o il ':' per X-OCCI-* 
         */
        Pattern p = Pattern.compile("^(.*?): *(?:(?:</)?([^/]+)(?:/(.*)>)?;)?(.*)");
        Matcher m = p.matcher(toparse);
        
        
        if(m.find())
        {
                String tipo = m.group(1);
                //logger.debug("Tipo: " + tipo);

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
                    type = OCCIStructureTypes.UKNOWN;
                    name = "UKNOWN";
                    value="UKNOWN";
                    return; //TODO: manage parse error
                }
                
                
                
                
                //toparse = toparse.replaceAll("^.*: ", ""); //TODO: improve it
                
                
                switch (type)
                {
                    case Category:
                    case Link:
                       
                        name = m.group(2);
                        value = m.group(3);
                        for (String token : Splitter.
                                            on(Pattern.compile("[; ]")).
                                            trimResults().
                                            split(m.group(4)))

                            {

                                putKeyValue(contents, token);
                            }
                        break;
                    case Location:
                            name = m.group(4);
                            value = null;
                            break;
                    case Attribute:
                            Entry<String,String> e = OCCIStructure.getKeyValue(m.group(4));
                            logger.debug("Attribute: " + e.getKey() + " value: " + e.getValue());
                            name = e.getKey();
                            value = e.getValue();
                            break;
//                        }
                        //logger.error("Parse error : " + parsed);
                        
                    default:
                        name="UKNOWN";
                        value="UKNOWN";
                        
                }
                

       

              
        } //TODO: manage error parsing
        else
        {
            name="UKNOWN";
            value="UKNOWN";
            type=OCCIStructureTypes.UKNOWN;
            logger.error("Parse error : " + toparse);
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

    public String getValue() {
        return value;
    }
    
    
    
    
    
    @Override
    public String toString()
    {
        
        
        
        StringBuilder sb = new StringBuilder(" name: ").append(name);
        if(value !=null)
        {
            sb.append(" valore: ").append(value);
        }
        switch(type)
        {
            case Category:
            case Link:
                final Joiner.MapJoiner mapJoiner = Joiner.on('&').withKeyValueSeparator("===");
                sb.append(" ---- ").append(mapJoiner.join(contents));
                break;
            default:
                        
        }
        
        return type + sb.toString();
        
        
        
        
        /*
        final Joiner.MapJoiner mapJoiner = Joiner.on('&').withKeyValueSeparator("===");
        return  type + " " + name + " ---- " + mapJoiner.join(contents);
        */
    }
    
            
    
    
}
