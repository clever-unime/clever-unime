/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.clitools;

import java.util.AbstractMap;
import java.util.Map.Entry;

/**
 *
 * @author maurizio
 */
public final class MethodInfo implements Comparable<MethodInfo>, HasName {
    final String methodName;
    final String returnType;
    final String comment;
    final Entry<String,String> arguments[]; //name|comment

    public MethodInfo(String methodName, String returnType, String comment, AbstractMap.Entry<String, String>[] arguments) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.comment = comment;
        this.arguments = arguments;
    }

    @Override
    public int compareTo(MethodInfo t) {
        int ris = this.methodName.compareTo(t.methodName);
        if(ris == 0)
            return Integer.valueOf(this.arguments.length).compareTo(Integer.valueOf(t.arguments.length));
        return ris;
    }

    @Override
    public String toString() {
        StringBuilder ris = new StringBuilder("\n").append(methodName).append("\t").append(comment).append("\n").append("Params:").append("\n");
        for(Entry<String,String> a : arguments)
        {
            ris.append(a.getKey()).append("\t").append(a.getValue()).append("\n");
        }
        ris.append("Returns: ").append(this.returnType);
        return ris.toString();
    }

    public String getName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getComment() {
        return comment;
    }

    public Entry<String, String>[] getArguments() {
        return arguments;
    }
    
    
}
