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
 * Copyright 2011 alessiodipietro.
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
package org.clever.HostManager.SAS;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.custommonkey.xmlunit.Diff;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 *
 * @author alessiodipietro
 */
public class AdvertisementExpirationTask extends TimerTask{
    private String requestId;
    private SASAgent sasAgent;
    private XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    private String advertiseRequest;
    private String publicationId;
    private Long alertExpirationMinutes;

    
    public AdvertisementExpirationTask(String requestId,AdvertiseRequestEntry advertiseRequestEntry,SASAgent sasAgent,Long alertExpirationMinutes){
        this.requestId=requestId;
        this.sasAgent=sasAgent;
        this.advertiseRequest=advertiseRequestEntry.advertiseRequest;
        this.publicationId=advertiseRequestEntry.publicationId;
        this.alertExpirationMinutes=alertExpirationMinutes;
    }

    @Override
    public void run() {
        
            //get advertisement from hashmap
            
            //System.out.println("expired");
            Document advertiseRequestDocument=sasAgent.stringToDom(advertiseRequest);
            Element advertiseRequestElement=advertiseRequestDocument.detachRootElement();
            //get publicationID
            //build ExpirationAdvertise Request
            Element expirationAdvertiseRequest=new Element("ExpirationAdvertiseRequest");
            Content featureOfInterest=((Element)advertiseRequestElement.clone()).getChild("FeatureOfInterest").detach();
            Content alertMessageStructure=((Element)advertiseRequestElement.clone()).getChild("AlertMessageStructure").detach();
            expirationAdvertiseRequest.addContent(featureOfInterest);
            expirationAdvertiseRequest.addContent(alertMessageStructure);
            
            //send expiration advertise request to sos and get Phenomenon Advertise from sos
            Document phenomenonAdvertise=sasAgent.sendExpirationAdvertiseRequest(expirationAdvertiseRequest);
            //Document phenomenonAdvertise=sasAgent.sendExpirationAdvertiseRequest(expirationAdvertiseRequest);
            Document phenomenonAdvertiseCopy=(Document)phenomenonAdvertise.clone();
            Element phenomenonAdvertiseElement=phenomenonAdvertise.detachRootElement();
            
            Element phenomenonAdvertiseElementCopy=phenomenonAdvertiseCopy.detachRootElement();
            phenomenonAdvertiseElementCopy.setName("Advertise");
            
            String nowString=advertiseRequestElement.getChild("DesiredPublicationExpiration").getText();
            advertiseRequestElement.getChild("DesiredPublicationExpiration").detach();
            Element desiredPublicationExpirationElement=(Element)phenomenonAdvertiseElement.getChild("DesiredPublicationExpiration").detach();
            
            //String desiredPublicationExpiration=desiredPublicationExpirationElement.getText();
            
            //set new desiredPublicationExpiration
            Date now=new Date();
            Long alertExpirationMinutesMillisecond=this.alertExpirationMinutes*60*1000;
            Long desiredPublicationExpirationMillisecond=now.getTime()+alertExpirationMinutesMillisecond;
            now.setTime(desiredPublicationExpirationMillisecond);
            String desiredPublicationExpiration=sasAgent.sdf.format(now);
            
           
            
            //execute diff: PhenomenAdvertise-advertiseRequestElement
            Diff advertiseDiff = null;
            Boolean equals=false;
            try {
                advertiseDiff = new Diff("<root>"+outputter.outputString(phenomenonAdvertiseElement.getContent())+"</root>", 
                                  "<root>"+outputter.outputString(advertiseRequestElement.getContent())+"</root>");
            } catch (SAXException ex) {
            } catch (IOException ex) {
            }
            equals = advertiseDiff.similar();
            
            if(equals){
                //if equal renew advertisement (with requestId)
                this.sasAgent.renewAdvertisement(requestId,publicationId,desiredPublicationExpiration);
            }else{
                //if not equal update advertisement
                phenomenonAdvertiseElementCopy.getChild("DesiredPublicationExpiration").setText(desiredPublicationExpiration);
                this.sasAgent.advertise(outputter.outputString(phenomenonAdvertiseElementCopy),requestId);
            }
        
     
    }
    
}
