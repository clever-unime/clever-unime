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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.VEInfo.NetworkSettings;
import org.clever.Common.VEInfo.VEDescription;
import org.clever.Common.VEInfo.VEState;
import org.clever.HostManager.HyperVisor.HyperVisorPlugin;

import org.jdom.Element;

enum HTTPMETHODS {

    GET,
    POST,
    PUT,
    DELETE,
    HEAD
}

class HttpClientFactory {

    private static DefaultHttpClient client;

    public synchronized static DefaultHttpClient getThreadSafeClient() {

        if (client != null) {
            return client;
        }

        client = new DefaultHttpClient();

        ClientConnectionManager mgr = client.getConnectionManager();

        HttpParams params = client.getParams();
        //qui vanno messi i parametri come autenticazione x509
        ThreadSafeClientConnManager tm = new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry());
        tm.setDefaultMaxPerRoute(20);

        tm.setMaxTotal(200);

        client = new DefaultHttpClient(tm, params);

        return client;
    }
}

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
    
    private DefaultHttpClient client = null;
    
    
    
    
    
    
    
    //Used to convert OCCI state codes to VEState.STATE (Clever class)
    Map<String, VEState.STATE> OCCI2CLEVERSTATES = new HashMap<String, VEState.STATE>() {
        {
            put("active", VEState.STATE.RUNNING);
            put("inactive", VEState.STATE.STOPPED);
            put("stopped", VEState.STATE.STOPPED);
        }
    };

    //String localPath = "/clever-repo/" + "cirros-0.3.0-x86_64-uec.img";
    public HvOCCI() {

        logger = Logger.getLogger(HvOCCI.class);

        logger.info("HvOcci plugin created: ");

        ThreadSafeClientConnManager tsccm = new ThreadSafeClientConnManager();

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

//     private Predicate<String> isOCCIAttribute = new Predicate<String>(){
//
//        @Override
//        public boolean apply(String t) {
//            return t.startsWith("X-OCCI-Attribute");
//        }
//    };
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
     * Metodo di comodo per effettuare un'invocazione senza argomenti
     *
     * @param post
     * @return
     * @throws Exception
     */
    private HttpResponse doOCCIInvocation(
            boolean consumeEntity,
            Integer success_status,
            StringBuilder error_message) throws Exception {
        return this.doOCCIInvocation(HTTPMETHODS.GET, null, null, null, null, consumeEntity, success_status, error_message);
    }

    /**
     * Effettua un'invocazione OCCI
     *
     * @param HTTPMETHODS Metodo HTTP
     * @param categories: Le Category da mettere nell'header della richiesta
     * @param occi_attributes: I X-OCCI-Attribute da mettere nell'header della
     * richiesta
     * @param path La stringa relativa alla risorsa dopo /compute/
     * @param inURLParams I parametri da mettere direttamente nell'URL dopo ? nella
     * @param consumeEntity if true the response.entity is consumed (for thread issue)
     * forma "key=value"
     * @return
     */
    private HttpResponse doOCCIInvocation(
                                HTTPMETHODS method, 
                                String[] categories, 
                                String[] occi_attributes, 
                                String path, 
                                String[] inURLParams,
                                boolean consumeEntity,
                                Integer success_status,
                                StringBuilder message_error
                                ) throws Exception {
        //DefaultHttpClient httpclient = new DefaultHttpClient();
        StringBuffer requestURL = new StringBuffer(this.occiCompute);
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


        request.addHeader("Content-Type", "text/occi");



        if (categories != null) {

            for (String category : categories) {
                request.addHeader("Category", category);

            }
        }

        if (occi_attributes != null) {
            for (String attribute : occi_attributes) {
                request.addHeader("X-OCCI-Attribute", attribute);
            }
        }

        if (!this.occiAuth.doAuth(request)) //authentication
        {

            throw new Exception("Authentication Error");
        }
        HttpResponse response = HttpClientFactory.getThreadSafeClient().execute(request);

        if(success_status!=null)
        {
            StringBuilder error = message_error;
            if(error == null)
            {
                error = new StringBuilder("Error :");
            }
            if(response.getStatusLine().getStatusCode() != success_status)
            {
                throw new Exception(this.manageInvocationErrors(error, response));
            }
            
            
            
        }




        if(consumeEntity)
        {
            EntityUtils.consume(response.getEntity());
        }

        return response;




    }

    /**
     * Metodo di comodo per effettuare chiamate su una VM
     *
     * @param vmId L'id della VM (OCCI id)
     * @param action l'azione (per es. "start"
     * @return
     * @throws Exception
     */
    private HttpResponse doOCCIInvocation(
                                HTTPMETHODS method,
                                String vmId,
                                String action,
                                boolean consumeEntity,
                                Integer success_status,
                                StringBuilder error_message) throws Exception {
        String categories[] = {
            action + "; scheme=\"http://schemas.ogf.org/occi/infrastructure/compute/action#\"; class=\"action\""
        };
        String inURLParams[] = {
            "action=" + action
        };

        return this.doOCCIInvocation(method, categories, null, vmId, inURLParams, consumeEntity, success_status, error_message);
    }

    /**
     * Metodo di comodo per effettuare chiamate su una VM
     *
     * @param vmId L'id della VM (OCCI id)
     * @param action l'azione (per es. "start"
     * @return
     * @throws Exception
     */
    private HttpResponse doOCCIInvocation(HTTPMETHODS method, 
                                          String vmId,
                                          boolean consumeEntity,
                                          Integer success_status,
                                          StringBuilder error_message) throws Exception {


        return this.doOCCIInvocation(method, null, null, vmId, null, consumeEntity, success_status, error_message);
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


        //InputStream instream = this.doOCCIInvocation(HTTPMETHODS.GET, null, null, id, null).getEntity().getContent();
        HttpResponse response = this.doOCCIInvocation(
                                            HTTPMETHODS.GET,
                                            id,
                                            false,
                                            HttpStatus.SC_OK,
                                            new StringBuilder("Error retrieving OCCI attributes for VM:").append(id));
       
        HttpEntity entity = response.getEntity();



        final Pattern p = Pattern.compile("X-OCCI-Attribute: occi\\.(compute|core)\\.(.*)=\"(.*)\"");
//    final Pattern p = Pattern.compile("X-OCCI-Attribute: occi\\.compute\\.(.*)=\"(.*)\"");
        final Map<String, String> attributes = new HashMap<String, String>();


        for (String f : Iterables.filter(Arrays.asList(this.getBodyAsString(entity).split("\n")), isOCCIAttribute)) {
            Matcher m = p.matcher(f);
            if (m.find()) {

                attributes.put(m.group(2), m.group(3)); // the first group is "compute" "core" 
            }
        }

        return attributes;

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


        return new VEState(
                OCCI2CLEVERSTATES.get(attributes.get("state")),
                attributes.get("id"),
                attributes.get("hostname"));
    }

    /**
     * Recover OCCI uuid from clever name
     *
     * @param vmId name of VM (clever name)
     *
     * @return OCCI UUID
     * @throws Exception
     */
    private String getOcciIDfromName(final String name) throws Exception {

        List<VEState> vms = this.recoverVMsFromOCCIServer();
        VEState vm = null;
        try {
            vm = Iterables.find(vms, new Predicate<VEState>() {
                @Override
                public boolean apply(VEState t) {
                    return (t == null || t.getName() == null ? false : t.getName().equals(name));
                }
            });
        } catch (java.util.NoSuchElementException ex) {
            throw new Exception("VM not found");
        }

        if (vm == null) {
            throw new Exception("VM not found");
        }
        logger.debug("From name " + name + " to id: " + vm.getId());
        return vm.getId();

    }

    /**
     * Recover state of VM from server
     *
     *
     * @return: List of VeState : the list is created by guava and is not
     * suitable for serialization (copy it in another list)
     */
    private List<VEState> recoverVMsFromOCCIServer() throws Exception {
        HttpResponse response = doOCCIInvocation(
                                        false,
                                        HttpStatus.SC_OK,
                                        new StringBuilder("Error listing VMS; "));
        
        List<String> responses = Lists.newLinkedList(
                Iterables.filter(
                            Arrays.asList(getBodyAsString(response.getEntity()).split("\n")),
                            new IsOCCIPredicate("X-OCCI-Location")
                )
        );

        List<VEState> result = Lists.transform(responses, new Function<String, VEState>() {
            @Override
            public VEState apply(String string) {
                String uuid = string.replaceAll(".*/", "");
                try {
                    VEState vm = getVMState(uuid);
                    logger.debug("VM found: " + vm);
                    return vm;
                } catch (Exception ex) {
                    logger.error("error in conversion from OCCI to clever vmstate : " + ex);
                    return new VEState(null, null, null);
                }
            }
        });
        return result;
    }

    
    /* ********* Methods to be invoked by external (Implement methods from RunnerPlugin)**********
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

                String computePath = occi.getChildText("compute_path");


                this.occiURL = new URL(protocol, host, Integer.parseInt(port), "/"); //con il null o stringa vuota come file NullPointerException

                this.occiCompute = this.occiURL.toString() + (computePath == null ? "/compute/" : computePath);


            } catch (MalformedURLException ex) {
                logger.error("Error in configuration parameters: " + ex.getMessage());
                throw new CleverException(ex);
            }



            logger.debug("URL used for OCCI invocation: " + this.occiCompute);


            Element auth = params.getChild("auth");
            try {
                if (auth.getAttributeValue("type").equals("keystone")) {
                    occiAuth = new OCCIAuth(new OCCIAuthKeystoneImpl(this.occiURL, params.getChildText("tenant"), auth));
                } else if (auth.getAttributeValue("type").equals("basic"))//basic
                {
                    occiAuth = new OCCIAuth(new OCCIAuthBasicImpl(auth));
                } else {
                    occiAuth = new OCCIAuth(new OCCINoAuth(this.occiURL));
                }
                occiAuth.initClient(HttpClientFactory.getThreadSafeClient()); //controllare se multithread funziona
            } catch (MalformedURLException ex) {
                logger.error("Error in configuration parameters (authorization)" + ex.getMessage());
                throw new CleverException(ex);
            }
        }

        this.owner = owner;
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
        HttpResponse response = this.doOCCIInvocation(
                                            HTTPMETHODS.POST,
                                            occiID,
                                            "start",
                                            true,
                                            HttpStatus.SC_OK,
                                            new StringBuilder("Error starting VM : ").append(name));
              
        return true;


    }

    @Override   //l'esclusivo viene ignorato - tutto il metodo e' da considerare e testare attentamente
    public boolean createVm(String veId, VEDescription ved, Boolean notExclusive) throws Exception {

        //Qui dovrebbe creare il favor con le caratteristiche prese da VED e registrare l'immagine presa tramite imagemanager (o storage manager)
        String flavor = ved.getName(), img = ved.getStorage().get(0).getDiskPath(); //per ora nello scenario della chiamata diretta all HM il diskpath e' il nome dell'immagine del repo OCCI


        String categories[] = {
            "compute; scheme=\"http://schemas.ogf.org/occi/infrastructure#\"; class=\"kind\"",
            flavor + "; scheme=\"http://schemas.openstack.org/template/resource#\"; class=\"mixin\"",
            img + "; scheme=\"http://schemas.openstack.org/template/os#\"; class=\"mixin\""
        };

        String occiattributes[] = {
            "occi.compute.hostname=\"" + veId + "\""
        };



        HttpResponse response = this.doOCCIInvocation(HTTPMETHODS.POST,
                categories,
                occiattributes,
                null,
                null,
                true,
                HttpStatus.SC_CREATED,
                new StringBuilder("Error in VM creation : ").append(veId));

        
        return true;



    }

    @Override
    public boolean createAndStart(String veId, VEDescription parameters, Boolean notExclusive) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean suspend(String name) throws Exception {


        String occiID = getOcciIDfromName(name);

        HttpResponse response = this.doOCCIInvocation(
                                        HTTPMETHODS.POST,
                                        occiID,
                                        "suspend",
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



        HttpResponse response = this.doOCCIInvocation(
                                        HTTPMETHODS.DELETE,
                                        occiID,
                                        true,
                                        HttpStatus.SC_OK,
                                        new StringBuilder("Error in VM destroy : ").append(name));

        return true;




    }

    @Override
    public boolean shutDownVm(String name) throws Exception {

        String occiID = getOcciIDfromName(name);


        HttpResponse response = this.doOCCIInvocation(
                                        HTTPMETHODS.POST,
                                        occiID,
                                        "stop",
                                        true,
                                        HttpStatus.SC_OK,
                                        new StringBuilder("Error in VM shutdown : ").append(name));
        return true;


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

    @Override
    public String getHYPVRName() {
        return "OCCI/1.1";
    }
    /**
     * ************************************************************
     */
}
