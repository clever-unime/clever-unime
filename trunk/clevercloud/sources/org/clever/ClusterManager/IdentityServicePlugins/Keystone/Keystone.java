/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.ClusterManager.IdentityServicePlugins.Keystone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.IdentityService.IdentityServicePlugin;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jdom.Element;
import org.json.JSONException;


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
public class Keystone implements IdentityServicePlugin {
    
    private String AUTH_URL ; //url completo
    
    private String ipAddress;
    private String portAddress;
    
    private String user;
    private String pass;
    private String tenant;
    
    
    
    
    private String ADMIN_TOKEN; // forse non è utilizzata
    
    //#################################################
     private Agent owner;
     Logger logger = Logger.getLogger("Keystone - Plugin");

    //#################################################



//########################
//  Costruttori
//######################## 
     
     
    public Keystone(String AUTH_URL) {
        this.AUTH_URL = AUTH_URL;
    }
     
 
     


/**
 * Costruttore di default.
 */
public Keystone() {
        this.AUTH_URL = "";
        this.ipAddress = "";
        this.portAddress = "";
        this.ADMIN_TOKEN = "";
        this.user="";
        this.pass="";
        this.tenant="";
    }
     


public void setAUTH_URL(String ipAddress, String portAddress) {
        this.AUTH_URL = "http://"+ipAddress+":"+portAddress;
    }


    //########################
    //Metodi setter e getter
    //######################## 
    
    
    public String getADMIN_TOKEN() {
        return ADMIN_TOKEN;
    }

    public void setADMIN_TOKEN(String ADMIN_TOKEN) {
        this.ADMIN_TOKEN = ADMIN_TOKEN;
    }

    public String getAUTH_URL() {
        return AUTH_URL;
    }

    public void setAUTH_URL(String AUTH_URL) {
        this.AUTH_URL = AUTH_URL;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPortAddress() {
        return portAddress;
    }

    public void setPortAddress(String portAddress) {
        this.portAddress = portAddress;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    
    
   
 //#################################################
 //   Metodi della classe
 //################################################# 
  

//############################################################################
    
/**
 * Method: POST
 * URI:	/v2.0/tokens
 * Description: Authenticates and generates a token.
 * @param tenantName
 * @param usename
 * @param password
 * @return 
 * @throws JSONException
 * @throws IOException 
 */
public Token authenticationUserPassTen(String tenantName, String usename, String password)
          throws JSONException, IOException{
      
        String json = createAuthenticationUserPassTenRequestJson(tenantName,usename,password);
        logger.debug("richiesta json: "+json);
        //######################
        String url_adduser="/v2.0/tokens";
        //###################### 
        String url=getAUTH_URL()+url_adduser;
        logger.debug("url: "+url);
        //debug
        //System.out.println(url);
        String outputRequest = httpPost(json,url);
        logger.debug("outputRequest: "+outputRequest);
        //debug
        System.out.println(outputRequest);
        String token_id=extractFieldToJsonNode("token","id",outputRequest);
        String token_issued_at=extractFieldToJsonNode("token","issued_at",outputRequest);
        String token_expires=extractFieldToJsonNode("token","expires",outputRequest);
        String publicUrlSwift =extractFieldToJsonNode("endpoints","publicURL",outputRequest);
        
        //################################################################
        //publicUrlSwift=publicUrlSwift.replace("controller", "10.0.2.15");
       //################################################################
        
       token_id = token_id.replace('"', ' ').trim();
       token_issued_at = token_issued_at.replace('"', ' ').trim(); 
       token_expires = token_expires.replace('"', ' ').trim(); 
       publicUrlSwift = publicUrlSwift.replace('"', ' ').trim();
       
       Token comodo = new Token( token_id,token_issued_at,token_expires,tenantName,publicUrlSwift);
       
       
       
return comodo;
     
        
}//AuthenticationUsenamePassword
  
/**
 * Questo metodo crea il template json per la richiesta di autenticazione 
 * di AuthenticationUsernamePassword().
 * @param tenantName
 * @param username
 * @param password
 * @return
 * @throws JSONException 
 */  
private String createAuthenticationUserPassTenRequestJson
        (String tenantName, String username, String password) throws JSONException{
   
      
 String json="{\"auth\":{\"tenantName\": \""+tenantName+"\", \"passwordCredentials\": {\"username\": \""+username+"\",\"password\": \""+password+"\"}}}";        
        
return  json;
      
  }//createAuthenticationUsenamePasswordRequestJson
  

/**
 * Il metodo esegue una connessione HTTP POST
 * @param json server a cui destinare la connessione
 * @param url
 * @return
 * @throws UnsupportedEncodingException
 * @throws IOException 
 */
private String httpPost(String json, String url) 
        throws UnsupportedEncodingException, IOException{
   
        //DefaultHttpClient httpclient = new DefaultHttpClient();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        
        HttpPost http = new HttpPost(url);
      
        String newAuth =json;
       
        //debug  
        //System.out.println("TAG passing your data: "+newAuth);
        
        StringEntity params =new StringEntity(newAuth);
             
        http.setHeader("Content-type", "application/json");
        http.setHeader("Accept", "application/json");//*
             
        http.setEntity((params));
       
       // HttpResponse response = httpclient.execute(http);
        CloseableHttpResponse response = httpclient.execute(http);
        
        //debug
        //System.out.println(response);
       
        System.out.println("Response Code : "+response.getStatusLine().getStatusCode());
 
	BufferedReader rd = new BufferedReader(
	        new InputStreamReader(response.getEntity().getContent()));
 
	StringBuffer result = new StringBuffer();
	String line = "";
	while ((line = rd.readLine()) != null) {
		result.append(line);
	}
  
 //debug       
 //System.out.println(result);
 
// httpclient.close();
 
 return result.toString();       
    
}//httpPost 


//############################################################################


/**
 * La guida ufficiale dice di fare così
 * 
 * Method: GET
 * URI: GET
 * Description: Validates a token and confirms that it belongs to a specified tenant.
 *		
 *MA non funziona. Di conseguenza lo faccio come segue.
 *  
 * Method: POST
 * URI:	/v2.0/tokens/
 * Description: Validates a token and confirms that it belongs to a specified tenant.
 * 
 * che equivale a:
 * 
 * curl -X POST -d '{ "auth":{ "token":{ "id":"id-no-admin" }, 
 *    "tenantName":"tenant-name" }}' -H "Content-Type:application/json" 
 *     -H "Accept: application/json" http://192.168.1.2:35357/v2.0/tokens 
 *
 * @param tokenId
 * @param tenantName
 * @return
 * @throws IOException 
 */
public String validateToken(String tokenId, String tenantName) throws IOException{
    
    String json_auth = createvalidateTokenRequestJson(tokenId,tenantName);
    //######################
    String url_service="/v2.0/tokens/";
    
//######################
    String url=getAUTH_URL()+url_service;
    //debug
    //System.out.println(url);
    String json = createHttpValidateTokenToKeystone(url,json_auth);
    return json;
    
}//validateToken

/**
 * crea il template json per la richiesta di validazione del token
 * @param non_admin_token
 * @param tenantName
 * @return 
 */
private String createvalidateTokenRequestJson(String non_admin_token, String tenantName){
    
 String json="{ \"auth\":{ \"token\":{ \"id\":\""+non_admin_token+"\" },\n" +
"\"tenantName\":\""+tenantName+"\" }}";   
 
 //System.out.println(json);
 
 return json;
}//createvalidateTokenRequestJson

/**
 * Normal response codes: 200, 203
 * Error response codes: identityFault (400, 500, …), badRequest (400), 
 *                       unauthorized (401), forbidden (403), 
 *                       badMethod (405), overLimit (413), 
 *                       serviceUnavailable (503), itemNotFound (404)
 * @param url
 * @return
 * @throws IOException 
 */
private String createHttpValidateTokenToKeystone(String url, String json) throws IOException{
    
       //DefaultHttpClient httpclient = new DefaultHttpClient();
        CloseableHttpClient httpclient = HttpClients.createDefault();  
    
       HttpPost http = new  HttpPost(url);
             
       String newToken =json;
       
       //debug  
       //System.out.println("TAG passing your data: "+newToken);
            

        StringEntity params =new StringEntity(newToken);
       
       
       
       http.setHeader("Content-type", "application/json");
       http.setHeader("Accept", "application/json");
     
       http.setEntity((params));
       
       //HttpResponse response = httpclient.execute(http);
       CloseableHttpResponse response = httpclient.execute(http);
       
       //debug
       //System.out.println(response);
       
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
 System.out.println(result);
        
 //httpclient.close();
 
return result.toString();   
   
}//createHttpvalidateTokenToKeystone


//#########################################################################

/**
     * Questo metodo estrae il campo field dal nodo jsonNode.
     * @param jsonNode
     * @param field
     * @param json
     * @return
     * @throws JSONException
     * @throws IOException 
     */
private String extractFieldToJsonNode(String jsonNode, String field, String json)throws JSONException, IOException{
      
     //jackson-all-1.7.7.jar
     ObjectMapper mapper = new ObjectMapper();
     
     JsonNode rootNode = mapper.readValue(json, JsonNode.class); 
     
     JsonNode element = rootNode.findParent(jsonNode);
     
     JsonNode id = element.findPath(field);
     
     //debug
     //System.out.println(id);
     
     return id.toString();
    }//extractFieldToJsonNode 

//#########################################################################

public Token getInfo4interactonSWIFT() throws JSONException,IOException {
        return this.authenticationUserPassTen("admin", "admin", "admin");
    }

//#########################################################################



//#####################################################
//metodi astratti derivati dal costrutto implement
//#####################################################


    @Override
    public void setOwner(Agent owner) {
        this.owner = owner;
    }

    @Override
    public void init(Element params, Agent owner) throws CleverException {
        //fondamentale
        this.setOwner(owner);

        logger.debug("INIZIO init() di Keystone");

        //ricavo queste variabili dal file configuration_identityService.xml 
        this.setIpAddress(params.getChildText("ipAddress"));
        this.setPortAddress(params.getChildText("port"));

        //setto la variabile AUTH_URL con l'indirizzo completo di Keystone
        this.setAUTH_URL(this.getIpAddress(), this.getPortAddress());
        logger.debug("\nAUTH_URL : " + this.getAUTH_URL());

        //acquisisco le credenziali dal file configuration_identityService.xml 
        //ed inizializzo le variabili locali
        this.setUser(params.getChildText("user"));
        this.setPass(params.getChildText("password"));
        this.setTenant(params.getChildText("tenant"));

        logger.debug("Ho acquisito le seguenti credenziali: \n");
        logger.debug("user:  " + this.getUser());
        logger.debug(" pass:  " + this.getPass());
        logger.debug(" tenant: " + this.getTenant());


        //istanzio un oggetto di tipo Token
        Token token = new Token();

        try {
            
            //effettuo un test sul servizio richiedendo un token

            token = this.authenticationUserPassTen(this.getUser(), getPass(), getTenant());
            
        } catch (JSONException ex) {
            logger.error("Errore in authenticationUserPassTen()" + ex);
        } catch (IOException ex) {
            logger.error("Errore in authenticationUserPassTen()" + ex);
        }

        if (token.getId() != "") {
            //se ho ricevuto un token
            this.owner.setPluginState(true);
            logger.debug("Keystone Service is OK !!!");
        } else {
            //se non ho ricevuto un token
            this.owner.setPluginState(false);
            logger.debug("Keystone Service is KO !!!");
        }

       

     //##################################    
     //this.owner.setPluginState(true);
     logger.debug("FINE init() di Keystone");
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

}//keystone
