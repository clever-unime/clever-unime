/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.administration.api.modules;

import com.mongodb.DBObject;
import it.eng.rspa.sigma.il.xml.publish.builder.ParametersKeys;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.log4j.Logger;
import org.clever.Common.Digest.Digest;
import org.clever.Common.Exceptions.CleverException;
import org.clever.administration.annotations.HasScripts;
import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.annotations.ShellParameter;
import org.clever.administration.api.Session;
import org.clever.Common.Utils.BigDataParameterContainer;
import org.clever.Common.Utils.ObjectSwift;
import org.clever.Common.XMLTools.ParserXML;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

/**
 *
 * @author agalletta
 */
@HasScripts(value="OMM", script="scripts/omm.bsh", comment="ObjectManager Administration Module for Clever")
public class ObjectManagerModule extends AdministrationModule{ 
    
   Logger logger=Logger.getLogger("ObjectManagerModule");
   String urlSwift,userAdmin,tenantAdmin;


    public ObjectManagerModule(Session s) {
        super(s);
        this.init();
    }
    

    
      
  @ShellCommand
    public List getUrlFiles(@ShellParameter(name="fileName", comment="fileName") String fileName,
                               @ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant) throws CleverException{
               
                 ArrayList params = new ArrayList();
                 params.add(fileName);
                 params.add(user);
                 params.add(tenant);

               try{
             return (List) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","getUrlFile", params,true);
              }
             catch(Exception ex){
                 logger.error(ex);
                     throw new CleverException(ex);
                     
             }
        
            }
    
    @ShellCommand
    public List getUUIDsByUser(@ShellParameter(name="user", comment="Name of the user") String user) throws CleverException{
               
                 ArrayList params = new ArrayList();
                 params.add(user);

               try{
             return (List) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","getUUIDsByUser", params,true);
              }
             catch(Exception ex){
                 logger.error(ex);
                     throw new CleverException(ex);
                     
             }
        
            }
    @ShellCommand
        public void listaUuidByUser() throws CleverException{
        
        List lista=this.getUUIDsByUser("admin");
        
        for(int i=0;i<lista.size();i++){
             System.out.println((lista.get(i)));
        
        }
    
    
    }
    
    @ShellCommand
    public List getUUIDsByTenant(@ShellParameter(name="tenant", comment="Name of the tenant") String tenant) throws CleverException{
               
                 ArrayList params = new ArrayList();
                 params.add(tenant);

               try{
             return (List) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","getUUIDsByTenant", params,true);
              }
             catch(Exception ex){
                 logger.error(ex);
                     throw new CleverException(ex);
                     
             }
        
            }
@ShellCommand    
public void listaUuidByTenant() throws CleverException{
        
        List lista=this.getUUIDsByTenant("admin");
        
        for(int i=0;i<lista.size();i++){
            System.out.println((lista.get(i)));
        
        }
    
    
    }
    
    @ShellCommand
    public List getUrlAllFiles(@ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant) throws CleverException{
               
                 ArrayList params = new ArrayList();
                 params.add(user);
                 params.add(tenant);

               try{
             return (List) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","getUrlAllFiles", params,true);
              }
             catch(Exception ex){
                 logger.error(ex);
                     throw new CleverException(ex);
                     
             }
        
            }
    @ShellCommand
public void listaAllUrl() throws CleverException{
        List lista=this.getUrlAllFiles("admin", "admin");
        
        for(int i=0;i<lista.size();i++){
             System.out.println(((DBObject)lista.get(i)).toString());
        
        }
    
    
    }

  
@ShellCommand
public void getByName(@ShellParameter(name="fileName", comment="name of file") String fileName,
                               @ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant,
                               @ShellParameter(name="path", comment="local directory") String path) throws CleverException{
 
    String  password,md5Name,nameContainer;
    AccountConfig config;
    Account account;
    ArrayList params = new ArrayList();
    Container container;
    StoredObject object;
    
    nameContainer=user+"__"+tenant+"_tmp";
    ObjectSwift obj=new ObjectSwift(fileName,user,tenant);
       
    System.out.println("Insert your password: ");
    Scanner sc = new Scanner(System.in);
    password =sc.next();
    config = new AccountConfig();
    config.setUsername(user);
    config.setPassword(String.valueOf(password));
    config.setAuthUrl(urlSwift);
    config.setTenantName(tenant);
    
    account = new AccountFactory(config).createAccount();
    container= account.getContainer(nameContainer);
     if(!container.exists()){  
         container.create();
    }
     container.setContainerRights(tenantAdmin+":"+userAdmin, tenantAdmin+":"+userAdmin);
     
     params.clear();
     params.add(obj);
     
     try{
          md5Name= (String) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"ObjectManagerAgent","getObjectByName", params,false);
       
       }
       catch(Exception ex){
           logger.error("error in getByName",ex);
           throw new CleverException(ex);
       }
     
     if(md5Name!=null){
         object = container.getObject(md5Name);
         object.downloadObject(new File(path));
         
         System.out.println("Done! path: "+path );
         object.delete();
       
    }
    else{
        System.out.println("File not found");
    }
    
    
    }    
    
@ShellCommand
public void getByName(@ShellParameter(name="fileName", comment="name of file") String fileName,
                               @ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant,
                               @ShellParameter(name="password", comment="password") String password,
                               @ShellParameter(name="path", comment="local directory") String path) throws CleverException{
 
    String  md5Name,nameContainer;
    AccountConfig config;
    Account account;
    ArrayList params = new ArrayList();
    Container container;
    StoredObject object;
    
    nameContainer=user+"__"+tenant+"_tmp";
    ObjectSwift obj=new ObjectSwift(fileName,user,tenant);
       
    config = new AccountConfig();
    config.setUsername(user);
    config.setPassword(String.valueOf(password));
    config.setAuthUrl(urlSwift);
    config.setTenantName(tenant);
    
    account = new AccountFactory(config).createAccount();
    container= account.getContainer(nameContainer);
     if(!container.exists()){  
         container.create();
    }
     container.setContainerRights(tenantAdmin+":"+userAdmin, tenantAdmin+":"+userAdmin);
     
     params.clear();
     params.add(obj);
     
     try{
          md5Name= (String) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"ObjectManagerAgent","getObjectByName", params,false);
       
       }
       catch(Exception ex){
           logger.error("error in getByName",ex);
           throw new CleverException(ex);
       }
     
     if(md5Name!=null){
         object = container.getObject(md5Name);
         object.downloadObject(new File(path));
         
         System.out.println("Done! path: "+path );
         object.delete();
       
    }
    else{
        System.out.println("File not found");
    }
    
    
    }    
    
    
   
@ShellCommand
public void getByUrl(@ShellParameter(name="url", comment="url of file") String url,
                               @ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant,
                               @ShellParameter(name="path", comment="local directory") String path) throws CleverException{
    
    String password,md5Name;
    AccountConfig config;
    Account account;
    ArrayList params = new ArrayList();
    ObjectSwift obj=new ObjectSwift(url,user,tenant);
    Container container;
    BigDataParameterContainer struct= new BigDataParameterContainer();
    StoredObject object;
    String nameContainer=user+"__"+tenant+"_tmp";
 
    System.out.println("Insert your password: ");
    Scanner sc = new Scanner(System.in);
    password=sc.next();
    config = new AccountConfig();
    config.setUsername(user);
    config.setPassword(String.valueOf(password));
    config.setAuthUrl(urlSwift);
    config.setTenantName(tenant);
    account = new AccountFactory(config).createAccount();

    container= account.getContainer(nameContainer);
     if(!container.exists()){  
         container.create();


     }
     container.setContainerRights(tenantAdmin+":"+userAdmin,tenantAdmin+":"+userAdmin);
   
     
     try{
         params.clear();
         params.add(obj);
         md5Name= (String) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"ObjectManagerAgent","getObjectbyUrl", params,false);
     }
     catch(Exception ex){
          logger.error("error in getByUrl",ex);
          throw new CleverException(ex);
     }
     if(md5Name!=null){
         object = container.getObject(md5Name);
         object.downloadObject(new File(path));
         
         System.out.println("Done! path: "+path );
         object.delete();
    }
    else{
        System.out.println("File not found");
    }
}
    
@ShellCommand
public void getByUrl(@ShellParameter(name="url", comment="url of file") String url,
                               @ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant,
                               @ShellParameter(name="password", comment="password") String password,
                               @ShellParameter(name="path", comment="local directory") String path) throws CleverException{
    
    String md5Name;
    AccountConfig config;
    Account account;
    ArrayList params = new ArrayList();
    ObjectSwift obj=new ObjectSwift(url,user,tenant);
    Container container;
    BigDataParameterContainer struct= new BigDataParameterContainer();
    StoredObject object;
    String nameContainer=user+"__"+tenant+"_tmp";
 
    config = new AccountConfig();
    config.setUsername(user);
    config.setPassword(String.valueOf(password));
    config.setAuthUrl(urlSwift);
    config.setTenantName(tenant);
    account = new AccountFactory(config).createAccount();

    container= account.getContainer(nameContainer);
     if(!container.exists()){  
         container.create();


     }
     container.setContainerRights(tenantAdmin+":"+userAdmin,tenantAdmin+":"+userAdmin);
   
     
     try{
         params.clear();
         params.add(obj);
         md5Name= (String) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"ObjectManagerAgent","getObjectbyUrl", params,false);
     }
     catch(Exception ex){
          logger.error("error in getByUrl",ex);
          throw new CleverException(ex);
     }
     if(md5Name!=null){
         object = container.getObject(md5Name);
         object.downloadObject(new File(path));
         
         System.out.println("Done! path: "+path );
         object.delete();
    }
    else{
        System.out.println("File not found");
    }
}
    
      

private void init(){
    
        ParserXML parser=new ParserXML(new File("./cfg/configuration_objectManager.xml"));
        urlSwift=parser.getElementContentInStructure("url");
        userAdmin=parser.getElementContentInStructure("userAdmin");
        tenantAdmin=parser.getElementContentInStructure("tenantAdmin");
    }
    

@ShellCommand
public void insertOnSwift(@ShellParameter(name="pathFile", comment="path of file") String pathFile,
                               @ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant) throws CleverException{
    
    String token,result;
    System.out.println("Insert your password: ");
    Scanner sc = new Scanner(System.in);
    String password=sc.next();
    AccountConfig config = new AccountConfig();
            config.setUsername(user);
            config.setPassword(String.valueOf(password));
            config.setAuthUrl(urlSwift);
            //config.setTenantId("aaaa");
            config.setTenantName(tenant);
            //config.setAuthenticationMethod(AuthenticationMethod.BASIC);
    Account account = new AccountFactory(config).createAccount();

    File infoFile=new File(pathFile);
    ParserXML parser=new ParserXML(infoFile);
    String estensione,originalName,md5Name,userFile=parser.getElementContent("pathFile");
    String nameContainer=user+"__"+tenant+"_tmp";
    ArrayList params = new ArrayList();
    ObjectSwift obj=new ObjectSwift(userFile,user,tenant);
    int indice;
    
    obj.addMetadata(ParametersKeys.NOTIFICATION_ID, "SAS/PublishBigDataMultimedia");
    obj.addMetadata(ParametersKeys.CONTENT_DEFINITION, parser.getElementContent("definition"));
    obj.addMetadata(ParametersKeys.LATITUDE_VALUE, parser.getElementContent("latitude"));
    obj.addMetadata(ParametersKeys.LONGITUDE_VALUE, parser.getElementContent("longitude"));
    obj.addMetadata(ParametersKeys.ALTITUDE_VALUE, parser.getElementContent("altitude"));
    obj.addMetadata(ParametersKeys.DCSUBJECT_VALUE, parser.getElementContent("subject"));
    obj.addMetadata(ParametersKeys.DCDESCRIPTION_VALUE, parser.getElementContent("description"));
    obj.addMetadata(ParametersKeys.DCDATE_VALUE, parser.getElementContent("timeOfAlert"));
    obj.addMetadata(ParametersKeys.TIMEOFALERT_VALUE, parser.getElementContent("timeOfAlert"));
    obj.addMetadata(ParametersKeys.DCTYPE_VALUE, parser.getElementContent("type"));
    obj.addMetadata(ParametersKeys.DCFORMAT_VALUE, parser.getElementContent("format"));
    obj.addMetadata(ParametersKeys.DCREFERENCES_VALUE, parser.getElementContent("references"));
    obj.addMetadata(ParametersKeys.DCMODIFIED_VALUE, parser.getElementContent("modified"));
                
    Container container = account.getContainer(nameContainer);
        System.out.println(container.exists());
        if(!container.exists()){
            container.create();
            logger.info("crea container");
        }
    File fileToUpload=new File(userFile);
          originalName=fileToUpload.getName();
       try {
         md5Name=Digest.getMD5Checksum(userFile);
       } catch (Exception ex) {
                logger.error(ex);
                md5Name=originalName;
       }
    indice=originalName.indexOf('.');
      if (indice==-1){
          estensione="";
      }
      else{
        estensione=originalName.substring(indice);
        }
       
    StoredObject object = container.getObject(md5Name+estensione);
        try{
            object.uploadObject(fileToUpload);
  
      //      System.out.println("Public URL: "+object.getPublicURL());
            }
        catch(Exception e){
            logger.error(e);
            }
        logger.info("upload nel container tmp terminato");
        token=account.authenticate().getToken();
        //  System.out.println("token"+ token);
        obj.addMetadata(ParametersKeys.DCPROVENANCE_VALUE, originalName);
        obj.addMetadata("originalName", originalName);
        obj.addMetadata("md5Name", md5Name+estensione);
        obj.setToken(token);
        params.clear();
        params.add(obj);
        try{
            
               result=(String) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"ObjectManagerAgent","moveObjectInterTenant", params,true);
              
        }
             catch(Exception ex){
                 logger.error("error in insert",ex);
                     throw new CleverException(ex);
                     
             }
          System.out.println(result);
          if(result.equalsIgnoreCase("ok")){
              object.delete();
          }
        }
    

/**
 * 
 * @param pathFile
 * @param user
 * @param tenant
 * @param password
 * @throws CleverException 
 */
public void insertOnSwift(@ShellParameter(name="pathFile", comment="path of file") String pathFile,
                               @ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant,
                               @ShellParameter(name="password", comment="password") String password) throws CleverException{
    
    String token,result;
    //System.out.println("Insert your password: ");
    //Scanner sc = new Scanner(System.in);
    //String password=sc.next();
    AccountConfig config = new AccountConfig();
            config.setUsername(user);
            config.setPassword(String.valueOf(password));
            config.setAuthUrl(urlSwift);
            //config.setTenantId("aaaa");
            config.setTenantName(tenant);
            //config.setAuthenticationMethod(AuthenticationMethod.BASIC);
    Account account = new AccountFactory(config).createAccount();

    File infoFile=new File(pathFile);
    ParserXML parser=new ParserXML(infoFile);
    String estensione,originalName,md5Name,userFile=parser.getElementContent("pathFile");
    String nameContainer=user+"__"+tenant+"_tmp";
    ArrayList params = new ArrayList();
    ObjectSwift obj=new ObjectSwift(userFile,user,tenant);
    int indice;
    
    obj.addMetadata(ParametersKeys.NOTIFICATION_ID, "SAS/PublishBigDataMultimedia");
    obj.addMetadata(ParametersKeys.CONTENT_DEFINITION, parser.getElementContent("definition"));
    obj.addMetadata(ParametersKeys.LATITUDE_VALUE, parser.getElementContent("latitude"));
    obj.addMetadata(ParametersKeys.LONGITUDE_VALUE, parser.getElementContent("longitude"));
    obj.addMetadata(ParametersKeys.ALTITUDE_VALUE, parser.getElementContent("altitude"));
    obj.addMetadata(ParametersKeys.DCSUBJECT_VALUE, parser.getElementContent("subject"));
    obj.addMetadata(ParametersKeys.DCDESCRIPTION_VALUE, parser.getElementContent("description"));
    obj.addMetadata(ParametersKeys.DCDATE_VALUE, parser.getElementContent("timeOfAlert"));
    obj.addMetadata(ParametersKeys.TIMEOFALERT_VALUE, parser.getElementContent("timeOfAlert"));
    obj.addMetadata(ParametersKeys.DCTYPE_VALUE, parser.getElementContent("type"));
    obj.addMetadata(ParametersKeys.DCFORMAT_VALUE, parser.getElementContent("format"));
    obj.addMetadata(ParametersKeys.DCREFERENCES_VALUE, parser.getElementContent("references"));
    obj.addMetadata(ParametersKeys.DCMODIFIED_VALUE, parser.getElementContent("modified"));
                
    Container container = account.getContainer(nameContainer);
        System.out.println(container.exists());
        if(!container.exists()){
            container.create();
            logger.info("crea container");
        }
    File fileToUpload=new File(userFile);
          originalName=fileToUpload.getName();
       try {
         md5Name=Digest.getMD5Checksum(userFile);
       } catch (Exception ex) {
                logger.error(ex);
                md5Name=originalName;
       }
    indice=originalName.indexOf('.');
      if (indice==-1){
          estensione="";
      }
      else{
        estensione=originalName.substring(indice);
        }
       
    StoredObject object = container.getObject(md5Name+estensione);
        try{
            object.uploadObject(fileToUpload);
  
      //      System.out.println("Public URL: "+object.getPublicURL());
            }
        catch(Exception e){
            logger.error(e);
            }
        logger.info("upload nel container tmp terminato");
        token=account.authenticate().getToken();
        //  System.out.println("token"+ token);
        obj.addMetadata(ParametersKeys.DCPROVENANCE_VALUE, originalName);
        obj.addMetadata("originalName", originalName);
        obj.addMetadata("md5Name", md5Name+estensione);
        obj.setToken(token);
        params.clear();
        params.add(obj);
        try{
            
               result=(String) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"ObjectManagerAgent","moveObjectInterTenant", params,true);
              
        }
             catch(Exception ex){
                 logger.error("error in insert",ex);
                     throw new CleverException(ex);
                     
             }
          System.out.println(result);
          if(result.equalsIgnoreCase("ok")){
              object.delete();
          }
        }
    



}
