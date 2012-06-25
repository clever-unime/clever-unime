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

/**
 *
 * @author marco carbone
 * 
 * Questa classe differisce leggermente da quelle usate per killare gli agenti lanciati
 * da HC e CC. Invece di prendere una lista di Processi, rende in ingresso un solo processo
 * quello relativo al ClusterCoordinator
 */
public class ShutdownThread_ClusterCoordinator implements Runnable 
{
    private Process cc;
    
    public ShutdownThread_ClusterCoordinator(Process p)
    {
        cc = p;
    }
    
    @Override
    public void run() 
    {
        cc.destroy(); //distruggo il processo ClusterCoordinator
    }
    
}
