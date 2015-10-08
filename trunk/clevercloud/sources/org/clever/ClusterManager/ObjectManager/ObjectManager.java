/*
 * Copyright 2014 Università di Messina
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
/*
 * The MIT License
 *
 * Copyright 2011 Alessio Di Pietro.
 * Copyright 2013-14 Giuseppe Tricomi
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
package org.clever.ClusterManager.ObjectManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import it.eng.rspa.sigma.il.xml.publish.builder.ILXMLPublishBuilder;
import it.eng.rspa.sigma.il.xml.publish.builder.ILXMLPublishBuilderException;
import it.eng.rspa.sigma.il.xml.publish.builder.MultimediaValues;
import it.eng.rspa.sigma.il.xml.publish.builder.ParametersKeys;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.Dispatcher.DispatcherAgent;
import org.clever.Common.Communicator.MethodInvoker;
import org.clever.Common.Communicator.Notification;
import org.clever.Common.Exceptions.CleverException;
import org.clever.ClusterManager.Dispatcher.CLusterManagerDispatcherPlugin;
import org.clever.ClusterManager.IdentityServicePlugins.Keystone.Token;
import org.clever.Common.Communicator.CmAgent;
import org.clever.Common.Digest.Digest;
import org.clever.Common.Utils.ObjectSwift;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoContainerForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoCreateObjectMetadataForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoCopyObjectForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoDeleteObjectForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoGetObjectForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoOperationContainerMetadataForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InsertContainer;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InsertObject;
import org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput;
import org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput;
import static org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput.tipoObjectOutput.InfoOperationContainerMetadataForMongoDb;
/**
 *
 *
 * @author Antonio Galletta
 */
public class ObjectManager extends CmAgent{
   
    private final String version = "0.0.1";
    private final Logger logger;
    private DispatcherAgent dispatcherAgent;
    private final CLusterManagerDispatcherPlugin dispatcherPlugin;
    private Token token;
    private FileOutputStream tempiUpload, tempiDbyname,tempiDbyURL;

    
    public ObjectManager(CLusterManagerDispatcherPlugin dispatcherPlugin)throws CleverException {
        super();
        logger = Logger.getLogger("ObjectManager");
        this.dispatcherPlugin = dispatcherPlugin;
    }
    public ObjectManager(DispatcherAgent dispatcheragent) throws CleverException {
        super();
        logger = Logger.getLogger("ObjectManager");
        this.dispatcherAgent= dispatcheragent;
        this.dispatcherPlugin = dispatcherAgent.getDispatcherPlugin();
    }
    
    @Override
    public void initialization()throws CleverException
    {     
        try {
            super.setAgentName("ObjectManagerAgent");
            super.start();
            tempiUpload=new FileOutputStream("tempiUpload.csv", true);
            tempiDbyname=new FileOutputStream("tempiDByName.csv", true);
            tempiDbyURL=new FileOutputStream("tempiDbyUrl.csv", true);
            logger.debug("inizializzazione terminata");
        } catch (FileNotFoundException ex) {
          logger.error("errore nella creazione file",ex);
        }
         }
  
  @Override
  public Class getPluginClass() {
        return ObjectManager.class;
    }

    @Override
    public Object getPlugin() {
        return this;
    }

        @Override
   public void shutDown()
    {
        
    }
   
       @Override
    public void handleNotification(Notification notification) throws CleverException {
        logger.debug("Received notification type: "+notification.getId());
        
    }
    
 public void insertInSwift(Object elementToInsert){
   
       ObjectSwift obj=(ObjectSwift) elementToInsert;
       logger.debug("insert in swift object");
       this.insertInSwift(obj);
   
  }
 

public void insertInSwift(ObjectSwift elementToInsert){
  
      String userSwift,tenant,publish,originalName,md5Name,nameContainer, pathObject,hostTarget,docJSON;
      SwiftParameterOutput swiftParamOut;
      ArrayList params=new ArrayList();
      InsertContainer swiftInsertContainer= new InsertContainer();
      SwiftParameterInput swiftParameterIn= new SwiftParameterInput();
      InsertObject swiftInsertObj= new InsertObject();
      BasicDBObject oggInfoUser=new BasicDBObject();
      HashMap <String, String> metadata;
      int statusCode,index;
      String tempi;
      long inizio = 0, fine = 0, intervallo;
      
      logger.info("insert in swift object swift");
      //inizializzo il nome del container ed il path dell'ogetto
      pathObject=elementToInsert.getPathObject();
      tenant=elementToInsert.getTenantSrc();
      userSwift=elementToInsert.getUserSrc();
      nameContainer=userSwift+"__"+tenant;
      metadata=elementToInsert.getMetadata();
//      MethodInvoker reqToken = new MethodInvoker("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
      ArrayList parametri=new ArrayList();  
      
      try { 
      swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertContainer;
      swiftInsertContainer.setContainer(nameContainer);
      logger.debug("richiesta token");
      //controlo se il token è null
 ////     if (token==null){
          inizio=System.currentTimeMillis();
     // token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
          token=this.requestToken();
      fine=System.currentTimeMillis();
     //controllare se il token tornato è null
  ////        }
      if (token==null){
       logger.error("impossibile ottenere il token");
       throw new CleverException(" impossibile ottenere il token ");
      }
      logger.debug("token ricevuto");
      
      intervallo=fine-inizio;
      tempi="tempo ottenimento token: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      
      swiftInsertContainer.setTokenId(token.getId());

      swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
      swiftInsertContainer.elaboraInfo();
      //accountName=swiftInsertContainer.getAccount();
      logger.debug("struttura dati InsertContainer riempita");
      
      logger.debug("creo elem info tenant-uuid in mongo");
      oggInfoUser.append("_id", nameContainer);
      oggInfoUser.append("user",userSwift);
      oggInfoUser.append("tenant",tenant);

      logger.debug("inserisco info associazione in mongo");
      parametri.add("obsDB");
      parametri.add("informazioniUtente");
      parametri.add(oggInfoUser.toString());

      //inizio=System.currentTimeMillis();
      
      
      MethodInvoker mi = new MethodInvoker("BigDataAgent","updateInCollection",false,parametri);


       inizio=System.currentTimeMillis();

//      this.updateInCollection("obsDB","informazioniUtente" ,oggInfoUser.toString());
      dispatcherPlugin.dispatchToIntern(mi);
   
      
            
      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="inserimento info utente: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      
      swiftParameterIn.setOgg(swiftInsertContainer);
      params.add("ObjectStorageAgent");
     
      //infoagent chiedi nome host con l'object storage attivo
     // mi = new MethodInvoker("InfoAgent", "getHostActive", true, params);
//    
     // hostTarget=(String) dispatcherPlugin.dispatchToIntern(mi);
      hostTarget=this.getNameActiveHost();
      //this.owner.invoke("InfoAgent", "getHostActive", true, params);
      logger.debug("prima dell'invocazione remota, nome host su cui invocare il metodo: "+hostTarget);
      if(hostTarget==null){
          throw new CleverException("error, non ci sono host con l'agente di switf attivo ");
      }
      params.clear();
      params.add(swiftParameterIn);
      
    
      
      MethodInvoker creaContainer = new MethodInvoker("ObjectStorageAgent", "createContainer", true, params);

 
      inizio=System.currentTimeMillis();
      
//  
      swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="tempo creazione container: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());

      
//((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createContainer", true, params);
      logger.debug("container creato con successo?"+swiftParamOut.toString());
           statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
      logger.debug("stato: "+statusCode);
      //procedo con le operazioni di inserimento oggetto, riempio le strutture dati
  
      
      
      if (statusCode>299){
      
          if(statusCode==401){
              logger.debug("token non valido");
           //   token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
               token=this.requestToken();
              //token=(Token) this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
               swiftInsertContainer.setTokenId(token.getId());
               swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
               swiftInsertContainer.elaboraInfo();
               swiftParameterIn.setOgg(swiftInsertContainer);
               params.clear();
               params.add(swiftParameterIn);
               creaContainer = new MethodInvoker("ObjectStorageAgent", "createContainer", true, params);
            //   swiftParamOut= (SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createContainer", true, params);
             
               inizio=System.currentTimeMillis();
               
               swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="tempo creazione container: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
               
               logger.debug("container creato con successo?"+swiftParamOut.toString());
               statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
               logger.debug("stato: "+statusCode);
      
               if(statusCode>299){
                  throw new CleverException("error, nella creazioen container, statusCode: "+statusCode);
               }

          }
          else{
         throw new CleverException("error, nella creazioen container, statusCode: "+statusCode);
          }
      }
      
      
      
      params.clear();
      logger.debug("pathfile::"+pathObject+"::");
      
      inizio=System.currentTimeMillis();
      md5Name=Digest.getMD5Checksum(pathObject);
      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="creazione nome MD5: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      index=pathObject.lastIndexOf("/");
      originalName=pathObject.substring(index+1);
      //md5Name=Digest.getMD5Checksum(pathObject)+""+originalName;
      logger.debug("nome file originale: "+originalName);
      logger.debug("nome file con md5: "+md5Name);
      metadata.put("originalName", originalName);
      
      String estensione;
      int indice;
      
      indice=originalName.indexOf('.');
      if (indice==-1){
          estensione="";
      }
      else{
        estensione=originalName.substring(indice);
        }
      
      swiftInsertObj.setContainer(nameContainer);
      swiftInsertObj.setTokenId(token.getId());
      swiftInsertObj.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
  //qui md5Name
      swiftInsertObj.setPathObject(pathObject);
      logger.debug("metadati:"+metadata.toString());
      swiftInsertObj.setMetadati(metadata);
      swiftInsertObj.elaboraInfo();
      swiftInsertObj.setObject(md5Name+estensione);
      swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertObject;
      swiftParameterIn.setOgg(swiftInsertObj);
      params.add(swiftParameterIn);
      
      logger.debug("richiamo il metodo remoto per l'inserimento dell'oggetto");

      MethodInvoker creaOggetto = new MethodInvoker("ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
      
      
      inizio=System.currentTimeMillis();
      
      
      swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaOggetto, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="tempo inserimento obj: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      

//((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
     statusCode=Integer.valueOf(((InfoCreateObjectMetadataForMongoDb) swiftParamOut).getStatusCode());
      
      logger.debug("codice risposta:"+statusCode);
      
      
      
       if (statusCode>299){
      
          if(statusCode==401){
              logger.debug("token non valido");
//              token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
                //token=(Token) this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
               token=this.requestToken();
               swiftInsertObj.setTokenId(token.getId());
               swiftInsertObj.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
               swiftInsertObj.elaboraInfo();
               swiftInsertObj.setObject(md5Name);
               params.clear();
               swiftParameterIn.setOgg(swiftInsertObj);
               params.add(swiftParameterIn);
               logger.debug("richiamo il metodo remoto per l'inserimento dell'oggetto");
               creaOggetto = new MethodInvoker("ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
      
               
      inizio=System.currentTimeMillis();
               
               swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaOggetto, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="tempo inserimento obj: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
               
               
               
// swiftParamOut=(SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
               statusCode=Integer.valueOf(((InfoCreateObjectMetadataForMongoDb) swiftParamOut).getStatusCode());
               logger.debug("codice risposta:"+statusCode);
      
               if(statusCode>299){
                  throw new CleverException("error, nella create object, statusCode: "+statusCode);
               }

          }
          else{
         throw new CleverException("error, nella create object, statusCode: "+statusCode);
          }
      }
     // accountName=((InfoCreateObjectForMongoDb)swiftParamOut).getAccount();
      else{ 
//devo inserire in mogno pure se l'inserimento non è andato a buon fine?
      docJSON=((InfoCreateObjectMetadataForMongoDb)swiftParamOut).infoToJsonMONGO();
    
      //controlla codice risposta
      
     // logger.debug("oggetto json da inserire: "+docJSON);
      logger.debug("richiamo inserimento sul db nella collezione: "+nameContainer);
   
      
   inizio=System.currentTimeMillis();

      publish=this.createPublish(docJSON, metadata);
      
       fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="tempo creazione publish: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      
      parametri.clear();
      parametri.add("obsDB");
      parametri.add(nameContainer);
      parametri.add(publish);
      parametri.add(md5Name);
      mi = new MethodInvoker("BigDataAgent","insertXMLString",false,parametri);
   
inizio=System.currentTimeMillis();
      
      dispatcherPlugin.dispatchToIntern(mi);    

 fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="inserimento su mongo: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
               

//  this.insertInCollection("obsDB",nameContainer ,docJSON);
      logger.debug("inserimento terminato");
       }
      }
       
       catch(Exception e){
           logger.error("error in insert In SWift",e);
           //inserisci nel logging quello che è successo
          // todo: fai restituire un'eccezione da gestire x far ripartire il processo
       }     
  }

public void moveObject(ObjectSwift elementToInsert){
  
      String userSwift,tenant,publish,md5Name,nameContainer, pathObject,hostTarget,docJSON;
      SwiftParameterOutput swiftParamOut;
      ArrayList params=new ArrayList();
      InsertContainer swiftInsertContainer= new InsertContainer();
      SwiftParameterInput swiftParameterIn= new SwiftParameterInput();
      InsertObject swiftInsertObj= new InsertObject();
      BasicDBObject oggInfoUser=new BasicDBObject();
      HashMap <String, String> metadata;
      int statusCode,index;
      String tempi;
      long inizio = 0, fine = 0, intervallo;
      
      logger.debug("insert in swift object swift");
      //inizializzo il nome del container ed il path dell'ogetto
      pathObject=elementToInsert.getPathObject();
      tenant=elementToInsert.getTenantSrc();
      userSwift=elementToInsert.getUserSrc();
      nameContainer=userSwift+"__"+tenant;
      metadata=elementToInsert.getMetadata();
//      MethodInvoker reqToken = new MethodInvoker("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
      ArrayList parametri=new ArrayList();  
      
      try { 
      swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertContainer;
      swiftInsertContainer.setContainer(nameContainer);
      logger.debug("richiesta token");
      //controlo se il token è null
 ////     if (token==null){
          inizio=System.currentTimeMillis();
     // token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
          token=this.requestToken();
      fine=System.currentTimeMillis();
     //controllare se il token tornato è null
  ////        }
      if (token==null){
       logger.error("impossibile ottenere il token");
       throw new CleverException(" impossibile ottenere il token ");
      }
      logger.debug("token ricevuto");
      
      intervallo=fine-inizio;
      tempi="ottenimento token: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      
      swiftInsertContainer.setTokenId(token.getId());

      swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
      swiftInsertContainer.elaboraInfo();
      //accountName=swiftInsertContainer.getAccount();
      logger.debug("struttura dati InsertContainer riempita");
      
      logger.debug("creo elem info tenant-uuid in mongo");
      oggInfoUser.append("_id", nameContainer);
      oggInfoUser.append("user",userSwift);
      oggInfoUser.append("tenant",tenant);

      logger.debug("inserisco info associazione in mongo");
      parametri.add("obsDB");
      parametri.add("informazioniUtente");
      parametri.add(oggInfoUser.toString());

      //inizio=System.currentTimeMillis();
      
      
      MethodInvoker mi = new MethodInvoker("BigDataAgent","updateInCollection",false,parametri);


       inizio=System.currentTimeMillis();

//      this.updateInCollection("obsDB","informazioniUtente" ,oggInfoUser.toString());
      dispatcherPlugin.dispatchToIntern(mi);
   
      
            
      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="info utente: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      
      swiftParameterIn.setOgg(swiftInsertContainer);
      params.add("ObjectStorageAgent");
     
      //infoagent chiedi nome host con l'object storage attivo
     // mi = new MethodInvoker("InfoAgent", "getHostActive", true, params);
//    
     // hostTarget=(String) dispatcherPlugin.dispatchToIntern(mi);
      hostTarget=this.getNameActiveHost();
      //this.owner.invoke("InfoAgent", "getHostActive", true, params);
      logger.debug("prima dell'invocazione remota, nome host su cui invocare il metodo: "+hostTarget);
      if(hostTarget==null){
          throw new CleverException("error, non ci sono host con l'agente di switf attivo ");
      }
      params.clear();
      params.add(swiftParameterIn);
      
    
      
      MethodInvoker creaContainer = new MethodInvoker("ObjectStorageAgent", "createContainer", true, params);

 
      inizio=System.currentTimeMillis();
      
//  
      swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="creazione container: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());

      
//((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createContainer", true, params);
      logger.debug("container creato con successo?"+swiftParamOut.toString());
           statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
      logger.debug("stato: "+statusCode);
      //procedo con le operazioni di inserimento oggetto, riempio le strutture dati
  
      
      
      if (statusCode>299){
      
          if(statusCode==401){
              logger.debug("token non valido");
           //   token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
               token=this.requestToken();
              //token=(Token) this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
               swiftInsertContainer.setTokenId(token.getId());
               swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
               swiftInsertContainer.elaboraInfo();
               swiftParameterIn.setOgg(swiftInsertContainer);
               params.clear();
               params.add(swiftParameterIn);
               creaContainer = new MethodInvoker("ObjectStorageAgent", "createContainer", true, params);
            //   swiftParamOut= (SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createContainer", true, params);
             
               inizio=System.currentTimeMillis();
               
               swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="creazione container: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
               
               logger.debug("container creato con successo?"+swiftParamOut.toString());
               statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
               logger.debug("stato: "+statusCode);
      
               if(statusCode>299){
                  throw new CleverException("error, nella creazioen container, statusCode: "+statusCode);
               }

          }
          else{
         throw new CleverException("error, nella creazioen container, statusCode: "+statusCode);
          }
      }
      
      params.clear();
      md5Name=metadata.get("md5Name");
      logger.debug("nome in md5 del file da inserire: "+md5Name);
      swiftInsertObj.setContainerDestination(nameContainer);
      swiftInsertObj.setContainerOrigin(nameContainer+"_tmp");
      swiftInsertObj.setObjectDestination(md5Name);
      swiftInsertObj.setObjectOrigin(md5Name);
      swiftInsertObj.setTokenId(token.getId());
      swiftInsertObj.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
  //qui md5Name
      swiftInsertObj.setPathObject(pathObject);
      logger.debug("metadati:"+metadata.toString());
      swiftInsertObj.setMetadati(metadata);
      swiftInsertObj.elaboraInfo();
      //swiftInsertObj.setObject(md5Name+estensione);
      swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertObject;
      swiftParameterIn.setOgg(swiftInsertObj);
      params.add(swiftParameterIn);
      
      logger.debug("richiamo il metodo remoto per l'inserimento dell'oggetto");

      MethodInvoker creaOggetto = new MethodInvoker("ObjectStorageAgent", "copyObject", true, params);
      inizio=System.currentTimeMillis();
      swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaOggetto, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="inserimento obj: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      

//((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
     statusCode=Integer.valueOf(((InfoCopyObjectForMongoDb) swiftParamOut).getStatusCode());
      
      logger.debug("codice risposta:"+statusCode);
      
      
      
       if (statusCode>299){
      
          if(statusCode==401){
              logger.debug("token non valido");
//              token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
                //token=(Token) this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
               token=this.requestToken();
               swiftInsertObj.setTokenId(token.getId());
               swiftInsertObj.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
               swiftInsertObj.elaboraInfo();
              // swiftInsertObj.setObject(md5Name);
               params.clear();
               swiftParameterIn.setOgg(swiftInsertObj);
               params.add(swiftParameterIn);
               logger.debug("richiamo il metodo remoto per l'inserimento dell'oggetto");
               creaOggetto = new MethodInvoker("ObjectStorageAgent", "copyObject", true, params);
      
               
      inizio=System.currentTimeMillis();
               
               swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaOggetto, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="inserimento obj: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
               
               
               
// swiftParamOut=(SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
               statusCode=Integer.valueOf(((InfoCopyObjectForMongoDb) swiftParamOut).getStatusCode());
               logger.debug("codice risposta:"+statusCode);
      
               if(statusCode>299){
                  throw new CleverException("error, nella create object, statusCode: "+statusCode);
               }

          }
          else{
         throw new CleverException("error, nella create object, statusCode: "+statusCode);
          }
      }
     // accountName=((InfoCreateObjectForMongoDb)swiftParamOut).getAccount();
      else{ 
           //cancella dal container tmp
      docJSON=((InfoCopyObjectForMongoDb)swiftParamOut).infoToJsonMONGO();
      params.clear();
      swiftInsertObj.setContainer(nameContainer+"_tmp");
      logger.debug("richiamo il metodo remoto per cancellare l'oggetto: "+md5Name);
      swiftInsertObj.setObject(md5Name);
      swiftInsertObj.setPathObject("");
      swiftInsertObj.elaboraInfo();
      //swiftInsertObj.setObject(md5Name+estensione);
      swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertObject;
      swiftParameterIn.setOgg(swiftInsertObj);
      params.add(swiftParameterIn);
 
      logger.debug("richiamo il metodo remoto per cancellare l'oggetto: "+md5Name);

      MethodInvoker delOggetto = new MethodInvoker("ObjectStorageAgent", "deleteObject", true, params);
      inizio=System.currentTimeMillis();
      swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(delOggetto, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="delete obj: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      

//((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
     statusCode=Integer.valueOf(((InfoDeleteObjectForMongoDb) swiftParamOut).getStatusCode());
      
      logger.debug("codice risposta del:"+statusCode);
      
      
      
       if (statusCode>299){
      
          if(statusCode==401){
              logger.debug("token non valido");
//              token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
                //token=(Token) this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
               token=this.requestToken();
               swiftInsertObj.setTokenId(token.getId());
               swiftInsertObj.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
               swiftInsertObj.elaboraInfo();
               swiftInsertObj.setObject(md5Name);
               params.clear();
               swiftParameterIn.setOgg(swiftInsertObj);
               params.add(swiftParameterIn);
               logger.debug("richiamo il metodo remoto per cancellare l'oggetto");
               delOggetto = new MethodInvoker("ObjectStorageAgent", "copyObject", true, params);
      
               
      inizio=System.currentTimeMillis();
               
               swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(delOggetto, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="delete obj: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
               
               
               
// swiftParamOut=(SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
               statusCode=Integer.valueOf(((InfoDeleteObjectForMongoDb) swiftParamOut).getStatusCode());
               logger.debug("codice risposta:"+statusCode);
      
               if(statusCode>299){
                  throw new CleverException("error, nella delete object, statusCode: "+statusCode);
               }

          }
          else{
         throw new CleverException("error, nella create object, statusCode: "+statusCode);
          }
      }
     // accountName=((InfoCreateObjectForMongoDb)swiftParamOut).getAccount();
      else{

      logger.debug("richiamo inserimento sul db nella collezione: "+nameContainer);
   
      
   inizio=System.currentTimeMillis();

      publish=this.createPublish(docJSON, metadata);
      
       fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="creazione publish: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      
      parametri.clear();
      parametri.add("obsDB");
      parametri.add(nameContainer);
      parametri.add(publish);
      parametri.add(md5Name);
      mi = new MethodInvoker("BigDataAgent","insertXMLString",false,parametri);
   
inizio=System.currentTimeMillis();
      
      dispatcherPlugin.dispatchToIntern(mi);    

 fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="inserimento publish: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
               

//  this.insertInCollection("obsDB",nameContainer ,docJSON);
      logger.debug("inserimento terminato");
       }
      }
      }
       
       catch(Exception e){
           logger.error("error in insert In SWift",e);
           //inserisci nel logging quello che è successo
          // todo: fai restituire un'eccezione da gestire x far ripartire il processo
       }     
  }

public String getObjectByName(ObjectSwift elementToFind){
  
      String state = null, userSwift,tenant, fileName,url;
      DBObject obj;
      ArrayList params=new ArrayList();
      List<DBObject> fileList;
      String tempi;
      long inizio = 0, fine = 0, intervallo;
    
      logger.debug("qqaazaza:getObjectByName");
      //inizializzo il nome del container ed il path dell'ogetto
      fileName=elementToFind.getPathObject();
      tenant=elementToFind.getTenantSrc();
      userSwift=elementToFind.getUserSrc();
      try { 
          MethodInvoker mi;
 
             params.clear();
             params.add(fileName);
             params.add(userSwift);
             params.add(tenant);
             params.add("true");

             mi = new MethodInvoker("BigDataAgent","getUrlFile",true,params);
       
             inizio=System.currentTimeMillis();
             fileList=(List) dispatcherPlugin.dispatchToIntern(mi);
             fine=System.currentTimeMillis();
             intervallo=fine-inizio;
             tempi="tempo recupero url: "+intervallo+" \n";
            tempiDbyname.write(tempi.getBytes());
       
            if(fileList.isEmpty()){
               
               throw new CleverException("File not found!");
             }
           else{
               obj=fileList.get(fileList.size()-1);
               url=(String)obj.get("/notification/org<_>clever<_>HostManager<_>SAS<_>SensorAlertMessage/value/identifier");
                }    
            params.clear();
            elementToFind.setPathObject(url);
           
           //richiama altro metodo
            state=this.getObjectbyUrl(elementToFind);
             return state;
        }
       
       catch(Exception e){
           logger.error("error in getobjbyName In SWift",e);
           return null;
           //inserisci nel logging quello che è successo
          // todo: fai restituire un'eccezione da gestire x far ripartire il processo
       }
      
  }



public String getObjectbyUrlOld(ObjectSwift elementToInsert){
  
      String tempi, state = null, userSwift,tenant,md5Name,nameContainerDst, hostTarget, url, nameContainerSrc;
      String array[];
      SwiftParameterOutput swiftParamOut;
      ArrayList params=new ArrayList();
      InsertContainer swiftInsertContainer= new InsertContainer();
      SwiftParameterInput swiftParameterIn= new SwiftParameterInput();
      InsertObject swiftInsertObj= new InsertObject();
      BasicDBObject oggInfoUser=new BasicDBObject();
      int statusCode,length;
      long inizio = 0, fine = 0, intervallo;
      
      logger.debug("get object from swift");
      //inizializzo il nome del container ed il path dell'ogetto
      url=elementToInsert.getPathObject();
      array=url.split("/");
      length=array.length;
      tenant=elementToInsert.getTenantSrc();
      md5Name=array[length-1];
      nameContainerSrc=array[length-2];
      logger.info("container src"+nameContainerSrc);
      userSwift=elementToInsert.getUserSrc();
      nameContainerDst=userSwift+"__"+tenant+"_tmp";
//      MethodInvoker reqToken = new MethodInvoker("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
      ArrayList parametri=new ArrayList();  
      
      try { 
      swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertContainer;
      swiftInsertContainer.setContainer(nameContainerDst);
      logger.debug("richiesta token");
      //controlo se il token è null
 ////     if (token==null){
          inizio=System.currentTimeMillis();
     // token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
          token=this.requestToken();
      fine=System.currentTimeMillis();
     //controllare se il token tornato è null
  ////        }
      if (token==null){
       logger.error("impossibile ottenere il token");
       throw new CleverException(" impossibile ottenere il token ");
      }
      logger.debug("token ricevuto");
      
      intervallo=fine-inizio;
      tempi="tempo ottenimento token: "+intervallo+" \n";
      this.tempiDbyURL.write(tempi.getBytes());
      
      swiftInsertContainer.setTokenId(token.getId());

      swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
      swiftInsertContainer.elaboraInfo();
      //accountName=swiftInsertContainer.getAccount();
      logger.debug("struttura dati InsertContainer riempita");
      
      logger.debug("creo elem info tenant-uuid in mongo");
      oggInfoUser.append("_id", nameContainerDst);
      oggInfoUser.append("user",userSwift);
      oggInfoUser.append("tenant",tenant);

      logger.debug("inserisco info associazione in mongo");
      parametri.add("obsDB");
      parametri.add("informazioniUtente");
      parametri.add(oggInfoUser.toString());

      //inizio=System.currentTimeMillis();
      
      
      MethodInvoker mi = new MethodInvoker("BigDataAgent","updateInCollection",false,parametri);


       inizio=System.currentTimeMillis();

//      this.updateInCollection("obsDB","informazioniUtente" ,oggInfoUser.toString());
      dispatcherPlugin.dispatchToIntern(mi);
     fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="inserimento info utente: "+intervallo+" \n";
      tempiDbyURL.write(tempi.getBytes());
      
      swiftParameterIn.setOgg(swiftInsertContainer);
      params.add("ObjectStorageAgent");
     
      //infoagent chiedi nome host con l'object storage attivo
     // mi = new MethodInvoker("InfoAgent", "getHostActive", true, params);
//    
     // hostTarget=(String) dispatcherPlugin.dispatchToIntern(mi);
      hostTarget=this.getNameActiveHost();
      //this.owner.invoke("InfoAgent", "getHostActive", true, params);
      logger.debug("prima dell'invocazione remota, nome host su cui invocare il metodo: "+hostTarget);
      if(hostTarget==null){
          throw new CleverException("error, non ci sono host con l'agente di switf attivo ");
      }
      params.clear();
      
      params.add(swiftParameterIn);
     
      MethodInvoker creaContainer = new MethodInvoker("ObjectStorageAgent", "createContainer", true, params);
 
      inizio=System.currentTimeMillis();
//  
      swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="tempo creazione container: "+intervallo+" \n";
      tempiDbyURL.write(tempi.getBytes());
      
      logger.debug("container creato con successo?"+swiftParamOut.toString());
           statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
      logger.debug("stato: "+statusCode);
      //procedo con le operazioni di inserimento oggetto, riempio le strutture dati
      
      
      if (statusCode>299){
      
          if(statusCode==401){
              logger.debug("token non valido");
           //   token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
               token=this.requestToken();
              //token=(Token) this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
               swiftInsertContainer.setTokenId(token.getId());
               swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
               swiftInsertContainer.elaboraInfo();
               swiftParameterIn.setOgg(swiftInsertContainer);
               params.clear();
               params.add(swiftParameterIn);
               creaContainer = new MethodInvoker("ObjectStorageAgent", "createContainer", true, params);
            //   swiftParamOut= (SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createContainer", true, params);
             
               inizio=System.currentTimeMillis();
               
               swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="tempo creazione container: "+intervallo+" \n";
      tempiDbyURL.write(tempi.getBytes());
               
               logger.debug("container creato con successo?"+swiftParamOut.toString());
               statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
               logger.debug("stato: "+statusCode);
      
               if(statusCode>299){
                  throw new CleverException("error, nella creazioen container, statusCode: "+statusCode);
               }

          }
          else{
         throw new CleverException("error, nella creazioen container, statusCode: "+statusCode);
          }
      }

      
        params.clear();
      logger.debug("nome in md5 del file da scaricare: "+md5Name);
      state=md5Name;
      swiftInsertObj.setContainerDestination(nameContainerDst);
      swiftInsertObj.setContainerOrigin(nameContainerSrc);
      swiftInsertObj.setObjectDestination(md5Name);
      swiftInsertObj.setObjectOrigin(md5Name);
      swiftInsertObj.setTokenId(token.getId());
      swiftInsertObj.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
  //qui md5Name
      swiftInsertObj.elaboraInfo();
      //swiftInsertObj.setObject(md5Name+estensione);
      swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertObject;
      swiftParameterIn.setOgg(swiftInsertObj);
      params.add(swiftParameterIn);
      
      logger.debug("richiamo il metodo remoto per l'inserimento dell'oggetto");

      MethodInvoker creaOggetto = new MethodInvoker("ObjectStorageAgent", "copyObject", true, params);
      inizio=System.currentTimeMillis();
      swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaOggetto, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="tempo copia obj: "+intervallo+" \n";
      tempiDbyURL.write(tempi.getBytes());
      

//((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
     statusCode=Integer.valueOf(((InfoCopyObjectForMongoDb) swiftParamOut).getStatusCode());
      
      logger.debug("sswssssscodice risposta:"+statusCode);
      
      
      
       if (statusCode>299){
      
          if(statusCode==401){
              logger.debug("token non valido");
//              token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
                //token=(Token) this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
               token=this.requestToken();
               swiftInsertObj.setTokenId(token.getId());
               swiftInsertObj.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
               swiftInsertObj.elaboraInfo();
               swiftInsertObj.setObject(md5Name);
               params.clear();
               swiftParameterIn.setOgg(swiftInsertObj);
               params.add(swiftParameterIn);
               logger.debug("richiamo il metodo remoto per l'inserimento dell'oggetto");
               creaOggetto = new MethodInvoker("ObjectStorageAgent", "copyObject", true, params);
      
               
      inizio=System.currentTimeMillis();
               
               swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaOggetto, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="tempo copia obj: "+intervallo+" \n";
      tempiDbyURL.write(tempi.getBytes());
               
               
               
// swiftParamOut=(SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
               statusCode=Integer.valueOf(((InfoCopyObjectForMongoDb) swiftParamOut).getStatusCode());
               logger.debug("codice risposta:"+statusCode);
      
               if(statusCode>299){
                  throw new CleverException("error, nella get object, statusCode: "+statusCode);
               }

          }
          else{
         throw new CleverException("error, nella create get, statusCode: "+statusCode);
          }
      }
     // accountName=((InfoCreateObjectForMongoDb)swiftParamOut).getAccount();
      else{
            

//  this.insertInCollection("obsDB",nameContainer ,docJSON);
      logger.debug("retrivial terminato");
     
       }
       return state;
      }
       
       catch(Exception e){
           logger.error("error in getobjbyName In SWift",e);
           return null;
           //inserisci nel logging quello che è successo
          // todo: fai restituire un'eccezione da gestire x far ripartire il processo
       }
      
  }

public String getObjectbyUrl(ObjectSwift elementToInsert){
    
      String tempi, state = null, userSwift,tenant,md5Name,nameContainerDst, hostTarget, url, nameContainerSrc;
      String array[];
      int length;
      long inizio = 0, fine = 0, intervallo;
  
    
    logger.debug("get object from swift");
    url=elementToInsert.getPathObject();
    array=url.split("/");
    length=array.length;
    tenant=elementToInsert.getTenantSrc();
    md5Name=array[length-1];
    nameContainerSrc=array[length-2];
    logger.info("container src"+nameContainerSrc);
    userSwift=elementToInsert.getUserSrc();
    nameContainerDst=userSwift+"__"+tenant+"_tmp";
    
     try {
         
          logger.debug("richiesta token");
 ////     if (token==null){
          inizio=System.currentTimeMillis();
          token=this.requestToken();
          fine=System.currentTimeMillis();
  ////        }
          if (token==null){
              logger.error("impossibile ottenere il token");
              throw new CleverException(" impossibile ottenere il token ");
          }
          logger.debug("token ricevuto");
          intervallo=fine-inizio;
          tempi="tempo ottenimento token: "+intervallo+" \n";
          this.tempiDbyURL.write(tempi.getBytes());
          logger.debug("nome in md5 del file da scaricare: "+md5Name);
          state=md5Name;
          
          hostTarget=this.getNameActiveHost();
          logger.debug("prima dell'invocazione remota, nome host su cui invocare il metodo: "+hostTarget);
          if(hostTarget==null){
              throw new CleverException("error, non ci sono host con l'agente di switf attivo ");
          }
          this.copyInterTenant(md5Name, nameContainerDst, nameContainerSrc, token.getId(), hostTarget, "admin", tenant, new HashMap());
          logger.debug("retrivial terminato");
          return state;
       }
       catch(Exception e){
           logger.error("error in getobjbyName In SWift",e);
           return null;
           //inserisci nel logging quello che è successo
          // todo: fai restituire un'eccezione da gestire x far ripartire il processo
       }
}

private String getNameActiveHost(){

    ArrayList params=new ArrayList();
    params.add("ObjectStorageAgent");
    MethodInvoker mi;
    String hostName = null;
    //infoagent chiedi nome host con l'object storage attivo
    mi = new MethodInvoker("InfoAgent", "getHostActive", true, params);
        try {
             hostName=(String) dispatcherPlugin.dispatchToIntern(mi);
        } catch (CleverException ex) {
            logger.error("errore nell'ottenimento nome host", ex);
        }finally{
            return hostName;
        }

}

private void checkStatusCode() throws CleverException{}

private String createPublish(String stringJson, HashMap <String, String> metadata){
    

    String publish = null;
    BasicDBObject docJson=(BasicDBObject) JSON.parse(stringJson);
    long timestamp=System.currentTimeMillis();
    metadata.put(ParametersKeys.IS_MULTIMEDIA, MultimediaValues.IS_MULTIMEDIA_TRUE);
    metadata.put(ParametersKeys.NOTIFICATION_TIME_STAMP,new Timestamp(timestamp).toString());
    metadata.put(ParametersKeys.DATE_VALUE, String.valueOf(timestamp));
    metadata.put(ParametersKeys.PUBLUICATIONID_VALUE,docJson.getString("container"));
    metadata.put(ParametersKeys.ALERTEXPIRES_VALUE, "0");
    metadata.put(ParametersKeys.DCTITLE_VALUE, docJson.getString("object"));
    metadata.put(ParametersKeys.DCCREATOR_VALUE, docJson.getString("account"));
    metadata.put(ParametersKeys.DCIDENTIFIER_VALUE, docJson.getString("url"));
    //metadata.put(ParametersKeys.DCPROVENANCE_VALUE, docJson.getString("X-Object-Meta originalName"));
    
    try {
        publish = ILXMLPublishBuilder.buildXMLPublish(metadata);
	logger.debug("newpublish: "+publish);
	} catch (ParserConfigurationException | ILXMLPublishBuilderException e) {

            logger.error("errore nella creazione notifica", e);
        }
    return publish;


}

public void prova(){
    logger.debug("provaprovaprova");
}

private Token requestToken(){
          
    MethodInvoker reqToken = new MethodInvoker("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
    Token token=null;
    try {
            token= (Token) dispatcherPlugin.dispatchToIntern(reqToken);
        } catch (CleverException ex) {
            logger.error("impossibile ottenere il token", ex);
        } finally{
        return token;
    }



}

public String getObjectFromSwift(String fileName,String user,String tenant, String destPath){
    
    List<DBObject> fileList;
    DBObject obj;
    String state = null,url,originalFileName,hostTarget;
    InfoGetObjectForMongoDb response;
    ArrayList params = new ArrayList();
    params.add(fileName);
    params.add(user);
    params.add(tenant);
    params.add("true");

    MethodInvoker mi;
    
    mi = new MethodInvoker("BigDataAgent","getUrlFile",true,params);
        try {
           fileList=(List) dispatcherPlugin.dispatchToIntern(mi);
           if(fileList.isEmpty()){
               state="File not found!";
           }
           else{
               obj=fileList.get(0);
               url=(String)obj.get("/notification/org<_>clever<_>HostManager<_>SAS<_>SensorAlertMessage/value/identifier");
               originalFileName=(String)obj.get("/notification/org<_>clever<_>HostManager<_>SAS<_>SensorAlertMessage/value/provenance");
               if(token==null){
                    token=this.requestToken();
                  if (token==null){
                         logger.error("impossibile ottenere il token");
                         throw new CleverException(" impossibile ottenere il token ");
                   }
                  
                  params.clear();
                  hostTarget=this.getNameActiveHost();
      //this.owner.invoke("InfoAgent", "getHostActive", true, params);
      logger.debug("prima dell'invocazione remota, nome host su cui invocare il metodo: "+hostTarget);
      if(hostTarget==null){
          throw new CleverException("error, non ci sono host con l'agente di switf attivo ");
      }
                  
                  
                  params.clear();
                  params.add(url);
                  params.add(token.getId());
                  params.add(destPath);
                  params.add(originalFileName);
                  
                  mi = new MethodInvoker("ObjectStorageAgent","httpGetdownloadObject",true,params);
                  response=(InfoGetObjectForMongoDb)dispatcherPlugin.dispatchToExtern(mi, hostTarget);
                  //response=(InfoGetObjectForMongoDb) dispatcherPlugin.dispatchToIntern(mi);
                   if(response.getStatusCode().equals("200")){
                      state= "done!";
                      //throw new CleverException(" errore, status code "+response.getStatusCode());
                     }
                   else
                  if(response.getStatusCode().equalsIgnoreCase("404")){
                      token=this.requestToken();
                      if (token==null){
                         logger.error("impossibile ottenere il token");
                         throw new CleverException(" impossibile ottenere il token ");
                      }
                  params.clear();
                  params.add(url);
                  params.add(token.getId());
                  params.add(destPath);
                  params.add(originalFileName);
                  
                  logger.debug("qqqwwwwparams: "+url+"; "+token.getId()+"; "+destPath+"; "+originalFileName);
                  
                  mi = new MethodInvoker("ObjectStorageAgent","httpGetdownloadObject",true,params);
                  response=(InfoGetObjectForMongoDb) dispatcherPlugin.dispatchToIntern(mi);
                 logger.debug("aaaa status code:"+response.getStatusCode());
                  if(response.getStatusCode().equals("200")){
                      state= "done!";
                      //throw new CleverException(" errore, status code "+response.getStatusCode());
                     }
                  }
                   else
                       throw new CleverException(" errore, status code "+response.getStatusCode());

               }
               
           }
           
        } catch (CleverException ex) {

            logger.error("exception in getObject", ex);
            state="error!";
        }catch (Exception ex) {

            logger.error("exception in getObject", ex);
            state="error!";
        }
        finally{
            return state;
        
        }

}

public String moveObjectInterTenant(ObjectSwift elementToInsert){
    
    String userSwift,tenant,nameContainer,nameContainerSrc,tempi,hostTarget,tenantSrc,userSrc,md5Name,docJson,publish;
    long inizio = 0, fine = 0, intervallo; 
    int statusCode;
    BasicDBObject oggInfoUser=new BasicDBObject();
    ArrayList parametri=new ArrayList();
    MethodInvoker mi;
    HashMap <String, String> metadata;
      
    metadata=elementToInsert.getMetadata();
    tenant=elementToInsert.getTenantSrc();
    userSwift=elementToInsert.getUserSrc();    
    nameContainer=userSwift+"__"+tenant;
    tenantSrc=elementToInsert.getTenantSrc();
    userSrc=elementToInsert.getUserSrc();
    nameContainerSrc=nameContainer+"_tmp";
    
     try { 
        logger.debug("richiesta token");
        inizio=System.currentTimeMillis();
        token=this.requestToken();
        fine=System.currentTimeMillis();
        
        if (token==null){
            logger.error("impossibile ottenere il token");
            throw new CleverException(" impossibile ottenere il token ");
        }
        logger.debug("token ricevuto");
        intervallo=fine-inizio;
        tempi="ottenimento token: "+intervallo+" \n";
        tempiUpload.write(tempi.getBytes());
        
        hostTarget=this.getNameActiveHost();
        logger.debug("prima dell'invocazione remota, nome host su cui invocare il metodo: "+hostTarget);
        if(hostTarget==null){
          throw new CleverException("error, non ci sono host con l'agente di switf attivo ");
        }
        
         statusCode=this.createContainer(nameContainer, hostTarget);
         if (statusCode>299 ){
             if(statusCode==401){
                 logger.info("token Scaduto");
                 token=this.requestToken();
                 this.createContainer(nameContainer, hostTarget);
             }
             else{
                  throw new CleverException("error, nella creazione container, statusCode: "+statusCode);
             }
         }
         oggInfoUser.append("_id", nameContainer);
         oggInfoUser.append("user",userSwift);
         oggInfoUser.append("tenant",tenant);
         parametri.add("obsDB");
         parametri.add("informazioniUtente");
         parametri.add(oggInfoUser.toString());
         mi = new MethodInvoker("BigDataAgent","updateInCollection",false,parametri);
         
         inizio=System.currentTimeMillis();
         dispatcherPlugin.dispatchToIntern(mi);
         fine=System.currentTimeMillis();
         intervallo=fine-inizio;
         tempi="info utente: "+intervallo+" \n";
         tempiUpload.write(tempi.getBytes());
         
         statusCode=this.allowWrite(nameContainer, hostTarget, tenantSrc, userSrc);
         if (statusCode>299 ){
             if(statusCode==401){
                 logger.info("token Scaduto");
                 token=this.requestToken();
                 this.allowWrite(nameContainer, hostTarget, tenantSrc, userSrc);
             }
             else{
                  throw new CleverException("error, nell'aggiunta permessi, statusCode: "+statusCode);
             }
         }
         md5Name=metadata.get("md5Name");
         docJson=this.copyInterTenant(md5Name, nameContainer,nameContainerSrc, elementToInsert.getToken(), hostTarget, tenant, "admin",metadata);
         logger.debug("richiamo inserimento sul db nella collezione: "+nameContainer);
         
         inizio=System.currentTimeMillis();
         publish=this.createPublish(docJson, metadata);
         fine=System.currentTimeMillis();
         intervallo=fine-inizio;
         tempi="creazione publish: "+intervallo+" \n";
         tempiUpload.write(tempi.getBytes());
         
         parametri.clear();
         parametri.add("obsDB");
         parametri.add(nameContainer);
         parametri.add(publish);
         parametri.add(md5Name);
         mi = new MethodInvoker("BigDataAgent","insertXMLString",false,parametri);
         
         inizio=System.currentTimeMillis();
         dispatcherPlugin.dispatchToIntern(mi);    
         fine=System.currentTimeMillis();
         intervallo=fine-inizio;
         tempi="inserimento publish: "+intervallo+" \n";
         tempiUpload.write(tempi.getBytes());
         logger.debug("inserimento terminato");
         return "ok";
        
     }
     catch(Exception e){
     
         logger.error("error in insert In SWift",e);
         return "error";
     }
}

public void moveObjectInterTenantss(ObjectSwift elementToInsert){
  
      String userSwift,tenant,publish,md5Name,nameContainer, pathObject,hostTarget,docJSON;
      SwiftParameterOutput swiftParamOut;
      ArrayList params=new ArrayList();
      InsertContainer swiftInsertContainer= new InsertContainer();
      SwiftParameterInput swiftParameterIn= new SwiftParameterInput();
      InsertObject swiftInsertObj= new InsertObject();
      BasicDBObject oggInfoUser=new BasicDBObject();
      HashMap <String, String> metadata;
      int statusCode,index;
      String tempi;
      long inizio = 0, fine = 0, intervallo;
      
      logger.debug("insert in swift object swift");
      //inizializzo il nome del container ed il path dell'ogetto
      pathObject=elementToInsert.getPathObject();
  //    tenant=elementToInsert.getTenantSrc();
  //    userSwift=elementToInsert.getUserSrc();
  //    nameContainer=userSwift+"__"+tenant;
  //    metadata=elementToInsert.getMetadata();
      ArrayList parametri=new ArrayList();  
      
      try { 
//      swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertContainer;
//      swiftInsertContainer.setContainer(nameContainer);
//      logger.debug("richiesta token");
//          inizio=System.currentTimeMillis();
//          token=this.requestToken();
//      fine=System.currentTimeMillis();
//      if (token==null){
//       logger.error("impossibile ottenere il token");
//       throw new CleverException(" impossibile ottenere il token ");
//      }
//      logger.debug("token ricevuto");
      
//      intervallo=fine-inizio;
//      tempi="ottenimento token: "+intervallo+" \n";
//      tempiUpload.write(tempi.getBytes());
      
 //     swiftInsertContainer.setTokenId(token.getId());

 //     swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
 //     swiftInsertContainer.elaboraInfo();
 ////     logger.debug("struttura dati InsertContainer riempita");
      
 ////     logger.debug("creo elem info tenant-uuid in mongo");
 //     oggInfoUser.append("_id", nameContainer);
 //     oggInfoUser.append("user",userSwift);
 //     oggInfoUser.append("tenant",tenant);

 ////     logger.debug("inserisco info associazione in mongo");
 //     parametri.add("obsDB");
 //     parametri.add("informazioniUtente");
 //     parametri.add(oggInfoUser.toString());

      
      
 //     MethodInvoker mi = new MethodInvoker("BigDataAgent","updateInCollection",false,parametri);
 //      inizio=System.currentTimeMillis();
 //     dispatcherPlugin.dispatchToIntern(mi);
 //      fine=System.currentTimeMillis();
 //     intervallo=fine-inizio;
 //     tempi="info utente: "+intervallo+" \n";
 //     tempiUpload.write(tempi.getBytes());
      
     // swiftParameterIn.setOgg(swiftInsertContainer);
 ////     params.add("ObjectStorageAgent");
     
//      hostTarget=this.getNameActiveHost();
//      logger.debug("prima dell'invocazione remota, nome host su cui invocare il metodo: "+hostTarget);
//      if(hostTarget==null){
//          throw new CleverException("error, non ci sono host con l'agente di switf attivo ");
//      }
////      params.clear();
//      params.add(swiftParameterIn);
      
   
      
 //     MethodInvoker creaContainer = new MethodInvoker("ObjectStorageAgent", "createContainer", true, params);

 
   //   inizio=System.currentTimeMillis();
   //   swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);
   //   fine=System.currentTimeMillis();
   //   intervallo=fine-inizio;
   //   tempi="creazione container: "+intervallo+" \n";
   //   tempiUpload.write(tempi.getBytes());

      
    //  logger.debug("container creato con successo?"+swiftParamOut.toString());
    //       statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
    //  logger.debug("stato: "+statusCode);
      //procedo con le operazioni di inserimento oggetto, riempio le strutture dati
  
      
      /**ok
      if (statusCode>299){
      
          if(statusCode==401){
              logger.debug("token non valido");
           //   token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
               token=this.requestToken();
              //token=(Token) this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
               swiftInsertContainer.setTokenId(token.getId());
               swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
               swiftInsertContainer.elaboraInfo();
               swiftParameterIn.setOgg(swiftInsertContainer);
               params.clear();
               params.add(swiftParameterIn);
               creaContainer = new MethodInvoker("ObjectStorageAgent", "createContainer", true, params);
            //   swiftParamOut= (SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createContainer", true, params);
             
               inizio=System.currentTimeMillis();
               
               swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="creazione container: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
               
               logger.debug("container creato con successo?"+swiftParamOut.toString());
               statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
               logger.debug("stato: "+statusCode);
      
               if(statusCode>299){
                  throw new CleverException("error, nella creazioen container, statusCode: "+statusCode);
               }

          }
          else{
         throw new CleverException("error, nella creazioen container, statusCode: "+statusCode);
          }
      }
      */
  ////    params.clear();
  //    swiftInsertContainer.setTenantNameSrc(elementToInsert.getTenantSrc());
  //    swiftInsertContainer.setUserNameSrc(elementToInsert.getUserSrc());
  //    params.add(swiftParameterIn);
      
  //    creaContainer = new MethodInvoker("ObjectStorageAgent", "allowWriteOnContainer", true, params);
  //    inizio=System.currentTimeMillis();
  //    swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);
  //    fine=System.currentTimeMillis();
  //    intervallo=fine-inizio;
  //    tempi="permessi scrittura: "+intervallo+" \n";
  //    tempiUpload.write(tempi.getBytes());
      
    //  logger.debug("permessi aggiunti con successo?"+swiftParamOut.toString());
    //       statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
    //  logger.debug("stato: "+statusCode);
  /*    
      if (statusCode>299){
      
          if(statusCode==401){
              logger.debug("token non valido");
           //   token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
               token=this.requestToken();
              //token=(Token) this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
               swiftInsertContainer.setTokenId(token.getId());
               swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
               swiftInsertContainer.elaboraInfo();
               swiftParameterIn.setOgg(swiftInsertContainer);
               params.clear();
               params.add(swiftParameterIn);
               creaContainer = new MethodInvoker("ObjectStorageAgent", "allowWriteOnContainer", true, params);
            //   swiftParamOut= (SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createContainer", true, params);
             
               inizio=System.currentTimeMillis();
               
               swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="permessi scrittura: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
               
               logger.debug("permessi aggiunti con successo?"+swiftParamOut.toString());
               statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
               logger.debug("stato: "+statusCode);
      
               if(statusCode>299){
                  throw new CleverException("error, nella creazioen container, statusCode: "+statusCode);
               }

          }
          else{
         throw new CleverException("error, nella creazioen container, statusCode: "+statusCode);
          }
      }        
              
       */  
      
    //  params.clear();
    //  md5Name=metadata.get("md5Name");
//      logger.debug("nome in md5 del file da inserire: "+md5Name);
//      swiftInsertObj.setContainerDestination(nameContainer);
//      swiftInsertObj.setContainerOrigin(nameContainer+"_tmp");
//      swiftInsertObj.setObjectDestination(md5Name);
//      swiftInsertObj.setObjectOrigin(md5Name);
  
 //     swiftInsertObj.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
  //qui md5Name
  //yy//    swiftInsertObj.setPathObject(pathObject);
   ////   logger.debug("metadati:"+metadata.toString());
//      swiftInsertObj.setMetadati(metadata);
//      swiftInsertObj.elaboraInfo();
//      swiftInsertObj.setAccount(nameContainer);
//      swiftInsertObj.setTokenId(elementToInsert.getToken());
//      swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertObject;
//      swiftParameterIn.setOgg(swiftInsertObj);
//      params.add(swiftParameterIn);
      
   ////   logger.debug("richiamo il metodo remoto per l'inserimento dell'oggetto");

//      MethodInvoker creaOggetto = new MethodInvoker("ObjectStorageAgent","copyObjectInterTenant" , true, params);
//      inizio=System.currentTimeMillis();
//      swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaOggetto, hostTarget);

 //     fine=System.currentTimeMillis();
 //     intervallo=fine-inizio;
 //     tempi="inserimento obj: "+intervallo+" \n";
 //     tempiUpload.write(tempi.getBytes());
      

 //   statusCode=Integer.valueOf(((InfoCopyObjectForMongoDb) swiftParamOut).getStatusCode());
 ////     logger.debug("codice risposta:"+statusCode);
      
      
      
 //      if (statusCode>299){
 //        throw new CleverException("error, nella create object, statusCode: "+statusCode);
 //     }
          if(true){}
      else{ 
           //cancella dal container tmp
     //docJSON=((InfoCopyObjectForMongoDb)swiftParamOut).infoToJsonMONGO();
      //params.clear();
/*      swiftInsertObj.setContainer(nameContainer+"_tmp");
      logger.debug("richiamo il metodo remoto per cancellare l'oggetto: "+md5Name);
      swiftInsertObj.setObject(md5Name);
      swiftInsertObj.setPathObject("");
      swiftInsertObj.elaboraInfo();
      //swiftInsertObj.setObject(md5Name+estensione);
      swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertObject;
      swiftParameterIn.setOgg(swiftInsertObj);
      params.add(swiftParameterIn);
 
      logger.debug("richiamo il metodo remoto per cancellare l'oggetto: "+md5Name);

      MethodInvoker delOggetto = new MethodInvoker("ObjectStorageAgent", "deleteObject", true, params);
      inizio=System.currentTimeMillis();
      swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(delOggetto, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="delete obj: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
      

//((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
     statusCode=Integer.valueOf(((InfoDeleteObjectForMongoDb) swiftParamOut).getStatusCode());
      
      logger.debug("codice risposta del:"+statusCode);
      
      
      
       if (statusCode>299){
      
          if(statusCode==401){
              logger.debug("token non valido");
//              token=(Token) dispatcherPlugin.dispatchToIntern(reqToken);
                //token=(Token) this.owner.invoke("IdentityServiceAgent","getInfo4interactonSWIFT",true,new ArrayList());
               token=this.requestToken();
               swiftInsertObj.setTokenId(token.getId());
               swiftInsertObj.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
               swiftInsertObj.elaboraInfo();
               swiftInsertObj.setObject(md5Name);
               params.clear();
               swiftParameterIn.setOgg(swiftInsertObj);
               params.add(swiftParameterIn);
               logger.debug("richiamo il metodo remoto per cancellare l'oggetto");
               delOggetto = new MethodInvoker("ObjectStorageAgent", "copyObject", true, params);
      
               
      inizio=System.currentTimeMillis();
               
               swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(delOggetto, hostTarget);

      fine=System.currentTimeMillis();
      intervallo=fine-inizio;
      tempi="delete obj: "+intervallo+" \n";
      tempiUpload.write(tempi.getBytes());
               
               
               
// swiftParamOut=(SwiftParameterOutput) ((CmAgent) this.owner).remoteInvocation(hostTarget, "ObjectStorageAgent", "createObjectMetadataMONGO", true, params);
               statusCode=Integer.valueOf(((InfoDeleteObjectForMongoDb) swiftParamOut).getStatusCode());
               logger.debug("codice risposta:"+statusCode);
      
               if(statusCode>299){
                  throw new CleverException("error, nella delete object, statusCode: "+statusCode);
               }

          }
          else{
         throw new CleverException("error, nella create object, statusCode: "+statusCode);
          }
      }*/
     // accountName=((InfoCreateObjectForMongoDb)swiftParamOut).getAccount();
              if(true){}
      else{

     // logger.debug("richiamo inserimento sul db nella collezione: "+nameContainer);
   
      
 //  inizio=System.currentTimeMillis();
 //     publish=this.createPublish(docJSON, metadata);
 //      fine=System.currentTimeMillis();
 //     intervallo=fine-inizio;
 //     tempi="creazione publish: "+intervallo+" \n";
 //     tempiUpload.write(tempi.getBytes());
      
    //  parametri.clear();
 //     parametri.add("obsDB");
 //     parametri.add(nameContainer);
 //     parametri.add(publish);
 //     parametri.add(md5Name);
 //     mi = new MethodInvoker("BigDataAgent","insertXMLString",false,parametri);
   
 //   inizio=System.currentTimeMillis();
 //   dispatcherPlugin.dispatchToIntern(mi);    
 //   fine=System.currentTimeMillis();
 //   intervallo=fine-inizio;
 //   tempi="inserimento publish: "+intervallo+" \n";
 //   tempiUpload.write(tempi.getBytes());
               

      logger.debug("inserimento terminato");
       }
      }
      }
       
       catch(Exception e){
           logger.error("error in insert In SWift",e);
           //inserisci nel logging quello che è successo
          // todo: fai restituire un'eccezione da gestire x far ripartire il processo
       }     
  }


public String copyInterTenant(String md5Name, String nameContainerDst, String nameContainerSrc, String tokenSrc,  String hostTarget, String accountSrc, String accountDst, HashMap <String, String> metadata) throws CleverException{
    
    InsertObject swiftInsertObj= new InsertObject();
    SwiftParameterInput swiftParameterIn= new SwiftParameterInput();
    ArrayList params=new ArrayList();
    MethodInvoker creaOggetto;
    String tempi,docJSON;
    long inizio = 0, fine = 0, intervallo;
    SwiftParameterOutput swiftParamOut;
    int statusCode;
      
    
    logger.debug("token: "+tokenSrc);
    logger.debug("contDst:"+nameContainerDst);
    logger.debug("contsrc: "+nameContainerSrc);
    logger.debug("accDst:"+accountDst);
    logger.debug("accSrc: "+accountSrc);
    logger.debug("tenantSrc: "+accountSrc);
    logger.debug("nome in md5 del file da inserire: "+md5Name);
    swiftInsertObj.setContainerDestination(nameContainerDst);
    swiftInsertObj.setContainerOrigin(nameContainerSrc);
    swiftInsertObj.setObjectDestination(md5Name);
    swiftInsertObj.setObjectOrigin(md5Name);
    swiftInsertObj.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    //swiftInsertObj.setPathObject(urlFinale);
    swiftInsertObj.setMetadati(metadata);
    swiftInsertObj.elaboraInfo();
    swiftInsertObj.setAccount("AUTH_"+accountDst);
    swiftInsertObj.setSourceAccount("AUTH_"+accountSrc);
    swiftInsertObj.setTokenId(tokenSrc);
    
    swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertObject;
    swiftParameterIn.setOgg(swiftInsertObj);
    params.add(swiftParameterIn);

    creaOggetto = new MethodInvoker("ObjectStorageAgent","copyObjectInterTenant" , true, params);
    inizio=System.currentTimeMillis();
    swiftParamOut=(SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaOggetto, hostTarget);
    fine=System.currentTimeMillis();
    intervallo=fine-inizio;
    tempi="inserimento obj: "+intervallo+" \n";
        try {
            tempiUpload.write(tempi.getBytes());
        } catch (IOException ex) {
            logger.error("impossibile scrivere nel file");
        }
    statusCode=Integer.valueOf(((InfoCopyObjectForMongoDb) swiftParamOut).getStatusCode());

    if (statusCode>299){
         throw new CleverException("error, nella create object, statusCode: "+statusCode);
      }
    docJSON=((InfoCopyObjectForMongoDb)swiftParamOut).infoToJsonMONGO();
    return docJSON;
    
}

public int allowWrite(String nameContainer,String hostTarget,String tenantSrc,String userSrc) throws CleverException{
    
    SwiftParameterInput swiftParameterIn= new SwiftParameterInput();
    InsertContainer swiftInsertContainer= new InsertContainer();
    ArrayList params=new ArrayList();
    MethodInvoker mi;
    String tempi;
    long inizio = 0, fine = 0, intervallo;
    SwiftParameterOutput swiftParamOut;
    int statusCode;
    
    
    swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertContainer;
    swiftInsertContainer.setContainer(nameContainer);
    swiftInsertContainer.setTokenId(token.getId());
    swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    swiftInsertContainer.elaboraInfo();
    swiftParameterIn.setOgg(swiftInsertContainer);
    swiftInsertContainer.setTenantNameSrc(tenantSrc);
    swiftInsertContainer.setUserNameSrc(userSrc);
    params.add(swiftParameterIn);
    
    mi = new MethodInvoker("ObjectStorageAgent", "allowWriteOnContainer", true, params);
    inizio=System.currentTimeMillis();
    swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(mi, hostTarget);
    fine=System.currentTimeMillis();
    intervallo=fine-inizio;
    tempi="permessi scrittura: "+intervallo+" \n";
        try {
            tempiUpload.write(tempi.getBytes());
        } catch (IOException ex) {
            logger.error("impossibile scrivere i tempi");
        }
    logger.debug("permessi aggiunti con successo?"+swiftParamOut.toString());
    statusCode=Integer.valueOf(((InfoOperationContainerMetadataForMongoDb) swiftParamOut).getStatusCode());
    logger.debug("stato: "+statusCode);
    
    return statusCode;
}

public int createContainer(String nameContainer,String hostTarget) throws CleverException{

    SwiftParameterInput swiftParameterIn= new SwiftParameterInput();
    InsertContainer swiftInsertContainer= new InsertContainer();
    ArrayList params=new ArrayList();
    String tempi;
    long inizio = 0, fine = 0, intervallo;
    SwiftParameterOutput swiftParamOut;
    int statusCode;
    
    logger.debug("container: "+nameContainer);
    logger.debug("urlPurl: "+token.getPublicUrlSwift());
    swiftParameterIn.type=SwiftParameterInput.tipoObjectInput.InsertContainer;
    swiftInsertContainer.setContainer(nameContainer);
    swiftInsertContainer.setTokenId(token.getId());
    swiftInsertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    swiftInsertContainer.elaboraInfo();
    swiftParameterIn.setOgg(swiftInsertContainer);
    params.add(swiftParameterIn);
    
    MethodInvoker creaContainer = new MethodInvoker("ObjectStorageAgent", "createContainer", true, params);

    inizio=System.currentTimeMillis();
    swiftParamOut= (SwiftParameterOutput) dispatcherPlugin.dispatchToExtern(creaContainer, hostTarget);
    fine=System.currentTimeMillis();
    intervallo=fine-inizio;
    tempi="creazione container: "+intervallo+" \n";
        try {
            tempiUpload.write(tempi.getBytes());
        } catch (IOException ex) {
            logger.error("errore nella scrittura tempi");
        }
    logger.debug("container creato con successo?"+swiftParamOut.toString());
    statusCode=Integer.valueOf(((InfoContainerForMongoDb) swiftParamOut).getStatusCode());
    logger.debug("stato: "+statusCode);

    return statusCode;
}


}
