/*
 * The MIT License
 * 
 * Copyright (c) 2013 Universita' degli studi di Messina
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package org.clever.Common.UUIDProvider;
/**
 * @author Giuseppe Tricomi
 */


import com.fasterxml.uuid.UUIDTimer;
import com.fasterxml.uuid.ext.FileBasedTimestampSynchronizer;
import com.fasterxml.uuid.*;
import com.fasterxml.uuid.ext.*;
import com.fasterxml.uuid.impl.*;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.Random;
import org.apache.log4j.Logger;

public class UUIDProvider {
    static private NoArgGenerator gen = null;
    static private Logger logger=Logger.getLogger("UUIDgen");
    static public NoArgGenerator getUUIDGeneratorInstance()
    {
        if (gen == null)
        {
             SecureRandom r = new SecureRandom();
             gen = Generators.randomBasedGenerator(r);
        }
        return gen;
        
    }
    
    static public UUID generateTimeBasedUUID(){
        TimeBasedGenerator tbg=null;
        try
        {
            tbg=new TimeBasedGenerator(EthernetAddress.fromInterface(),new UUIDTimer(new Random(),new FileBasedTimestampSynchronizer()));
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
        return tbg.generate();
    }
    
    static public Integer getPositiveInteger()
    {
        return Math.abs(org.clever.Common.UUIDProvider.UUIDProvider.getUUIDGeneratorInstance().generate().hashCode());
    }
    
}

