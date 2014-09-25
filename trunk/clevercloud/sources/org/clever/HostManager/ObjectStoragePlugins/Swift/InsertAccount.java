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
public class InsertAccount extends SwiftParameterInput{
  
    
//########################    
//Attributi della classe
//########################
    
private String base; // es: http://172.17.2.26:8080/v1/
private String account; // es: AUTH_2587b80868a74859955e17d3a7360b27
private String X_Account_Meta; // es: Rischio
private String name;   // es: Idrogeologico
private String operazione; //{create|update|delete metadata }

private String tokenId; //token che verrà usato nelle connessioni http

private String UrlSwiftPresoDalToken; // es: http://172.17.2.26:8080/v1/AUTH_2587b80868a74859955e17d3a7360b27
    
//##############
//Costruttori
//##############



/**
 * Costruttore di default.
 */
public InsertAccount() {
    
    this.base = "";
    this.account = "";
    this.X_Account_Meta = "";
    this.name = "";
    this.UrlSwiftPresoDalToken = "";
    this.type = tipoObjectInput.InsertAccount;
    this.tokenId = "";
    
}
    

/**
 * Metodo utile in fase di debug.
 */
public void debug(){
        
    System.out.println("#############################################");
    System.out.println("########   DEBUG: InsertAccount   ###########");
    System.out.println("Account: "+this.getAccount());
    System.out.println("Base : "+this.getBase());
    System.out.println("urlSwiftPresoDalToken : "+this.getUrlSwiftPresoDalToken());
    System.out.println("Operazione: "+this.getOperazione());
    System.out.println("X_Account_Meta : "+this.getX_Account_Meta());
    System.out.println("name : "+this.getName());
    System.out.println("Token  : "+this.getTokenId());
    System.out.println("#############################################");
        
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

    public String getX_Account_Meta() {
        return X_Account_Meta;
    }

    public void setX_Account_Meta(String X_Account_Meta) {
        this.X_Account_Meta = X_Account_Meta;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlSwiftPresoDalToken() {
        return UrlSwiftPresoDalToken;
    }

    public void setUrlSwiftPresoDalToken(String UrlSwiftPresoDalToken) {
        this.UrlSwiftPresoDalToken = UrlSwiftPresoDalToken;
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
