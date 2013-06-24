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
                    type = OCCIStructureTypes.UKNOWN;
                    name = "UKNOWN";
                    value="UKNOWN";
                    return; //TODO: manage parse error
                }
                
                
                
                String parsed = m.replaceAll("");
                //toparse = toparse.replaceAll("^.*: ", ""); //TODO: improve it
                
                
                switch (type)
                {
                    case Category:
                    case Link:
                        p = Pattern.compile("^(?:</)?([^/]+)(?:/.*>)?;(.*)");
//                        ^(.*): (.*?);
                        
                        m = p.matcher(parsed);
                        m.find();
                        name = m.group(1);
                        value = m.group(2);
                        for (String token : Splitter.
                                            on(Pattern.compile("[; ]")).
                                            trimResults().
                                            split(value))

                            {

                                putKeyValue(contents, token);
                            }
                        break;
                    case Attribute:
                    case Location:
                        p = Pattern.compile( "^*([^\\s]+?) *?= *\"?([^\"]+)\"?");
                        
                        //p = Pattern.compile("(.*)=\"?(.*)\"?");
                        m = p.matcher(parsed);
                        if(m.find())
                        {
                            name = m.group(1);
                            value = m.group(2);
                            break;
                        }
                        logger.error("Parse error : " + parsed);
                        
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
        
        
        
        StringBuilder sb = new StringBuilder(" ").append(name);
         
        switch(type)
        {
            case Category:
            case Link:
                final Joiner.MapJoiner mapJoiner = Joiner.on('&').withKeyValueSeparator("===");
                sb.append(" ---- ").append(mapJoiner.join(contents));
                break;
               
            case Attribute:
            case Location:
                sb.append(" === ").append(value);
                        
        }
        
        return type + sb.toString();
        
        
        
        
        /*
        final Joiner.MapJoiner mapJoiner = Joiner.on('&').withKeyValueSeparator("===");
        return  type + " " + name + " ---- " + mapJoiner.join(contents);
        */
    }
    
            
    
    
}
