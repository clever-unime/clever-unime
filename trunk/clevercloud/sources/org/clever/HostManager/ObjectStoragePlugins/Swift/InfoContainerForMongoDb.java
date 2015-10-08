/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.HostManager.ObjectStoragePlugins.Swift;

/**
 *
 * @author riccardo
 */
public class InfoContainerForMongoDb extends SwiftParameterOutput {
    
   
    
//########################    
//Attributi della classe
//######################## 
    
   private String account;
   private String container;
   private String url;
   
   private String statusCode;
   
   private String date ; 

//##############
//Costruttori
//##############
   
/**
 * Costruttore
 * 
 * @param account
 * @param container
 * @param url
 * @param statusCode
 * @param date 
 */   
public InfoContainerForMongoDb(String account, String container, String url, String statusCode, String date) {
    this.account = account;
    this.container = container;
    this.url = url;
    this.statusCode = statusCode;
    this.date = date;
    this.type = tipoObjectOutput.InfoContainerForMongoDb;
}
    
/**
 * Costruttore di default
 */
public InfoContainerForMongoDb() {
    this.account = "";
    this.container = "";
    this.url = "";
    this.statusCode = "";
    this.date = "";
    this.type = tipoObjectOutput.InfoContainerForMongoDb;
}
   
/**
 * Metodo utile in fase di debug.
 */
public void debug (){
    System.out.println("\n\n##############################");
    System.out.println("########   DEBUG   ###########");
    System.out.println("Account: "+this.getAccount());
    System.out.println("Container: "+this.getContainer());
    System.out.println("Url :"+this.getUrl());
    System.out.println("StatusCode: "+this.getStatusCode());
    System.out.println("Date : "+this.getDate());
    System.out.println("##############################");
}



/**
 * Metodo che crea una stringa formattata json con le informazioni che ricava 
 * dall'operazione di creazione di un container su swift.
 * @return 
 */
public String infoToJson(){
    
       String json ="{\n" +
"    \"account\": \""+this.getAccount()+"\",\n" +
"    \"container\": \""+this.getContainer()+"\",\n" +
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

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
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



    
    
}
