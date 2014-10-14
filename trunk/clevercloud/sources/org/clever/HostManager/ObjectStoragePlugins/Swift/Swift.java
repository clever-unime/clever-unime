package org.clever.HostManager.ObjectStoragePlugins.Swift;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.HostManager.ObjectStorage.ObjectStoragePlugin;
import org.jdom2.Element;



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
public class Swift implements ObjectStoragePlugin{
    

    // private String publicUrlSwift;
    // private String token;
     
     //#################################################
     private Agent owner;
     Logger logger = Logger.getLogger("Swift - Plugin");

    //#################################################
    //################################
    // COSTRUTTORI
    //################################
    
     
     
     public Swift() {
    }
    
    
    
     
    
    //################################
    // METODI SETTER E GETTER
    //################################
     
 /*    
public String getPublicUrlSwift() {
        return publicUrlSwift;
    }

public void setPublicUrlSwift(String publicUrlSwift) {
        this.publicUrlSwift = publicUrlSwift;
    }

public String getToken() {
        return token;
    }

public void setToken(String token) {
        this.token = token;
    }


public void debug(){
    System.out.println("publicUrlSwift: "+this.getPublicUrlSwift());
    System.out.println("token: "+this.getToken());
      
}//debug

*/
     
     
//################################
//    METODI DELLA CLASSE
//################################


//############################################################################
//Operazione su Account
//########################


/**
 * 
 * Method: POST
 * URI:	/v1/{account}
 * Description: Creates account metadata.
 * 
 * Create account metadata:
 * curl -i $publicURL -X POST -H "X-Auth-Token: $token" 
 *                                           -H "X-Account-Meta-Book: MobyDick" 
 *                                            
 * 
 * 
 * HTTP/1.1 204 No Content
 * Content-Length: 0
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx8c2dd6aee35442a4a5646-0052d954fb
 * Date: Fri, 17 Jan 2014 16:06:19 GMT
 * 
 * @param swiftParameterInput
 * @return  
 * @throws java.io.IOException  
 */
public SwiftParameterOutput createAccountMetadata(SwiftParameterInput swiftParameterInput) throws IOException {
    
   if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertAccount){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertAccount);
       
    //creo questo oggetto di comodo
    InsertAccount insertAccount = new InsertAccount();

    //eseguo il dowcasting
    insertAccount = (InsertAccount) swiftParameterInput.ogg; // <----######
    
        
    insertAccount.setOperazione("create metadata");
    
    //######################
    String url= insertAccount.getBase()+insertAccount.getAccount();
    //######################
    //debug
    //System.out.println(url);
    
    InfoOperationAccountForMongoDb risposta = httpPostAccountMetadata(url,insertAccount.getTokenId(),insertAccount.getX_Account_Meta(), insertAccount.getName());
        
    if(risposta.getStatusCode().equals("204")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertContainer
      
    risposta.setAccount(insertAccount.getAccount());
    risposta.setX_Account_Meta(insertAccount.getX_Account_Meta());
    risposta.setName(insertAccount.getName());
    risposta.setOperazione(insertAccount.getOperazione());
    
   }
   
    return risposta;
   
   }//if
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   }
    
    
}//createAccountMetadata

/**
 * 
 * Normal response codes: 204
 * 
 * @param url_servizio
 * @param X_Auth_Token
 * @param X_Account_Meta
 * @param name
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private InfoOperationAccountForMongoDb httpPostAccountMetadata( String url, String X_Auth_Token, String X_Account_Meta, String name) throws UnsupportedEncodingException, IOException{
         
       DefaultHttpClient httpclient = new DefaultHttpClient();
      
       HttpPost http = new HttpPost(url);
      
       http.setHeader("Content-type", "application/json");
       http.setHeader("Accept", "application/json");
       http.setHeader("X-Auth-Token", X_Auth_Token);
       http.setHeader("X-Account-Meta-"+X_Account_Meta, name);
               
   HttpResponse response = httpclient.execute(http);
       
   //System.out.println(response);
      
   //System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
       
       
  //creo un oggetto risposta
  InfoOperationAccountForMongoDb risposta = new InfoOperationAccountForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  
   //solo se la richiesta è andata a buon fine carico le altre informazioni
   // contenute nell'oggetto insertObject 
 
  if(risp.equals("204")){   
      
  //debug
  //System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
  //carico l'oggetto di risposta di alcune informazioni
  
    String date =response.getLastHeader("Date").toString().replace("Date:","").trim(); 
   
  if(date!=""){
       //System.out.println("date: "+date);
       risposta.setDate(date);
   }
  
  
 
  
 }//if 
 
  return risposta;       
       
       
}//httpPost

/**
 * 
 * Method: POST
 * URI: /v1/{account}
 * Description:  Updates account metadata.
 * 
 * 
 * Update account metadata:
 * 
 * curl -i $publicURL -X POST -H "X-Auth-Token: $token" 
 *                               -H "X-Account-Meta-Subject: AmericanLiterature"
 * 
 * HTTP/1.1 204 No Content
 * Content-Length: 0
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx1439b96137364ab581156-0052d95532
 * Date: Fri, 17 Jan 2014 16:07:14 GMT
 *  
 * @param swiftParameterInput 
 * @return  
 * @throws java.io.IOException 
 */
public SwiftParameterOutput updateAccountMetadata(SwiftParameterInput swiftParameterInput) throws IOException{
    
    if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertAccount){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertAccount);
    
    //creo questo oggetto di comodo
    InsertAccount insertAccount = new InsertAccount();
    //eseguo il dowcasting
    insertAccount = (InsertAccount) swiftParameterInput.ogg; // <----######
    
    insertAccount.setOperazione("update metadata");
    
    //######################
    String url= insertAccount.getBase()+insertAccount.getAccount();
    //######################
    //debug
    //System.out.println(url);
    
    InfoOperationAccountForMongoDb risposta = httpPostAccountMetadata(url,insertAccount.getTokenId(),insertAccount.getX_Account_Meta(), insertAccount.getName());
    
    if(risposta.getStatusCode().equals("204")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertContainer
      
    risposta.setAccount(insertAccount.getAccount());
    risposta.setX_Account_Meta(insertAccount.getX_Account_Meta());
    risposta.setName(insertAccount.getName());
    risposta.setOperazione(insertAccount.getOperazione());
        
   }
   
    return risposta;
   }//if
   else{
      
       logger.error("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//updateAccountMetadata


/**
 *
 * Method: POST
 * URI:	/v1/{account}
 * Description: Deletes account metadata.
 * 
 * @param swiftParameterInput 
 * @return  
 * @throws java.io.IOException  
 */
public SwiftParameterOutput deleteAccountMetadata( SwiftParameterInput swiftParameterInput) throws IOException{
  
    //creo questo oggetto di comodo
    InsertAccount insertAccount = new InsertAccount();

    //eseguo il dowcasting
    insertAccount = (InsertAccount) swiftParameterInput.ogg; // <----######
    
    insertAccount.setOperazione("delete metadata");
    
    //######################
    String url= insertAccount.getBase()+insertAccount.getAccount();
    //######################
    //debug
    //System.out.println(url);
        
    InfoOperationAccountForMongoDb risposta = httpPostDeleteAccountMetadata(url,insertAccount.getTokenId(),insertAccount.getX_Account_Meta(),insertAccount.getName());
    
    if(risposta.getStatusCode().equals("204")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertContainer
      
    risposta.setAccount(insertAccount.getAccount());
    risposta.setX_Account_Meta(insertAccount.getX_Account_Meta());
    risposta.setName(insertAccount.getName());
    risposta.setOperazione(insertAccount.getOperazione());
    
   }
   
    return risposta;
        
}//deleteAccountMetadata

/**
  * Delete account metadata:
  * 
  * curl -i $publicURL -X POST -H "X-Auth-Token: $token" 
  *                                       -H "X-Remove-Account-Meta-Subject: x"
  * 
  * HTTP/1.1 204 No Content
  * Content-Length: 0
  * Content-Type: text/html; charset=UTF-8
  * X-Trans-Id: tx411cf57701424da99948a-0052d9556f
  * Date: Fri, 17 Jan 2014 16:08:15 GMT
 * 
 * @param url_servizio
 * @param X_Auth_Token
 * @param X_Account_Meta1
 * @param name1
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private InfoOperationAccountForMongoDb httpPostDeleteAccountMetadata( String url, String X_Auth_Token, String X_Account_Meta, String name) throws UnsupportedEncodingException, IOException{
         
       DefaultHttpClient httpclient = new DefaultHttpClient();
      
       HttpPost http = new HttpPost(url);
      
       http.setHeader("Content-type", "application/json");
       http.setHeader("Accept", "application/json");
       http.setHeader("X-Auth-Token", X_Auth_Token);
       http.setHeader("X-Remove-Account-Meta-"+X_Account_Meta, name);
      
       HttpResponse response = httpclient.execute(http);
       
       //System.out.println(response);
       //System.out.println("Response Code : "+response.getStatusLine().getStatusCode());
 
       //creo un oggetto risposta
  InfoOperationAccountForMongoDb risposta = new InfoOperationAccountForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  
   //solo se la richiesta è andata a buon fine carico le altre informazioni
   // contenute nell'oggetto insertObject 
 
  if(risp.equals("204")){   
      
  //debug
  //System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
  //carico l'oggetto di risposta di alcune informazioni
  
    String date =response.getLastHeader("Date").toString().replace("Date:","").trim(); 
   
  if(date!=""){
       //System.out.println("date: "+date);
       risposta.setDate(date);
   }
  
  
 
  
 }//if 
 
  return risposta; 
	 
}//httpPost


/**
 * Method: GET
 * URI: /v1/{account}​{?limit,​marker,​end_marker,​format,​prefix,​delimiter}
 * Description: Shows details for a specified account and lists containers, sorted by name, in the account.
 * 
 * 
 * Show account details and list containers, and ask for a JSON response:
 * curl -i $publicURL?format=json -X GET -H "X-Auth-Token: $token" 
 * 
 * 
 * If the request succeeds, the operation returns one of these status codes:
 * 
 * 200. Success. The response body lists the containers.
 * 
 * 204. Success. The response body shows no containers
 * 
 * 
 * 
     * @param swiftParameterInput
 * @return 
 * @throws java.io.IOException
 */
public SwiftParameterOutput showAccountDetailsAndListContainers(SwiftParameterInput swiftParameterInput) throws IOException{
    
    if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertAccount){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertAccount);
    
    //creo questo oggetto di comodo
    InsertAccount insertAccount = new InsertAccount();

    //eseguo il dowcasting
    insertAccount = (InsertAccount) swiftParameterInput.ogg; // <----######
    
    insertAccount.setOperazione("list container information");
    
    //######################
    String url= insertAccount.getBase()+insertAccount.getAccount();
    //######################
    //debug
    //System.out.println(url);
    
    InfoListAccountDetailsForMongoDb risposta = new InfoListAccountDetailsForMongoDb();
    
     risposta = httpGet(url,insertAccount.getTokenId());    
    
     if(risposta.getStatusCode().equals("200")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertContainer
      
    risposta.setAccount(insertAccount.getAccount());
    risposta.setOperazione(insertAccount.getOperazione());
     
   }
   
    return risposta;
   }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//ShowAccountDetailsAndListContainers


/**
     * Normal response codes: 204
     * Error response codes: identityFault (400, 500, …), badRequest (400), 
     *                       unauthorized (401), forbidden (403), badMethod (405), 
     *                       overLimit (413), serviceUnavailable (503), 
     *                       itemNotFound (404)
     * @param url
     * @param X_Auth_Token
     * @return
     * @throws IOException 
     */    
private InfoListAccountDetailsForMongoDb httpGet(String url, String X_Auth_Token) throws IOException{
    
       
       DefaultHttpClient httpclient = new DefaultHttpClient();
     
      
       HttpGet http = new  HttpGet(url);
             
       http.setHeader("Content-type", "application/json");
       http.setHeader("Accept", "application/json");//*
       http.setHeader("X-Auth-Token", X_Auth_Token);
     
       HttpResponse response = httpclient.execute(http);
       
       System.out.println(response);
       
       System.out.println("Response Code : " 
                + response.getStatusLine().getStatusCode());
 
	BufferedReader rd = new BufferedReader(
	        new InputStreamReader(response.getEntity().getContent()));
 
	StringBuffer result = new StringBuffer();
	String line = "";
	while ((line = rd.readLine()) != null) {
		result.append(line);
	}
    
 //debug
 //System.out.println(result);
        
        
 //httpclient.close();
 
 
     //creo un oggetto risposta
  InfoListAccountDetailsForMongoDb risposta = new InfoListAccountDetailsForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  risposta.setResponse(result.toString());
  
   //solo se la richiesta è andata a buon fine carico le altre informazioni
   // contenute nell'oggetto insertObject 
 
  if(risp.equals("200")){   
      
  //debug
  //System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
  //carico l'oggetto di risposta di alcune informazioni
  
   String date =response.getLastHeader("Date").toString().replace("Date:","").trim(); 
   String X_Account_Object_Count = response.getLastHeader("X-Account-Object-Count").toString().replace("X-Account-Object-Count:","").trim();
   String X_Account_Container_Count = response.getLastHeader("X-Account-Container-Count").toString().replace("X-Account-Container-Count:","").trim(); 
   String X_Account_Object_Bytes_Used = response.getLastHeader("X-Account-Bytes-Used").toString().replace("X-Account-Bytes-Used:","").trim();
   
  // String X_Account_Meta_Name = response.getLastHeader("X-Account-Meta-"+"Rischio").toString().replace("X-Account-Meta-"+"Rischio:","").trim();
    
    
  if(date!=""){
       //debug
       //System.out.println("date: "+date);
       risposta.setDate(date);
   }
   
  if(X_Account_Object_Count!=""){
      //debug
      //System.out.println("X_Account_Object_Count: "+X_Account_Object_Count);
       risposta.setX_Account_Object_Count(Integer.parseInt(X_Account_Object_Count));
   }

  
  
  
  
  if(X_Account_Container_Count!=""){
      //debug
      //System.out.println("X_Account_Container_Count: "+X_Account_Container_Count);
        risposta.setX_Account_Container_Count(Integer.parseInt(X_Account_Container_Count));
   }
 
  
 
  
  if(X_Account_Object_Bytes_Used!=""){
      //debug
      //System.out.println("X_Account_Object_Bytes_Used: "+X_Account_Object_Bytes_Used);
       long l = Long.parseLong(X_Account_Object_Bytes_Used);
       risposta.setX_Account_Object_Bytes_Used(l);
   
   }
 
  
  
  
/*  
  if(X_Account_Meta_Name!=""){
       System.out.println("X_Account_Meta_Name: "+X_Account_Meta_Name);
       risposta.setX_Account_Meta_Name(X_Account_Meta_Name);
   }
  
*/  
  
  
  
}
  return risposta;
}//httpGet
  
  
  
//############################################################################
//Operazione sui Container
//############################################################################


/**
 * Method: PUT
 * URI: /v1/{account}/{container}
 * Description: Creates a container.
 * 
 * Create a container with no metadata: 
 * curl -i $publicURL/steven -X PUT -H "Content-Length: 0" -H "X-Auth-Token: $token"
 * 
 * HTTP/1.1 201 Created
 * Content-Length: 0
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx7f6b7fa09bc2443a94df0-0052d58b56
 * Date: Tue, 14 Jan 2014 19:09:10 GMT
 * 
 * Create a container with metadata:
 * curl -i $publicURL/marktwain -X PUT -H "X-Auth-Token: $token" -H "X-Container-Meta-Book: TomSawyer"
 * 
 * HTTP/1.1 201 Created
 * Content-Length: 0
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx06021f10fc8642b2901e7-0052d58f37
 * Date: Tue, 14 Jan 2014 19:25:43 GMT
 * 
 * @param swiftParameterInput
 * @return 
 * @throws IOException 
 */
public SwiftParameterOutput createContainer(SwiftParameterInput swiftParameterInput) throws IOException{
    
     if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertContainer){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertContainer);
    
    //creo questo oggetto di comodo
    InsertContainer insertContainer = new InsertContainer();

    //eseguo il dowcasting
    insertContainer = (InsertContainer) swiftParameterInput.ogg; // <----######
   
    
    
    //######################
    String url=insertContainer.getBase()+insertContainer.getAccount()+"/"+insertContainer.getContainer();
    //######################
    //debug
    System.out.println(url);
    
   InfoContainerForMongoDb risposta = httpPut(url,insertContainer.getTokenId());
   
   
   // 201 lo crea 
   // 202 lo crea ma già esisteva
   
   
   if(risposta.getStatusCode().equals("202") || risposta.getStatusCode().equals("201")){ 
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertContainer
      
    risposta.setAccount(insertContainer.getAccount());
    risposta.setContainer(insertContainer.getContainer());
    
   }
     
    return risposta;
    }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//createContainer

/**
 * Normal response codes: 201, 204
 * 
 * Create a container with no metadata: curl -i $publicURL/steven -X PUT 
 *                                                 -H "Content-Length: 0" 
 *                                                 -H "X-Auth-Token: $token"
 * 
 * Create a container with metadata: curl -i $publicURL/marktwain -X PUT 
 *                                                 -H "X-Auth-Token: $token" 
 *                                                 -H "X-Container-Meta-Book: TomSawyer" 
 * 
 * @param url
 * @param X_Auth_Token
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private InfoContainerForMongoDb httpPut( String url, String X_Auth_Token) throws UnsupportedEncodingException, IOException{
         
       DefaultHttpClient httpclient = new DefaultHttpClient();
      
       HttpPut http = new HttpPut(url);
      
       http.setHeader("Content-type", "application/json");
       http.setHeader("Accept", "application/json");
       http.setHeader("X-Auth-Token", X_Auth_Token);
       
       
      
       HttpResponse response = httpclient.execute(http);
       
     //  System.out.println(response);
     //  System.out.println("Response Code : "+response.getStatusLine().getStatusCode());
 
// httpclient.close();

       
  //creo un oggetto risposta
  InfoContainerForMongoDb risposta = new InfoContainerForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 
 
 if(risp.equals("202") || risp.equals("201")){   
      //debug
      // System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
      //carico l'oggetto di risposta di alcune unformazioni
  
    String date =response.getLastHeader("Date").toString().replace("Date:","").trim(); 
   
  if(date!=""){
       //System.out.println("date: "+date);
       risposta.setDate(date);
   }
  
  
 
  
 }//if 
 
  return risposta;       
       
      
}//createHttpRequestToKeystone

/**
 * Method: DELETE
 * URI: /v1/{account}/{container}
 * Description: Deletes an empty container.
 * 
 * Delete the steven container:
 * curl -i $publicURL/steven -X DELETE -H "X-Auth-Token: $token"
 * 
 * If the container does not exist, the response is:
 * HTTP/1.1 404 Not Found
 * Content-Length: 70
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx4d728126b17b43b598bf7-0052d81e34
 * Date: Thu, 16 Jan 2014 18:00:20 GMT
 * 
 * If the container exists and the deletion succeeds, the response is:
 * HTTP/1.1 204 No Content
 * Content-Length: 0
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: txf76c375ebece4df19c84c-0052d81f14
 * Date: Thu, 16 Jan 2014 18:04:04 GMT
 * 
 * If the container exists but is not empty, the response is:
 * HTTP/1.1 409 Conflict
 * Content-Length: 95
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx7782dc6a97b94a46956b5-0052d81f6b
 * Date: Thu, 16 Jan 2014 18:05:31 GMT
 * <html><h1>Conflict</h1><p>There was a conflict when trying to complete your request.</p></html>
 * 
 * @param swiftParameterInput
 * @return 
 * @throws IOException 
 */
public SwiftParameterOutput deleteContainer(SwiftParameterInput swiftParameterInput) throws IOException{
    
     if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertContainer){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertContainer);
    
    //creo questo oggetto di comodo
    InsertContainer insertContainer = new InsertContainer();

    //eseguo il dowcasting
    insertContainer = (InsertContainer) swiftParameterInput.ogg; // <----######
    
    //######################
    String url=insertContainer.getBase()+insertContainer.getAccount()+"/"+insertContainer.getContainer();
    //######################
    //debug
    System.out.println(url);
    
   InfoContainerForMongoDb risposta = httpDeleteContainer(url,insertContainer.getTokenId());
   
   if(risposta.getStatusCode().equals("204")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertContainer
      
    risposta.setAccount(insertContainer.getAccount());
    risposta.setContainer(insertContainer.getContainer());
    
   }
   
    return risposta;
    
    }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//deleteContainer

/**
 * Normal response codes: 204
 * Error response codes: NotFound (404), Conflict (409)
 * @param url
 * @param X_Auth_Token
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private InfoContainerForMongoDb httpDeleteContainer( String url, String X_Auth_Token) throws UnsupportedEncodingException, IOException{
         
    DefaultHttpClient httpclient = new DefaultHttpClient();
      
    HttpDelete http = new HttpDelete(url);
      
    http.setHeader("Content-type", "application/json");
    http.setHeader("Accept", "application/json");
    http.setHeader("X-Auth-Token", X_Auth_Token);
 
       
    HttpResponse response = httpclient.execute(http);
       
    // System.out.println(response);
     System.out.println("Response Code : "+response.getStatusLine().getStatusCode());
 
//    httpclient.close();
 
    //creo un oggetto risposta
  InfoContainerForMongoDb risposta = new InfoContainerForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  
  
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 
 
 if(risp.equals("204")){   
      //debug
      //System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
      //carico l'oggetto di risposta di alcune unformazioni
  
    String date =response.getLastHeader("Date").toString().replace("Date:","").trim(); 
   
  if(date!=""){
       //System.out.println("date: "+date);
       risposta.setDate(date);
   }
  
  
 
  
 }//if 
 
  return risposta;       
       
 
  }//httpDelete



//############################################################################
// Operazioni su container metadata
//############################################################################

/**
 * Method: POST
 * URI: /v1/{account}/{container}
 * Description: creates, custom metadata for a container.
 * 
 * Create container metadata:
 * curl -i $publicURL/marktwain -X POST -H "X-Auth-Token: $token" 
 *                              -H "X-Container-Meta-Author: MarkTwain" 
 *                              -H "X-Container-Meta-Century: Nineteenth"
 * 
 * HTTP/1.1 204 No Content
 * Content-Length: 0
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx05dbd434c651429193139-0052d82635
 * Date: Thu, 16 Jan 2014 18:34:29 GMT
 * 
 * @param swiftParameterInput 
 * @return  
 * @throws java.io.IOException 
 */
public SwiftParameterOutput createContainerMetadata(SwiftParameterInput swiftParameterInput) throws IOException{

    if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertContainer){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertContainer);
    
    //creo questo oggetto di comodo
    InsertContainer insertContainer = new InsertContainer();

    //eseguo il dowcasting
    insertContainer = (InsertContainer) swiftParameterInput.ogg; // <----######
    
    insertContainer.setOperazione("create container metadata");

    //######################
    String url=insertContainer.getBase()+insertContainer.getAccount()+"/"+insertContainer.getContainer();
    //######################
    //debug
    //System.out.println(url);
    
    InfoOperationContainerMetadataForMongoDb risposta = httpPost(url,insertContainer.getTokenId(), insertContainer.getX_Container_Meta(), insertContainer.getName());
    
    if(risposta.getStatusCode().equals("204")){ 
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertContainer
      
    risposta.setAccount(insertContainer.getAccount());
    risposta.setContainer(insertContainer.getContainer());
    risposta.setOperazione(insertContainer.getOperazione());
    risposta.setX_Container_Meta(insertContainer.getX_Container_Meta());
    risposta.setName(insertContainer.getName());
   }
    
   //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput;  
 
 }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//createContainerMetadata


/**
 * Normal response codes: 204
 * 
 * Create container metadata:
 * curl -i $publicURL/marktwain -X POST -H "X-Auth-Token: $token" 
 *                              -H "X-Container-Meta-Author: MarkTwain" 
 *                              -H "X-Container-Meta-Century: Nineteenth"
 * 
 * 
 * @param url_servizio
 * @param X_Auth_Token
 * @param X_Container_Meta1    Author
 * @param name1                MarkTwain  
 * @param X_Container_Meta2    Century
 * @param name2                Nineteenth
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private InfoOperationContainerMetadataForMongoDb httpPost( String url, String X_Auth_Token, String X_Container_Meta, String name) throws UnsupportedEncodingException, IOException{
         
       DefaultHttpClient httpclient = new DefaultHttpClient();
      
       HttpPost http = new HttpPost(url);
      
       http.setHeader("Content-type", "application/json");
       http.setHeader("Accept", "application/json");
       http.setHeader("X-Auth-Token", X_Auth_Token);
       http.setHeader("X-Container-Meta-"+X_Container_Meta, name);
       
       HttpResponse response = httpclient.execute(http);
       
    System.out.println(response);
    //System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
 
    // httpclient.close();
       
    //creo un oggetto risposta
    InfoOperationContainerMetadataForMongoDb risposta = new InfoOperationContainerMetadataForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 
 
 if(risp.equals("204") ){   
      //debug
      // System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
      //carico l'oggetto di risposta di alcune unformazioni
  String date =  response.getLastHeader("Date").toString().replace("Date:","").trim(); 
      
  if(date!=""){
       //System.out.println("date: "+date);
       risposta.setDate(date);
   }
  
  
 
  
 }//if 
 
  return risposta;       
    
    
}//httpPost

/**
 * Method: POST
 * URI: /v1/{account}/{container}
 * Description: updates custom metadata for a container.
 * Update container metadata:
 * curl -i $publicURL/marktwain -X POST -H "X-Auth-Token: $token" 
 *                                      -H "X-Container-Meta-Author: SamuelClemens"
 * 
 * HTTP/1.1 204 No Content
 * Content-Length: 0
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: txe60c7314bf614bb39dfe4-0052d82653
 * Date: Thu, 16 Jan 2014 18:34:59 GMT
 * @param swiftParameterInput 
 * @return  
 * @throws java.io.IOException 
 */
public SwiftParameterOutput updateContainerMetadata(SwiftParameterInput swiftParameterInput) throws IOException{
 
    if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertContainer){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertContainer);
    
    //creo questo oggetto di comodo
    InsertContainer insertContainer = new InsertContainer();

    //eseguo il dowcasting
    insertContainer = (InsertContainer) swiftParameterInput.ogg; // <----######
    
    insertContainer.setOperazione("update container metadata");

    //######################
    String url=insertContainer.getBase()+insertContainer.getAccount()+"/"+insertContainer.getContainer();
    //######################
    //debug
    //System.out.println(url);
    
    InfoOperationContainerMetadataForMongoDb risposta = httpPost(url,insertContainer.getTokenId(), insertContainer.getX_Container_Meta(), insertContainer.getName());
    
    if(risposta.getStatusCode().equals("204")){ 
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertContainer
      
    risposta.setAccount(insertContainer.getAccount());
    risposta.setContainer(insertContainer.getContainer());
    risposta.setOperazione(insertContainer.getOperazione());
    risposta.setX_Container_Meta(insertContainer.getX_Container_Meta());
    risposta.setName(insertContainer.getName());
   }
  
    //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput;  
    
    
 }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//updateContainerMetadata


/**
 * Method: POST
 * URI: /v1/{account}/{container}
 * Description: deletes custom metadata for a container.
 * Delete container metadata:
 * curl -i $publicURL/marktwain -X POST -H "X-Auth-Token: $token" 
 *                                      -H "X-Remove-Container-Meta-Century: x"
 * 
 * HTTP/1.1 204 No Content
 * Content-Length: 0
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx7997e18da2a34a9e84ceb-0052d826d0
 * Date: Thu, 16 Jan 2014 18:37:04 GMT
     * @param swiftParameterInput 
 * @return  
 * @throws java.io.IOException  
 */
public SwiftParameterOutput deleteContainerMetadata(SwiftParameterInput swiftParameterInput) throws IOException{
    
    
    if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertContainer){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertContainer);
    
    //creo questo oggetto di comodo
    InsertContainer insertContainer = new InsertContainer();

    //eseguo il dowcasting
    insertContainer = (InsertContainer) swiftParameterInput.ogg; // <----######
    
    insertContainer.setOperazione("delete container metadata");

    //######################
    String url=insertContainer.getBase()+insertContainer.getAccount()+"/"+insertContainer.getContainer();
    //######################
    //debug
    //System.out.println(url);
    
    InfoOperationContainerMetadataForMongoDb risposta = httpPostDelete(url,insertContainer.getTokenId(), insertContainer.getX_Container_Meta(), insertContainer.getName());
    
    if(risposta.getStatusCode().equals("204")){ 
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertContainer
      
    risposta.setAccount(insertContainer.getAccount());
    risposta.setContainer(insertContainer.getContainer());
    risposta.setOperazione(insertContainer.getOperazione());
    risposta.setX_Container_Meta(insertContainer.getX_Container_Meta());
    risposta.setName(insertContainer.getName());
   }
   
 
    //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput;  
    
      
 }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//deleteContainerMetadata

/**
 *Normal response codes: 204
 * 
 * Delete container metadata:
 * curl -i $publicURL/marktwain -X POST -H "X-Auth-Token: $token" -H "X-Remove-Container-Meta-Century: x"
 * 
 * @param url_servizio
 * @param X_Auth_Token
 * @param X_Container_Meta     Author
 * @param name                 SamuelClemens
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private InfoOperationContainerMetadataForMongoDb httpPostDelete( String url, String X_Auth_Token,String X_Container_Meta, String name) throws UnsupportedEncodingException, IOException{
         
       DefaultHttpClient httpclient = new DefaultHttpClient();
      
       HttpPost http = new HttpPost(url);
      
       http.setHeader("Content-type", "application/json");
       http.setHeader("Accept", "application/json");
       http.setHeader("X-Auth-Token", X_Auth_Token);
       if((X_Container_Meta!= null) && (name !=null)){
       http.setHeader("X-Remove-Container-Meta-"+X_Container_Meta, name);
       }
       
       HttpResponse response = httpclient.execute(http);
       
      // System.out.println(response);
      // System.out.println("Response Code : "+response.getStatusLine().getStatusCode());
 
         //creo un oggetto risposta
    InfoOperationContainerMetadataForMongoDb risposta = new InfoOperationContainerMetadataForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 
 
 if(risp.equals("204") ){ 
     
  //debug
  // System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
      //carico l'oggetto di risposta di alcune unformazioni
  String date =  response.getLastHeader("Date").toString().replace("Date:","").trim(); 
      
  if(date!=""){
       //System.out.println("date: "+date);
       risposta.setDate(date);
   }
  
 }//if 
 
  return risposta;       
    
    
  
  }//httpPostDelete

/**
 * Method: HEAD
 * URI:	/v1/{account}/{container}
 * Description: Shows container metadata, including the number of objects and the total bytes of all objects stored in the container.
 * 
 * Show container metadata request:
 * curl -i $publicURL/marktwain -X HEAD -H "X-Auth-Token: $token"
 * 
 * HTTP/1.1 204 No Content
 * Content-Length: 0
 * X-Container-Object-Count: 1
 * Accept-Ranges: bytes
 * X-Container-Meta-Book: TomSawyer
 * X-Timestamp: 1389727543.65372
 * X-Container-Meta-Author: SamuelClemens
 * X-Container-Bytes-Used: 14
 * Content-Type: text/plain; charset=utf-8
 * X-Trans-Id: tx0287b982a268461b9ec14-0052d826e2
 * Date: Thu, 16 Jan 2014 18:37:22 GMT
 * 
 * @param swiftParameterInput 
 * @return  
 * @throws java.io.IOException 
 */
public SwiftParameterOutput showContainerMetadata(SwiftParameterInput swiftParameterInput) throws IOException{
    
    if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertContainer){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertContainer);
    
    //creo questo oggetto di comodo
    InsertContainer insertContainer = new InsertContainer();

    //eseguo il dowcasting
    insertContainer = (InsertContainer) swiftParameterInput.ogg; // <----######
    
    insertContainer.setOperazione("show container metadata");
    
    //######################
    String url=insertContainer.getBase()+insertContainer.getAccount()+"/"+insertContainer.getContainer();
    //######################
    //debug
    //System.out.println(url);
    
    InfoListContainerForMongoDb risposta =httpHeadContainer(url,insertContainer.getTokenId());
    
     if(risposta.getStatusCode().equals("204")){ 
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertContainer
      
    risposta.setAccount(insertContainer.getAccount());
    risposta.setContainer(insertContainer.getContainer());
    risposta.setOperazione(insertContainer.getOperazione());
    
   }
   
     
     //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput;  
 
     
 }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//containerMetadata

/**
 * Normal response codes: 204
 * 
 * curl -i $publicURL/marktwain -X HEAD -H "X-Auth-Token: $token"
 * 
 * @param url_servizio
 * @param X_Auth_Token
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private InfoListContainerForMongoDb httpHeadContainer( String url, String X_Auth_Token ) throws UnsupportedEncodingException, IOException{
         
       DefaultHttpClient httpclient = new DefaultHttpClient();
      
       HttpHead http = new HttpHead(url);
                       
       http.setHeader("Content-type", "application/json");
       http.setHeader("Accept", "application/json");
       http.setHeader("X-Auth-Token", X_Auth_Token);

       HttpResponse response = httpclient.execute(http);
       
       //System.out.println(response);
       //System.out.println("Response Code : "+response.getStatusLine().getStatusCode());
 
        
	BufferedReader rd = new BufferedReader(
	        new InputStreamReader(response.getEntity().getContent()));
 
	StringBuffer result = new StringBuffer();
	String line = "";
	while ((line = rd.readLine()) != null) {
		result.append(line);
	}
       
       
       
            //creo un oggetto risposta
    InfoListContainerForMongoDb risposta = new InfoListContainerForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  risposta.setResponse(result.toString());
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 
 
 if(risp.equals("204") ){ 
     
  //debug
  // System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
  //carico l'oggetto di risposta di alcune unformazioni
  String date =  response.getLastHeader("Date").toString().replace("Date:","").trim(); 
  
  String X_Container_Object_Count = response.getLastHeader("X-Container-Object-Count").toString().replace("X-Container-Object-Count:","").trim();
  String X_Container_Object_Bytes_Used = response.getLastHeader("X-Container-Bytes-Used").toString().replace("X-Container-Bytes-Used:","").trim();
   
  // String X_Account_Meta_Name = response.getLastHeader("X-Account-Meta-"+"Rischio").toString().replace("X-Account-Meta-"+"Rischio:","").trim();
    
  
  
  
  if(date!=""){
       //System.out.println("date: "+date);
       risposta.setDate(date);
   }
  
  
   if(X_Container_Object_Count!=""){
      //debug
      //System.out.println("X_Account_Container_Count: "+X_Account_Container_Count);
        risposta.setX_Container_Object_Count(Integer.parseInt(X_Container_Object_Count));
   }
 
  
 
  
  if(X_Container_Object_Bytes_Used!=""){
      //debug
      //System.out.println("X_Account_Object_Bytes_Used: "+X_Account_Object_Bytes_Used);
       long l = Long.parseLong(X_Container_Object_Bytes_Used);
       risposta.setX_Container_Object_Bytes_Used(l);
   
   }
  
  
  /*  
  if(X_Account_Meta_Name!=""){
       System.out.println("X_Account_Meta_Name: "+X_Account_Meta_Name);
       risposta.setX_Account_Meta_Name(X_Account_Meta_Name);
   }
  
*/  
  
  
 }//if 
 
  return risposta;       
    
    
       
}//httpHead


//############################################################################
// Operazioni sugli object
//##############################################


/**
 * 
 * Method: PUT
 * URI:	/v1/{account}/{container}/{object}​{?multipart-manifest,​signature,​expires}
 * Description: Creates a new object with specified data content and metadata, or replaces an existing object with specified data content and metadata.
		

 * 
 * Create object:
 * curl -i $publicURL/janeausten/helloworld.txt -X PUT -H "Content-Length: 1" 
 *                                                     -H "Content-Type: text/html; charset=UTF-8" 
 *                                                     -H "X-Auth-Token: $token"
 * 
 * 
 * HTTP/1.1 201 Created
 * Last-Modified: Fri, 17 Jan 2014 17:28:35 GMT
 * Content-Length: 116
 * Etag: d41d8cd98f00b204e9800998ecf8427e
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx4d5e4f06d357462bb732f-0052d96843
 * Date: Fri, 17 Jan 2014 17:28:35 GMT
 * 
 * @param swiftParameterInput
 * @return 
 * @throws java.io.IOException
 */
public SwiftParameterOutput createObject(SwiftParameterInput swiftParameterInput) throws IOException{
   
    if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertObject){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertObject);
    
    //creo questo oggetto di comodo
    InsertObject insertObject = new InsertObject();

    //eseguo il dowcasting
    insertObject = (InsertObject) swiftParameterInput.ogg; // <----######
        
   insertObject.setOperazione("create object");
    
   String urlFinale=insertObject.getBase()+insertObject.getAccount()+"/"+insertObject.getContainer()+"/"+insertObject.getObject();
   
   //debug
   //System.out.println("url passato alla richiesta http: "+urlFinale); 
   
   InfoCreateObjectForMongoDb risposta = httpPutCreateObject(urlFinale,insertObject.getTokenId(),insertObject.getPathObject());
    
   if(risposta.getStatusCode().equals("201")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertObject
      
    risposta.setAccount(insertObject.getAccount());
    risposta.setContainer(insertObject.getContainer());
    risposta.setObject(insertObject.getObject());
    risposta.setContentLength(Long.toString(insertObject.getObjectLength()));
     risposta.setOperazione(insertObject.getOperazione());
   }
   
   ///////////////////////////
   
   //creo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
    
    swiftParameterOutput.type=SwiftParameterOutput.tipoObjectOutput.InfoCreateObjectForMongoDb; //####
 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
   return swiftParameterOutput;
    
   ////////////////////////////
   
   
 //  return risposta;
   
   }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//createObject

/**
 * Normal response codes: 201
 * Error response codes: timeout (408), lengthRequired (411), unprocessableEntity (422)
 * 
 * @param url
 * @param X_Auth_Token
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private  InfoCreateObjectForMongoDb httpPutCreateObject( String url, String X_Auth_Token,String pathObject) throws IOException{
  
 int responseCode= 0;
    
  CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPut http = new HttpPut(url);
            File file = new File(pathObject);
            FileEntity entity = new FileEntity(file,ContentType.create("text/plain", "UTF-8"));        
 
            http.setHeader("X-Auth-Token", X_Auth_Token);
            http.setEntity(entity);
            
            
            
            System.out.println("executing request " + http.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(http);
            try {
                //System.out.println("----------------------------------------");
                //System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                responseCode= response.getStatusLine().getStatusCode();
                if (resEntity != null) {
                 //   System.out.println("Response content length: " + resEntity.getContentLength());
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }

    
   //creo un oggetto risposta
   InfoCreateObjectForMongoDb risposta = new InfoCreateObjectForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(responseCode);
  risposta.setStatusCode(risp);
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 

/*  
  
 if(risp.equals("201")){   
      //debug
      //System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
      //carico l'oggetto di risposta di alcune unformazioni
  
  String date = urlconnection.getHeaderField("Date");
     //debug
     //System.out.println("date: "+date);
     risposta.setDate(date);
     
     String etag = urlconnection.getHeaderField("Etag");
     //debug
     //System.out.println("etag: "+etag);
     risposta.setEtag(etag);
  
     String Last_Modified = urlconnection.getHeaderField("Last-Modified"); 
     //debug
     //System.out.println("Last-Modified: "+Last_Modified);
     risposta.setLastModified(Last_Modified);
     
        
  
 }//if 
*/
 
  return risposta;   
    
    
}//httpPost

/**
 * 
 * Method: PUT
 * URI:	/v1/{account}/{container}/{object}​{?multipart-manifest,​signature,​expires}
 * Description: Creates a new object with specified data content and metadata, or replaces an existing object with specified data content and metadata.
		

 * 
 * Replace object:
 * curl -i $publicURL/janeausten/helloworld.txt -X PUT -H "Content-Length: 1" 
 *                                                     -H "Content-Type: text/html; charset=UTF-8" 
 *                                                     -H "X-Auth-Token: $token"
 * 
 * 
 * HTTP/1.1 201 Created
 * Last-Modified: Fri, 17 Jan 2014 17:28:35 GMT
 * Content-Length: 116
 * Etag: d41d8cd98f00b204e9800998ecf8427e
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx4d5e4f06d357462bb732f-0052d96843
 * Date: Fri, 17 Jan 2014 17:28:35 GMT
 * 
 * @param swiftParameterInput
 * @return 
 * @throws java.io.IOException 
 */ 
public SwiftParameterOutput replaceObject(SwiftParameterInput swiftParameterInput) throws IOException{
   
   if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertObject){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertObject);
    
    //creo questo oggetto di comodo
    InsertObject insertObject = new InsertObject();

    //eseguo il dowcasting
    insertObject = (InsertObject) swiftParameterInput.ogg; // <----######
    
   insertObject.setOperazione("replace object"); 
     
   String urlFinale=insertObject.getBase()+insertObject.getAccount()+"/"+insertObject.getContainer()+"/"+insertObject.getObject();
   
   //debug
   //System.out.println("url passato alla richiesta http: "+urlFinale); 
   
   InfoCreateObjectForMongoDb risposta = httpPutCreateObject(urlFinale,insertObject.getTokenId(),insertObject.getPathObject());
    
   if(risposta.getStatusCode().equals("201")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertObject
      
    risposta.setAccount(insertObject.getAccount());
    risposta.setContainer(insertObject.getContainer());
    risposta.setObject(insertObject.getObject());
    risposta.setContentLength(Long.toString(insertObject.getObjectLength()));
    risposta.setOperazione(insertObject.getOperazione());
   }
   
    
     //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput;  
   
    }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
   
}//replaceObject

/**
 * 
 * Method: COPY
 * URI:/v1/{account}/{container}/{object}
 * Description: Copies an object to another object in the object store.
 * 
 * Copy the goodbye object from the marktwain container to the janeausten container: 
 *
 * curl -i $publicURL/marktwain/goodbye -X COPY -H "X-Auth-Token: $token" 
 *                                              -H "Destination: janeausten/goodbye"
 * 
 * 
 * HTTP/1.1 201 Created
 * Content-Length: 0
 * X-Copied-From-Last-Modified: Thu, 16 Jan 2014 21:19:45 GMT
 * X-Copied-From: marktwain/goodbye
 * Last-Modified: Fri, 17 Jan 2014 18:22:57 GMT
 * Etag: 451e372e48e0f6b1114fa0724aa79fa1
 * Content-Type: text/html; charset=UTF-8
 * X-Object-Meta-Movie: AmericanPie
 * X-Trans-Id: txdcb481ad49d24e9a81107-0052d97501
 * Date: Fri, 17 Jan 2014 18:22:57 GMT
 * 
 * Alternatively, you can use PUT to copy the goodbye object from the marktwain 
 * container to the janeausten container. This request requires a Content-Length 
 * header even if it is set to zero (0).
 * 
 * curl -i $publicURL/janeausten/goodbye -X PUT -H "X-Auth-Token: $token" 
 *                                              -H "X-Copy-From: /marktwain/goodbye" 
 *                                              -H "Content-Length: 0"
 * 
 * HTTP/1.1 201 Created
 * Content-Length: 0
 * X-Copied-From-Last-Modified: Thu, 16 Jan 2014 21:19:45 GMT
 * X-Copied-From: marktwain/goodbye
 * Last-Modified: Fri, 17 Jan 2014 18:22:57 GMT
 * Etag: 451e372e48e0f6b1114fa0724aa79fa1
 * Content-Type: text/html; charset=UTF-8
 * X-Object-Meta-Movie: AmericanPie
 * X-Trans-Id: txdcb481ad49d24e9a81107-0052d97501
 * Date: Fri, 17 Jan 2014 18:22:57 GMT
 * 
 * @param swiftParameterInput
 * @return 
 * @throws java.io.IOException
 */
public SwiftParameterOutput copyObject(SwiftParameterInput swiftParameterInput) throws IOException{
   
   if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertObject){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertObject);
    
    //creo questo oggetto di comodo
    InsertObject insertObject = new InsertObject();

    //eseguo il dowcasting
    insertObject = (InsertObject) swiftParameterInput.ogg; // <----###### 
    
   insertObject.setOperazione("copy object"); 
     
   String urlFinale=insertObject.getBase()+insertObject.getAccount()+"/"+insertObject.getContainerDestination()+"/"+insertObject.getObjectDestination();
   //debug
   //System.out.println("url passato alla richiesta http: "+urlFinale);  
     
    
    String containerObjectOrigin = "/"+insertObject.getContainerOrigin()+"/"+insertObject.getObjectOrigin();
    //debug
    //System.out.println(containerObjectOrigin);
    
    
    InfoCopyObjectForMongoDb risposta = httpPut(urlFinale,insertObject.getTokenId(),containerObjectOrigin);
    
    if(risposta.getStatusCode().equals("201")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertObject
      
    risposta.setAccount(insertObject.getAccount());
    risposta.setContainerOrigin(insertObject.getContainerOrigin());
    risposta.setObjectOrigin(insertObject.getObjectOrigin());
    risposta.setContainerDestination(insertObject.getContainerDestination());
    risposta.setObjectDestination(insertObject.getObjectDestination());
    risposta.setOperazione(insertObject.getOperazione());
   }
   
      //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput; 
   
    }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//copyObject

/**
 * 
 * Normal response codes: 201
 *
 * Alternatively, you can use PUT to copy the goodbye object from the marktwain 
 * container to the janeausten container. This request requires a Content-Length 
 * header even if it is set to zero (0).
 * 
 * curl -i $publicURL/janeausten/goodbye -X PUT -H "X-Auth-Token: $token" 
 *                                              -H "X-Copy-From: /marktwain/goodbye" 
 *                                              -H "Content-Length: 0"
 * 
 * 
 * @param url
 * @param X_Auth_Token
 * @return
 * @throws IOException 
 */
private InfoCopyObjectForMongoDb httpPut(String url, String X_Auth_Token,String containerObjectDestination) throws IOException{
  
    URL obj = new URL(url);
    HttpURLConnection con = null;
        
    con=(HttpURLConnection) obj.openConnection();
     
    con.setRequestMethod("PUT");
    con.setRequestProperty("X-Auth-Token",X_Auth_Token );//ok
    con.setRequestProperty("X-Copy-From", containerObjectDestination);
    con.setRequestProperty("Accept-Charset", "UTF-8");
    con.setChunkedStreamingMode(0);
    con.setDoOutput(true);
    con.setDoInput(true);
 
    //debug
    //System.out.println("Content-Length : " + con.getContentLength());
        
    int responseCode = con.getResponseCode();
    //debug
    //System.out.println("\nSending 'PUT' request to URL : " + url2);
    //System.out.println("Response Code : " + responseCode);
    //System.out.println("Response Message : " + con.getResponseMessage());
    
    //creo un oggetto risposta
    InfoCopyObjectForMongoDb risposta = new InfoCopyObjectForMongoDb();
      
    //carico l'url 
    risposta.setUrl(url);
    //carico lo status code
    String risp =  Integer.toString(responseCode);
    risposta.setStatusCode(risp);
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 
 
 if(risp.equals("201")){   
     //debug
     //System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
      //carico l'oggetto di risposta di alcune unformazioni

     String date = con.getHeaderField("Date");
     //debug
     //System.out.println("date: "+date);
     risposta.setDate(date);
     
     String etag = con.getHeaderField("Etag");
     //debug
     //System.out.println("etag: "+etag);
     risposta.setEtag(etag);
     
     String X_Copied_From_Last_Modified = con.getHeaderField("X-Copied-From-Last-Modified"); 
     //debug
     //System.out.println("X-Copied-From-Last-Modified: "+X_Copied_From_Last_Modified);
     risposta.setX_Copied_From_Last_Modified(X_Copied_From_Last_Modified);
     
     String X_Copied_From = con.getHeaderField("X-Copied-From"); 
     //debug
     //System.out.println("X-Copied-From: "+X_Copied_From);
     risposta.setX_Copied_From(X_Copied_From);
     
     String Last_Modified = con.getHeaderField("Last-Modified"); 
     //debug
     //System.out.println("Last-Modified: "+Last_Modified);
     risposta.setLast_Modified(Last_Modified);
     
     //String X_Object_Meta_name; non gestito in questa versione
     
     
 
 }//if 
 
 
 //chiudo la connessione 
 con.disconnect();
  
  return risposta;
    
    
 
}//httpPut

public SwiftParameterOutput deleteObject(SwiftParameterInput swiftParameterInput) throws IOException{
   
   if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertObject){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertObject);
    
    //creo questo oggetto di comodo
    InsertObject insertObject = new InsertObject();

    //eseguo il dowcasting
    insertObject = (InsertObject) swiftParameterInput.ogg; // <----######
    
   insertObject.setOperazione("delete object"); 
     
   String urlFinale=insertObject.getBase()+insertObject.getAccount()+"/"+insertObject.getContainer()+"/"+insertObject.getObject();
   
   //debug
   //System.out.println("url passato alla richiesta http: "+urlFinale); 
    
   
   InfoDeleteObjectForMongoDb risposta = httpDeleteObject(urlFinale,insertObject.getTokenId());
    
   if(risposta.getStatusCode().equals("204")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertObject
      
    risposta.setAccount(insertObject.getAccount());
    risposta.setContainer(insertObject.getContainer());
    risposta.setObject(insertObject.getObject());
    risposta.setOperazione(insertObject.getOperazione());
    
   }
   
    
     //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput;  
   
    }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
   
  
    
}//deleteObject

/**
 * Normal response codes: 204
 * Error response codes: NotFound (404), Conflict (409)
 * @param url
 * @param X_Auth_Token
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private InfoDeleteObjectForMongoDb httpDeleteObject( String url, String X_Auth_Token) throws UnsupportedEncodingException, IOException{
         
    DefaultHttpClient httpclient = new DefaultHttpClient();
      
    HttpDelete http = new HttpDelete(url);
      
    http.setHeader("Content-type", "application/json");
    http.setHeader("Accept", "application/json");
    http.setHeader("X-Auth-Token", X_Auth_Token);
 
       
    HttpResponse response = httpclient.execute(http);
       
    // System.out.println(response);
    // System.out.println("Response Code : "+response.getStatusLine().getStatusCode());
 
//    httpclient.close();
 
    //creo un oggetto risposta
  InfoDeleteObjectForMongoDb risposta = new InfoDeleteObjectForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 
 
 if(risp.equals("204")){   
      //debug
      //System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
      //carico l'oggetto di risposta di alcune unformazioni
  
    String date =response.getLastHeader("Date").toString().replace("Date:","").trim(); 
   
  if(date!=""){
       //System.out.println("date: "+date);
       risposta.setDate(date);
   }
  
  
 
  
 }//if 
 
  return risposta;       
       
 
  }//httpDelete


/**
 * curl –X GET 
 * -H "X-Auth-Token: fc81aaa6-98a1-9ab0-94ba-aba9a89aa9ae" \
 * https://storage.swiftdrive.com/v1/CF_xer7_343/dogs/JingleRocky.jpg > JingleRocky.jpg
 * 
 *  
 * @param swiftParameterInput
 * @return
 * @throws IOException 
 */
public SwiftParameterOutput downloadObject(SwiftParameterInput swiftParameterInput) throws IOException{
    
   if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertObject){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertObject);
    
    //creo questo oggetto di comodo
    InsertObject insertObject = new InsertObject();

    //eseguo il dowcasting
    insertObject = (InsertObject) swiftParameterInput.ogg; // <----###### 
    
   insertObject.setOperazione("download object"); 
     
   String urlFinale=insertObject.getBase()+insertObject.getAccount()+"/"+insertObject.getContainer()+"/"+insertObject.getObject();
   
   //debug
   //System.out.println("url passato alla richiesta http: "+urlFinale); 
   
   InfoGetObjectForMongoDb risposta = httpGetdownloadObject(urlFinale,insertObject.getTokenId(),insertObject.getPathObject(),insertObject.getObject());
    
   if(risposta.getStatusCode().equals("200")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertObject
      
    risposta.setAccount(insertObject.getAccount());
    risposta.setContainer(insertObject.getContainer());
    risposta.setObject(insertObject.getObject());
    risposta.setOperazione(insertObject.getOperazione());
    risposta.setPathObjec(insertObject.getPathObject());
   }
   
    
     //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput;  
  
    }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
   
}//downloadObject

private InfoGetObjectForMongoDb httpGetdownloadObject(String url, String X_Auth_Token,String pathDestination, String fileName) throws IOException{
 

        URL url1 = new URL(url);
        HttpURLConnection httpConn = (HttpURLConnection) url1.openConnection();
        
        httpConn.setRequestMethod("GET");
        httpConn.setRequestProperty("X-Auth-Token",X_Auth_Token );
        
        int responseCode = httpConn.getResponseCode();
       
        // opens input stream from the HTTP connection
        InputStream inputStream = httpConn.getInputStream();
        String saveFilePath = pathDestination+fileName;
             
        // opens an output stream to save into file
        FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
        int BUFFER_SIZE = 4096;
        int bytesRead = -1;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
             outputStream.write(buffer, 0, bytesRead);
         }
 
        outputStream.close();
        inputStream.close();
 
        
     String risp =  Integer.toString(responseCode);
      
     //creo un oggetto risposta
  InfoGetObjectForMongoDb risposta = new InfoGetObjectForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  risposta.setStatusCode(risp);
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 
 
 if(risp.equals("200")){   
 
     //debug
     //System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
      //carico l'oggetto di risposta di alcune unformazioni

     String date = httpConn.getHeaderField("Date");
     //debug
     //System.out.println("date: "+date);
     risposta.setDate(date);
     
     String etag = httpConn.getHeaderField("Etag");
     //debug
     //System.out.println("etag: "+etag);
     risposta.setEtag(etag);
 
      String Last_Modified = httpConn.getHeaderField("Last-Modified"); 
     //debug
     //System.out.println("Last-Modified: "+Last_Modified);
     risposta.setLastModified(Last_Modified);
     
     
     String contentLength = httpConn.getHeaderField("Content-Length");
     //debug
     //System.out.println("contentLength: "+contentLength);
     risposta.setContentLength(contentLength);
     
     
    /* 
     String contentType = httpConn.getHeaderField("Content-Type");
     //debug
     System.out.println("contentType: "+contentType);
     //risposta.setContentLength(contentLength);
     
     String contentEncoding = httpConn.getHeaderField("Content-Encoding");
     //debug
     System.out.println("contentEncoding: "+contentEncoding);
     //risposta.setContentLength(contentLength);
     
     */
     
       
 }//if 
 
  return risposta; 
  
}//httpGetdownloadObject


//############################################################################
// Operazioni sugli object metadata
//############################################################################


/**
 * 
 * Method: HEAD
 * URI: /v1/{account}/{container}/{object}​{?signature,​expires}
 * Description: Shows object metadata.
 * 
 * Show object metadata:
 * curl -i $publicURL/marktwain/goodbye -X HEAD -H "X-Auth-Token: $token"
 * 
 * HTTP/1.1 200 OK
 * Content-Length: 14
 * Accept-Ranges: bytes
 * Last-Modified: Thu, 16 Jan 2014 21:12:31 GMT
 * Etag: 451e372e48e0f6b1114fa0724aa79fa1
 * X-Timestamp: 1389906751.73463
 * X-Object-Meta-Book: GoodbyeColumbus
 * Content-Type: application/octet-stream
 * X-Trans-Id: tx37ea34dcd1ed48ca9bc7d-0052d84b6f
 * Date: Thu, 16 Jan 2014 21:13:19 GMT
 * 
 * @param swiftParameterInput
 * @return 
 * @throws java.io.IOException 
 */
public SwiftParameterOutput  showObjectMetadata(SwiftParameterInput swiftParameterInput) throws IOException{

    if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertObject){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertObject);
    
    //creo questo oggetto di comodo
    InsertObject insertObject = new InsertObject();

    //eseguo il dowcasting
    insertObject = (InsertObject) swiftParameterInput.ogg; // <----######
    
    insertObject.setOperazione("list object metadata");
    
   String urlFinale=insertObject.getBase()+insertObject.getAccount()+"/"+insertObject.getContainer()+"/"+insertObject.getObject();
   
   //debug
   //System.out.println("url passato alla richiesta http: "+urlFinale);
   
   
   InfoListObjectMetadataForMongoDb  risposta =  httpHeadObject(urlFinale,insertObject.getTokenId());

    if(risposta.getStatusCode().equals("200")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertObject
      
    risposta.setAccount(insertObject.getAccount());
    risposta.setContainer(insertObject.getContainer());
    risposta.setObject(insertObject.getObject());
    risposta.setOperazione(insertObject.getOperazione());
     
   }
   
    
     //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput;  
 
   
    }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
   
    
}//ShowObjectMetadata

/**
 * Normal response codes: 204
 * 
 * curl -i $publicURL/marktwain -X HEAD -H "X-Auth-Token: $token"
 * 
 * @param url_servizio
 * @param X_Auth_Token
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private InfoListObjectMetadataForMongoDb httpHeadObject( String url, String X_Auth_Token ) throws UnsupportedEncodingException, IOException{
         
       DefaultHttpClient httpclient = new DefaultHttpClient();
      
       HttpHead http = new HttpHead(url);
                       
       http.setHeader("Content-type", "application/json");
       http.setHeader("Accept", "application/json");
       http.setHeader("X-Auth-Token", X_Auth_Token);

       HttpResponse response = httpclient.execute(http);
       
       //System.out.println(response);
       //System.out.println("Response Code : "+response.getStatusLine().getStatusCode());
 
            //creo un oggetto risposta
    InfoListObjectMetadataForMongoDb risposta = new InfoListObjectMetadataForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  risposta.setResponse(response.toString());
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 
 
 if(risp.equals("200") ){ 
     
  //debug
  // System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
  //carico l'oggetto di risposta di alcune unformazioni
  String date =  response.getLastHeader("Date").toString().replace("Date:","").trim(); 
  String etag =  response.getLastHeader("Etag").toString().replace("Etag:","").trim();
  
 
  // String X_Account_Meta_Name = response.getLastHeader("X-Account-Meta-"+"Rischio").toString().replace("X-Account-Meta-"+"Rischio:","").trim();
    
  if(date!=""){
       //System.out.println("date: "+date);
       risposta.setDate(date);
  }
  
  
  if(etag!=""){
       //System.out.println("etag: "+etag);
       risposta.setEtag(etag);
   }
  
  
  
  /*  
  if(X_Account_Meta_Name!=""){
       System.out.println("X_Account_Meta_Name: "+X_Account_Meta_Name);
       risposta.setX_Account_Meta_Name(X_Account_Meta_Name);
   }
  
*/  
  
  
 }//if 
 
  return risposta;       
    
    
       
}//httpHead

/**
 * Method: POST
 * URI: /v1/{account}/{container}/{object}
 * Description: Creates object metadata.
 * 
 * Create object metadata:
 * curl -i $publicURL/marktwain/goodbye -X POST -H "X-Auth-Token: $token" 
 *                                              -H "X-Object-Meta-Book: GoodbyeColumbus"
 * 
 * 
 * HTTP/1.1 202 Accepted
 * Content-Length: 76
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: txb5fb5c91ba1f4f37bb648-0052d84b3f
 * Date: Thu, 16 Jan 2014 21:12:31 GMT
 * <html><h1>Accepted</h1><p>The request is accepted for processing.</p></html>
 * 
 * @param swiftParameterInput
 * @return 
 * @throws java.io.IOException 
  */
public SwiftParameterOutput  createObjectMetadata(SwiftParameterInput swiftParameterInput) throws IOException{
    
   if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertObject){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertObject);
    
    //creo questo oggetto di comodo
    InsertObject insertObject = new InsertObject();

   //eseguo il dowcasting
    insertObject = (InsertObject) swiftParameterInput.ogg; // <----###### 
    
   insertObject.setOperazione("create object metadata");
    
   String urlFinale=insertObject.getBase()+insertObject.getAccount()+"/"+insertObject.getContainer()+"/"+insertObject.getObject();
   
   //debug
   //System.out.println("url passato alla richiesta http: "+urlFinale); 
    
    InfoCreateObjectMetadataForMongoDb  risposta = httpPostObjectMetadata(urlFinale,insertObject.getTokenId(),insertObject.getX_Container_Meta(),insertObject.getName());
    
    
    if(risposta.getStatusCode().equals("202")||risposta.getStatusCode().equals("201")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertObject
      
    risposta.setAccount(insertObject.getAccount());
    risposta.setContainer(insertObject.getContainer());
    risposta.setObject(insertObject.getObject());
    risposta.setX_Container_Meta(insertObject.getX_Container_Meta());
    risposta.setName(insertObject.getName());
    risposta.setOperazione(insertObject.getOperazione());
     
   }
   
    
     //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput;  
   
    }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
  
}//createObjectMetadata

/**
 * 
 * @param url
 * @param X_Auth_Token
 * @param X_Container_Meta
 * @param name
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private InfoCreateObjectMetadataForMongoDb httpPostObjectMetadata( String url, String X_Auth_Token,String X_Container_Meta, String name) throws UnsupportedEncodingException, IOException{
         
    
    DefaultHttpClient httpclient = new DefaultHttpClient();
      
    HttpPut http = new  HttpPut(url);
     
     http.setHeader("X-Auth-Token", X_Auth_Token);
     http.setHeader("X-Object-Meta-"+X_Container_Meta, name);
               
     HttpResponse response = httpclient.execute(http);
       
     //System.out.println(response);
      
     //System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
 
	
    //creo un oggetto risposta
   InfoCreateObjectMetadataForMongoDb risposta = new InfoCreateObjectMetadataForMongoDb();
   
   
  //carico l'url 
  risposta.setUrl(url);
  //carico lo status code
  String risp =  Integer.toString(response.getStatusLine().getStatusCode());
  risposta.setStatusCode(risp);
  
 //solo se la richiesta è andata a buon fine carico le altre informazioni
 // contenute nell'oggetto insertObject 
 
 if(risp.equals("202")||risp.equals("201")){   
      //debug
      //System.out.println("La richiesta è andata a buon fine: "+risposta.getStatusCode()); 
      
      //carico l'oggetto di risposta di alcune unformazioni
  
  
   String date =response.getLastHeader("Date").toString().replace("Date:","").trim(); 
   
  if(date!=""){
       //System.out.println("date: "+date);
       risposta.setDate(date);
   }
  
  
 }//if 
 
  return risposta;
          
}//httpPostObjectMetadata

/**
 * Method: POST
 * URI: /v1/{account}/{container}/{object}
 * Description: Updates object metadata.
 * 
 * Update object metadata:
 * curl -i $publicURL/marktwain/goodbye -X POST -H "X-Auth-Token: $token" 
 *                                      -H "X-Object-Meta-Book: GoodbyeOldFriend"
 * 
 * HTTP/1.1 202 Accepted
 * Content-Length: 76
 * Content-Type: text/html; charset=UTF-8
 * X-Trans-Id: tx5ec7ab81cdb34ced887c8-0052d84ca4
 * Date: Thu, 16 Jan 2014 21:18:28 GMT
 * <html><h1>Accepted</h1><p>The request is accepted for processing.</p></html>
 * 
 * @param swiftParameterInput
 * @return 
 * @throws java.io.IOException
 */
public SwiftParameterOutput  updateObjectMetadata(SwiftParameterInput swiftParameterInput) throws IOException{
    
    if(swiftParameterInput.type == SwiftParameterInput.tipoObjectInput.InsertObject){
       
      //debug
     //System.out.println("Ho caricato un giusto oggetto di input. "+SwiftParameterInput.tipoObjectInput.InsertObject);
    
    //creo questo oggetto di comodo
    InsertObject insertObject = new InsertObject();

    //eseguo il dowcasting
    insertObject = (InsertObject) swiftParameterInput.ogg; // <----######
    
    insertObject.setOperazione("update object metadata");
    
   String urlFinale=insertObject.getBase()+insertObject.getAccount()+"/"+insertObject.getContainer()+"/"+insertObject.getObject();
   
   //debug
   //System.out.println("url passato alla richiesta http: "+urlFinale); 
   
    InfoCreateObjectMetadataForMongoDb  risposta = httpPostObjectMetadata(urlFinale,insertObject.getTokenId(),insertObject.getX_Container_Meta(),insertObject.getName());
    
    
    if(risposta.getStatusCode().equals("201")||risposta.getStatusCode().equals("202")){
   
    //carico l'oggetto di risposta di alcune informazioni presenti in insertObject
      
    risposta.setAccount(insertObject.getAccount());
    risposta.setContainer(insertObject.getContainer());
    risposta.setObject(insertObject.getObject());
    risposta.setX_Container_Meta(insertObject.getX_Container_Meta());
    risposta.setName(insertObject.getName());
    risposta.setOperazione(insertObject.getOperazione());
   }
   
    
     //dichiaro questo oggetto padre di comodo 
   SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();
   
   //effettuo l'upcasting
   
   swiftParameterOutput = (SwiftParameterOutput) risposta;
   
 return swiftParameterOutput;  
   
    }//if
    
   else{
      
       System.out.println("Ho caricato un oggetto di input errato.");
       return null;
   } 
    
}//updateObjectMetadata


//###########################################################################

//#####################################################
//metodi astratti derivati dal costrutto implement
//#####################################################


    @Override
    public void setOwner(Agent owner) {
        this.owner = owner;
    }

    @Override
    public void init(Element params, Agent owner) throws CleverException {
        logger.debug("INIZIO init() di swift");
        
        logger.debug("Swift OK");
    //vuoto        
  
       
      
     //##################################    
     this.owner.setPluginState(true);
     logger.debug("FINE init() di swift");
     //##################################   
    }//init()
    
    
    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getDescription() {
        return("This plugin provides an integration with OpenStack Object Storage Swift ");
    }
    
    @Override
    public void shutdownPluginInstance(){
        

    }
    
    
    
    
    

}//Swift
