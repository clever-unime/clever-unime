/*
 * The MIT License
 *
 * Copyright 2013 INFN CT.
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

import org.clever.HostManager.HyperVisorPlugins.OCCI.auth.OCCIAuthBasicImpl;
import org.clever.HostManager.HyperVisorPlugins.OCCI.auth.OCCINoAuth;
import org.clever.HostManager.HyperVisorPlugins.OCCI.auth.OCCIAuthKeystoneImpl;
import org.clever.HostManager.HyperVisorPlugins.OCCI.auth.OCCIAuth;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Exceptions.HyperVisorException;
import org.clever.Common.VEInfo.NetworkSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.VEInfo.VEState;
import org.clever.HostManager.HyperVisor.HyperVisorPlugin;
import org.clever.HostManager.HyperVisorPlugins.OCCI.HTTPUtils.HttpClientFactory;
import org.clever.HostManager.HyperVisorPlugins.OCCI.auth.OCCIAuthToken;
import org.clever.HostManager.HyperVisorPlugins.OCCI.auth.OCCIAuthX509Impl;

import org.jdom.Element;

enum HTTPMETHODS {

    GET,
    POST,
    PUT,
    DELETE,
    HEAD
}







/**
 *
 * @author Salvatore Monforte, Maurizio Paone, Davide Saitta
 */
public class HvOCCI implements HyperVisorPlugin {
    private boolean aac;

    
    class OCCIFeatures {

       

       
    
        
     class FeaturesCacheLoader extends CacheLoader<String,String>
     {
            private final String errorMessage;
            public FeaturesCacheLoader(String err)
            {
                errorMessage = err;
            }
            @Override
            public String load(String k) throws Exception {
                HvOCCI.this.retrieveServerFeatures(); //recupera le features dal server
                String result = actions.getIfPresent(k); //prende la feature cercata 
                if(result==null) //se null non presente neanche sul server
                    throw new HyperVisorException(new StringBuilder(errorMessage).append(' ').append(k).toString());
                return result; //ritorna la feature
            }
     }
        
        
        
    
    
     private LoadingCache<String, String> actions;
     private LoadingCache<String, String> templates;
     private LoadingCache<String, String> images;

     public OCCIFeatures()
     {
         actions = CacheBuilder.
                        newBuilder().
                        maximumSize(1000).
                        expireAfterAccess(30, TimeUnit.DAYS).
                        build(new FeaturesCacheLoader("OCCI action not found"));
         
          templates = CacheBuilder.
                        newBuilder().
                        maximumSize(1000).
                        expireAfterAccess(30, TimeUnit.DAYS).
                        build(new FeaturesCacheLoader("Template not found"));
          
          images = CacheBuilder.
                        newBuilder().
                        maximumSize(1000).
                        expireAfterAccess(30, TimeUnit.DAYS).
                        build(new FeaturesCacheLoader("Image not found"));
         
         
     }
     
     public void addAction(String name, String scheme)
     {
         actions.put(name, scheme);
     }
     
     
      public void addTemplate(String name, String schema) {
            logger.debug("Inserisco template: " + name +" "+schema);
            templates.put(name, schema);
      }

      public void addImage(String name, String schema) {
           images.put(name, schema);
      }
     
      
      
      private String getEntity(LoadingCache<String, String> entities, String name) throws Exception
      {
          try {
             return entities.get(name);
         } catch (ExecutionException ex) {
             if(ex.getCause()!=null)
                    throw new Exception(ex.getCause());
             throw new HyperVisorException("Unknown error");
         }
      }
    
     public String getAction(String name) throws Exception
     {
        return getEntity(actions,name);
     }
     
      private String getTemplate(String flavor) throws Exception {
            return getEntity(templates,flavor);
        }
      
       private String getImage(String image) throws Exception {
            return getEntity(images,image);
        }
    
}
    
    
    
    
    private Agent owner;
    private Logger logger;
    
    private HttpClientFactory httpClientFactory;
    
    
    
    private URL occiURL = null;
    private OCCIAuth occiAuth = null;
    private String occiCompute = null;
    
    private LoadingCache<String, String> fromNameToUUID;
    
    private OCCIFeatures features;
    
    
    
    Map<String, VEState.STATE> OCCI2CLEVERSTATES = ImmutableMap.of(
            "active", VEState.STATE.RUNNING,
            "inactive", VEState.STATE.STOPPED,
            "stopped", VEState.STATE.STOPPED);
    private String occiQuery;

    
    public HvOCCI() {

        logger = Logger.getLogger(HvOCCI.class);

       

       


       

        
        
         logger.info("HvOcci plugin created: ");
        // List<String> l = asList(new String("ddd"));

    }

    /**
     * ******************* Predicati ***********************
     */
    class IsOCCIPredicate implements Predicate<String> {

        private final String prefix;

        public IsOCCIPredicate(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean apply(String t) {
            return t.startsWith(prefix);
        }
    }

    class IsInVMState implements Predicate<VEState> {

        private final VEState.STATE state;

        public IsInVMState(VEState.STATE state) {
            this.state = state;
        }

        @Override
        public boolean apply(VEState t) {
            return t.getState() == state;
        }
    }
    private Predicate<String> isOCCIAttribute = new IsOCCIPredicate("X-OCCI-Attribute");
    private Predicate<String> isOCCICategory = new IsOCCIPredicate("Category");


    /**
     * *****************************************************
     */
    /**
     * Return the body of a HTTPENTITY as string. This method also consumes the
     * entity to release the connection (useful to pooling management)
     *
     * @param entity the HttpEntity
     * @return The HTTP body as String
     * @throws IOException
     */
    private String getBodyAsString(HttpEntity entity) throws IOException {
        InputStream in = entity.getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer, "UTF-8");
        EntityUtils.consume(entity);
        return writer.toString();

    }

    /**
     * Consumes the HTTP entity to release the connection.
     * Append to StringBuilder the error message
     *
     * @param sb A stringbuilder where the method append the HTTP status
     * @param response The HttpResponse that embed the Invocation error
     * @return A string with HTTP status appended to StringBuilder parameter
     */
    private String manageInvocationErrors(StringBuilder sb, HttpResponse response) {

        try {
            sb.
                    append(" HTTP status : ").
                    append(response.getStatusLine().getStatusCode()).
                    append(" message : ").
                    append(this.getBodyAsString(response.getEntity()));
                    

        } catch (IOException ex) {
            logger.error("Error consuming response entity");
        }
        return sb.toString();
    }

    
    
    /**
     * Effettua un'invocazione OCCI passando il path di base
     *
     * @param HTTPMETHODS Metodo HTTP
     * @param basePath Path base per l'azione 
     * @param categories: Le Category da mettere nell'header della richiesta
     * @param occi_attributes: I X-OCCI-Attribute da mettere nell'header della
     * richiesta
     * @param path La stringa relativa alla risorsa dopo /compute/
     * @param inURLParams I parametri da mettere direttamente nell'URL dopo ?
     * nella
     * @param consumeEntity if true the response.entity is consumed (for thread
     * issue) forma "key=value"
     * @return
     */
    private HttpResponse doOCCIInvocation(
            HTTPMETHODS method,
            String basePath,
            String[] categories,
            String[] occi_attributes,
            String path,
            String[] inURLParams,
            boolean paramsInBody,
            boolean consumeEntity,
            Integer success_status,
            StringBuilder message_error) throws HyperVisorException, IOException {
       
        StringBuilder requestURL = new StringBuilder(basePath);
        if (path != null) {
            requestURL.append(path);
        }

        if (inURLParams != null && inURLParams.length != 0) {
            requestURL.append("?").append(Joiner.on('&').join(inURLParams));

        }



        HttpUriRequest request;


        switch (method) {
            case GET:
                request = new HttpGet(requestURL.toString());
                break;
            case POST:
                request = new HttpPost(requestURL.toString());
                break;
            case PUT:
                request = new HttpPut(requestURL.toString());
                break;
            case DELETE:
                request = new HttpDelete(requestURL.toString());
                break;
            default:
                request = new HttpGet(requestURL.toString());


        }


        

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        
        if (categories != null) {

            for (String category : categories) {
                //request.addHeader("Category", category);
                params.add(new BasicNameValuePair("Category", category));
            }
        }

        if (occi_attributes != null) {
            
            
            for (String attribute : occi_attributes) {
                //request.addHeader("X-OCCI-Attribute", attribute);
                params.add(new BasicNameValuePair("X-OCCI-Attribute", attribute));
                
            }
           
        }
        
        
        
        //paramsInBody=true;
        if(method == HTTPMETHODS.POST && paramsInBody)
            {
                request.addHeader("Content-Type", "text/plain, text/occi");
                request.addHeader("Accept", "text/plain, text/occi");

                StringBuilder body = new StringBuilder();
                //provare openstack
                for(NameValuePair p : params)
                {
                    body.append(p.getName()).append(": ").append(p.getValue()).append("\n");
                }
                ((HttpPost)request).setEntity(new StringEntity(body.toString()));
            }
        else
        {
            //request.addHeader("Content-Type", "text/plain, text/occi");
            request.addHeader("Content-Type", "text/occi");

            for(NameValuePair p : params)
                {
                    request.addHeader(p.getName(), p.getValue());
                }
            
        }

        if (!this.occiAuth.doAuth(request)) //authentication
        {

            throw new HyperVisorException("Authentication Error");
        }
        HttpResponse response = null;
        int tries = 0;
        IOException e = null;
        while(response == null && tries++ < 4) //TODO: retrieve number of HTTP request retries by plugin params
        {    try {
                response = httpClientFactory.getThreadSafeClient().execute(request);
            } catch (IOException ex) {
                logger.error("HTTP Timeout on read or socket operation" + ex.getMessage());
                e = ex;
            }
        }
        if(response==null)
        {
            logger.error("Fatal HTTP Timeout on read or socket operation" );
            throw new HyperVisorException("Fatal timeout on HTTP request " + (e!=null? e.getMessage() : "null exception")); //TODO:embed exception in CleverException
        }

        if (success_status != null) {
            StringBuilder error = message_error;
            if (error == null) {
                error = new StringBuilder("Error :");
            }
            if (response.getStatusLine().getStatusCode() != success_status) {
                
                throw new HyperVisorException(this.manageInvocationErrors(error, response));
                //EntityUtils.consume(response.getEntity());
            }



        }

        if (consumeEntity) {
            EntityUtils.consume(response.getEntity());
        }
        return response;




    }
    
    
    
    
    /**
     * Metodo di comodo per effettuare un'invocazione senza argomenti
     *
     * @param post
     * @return
     * @throws Exception
     */
    private HttpResponse doOCCIComputeInvocation(
            boolean consumeEntity,
            Integer success_status,
            StringBuilder error_message) throws Exception {
        return this.doOCCIComputeInvocation(HTTPMETHODS.GET,
                                            null,
                                            null,
                                            null,
                                            null,
                                            false,
                                            consumeEntity,
                                            success_status,
                                            error_message);
    }

    /**
     * Effettua un'invocazione OCCI al servizio di compute
     *
     * @param HTTPMETHODS Metodo HTTP
     * @param categories: Le Category da mettere nell'header della richiesta
     * @param occi_attributes: I X-OCCI-Attribute da mettere nell'header della
     * richiesta
     * @param path La stringa relativa alla risorsa dopo /compute/
     * @param inURLParams I parametri da mettere direttamente nell'URL dopo ?
     * nella
     * @param consumeEntity if true the response.entity is consumed (for thread
     * issue) forma "key=value"
     * @return
     */
    private HttpResponse doOCCIComputeInvocation(
            HTTPMETHODS method,
            String[] categories,
            String[] occi_attributes,
            String path,
            String[] inURLParams,
            boolean paramsInBody,
            boolean consumeEntity,
            Integer success_status,
            StringBuilder message_error) throws HyperVisorException, IOException {
       
       return this.doOCCIInvocation(method,
                                    this.occiCompute, 
                                    categories,
                                    occi_attributes,
                                    path,
                                    inURLParams,
                                    paramsInBody,
                                    consumeEntity,
                                    success_status,
                                    message_error);


    }
   
    
    
    
    /**
     * Metodo di comodo per effettuare chiamate su una VM
     *
     * @param vmId L'id della VM (OCCI id)
     * @param action l'azione (per es. "start"
     * @return
     * @throws Exception
     */
    private HttpResponse doOCCIComputeInvocation(
            HTTPMETHODS method,
            String vmId,
            String action,
            boolean paramsInBody,
            boolean consumeEntity,
            Integer success_status,
            StringBuilder error_message) throws Exception {
        
        
        
        String schema = features.getAction(action);
        
       
        
        /*
        String categories[] = {
            action + "; scheme=\"http://schemas.ogf.org/occi/infrastructure/compute/action#\"; class=\"action\""
        };*/
        
        String categories[] = {
            new StringBuilder(action).append("; scheme=\"").append(schema).append("\"; class=\"action\"").toString() 
         };
        
        String inURLParams[] = {
            "action=" + action
        };

        return this.doOCCIComputeInvocation(method,
                                            categories,
                                            null,
                                            vmId,
                                            inURLParams,
                                            paramsInBody,
                                            consumeEntity,
                                            success_status,
                                            error_message);
    }

    /**
     * Metodo di comodo per effettuare chiamate su una VM
     *
     * @param vmId L'id della VM (OCCI id)
     * 
     * @return
     * @throws Exception
     */
    private HttpResponse doOCCIComputeInvocation(HTTPMETHODS method,
            String vmId,
            boolean paramsInBody,
            boolean consumeEntity,
            Integer success_status,
            StringBuilder error_message) throws Exception {


        return this.doOCCIComputeInvocation(method,
                                            null,
                                            null,
                                            vmId,
                                            null,
                                            paramsInBody,
                                            consumeEntity,
                                            success_status,
                                            error_message);
    }

    private OCCIResponse _getVMDetails(String id) throws Exception {
        HttpResponse response = this.doOCCIComputeInvocation(
                HTTPMETHODS.GET,
                id,
                false,
                false,
                HttpStatus.SC_OK,
                new StringBuilder("Error retrieving OCCI attributes for VM:").append(id));

        HttpEntity entity = response.getEntity();
        Iterable<String> responses = splitResponse(entity);
        OCCIResponse r = new OCCIResponse(responses);
        logger.debug(r.toString());
        return r;
    }

    /**
     * Recover OCCI Attributes (occi.(core|compute).* ... ) from OCCI server
     *
     * @param vmId L'id della VM (OCCI id)
     *
     * @return Map with OCCI attribute name (e.g. hostname) as key and OCCI
     * value as MAP value
     * @throws Exception
     */
    private Map<String, String> getOcciAttributes(String id) throws Exception {

        HttpResponse response = this.doOCCIComputeInvocation(
                HTTPMETHODS.GET,
                id,
                false,
                false,
                HttpStatus.SC_OK,
                new StringBuilder("Error retrieving OCCI attributes for VM:").append(id));

        HttpEntity entity = response.getEntity();

        Iterable<String> responses = splitResponse(entity);
        OCCIResponse r = new OCCIResponse(responses);
        logger.debug("Response: " + r);
        return r.getAttributes();


    }
    
    
    private void retrieveServerFeatures() throws HyperVisorException
    {
        try {
            logger.debug("Retrieving features from server");
        HttpResponse response = this.doOCCIInvocation(
                                HTTPMETHODS.GET,
                                this.occiQuery,
                                null,
                                null,
                                null,
                                null,
                                false,
                                false,
                                HttpStatus.SC_OK,
                                new StringBuilder("Error on server features retrieving "));
            OCCIResponse f = new OCCIResponse(splitResponse(response.getEntity()));
            
            logger.debug("Features: " + f.toString());
            
            for (Entry<String, OCCIStructure> a : f.getCategories().entries())
            {
                final String classe = a.getValue().get("class");
                final String name = a.getKey();
                final String schema = a.getValue().get("scheme");
                final String rel = a.getValue().get("rel");
                if(classe.equals("action"))
                {
                    logger.debug("Action found : " + name);
                    features.addAction(name, schema);
                }
                else if(rel != null && classe.equals("mixin"))
                {
                    if( rel.equals("http://schemas.ogf.org/occi/infrastructure#resource_tpl"))
                    {
                        logger.debug("Template found : " + name);
                        features.addTemplate(name, schema);
                    }
                    else if(rel.equals("http://schemas.ogf.org/occi/infrastructure#os_tpl"))
                    {
                        logger.debug("Image template found : " + name);
                        features.addImage(name, schema);
                    }                    
                    
                }
            }
            
            
        } catch (IOException ex) {
            throw new HyperVisorException("Error parsing the server response: " + ex.getMessage());
        }
    }
    
    

    /**
     * Recover VEState of a particular VM from OCCI server
     *
     * @param vmId L'id della VM (OCCI id)
     *
     * @return Map with OCCI attribute name (e.g. hostname) as key and OCCI
     * value as MAP value
     * @throws Exception
     */
    private VEState getVMState(String id) throws Exception {
        Map<String, String> attributes = getOcciAttributes(id); //retrieve occi attributes from OCCI server
        String name = attributes.get("occi.compute.hostname"); //openstack uses hostname as name 

        return new VEState(
                OCCI2CLEVERSTATES.get(attributes.get("occi.compute.state")),
                attributes.get("occi.core.id"),
                (name == null ? attributes.get("occi.core.title") : name)); //opennebula uses title as name ,instead openstack uses hostname (standard ? )
    }

    /**
     * Recover OCCI uuid from clever name , invoking the OCCI server
     *
     * @param vmId name of VM (clever name)
     *
     * @return OCCI UUID
     * @throws Exception
     */
    private String _getOcciIDfromName(final String name) throws Exception {
        int retries = 10; //TODO: retrieve from params
        Predicate <VEState> pr = new Predicate<VEState>() {
                @Override
                public boolean apply(VEState t) {
                    logger.debug("esamino: " + t.getName() + " confrontata con: "+ name);
                    return ((t == null || t.getName() == null) ? false : t.getName().equalsIgnoreCase(name));
                }
            };
        VEState vm = null;
        while( retries-- > 0 )
        {
            List<VEState> vms = this.recoverVMsFromOCCIServer();
            
            try {
                vm = Iterables.find(vms, pr);
            } catch (java.util.NoSuchElementException ex) {
                if(retries > 0)
                {
                    logger.debug("tentativo: " + retries);
                    continue;
                }
                throw new Exception("VM not found");
            }

            if (vm == null) {
                if(retries > 0)
                {
                    logger.debug("tentativo: " + retries);
                    continue;
                }
                throw new Exception("VM not found");
            }
        }
        
        if (vm == null) {
                throw new Exception("VM not found");
            }
        
        
        logger.debug("From name " + name + " to id: " + vm.getId());
        return vm.getId();

    }

    /**
     * Recover OCCI uuid from clever name , invoking the cache
     *
     * @param vmId name of VM (clever name)
     *
     * @return OCCI UUID
     * @throws Exception
     */
    private String getOcciIDfromName(final String name) throws Exception {
        return this.fromNameToUUID.get(name);
        //return this._getOcciIDfromName(name);
    }
    
    
    private Iterable<String> splitResponse(HttpEntity entity) throws IOException
    {
        return Splitter.on("\n").omitEmptyStrings().trimResults().split(this.getBodyAsString(entity));
    }
    

    /**
     * Recover state of VM from server
     *
     *
     * @return: List of VeState : the list is created by guava and is not
     * suitable for serialization (copy it in another list)
     */
    private List<VEState> recoverVMsFromOCCIServer() throws Exception {
        HttpResponse response = doOCCIComputeInvocation(
                false,
                HttpStatus.SC_OK,
                new StringBuilder("Error listing VMS; "));

        //Iterable<String> responses = Splitter.on("\n").omitEmptyStrings().trimResults().split(this.getBodyAsString(response.getEntity()));
        Iterable<String> responses = splitResponse(response.getEntity());
        Collection<String> vms = new OCCIResponse(responses).getLocations().keySet();


        Iterable<VEState> result = Iterables.transform(vms, new Function<String, VEState>() {
            @Override
            public VEState apply(String string) {
                String uuid = string.replaceAll(".*/", "");
                try {
                    VEState vm = getVMState(uuid);
                    HvOCCI.this.fromNameToUUID.put(vm.getName(), vm.getId());
                    logger.debug("VM found: " + vm);
                    return vm;
                } catch (Exception ex) {
                    logger.error("error on conversion from OCCI to clever vmstate : " + ex);
                    return new VEState(null, null, null);
                }
            }
        });
        return Lists.newArrayList(result);
    }

    /* ********* Methods to be invoked by external ( methods Implementation from RunnerPlugin)**********
     */
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
                
                aac = (occi.getChildText("acceptAllCertificates") !=null && occi.getChildText("acceptAllCertificates").equalsIgnoreCase("true") ? true : false);
                aac &= protocol.equalsIgnoreCase("https"); // non ha senso accettare tutti i certificati se non https
                logger.info("Accept all certificate: " + aac);
                
                
                String computePath = occi.getChildText("compute_path");


                this.occiURL = new URL(protocol, host, Integer.parseInt(port), "/"); //con il null o stringa vuota come file NullPointerException

                this.occiCompute = this.occiURL.toString() + (computePath == null ? "/compute/" : computePath);
                
                this.occiQuery = this.occiURL.toString() + "/-/";
                


            } catch (MalformedURLException ex) {
                logger.error("Error in configuration parameters: " + ex.getMessage());
                throw new CleverException(ex);
            }



            logger.debug("URL used for OCCI invocation: " + this.occiCompute);

            
            
            features = new OCCIFeatures();
            logger.debug("HttpClientFactory creating ...");
            try {
                httpClientFactory = new HttpClientFactory(occiURL.getProtocol(),aac, new Integer[]{occiURL.getPort()}); //per ora metto solo la porta di occi
            } catch (NoSuchAlgorithmException ex) {
                 logger.error("Error in configuration parameters (authorization)" + ex.getMessage());
                throw new CleverException(ex);
            } catch (KeyManagementException ex) {
                 logger.error("Error in configuration parameters (authorization)" + ex.getMessage());
                throw new CleverException(ex);
            } catch (KeyStoreException ex) {
                logger.error("Error in configuration parameters (authorization)" + ex.getMessage());
                throw new CleverException(ex);
            } catch (UnrecoverableKeyException ex) {
                logger.error("Error in configuration parameters (authorization)" + ex.getMessage());
                throw new CleverException(ex);
            }
            

            Element auth = params.getChild("auth");
            try {
                if (auth.getAttributeValue("type").equals("keystone")) {
                    occiAuth = new OCCIAuth(new OCCIAuthKeystoneImpl(this.occiURL, params.getChildText("tenant"), auth));
                } else if (auth.getAttributeValue("type").equals("basic"))//basic
                {
                    occiAuth = new OCCIAuth(new OCCIAuthBasicImpl(auth));
                }  else if (auth.getAttributeValue("type").equals("x509"))
                {
                    logger.debug("auth x509");
                    occiAuth = new OCCIAuth(new OCCIAuthX509Impl("cert.pem"));
                } 
                else if (auth.getAttributeValue("type").equals("token"))
                {
                    logger.debug("auth token (for okeanos)");
                    occiAuth = new OCCIAuth(new OCCIAuthToken(auth));
                }
                
                else {
                    occiAuth = new OCCIAuth(new OCCINoAuth(this.occiURL));
                }
                occiAuth.initClient(httpClientFactory); 
            } catch (MalformedURLException ex) {
                logger.error("Error in configuration parameters (authorization)" + ex.getMessage());
                throw new CleverException(ex);
            } catch (Exception ex) {
                 logger.error("Error in configuration parameters (authorization)" + ex.getMessage());
                throw new CleverException(ex);
            }
        }

        this.owner = owner;
        
        
        
         this.fromNameToUUID = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30, TimeUnit.DAYS).build(new CacheLoader<String, String>() {
            @Override
            public String load(String k) throws Exception {
                logger.debug("Cache fail: " + k);
                return _getOcciIDfromName(k);
            }
        });

        
        
        
        
         this.retrieveServerFeatures();
    }

    @Override
    public List<VEState> listVms() throws Exception {
        return Lists.newArrayList((this.recoverVMsFromOCCIServer())); //Copy for serialization
    }

    //TODO: test if Copy result list for serialization issues (guava)
    @Override
    public List<VEState> listRunningVms() throws Exception {

        List<VEState> vms = this.recoverVMsFromOCCIServer();


        List<VEState> runningVms = Lists.newArrayList(Iterables.filter(vms, new IsInVMState(VEState.STATE.RUNNING)));

        return runningVms;


    }

    @Override
    public boolean startVm(String name) throws Exception {

        String occiID = getOcciIDfromName(name);
        int ris;
        HttpResponse response = this.doOCCIComputeInvocation(
                HTTPMETHODS.POST,
                occiID,
                "start", //start dovrebbe essere da un suspend e restart dal poweroff: differenze tra openstack e opennebula
                false,
                true,
                HttpStatus.SC_OK,
                new StringBuilder("Error starting VM : ").append(name));

        return true;


    }

    @Override   //l'esclusivo viene ignorato - tutto il metodo e' da considerare e testare attentamente
    public boolean createVm(String veId, VEDescription ved, Boolean notExclusive) throws Exception {

        //Qui dovrebbe creare il favor con le caratteristiche prese da VED e registrare l'immagine presa tramite imagemanager (o storage manager)
        String flavor = ved.getName(), img = ved.getStorage().get(0).getDiskPath(); //per ora nello scenario della chiamata diretta all HM il diskpath e' il nome dell'immagine del repo OCCI

        
        
        String templateSchema = features.getTemplate(flavor);
        String imageSchema = features.getImage(img);
        String categories[] = {
            "compute; scheme=\"http://schemas.ogf.org/occi/infrastructure#\"; class=\"kind\"", //forse si dovrebbero aggiungere anche i kind
            new StringBuilder(flavor).append("; scheme=\"").append(templateSchema).append("\"; class=\"mixin\"").toString(),
            new StringBuilder(img).append("; scheme=\"").append(imageSchema).append("\"; class=\"mixin\"").toString(),
            
                                        
                
        };

        String occiattributes[] = {
            "occi.compute.hostname=\"" + veId + "\"",
            "occi.core.title=\"" + veId + "\"" //vediamo se funziona per entrambi
        };



        HttpResponse response = this.doOCCIComputeInvocation(HTTPMETHODS.POST,
                categories,
                occiattributes,
                null,
                null,
                true,
                true,
                HttpStatus.SC_CREATED,
                new StringBuilder("Error in VM creation : ").append(veId));


        return true;



    }

    @Override
    public boolean createAndStart(String veId, VEDescription parameters, Boolean notExclusive) throws Exception {
        throw new UnsupportedOperationException("Not supported yet. 1");
    }

    @Override
    public boolean suspend(String name) throws Exception {


        String occiID = getOcciIDfromName(name);

        HttpResponse response = this.doOCCIComputeInvocation(
                HTTPMETHODS.POST,
                occiID,
                "suspend",
                false,
                true,
                HttpStatus.SC_OK,
                new StringBuilder("Error in VM suspend : ").append(name));

        return true;


    }

    // cancella vm
    @Override
    public boolean destroyVm(String name) throws Exception {

        //        System.out.println("HvOCCI destroyVm occiName " + occiName);

        String occiID = getOcciIDfromName(name);



        HttpResponse response = this.doOCCIComputeInvocation(
                HTTPMETHODS.DELETE,
                occiID,
                false,
                true,
                HttpStatus.SC_OK,
                new StringBuilder("Error in VM destroy : ").append(name));

        return true;




    }

    @Override
    public boolean shutDownVm(String name) throws Exception {

        String occiID = getOcciIDfromName(name);


        HttpResponse response = this.doOCCIComputeInvocation(
                HTTPMETHODS.POST,
                occiID,
                "stop",
                false,
                true,
                HttpStatus.SC_OK,
                new StringBuilder("Error in VM shutdown : ").append(name));
        return true;


    }

    @Override
    public boolean shutDownVm(String id, Boolean poweroff) throws Exception {
        //poweroff parameter ignored
        return this.shutDownVm(id);
    }

    @Override
    public boolean shutDownVm(String[] ids, Boolean poweroff) throws Exception {
        //poweroff parameter ignored
        return this.shutDownVm(ids);
    }

    @Override
    public boolean isRunning(String name) throws Exception {

        String occiID = getOcciIDfromName(name);

        return this.getVMState(occiID).getState() == VEState.STATE.RUNNING;

    }

    
    
    
    

    @Override
    public boolean deleteSnapshot(String name, String nameS) throws Exception {

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
        throw new UnsupportedOperationException("Not supported yet. 2");
    }

    @Override
    public boolean ExportOvfToLocal(String id, String TargetPhysicalPath) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.3");
    }

    @Override
    public boolean ImportLocalOvf(String id, String OVF_physicalPath) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.4");
    }

    @Override
    public void setOwner(Agent o) {
        this.owner = o;
    }

    @Override
    public boolean attackInterface(String id, String inf, String mac, String type) {
        throw new UnsupportedOperationException("Not supported yet.5");
    }

    

    @Override
    public boolean destroyVm(String[] ids) throws Exception {
        for (String id : ids) {
            if (!this.destroyVm(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shutDownVm(String[] ids) throws Exception {
        for (String id : ids) {
            if (!this.shutDownVm(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean startVm(String[] ids) throws Exception {
        for (String id : ids) {
            if (!this.startVm(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean createVm(Map<String, VEDescription> ves) throws Exception {
        for (Map.Entry<String, VEDescription> entry : ves.entrySet()) {
            if (!this.createVm(entry.getKey(), entry.getValue(), false)) {
                return false;
            }
        }
        return true;
    }
    
    
    
    //PER OCCI
    
    public Map<String, Object> getVMDetails(String name) throws Exception {
        String occiID = getOcciIDfromName(name);
        OCCIResponse res = this._getVMDetails(occiID);
        Map<String, Object> result = Maps.newHashMap();


        try {
            List<Map<String,String>> reti = Lists.newArrayList();
            for (OCCIStructure n : res.getLinks().get("network")) {
                Map<String, String> detail = Maps.newHashMap();
                detail.put("ip", n.get("occi.networkinterface.address"));
                detail.put("mac", n.get("occi.networkinterface.mac"));
                detail.put("state", n.get("occi.networkinterface.state"));

                reti.add(detail);
            }
            result.put("network", reti);

            result.put("display", res.getAttributes().get("org.openstack.compute.console.vnc"));
            
            
            result.put("memory", res.getAttributes().get("occi.compute.memory"));
            
            result.put("cores", res.getAttributes().get("occi.compute.cores"));
            
            result.put("architecture", res.getAttributes().get("occi.compute.architecture"));
            
          

        } catch (NullPointerException e) {
            //display not found
        }


        return result;
    }
    
    
    
    public List<String> listTemplates() throws Exception {
        return Lists.newArrayList(this.features.templates.asMap().keySet());
    }
    
    public List<String> listImageTemplates() throws Exception {
        return Lists.newArrayList(this.features.images.asMap().keySet());
    }
    
    public List<String> listActions() throws Exception {
        
        List<String> actions = new ArrayList();
        for (Entry<String,String> action : features.actions.asMap().entrySet())
        {
            actions.add(action.getKey() + " - " + action.getValue());
        }
        
        
        
        return  actions;
    }
    
    
    
    
    ////////////////////////7

    @Override
    public String getHYPVRName() {
        return "OCCI/1.1";
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
        return "HM OCCI Plugin ";
    }
    
}
