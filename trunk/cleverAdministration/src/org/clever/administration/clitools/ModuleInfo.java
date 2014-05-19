/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.clitools;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author maurizio
 */
public final class ModuleInfo implements Comparable<ModuleInfo>, HasName{
    final String name;
    final String comment;
    final TreeSet<MethodInfo> methodsList;

    public ModuleInfo(String name, String comment, MethodInfo[] methods) {
        this.name = name;
        this.comment = comment;
        this.methodsList = Sets.newTreeSet(Arrays.asList(methods));
    }

    @Override
    public String toString() {
        StringBuilder ris = new StringBuilder(name).append("\n").append(comment).append("\n");
        //for (MethodInfo mi : methodsList)
        //{
        //    ris.append(mi).append("\n");
        //}
        return ris.toString();
    }

    @Override
    public int compareTo(ModuleInfo t) {
        return this.name.compareTo(t.name);
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public TreeSet<MethodInfo> getMethodsList() {
        return methodsList;
    }

    public String getDetails() {
        StringBuilder ris = new StringBuilder("Methods").append("\n");
        for (MethodInfo mi : methodsList)
        {
            ris.append(mi.getName()).append("\t").append(mi.getComment()).append("\n");
        }
        return ris.toString();
    }

    public String getMethodsDetails(final String get) {
       Predicate<MethodInfo> find = new Predicate<MethodInfo>(){

            @Override
            public boolean apply(MethodInfo t) {
                return t.getName().equals(get);
            }
        };
        SortedSet<MethodInfo> metFiletered = Sets.filter(methodsList, find);
        StringBuilder ris = new StringBuilder("Methods").append("\n");
        for (MethodInfo mi : metFiletered)
        {
            ris.append(mi).append("\n");
        }
      
        return ris.toString();
    }
    
    
    
    
}
