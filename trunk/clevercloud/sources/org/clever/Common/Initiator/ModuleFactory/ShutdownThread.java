/*
 * Copyright 2014 Università di Messina
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
/*
 * The MIT License
 *
 * Copyright 2012 Marco Carbone
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
package org.clever.Common.Initiator.ModuleFactory;

import java.util.List;

/**
 * Questa classe rappresenta il cuore del thread di shutdwon. Viene istanziata
 * dal Cm e dall' Hm. Questi gli passeranno le lista dei processi agenti
 * istanziati. nel momento in cui si verificherà lo shutdown, grazie ad una
 * funzionalità della classe Runtime di java, il sistema avvienrà questo Tjhread
 * (uno nel Cm e uno nell'Hm) che provvederà a distruggere tutti i processia
 * genti prensenti in lista, evitando così il problema dei processi appesi!
 *
 * @author marco carbone
 */
public class ShutdownThread implements Runnable {

    private List lst;
    private Process p;
    private ThreadGroup replaceAgent;

    public ShutdownThread(List lst, ThreadGroup replaceAgent) {
        this.replaceAgent = replaceAgent;
        this.lst = lst;
    }

    @Override
    public synchronized void run() {
        replaceAgent.interrupt(); //devo interrompere prima tutti i thread di ripristino degli agenti!

        int k;

        for (k = 0; k < lst.size(); k++) {
            p = (Process) lst.get(k);

            p.destroy();
        }
    }
}
