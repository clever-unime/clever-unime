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
/**
 * The MIT License
 * 
 * @author dott. Riccardo Di Pietro - 2014
 * MDSLab Messina
 * dipcisco@hotmail.com
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
package org.clever.HostManager.ObjectStoragePlugins.Swift;


public class InfoCopyObjectForMongoDb extends SwiftParameterOutput{
   
//########################    
//Attributi della classe
//######################## 
    
   private String account;
   private String etag;
   
   private String url;
   private String operazione; 
   
   private String statusCode;
   
   private String date ; 
    
//##################################
// copy object from one containet to another
private String containerDestination;
private String objectDestination;
private String containerOrigin;
private String objectOrigin;
private String X_Copied_From_Last_Modified;
private String Last_Modified;
private String X_Copied_From;

//##################################


//##############
//Costruttori
//##############


/**
 * Costruttore di default.
 */
public InfoCopyObjectForMongoDb() {
        this.account = "";
        this.url = "";
        this.operazione = "";
        this.statusCode = "";
        this.date = "";
        this.containerDestination = "";
        this.objectDestination = "";
        this.containerOrigin = "";
        this.objectOrigin = "";
        this.etag ="";
        this.X_Copied_From ="";
        this.type = tipoObjectOutput.InfoCopyObjectForMongoDb;
    }


/**
 * Metodo utile in fase di debug.
 */
public void debug (){
    System.out.println("\n\n##############################");
    System.out.println("########   DEBUG InfoCopyObjectForMongoDb  ###########");
    System.out.println("Operazione: "+this.getOperazione());
    System.out.println("Account: "+this.getAccount());
    System.out.println("containerOrigin: "+this.getContainerOrigin());
    System.out.println("objectOrigin: "+this.getObjectOrigin());    
    System.out.println("containerDestination: "+this.getContainerDestination());
    System.out.println("objectDestination: "+this.getObjectDestination());
    System.out.println("etag: "+this.getEtag());
    System.out.println("X_Copied_From_Last_Modified: "+this.getX_Copied_From_Last_Modified());
    System.out.println("Last_Modified: "+this.getLast_Modified());
    System.out.println("X_Copied_From: "+this.getX_Copied_From());
    System.out.println("Url :"+this.getUrl());
    System.out.println("StatusCode: "+this.getStatusCode());
    System.out.println("Date : "+this.getDate());
    System.out.println("##############################");
}


/**
 * Metodo che crea una stringa formattata json con le informazioni che ricava 
 * dall'operazione di inserimento di un oggetto su swift.
 * @return 
 */
public String infoToJson(){
    
       String json ="{\n" +
"    \"Operazione\": \""+this.getOperazione()+"\",\n" +               
"    \"account\": \""+this.getAccount()+"\",\n" +
"    \"containerOrigin\": \""+this.getContainerOrigin()+"\",\n" +
"    \"objectOrigin\": \""+this.getObjectOrigin()+"\",\n" +
"    \"containerDestination\": \""+this.getContainerDestination()+"\",\n" +
"    \"objectDestination\": \""+this.getObjectDestination()+"\",\n" +
"    \"etag\": \""+this.getEtag()+"\",\n" +
"    \"X_Copied_From_Last_Modified\": \""+this.getX_Copied_From_Last_Modified()+"\",\n" +
"    \"Last_Modified\": \""+this.getLast_Modified()+"\",\n" +
"    \"X_Copied_From\": \""+this.getX_Copied_From()+"\",\n" +               
"    \"url\": \""+this.getUrl()+"\",\n" +               
"    \"statusCode\": \""+this.getStatusCode()+"\",\n" +
"    \"date\": \""+this.getDate()+"\"\n" +
"}";
    
   return json;
}






//########################
//Metodi Setter e Getter
//########################


    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOperazione() {
        return operazione;
    }

    public void setOperazione(String operazione) {
        this.operazione = operazione;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContainerDestination() {
        return containerDestination;
    }

    public void setContainerDestination(String containerDestination) {
        this.containerDestination = containerDestination;
    }

    public String getObjectDestination() {
        return objectDestination;
    }

    public void setObjectDestination(String objectDestination) {
        this.objectDestination = objectDestination;
    }

    public String getContainerOrigin() {
        return containerOrigin;
    }

    public void setContainerOrigin(String containerOrigin) {
        this.containerOrigin = containerOrigin;
    }

    public String getObjectOrigin() {
        return objectOrigin;
    }

    public void setObjectOrigin(String objectOrigin) {
        this.objectOrigin = objectOrigin;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getX_Copied_From_Last_Modified() {
        return X_Copied_From_Last_Modified;
    }

    public void setX_Copied_From_Last_Modified(String X_Copied_From_Last_Modified) {
        this.X_Copied_From_Last_Modified = X_Copied_From_Last_Modified;
    }

    public String getLast_Modified() {
        return Last_Modified;
    }

    public void setLast_Modified(String Last_Modified) {
        this.Last_Modified = Last_Modified;
    }

    public String getX_Copied_From() {
        return X_Copied_From;
    }

    public void setX_Copied_From(String X_Copied_From) {
        this.X_Copied_From = X_Copied_From;
    }
   
    
    
 


    
}
