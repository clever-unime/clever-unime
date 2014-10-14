

package org.clever.administration.api.modules;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.clever.ClusterManager.IdentityServicePlugins.Keystone.Token;
import org.clever.Common.Exceptions.CleverException;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoContainerForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoCopyObjectForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoCreateObjectForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoCreateObjectMetadataForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoDeleteObjectForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoGetObjectForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoListAccountDetailsForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoListContainerForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoListObjectMetadataForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoOperationAccountForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InfoOperationContainerMetadataForMongoDb;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InsertAccount;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InsertContainer;
import org.clever.HostManager.ObjectStoragePlugins.Swift.InsertObject;
import org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterInput;
import org.clever.HostManager.ObjectStoragePlugins.Swift.SwiftParameterOutput;
import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.annotations.ShellParameter;
import org.clever.administration.api.Session;

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
public class ObjectStorageSwiftModule extends AdministrationModule{ 
    
Logger logger=Logger.getLogger("SwiftObjectStorageModule");


public ObjectStorageSwiftModule(Session s) {
      super(s);
}
    
/**
 * Description: shows details for a specified account and lists containers, sorted by name, in the account.
 * 
 * 
 * Show account details and list containers, and ask for a JSON response:
 * curl -i $publicURL?format=json -X GET -H "X-Auth-Token: $token" 
 * 
 * @param hostManager
 * @param user
 * @param pass
 * @param tenant
 * @return 
 * @throws CleverException 
 */
@ShellCommand
public String showAccountDetailsAndListContainers(@ShellParameter(name="riccardo", comment="hostManager") String hostManager,
                                    //            @ShellParameter(name="authUrlKeystone", comment="authUrlKeystone") String authUrlKeystone,
                                                  @ShellParameter(name="user", comment="user") String user,
                                                  @ShellParameter(name="pass", comment="pass") String pass,
                                                  @ShellParameter(name="tenant", comment="tenant") String tenant
                                                  ) 
                                                  throws CleverException{
        
    
  //#####################################
  // ricavo token e publicUrlSwift
  //#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per showAccountDetailsAndListContainers():", ex);
        System.out.println("Errore nell'operazione di richiesta del token per showAccountDetailsAndListContainers():" + ex);
        return null;
    }
               
    
    //#####################################
    // showAccountDetailsAndListContainers
    //#####################################
    
    //creo un oggetto di inserimento figlio
    InsertAccount insertAccount = new InsertAccount();
    //gli passo i parametri
    insertAccount.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertAccount.setTokenId(token.getId());
    
    //ricavo altri parametri
    insertAccount.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertAccount; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertAccount; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    //pulisco la lista di parametri
    //params.clear();
    ArrayList params2 = new ArrayList();
    
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "showAccountDetailsAndListContainers", params2, false);
    } catch (CleverException ex) {
       // System.out.println("Errore nel comando updateAccountMetadata(): " + ex);
        logger.error("Errore nel comando createAccountMetadata(): ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoListAccountDetailsForMongoDb risp = new InfoListAccountDetailsForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoListAccountDetailsForMongoDb) swiftParameterOutput;
              
    stringa = risp.getResponse();
    System.out.println(stringa);
   
      
    return stringa; 
        
    }    

   
/**
 * Description: creates account metadata.
 * 
 * Create account metadata:
 * curl -i $publicURL -X POST -H "X-Auth-Token: $token" 
 *                                           -H "X-Account-Meta-Book: MobyDick" 
 *                                           
 * @param hostManager
 * @param user
 * @param pass
 * @param tenant
 * @param X_Account_Meta
 * @param name
 * @return 
 * @throws CleverException 
 */
    @ShellCommand
    public String createAccountMetadata(
                                      @ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                                     // @ShellParameter(name="authUrlKeystone", comment="authUrlKeystone") String authUrlKeystone,
                                      @ShellParameter(name="user", comment="user") String user,
                                      @ShellParameter(name="pass", comment="pass") String pass,
                                      @ShellParameter(name="tenant", comment="tenant") String tenant,
                                      @ShellParameter(name="X_Account_Meta", comment="Account-Meta-Name") String X_Account_Meta,
                                      @ShellParameter(name="name", comment="Account-Meta-Data name")String  name
                                      ) throws CleverException{
        
       
  //#####################################
  // ricavo token e publicUrlSwift
  //#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per createAccountMetadata():", ex);
        System.out.println("Errore nell'operazione di richiesta del token per createAccountMetadata():" + ex);
        return null;
    }
               
    
    //#####################################
    // createAccountMetadata
    //#####################################
    
           
    
    //creo un oggetto di inserimento figlio
    InsertAccount insertAccount = new InsertAccount();
    //gli passo i parametri
    insertAccount.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertAccount.setTokenId(token.getId());
    insertAccount.setX_Account_Meta(X_Account_Meta);
    insertAccount.setName(name);
    //ricavo altri parametri
    insertAccount.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertAccount; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertAccount; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    //pulisco la lista di parametri
    //params.clear();
    ArrayList params2 = new ArrayList();
    
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "createAccountMetadata", params2, false);
    } catch (CleverException ex) {
       // System.out.println("Errore nel comando updateAccountMetadata(): " + ex);
        logger.error("Errore nel comando createAccountMetadata(): ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoOperationAccountForMongoDb risp = new InfoOperationAccountForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoOperationAccountForMongoDb) swiftParameterOutput;
              
                            
    //204
    if(risp.getStatusCode().equals("204")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
    
    return stringa;
    
    }//createAccountMetadata

    
  
/**
 * Description: update account metadata.
 * 
 * Update account metadata:
 * curl -i $publicURL -X POST -H "X-Auth-Token: $token" 
 *                               -H "X-Account-Meta-Subject: AmericanLiterature"
 * @param hostManager
 * @param user
 * @param pass
 * @param tenant
 * @param X_Account_Meta
 * @param name
 * @return
 * @throws CleverException 
 */    
@ShellCommand
public String updateAccountMetadata( @ShellParameter(name = "riccardo", comment = "hostMAnager") String hostManager,
                                //     @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                     @ShellParameter(name = "user", comment = "user") String user,
                                     @ShellParameter(name = "pass", comment = "pass") String pass,
                                     @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                     @ShellParameter(name = "X_Account_Meta", comment = "Account-Meta-Name") String X_Account_Meta,
                                     @ShellParameter(name = "name", comment = "Account-Meta-Data name1") String name
    ) throws CleverException {

  //#####################################
  // ricavo token e publicUrlSwift
  //#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per updateAccountMetadata():", ex);
        System.out.println("Errore nell'operazione di richiesta del token per updateAccountMetadata():" + ex);
        return null;
    }
               
    
    //#####################################
    // updateAccountMetadata
    //#####################################
    
           
    
    //creo un oggetto di inserimento figlio
    InsertAccount insertAccount = new InsertAccount();
    //gli passo i parametri
    insertAccount.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertAccount.setTokenId(token.getId());
    insertAccount.setX_Account_Meta(X_Account_Meta);
    insertAccount.setName(name);
    //ricavo altri parametri
    insertAccount.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertAccount; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertAccount; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
    
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "updateAccountMetadata", params2, false);
    } catch (CleverException ex) {
       // System.out.println("Errore nel comando updateAccountMetadata(): " + ex);
        logger.error("Errore nel comando updateAccountMetadata(): ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoOperationAccountForMongoDb risp = new InfoOperationAccountForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoOperationAccountForMongoDb) swiftParameterOutput;
              
                            
    //204
    if(risp.getStatusCode().equals("204")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
    
    return stringa;
    
    }//updateAccountMetadata
    


/**
 * Description: deletes account metadata.
 * 
 * Deletes account metadata:
 * curl -i $publicURL -X POST -H "X-Auth-Token: $token" 
 *                                       -H "X-Remove-Account-Meta-Subject: x"
 * @param hostManager
 * @param user
 * @param pass
 * @param X_Account_Meta
 * @param tenant
 * @param name
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String deleteAccountMetadata(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                                //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                    @ShellParameter(name = "user", comment = "user") String user,
                                    @ShellParameter(name = "pass", comment = "pass") String pass,
                                    @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                    @ShellParameter(name="X_Account_Meta", comment="Account-Meta-Name") String X_Account_Meta,
                                    @ShellParameter(name="name", comment="Account-Meta-Data name")String  name
                                    )throws CleverException{

     
        
   //#####################################
  // ricavo token e publicUrlSwift
  //#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per deleteAccountMetadata():", ex);
        System.out.println("Errore nell'operazione di richiesta del token per deleteAccountMetadata():" + ex);
        return null;
    }
               
    
    //#####################################
    // deleteAccountMetadata
    //#####################################
    
           
    
    //creo un oggetto di inserimento figlio
    InsertAccount insertAccount = new InsertAccount();
    //gli passo i parametri
    insertAccount.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertAccount.setTokenId(token.getId());
    insertAccount.setX_Account_Meta(X_Account_Meta);
    insertAccount.setName(name);
    //ricavo altri parametri
    insertAccount.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertAccount; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertAccount; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
    
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "deleteAccountMetadata", params2, false);
    } catch (CleverException ex) {
       // System.out.println("Errore nel comando updateAccountMetadata(): " + ex);
        logger.error("Errore nel comando updateAccountMetadata(): ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoOperationAccountForMongoDb risp = new InfoOperationAccountForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoOperationAccountForMongoDb) swiftParameterOutput;
             
            
    //204
    if(risp.getStatusCode().equals("204")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
    
    return stringa;    
 
}//deleteAccountMetadata

//#############################################################################



/**
 * Description: creates a container with no metadata.
 * 
 * Create a container with no metadata: 
 * curl -i $publicURL/container -X PUT -H "Content-Length: 0" -H "X-Auth-Token: $token"
 * 
 * @param hostManager
 * @param user
 * @param pass
 * @param tenant
 * @param container
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String createContainer(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                                //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                    @ShellParameter(name = "user", comment = "user") String user,
                                    @ShellParameter(name = "pass", comment = "pass") String pass,
                                    @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                    @ShellParameter(name="container", comment="container") String container
                              )throws CleverException{

  
  //#####################################
  // ricavo token e publicUrlSwift
  //#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per createContainer():", ex);
        System.out.println("Errore nell'operazione di richiesta del token per createContainer():" + ex);
        return null;
    }
               
    
    //#####################################
    // createContainer
    //#####################################
    
           
    
    //creo un oggetto di inserimento figlio
    InsertContainer insertContainer = new InsertContainer();
    //gli passo i parametri
    insertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertContainer.setTokenId(token.getId());
    insertContainer.setContainer(container);
    
    //ricavo altri parametri
    insertContainer.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertContainer; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertContainer; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "createContainer", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando createContainer(): " + ex);
        logger.error("Errore nel comando createContainer(): ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoContainerForMongoDb risp = new InfoContainerForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoContainerForMongoDb) swiftParameterOutput;
              
                            
    //201
    if(risp.getStatusCode().equals("201")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
     
    }
    
    if(risp.getStatusCode().equals("202")){
        
        stringa = risp.getStatusCode() + ": il container già esiste!!!";
     }
    
     if(risp.getStatusCode().equals("")){
        
        stringa = risp.getStatusCode() + "l'operazione non è andata a buon fine!!!";
     
    }
    
    System.out.println(stringa);
    
    return stringa;
    
 }//createContainer


/**
 * Description: deletes an empty container.
 * 
 * Deletes an empty container:
 * curl -i $publicURL/container -X DELETE -H "X-Auth-Token: $token"
 * 
 * @param hostManager
 * @param user
 * @param pass
 * @param tenant
 * @param container
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String deleteContainer(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                                //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                    @ShellParameter(name = "user", comment = "user") String user,
                                    @ShellParameter(name = "pass", comment = "pass") String pass,
                                    @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                    @ShellParameter(name="container", comment="container") String container
                                    )throws CleverException{
   
  
  //#####################################
  // ricavo token e publicUrlSwift
  //#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per deleteContainer():", ex);
        System.out.println("Errore nell'operazione di richiesta del token per deleteContainer():" + ex);
        return null;
    }
               
    
    //#####################################
    // deleteContainer
    //#####################################
    
           
    
    //creo un oggetto di inserimento figlio
    InsertContainer insertContainer = new InsertContainer();
    //gli passo i parametri
    insertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertContainer.setTokenId(token.getId());
    insertContainer.setContainer(container);
    
    //ricavo altri parametri
    insertContainer.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertContainer; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertContainer; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "deleteContainer", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando deleteContainer(): " + ex);
        logger.error("Errore nel comando deleteContainer(): ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoContainerForMongoDb risp = new InfoContainerForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoContainerForMongoDb) swiftParameterOutput;
              
                            
    //204
    if(risp.getStatusCode().equals("204")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
    
    return stringa;
    
}//deleteContainer

//#############################################################################

/**
 * 
 * Description: create container with metadata.
 * 
 * Create container with metadata:
 * curl -i $publicURL/marktwain -X POST -H "X-Auth-Token: $token" 
 *                              -H "X-Container-Meta-Author: MarkTwain" 
 *                              -H "X-Container-Meta-Century: Nineteenth"
 * 
 * @param hostManager
 * @param user
 * @param pass
 * @param tenant
 * @param container
 * @param X_Container_Meta
 * @param name
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String createContainerMetadata(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                                   //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                      @ShellParameter(name = "user", comment = "user") String user,
                                      @ShellParameter(name = "pass", comment = "pass") String pass,
                                      @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                      @ShellParameter(name="container", comment="container") String container,
                                      @ShellParameter(name="X_Container_Meta", comment="X_Container_Meta") String X_Container_Meta,
                                      @ShellParameter(name="name", comment="X_Container_Meta name")String  name
                                      )throws CleverException{


//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per createContainerMetadata():", ex);
        System.out.println("Errore nell'operazione di richiesta del token per createContainerMetadata():" + ex);
        return null;
    }
               
    
    //#####################################
    // updateContainerMetadata
    //#####################################
    
           
    
    //creo un oggetto di inserimento figlio
    InsertContainer insertContainer = new InsertContainer();
    //gli passo i parametri
    insertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertContainer.setTokenId(token.getId());
    insertContainer.setContainer(container);
    insertContainer.setX_Container_Meta(X_Container_Meta);
    insertContainer.setName(name);
    
    //ricavo altri parametri
    insertContainer.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertContainer; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertContainer; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "createContainerMetadata", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando createContainerMetadata(): " + ex);
        logger.error("Errore nel comando createContainerMetadata(): ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoOperationContainerMetadataForMongoDb risp = new InfoOperationContainerMetadataForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoOperationContainerMetadataForMongoDb) swiftParameterOutput;
              
                            
   //204
    if(risp.getStatusCode().equals("204")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
             
    return stringa;    
    
}//createContainerMetadata




/**
 * 
 * Description: update container metadata.
 * 
 * Update container metadata:
 * curl -i $publicURL/marktwain -X POST -H "X-Auth-Token: $token" 
 *                                      -H "X-Container-Meta-Author: SamuelClemens"
 * 
 * @param hostManager
 * @param user
 * @param container
 * @param pass
 * @param X_Container_Meta
 * @param tenant
 * @param name
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String updateContainerMetadata(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                                  //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                      @ShellParameter(name = "user", comment = "user") String user,
                                      @ShellParameter(name = "pass", comment = "pass") String pass,
                                      @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                      @ShellParameter(name="container", comment="container") String container,
                                      @ShellParameter(name="X_Container_Meta", comment="X_Container_Meta") String X_Container_Meta,
                                      @ShellParameter(name="name", comment="X_Container_Meta name")String  name
                                      )throws CleverException{
    

//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per updateContainerMetadata():", ex);
        System.out.println("Errore nell'operazione di richiesta del token per updateContainerMetadata():" + ex);
        return null;
    }
               
    
    //#####################################
    // updateContainerMetadata
    //#####################################
    
           
    
    //creo un oggetto di inserimento figlio
    InsertContainer insertContainer = new InsertContainer();
    //gli passo i parametri
    insertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertContainer.setTokenId(token.getId());
    insertContainer.setContainer(container);
    insertContainer.setX_Container_Meta(X_Container_Meta);
    insertContainer.setName(name);
    
    //ricavo altri parametri
    insertContainer.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertContainer; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertContainer; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "updateContainerMetadata", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando updateContainerMetadata(): " + ex);
        logger.error("Errore nel comando updateContainerMetadata(): ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoOperationContainerMetadataForMongoDb risp = new InfoOperationContainerMetadataForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoOperationContainerMetadataForMongoDb) swiftParameterOutput;
              
                            
    //204
    if(risp.getStatusCode().equals("204")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
             
    return stringa;
  
     
}//updateContainerMetadata



/**
 * 
 * Description: delete container metadata.
 * 
 * Delete container metadata:
 * curl -i $publicURL/marktwain -X POST -H "X-Auth-Token: $token" 
 *                                      -H "X-Remove-Container-Meta-Century: x"
 * 
 * @param hostManager
 * @param user
 * @param pass
 * @param tenant
 * @param container
 * @param X_Container_Meta
 * @param name
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String deleteContainerMetadata(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                                  //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                      @ShellParameter(name = "user", comment = "user") String user,
                                      @ShellParameter(name = "pass", comment = "pass") String pass,
                                      @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                      @ShellParameter(name="container", comment="container") String container,
                                      @ShellParameter(name="X_Container_Meta", comment="X_Container_Meta") String X_Container_Meta,
                                      @ShellParameter(name="name", comment="X_Container_Meta name")String  name
                                      )throws CleverException{
 
    
//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per deleteContainerMetadata():", ex);
        System.out.println("Errore nell'operazione di richiesta del token per deleteContainerMetadata():" + ex);
        return null;
    }
               
    
    //#####################################
    // deleteContainerMetadata
    //#####################################
    
           
    
    //creo un oggetto di inserimento figlio
    InsertContainer insertContainer = new InsertContainer();
    //gli passo i parametri
    insertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertContainer.setTokenId(token.getId());
    insertContainer.setContainer(container);
    insertContainer.setX_Container_Meta(X_Container_Meta);
    insertContainer.setName(name);
    
    //ricavo altri parametri
    insertContainer.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertContainer; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertContainer; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    //pulisco la lista di parametri
    //params.clear();
    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "deleteContainerMetadata", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando deleteContainerMetadata(): " + ex);
        logger.error("Errore nel comando deleteContainerMetadata(): ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoOperationContainerMetadataForMongoDb risp = new InfoOperationContainerMetadataForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoOperationContainerMetadataForMongoDb) swiftParameterOutput;
              
                            
    //204
    if(risp.getStatusCode().equals("204")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
             
    return stringa; 
  
}//deleteContainerMetadata


/**
 * Description: show container metadata request.
 * 
 * Show container metadata request:
 * curl -i $publicURL/marktwain -X HEAD -H "X-Auth-Token: $token"
 * 
 * @param hostManager
 * @param container
 * @param user
 * @param pass
 * @param tenant
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String showContainerMetadata(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                                //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                    @ShellParameter(name = "user", comment = "user") String user,
                                    @ShellParameter(name = "pass", comment = "pass") String pass,
                                    @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                    @ShellParameter(name="container", comment="container") String container
                                    )throws CleverException{
 
    
//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per showContainerMetadata:", ex);
        System.out.println("Errore nell'operazione di richiesta del token per showContainerMetadata:" + ex);
        return null;
    }
               
    
    //#####################################
    // showContainerMetadata
    //#####################################
    
           
    
    //creo un oggetto di inserimento figlio
    InsertContainer insertContainer = new InsertContainer();
    //gli passo i parametri
    insertContainer.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertContainer.setTokenId(token.getId());
    insertContainer.setContainer(container);
        
    //ricavo altri parametri
    insertContainer.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertContainer; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertContainer; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "showContainerMetadata", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando showContainerMetadata: " + ex);
        logger.error("Errore nel comando showContainerMetadata: ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    //mi creo il particolare oggetto figlio di risposta
    InfoListContainerForMongoDb risp = new InfoListContainerForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoListContainerForMongoDb) swiftParameterOutput;
              
    
    
   stringa = risp.getResponse();
   System.out.println(stringa);
        
   return stringa;     
    
}//showContainerMetadata



//#############################################################################


/**
 * 
 * Description: create object.
 * 
 * Create object:
 * curl -i $publicURL/janeausten/helloworld.txt -X PUT -H "Content-Length: 1" 
 *                                                     -H "Content-Type: text/html; charset=UTF-8" 
 *                                                     -H "X-Auth-Token: $token"
 * 
 * @param hostManager
 * @param user
 * @param pass
 * @param container
 * @param tenant
 * @param pathObject
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String createObject(@ShellParameter(name="riccardo", comment="hostManager") String hostManager,
                                //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                    @ShellParameter(name = "user", comment = "user") String user,
                                    @ShellParameter(name = "pass", comment = "pass") String pass,
                                    @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                    @ShellParameter(name="container", comment="container") String container,
                                    @ShellParameter(name="pathObject", comment="pathObject") String pathObject
                                    )throws CleverException{
 
        
//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per createObject:", ex);
        System.out.println("Errore nell'operazione di richiesta del token per createObject:" + ex);
        return null;
    }
               
    
    //#####################################
    // createObject
    //#####################################
       
    //creo un oggetto di inserimento figlio
    InsertObject insertObject = new InsertObject();
    //gli passo i parametri
    insertObject.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertObject.setTokenId(token.getId());
    insertObject.setContainer(container);
    insertObject.setPathObject(pathObject);
    
    //ricavo altri parametri
    insertObject.elaboraInfo();
    
    //creo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertObject; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertObject; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    //pulisco la lista di parametri
    //params.clear();
    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "createObject", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando createObject: " + ex);
        logger.error("Errore nel comando createObject: ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
       
    //mi creo il particolare oggetto figlio di risposta
    InfoCreateObjectForMongoDb risp = new InfoCreateObjectForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoCreateObjectForMongoDb) swiftParameterOutput;
              
                            
    //201
    if(risp.getStatusCode().equals("201")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
             
    return stringa;
    
     
}//createObject



/**
 * Description: replace object.
 * 
 * Replace object:
 * curl -i $publicURL/janeausten/helloworld.txt -X PUT -H "Content-Length: 1" 
 *                                                     -H "Content-Type: text/html; charset=UTF-8" 
 *                                                     -H "X-Auth-Token: $token"
 * 
 * @param hostManager
 * @param user
 * @param container
 * @param pass
 * @param pathObject
 * @param tenant
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String replaceObject(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                        //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                            @ShellParameter(name = "user", comment = "user") String user,
                            @ShellParameter(name = "pass", comment = "pass") String pass,
                            @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                            @ShellParameter(name="container", comment="container") String container,
                            @ShellParameter(name="pathObject", comment="pathObject") String pathObject
                            )throws CleverException{
  

        
//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per replaceObject:", ex);
        System.out.println("Errore nell'operazione di richiesta del token per replaceObject:" + ex);
        return null;
    }
               
    
    //#####################################
    // replaceObject
    //#####################################
       
    //creo un oggetto di inserimento figlio
    InsertObject insertObject = new InsertObject();
    //gli passo i parametri
    insertObject.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertObject.setTokenId(token.getId());
    insertObject.setContainer(container);
    insertObject.setPathObject(pathObject);
    
    //ricavo altri parametri
    insertObject.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertObject; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertObject; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "replaceObject", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando createObject: " + ex);
        logger.error("Errore nel comando createObject: ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoCreateObjectForMongoDb risp = new InfoCreateObjectForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoCreateObjectForMongoDb) swiftParameterOutput;
              
                            
    //201
    if(risp.getStatusCode().equals("201")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
             
    return stringa;    
    
    
      
}//replaceObject



/**
 * 
 * Description: permanently deletes an object from the object store.
 * 
 * Permanently deletes an object from the object store:
 * curl -i $publicURL/marktwain/helloworld -X DELETE -H "X-Auth-Token: $token"
 * 
 * @param hostManager
 * @param user
 * @param container
 * @param pass
 * @param object
 * @param tenant
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String deleteObject(@ShellParameter(name="riccardo", comment="hostManager") String hostManager,
                      //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                           @ShellParameter(name = "user", comment = "user") String user,
                           @ShellParameter(name = "pass", comment = "pass") String pass,
                           @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                           @ShellParameter(name="container", comment="container") String container,
                           @ShellParameter(name="object", comment="object") String object
                           )throws CleverException{
 
//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per deleteObject:", ex);
        System.out.println("Errore nell'operazione di richiesta del token per deleteObject:" + ex);
        return null;
    }
               
    
    //#####################################
    // deleteObject
    //#####################################
       
    //creo un oggetto di inserimento figlio
    InsertObject insertObject = new InsertObject();
    //gli passo i parametri
    insertObject.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertObject.setTokenId(token.getId());
    insertObject.setContainer(container);
    insertObject.setObject(object);
    
    //ricavo altri parametri
    insertObject.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertObject; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertObject; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "deleteObject", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando deleteObject: " + ex);
        logger.error("Errore nel comando deleteObject: ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoDeleteObjectForMongoDb risp = new InfoDeleteObjectForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoDeleteObjectForMongoDb) swiftParameterOutput;
              
                            
    //204
    if(risp.getStatusCode().equals("204")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
             
    return stringa;
       
 }//deleteObject



/**
 * Description: copies an object to another object in the object store.
 * 
 * Copies an object to another object in the object store:
 * curl -i $publicURL/marktwain/goodbye -X COPY -H "X-Auth-Token: $token" 
 *                                              -H "Destination: janeausten/goodbye"
 *
 * @param hostManager
 * @param user
 * @param containerOrigin
 * @param pass
 * @param objectOrigin
 * @param tenant
 * @param containerDestination
 * @param objectDestination
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String copyObject(@ShellParameter(name="riccardo", comment="hostManager") String hostManager,
                     //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                         @ShellParameter(name = "user", comment = "user") String user,
                         @ShellParameter(name = "pass", comment = "pass") String pass,
                         @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                         @ShellParameter(name="containerOrigin", comment="containerOrigin") String containerOrigin,
                         @ShellParameter(name="objectOrigin", comment="objectOrigin") String objectOrigin,
                         @ShellParameter(name="containerDestination", comment="containerDestination") String containerDestination,
                         @ShellParameter(name="objectDestination", comment="objectDestination") String objectDestination
                         )throws CleverException{
    
  
//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per copyObject:", ex);
        System.out.println("Errore nell'operazione di richiesta del token per copyObject:" + ex);
        return null;
    }
               
    
    //#####################################
    // copyObject
    //#####################################
       
    //creo un oggetto di inserimento figlio
    InsertObject insertObject = new InsertObject();
    //gli passo i parametri
    insertObject.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertObject.setTokenId(token.getId());
    insertObject.setContainerOrigin(containerOrigin);
    insertObject.setObjectOrigin(objectOrigin);
    insertObject.setContainerDestination(containerDestination);
    insertObject.setObjectDestination(objectDestination);
    
    
    //ricavo altri parametri
    insertObject.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertObject; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertObject; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "copyObject", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando copyObject: " + ex);
        logger.error("Errore nel comando copyObject: ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoCopyObjectForMongoDb risp = new InfoCopyObjectForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoCopyObjectForMongoDb) swiftParameterOutput;
              
                            
    //201
    if(risp.getStatusCode().equals("201")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
    return stringa;
 
    
}//copyObject




/**
 * Description: shows object metadata.
 * 
 * Shows object metadata:
 * curl -i $publicURL/marktwain/goodbye -X HEAD -H "X-Auth-Token: $token"
 * 
 * @param hostManager
 * @param user
 * @param pass
 * @param tenant
 * @param container
 * @param object
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String showObjectMetadata(@ShellParameter(name="riccardo", comment="hostManager") String hostManager,
                             //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                 @ShellParameter(name = "user", comment = "user") String user,
                                 @ShellParameter(name = "pass", comment = "pass") String pass,
                                 @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                 @ShellParameter(name="container", comment="container") String container,
                                 @ShellParameter(name="object", comment="object") String object
                                 )throws CleverException{
 
    
//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per showObjectMetadata:", ex);
        System.out.println("Errore nell'operazione di richiesta del token per showObjectMetadata:" + ex);
        return null;
    }
               
    
    //#####################################
    // deleteObject
    //#####################################
       
    //creo un oggetto di inserimento figlio
    InsertObject insertObject = new InsertObject();
    //gli passo i parametri
    insertObject.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertObject.setTokenId(token.getId());
    insertObject.setContainer(container);
    insertObject.setObject(object);
    
    //ricavo altri parametri
    insertObject.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertObject; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertObject; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "showObjectMetadata", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando showObjectMetadata: " + ex);
        logger.error("Errore nel comando showObjectMetadata: ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoListObjectMetadataForMongoDb risp = new InfoListObjectMetadataForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoListObjectMetadataForMongoDb) swiftParameterOutput;
       
    stringa = risp.getResponse();
    System.out.println(stringa);
       
    return stringa;
   
}//showObjectMetadata



/**
 * Description: create object metadata.
 * 
 * Create object metadata:
 * curl -i $publicURL/marktwain/goodbye -X POST -H "X-Auth-Token: $token" 
 *                                              -H "X-Object-Meta-Book: GoodbyeColumbus"
 * @param hostManager
 * @param user
 * @param pass
 * @param tenant
 * @param container
 * @param object
 * @param X_Container_Meta
 * @param name
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String createObjectMetadata(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                             //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                 @ShellParameter(name = "user", comment = "user") String user,
                                 @ShellParameter(name = "pass", comment = "pass") String pass,
                                 @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                 @ShellParameter(name="container", comment="container") String container,
                                 @ShellParameter(name="object", comment="object") String object,
                                 @ShellParameter(name="X_Container_Meta", comment="X_Container_Meta") String X_Container_Meta,
                                 @ShellParameter(name="name", comment="X_Container_Meta name")String  name
                                 )throws CleverException{

//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per createObjectMetadata:", ex);
        System.out.println("Errore nell'operazione di richiesta del token per createObjectMetadata:" + ex);
        return null;
    }
               
    
    //#####################################
    // createObjectMetadata
    //#####################################
       
    //creo un oggetto di inserimento figlio
    InsertObject insertObject = new InsertObject();
    //gli passo i parametri
    insertObject.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertObject.setTokenId(token.getId());
    insertObject.setContainer(container);
    insertObject.setObject(object);
    insertObject.setX_Container_Meta(X_Container_Meta);
    insertObject.setName(name);
    
    //ricavo altri parametri
    insertObject.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertObject; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertObject; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "createObjectMetadata", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando createObjectMetadata: " + ex);
        logger.error("Errore nel comando createObjectMetadata: ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoCreateObjectMetadataForMongoDb risp = new InfoCreateObjectMetadataForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoCreateObjectMetadataForMongoDb) swiftParameterOutput;
              
                            
    //201
    if(risp.getStatusCode().equals("201")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
             
    return stringa;
     
}//createObjectMetadata


/**
 *  Description: update object metadata.
 * 
 * Update object metadata:
 * curl -i $publicURL/marktwain/goodbye -X POST -H "X-Auth-Token: $token" 
 *                                      -H "X-Object-Meta-Book: GoodbyeOldFriend"
 * 
 * @param hostManager
 * @param user
 * @param container
 * @param pass
 * @param object
 * @param tenant
 * @param X_Container_Meta
 * @param name
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String updateObjectMetadata(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                              //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                                   @ShellParameter(name = "user", comment = "user") String user,
                                   @ShellParameter(name = "pass", comment = "pass") String pass,
                                   @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                                   @ShellParameter(name="container", comment="container") String container,
                                   @ShellParameter(name="object", comment="object") String object,
                                   @ShellParameter(name="X_Container_Meta", comment="X_Container_Meta") String X_Container_Meta,
                                   @ShellParameter(name="name", comment="X_Container_Meta name")String  name
                                   )throws CleverException{

//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per updateObjectMetadata:", ex);
        System.out.println("Errore nell'operazione di richiesta del token per updateObjectMetadata:" + ex);
        return null;
    }
               
    
    //#####################################
    // updateObjectMetadata
    //#####################################
       
    //creo un oggetto di inserimento figlio
    InsertObject insertObject = new InsertObject();
    //gli passo i parametri
    insertObject.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertObject.setTokenId(token.getId());
    insertObject.setContainer(container);
    insertObject.setObject(object);
    insertObject.setX_Container_Meta(X_Container_Meta);
    insertObject.setName(name);
    
    //ricavo altri parametri
    insertObject.elaboraInfo();
    

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertObject; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertObject; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "updateObjectMetadata", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando updateObjectMetadata: " + ex);
        logger.error("Errore nel comando updateObjectMetadata: ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
    InfoCreateObjectMetadataForMongoDb risp = new InfoCreateObjectMetadataForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoCreateObjectMetadataForMongoDb) swiftParameterOutput;
              
                            
    //201
    if(risp.getStatusCode().equals("201")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
             
    return stringa;
    
}//updateObjectMetadata


/**
 * 
 * Description: get an object from the object store.
 * 
 * Get an object from the object store:
 * curl -X GET -H "X-Auth-Token: $token" $publicURL/dogs/JingleRocky.jpg > JingleRocky.jpg
 * 
 * @param hostManager
 * @param user
 * @param container
 * @param tenant
 * @param pass
 * @param object
 * @param pathDestination
 * @return
 * @throws CleverException 
 */
@ShellCommand
public String downloadObject(@ShellParameter(name="riccardo", comment="hostMAnager") String hostManager,
                         //  @ShellParameter(name = "authUrlKeystone", comment = "authUrlKeystone") String authUrlKeystone,
                             @ShellParameter(name = "user", comment = "user") String user,
                             @ShellParameter(name = "pass", comment = "pass") String pass,
                             @ShellParameter(name = "tenant", comment = "tenant") String tenant,
                             @ShellParameter(name="container", comment="container") String container,
                             @ShellParameter(name="object", comment="object") String object,
                             @ShellParameter(name="pathDestination", comment="pathDestination") String pathDestination
                            )throws CleverException{
 
    
 
//#####################################
// ricavo token e publicUrlSwift
//#####################################
    
    ArrayList params = new ArrayList();

    Token token = new Token();

    params.add(tenant);
    params.add(user);
    params.add(pass);

    try {
        token = (Token) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(), "IdentityServiceAgent", "authenticationUserPassTen", params, false);
                 //debug
                 //token.debug();
    } catch (CleverException ex) {

        logger.error("Errore nell'operazione di richiesta del token per downloadObject:", ex);
        System.out.println("Errore nell'operazione di richiesta del token per downloadObject:" + ex);
        return null;
    }
               
    
    //#####################################
    // downloadObject
    //#####################################
       
    //creo un oggetto di inserimento figlio
    InsertObject insertObject = new InsertObject();
    //gli passo i parametri
    insertObject.setUrlSwiftPresoDalToken(token.getPublicUrlSwift());
    insertObject.setTokenId(token.getId());
    insertObject.setContainer(container);
    insertObject.setPathObject(pathDestination);
        
    
    //ricavo altri parametri
    insertObject.elaboraInfo();
    
    //inizializzo il nome dell'oggetto questa volta dopo elaboraInfo()
    insertObject.setObject(object);

    //credo l'oggetto di inserimento padre
    SwiftParameterInput swiftParameterInput = new SwiftParameterInput();

    //faccio l'upcasting da figlio a padre
    swiftParameterInput.type=SwiftParameterInput.tipoObjectInput.InsertObject; //####
    swiftParameterInput.ogg = (SwiftParameterInput) insertObject; //#####

    //credo l'oggetto di risposta padre
    SwiftParameterOutput swiftParameterOutput = new SwiftParameterOutput();

    ArrayList params2 = new ArrayList();
        
    //inserisco come parametro l'oggetto di inserimento padre
    params2.add(swiftParameterInput);

    String stringa = "";
    Object risposta;
    
    try {
        risposta = this.execSyncCommand(hostManager, "ObjectStorageAgent", "downloadObject", params2, false);
    } catch (CleverException ex) {
        System.out.println("Errore nel comando downloadObject: " + ex);
        logger.error("Errore nel comando downloadObject: ",ex);
        return null;
    }
    
    //conversione object 
    swiftParameterOutput = (SwiftParameterOutput) risposta;
    
    
    //mi creo il particolare oggetto figlio di risposta
   InfoGetObjectForMongoDb risp = new  InfoGetObjectForMongoDb();
   
    //effettuo il downcosting, da padre a figlio
    risp = (InfoGetObjectForMongoDb) swiftParameterOutput;
              
                            
    //200
    if(risp.getStatusCode().equals("200")){
        
        stringa = risp.getStatusCode() + ": l'operazione è andata a buon fine!!!";
        
        
    }else{ stringa = risp.getStatusCode() + ": l'operazione non è andata a buon fine!!!";}
    
    System.out.println(stringa);
             
    return stringa;
   
   
}//downloadObject





}//ObjectStorageSwift
