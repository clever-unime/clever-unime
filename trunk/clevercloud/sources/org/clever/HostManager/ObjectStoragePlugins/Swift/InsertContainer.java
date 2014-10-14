/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class InsertContainer extends SwiftParameterInput {
   
//########################    
//Attributi della classe
//########################
    
private String base; // es: http://172.17.2.26:8080/v1/
private String account; // es: AUTH_2587b80868a74859955e17d3a7360b27
private String container; // es: myfiles
private String operazione;  

private String tokenId; //token che verrà usato nelle connessioni http

//############
private String X_Container_Meta;
private String name;
//############


private String UrlSwiftPresoDalToken;
    
//##############
//Costruttori
//##############

/**
 * Costruttore di default
 */
public InsertContainer() {
    this.base = "";
    this.account = "";
    this.container = "";
    this.UrlSwiftPresoDalToken = "";
    this.X_Container_Meta = "";
    this.name = "";
    this.operazione = "";
    this.tokenId = "";
    this.type = tipoObjectInput.InsertContainer;
    
}
 

/**
 * Metodo utile in fase di debug.
 */
public void debug(){
        
    System.out.println("##############################");
    System.out.println("########   DEBUG   ###########");
    System.out.println("Operazione: "+this.getOperazione());
    System.out.println("Account: "+this.getAccount());
    System.out.println("Container : "+this.getContainer());
    System.out.println("Base : "+this.getBase());
    System.out.println("urlSwiftPresoDalToken : "+this.getUrlSwiftPresoDalToken());
    System.out.println("-----------------------------");
    System.out.println("X_Container_Meta : "+this.getX_Container_Meta());
    System.out.println("name : "+this.getName());
    System.out.println("-----------------------------");
     System.out.println("Token  : "+this.getTokenId());
    System.out.println("##############################");
        
    }//debug
    
/**
 * Questo metodo consente di ricavare, a partire dalle informazioni di:
 * 
 * publicUrl di Swift ricavato dal json restituito dalla richiesta di autenticazione fatta a keystone
 * 
 * le seguenti informazioni:
 * 
 * accountName
 * urlBase
 * 
 */
public void elaboraInfo(){
    
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

    public String getUrlSwiftPresoDalToken() {
        return UrlSwiftPresoDalToken;
    }

    public void setUrlSwiftPresoDalToken(String UrlSwiftPresoDalToken) {
        this.UrlSwiftPresoDalToken = UrlSwiftPresoDalToken;
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

    public String getOperazione() {
        return operazione;
    }

    public void setOperazione(String operazione) {
        this.operazione = operazione;
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
    





    
}
