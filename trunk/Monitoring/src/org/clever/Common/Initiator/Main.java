 /*
 * The MIT License
 *
 * Copyright 2011 Marco Carbone
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.clever.Common.Initiator;

import java.util.logging.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.apache.log4j.*;
import org.clever.Common.Shared.LoggerInstantiator;

/**
 *
 * @author marco carbone
 */
public class Main { //Questo è IL MAIN PRINCIPALE DI TUTTO IL PROGETTO CLEVER!!!!
    
    static Initiator in;
    
    public static void main(String[] args) throws CleverException
    {              
        in = Initiator.getInstance(); //creo un oggetto Initiator
        in.start(); //faccio partire l'initiator        
        
        while ( true )
        {
            try
            {
                Thread.sleep( 1000000 );
            }
             catch ( InterruptedException ex )
            {
                System.exit( 1 );
            }
         }
    }
    
}