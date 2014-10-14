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


public class InfoOperationAccountForMongoDb extends SwiftParameterOutput{
    
//########################    
//Attributi della classe
//######################## 
    
   private String account;
   private String url;
   private String X_Account_Meta;
   private String name;
   
   private String operazione; // {create|update|delete metadata}
   
   private String statusCode; //status code della richiesta http
   
   private String date ; //the transaction date and time.

   
//##############
//Costruttori
//##############
  
   
public InfoOperationAccountForMongoDb(String account, String url, String operazione,String X_Account_Meta, String name, String statusCode, String date) {
    this.operazione = operazione;
    this.account = account;
    this.url = url;
    this.X_Account_Meta = X_Account_Meta;
    this.name = name;
    this.statusCode = statusCode;
    this.date = date;
    this.type = tipoObjectOutput.InfoOperationAccountForMongoDb;
}
 

/**
 * Costruttore di default
 */
public InfoOperationAccountForMongoDb() {
    this.operazione = "";
    this.account = "";
    this.url = "";
    this.X_Account_Meta = "";
    this.name = "";
    this.statusCode = "";
    this.date = "";
}


/**
 * Metodo utile in fase di debug.
 */
public void debug (){
    System.out.println("\n\n##########################################################");
    System.out.println("########   DEBUG: InfoOperationAccountForMongoDb   ###########");
    System.out.println("Account: "+this.getAccount());
    System.out.println("X_Account_Meta: "+this.getX_Account_Meta());
    System.out.println("name: "+this.getName());
    System.out.println("operazione: "+this.getOperazione());
    System.out.println("Url :"+this.getUrl());
    System.out.println("StatusCode: "+this.getStatusCode());
    System.out.println("Date : "+this.getDate());
    System.out.println("##########################################################");
}


/**
 * Metodo che crea una stringa formattata json con le informazioni che ricava 
 * dall'operazione di creazione di un container su swift.
 * @return 
 */
public String infoToJson(){
    System.out.println("\n\n");
       String json ="{\n" +
"   \"operazione\": \""+this.getOperazione()+"\",\n" +               
"    \"account\": \""+this.getAccount()+"\",\n" +
"    \"X_Account_Meta\": \""+this.getX_Account_Meta()+"\",\n" +
"    \"name\": \""+this.getName()+"\",\n" +
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

    public String getX_Account_Meta() {
        return X_Account_Meta;
    }

    public void setX_Account_Meta(String X_Account_Meta) {
        this.X_Account_Meta = X_Account_Meta;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getOperazione() {
        return operazione;
    }

    public void setOperazione(String operazione) {
        this.operazione = operazione;
    }



   
    
}
