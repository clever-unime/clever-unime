
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
public class InfoCreateObjectMetadataForMongoDb extends SwiftParameterOutput{
   
//########################    
//Attributi della classe
//######################## 
    
   private String account;
   private String container;
   private String object;
   private String url;
   private String operazione;
   
   private String statusCode;
   
   private String date ; 

   private String X_Container_Meta;
   private String name;
   
//##############   
//Costruttori   
//##############
   
 
/**
 * Costruttore di Default
 */
public InfoCreateObjectMetadataForMongoDb() {
        this.account = "";
        this.container = "";
        this.object = "";
        this.url = "";
        this.statusCode = "";
        this.date = "";
        this.operazione = "";
        this.X_Container_Meta = "";
        this.name = "";
        this.type = tipoObjectOutput.InfoCreateObjectMetadataForMongoDb;
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
    System.out.println("X_Container_Meta: "+this.getX_Container_Meta());
    System.out.println("name: "+this.getName());
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
"    \"container\": \""+this.getContainer()+"\",\n" +
"    \"object\": \""+this.getObject()+"\",\n" +
"    \"X_Container_Meta\": \""+this.getX_Container_Meta()+"\",\n" +
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

    public String getX_Container_Meta() {
        return X_Container_Meta;
    }

    public void setX_Container_Meta(String X_Container_Meta) {
        this.X_Container_Meta = X_Container_Meta;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
   


    
}//class
