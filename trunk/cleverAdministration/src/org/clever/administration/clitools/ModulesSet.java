/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.clitools;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author maurizio
 */
public class ModulesSet extends TreeSet<ModuleInfo>{
     public ModuleInfo findModule(final String get) {
        Predicate<ModuleInfo> find = new Predicate<ModuleInfo>(){

            @Override
            public boolean apply(ModuleInfo t) {
                return t.getName().equals(get);
            }
        };
        SortedSet<ModuleInfo> mods = Sets.filter(this, find); 
        return (mods.isEmpty()?null:mods.iterator().next());
            
        
        
    }
}
