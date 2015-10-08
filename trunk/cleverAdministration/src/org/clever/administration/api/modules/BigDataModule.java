/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.administration.api.modules;

import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Utils.TypeOfElement;
import org.clever.administration.annotations.HasScripts;
import org.clever.administration.annotations.ShellCommand;
import org.clever.administration.annotations.ShellParameter;
import org.clever.administration.api.Session;
import org.clever.Common.Utils.BigDataMethodName;
import org.clever.Common.Utils.BigDataParameterContainer;
import org.clever.Common.Utils.CalculateOnFieldParameterContainer;
import org.clever.Common.Utils.ObjectSwift;
import org.clever.Common.Utils.OperationName;

/**
 *
 * @author agalletta
 */
@HasScripts(value="BDM", script="scripts/big.bsh", comment="BigData Administration Module for Clever")
public class BigDataModule extends AdministrationModule{ 
    
   Logger logger=Logger.getLogger("BigDataModule");


    public BigDataModule(Session s) {
        super(s);
    }
    
    @ShellCommand
    public void insertOnDB(@ShellParameter(name="dbName", comment="Name of the Database") String dbName,
                           @ShellParameter(name="elemToInsert", comment="object to insert in DB")Object elementToInsert,
                           @ShellParameter(name="type", comment="Type of element") String type) throws CleverException{
        
        
        BigDataParameterContainer struct= new BigDataParameterContainer();
                struct.setDbName(dbName);
                struct.setElemToInsert(elementToInsert);
                struct.setType(TypeOfElement.valueOf(type));
        
                 ArrayList params = new ArrayList();
                 params.add(struct);
                 
             try{
                 this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","insertOnDB", params,false);
             }
             catch(Exception ex){
                     throw new CleverException(ex);}
        
            }
    
     @ShellCommand
    public Set getCollectionNames(@ShellParameter(name="dbName", comment="Name of the Database") String dbName) throws CleverException{
        
                 ArrayList params = new ArrayList();
                 params.add(dbName);
               try{
               return (Set)this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","getCollectionNames", params,false);
              }
             catch(Exception ex){
                 logger.error(ex);
                     throw new CleverException(ex);
                     
             }
        
            }
    
       @ShellCommand
    public List findOnDB(@ShellParameter(name="dbName", comment="Name of the Database") String dbName,
                           @ShellParameter(name="collectionName", comment="name of collection")String collectionName,
                           @ShellParameter(name="nameFiled", comment="name field")String nameFiled,
                           @ShellParameter(name="thresholdMin", comment="thresholdMin") Object thresholdMin,
                           @ShellParameter(name="thresholdMax", comment="thresholdMax null if you not find in range") Object thresholdMax,
                           @ShellParameter(name="methodName", comment="method Name") String  mtdName) throws CleverException{
        
             BigDataParameterContainer struct= new BigDataParameterContainer();
                struct.addFields(nameFiled);
                struct.addThreshold(thresholdMin);
                struct.addThreshold(thresholdMax);
                struct.setMethod(BigDataMethodName.valueOf(mtdName));
                struct.setDbName(dbName);
             if(!collectionName.equals("null")){
                struct.setCollectionName(collectionName);
                }
        
        
        
        ArrayList params = new ArrayList();
                 params.add(struct);
                 
                 
             try{
                return(List) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","findOnDB", params,false);
             }
             catch(Exception ex){
                     throw new CleverException(ex);}
        
            }
   
   @ShellCommand
    public List getListDB() throws CleverException{
        try{
                return(List) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","getListDB", new ArrayList(),false);
             }
             catch(Exception ex){
                     throw new CleverException(ex);}
        
            }
 
         @ShellCommand
    public List getLastmsec(@ShellParameter(name="dbName", comment="Name of the Database") String dbName,
                           @ShellParameter(name="collectionName", comment="name of collection, null if you want do a find on DB")String collectionName,
                           @ShellParameter(name="range", comment="range that you want find") long msec) throws CleverException{
        
             BigDataParameterContainer struct= new BigDataParameterContainer();
                struct.addFields("insertTimestamp");
                struct.addThreshold((System.currentTimeMillis()-msec));
                struct.setMethod(BigDataMethodName.findGreaterOrEqualThan);
                struct.setDbName(dbName);
             if(!collectionName.equals("null")){
                struct.setCollectionName(collectionName);
                }
        
        
        
        ArrayList params = new ArrayList();
                 params.add(struct);
                 
                 
             try{
                return(List) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","findOnDB", params,false);
             }
             catch(Exception ex){
                     throw new CleverException(ex);}
        
            }
    

  @ShellCommand
  public List calculateOnField(String dbName,String collectionName,String fieldName, String opName,long step,String unitStep,String initialDate,String initialTime,String finalDate,String finalTime,String timeZone,long range,String unitRange) throws CleverException{
 String datainizio = null,dataFine = null,oraInizio = null,OraFine = null;
 TimeZone fusoOrario = null;
 if(!initialDate.equals("null")){
     datainizio=initialDate;
 }
 logger.debug(initialDate);
 if(!initialTime.equals("null")){
     oraInizio=initialTime.replace("-", ":");
 }
 logger.debug(initialTime+"asd"+oraInizio);
 if(!finalDate.equals("null")){
     dataFine=finalDate;
 }
 logger.debug(finalDate);
 
 if(!finalTime.equals("null")){
     OraFine=finalTime.replace("-", ":");
 }
 logger.debug(finalTime+"asd"+OraFine);
 if(!timeZone.equals("null")){
     fusoOrario=TimeZone.getTimeZone(timeZone);
 }

      
      CalculateOnFieldParameterContainer container=new CalculateOnFieldParameterContainer( dbName, collectionName, fieldName, OperationName.valueOf(opName), step, TimeUnit.valueOf(unitStep), datainizio,oraInizio,dataFine,OraFine, fusoOrario, range, TimeUnit.valueOf(unitRange));
 Logger logger=Logger.getLogger("BigDataModule");
                 logger.debug(container.getEndDate()+"sssssss"+container.getStartDate());
   // CalculateOnFieldParameterContainer c=new CalculateOnFieldParameterContainer("sensing","sensor1","TMP",OperationName.maximum,0,TimeUnit.MINUTES,null,"16:00:00",null,"16:15:00",null,10,TimeUnit.MINUTES);
 logger.debug("bbbbbbbbbbbbbbb");
    ArrayList params = new ArrayList();
                 params.add(container);
                 
  try{
                return(List) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","calculateOnField", params,false);
             }
             catch(Exception ex){
                     throw new CleverException(ex);}
}
   @ShellCommand
    public void insertOnSwift(@ShellParameter(name="pathFile", comment="path of file") String pathFile,
                               @ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant,
                               @ShellParameter(name="password", comment="Name of the password") String password) throws CleverException{
               
                ObjectSwift obj=new ObjectSwift(pathFile,user,tenant);
                 ArrayList params = new ArrayList();
                 params.add(obj);
               try{
               this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","insertInSwift", params,false);
              }
             catch(Exception ex){
                 logger.error(ex);
                     throw new CleverException(ex);
                     
             }
        
            }
    @ShellCommand
     public void insertOnSwift() throws CleverException{
        
                ObjectSwift obj=new ObjectSwift("/home/agalletta/Scrivania/prova/Incantesimo.mp3.rar","admin","admin");
                 ArrayList params = new ArrayList();
                 HashMap hm=new HashMap();
                 //hm.put("manipulation", "cutted");
                 //hm.put("originEtag", "qwertyuiih");
                 //hm.put("altroMetadatoGenerico", 1234);
                 obj.addMetadata("manipulation", "cutted");
                 obj.addMetadata("originEtag", "qwertyuiih");
                 obj.addMetadata("altroMetadatoGenerico", "1234");
                 
                 params.add(obj);
               try{
               this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","insertInSwift", params,false);
              }
             catch(Exception ex){
                 logger.error(ex);
                     throw new CleverException(ex);
                     
             }
        
            }
     
  @ShellCommand
    public List getUrlFiles(@ShellParameter(name="fileName", comment="fileName") String fileName,
                               @ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant,
                               @ShellParameter(name="all", comment="complete document") String all) throws CleverException{
               
                 ArrayList params = new ArrayList();
                 params.add(fileName);
                 params.add(user);
                 params.add(tenant);
                 params.add(all);

               try{
             return (List) this.execSyncCommand(this.session.getHostAdministrationModule().getActiveCM(),"BigDataAgent","getUrlFile", params,true);
              }
             catch(Exception ex){
                 logger.error(ex);
                     throw new CleverException(ex);
                     
             }
        
            }
  
    @ShellCommand
    public List getAllUrlFiles(@ShellParameter(name="user", comment="Name of the user") String user,
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant,
                               @ShellParameter(name="all", comment="complete document") String all) throws CleverException{
               

               try{
                   return this.getUrlFiles("",user, tenant, all);
              }
             catch(Exception ex){
                 logger.error(ex);
                     throw new CleverException(ex);
                     
             }
        
            }

    @ShellCommand
    public void listaUrl() throws CleverException{
        
        List lista=this.getUrlFiles("Incantesimo.mp3.rar", "admin", "admin","true");
        
        for(int i=0;i<lista.size();i++){
            System.out.println(((DBObject)lista.get(i)).toString());
        
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
                               @ShellParameter(name="tenant", comment="Name of the tenant") String tenant,
                               @ShellParameter(name="all", comment="complete document") String all) throws CleverException{
               
                 ArrayList params = new ArrayList();
                 params.add(user);
                 params.add(tenant);
                 params.add(all);

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
        List lista=this.getUrlAllFiles("admin", "admin", "false");
        
        for(int i=0;i<lista.size();i++){
             System.out.println(((DBObject)lista.get(i)).toString());
        
        }
    
    
    }
    
}
