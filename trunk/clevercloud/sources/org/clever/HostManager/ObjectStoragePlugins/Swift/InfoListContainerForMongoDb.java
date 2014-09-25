/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.HostManager.ObjectStoragePlugins.Swift;

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
public class InfoListContainerForMongoDb extends SwiftParameterOutput{
    
    
  //########################    
//Attributi della classe
//######################## 
   
   private String account;
   private String container;
   private String url;
   private String statusCode; //status code restituito dalla richiesta http
   private String date ; // data e tempo della transazione
   private String response; //risposta json della richiesta
   private String operazione;  
    
   private int X_Container_Object_Count; // il numero degli oggetti del container
   private Long X_Container_Object_Bytes_Used; // totale spazio in byte immagazzinato dal container

   //private String X_Container_Meta_Name; // elemento personalizzato dell'header
 
 

//##############
//Costruttori
//##############

   
/**
 * Costruttore di default.
 */  
 public InfoListContainerForMongoDb() {
        this.account = "";
        this.url = "";
        this.statusCode = "";
        this.date = "";
        this.response = "";
        this.operazione = "";
        this.X_Container_Object_Count = 0;
        this.X_Container_Object_Bytes_Used = null;
//        this.X_Container_Meta_Name = "";
        this.type = tipoObjectOutput.InfoListContainerForMongoDb;
    }
  
 
/**
 * Metodo utile in fase di debug.
 */
public void debug (){
    System.out.println("\n\n##############################");
    System.out.println("########   DEBUG   ###########");
    System.out.println("Operazione: "+this.getOperazione());
    System.out.println("Account: "+this.getAccount());
    System.out.println("Container: "+this.getContainer());
    System.out.println("Url :"+this.getUrl());
    System.out.println("StatusCode: "+this.getStatusCode());
    System.out.println("Response"+ this.getResponse());
    System.out.println("Date : "+this.getDate());
    System.out.println("X_Container_Object_Count : "+this.getX_Container_Object_Count());
    System.out.println("X_Container_Object_Bytes_Used : "+this.getX_Container_Object_Bytes_Used());
//    System.out.println("X_Container_Meta_Name : "+this.getX_Account_Meta_Name());
    System.out.println("##############################");
}
   
 

/**
 * Metodo che crea una stringa formattata json con le informazioni che ricava 
 * dall'operazione di inserimento di un oggetto su swift.
 * @return 
 */
public String infoToJson(){
    
       String json ="{\n" +
"    \"operazione\": \""+this.getOperazione()+"\",\n" +               
"    \"account\": \""+this.getAccount()+"\",\n" +
"    \"container\": \""+this.getContainer()+"\",\n" +
"    \"url\": \""+this.getUrl()+"\",\n" +
"    \"statusCode\": \""+this.getStatusCode()+"\",\n" +
"    \"Response\": \""+this.getResponse()+"\",\n" +               
"    \"X_Container_Object_Count\": \""+this.getX_Container_Object_Count()+"\",\n" +
"    \"X_Container_Object_Bytes_Used\": \""+this.getX_Container_Object_Bytes_Used()+"\",\n" +
               
/*  "    \"X_Container_Meta_Name\": \""+this.getX_Container_Meta_Name()+"\",\n" +  */
               
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

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getOperazione() {
        return operazione;
    }

    public void setOperazione(String operazione) {
        this.operazione = operazione;
    }

    public int getX_Container_Object_Count() {
        return X_Container_Object_Count;
    }

    public void setX_Container_Object_Count(int X_Container_Object_Count) {
        this.X_Container_Object_Count = X_Container_Object_Count;
    }

    public Long getX_Container_Object_Bytes_Used() {
        return X_Container_Object_Bytes_Used;
    }

    public void setX_Container_Object_Bytes_Used(Long X_Container_Object_Bytes_Used) {
        this.X_Container_Object_Bytes_Used = X_Container_Object_Bytes_Used;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }



   
   
}
