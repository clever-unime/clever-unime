/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.administration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author maurizio
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HasScripts {
    
    /**
     *
     * @return The module name 
     */
    String value();
    String script() default "module.bsh"; //la risorsa script sara' cosi' nello stesso package del nome e si chiamera' module.bsh
}
