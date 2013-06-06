/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.Common.XMPPCommunicator;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import java.security.SecureRandom;

/**
 *
 * @author maurizio
 */
public class UUIDProvider {
    static private NoArgGenerator gen = null;
    
    static public NoArgGenerator getUUIDGeneratorInstance()
    {
        if (gen == null)
        {
             SecureRandom r = new SecureRandom();
             gen = Generators.randomBasedGenerator(r);
        }
        return gen;
        
    }
    
    static public Integer getPositiveInteger()
    {
        return Math.abs(UUIDProvider.getUUIDGeneratorInstance().generate().hashCode());
    }
    
}
