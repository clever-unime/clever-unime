/**
 * The MIT License
 *
 * @author dott. Riccardo Di Pietro - 2014 MDSLab Messina dipcisco@hotmail.com
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.clever.Common.OpenAm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;

/**
 *
 * @author dott. Riccardo Di Pietro
 */
public class Openam {

    private String AUTH_URL; //http://openam.unime.it:8080/OpenAM-11.0.0
    private String openamHost; //openam.unime.it
    private String port; //8080
    private String deployUrl;//OpenAM-11.0.0
    private String service;

    private String username;
    private String password;

    private String tokenID;

    //#################################################
    Logger logger = Logger.getLogger("Openam - Plugin");
    //#################################################

    //lista a cui è applicata l'authorization
    ArrayList<String> cmdAutho = new ArrayList<String>();
    /**
     * *
     * La richiesta di default per il momento è sempre di tipo GET questa è
     * settata sull'interfaccia web.
     */
    private final String DEFAULT_VERB = "GET";

//########################
//  Costruttori
//######################## 
    /**
     * Constructor
     *
     * @param openamHost
     * @param port
     * @param deployUrl
     */
    public Openam(String openamHost, String port, String deployUrl) {
        this.openamHost = openamHost;
        this.port = port;
        this.deployUrl = deployUrl;
        this.service = "";

    }

    /**
     * Default Constructor
     */
    public Openam() {
        logger.debug("WEWWWWWWW");
        this.openamHost = "";
        this.port = "";
        this.deployUrl = "";
        this.service = "";

    }

    /*
     * set url of openam service
     */
    public void setAUTH_URL(String openamHost, String port, String deployUrl) {
        this.AUTH_URL = "http://" + openamHost + ":" + port + "/" + deployUrl;
    }

    //########################
    //Metodi setter e getter
    //######################## 
    public String getOpenamHost() {
        return openamHost;
    }

    public void setOpenamHost(String openamHost) {
        this.openamHost = openamHost;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDeployUrl() {
        return deployUrl;
    }

    public void setDeployUrl(String deployUrl) {
        this.deployUrl = deployUrl;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getAUTH_URL() {
        return AUTH_URL;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<String> getCmdAutho() {
        return cmdAutho;
    }

    public void setCmdAutho(ArrayList<String> cmdAutho) {
        this.cmdAutho = cmdAutho;
    }

    //#################################################
    //   Metodi della classe
    //################################################# 
    /**
     * The simplest user name/password authentication returns a tokenId that
     * applications can present as a cookie value for other operations that
     * require authentication.
     *
     * curl \ --request POST \ --header "X-OpenAM-Username: demo" \ --header
     * "X-OpenAM-Password: changeit" \ --header "Content-Type: application/json"
     * \ --data "{}" \ https://openam.example.com:8443/openam/json/authenticate
     *
     * { "tokenId": "AQIC5w...NTcy*", "successUrl": "/openam/console" }
     *
     *
     * @param username
     * @param password
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public httpResp simpleAuthentication(String username, String password) throws IOException, JSONException {

        httpResp risp = new httpResp();

        this.setService("json/authenticate");

        String url = "http://" + this.getOpenamHost() + ":" + this.getPort() + "/" + this.getDeployUrl() + "/" + this.getService();

        risp = httpPostAuthentication(url, username, password);

        if ("200".equals(risp.getHttpCode())) {

            risp.setTokenId(extractFieldToJsonNode("tokenId", "tokenId", risp.getJson()));

        }

        return risp;
    }

    //#################################################
    //   Metodi della classe
    //################################################# 
    /**
     * The simplest user name/password authentication returns a tokenId that
     * applications can present as a cookie value for other operations that
     * require authentication.
     *
     * curl \ --request POST \ --header "X-OpenAM-Username: demo" \ --header
     * "X-OpenAM-Password: changeit" \ --header "Content-Type: application/json"
     * \ --data "{}" \ https://openam.example.com:8443/openam/json/authenticate
     *
     * { "tokenId": "AQIC5w...NTcy*", "successUrl": "/openam/console" }
     *
     *
     * @param username
     * @param password
     * @return token if authentication has success or null if fails
     * @throws IOException
     * @throws JSONException
     */
    public String authenticate(String username, String password) throws IOException, JSONException {

        httpResp risp = new httpResp();

        this.setService("json/authenticate");

        String url = "http://" + this.getOpenamHost() + ":" + this.getPort() + "/" + this.getDeployUrl() + "/" + this.getService();

        risp = httpPostAuthentication(url, username, password);

        if ("200".equals(risp.getHttpCode())) {

            return extractFieldToJsonNode("tokenId", "tokenId", risp.getJson());

        }

        return null;
    }

    /**
     *
     * @param url
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    private httpResp httpPostAuthentication(String url, String username, String password) throws IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpPost http = new HttpPost(url);

        // String json = "";
        // String newAuth =json;
        //debug  
        //System.out.println("TAG passing your data: "+newAuth);
        //StringEntity params =new StringEntity(newAuth);
        http.setHeader("Content-Type", "application/json");
        http.setHeader("X-OpenAM-Username", username);
        http.setHeader("X-OpenAM-Password", password);

        //http.setEntity((params));
        CloseableHttpResponse response = httpclient.execute(http);

        //debug
        //System.out.println(response);
        logger.debug("POST \"Authentication\" Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        httpResp risp = new httpResp();
        risp.setHttpCode(Integer.toString(response.getStatusLine().getStatusCode()));
        risp.setJson(result.toString());

        httpclient.close();

        return risp;

    }//httppost

    /**
     * Questo metodo estrae il campo field dal nodo jsonNode.
     *
     * @param jsonNode
     * @param field
     * @param json
     * @return
     * @throws JSONException
     * @throws IOException
     */
    private String extractFieldToJsonNode(String jsonNode, String field, String json) throws JSONException, IOException {

        //jackson-all-1.7.7.jar
        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = mapper.readValue(json, JsonNode.class);

        JsonNode element = rootNode.findParent(jsonNode);

        JsonNode id = element.findPath(field);

        //debug
        //System.out.println(id);
        return id.toString().replace('"', ' ').trim();
    }//extractFieldToJsonNode 

//############################################################################
    /**
     * check over REST whether a token is valid.
     *
     * curl \ --request POST \ --header "Content-Type: application/json" \
     * http://openam.example.com:8080/openam/json/sessions/AQIC5...?_action=validate
     *
     * {"valid":true,"uid":"demo","realm":"/realm"}
     *
     * An invalid token returns only information about the validity.
     *
     * curl \ --request POST \ --header "Content-Type: application/json" \
     * http://openam.example.com:8080/openam/json/sessions/AQIC5...?_action=validate
     *
     * {"valid":false}
     *
     * @param tokenId
     * @return
     * @throws IOException
     */
    public httpResp tokenValidation(String tokenId) throws IOException {

        httpResp risp = new httpResp();

        this.setService("identity/isTokenValid");

        String url = "http://" + this.getOpenamHost() + ":" + this.getPort() + "/" + this.getDeployUrl() + "/" + this.getService();

        //debug
        //System.out.println(url);
        //System.out.println(tokenId);
        risp = httpPostValidationToken(url, tokenId);

        return risp;

    }

    private httpResp httpPostValidationToken(String url, String tokenId) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost http = new HttpPost(url);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("tokenid", tokenId));
        //params.add(new BasicNameValuePair("param-2", "Hello!"));
        http.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        // add request header
        //http.addHeader("Content-Type", "application/json");
        CloseableHttpResponse response = httpClient.execute(http);

        //debug
        //System.out.println(response);
        System.out.println("POST \"ValidationToken\" Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        //debug       
        //System.out.println(result);
        httpResp risp = new httpResp();
        risp.setHttpCode(Integer.toString(response.getStatusLine().getStatusCode()));
        risp.setJson(result.toString());

        String isTrue = result.toString().replace("boolean=", "").trim();
        //System.out.println("isTrue "+isTrue);
        if ("true".equals(isTrue)) {
            risp.setTokenValidity(true);
        } else {
            risp.setTokenValidity(false);
        }

        httpClient.close();

        return risp;

    }//httppost

//############################################################################
    /**
     * logout using the token to end the user session.
     *
     * curl "https://openam.example.com:8443/openam/identity/logout?
     * subjectid=tokenId"
     *
     * @param tokenId
     * @return
     * @throws IOException
     */
    public httpResp tokenLogout(String tokenId) throws IOException {

        httpResp risp = new httpResp();

        this.setService("identity/logout");

        String url = "http://" + this.getOpenamHost() + ":" + this.getPort() + "/" + this.getDeployUrl() + "/" + this.getService();
        //debug
        //System.out.println(url); 

        risp = httpPostTokenLogout(url, tokenId);

        return risp;

    }

    private httpResp httpPostTokenLogout(String url, String tokenId) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost http = new HttpPost(url);

        //  String newAuth ="subjectid="+data;
        //debug  
        //System.out.println("TAG passing your data: "+newAuth);
        //  StringEntity params =new StringEntity(newAuth);
        // http.setHeader("Content-Type", "application/json");
        //   http.setHeader("X-OpenAM-Username", username);
        //   http.setHeader("X-OpenAM-Password", password);
        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("subjectid", tokenId));
        //params.add(new BasicNameValuePair("param-2", "Hello!"));
        http.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //   http.setEntity((params));
        CloseableHttpResponse response = httpClient.execute(http);

        //debug
        //System.out.println(response);
        logger.debug("POST \"TokenLogout\" Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        //debug       
        //System.out.println(result);
        httpResp risp = new httpResp();
        risp.setHttpCode(Integer.toString(response.getStatusLine().getStatusCode()));
        risp.setJson(result.toString());

        httpClient.close();

        return risp;

    }//httppost

//############################################################################
    public httpResp simpleAuthorization(String uri, String verb) throws IOException {

        httpResp risp = new httpResp();
        
        this.setService("identity/authorize?uri=" + uri + "&action=" + verb + "&subjectid=" + this.getTokenID());

        String url = "http://" + this.getOpenamHost() + ":" + this.getPort() + "/" + this.getDeployUrl() + "/" + this.getService();

        //debug
        logger.debug("simpleAutorization URL: " + url);

        risp = httpGetAutho(url);

        return risp;
    }

    private httpResp httpGetAutho(String url) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);

        // add request header
//	request.addHeader("User-Agent", USER_AGENT);
        CloseableHttpResponse response = httpClient.execute(request);

        logger.debug("GET \"Autho\" Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        //debug       
        //System.out.println(result);
        httpResp risp = new httpResp();
        risp.setHttpCode(Integer.toString(response.getStatusLine().getStatusCode()));
        risp.setJson(result.toString());

        String isTrue = result.toString().replace("boolean=", "").trim();
        //System.out.println("isTrue "+isTrue);
        if ("true".equals(isTrue)) {
            risp.setUriAutho(true);
        } else {
            risp.setUriAutho(false);
        }

        httpClient.close();

        return risp;
    }

//############################################################################
    /**
     *
     * curl --request POST --header "iplanetDirectoryPro: tokenId" --header
     * "Content-Type: application/json" --data '{ "name": "bjensen",
     * "userpassword": "secret12", "mail": "bjensen@example.com" }'
     *
     * @param tokenId
     * @param json
     * @param nomeUser
     * @return
     * @throws IOException
     */
    public httpResp createIdentity(String tokenId, String json, String nomeUser) throws IOException {

        httpResp risp = new httpResp();
        this.setService("json/users/" + nomeUser);

        String url = "http://" + this.getOpenamHost() + ":" + this.getPort() + "/" + this.getDeployUrl() + "/" + this.getService();
        //debug
        System.out.println(url);

        risp = httpPutCreateIdentity(url, tokenId, json);

        return risp;
    }

    public String jsonIdentity(String username, String password, String email) {

        String json = "{\"username\": \"" + username + "\", \"userpassword\": \"" + password + "\", \"mail\": \"" + email + "\" }";
        return json;
    }

    private httpResp httpPutCreateIdentity(String url, String tokenId, String json) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPut http = new HttpPut(url);

        String newAuth = json;

        //debug  
        logger.debug("TAG passing your data: " + newAuth);

        StringEntity params = new StringEntity(newAuth);

        http.setHeader("Content-Type", "application/json");
        http.setHeader("iplanetDirectoryPro", tokenId);
        http.setHeader("If-None-Match", "*");

        http.setEntity((params));

        CloseableHttpResponse response = httpClient.execute(http);

        //debug
        //System.out.println(response);
        logger.debug("PUT \"CreateIdentity\" Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        //debug       
        //System.out.println(result);
        httpResp risp = new httpResp();
        risp.setHttpCode(Integer.toString(response.getStatusLine().getStatusCode()));
        risp.setJson(result.toString());

        httpClient.close();

        return risp;

    }//httppost

//#########################################################################
    public httpResp readIdentity(String tokenId, String nomeUser) throws IOException {

        httpResp risp = new httpResp();
        this.setService("json/users/" + nomeUser);

        String url = "http://" + this.getOpenamHost() + ":" + this.getPort() + "/" + this.getDeployUrl() + "/" + this.getService();

        //debug
        System.out.println(url);

        risp = httpGetReadIdentity(url, tokenId);

        return risp;

    }

    private httpResp httpGetReadIdentity(String url, String tokenId) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);

        // add request header
        request.addHeader("iplanetDirectoryPro", tokenId);

        CloseableHttpResponse response = httpClient.execute(request);

        logger.debug("GET \"readIdentity\" Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        httpResp risp = new httpResp();
        risp.setHttpCode(Integer.toString(response.getStatusLine().getStatusCode()));
        risp.setJson(result.toString());

        httpClient.close();

        return risp;
    }

//#############################################################################
    public httpResp updateIdentity(String tokenId, String nomeUser, String json) throws IOException {

        httpResp risp = new httpResp();
        this.setService("json/users/" + nomeUser);

        String url = "http://" + this.getOpenamHost() + ":" + this.getPort() + "/" + this.getDeployUrl() + "/" + this.getService();

        //debug
        System.out.println(url);

        risp = httpPutUpdateIdentity(url, tokenId, json);

        return risp;

    }

    private httpResp httpPutUpdateIdentity(String url, String tokenId, String json) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPut http = new HttpPut(url);

        String newAuth = json;

        //debug  
        logger.debug("TAG passing your data: " + newAuth);

        StringEntity params = new StringEntity(newAuth);

        http.setHeader("Content-Type", "application/json");
        // http.setHeader("Content-Type", "UTF-8");
        http.setHeader("iplanetDirectoryPro", tokenId);

        http.setEntity((params));

        CloseableHttpResponse response = httpClient.execute(http);

        //debug
        //System.out.println(response);
        logger.debug("PUT \"updateIdentity\" Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        //debug       
        //System.out.println(result);
        httpResp risp = new httpResp();
        risp.setHttpCode(Integer.toString(response.getStatusLine().getStatusCode()));
        risp.setJson(result.toString());

        httpClient.close();

        return risp;

    }

//#############################################################################
    public httpResp deleteIdentity(String tokenId, String nomeUser) throws IOException {

        httpResp risp = new httpResp();

        this.setService("json/users/" + nomeUser);

        String url = "http://" + this.getOpenamHost() + ":" + this.getPort() + "/" + this.getDeployUrl() + "/" + this.getService();

        //debug
        System.out.println(url);

        risp = httpDeleteIdentity(url, tokenId);

        return risp;

    }

    private httpResp httpDeleteIdentity(String url, String tokenId) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpDelete request = new HttpDelete(url);

        // add request header
        request.addHeader("iplanetDirectoryPro", tokenId);

        CloseableHttpResponse response = httpClient.execute(request);

        logger.debug("DELETE \"deleteIdentity\" Response Code : " + response.getStatusLine().getStatusCode());

        httpResp risp = new httpResp();
        risp.setHttpCode(Integer.toString(response.getStatusLine().getStatusCode()));

        httpClient.close();

        return risp;

    }

//#############################################################################
    public Boolean authorizeUser(String token, String moduleName, String commandName, List params) {

        if (token == null || moduleName == null || commandName == null) {
            logger.error("One or more parameters are null.\n"
                    + "token: " + token
                    + "\nmoduleName: " + moduleName
                    + "\ncommandName: " + commandName);
            return false;
        }

        logger.debug("Starting autorizeUser using.\n"
                + "token: " + token
                + "\nmoduleName: " + moduleName
                + "\ncommandName: " + commandName);

        Boolean risposta = true;

        //se il comando è all'interno della lista effettuo la richiesta di authorization
        for (int i = 0; i < cmdAutho.size(); i++) {

            String item = cmdAutho.get(i);

            if (item == null ? commandName == null : item.equals(commandName)) {
                logger.debug("Command needs to be authorized!" + commandName);
                //istanzio il client e gli setto il token
                Openam client = new Openam(getOpenamHost(), getPort(), getDeployUrl());
                
                client.setTokenID(token);

                try {
                    httpResp risp = client.simpleAuthorization(commandName, DEFAULT_VERB);
                    risposta = risp.getUriAutho();
                    logger.debug("authorizeUser Http code response: " + risp.getHttpCode());
                    logger.debug("authorizeUser authorization response: " + risp.getUriAutho());
                } catch (IOException ex) {
                    logger.error("Error in authorizeUser() " + ex);
                    risposta = false;
                }
                break;
            }
        }
        logger.debug("authorizeUser response: " + risposta);
        return risposta;
    }
}//class

