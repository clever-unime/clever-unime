/*
 * Copyright [2014] [Università di Messina]
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
 *  Copyright (c) 2010 Filippo Bua
 *  Copyright (c) 2010 Maurizio Paone
 *  Copyright (c) 2010 Francesco Tusa
 *  Copyright (c) 2010 Massimo Villari
 *  Copyright (c) 2010 Antonio Celesti
 *  Copyright (c) 2010 Antonio Nastasi
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

package org.clever.Common.XMPPCommunicator;

import java.util.List;
import org.jdom2.*;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class RequestInformation extends ExecOperation {
    
    public static enum InformationType {NODE_HARDWARE, RESOURCE_USAGE, NODE_STATE};
    
    private InformationType type;

    /* Gli alert non sono ancora supportati e quindi mancano anche i relativi metodi
     * private EventDescriptor alert;
     */

    public RequestInformation(String name, List params, String module, InformationType type){
        super(name, params, module);
        this.type = type;
    }

    public InformationType getType(){
        return(type);
    }

    public void setType(InformationType type){
        this.type = type;
    }

    @Override
    public String generateXML(){
        int i;
        // Creo <information></information>
        Element root = new Element("information");
        // Lo aggiungo al mio Document che rappresenta tutto l' xml
        Document doc = new Document(root);
        // Aggiungo l'attributo <information type="TYPE"></information>
        root.setAttribute("type", type.name());
        /* Aggiungo un figlio e ottengo:
         * <information type="TYPE">
         *      <operation></operation>
         * </information>
         */
        // WARNING: L'elemento module è messo per prova
        Element mod = new Element("module");
        mod.addContent(module);
        root.addContent(mod);
        Element nome = new Element("name");
        nome.addContent(operation);
        root.addContent(nome);
        Element[] par = new Element[params.size()];
        for(i=0; i<params.size(); i++){
            par[i] = new Element("parameter");
            par[i].setAttribute("name", "p" + i);
            par[i].setAttribute("useAttachementId", "true");
            par[i].addContent(String.valueOf(i));
            root.addContent(par[i]);
        }
        XMLOutputter xout = new XMLOutputter();
        Format f = Format.getPrettyFormat();
        xout.setFormat(f);
        return(xout.outputString(doc));
    }

}
