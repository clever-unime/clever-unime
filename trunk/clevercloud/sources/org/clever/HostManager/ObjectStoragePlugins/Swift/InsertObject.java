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
/*
 * Questo metodo serve a gestire le informazioni da passare al metodo
 * createObject() della classe Swift.
 */

package org.clever.HostManager.ObjectStoragePlugins.Swift;

import java.io.File;


public class InsertObject extends SwiftParameterInput{
    
//########################    
//Attributi della classe
//########################
    
private String base; // es: http://172.17.2.26:8080/v1/
private String account; // es: AUTH_2587b80868a74859955e17d3a7360b27
private String container; // es: myfiles
private String object; // es: riccardo.avi
private Long objectLength; // es: 464206956
private String pathObject; // path del file system es: /home/riccardo/Desktop/riccardo.avi
private String operazione; // {create| replace| copy | delete | get content}

private String urlSwiftPresoDalToken;
private String urlMigration;
private String tokenId; //token che verrà usato nelle connessioni http

//##################################
// copy object from one containet to another
private String containerDestination;
private String objectDestination;
private String containerOrigin;
private String objectOrigin;
//##################################

private String X_Container_Meta;
private String name;


//##############   
//Costruttori   
//##############

 
/**
 * Costruttore di default.
 */
public InsertObject() {
        this.base = "";
        this.account = "";
        this.container = "";
        this.object = "";
        this.objectLength = null;
        this.pathObject = ""; 
        this.urlSwiftPresoDalToken = "";
        this.operazione = "";
        this.X_Container_Meta = "";
        this.name = "";
        this.type = tipoObjectInput.InsertObject;
        this.tokenId = "";
        this.urlMigration="";
       
}
 
/**
 * Metodo utile in fase di debug.
 */
public void debug(){
        
    System.out.println("#############################################");
    System.out.println("########   DEBUG  InsertObject()  ###########");
    System.out.println("Operazione: "+this.getOperazione());
    System.out.println("Account: "+this.getAccount());
    System.out.println("Container : "+this.getContainer());
    System.out.println("Object : "+this.getObject());
    System.out.println("Base : "+this.getBase());
    System.out.println("ObjectLength : "+this.getObjectLength());
    System.out.println("pathObject : "+this.getPathObject());
    System.out.println("urlSwiftPresoDalToken : "+this.getUrlSwiftPresoDalToken());
    System.out.println("Token  : "+this.getTokenId());
    System.out.println("##############################################");
        
    }//debug


/**
 * Metodo utile in fase di debug.
 */
public void debugForCopy(){
        
    System.out.println("#############################################");
    System.out.println("########   DEBUG  InsertObject()  ###########");
    System.out.println("Account: "+this.getAccount());
    System.out.println("Base : "+this.getBase());
    System.out.println("urlSwiftPresoDalToken : "+this.getUrlSwiftPresoDalToken());
    System.out.println("Operazione: "+this.getOperazione());
    System.out.println("containerOrigin : "+this.getContainerOrigin());
    System.out.println("objectOrigin : "+this.getObjectOrigin());
    System.out.println("containerDestination : "+this.getContainerDestination());
    System.out.println("objectDestination : "+this.getObjectDestination());
    System.out.println("Token  : "+this.getTokenId());
    System.out.println("##############################################");
        
    }//debug


/**
 * Metodo utile in fase di debug.
 */
public void debugForDownload(){
        
    System.out.println("#############################################");
    System.out.println("########   DEBUG  InsertObject()  ###########");
    System.out.println("Account: "+this.getAccount());
    System.out.println("Container : "+this.getContainer());
    System.out.println("Base : "+this.getBase());
    System.out.println("urlSwiftPresoDalToken : "+this.getUrlSwiftPresoDalToken());
    System.out.println("Operazione: "+this.getOperazione());
    System.out.println("Path dove salvare il file : "+this.getPathObject());
    System.out.println("Token  : "+this.getTokenId());
    System.out.println("##############################################");
        
    }//debug



/**
 * Metodo utile in fase di debug.
 */
public void debugMetadata(){
        
    System.out.println("#############################################");
    System.out.println("########   DEBUG  InsertObject()  ###########");
    System.out.println("Operazione: "+this.getOperazione());
    System.out.println("Account: "+this.getAccount());
    System.out.println("Container : "+this.getContainer());
    System.out.println("Object : "+this.getObject());
    System.out.println("X_Container_Meta : "+this.getX_Container_Meta());
    System.out.println("name : "+this.getName());
    System.out.println("Base : "+this.getBase());
    System.out.println("urlSwiftPresoDalToken : "+this.getUrlSwiftPresoDalToken());
    System.out.println("Token  : "+this.getTokenId());
    System.out.println("##############################################");
        
    }//debug

    
/**
 * Questo metodo consente di ricavare, a partire dalle informazioni di:
 * 
 * Nome Container dove posizionare il file object
 * Path del file dove è situato il file object da salvare su Swift
 * publicUrl di Swift ricavato dal json restituito dalla richiesta di autenticazione fatta a keystone
 * 
 * 
 * le seguenti informazioni:
 * 
 * acctName
 * file.length()
 * 
 */
public void elaboraInfo(){
   
   if(this.getUrlSwiftPresoDalToken()!=""){ 
    
  //#### Ricavo l'account
  //#inizio
  int index = this.getUrlSwiftPresoDalToken().lastIndexOf("/");
  String accountName = this.getUrlSwiftPresoDalToken().substring(index + 1);
  this.setAccount(accountName);
  //debug
  //System.out.println("accountName: "+accountName);
  //#fine 
  
   //#### Ricavo l'urlBase
  //#inizio
  String urlBase = this.getUrlSwiftPresoDalToken().replace(accountName,"");
  this.setBase(urlBase);
  //debug
  //System.out.println("url di base: "+urlBase);
  //#fine 
   }
  
  if(this.getPathObject()!=""){
  
  //#### Ricavo il nome dell'object
  //#inizio
  String comodo2 =this.getPathObject();
  int index2 = comodo2.lastIndexOf("/");
  String objectName = comodo2.substring(index2 + 1);
  this.setObject(objectName);
  //debug
  //System.out.println("objectName : "+objectName);
  //#fine 
  
  //#### Ricavo il il content length del file
  //#inizio
  File file = new File(this.getPathObject());
  this.setObjectLength(file.length());
  //debug
  //System.out.println("La memoria occupata dal file è: "+file.length());
  } 
    
}//elaboraInfoDaUrlSwiftPresoDalToken

public void elaboraInfoFromUrlComplete(){
   
   if(this.getUrlMigration()!=""){ 
    
  //#### Ricavo l'oggetto
  //#inizio
  int index = this.getUrlMigration().lastIndexOf("/");
  String objectName = this.getUrlMigration().substring(index + 1);
  this.setObject(objectName);
  //debug
  //#fine 
  
  //#### Ricavo il container
  //#inizio
  String tmp=this.getUrlMigration().replace("/"+objectName, "");
  index = tmp.lastIndexOf("/");
  String containerName = tmp.substring(index + 1);
  this.setContainer(containerName);
  //debug
  //#fine 
  
  //#### Ricavo l'account
  //#inizio
  tmp=tmp.replace("/"+containerName, "");
  index = tmp.lastIndexOf("/");
  String accountName = tmp.substring(index + 1);
  this.setAccount(accountName);
  //debug
  //#fine
  
   //#### Ricavo l'urlBase
  //#inizio
  String urlBase = tmp.replace(accountName,"");
  this.setBase(urlBase);
  //debug
  //#fine 
   }
  
  if(this.getPathObject()!=""){
  
  //#### Ricavo il nome dell'object
  //#inizio
  String comodo2 =this.getPathObject();
  int index2 = comodo2.lastIndexOf("/");
  String objectName = comodo2.substring(index2 + 1);
  this.setObject(objectName);
  //debug
  //System.out.println("objectName : "+objectName);
  //#fine 
  
  //#### Ricavo il il content length del file
  //#inizio
  File file = new File(this.getPathObject());
  this.setObjectLength(file.length());
  //debug
  //System.out.println("La memoria occupata dal file è: "+file.length());
  } 
    
}//elaboraInfoDaUrlSwiftPresoDalToken

    
//########################
//Metodi Setter e Getter
//########################


    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

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

    public Long getObjectLength() {
        return objectLength;
    }

    public void setObjectLength(Long objectLength) {
        this.objectLength = objectLength;
    }

    public String getPathObject() {
        return pathObject;
    }

    public void setPathObject(String pathObject) {
        this.pathObject = pathObject;
    }

    public String getUrlSwiftPresoDalToken() {
        return urlSwiftPresoDalToken;
    }

    public void setUrlSwiftPresoDalToken(String urlSwiftPresoDalToken) {
        this.urlSwiftPresoDalToken = urlSwiftPresoDalToken;
    }

    public String getOperazione() {
        return operazione;
    }

    public void setOperazione(String operazione) {
        this.operazione = operazione;
    }

    public String getContainerDestination() {
        return containerDestination;
    }

    public void setContainerDestination(String containerDestination) {
        this.containerDestination = containerDestination;
    }

    public String getObjectDestination() {
        return objectDestination;
    }

    public void setObjectDestination(String objectDestination) {
        this.objectDestination = objectDestination;
    }

    public String getContainerOrigin() {
        return containerOrigin;
    }

    public void setContainerOrigin(String containerOrigin) {
        this.containerOrigin = containerOrigin;
    }

    public String getObjectOrigin() {
        return objectOrigin;
    }

    public void setObjectOrigin(String objectOrigin) {
        this.objectOrigin = objectOrigin;
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

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public tipoObjectInput getType() {
        return type;
    }

    public void setType(tipoObjectInput type) {
        this.type = type;
    }

    public String getUrlMigration() {
        return urlMigration;
    }

    public void setUrlMigration(String urlMigration) {
        this.urlMigration = urlMigration;
    }

    
    
    
    
   
    
}//class
