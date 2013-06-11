/*
 * The MIT License
 *
 * Copyright 2013 davide.
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
package org.clever.HostManager.HyperVisorPlugins.OCCI;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.VEInfo.NetworkSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.HostManager.HyperVisor.HyperVisorPlugin;
import org.jdom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Salvatore Monforte, Maurizio Paone, Davide Saitta
 */
public class HvOCCI implements HyperVisorPlugin {

    
  
    
  private Agent owner;
  private Logger logger;
  private URL occiURL = null;
  private OCCIAuth occiAuth = null;
  private String occiCompute = null; 
  
  
  //String localPath = "/clever-repo/" + "cirros-0.3.0-x86_64-uec.img";

  public HvOCCI() throws IOException, ParserConfigurationException, URISyntaxException, SAXException, HttpException {

    logger = Logger.getLogger(HvOCCI.class);
    //PropertyConfigurator.configure( "logger.properties" );
    logger.info("HvOcci plugin created: ");
  }

  //  * @param params : This object contains the node <pluginParams> of configuration_hypervisor.xml
  @Override
  public void init(Element params, Agent owner) throws CleverException {

    if (params != null) {
      Element occi = params.getChild("occi");
      try {
          
        String protocol = occi.getChildText("protocol");
        logger.info("Protocols for OCCI invocation: " + protocol);
        String host = occi.getChildText("host");
        logger.info("HOST for OCCI invocation: " + host);
        String port = occi.getChildText("port");
        logger.info("Port for OCCI invocation: " + port);
          
           logger.debug("occiURL instantiating ... ");
          
        this.occiURL = new URL(protocol,host,Integer.parseInt(port), "/"); //con il null o stringa vuota copme file NullPointerException
      } catch (MalformedURLException ex) {
        logger.error("Error in configuration parameters: " + ex.getMessage());
        throw new CleverException(ex);
      }
      
      logger.debug("URL: " + this.occiURL);
      
      this.occiCompute = this.occiURL.toString() + "/compute/";
      
      logger.debug("URL used for OCCI invocation: " + this.occiCompute);
      
      
      Element auth = params.getChild("auth");
      try {
        if (auth.getAttributeValue("type").equals("keystone")) {
          // URL occiURL, URL keystoneURL, UsernamePasswordCredentials credentials, String tenant
         /* occiAuth = new OCCIAuth(
                  new OCCIAuthKeystoneImpl(this.occiURL,
                                            new URL(auth.getChildText("protocol"), auth.getChildText("host"),
                                                    Integer.parseInt(auth.getChildText("port")), null),
                                            new UsernamePasswordCredentials(
                                                                            auth.getChildText("username"),
                                                                            auth.getChildText("password")), params.getChildText("tenant")
                                            )
                   
                  );*/
            occiAuth = new OCCIAuth( new OCCIAuthKeystoneImpl(this.occiURL, params.getChildText("tenant"), auth));
        }
        else //basic
        {
            occiAuth = new OCCIAuth( new OCCIAuthBasicImpl(auth));
        }
      } catch (MalformedURLException ex) {
        logger.error("Error in configuration parameters (authorization)" + ex.getMessage());
        throw new CleverException(ex);
      }
    }

    this.owner = owner;
  }

  
  
   /**
    * Metodo di comodo per effettuare un'invocazione senza argomenti
    * @param post
    * @return
    * @throws Exception 
    */
    private HttpResponse doOCCIInvocation(boolean post ) throws Exception {
        return this.doOCCIInvocation(post, null, null, null, null);
    }
  
     
    
   
    
    
  /**
   * Effettua un'invocazione OCCI
   * @param post Metodo HTTP: post o get
   * @param categories: Le Category da mettere nell'header della richiesta
   * @param occi_attributes: I X-OCCI-Attribute da mettere nell'header della richiesta
   * @param path La stringa relativa alla risorsa dopo /compute/
   * @param params I parametri da mettere direttamente nell'URL dopo ? nella forma "key=value"
   * @return 
   */
  private HttpResponse doOCCIInvocation(boolean post , String [] categories, String[] occi_attributes, String path, String[] inURLParams) throws Exception {
      DefaultHttpClient httpclient = new DefaultHttpClient();
      StringBuffer requestURL = new StringBuffer(this.occiCompute);
      if(path != null)
      {
          requestURL.append(path);
      }
      
      if (inURLParams != null)
      {
          requestURL.append("?");
          boolean separatore = false;
          for (String param : inURLParams)
          {
              if(separatore)
              {
                  requestURL.append("&");
              }
              else
                  separatore = true;
              requestURL.append(param);
          }
      }
      
      
      
      HttpUriRequest request;
      if(post)
      {
          request = new HttpPost(requestURL.toString());
      }
      else
      {
          request = new HttpGet(requestURL.toString());
      }
      request.addHeader("Content-Type", "text/occi");
      if(!this.occiAuth.doAuth(request)) //authentication
      {
          //non autenticato
          throw new Exception("Authentication Error");
      }
      
      if(categories != null)
      {
          
          for (String category : categories)
          {
              request.addHeader("Category", category);
              
          }
      }
      if(occi_attributes != null)
      {
          for (String attribute : occi_attributes)
          {
              request.addHeader("X-OCCI-Attribute", attribute);
          }
      }
      return httpclient.execute(request);
      /*HttpEntity entity = response.getEntity();
      return entity.getContent();
      */
      
      
      
  }
 
  /**
   * Metodo di comodo per effettuare chiamate su una VM
   * @param vmId L'id della VM (OCCI id)
   * @param action l'azione (per es. "start"
   * @return
   * @throws Exception 
   */
  
    private HttpResponse doOCCIInvocation(String vmId , String action) throws Exception 
     {
         String categories [] = {
                "start; scheme=\"http://schemas.ogf.org/occi/infrastructure/compute/action#\"; class=\"action\""
                 };    
          String inURLParams [] = {
                "action" + action
            };    

         return this.doOCCIInvocation(true, categories, null, vmId, inURLParams);
     }
    
  
  
  //TODO restituire VEState
  
  
  @Override
  public List listVms() throws Exception {

    InputStream instream = this.doOCCIInvocation(false).getEntity().getContent();
    ArrayList<String> lid = getArray(instream);
    ArrayList<String> lname = new ArrayList<String>();

    for (int i = 0; i < lid.size(); i++) {
      lname.add(getOcciNameFromID(lid.get(i)));
    }
    return lname;
  }

  @Override
  public List listRunningVms() throws Exception {

    InputStream instream = this.doOCCIInvocation(false).getEntity().getContent();

    ArrayList<String> lid = getArray(instream);
    ArrayList<String> lname = new ArrayList<String>();
    String vmrunning = null;

    for (int i = 0; i < lid.size(); i++) {
      vmrunning = getOcciNameFromID(lid.get(i));
      if (isRunning(vmrunning)) //era commentato da Davide l'ho decommentato
      {
        lname.add(vmrunning);
      }

    }
    return lname;

  }




  @Override
  public boolean startVm(String occiName) throws Exception {

    String occiID = getOcciIDfromName(occiName);

    
    
    return this.doOCCIInvocation(occiID, "start").getStatusLine().getStatusCode() == HttpStatus.SC_OK ;
   

    

  }

  @Override   //l'esclusivo viene ignorato - tutto il metodo e' da considerare e testare attentamente
  public boolean createVm(String veId, VEDescription parameters, Boolean notExclusive) throws Exception {

    //Qui dovrebbe creare il favor con le caratteristiche prese da VED e registrare l'immagine presa tramite imagemanager (o storage manager)
    String flavor=null, img=null;
    
    
    String categories [] = {
                "compute; scheme=\"http://schemas.ogf.org/occi/infrastructure#\"; class=\"kind\"",
                flavor + "; scheme=\"http://schemas.openstack.org/template/resource#\"; class=\"mixin\"",
                img + "; scheme=\"http://schemas.openstack.org/template/os#\"; class=\"mixin\""
    };    
    
    String occiattributes [] = {
                "occi.compute.hostname=\"" + veId + "\"" //controllare cosa vuol dire
    };
    
    
    
    
    
    
    
    return this.doOCCIInvocation(true, 
                                 categories,
                                 null,
                                 null,
                                 occiattributes).getStatusLine().getStatusCode() == HttpStatus.SC_CREATED;
   


  }

  @Override
  public boolean createAndStart(String veId, VEDescription parameters, Boolean notExclusive) throws Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean suspend(String occiName) throws Exception {


    String occiID = getOcciIDfromName(occiName);

    return this.doOCCIInvocation(occiID, "suspend").getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    

  }

  // fa lo stop violento era uguale alla shutDownVM
  @Override
  public boolean destroyVm(String occiName) throws Exception {

//        System.out.println("HvOCCI destroyVm occiName " + occiName);

    String occiID = getOcciIDfromName(occiName);

    return this.doOCCIInvocation(occiID, "stop").getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    


  }

  @Override
  public boolean shutDownVm(String occiName) throws Exception {

      String occiID = getOcciIDfromName(occiName);

      return this.doOCCIInvocation(occiID, "stop").getStatusLine().getStatusCode() == HttpStatus.SC_OK;


  }

  @Override
  public boolean isRunning(String occiName) throws Exception {

    String occiID = getOcciIDfromName(occiName);

 
    InputStream instream = this.doOCCIInvocation(false, null, null, occiID, null).getEntity().getContent();
    
    
    String res = read(instream);

    int inizio = res.lastIndexOf("occi.compute.state=\"") + 20;
    int fine = res.lastIndexOf("X-OCCI-Attribute") - 191;

    if (res.substring(inizio, fine).equals("active")) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean deleteSnapshot(String occiName, String nameS) throws Exception {
      
  //da controllare cosa vuol dire probabilmente meglio come destroyVM
      
      throw new UnsupportedOperationException("deleteSnapshot Not supported yet.");
/*
    String occiID = getOcciIDfromName(occiName);

    HttpClient httpClient = new DefaultHttpClient();
//        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
    HttpDelete httpDelete = new HttpDelete("http://" + server + ":" + portOCCI + "/compute/" + occiID);
    httpDelete.addHeader("Content-Type", "text/occi");
    httpDelete.addHeader("X-Auth-Token", token);

    HttpResponse response = httpClient.execute(httpDelete);
//        int statusCode = response.getStatusLine().getStatusCode();
//        return statusCode == 200 ? true : false;
    if (response.toString().contains("HTTP/1.1 200 OK")) {
      return true;
    } else {
      return false;
    }
*/
  }

  

  @Override
  public boolean resume(String id) throws Exception {
    throw new UnsupportedOperationException("resume Not supported yet.");
  }

  @Override
  public boolean saveState(String id, String path) throws Exception {
    throw new UnsupportedOperationException("saveState Not supported yet.");
  }

  @Override
  public boolean resumeState(String id, String path) throws Exception {
    throw new UnsupportedOperationException("resumeState Not supported yet.");
  }

  @Override
  public boolean addAdapter(String id, NetworkSettings settings) throws Exception {
    throw new UnsupportedOperationException("addAdapter Not supported yet.");
  }

  @Override
  public List getOSTypes() {
    throw new UnsupportedOperationException("getOSTypes Not supported yet.");
  }

  @Override
  public boolean cloneVM(String id, String clone, String description) throws Exception {
    throw new UnsupportedOperationException("cloneVM Not supported yet.");
  }

  @Override
  public boolean takeSnapshot(String id, String nameS, String description) throws Exception {
    throw new UnsupportedOperationException("takeSnapshot Not supported yet.");
  }

  @Override
  public boolean restoreSnapshot(String id, String nameS) throws Exception {
    throw new UnsupportedOperationException(" restoreSnapshot Not supported yet.");
  }

  @Override
  public String currentSnapshot(String id) throws Exception {
    throw new UnsupportedOperationException(" currentSnapshot Not supported yet.");
  }

  @Override
  public long snapshotCount(String id) throws Exception {
    throw new UnsupportedOperationException("snapshotCount Not supported yet.");
  }

  

  @Override
  public boolean deleteAllSnapshot(String id) throws Exception {
    throw new UnsupportedOperationException("deleteAllSnapshot Not supported yet.");
  }

  @Override
  public boolean renameVM(String id, String new_id) throws Exception {
    throw new UnsupportedOperationException("renameVM Not supported yet.");
  }

  @Override
  public boolean resetVM(String id) throws Exception {
    throw new UnsupportedOperationException("resetVM Not supported yet.");
  }

  @Override
  public List listSnapshot(String id) throws Exception {
    throw new UnsupportedOperationException("listSnapshot Not supported yet.");
  }

  @Override
  public boolean renameSnapshot(String id, String snapName, String newSnapName, String description) throws Exception {
    throw new UnsupportedOperationException("renameSnapshot Not supported yet.");
  }

  @Override
  public boolean attachPortRemoteAccessVm(String id) throws Exception {
    throw new UnsupportedOperationException("attachPortRemoteAccessVmNot supported yet.");
  }

  @Override
  public void releasePortRemoteAccessVm(String id) throws Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean ExportOvfToLocal(String id, String TargetPhysicalPath) throws Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean ImportLocalOvf(String id, String OVF_physicalPath) throws Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setOwner(Agent o) {
    this.owner = o;
  }

  @Override
  public boolean attackInterface(String id, String inf, String mac, String type) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getName() {

    return ("HvOcci");

  }

  @Override
  public String getVersion() {
    return "0.1-alfa";
  }

  @Override
  public String getDescription() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /*public String detailsVM(String occiName) throws URISyntaxException, HttpException, IOException, Exception {

    String occiID = getOcciIDfromName(occiName);

    HttpClient httpClient = new DefaultHttpClient();
    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000); //timeout connessione    
    HttpGet httpget = new computeURL   + occiID
    );
        httpget.addHeader("X-Auth-Token", token);
    httpget.addHeader("Content-Type", "text/occi");
    HttpResponse response = httpClient.execute(httpget);
    HttpEntity entity = response.getEntity();
    InputStream instream = entity.getContent();
    return read(instream);
  }
*/
  
  
  
  public String getOcciIDfromName(String occiName) throws Exception {

    InputStream instream = this.doOCCIInvocation(false).getEntity().getContent();
    ArrayList<String> lid = getArray(instream);
    String name = null;
    String idfound = null;

    for (int i = 0; i < lid.size(); i++) {
      idfound = lid.get(i);
      name = getOcciNameFromID(idfound);
      if (occiName.equals(name)) {
        return idfound;
      }
    }
//        return lname;
    throw new Exception("VM not found");

  }

  //    X-OCCI-Attribute: occi.occiURL.hostname="cvm1" 
// X-OCCI-Attribute: occi.core.id="a0f9783e-e81e-47ec-8889-c4a9a4ff4e9e" 
  public String getOcciNameFromID(String id) throws Exception {

 
    InputStream instream = this.doOCCIInvocation(false).getEntity().getContent();
    String res = read(instream);

    int inizio = res.lastIndexOf("occi.compute.hostname=\"") + 23;
    int fine = res.lastIndexOf("X-OCCI-Attribute") - 4;
    return res.substring(inizio, fine);

  }

  
  /*
   non usata
  public String listRAW() throws CleverException, IOException, URISyntaxException, HttpException {

    DefaultHttpClient httpclient = new DefaultHttpClient();
    HttpGet httget = new HttpGet(occiURL);
    httget.addHeader("Content-Type", "text/occi");
    httget.addHeader("X-Auth-Token", token);
    HttpResponse response = httpclient.execute(httget);
    HttpEntity entity = response.getEntity();
    InputStream instream = entity.getContent();

    String res = read(instream);
    return res;


  }
*/
  /* Non usata
  public String checkOCCI(String server, String portOCCI, String token)
      throws URISyntaxException, HttpException, IOException {

    HttpClient httpClient = new DefaultHttpClient();
    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
    HttpGet httpget = new HttpGet("http://" + server + ":" + portOCCI + "/-/");
    httpget.addHeader("X-Auth-Token", token);
    HttpResponse response = httpClient.execute(httpget);
    HttpEntity entity = response.getEntity();
    InputStream instream = entity.getContent();

    String res = read(instream);
    return res;

  }
*/
  

  private String read(InputStream in) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
    for (String line = r.readLine(); line != null; line = r.readLine()) {
      sb.append(line).append(" \n ");
    }
    in.close();
    return sb.toString();
  }

  private ArrayList<String> getArray(InputStream in) throws IOException {
    ArrayList<String> ia = new ArrayList<String>();
    BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
    for (String line = r.readLine(); line != null; line = r.readLine()) {
      if (line.length() > 2) {
        ia.add(line.substring(line.lastIndexOf("/") + 1));
      }
    }

//        ia.remove("c0b21668-8bfd-4e1e-9a13-267abfc51a25");        
//        ia.remove("92dd6c98-6326-4bf1-ac8e-802f9f227461");
// 
//        ia.remove("37314b35-b0a3-4543-b309-f5eab4d4a372");
////        ia.remove("8feee708-0d68-400b-8cd1-064c93ac8f79");
//        ia.remove("b4ca44e7-45a5-47de-ae9b-a131f300f669");
//        ia.remove("e5b74a60-77cc-46ac-84f1-b77f4731429e");
//        ia.remove("cf007497-faad-40af-892c-4c6384a54f01");
//        ia.remove("a43bcd9b-5f55-47ff-8f1d-be3910c4139d");
//        ia.remove("8944586c-b317-4949-b9ac-9ea080ee33e5");
//        ia.remove("c93e5915-2161-42a8-8319-0da31e223392");
//        ia.remove("de92bc81-3705-4f4d-9fc1-c62a5c46bf47");
//        ia.remove("a0c06f80-aa59-4abe-ad78-3943cb755719");



    in.close();
    return ia;
  }

    @Override
    public boolean destroyVm(String[] ids) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean shutDownVm(String[] ids) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean startVm(String[] ids) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getHYPVRName() {
        throw new UnsupportedOperationException("getHYPVRName Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
  
}
