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
public class InfoListAccountDetailsForMongoDb extends SwiftParameterOutput{
    

//########################    
//Attributi della classe
//######################## 
   
   private String account;
   private String url;
   private String statusCode; //status code restituito dalla richiesta http
   private String date ; // data e tempo della transazione
   private String response; //risposta json della richiesta
   private String operazione; //
   
   
   private int X_Account_Object_Count; // il numero degli oggetti dell'account
   private int X_Account_Container_Count ; // il numero di container
   private Long X_Account_Object_Bytes_Used; // totale spazio in byte immagazzinato dall'account
 //  private String X_Account_Meta_Name; // elemento personalizzato dell'header
   
//##############
//Costruttori
//##############
   
/**
 * Costruttore di default.
 */  
public InfoListAccountDetailsForMongoDb() {
    this.account = "";
    this.url = "";
    this.statusCode = "";
    this.date = "";
    this.response = "";
    this.X_Account_Object_Count = 0;
    this.X_Account_Container_Count = 0;
    this.X_Account_Object_Bytes_Used = null;
 //   this.X_Account_Meta_Name = "";
    this.operazione = "";
    this.type = tipoObjectOutput.InfoListAccountDetailsForMongoDb;
    }
  
/**
 * Metodo utile in fase di debug.
 */
public void debug (){
    System.out.println("\n\n##############################");
    System.out.println("########   DEBUG   ###########");
    System.out.println("Operazione: "+this.getOperazione());
    System.out.println("Account: "+this.getAccount());
    System.out.println("Url :"+this.getUrl());
    System.out.println("StatusCode: "+this.getStatusCode());
    System.out.println("Response"+ this.getResponse());
    System.out.println("Date : "+this.getDate());
    System.out.println("X_Account_Object_Count : "+this.getX_Account_Object_Count());
    System.out.println("X_Account_Container_Count : "+this.getX_Account_Container_Count());
    System.out.println("X_Account_Object_Bytes_Used : "+this.getX_Account_Object_Bytes_Used());
//    System.out.println("X_Account_Meta_Name : "+this.getX_Account_Meta_Name());
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
"    \"url\": \""+this.getUrl()+"\",\n" +
"    \"statusCode\": \""+this.getStatusCode()+"\",\n" +
"    \"Response\": \""+this.getResponse()+"\",\n" +               
"    \"X_Account_Object_Count\": \""+this.getX_Account_Object_Count()+"\",\n" +
"    \"X_Account_Container_Count\": \""+this.getX_Account_Container_Count()+"\",\n" +
"    \"X_Account_Object_Bytes_Used\": \""+this.getX_Account_Object_Bytes_Used()+"\",\n" +
               
/*  "    \"X_Account_Meta_Name\": \""+this.getX_Account_Meta_Name()+"\",\n" +  */
               
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

    public int getX_Account_Object_Count() {
        return X_Account_Object_Count;
    }

    public void setX_Account_Object_Count(int X_Account_Object_Count) {
        this.X_Account_Object_Count = X_Account_Object_Count;
    }

    public int getX_Account_Container_Count() {
        return X_Account_Container_Count;
    }

    public void setX_Account_Container_Count(int X_Account_Container_Count) {
        this.X_Account_Container_Count = X_Account_Container_Count;
    }

    public Long getX_Account_Object_Bytes_Used() {
        return X_Account_Object_Bytes_Used;
    }

    public void setX_Account_Object_Bytes_Used(Long X_Account_Object_Bytes_Used) {
        this.X_Account_Object_Bytes_Used = X_Account_Object_Bytes_Used;
    }

    /*
    public String getX_Account_Meta_Name() {
        return X_Account_Meta_Name;
    }

    public void setX_Account_Meta_Name(String X_Account_Meta_Name) {
        this.X_Account_Meta_Name = X_Account_Meta_Name;
    }
*/
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


    
}
