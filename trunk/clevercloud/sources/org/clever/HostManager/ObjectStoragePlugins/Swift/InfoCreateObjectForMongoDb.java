
/*
 * Questo metodo contiene tutte le informazioni che si ricavano nel processo di  
 * inserimento di un oggetto su Swift. Tali informazioni verranno poi estrapolate
 * in un formato Json all'interno di una String. Il passo successivo sarà caricarle
 * si MongoDb.
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
public class InfoCreateObjectForMongoDb extends SwiftParameterOutput {
   
//########################    
//Attributi della classe
//######################## 
    
   private String account;
   private String container;
   private String object;
   private String url;
   private String operazione;
   
   private String statusCode;
   
   private String lastModified ;
   private String contentLength ;
   private String etag ;
   private String date ; 

    
//##############   
//Costruttori   
//##############
   
 
/**
 * Costruttore di Default
 */
public InfoCreateObjectForMongoDb() {
        this.account = "";
        this.container = "";
        this.object = "";
        this.url = "";
        this.statusCode = "";
        this.lastModified = "";
        this.contentLength = "";
        this.etag = "";
        this.date = "";
        this.operazione = "";
        this.type = tipoObjectOutput.InfoCreateObjectForMongoDb;
   
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
    System.out.println("Object: "+this.getObject());
    System.out.println("Url :"+this.getUrl());
    System.out.println("StatusCode: "+this.getStatusCode());
    System.out.println("ContentLength: "+this.getContentLength());
    System.out.println("Etag : "+this.getEtag());
    System.out.println("Date : "+this.getDate());
    System.out.println("LastModified: "+this.getLastModified());
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
"    \"container\": \""+this.getContainer()+"\",\n" +
"    \"object\": \""+this.getObject()+"\",\n" +
"    \"url\": \""+this.getUrl()+"\",\n" +
"    \"statusCode\": \""+this.getStatusCode()+"\",\n" +
"    \"lastModified\": \""+this.getLastModified()+"\",\n" +
"    \"contentLength\": \""+this.getContentLength()+"\",\n" +
"    \"etag\": \""+this.getEtag()+"\",\n" +
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

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }


public String getUrl() {
        return url;
    }

public void setUrl(String url) {
        this.url = url;
    }

public String getLastModified() {
        return lastModified;
    }

public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

public String getContentLength() {
        return contentLength;
    }

public void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

public String getEtag() {
        return etag;
    }

public void setEtag(String etag) {
        this.etag = etag;
    }


public String getDate() {
        return date;
    }

public void setDate(String date) {
        this.date = date;
    }

public String getStatusCode() {
        return statusCode;
    }

public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getOperazione() {
        return operazione;
    }

    public void setOperazione(String operazione) {
        this.operazione = operazione;
    }

    public tipoObjectOutput getType() {
        return type;
    }

    public void setType(tipoObjectOutput type) {
        this.type = type;
    }
  
}//class
