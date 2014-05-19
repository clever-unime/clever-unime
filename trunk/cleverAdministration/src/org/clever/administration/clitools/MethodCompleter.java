/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.clitools;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jline.console.completer.Completer;









class StartWithPredicate<T extends HasName> implements Predicate<T> {
    private final String prefix;

    
    
    public StartWithPredicate(String prefix)
    {
        this.prefix = prefix;
    }
    
    @Override
    public boolean apply(HasName t) {
        return t.getName().startsWith(prefix);
    }

   
    
}





class HasNameFunction<T extends HasName> implements Function<T,String>
{

    @Override
    public String apply(T f) {
        return f.getName();
    }
    
}





class ModMet<MODT,METT> {
    private MODT module = null;
    private METT method = null;
    public ModMet(MODT m, METT me)
    {
        module = m;
        method=me;
    }

    ModMet() {
        
    }

    public MODT getModule() {
        return module;
    }

    public void setModule(MODT module) {
        this.module = module;
    }

    public METT getMethod() {
        return method;
    }

    public void setMethod(METT method) {
        this.method = method;
    }
    
    
}


/**
 *
 * @author maurizio
 */
public class MethodCompleter implements Completer{
    private final ModulesSet modules;
    private HasNameFunction transformFunc = new HasNameFunction();
    public MethodCompleter(final ModulesSet modules)
    {
        this.modules = modules;
    }
    
    
    
    
    
    @Override
    public int complete(String string, int cursor, List<CharSequence> list) {
        
        if(string.trim().isEmpty())
        {
             Iterables.addAll(list, Iterables.transform(modules, transformFunc));
             return cursor;
        }
        
        
        ModMet<String,String> modMethod = new ModMet<String,String>();
        int ris = parse(string,cursor,modMethod);
        if (ris == -1)
            return -1;
        
        String module = modMethod.getModule();
        String method = modMethod.getMethod();
        //System.out.println(module + " ---- " + method);
        if(method != null) //method name
            {
                ModuleInfo mi = modules.findModule(module);
                if(mi==null)
                {
                    return -1;
                }
                if(method.isEmpty())
                {
                    
                    Iterables.addAll(list, Iterables.transform(mi.getMethodsList(), transformFunc));
                }
                else
                {
                //farsi dare la lista dei metodi con getmethodslist filtrarla con starwith e aggiungerla in list
                SortedSet<MethodInfo> met = mi.getMethodsList();
                Iterable<String> f = Iterables.filter(met, new StartWithPredicate(method));
                Iterables.addAll(list, Iterables.transform(f, transformFunc));
                }
               
                       
                
            }
        else
        {
            //System.out.println(module);
            if(module.isEmpty())
            {
                Iterables.addAll(list, Iterables.transform(modules, transformFunc));
            }
            else
            {
            
            Iterable f = Iterables.filter(modules, new StartWithPredicate(module));
            Iterables.addAll(list, Iterables.transform(f, transformFunc));
            }   
            
                
        }
        if(list.size() == 1)
        {
            if(method!=null)
                list.set(0, list.get(0) + "(");
            else
                list.set(0, list.get(0) + ".");
        }
        return ris;
    }

    private int  parse(final String string, int cursor, ModMet<String,String> modmet) {
        if(string == null)
            return -1;
        
        Pattern p = Pattern.compile("^.*?([^ .]+)(?:\\.([^. ]*))? *$");
        Matcher m = p.matcher(string);
        
        int ris = string.lastIndexOf(".");
        if(ris==-1)
        {
            ris = string.lastIndexOf(" ");
        }
        ris++;
        
        
        
        
        if(m.find())
        {
            modmet.setModule(m.group(1));
            modmet.setMethod(m.group(2));
            return ris;
        }
        else
            return -1;
        
    }
    
}
