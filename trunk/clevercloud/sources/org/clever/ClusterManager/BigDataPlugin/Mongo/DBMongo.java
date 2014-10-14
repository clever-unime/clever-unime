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
 * Copyright 2014 agalletta.
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

package org.clever.ClusterManager.BigDataPlugin.Mongo;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.clever.ClusterManager.BigData.BigDataPlugin;
import org.clever.Common.Communicator.Agent;
import org.clever.Common.Exceptions.CleverException;
import org.clever.Common.Utils.TypeOfElement;
import org.clever.Common.Utils.UtilityJson;
import org.clever.Common.XMLTools.ParserXML;
import org.clever.Common.Utils.BigDataMethodName;
import org.clever.Common.Utils.BigDataParameterContainer;
import org.clever.Common.Utils.CalculateOnFieldParameterContainer;
import org.clever.Common.Utils.OperationName;
import org.jdom2.Element;

/**
 *
 * @author Antonio Galletta 2014
 */
public class DBMongo implements BigDataPlugin {
    
    private String serverURL;
    private String dbName;
    private String user;
    private String password;
    private int port;
    private MongoClient mongoClient;
    private HashMap map;
    private final Logger logger;
    private boolean connection;
    private Agent owner;
    
    public DBMongo(){
    
        logger = Logger.getLogger("BigDataPlugin");
        map=new HashMap();
        connection=false;
    
    
    }
    
    private void init(){
        this.connect();
        
        // logger.debug("prima inserimento");
        //String sens1="<sensor name=\"sensor-981\"><VirtualSensor1><ss2><TMP>"+"988"+"</TMP></ss2><UMD>"+"123"+"</UMD><PRS>"+"333"+"</PRS><timeStamp>"+System.currentTimeMillis()+"</timeStamp></VirtualSensor1><VirtualSensor2><TMP>"+"ax"+"</TMP><UMD>"+"456"+"</UMD><PRS>"+"44554"+"</PRS><timeStamp>"+System.currentTimeMillis()+"</timeStamp></VirtualSensor2></sensor>";     
           // System.out.println(sens1);
          //  this.insertSensing(sens1, TypeOfElement.STRINGXML);
    }
    
    @Override
    public void init(Element params, Agent owner) throws CleverException {
        
        try {
        
        logger.debug("metodo init pubblico");
        dbName=params.getChildText("dbName");
        user=params.getChildText("user");
        password=params.getChildText("password");
        serverURL= params.getChildText("serverUrl");
        port=Integer.valueOf(params.getChildText("port"));
        init();
        }
    
    catch(Exception ex){
       ex.printStackTrace();
    }

    }
    
    /**
     * 
     * this method is used to connect to DB 
     */
    private void connect() {
     ServerAddress server;
     MongoCredential credential;
     logger.debug("dentro connect");
      try {
          server = new ServerAddress(this.serverURL,this.port);
          credential= MongoCredential.createMongoCRCredential(user, dbName, password.toCharArray());
          mongoClient=new MongoClient(server,Arrays.asList(credential));
          connection=true;
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
    private void close(){
         
        try{
           
              mongoClient.close();
              map=new HashMap();
              connection=false;
           }
        
        catch(java.lang.IllegalStateException ex){
            logger.error("Error in mongo instance: ", ex);
        }
        catch(MongoException ex){
            logger.error("Error in operation: ", ex);
        }
        catch(java.lang.NullPointerException ex){
            logger.error("Error nullpointercollection: ", ex);
             }
    }
     
    private boolean isConnect(){
     
         return connection;
         
     }
    
     
     /**
    * metodo che viene utilizzato per ottenere la listCollection di tutti i db  presenti
    * 
    * @return listCollection contenente tutti i db presenti su questo client 
    */
   public List getListDB(){
       
    return mongoClient.getDatabaseNames();
   
   }
   
   
    /**
    * metodo che viene utilizzato per ottenere un db, restituito se già esistente, creato qualora non esistesse
    * 
    * @param name nome del db
    * @return database 
    */
   private DB getDB(String name){
   
       DB database=null;
          database=(DB)map.get(name);
       
       if (database==null){
           database=mongoClient.getDB(name);
           map.put(name,database);
       }
       
       
       return database;
   
   }
   
     /**
    * metodo che scrive le informazioni sul db
    * 
    * @param collection collezione in cui inserire l'elemento
    * @param objNew elemento json da inserire
    */
   private void inserisci(DBCollection collection, BasicDBObject objNew) {
        BasicDBObject objOld=null,objUpd;
       boolean b=false;
       String idObjNew=objNew.getString("_id");
        logger.debug("dentro metodo inserisci:"+objNew.toString());
       
       try{
            if(idObjNew!=null&&(idObjNew.equals("campi")||idObjNew.equals("attributi"))){
            
                objOld=(BasicDBObject) collection.findOne(new BasicDBObject("_id",idObjNew));
               // logger.debug(objNew.toString());
                if(objOld!=null){
                    
              //  logger.debug(objOld.toString());
                b=objNew.equals(objOld);
                if(!b){
                    logger.debug("diversi");
                    objUpd=composeDocuments(objOld, objNew);
                    //logger.debug("qwe "+objUpd.toString());
                    //logger.debug("tre "+objNew.toString());
                     
                        collection.save(objUpd);
                   
                }
            
            }else{
                collection.insert(objNew);
                }
            
            }
            else{
                logger.debug("not present"+objNew.toString());
            collection.insert(objNew);
            }
        }
        catch(MongoException ex){
            logger.error("Error in operation: ", ex);
           // errorState=true;
        }
        catch(java.lang.NullPointerException ex){
            logger.error("Error in operation: ", ex);
           // errorState=true;
        }
        catch(java.lang.IllegalStateException ex){
            logger.error("Error in operation: ", ex);
           // errorState=true;
            
        }
    }
   
   private void insertXMLString(DB dataBase, Object elemToInsert) {
   
       String stringXML=(String)elemToInsert;
       DBCollection collection;
       ParserXML parser=new ParserXML(stringXML);
       String collName=null;
       ArrayList<BasicDBObject> list=new ArrayList();
       Element root=parser.getRootElement();
       logger.debug("name root element: "+root.getName());
       collName=parser.getElementAttributeContent(root.getName(), "name");
       if(collName==null){
           collName=parser.getElementContentInStructure("publicationId");
        }
       
       logger.debug(" nome collezione: "+collName);
    //   long inizio, fine;
    //   String str;
    //   FileOutputStream fos= new FileOutputStream("inserimento.csv", true);
        try {
            list=new UtilityJson().XMLMultiLevelToJSON2(stringXML);
        } catch (IOException ex) {
            logger.error("Error in operation: ", ex);
        }
       
         collection=this.getCollection(dataBase, collName);
     //  collection=dataBase.getCollection(collName);
     //  inizio=System.currentTimeMillis();
      for(int numElem=0;numElem<list.size();numElem++){
       this.inserisci(collection, list.get(numElem));
       }
     //  fine=System.currentTimeMillis();
     //  str="tempo inserimento: "+(fine-inizio)+"\n";
     //  fos.write(str.getBytes());
       
       }
   
   private void insertDocJSON(DB dataBase, Object elemToInsert) {
   
       String docJSON=(String)elemToInsert;
       DBCollection collection;
       BasicDBObject obj=(BasicDBObject) JSON.parse(docJSON);
       String collName=null;
      
       collName=obj.getString("name");

       
       logger.debug(" dentro insertDocJSON");
    //   long inizio, fine;
    //   String str;
    //   FileOutputStream fos= new FileOutputStream("inserimento.csv", true);
        collection=this.getCollection(dataBase, collName);
     //  collection=dataBase.getCollection(collName);
     //  inizio=System.currentTimeMillis();
       this.inserisci(collection, obj);
      
     //  fine=System.currentTimeMillis();
     //  str="tempo inserimento: "+(fine-inizio)+"\n";
     //  fos.write(str.getBytes());
       
       }
   
    @Override
   public void insertSensing(BigDataParameterContainer container){
       this.insertSensing(container.getElemToInsert(),container.getType());
    }
   /**
    * metodo che viene utilizzato dalla parte di sensoristica per l'inserimeno di documenti
    * @param elementToInsert elemento da inserire
    * @param type questa variabile viene utilizzata per distinguere i vari tipi di oggetti da inserire
    */
    
   private void insertSensing(Object elementToInsert, TypeOfElement type){
       
       String dbName="sensing";
       logger.debug("dentro metodo insertSensing, nome del db della sensoristica: "+dbName);
       DB dataBase=mongoClient.getDB(dbName);
       
       
       switch(type){
       
           case STRINGXML:
       
           this.insertXMLString(dataBase, elementToInsert);
               break;
           case DOCJSON:
       
           this.insertDocJSON(dataBase, elementToInsert);
               break;    
               
           default: 
               logger.error("errore caso non riconosciuto");
       
       }
       
   }   
   
   // @Override
   private void insertOnDB(String dbName,Object elementToInsert, TypeOfElement type){
       
      
       logger.debug("dentro metodo insertinDB, nome del db: "+dbName);
       DB dataBase=mongoClient.getDB(dbName);
       
       
       switch(type){
       
           case STRINGXML:
       
           this.insertXMLString(dataBase, elementToInsert);
               break;
           case DOCJSON:
       
           this.insertDocJSON(dataBase, elementToInsert);
               break;    
               
           default: 
               logger.error("errore caso non riconosciuto");
       
       }
       
   }   
   
    @Override
   public void insertOnDB(BigDataParameterContainer container){
       this.insertOnDB(container.getDbName(), container.getElemToInsert(), container.getType());
      
       
   }   
     
    @Override
    public void insertVMLog(BigDataParameterContainer container){
       this.insertVMLog(container.getElemToInsert(),container.getType());
    }
        /**
    * metodo che viene utilizzato dalla parte di monitoring per l'inserimeno di documenti riguardanti il logging delle macchine
    * @param elementToInsert elemento da inserire
    * @param type questa variabile viene utilizzata per distinguere i vari tipi di oggetti da inserire
    */
    
   private void insertVMLog(Object elementToInsert, TypeOfElement type){
       
       String nomeDB="VMLogging";
       logger.debug("dentro metodo insertVMLog, nome del db della sensoristica: "+nomeDB);
       DB dataBase=mongoClient.getDB(nomeDB);
       
       switch(type){
       
           case STRINGXML:
               this.insertXMLString(dataBase, elementToInsert);
               break;
               
           case DOCJSON:
       
           this.insertDocJSON(dataBase, elementToInsert);
               break;  
               
           default: 
               logger.error("errore caso non riconosciuto");
       
       }
       
   }   
     
    @Override
   public void insertHostState(BigDataParameterContainer container){
       this.insertHostState(container.getElemToInsert(),container.getType());
    }
   
     /**
    * metodo che viene utilizzato dalla parte di monitoring per l'inserimeno di documenti riguardanti lo stato delle macchine
    * @param elementToInsert elemento da inserire
    * @param type questa variabile viene utilizzata per distinguere i vari tipi di oggetti da inserire
    */
   
   private void insertHostState(Object elementToInsert, TypeOfElement type){
       
        String nomeDB="VMState";
       logger.debug("dentro metodo insertVMLog, nome del db dello stato degli host: "+nomeDB);
       DB dataBase=mongoClient.getDB(nomeDB);
       
       switch(type){
       
           case STRINGXML:
               this.insertXMLString(dataBase, elementToInsert);
                break;
           case DOCJSON:
       
           this.insertDocJSON(dataBase, elementToInsert);
               break;
               
           default: 
               logger.error("errore caso non riconosciuto");
       
       }
       
   }   
   
   /**
     * metodo che viene utlizzato per restituire la listCollection di tutte le collezioni presenti nel db
     * @param dbName nome del database
     * @return Set di stringhe in cui sono conenute le collezioni
     */
   public Set getCollectionNames(String dbName){
        logger.debug("dentro metodo getCollectionNames sul db: "+ dbName);
        return this.getDB(dbName).getCollectionNames();
     
   }
   
        /**
     * metodo che viene utlizzato per restituire la listCollection di tutte le collezioni presenti nel db di sensing
     * @return Set di stringhe in cui sono conenute le collezioni
     */
   public Set getSensingCollectionNames(){
   
       return this.getCollectionNames("sensing");
   }
   
     /**
     * metodo che viene utlizzato per restituire la listCollection di tutte le collezioni presenti nel db di Logging delle VM
     * @return Set di stringhe in cui sono conenute le collezioni
     */
   public Set getVMLogCollectionNames(){
   
       return this.getCollectionNames("VMLogging");
   }
   
     /**
     * metodo che viene utlizzato per restituire la listCollection di tutte le collezioni presenti nel db in cui è conenuto lo stato delle VM
     * @return Set di stringhe in cui sono conenute le collezioni
     */
   public Set getVMStateCollectionNames(){
   
       return this.getCollectionNames("VMState");
   }
   
       /**
      * metodo che viene utilizzato per restituire un oggetto in base all'id
      * @param nameDB nome del dataBase
      * @param nameCollection nome Collezione
      * @param id id dell'oggetto da cercare
      * @return l'oggetto
      */
    @Override
   public DBObject findByIdString(String nameDB,String nameCollection,String id){

       logger.debug("dentro find");
       return  this.getCollection(this.getDB(nameDB), nameCollection).findOne(new BasicDBObject("_id",id));
   
//  return this.getDB(nameDB).getCollection(nameCollection).findOne(new BasicDBObject("_id",id));
   
   }
   
      /**
      * metodo che viene utilizzato per restituire un oggetto in base all'id
      * @param nameDB nome del dataBase
      * @param nameCollection nome Collezione
      * @param id id dell'oggetto da cercare
      * @return l'oggetto
      */
    @Override
   public DBObject findByObjId(String nameDB,String nameCollection,ObjectId id){
      
       logger.debug("dentro find");
      return  this.getCollection(this.getDB(nameDB), nameCollection).findOne(id);
   
   }
   
    /**
    
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti contenuti in una collezione
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @return curosore contente gli oggetti
    */
   private DBCursor findAllElementInCollection(String nameDB,String nameCollection){
       
        logger.debug("dentro find");
      return  this.getCollection(this.getDB(nameDB), nameCollection).find();
   
   }
   
   /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone) minori di una soglia
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return curosore contente gli oggetti
    */
   private DBCursor findLessThanOnCollectionToCursor(String nameDB, String nameCollection, String nameField,Object threshold){
       
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        DBCollection collezione;
        
 
    logger.debug("dentro findLessThanOnCollection chiamato su: "+nameDB);
       collezione=this.getCollection(database, nameCollection);
// collezione=database.getCollection(nameCollection);
         listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$lt", threshold)));
            }
       }
     //  orList.add(new BasicDBObject(nameField, new BasicDBObject("$lt", threshold)));
      query=new BasicDBObject("$or", orList);
        return collezione.find(query);
    }
 
   /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone) minori di una soglia
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return lista contente gli oggetti
    */
   private List<DBObject> findLessThanOnCollectionToList(String nameDB, String nameCollection, String nameField,Object threshold){
       
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        DBCollection collezione;
        
 
    logger.debug("dentro findLessThanOnCollection chiamato su: "+nameDB);
        collezione=this.getCollection(database, nameCollection);
//  collezione=database.getCollection(nameCollection);
         listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$lt", threshold)));
            }
       }
    //   orList.add(new BasicDBObject(nameField, new BasicDBObject("$lt", threshold)));
      query=new BasicDBObject("$or", orList);
        return collezione.find(query).toArray();
    }
   
    private List<DBObject> findEqualThanOnCollectionToList(String nameDB, String nameCollection, String nameField,Object threshold){
       
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        DBCollection collezione;
        
 
    logger.debug("dentro findLessThanOnCollection chiamato su: "+nameDB);
        collezione=this.getCollection(database, nameCollection);
//     collezione=database.getCollection(nameCollection);
         listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), threshold));
            }
       }
      // orList.add(new BasicDBObject(nameField, threshold));
      query=new BasicDBObject("$or", orList);
        return collezione.find(query).toArray();
    }
   
   
    /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti (contenuti in una collezone) maggiori di una soglia
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return curosore contente gli oggetti 
    */
   private DBCursor findGreaterThanOnCollectionToCursor(String nameDB, String nameCollection, String nameField,Object threshold){
       
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        DBCollection collezione;
        
 
    logger.debug("dentro findGreaterThanOnCollection chiamato su: "+nameDB);
        collezione=this.getCollection(database, nameCollection);
//   collezione=database.getCollection(nameCollection);
         listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$gt", threshold)));
            }
       }
   //    orList.add(new BasicDBObject(nameField, new BasicDBObject("$gt", threshold)));
      query=new BasicDBObject("$or", orList);
        return collezione.find(query);
   }
   
    /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti (contenuti in una collezone) maggiori di una soglia
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return lista contente gli oggetti 
    */
   private List<DBObject> findGreaterThanOnCollectionToList(String nameDB, String nameCollection, String nameField,Object threshold){
       
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        DBCollection collezione;
        
 
    logger.debug("dentro findGreaterThanOnCollection chiamato su: "+nameDB);
        collezione=this.getCollection(database, nameCollection);
//     collezione=database.getCollection(nameCollection);
         listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$gt", threshold)));
            }
       }
     //  orList.add(new BasicDBObject(nameField, new BasicDBObject("$gt", threshold)));
      query=new BasicDBObject("$or", orList);
        return collezione.find(query).toArray();
   }

   /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone) minori o uguali ad una soglia
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return curosore contente gli oggetti
    */
   private DBCursor findLessOrEqualThanOnCollectionToCursor(String nameDB, String nameCollection, String nameField,Object threshold){
  
       ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        DBCollection collezione;
        
 
    logger.debug("dentro findLessOrEqualThanOnCollection chiamato su: "+nameDB);
        collezione=this.getCollection(database, nameCollection);
//    collezione=database.getCollection(nameCollection);
         listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$lte", threshold)));
            }
       }
      // orList.add(new BasicDBObject(nameField, new BasicDBObject("$lte", threshold)));
      query=new BasicDBObject("$or", orList);
        return collezione.find(query);
   }
   
    /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone) minori o uguali ad una soglia
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return lista contente gli oggetti
    */
   private List<DBObject> findLessOrEqualThanOnCollectionToList(String nameDB, String nameCollection, String nameField,Object threshold){
  
       ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        DBCollection collezione;
        
 
    logger.debug("dentro findLessOrEqualThanOnCollection chiamato su: "+nameDB);
     
        collezione=this.getCollection(database, nameCollection);
//    collezione=database.getCollection(nameCollection);
         listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$lte", threshold)));
            }
       }
     //  orList.add(new BasicDBObject(nameField, new BasicDBObject("$lte", threshold)));
      query=new BasicDBObject("$or", orList);
        return collezione.find(query).toArray();
   }
   
   /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti (contenuti in una collezone) maggiori od uguali ad una soglia
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return curosore contente gli oggetti
    */
   private DBCursor findGreaterOrEqualThanOnCollectionToCursor(String nameDB, String collectionName, String nameField,Object threshold){
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        DBCollection collezione;
        
 
    logger.debug("dentro findGreaterOrEqualThanOnCollection chiamato su: "+nameDB);
        collezione=this.getCollection(database, collectionName);
//  collezione=database.getCollection(collectionName);
         listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$gte", threshold)));
            }
       }
      // orList.add(new BasicDBObject(nameField, new BasicDBObject("$gte", threshold)));
      query=new BasicDBObject("$or", orList);
        return collezione.find(query);
   }

    /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti (contenuti in una collezone) maggiori od uguali ad una soglia
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return lista contente gli oggetti
    */
   private List<DBObject> findGreaterOrEqualThanOnCollectionToList(String nameDB, String collectionName, String nameField,Object threshold){
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        DBCollection collezione;
        
 
    logger.debug("dentro findGreaterOrEqualThanOnCollection chiamato su: "+nameDB);
         collezione=this.getCollection(database, collectionName);
//  collezione=database.getCollection(collectionName);
         listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$gte", threshold)));
            }
       }
      // orList.add(new BasicDBObject(nameField, new BasicDBObject("$gte", threshold)));
      query=new BasicDBObject("$or", orList);
        return collezione.find(query).toArray();
   }

   
   /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone), estremi inclusi,contenuti in un range
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return curosore contente gli oggetti
    */
   private DBCursor findOnRangeWithBorderOnCollectionToCursor(String nameDB, String nameCollection, String nameField,Object minThreshold,Object maxThreshold){
    
    ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
    Set listCollection= database.getCollectionNames();
    Iterator it=listCollection.iterator();
    DBCollection collezione;
    DBCursor cursor;
    
    int index;
    logger.debug("dentro findOnRangeWithBorderOnDB chiamato su: "+nameDB);
         collezione=this.getCollection(database, nameCollection);
//  collezione=database.getCollection(nameCollection);
        listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
        if(listFieldName!=null){
            for(index=0;index<listFieldName.size();index++){
                queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject)(queryBuilder.put((String)listFieldName.get(index)).greaterThanEquals(minThreshold).and((String)listFieldName.get(index)).lessThanEquals(maxThreshold)).get());
                }  }
          //  orList.add((BasicDBObject)(queryBuilder.put(nameField).greaterThanEquals(minThreshold).and(nameField).lessThanEquals(maxThreshold)).get());
            query=new BasicDBObject("$or", orList);
          return collezione.find(query);
       
   }
   
     
   /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone), estremi inclusi,contenuti in un range
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return curosore contente gli oggetti
    */
   private List<DBObject> findOnRangeWithBorderOnCollectionToList(String nameDB, String nameCollection, String nameField,Object minThreshold,Object maxThreshold){
    
    ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
    Set listCollection= database.getCollectionNames();
    Iterator it=listCollection.iterator();
    DBCollection collezione;
    DBCursor cursor;
    
    int index;
    logger.debug("dentro findOnRangeWithBorderOnDB chiamato su: "+nameDB);
        collezione=this.getCollection(database, nameCollection);
//   collezione=database.getCollection(nameCollection);
        listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
        if(listFieldName!=null){
            for(index=0;index<listFieldName.size();index++){
                queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject)(queryBuilder.put((String)listFieldName.get(index)).greaterThanEquals(minThreshold).and((String)listFieldName.get(index)).lessThanEquals(maxThreshold)).get());
                }  }
          //  orList.add((BasicDBObject)(queryBuilder.put(nameField).greaterThanEquals(minThreshold).and(nameField).lessThanEquals(maxThreshold)).get());
            query=new BasicDBObject("$or", orList);
          return collezione.find(query).toArray();
       
   }
   
    /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone), estremi esclusi,contenuti in un range
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return curosore contente gli oggetti
    */
   private DBCursor findOnRangeWithoutBorderOnCollectionToCursor(String nameDB, String nameCollection, String nameField,Object minThreshold,Object maxThreshold){
     
    ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
    DBCollection collezione;
    DBCursor cursor;
   
   logger.debug("dentro findOnRangeWithoutBorderOnDB chiamato su: "+nameDB);
   
        collezione=this.getCollection(database, nameCollection);
        //collezione=database.getCollection(nameCollection);
        listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
        if(listFieldName!=null){
            for(int index=0;index<listFieldName.size();index++){
               queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject) queryBuilder.put((String)listFieldName.get(index)).greaterThan(minThreshold).and((String)listFieldName.get(index)).lessThan(maxThreshold).get());
                }}
        // orList.add((BasicDBObject) queryBuilder.put(nameField).greaterThan(minThreshold).and(nameField).lessThan(maxThreshold).get());
            query=new BasicDBObject("$or", orList);
            return collezione.find(query);
        
   }
   
     /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone), estremi esclusi,contenuti in un range
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return curosore contente gli oggetti
    */
   private List<DBObject> findOnRangeWithoutBorderOnCollectionToList(String nameDB, String nameCollection, String nameField,Object minThreshold,Object maxThreshold){
     
    ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
    DBCollection collezione;
    
   logger.debug("dentro findOnRangeWithoutBorderOnDB chiamato su: "+nameDB);
   
        collezione=this.getCollection(database, nameCollection);
       // collezione=database.getCollection(nameCollection);
        listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
        if(listFieldName!=null){
            for(int index=0;index<listFieldName.size();index++){
               queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject) queryBuilder.put((String)listFieldName.get(index)).greaterThan(minThreshold).and((String)listFieldName.get(index)).lessThan(maxThreshold).get());
                }}
        // orList.add((BasicDBObject) queryBuilder.put(nameField).greaterThan(minThreshold).and(nameField).lessThan(maxThreshold).get());
            query=new BasicDBObject("$or", orList);
            return collezione.find(query).toArray();
        
   }
   
     /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone), estremo inferiore incluso,contenuti in un range
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return curosore contente gli oggetti
    */
   private DBCursor findOnRangeWithBorderInfOnCollectionToCursor(String nameDB, String nameCollection, String nameField,Object minThreshold,Object maxThreshold){
        
    ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
    DBCollection collezione;
    
    logger.debug("dentro findOnRangeWithBorderInfOnDB chiamato su: "+nameDB);
   
        collezione=this.getCollection(database, nameCollection);
       // collezione=database.getCollection(nameCollection);
        listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
        if(listFieldName!=null){
            for(int index=0;index<listFieldName.size();index++){
                queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject) queryBuilder.put((String)listFieldName.get(index)).greaterThanEquals(minThreshold).and((String)listFieldName.get(index)).lessThan(maxThreshold).get());
                }}
           // orList.add((BasicDBObject) queryBuilder.put(nameField).greaterThanEquals(minThreshold).and(nameField).lessThan(maxThreshold).get());
            query=new BasicDBObject("$or", orList);
          //  System.out.println(query);
            return collezione.find(query);
        
   }
     /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone), estremo inferiore incluso,contenuti in un range
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return curosore contente gli oggetti
    */
   private List<DBObject>  findOnRangeWithBorderInfOnCollectionToList(String nameDB, String nameCollection, String nameField,Object minThreshold,Object maxThreshold){
        
    ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
    DBCollection collezione;
    
    logger.debug("dentro findOnRangeWithBorderInfOnDB chiamato su: "+nameDB);
   
        collezione=this.getCollection(database, nameCollection);
      //  collezione=database.getCollection(nameCollection);
        listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
        if(listFieldName!=null){
            for(int index=0;index<listFieldName.size();index++){
                queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject) queryBuilder.put((String)listFieldName.get(index)).greaterThanEquals(minThreshold).and((String)listFieldName.get(index)).lessThan(maxThreshold).get());
                }}
          //  orList.add((BasicDBObject) queryBuilder.put(nameField).greaterThanEquals(minThreshold).and(nameField).lessThan(maxThreshold).get());
            query=new BasicDBObject("$or", orList);
           // System.out.println(query);
            return collezione.find(query).toArray();
        
   }
   
     /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone), estremo superiore incluso,contenuti in un range
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return curosore contente gli oggetti
    */
   private DBCursor findOnRangeWithBorderSupOnCollectionToCursor(String nameDB, String nameCollection, String nameField,Object minThreshold,Object maxThreshold){
   
       ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
   DBCollection collezione;
   logger.debug("dentro findOnRangeWithBorderInfOnDB chiamato su: "+nameDB);
        
        collezione=this.getCollection(database, nameCollection);
       // collezione=database.getCollection(nameCollection);
        listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
        if(listFieldName!=null){
            for(int index=0;index<listFieldName.size();index++){
                queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject) queryBuilder.put((String)listFieldName.get(index)).greaterThan(minThreshold).and((String)listFieldName.get(index)).lessThanEquals(maxThreshold).get());
                }}
        // orList.add((BasicDBObject) queryBuilder.put(nameField).greaterThan(minThreshold).and(nameField).lessThanEquals(maxThreshold).get());
            query=new BasicDBObject("$or", orList);
         //   System.out.println(query);
             return collezione.find(query);
       
   }
   
      /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in una collezone), estremo superiore incluso,contenuti in un range
    * @param nameDB nome del dataBase
    * @param nameCollection nome Collezione
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return curosore contente gli oggetti
    */
   private List<DBObject> findOnRangeWithBorderSupOnCollectionToList(String nameDB, String nameCollection, String nameField,Object minThreshold,Object maxThreshold){
   
       ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
   DBCollection collezione;
   logger.debug("dentro findOnRangeWithBorderInfOnDB chiamato su: "+nameDB);
     
        collezione=this.getCollection(database, nameCollection);
       // collezione=database.getCollection(nameCollection);
        listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
        if(listFieldName!=null){
            for(int index=0;index<listFieldName.size();index++){
                queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject) queryBuilder.put((String)listFieldName.get(index)).greaterThan(minThreshold).and((String)listFieldName.get(index)).lessThanEquals(maxThreshold).get());
                }}
         //orList.add((BasicDBObject) queryBuilder.put(nameField).greaterThan(minThreshold).and(nameField).lessThanEquals(maxThreshold).get());
            query=new BasicDBObject("$or", orList);
          //  System.out.println(query);
             return collezione.find(query).toArray();
       
   }
  
   
  public BasicDBList getListFieldName(String nameDB, String nomeField,String collectionName){
      return this.getListField( nameDB, nomeField, collectionName,"campi");
  } 
   /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in un DataBase) minori di una soglia
    * @param nameDB nome del dataBase
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return lista contente gli oggetti
    * 
    */
    private List<DBObject> findLessThanOnDB(String nameDB,String nameField,Object Threshold){  
    
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;
        DB database=this.getDB(nameDB);
        Set listCollection= database.getCollectionNames();
        Iterator it=listCollection.iterator();
        DBCollection collezione;
        DBCursor cursor;
        List listElem=new ArrayList();
        String collectionName;
        
        logger.debug("dentro findLessThanOnDB chiamato su: "+nameDB);
 
    while(it.hasNext()){
        collectionName=(String)it.next();
        collezione=this.getCollection(database, collectionName);
// collezione=database.getCollection(collectionName);
        listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
      if(listFieldName!=null){ 
        for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$lt", Threshold)));
            }
      }
     //   orList.add(new BasicDBObject(nameField, new BasicDBObject("$lt", Threshold)));
        query=new BasicDBObject("$or", orList);
        cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
        
    }
    return listElem;
   }
    
 private List<DBObject> findEqualThanOnDB(String nameDB,String nameField,Object Threshold){  
    
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;
        DB database=this.getDB(nameDB);
        Set listCollection= database.getCollectionNames();
        Iterator it=listCollection.iterator();
        DBCollection collezione;
        DBCursor cursor;
        List listElem=new ArrayList();
        String collectionName;
        
        logger.debug("dentro findLessThanOnDB chiamato su: "+nameDB);
 
    while(it.hasNext()){
        collectionName=(String)it.next();
        collezione=this.getCollection(database, collectionName);
    // collezione=database.getCollection(collectionName);
        listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
      if(listFieldName!=null){ 
        for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index),Threshold));
            }
      }
       // orList.add(new BasicDBObject(nameField, Threshold));
        query=new BasicDBObject("$or", orList);
        cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
        
    }
    return listElem;
   }
       
    
        /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in un DataBase) maggiori di una soglia
    * @param nameDB nome del dataBase
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return lista contente gli oggetti
    * 
    */
    private List<DBObject> findGreaterThanOnDB(String nameDB,String nameField,Object Threshold){  
    
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        Set listCollection= database.getCollectionNames();
        Iterator it=listCollection.iterator();
        DBCollection collezione;
        DBCursor cursor;
        List listElem=new ArrayList();
        String collectionName;
 
         logger.debug("dentro findGreaterThanOnDB chiamato su: "+nameDB);
        
    while(it.hasNext()){
        collectionName=(String)it.next();
        collezione=this.getCollection(database, collectionName);
//collezione=database.getCollection(collectionName);
        listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$gt", Threshold)));
            }
      
       }
       //  orList.add(new BasicDBObject(nameField, new BasicDBObject("$gt", Threshold)));
          
         query=new BasicDBObject("$or", orList);
        cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
        
    }
    return listElem;
   }
    
      /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in un DataBase) minori o uguali ad una soglia
    * @param nameDB nome del dataBase
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return lista contente gli oggetti
    * 
    */
     private List<DBObject> findLessOrEqualThanOnDB(String nameDB,String nameField,Object Threshold){  
    
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        Set listCollection= database.getCollectionNames();
        Iterator it=listCollection.iterator();
        DBCollection collezione;
        DBCursor cursor;
        List listElem=new ArrayList();
        String collectionName;
 
        logger.debug("dentro findLessOrEqualThanOnDB chiamato su: "+nameDB);
        
    while(it.hasNext()){
        collectionName=(String)it.next();
        collezione=this.getCollection(database, collectionName);
    // collezione=database.getCollection(collectionName);
        listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$lte", Threshold)));
            }
        query=new BasicDBObject("$or", orList);
       }
      //  orList.add(new BasicDBObject(nameField, new BasicDBObject("$lte", Threshold)));
       query=new BasicDBObject("$or", orList);
        cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
        
    }
    return listElem;
   }
     
      /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in un DataBase) maggiori o uguali ad una soglia
    * @param nameDB nome del dataBase
    * @param nameField nome del campo da ricercare
    * @param threshold valore della soglia
    * @return lista contente gli oggetti
    * 
    */
  private List<DBObject> findGreaterOrEqualThanOnDB(String nameDB,String nameField,Object Threshold){  
    
    
    ArrayList orList = new ArrayList(); 
    BasicDBList listFieldName;  
    BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
    DB database=this.getDB(nameDB);
    Set listCollection= database.getCollectionNames();
    Iterator it=listCollection.iterator();
    DBCollection collezione;
    DBCursor cursor;
    List listElem=new ArrayList();
    String collectionName;
 
    logger.debug("dentro findGreaterOrEqualThanOnDB chiamato su: "+nameDB);
    
    while(it.hasNext()){
        collectionName=(String)it.next();
        collezione=this.getCollection(database, collectionName);
//collezione=database.getCollection(collectionName);
        listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$gte", Threshold)));
            }
        
       }
   //    orList.add(new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold)));
      query=new BasicDBObject("$or", orList);
        cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
        
    }
    return listElem;
   }
  
     /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in un DataBase), estremi inclusi, appartenenti ad un certo range
    * @param nameDB nome del dataBase
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return lista contente gli oggetti
    * 
    */
   private List<DBObject> findOnRangeWithBorderOnDB(String nameDB, String nameField,Object minThreshold,Object maxThreshold){  
    
    ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
    Set listCollection= database.getCollectionNames();
    Iterator it=listCollection.iterator();
    DBCollection collezione;
    DBCursor cursor;
    List listElem=new ArrayList();
    String collectionName;
    int index;
    logger.debug("dentro findOnRangeWithBorderOnDB chiamato su: "+nameDB);
    while(it.hasNext()){
        collectionName=(String)it.next();
        collezione=this.getCollection(database, collectionName);
//collezione=database.getCollection(collectionName);
        listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
        if(listFieldName!=null){
            for(index=0;index<listFieldName.size();index++){
                queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject)(queryBuilder.put((String)listFieldName.get(index)).greaterThanEquals(minThreshold).and((String)listFieldName.get(index)).lessThanEquals(maxThreshold)).get());
                }  }
        //    orList.add((BasicDBObject)(queryBuilder.put(nameField).greaterThanEquals(minThreshold).and(nameField).lessThanEquals(maxThreshold)).get());
            query=new BasicDBObject("$or", orList);
            cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
      
    }
    return listElem;
   }
   
         /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in un DataBase), estremi esclusi, appartenenti ad un certo range
    * @param nameDB nome del dataBase
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return lista contente gli oggetti
    * 
    */
  private List<DBObject> findOnRangeWithoutBorderOnDB(String nameDB, String nameField,Object minThreshold,Object maxThreshold){  
    
    ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
   QueryBuilder queryBuilder = new QueryBuilder();
         
    BasicDBObject query;
    DB database=this.getDB(nameDB);
    Set listCollection= database.getCollectionNames();
    Iterator it=listCollection.iterator();
    DBCollection collezione;
    DBCursor cursor;
    List listElem=new ArrayList();
    String collectionName;
   logger.debug("dentro findOnRangeWithoutBorderOnDB chiamato su: "+nameDB);
   
    while(it.hasNext()){
        collectionName=(String)it.next();
        collezione=this.getCollection(database, collectionName);
// collezione=database.getCollection(collectionName);
        listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
        if(listFieldName!=null){
            for(int index=0;index<listFieldName.size();index++){
               queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject) queryBuilder.put((String)listFieldName.get(index)).greaterThan(minThreshold).and((String)listFieldName.get(index)).lessThan(maxThreshold).get());
                }}
        // orList.add((BasicDBObject) queryBuilder.put(nameField).greaterThan(minThreshold).and(nameField).lessThan(maxThreshold).get());
            query=new BasicDBObject("$or", orList);
            cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
        
    }
    return listElem;
   }
 
       /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in un DataBase), estremo inferiore incluso, appartenenti ad un certo range
    * @param nameDB nome del dataBase
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return lista contente gli oggetti
    * 
    */
   private List<DBObject> findOnRangeWithBorderInfOnDB(String nameDB, String nameField,Object minThreshold,Object maxThreshold){  
    
    ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
    Set listCollection= database.getCollectionNames();
    Iterator it=listCollection.iterator();
    DBCollection collezione;
    DBCursor cursor;
    List listElem=new ArrayList();
    String collectionName;
     logger.debug("dentro findOnRangeWithBorderInfOnDB chiamato su: "+nameDB);
   
    while(it.hasNext()){
        collectionName=(String)it.next();
        collezione=this.getCollection(database, collectionName);
//  collezione=database.getCollection(collectionName);
        listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
        if(listFieldName!=null){
            for(int index=0;index<listFieldName.size();index++){
                queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject) queryBuilder.put((String)listFieldName.get(index)).greaterThanEquals(minThreshold).and((String)listFieldName.get(index)).lessThan(maxThreshold).get());
                }}
           // orList.add((BasicDBObject) queryBuilder.put(nameField).greaterThanEquals(minThreshold).and(nameField).lessThan(maxThreshold).get());
            query=new BasicDBObject("$or", orList);
           // System.out.println(query);
            cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
        
    }
    return listElem;
   }
 
         /**
    * metodo che viene utilizzato per restituire un cursore contenenti tutti gli oggetti(contenuti in un DataBase), estremo superiore incluso, appartenenti ad un certo range
    * @param nameDB nome del dataBase
    * @param nameField nome del campo da ricercare
    * @param minThreshold soglia inferiore
    * @param maxThreshold soglia superiore
    * @return lista contente gli oggetti
    * 
    */
  private List<DBObject> findOnRangeWithBorderSupOnDB(String nameDB, String nameField,Object minThreshold,Object maxThreshold){  
    
    ArrayList orList = new ArrayList();
    BasicDBList listFieldName;
    QueryBuilder queryBuilder = new QueryBuilder();
    BasicDBObject query;
    DB database=this.getDB(nameDB);
    Set listCollection= database.getCollectionNames();
    Iterator it=listCollection.iterator();
    DBCollection collezione;
    DBCursor cursor;
    List listElem=new ArrayList();
    String collectionName;
    logger.debug("dentro findOnRangeWithBorderInfOnDB chiamato su: "+nameDB);
     
    while(it.hasNext()){
        collectionName=(String)it.next();
        collezione=this.getCollection(database, collectionName);
// collezione=database.getCollection(collectionName);
        listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
        if(listFieldName!=null){
            for(int index=0;index<listFieldName.size();index++){
                queryBuilder=queryBuilder.start();
                orList.add((BasicDBObject) queryBuilder.put((String)listFieldName.get(index)).greaterThan(minThreshold).and((String)listFieldName.get(index)).lessThanEquals(maxThreshold).get());
                }}
         //orList.add((BasicDBObject) queryBuilder.put(nameField).greaterThan(minThreshold).and(nameField).lessThanEquals(maxThreshold).get());
            query=new BasicDBObject("$or", orList);
           // System.out.println(query);
            cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
        
    }
    return listElem;
   }
 
   
  private List<DBObject> sensingFindLessThan(String nameFiled, Object threshold){
      
      return this.findLessThanOnDB("sensing", nameFiled, threshold);
  
  }
  private List<DBObject> sensingFindEqualThan(String nameFiled, Object threshold){
      
      return this.findEqualThanOnDB("sensing", nameFiled, threshold);
  
  }
  
  private List<DBObject> sensingFindLessOrEqualThan(String nameFiled, Object threshold){
      
      return this.findLessOrEqualThanOnDB("sensing", nameFiled, threshold);
  
  }
  
  private List<DBObject> sensingFindGreaterThan(String nameFiled, Object threshold){
      
      return this.findGreaterThanOnDB("sensing", nameFiled, threshold);
  
  }
  
  private List<DBObject> sensingFindGreaterOrEqualThan(String nameFiled, Object threshold){
      
      return this.findGreaterOrEqualThanOnDB("sensing", nameFiled, threshold);
  
  }
  
  private List<DBObject> sensingFindOnRangeWithBorder(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithBorderOnDB("sensing", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> sensingFindOnRangeWithoutBorder(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithoutBorderOnDB("sensing", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> sensingFindOnRangeWithBorderInf(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithBorderInfOnDB("sensing", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> sensingFindOnRangeWithBorderSup(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithBorderSupOnDB("sensing", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> VMLogFindLessThan(String nameFiled, Object threshold){
      
      return this.findLessThanOnDB("VMLogging", nameFiled, threshold);
  
  }
  
  private List<DBObject> VMLogFindEqualThan(String nameFiled, Object threshold){
      
      return this.findEqualThanOnDB("VMLogging", nameFiled, threshold);
  
  }
    
  private List<DBObject> VMLogFindLessOrEqualThan(String nameFiled, Object threshold){
      
      return this.findLessOrEqualThanOnDB("VMLogging", nameFiled, threshold);
  
  }
  
  private List<DBObject> VMLogFindGreaterThan(String nameFiled, Object threshold){
      
      return this.findGreaterThanOnDB("VMLogging", nameFiled, threshold);
  
  }
  
  private List<DBObject> VMLogFindGreaterOrEqualThan(String nameFiled, Object threshold){
      
      return this.findGreaterOrEqualThanOnDB("VMLogging", nameFiled, threshold);
  
  }
  
  private List<DBObject> VMLogFindOnRangeWithBorder(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithBorderOnDB("VMLogging", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> VMLogFindOnRangeWithoutBorder(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithoutBorderOnDB("VMLogging", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> VMLogFindOnRangeWithBorderInf(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithBorderInfOnDB("VMLogging", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> VMLogFindOnRangeWithBorderSup(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithBorderSupOnDB("VMLogging", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> VMStateFindLessThan(String nameFiled, Object threshold){
      
      return this.findLessThanOnDB("VMState", nameFiled, threshold);
  
  }
  
  private List<DBObject> VMStateFindEqualThan(String nameFiled, Object threshold){
      
      return this.findEqualThanOnDB("VMState", nameFiled, threshold);
  
  }
  
  private List<DBObject> VMStateFindLessOrEqualThan(String nameFiled, Object threshold){
      
      return this.findLessOrEqualThanOnDB("VMState", nameFiled, threshold);
  
  }
  
  private List<DBObject> VMStateFindGreaterThan(String nameFiled, Object threshold){
      
      return this.findGreaterThanOnDB("VMState", nameFiled, threshold);
  
  }
  
  private List<DBObject> VMStateFindGreaterOrEqualThan(String nameFiled, Object threshold){
      
      return this.findGreaterOrEqualThanOnDB("VMState", nameFiled, threshold);
  
  }
  
  private List<DBObject> VMStateFindOnRangeWithBorder(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithBorderOnDB("VMState", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> VMStateFindOnRangeWithoutBorder(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithoutBorderOnDB("VMState", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> VMStateFindOnRangeWithBorderInf(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithBorderInfOnDB("VMState", nameField, minThreshold, maxThreshold);
  
  }
  
  private List<DBObject> VMStateFindOnRangeWithBorderSup(String nameField,Object minThreshold,Object maxThreshold){
  
      return this.findOnRangeWithBorderSupOnDB("VMState", nameField, minThreshold, maxThreshold);
  
  }
   
   @Override
    public List<DBObject> findOnSensingDB(String nameFiled, Object thresholdMin, Object thresholdMax, BigDataMethodName name) {

        
        switch(name){
       
           case findLessThan:
                return this.sensingFindLessThan(nameFiled, thresholdMin);
                
           case findGreaterThan:
                return this.sensingFindGreaterThan(nameFiled, thresholdMin);
                
           case findLessOrEqualThan:
                return this.sensingFindLessOrEqualThan(nameFiled, thresholdMin);
                
           case findGreaterOrEqualThan:
                return this.sensingFindGreaterOrEqualThan(nameFiled, thresholdMin);

           case findOnRangeWithBorder:
                return this.sensingFindOnRangeWithBorder(nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithoutBorder:
                return this.sensingFindOnRangeWithoutBorder(nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithBorderSup:
                return this.sensingFindOnRangeWithBorderSup(nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithBorderInf:
                return this.sensingFindOnRangeWithBorderInf(nameFiled, thresholdMin, thresholdMax);
           case equal:
                return this.sensingFindEqualThan(nameFiled, thresholdMin);
               
           default: 
               logger.error("errore caso non riconosciuto");
        
        throw new UnsupportedOperationException("Not supported yet.");
        }  
    }
   
    /**
     * metodo utilizzato per fare una find su un generico db
     * @param nameDB nome del DB
     * @param nameFiled nome campo
     * @param thresholdMin soglia inferiore
     * @param thresholdMax soglia superiore, null in caso si usino i metodi che richiedono una singola soglia
     * @param nameMtd nome del metodo
     * @return lista contente gli oggetti
     */
      private List<DBObject> findOnDB(String nameDB,String nameFiled, Object thresholdMin, Object thresholdMax, BigDataMethodName nameMtd) {

        logger.debug("dentro findon Db");
        switch(nameMtd){
       
           case findLessThan:
                return this.findLessThanOnDB(nameDB,nameFiled, thresholdMin);
                
           case findGreaterThan:
                return this.findGreaterThanOnDB(nameDB,nameFiled, thresholdMin);
                
           case findLessOrEqualThan:
                return this.findLessOrEqualThanOnDB(nameDB,nameFiled, thresholdMin);
                
           case findGreaterOrEqualThan:
                return this.findGreaterOrEqualThanOnDB(nameDB,nameFiled, thresholdMin);

           case findOnRangeWithBorder:
                return this.findOnRangeWithBorderOnDB(nameDB,nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithoutBorder:
                return this.findOnRangeWithoutBorderOnDB(nameDB,nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithBorderSup:
                return this.findOnRangeWithBorderSupOnDB(nameDB,nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithBorderInf:
                return this.findOnRangeWithBorderInfOnDB(nameDB,nameFiled, thresholdMin, thresholdMax);
           case equal:
                return this.findEqualThanOnDB(nameDB,nameFiled, thresholdMin);
               
           default: 
               logger.error("errore caso non riconosciuto");
        
        throw new UnsupportedOperationException("Not supported yet.");
        }  
    }
   
    
       private List<DBObject> findOnCollection(String nameDB,String nameCollection,String nameFiled, Object thresholdMin, Object thresholdMax, BigDataMethodName nameMtd) {

        logger.debug("dentro findon Dbbbbbbbbbbb");
        switch(nameMtd){
       
           case findLessThan:
                return this.findLessThanOnCollectionToList(nameDB, nameCollection, nameFiled, thresholdMin);
                
           case findGreaterThan:
                return this.findGreaterThanOnCollectionToList(nameDB, nameCollection, nameFiled, thresholdMin);
                
           case findLessOrEqualThan:
                return this.findLessOrEqualThanOnCollectionToList(nameDB, nameCollection, nameFiled, thresholdMin);
                
           case findGreaterOrEqualThan:
                return this.findGreaterOrEqualThanOnCollectionToList(nameDB, nameCollection, nameFiled, thresholdMin);

           case findOnRangeWithBorder:
                return this.findOnRangeWithBorderInfOnCollectionToList(nameDB, nameCollection, nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithoutBorder:
                return this.findOnRangeWithoutBorderOnCollectionToList(nameDB, nameCollection, nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithBorderSup:
                return this.findOnRangeWithBorderSupOnCollectionToList(nameDB, nameCollection, nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithBorderInf:
                return this.findOnRangeWithBorderInfOnCollectionToList(nameDB, nameCollection, nameFiled, thresholdMin, thresholdMax);
           case equal:
                return this.findEqualThanOnCollectionToList(nameDB, nameCollection, nameFiled, thresholdMin);
               
           default: 
               logger.error("errore caso non riconosciuto");
        
        throw new UnsupportedOperationException("Not supported yet.");
        }  
    }
   
      
    @Override
      public List<DBObject> find(BigDataParameterContainer struct) {
          
            ArrayList nameFields=struct.getFields();
            ArrayList thresholds=struct.getThresholds();
            BigDataMethodName method=struct.getMethod();
            String dbName=struct.getDbName();
            String collectionName=struct.getCollectionName();


          if((collectionName==null)&&(thresholds.size()==2)){
            return this.findOnDB(dbName, ((String)nameFields.get(0)), thresholds.get(0), thresholds.get(1), method);
          }
          else
              if((collectionName==null)&&(thresholds.size()==1)){
            return this.findOnDB(dbName, ((String)nameFields.get(0)), thresholds.get(0), null, method);
          }
          else
              if((collectionName!=null)&&(thresholds.size()==2)){
          return this.findOnCollection(dbName,collectionName,((String)nameFields.get(0)), thresholds.get(0), thresholds.get(1), method);
          }
          else{
          return this.findOnCollection(dbName,collectionName,((String)nameFields.get(0)), thresholds.get(0), null, method);
          }
       
    }
   
    public List<DBObject> findOnDB(String nameDB,BasicDBObject query){
          
         DB database=this.getDB(nameDB);
         Set listCollection= database.getCollectionNames();
         Iterator it=listCollection.iterator();
         DBCollection collezione;
         DBCursor cursor;
         List listElem=new ArrayList();
        String collectionName;
        logger.debug("dentro findOnDB chiamato su: "+nameDB);
   
    while(it.hasNext()){
        collectionName=(String)it.next();
        collezione=this.getCollection(database, collectionName);
             //collezione=database.getCollection(collectionName);
        cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
        
        }
    return listElem;
      
      
      }
      
      public List<DBObject> findOnCollection(String nameDB,String collectionName,BasicDBObject query){
          
         DB database=this.getDB(nameDB);
         DBCollection collezione;
         DBCursor cursor;
         List listElem=new ArrayList();
        
        logger.debug("dentro findOnCollection chiamato su: "+nameDB);
             collezione=this.getCollection(database, collectionName);
       // collezione=database.getCollection(collectionName);
        cursor=collezione.find(query);
        listElem.addAll(cursor.toArray());
        
        return listElem;
      
      
      }
      
  
    @Override
    public List<DBObject> findOnVMLogDB(String nameFiled, Object thresholdMin, Object thresholdMax, BigDataMethodName name) {

        
        switch(name){
       
           case findLessThan:
                return this.VMLogFindLessThan(nameFiled, thresholdMin);
                
           case findGreaterThan:
                return this.VMLogFindGreaterThan(nameFiled, thresholdMin);
                
           case findLessOrEqualThan:
                return this.VMLogFindLessOrEqualThan(nameFiled, thresholdMin);
                
           case findGreaterOrEqualThan:
                return this.VMLogFindGreaterOrEqualThan(nameFiled, thresholdMin);

           case findOnRangeWithBorder:
                return this.VMLogFindOnRangeWithBorder(nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithoutBorder:
                return this.VMLogFindOnRangeWithoutBorder(nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithBorderSup:
                return this.VMLogFindOnRangeWithBorderSup(nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithBorderInf:
                return this.VMLogFindOnRangeWithBorderInf(nameFiled, thresholdMin, thresholdMax);
     
           case equal:
                return this.VMLogFindEqualThan(nameFiled, thresholdMin);
               
           default: 
               logger.error("errore caso non riconosciuto");
        
        throw new UnsupportedOperationException("Not supported yet.");
        }  
    }
    
     @Override
    public List<DBObject> findOnHostStateDB(String nameFiled, Object thresholdMin, Object thresholdMax, BigDataMethodName name) {

        
        switch(name){
       
           case findLessThan:
                return this.VMStateFindLessThan(nameFiled, thresholdMin);
                
           case findGreaterThan:
                return this.VMStateFindGreaterThan(nameFiled, thresholdMin);
                
           case findLessOrEqualThan:
                return this.VMStateFindLessOrEqualThan(nameFiled, thresholdMin);
                
           case findGreaterOrEqualThan:
                return this.VMStateFindGreaterOrEqualThan(nameFiled, thresholdMin);

           case findOnRangeWithBorder:
                return this.VMStateFindOnRangeWithBorder(nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithoutBorder:
                return this.VMStateFindOnRangeWithoutBorder(nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithBorderSup:
                return this.VMStateFindOnRangeWithBorderSup(nameFiled, thresholdMin, thresholdMax);

           case findOnRangeWithBorderInf:
                return this.VMStateFindOnRangeWithBorderInf(nameFiled, thresholdMin, thresholdMax);

           case equal:
                return this.VMStateFindEqualThan(nameFiled, thresholdMin);
               
           default: 
               logger.error("errore caso non riconosciuto");
               throw new UnsupportedOperationException("Not supported yet.");
        }  
    }
   
     @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void shutdown(){
        this.close();
    }
    
     @Override
    public void setOwner(Agent owner) {
        this.owner=owner;
    }

    @Override
    public void shutdownPluginInstance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
private ArrayList calcOnField(String nameDB,String collectionName, String nameField,long inizialTime, long finalTime,String opName,String operation){
    
    collectionName=collectionName.replaceAll("-", "__");
    DB database=this.getDB(nameDB);
    DBCollection collezione;
    BasicDBList listFieldName=this.getListFieldName(nameDB, nameField, collectionName);
    ArrayList<DBObject> list=new ArrayList();
    int index;
    DBObject match,fields,project,groupFields,group,elemToinsert;
    List<DBObject> pipeline;
    AggregationOutput output;
    Iterator cursore;

    try{ 
     collezione=this.getCollection(database, collectionName);
   //  collezione=database.getCollection(collectionName);
     match = new BasicDBObject("$match",new BasicDBObject("insertTimestamp", new BasicDBObject("$gt", inizialTime).append("$lte", finalTime)));
     fields = new BasicDBObject("_id", 0);
logger.debug("elem match "+match.toString());
for( index=0;index<listFieldName.size();index++){
            fields.put((String)listFieldName.get(index),1);
            }
 project = new BasicDBObject("$project", fields );
 
for(index=0;index<listFieldName.size();index++){
        groupFields = new BasicDBObject("_id",(String)listFieldName.get(index) );
        groupFields.put(opName, new BasicDBObject( operation, "$"+(String)listFieldName.get(index)));
        group = new BasicDBObject("$group", groupFields);
        pipeline = Arrays.asList(match, project, group);
        output = collezione.aggregate(pipeline);
        //Iterable<DBObject> cursor= output.results();
        cursore=output.results().iterator();
   while(cursore.hasNext()){
        elemToinsert=(DBObject) cursore.next();
        logger.debug(elemToinsert.toString());
        if(elemToinsert.get(opName)!=null){
            logger.debug("put");
        elemToinsert.put("start interval",new Date(inizialTime).toGMTString() );
        elemToinsert.put("end interval",new Date(finalTime).toGMTString() );
        list.add(elemToinsert);
   }else{
        logger.debug("not put");
        }
   }
   
    }

return list;
    }
    catch(Exception ex){
        logger.error("error in calcOnField",ex);
        return null;
    }
}

private ArrayList operaSuiCampi(String nameDB,String collectionName, String nameField,long inizialTime, long finalTime,OperationName opName){
    
    switch(opName){
        
        case maximum:
            return this.calcOnField(nameDB, collectionName, nameField, inizialTime, finalTime, "maximum", "$max");
           
        case minimum:
            return this.calcOnField(nameDB, collectionName, nameField, inizialTime, finalTime, "minimum", "$min");
        
        case average:
            return this.calcOnField(nameDB, collectionName, nameField, inizialTime, finalTime, "average", "$avg");
        
        case first:
            return this.calcOnField(nameDB, collectionName, nameField, inizialTime, finalTime, "first", "$first");
        
        case last:
            return this.calcOnField(nameDB, collectionName, nameField, inizialTime, finalTime, "last", "$last");
       
        case sum:
            return this.calcOnField(nameDB, collectionName, nameField, inizialTime, finalTime, "sum", "$sum");
            
        default:
            logger.error("case not recognized");
            
            return null;
                 
    }
    
}

/**
 * 
 * @param container
 * @return null if an error is occurred, an empty arrayList if there aren't elements in MongoDB in that interval time, an ArrayList within DBObjects which contain the query results 
 */
    @Override
    public ArrayList calculateOnField(CalculateOnFieldParameterContainer container){
    
    String nameDB=container.getDbName();
    String collectionName=container.getCollectionName();
    String nameField=container.getFieldName();
    long inizialTime=container.getStartDate();
    long finalTime=container.getEndDate();
    OperationName opName=container.getOpName();
    long range=container.getRange();
    TimeUnit unitRange=container.getUnitRange();
    long step=container.getStep();
    TimeUnit unitStep=container.getUnitStep();
    ArrayList result=new ArrayList();
    long finalTmp=0;
    boolean flag=true;
   // logger.debug("quiiiiiiiiii");
    if(nameDB==null||collectionName==null||nameField==null||opName==null){
        logger.error("bad parameter");
        return null;
        }
    else
        {
            if(inizialTime==-1&&finalTime==-1){
                if(range!=0){
                    inizialTime=System.currentTimeMillis()- TimeUnit.MILLISECONDS.convert(range,unitRange); 
                    finalTime=System.currentTimeMillis();
                    }
                else{
                    //todo: lanciare eccezione? 
                    logger.error("error: start date and final date are equal");
                    return null;
                    }
            }
            else
                if(inizialTime!=-1&&finalTime==-1){
                    if(range!=0){
                        finalTime=inizialTime+TimeUnit.MILLISECONDS.convert(range,unitRange);
                      }
                    else{
                        finalTime=System.currentTimeMillis();
                    }
                }
            else
                if(inizialTime==-1&&finalTime!=-1){
                    if(range!=0){
                        inizialTime=finalTime-TimeUnit.MILLISECONDS.convert(range,unitRange);
                    }
                    else{
                        inizialTime=0;
                    }
                }
        if(step==0){
            result.addAll(this.operaSuiCampi(nameDB, collectionName, nameField, inizialTime, finalTime, opName));
            }
        else{
            step=TimeUnit.MILLISECONDS.convert(step,unitStep);
            
            while(flag){
                finalTmp=inizialTime+step;
                logger.debug("inizio:"+new Date(inizialTime).toGMTString()+"  fine:"+new Date(finalTmp).toGMTString());
                   if(finalTmp<finalTime){
                        result.addAll(this.operaSuiCampi(nameDB, collectionName, nameField, inizialTime, finalTmp, opName));
                        inizialTime=finalTmp;
                        }
                    else{
                        flag=false;
                        result.addAll(this.operaSuiCampi(nameDB, collectionName, nameField, inizialTime, finalTime, opName));
                    }
                }
            }
        }
        
    return result;
    }

  
    
private DBCursor findElement(String nameDB, String nameCollection, String nameField,boolean isPresent){
       
        ArrayList orList = new ArrayList(); 
        BasicDBList listFieldName;  
        BasicDBObject query;// = new BasicDBObject(nameField, new BasicDBObject("$gte", Threshold));
        DB database=this.getDB(nameDB);
        DBCollection collezione;
        
 
  //  logger.debug("dentro findLessThanOnCollection chiamato su: "+nameDB);
         collezione=this.getCollection(database, nameCollection);
// collezione=database.getCollection(nameCollection);
         listFieldName=this.getListFieldName(nameDB, nameField, nameCollection);
       if(listFieldName!=null){ 
         for(int index=0;index<listFieldName.size();index++){
            orList.add(new BasicDBObject((String)listFieldName.get(index), new BasicDBObject("$exists", isPresent)));
            }
       }
      // orList.add(new BasicDBObject(nameField, threshold));
      query=new BasicDBObject("$or", orList);
        return collezione.find(query);
    }
  
private DBCursor findIfPresentOnCollectionToCursor(String nameDB, String nameCollection, String nameField){
      return this.findElement(nameDB, nameCollection, nameField, true);
  
  }

private DBCursor findIfNotPresentOnCollectionToCursor(String nameDB, String nameCollection, String nameField){
      return this.findElement(nameDB, nameCollection, nameField, false);
  
  }
  
    
public List<DBObject> findIfPresentOnCollectionToArray(String nameDB, String nameCollection, String nameField){
      return this.findElement(nameDB, nameCollection, nameField, true).toArray();
  
  }

    
public List<DBObject> findIfNotPresentOnCollectionToArray(String nameDB, String nameCollection, String nameField){
      return this.findElement(nameDB, nameCollection, nameField, false).toArray();
  
  }
  
private List<DBObject> findIfNotPresentOnDB(String nameDB,String nameField){  
    
        DB database=this.getDB(nameDB);
        Set listCollection= database.getCollectionNames();
        Iterator it=listCollection.iterator();
        List listElem=new ArrayList();
        String collectionName;
        
     //   logger.debug("dentro findLessThanOnDB chiamato su: "+nameDB);
 
    while(it.hasNext()){
        collectionName=(String)it.next();
        listElem.addAll(this.findElement(nameDB, collectionName, nameField, false).toArray());
        
    }
    return listElem;
   }
  
  
private List<DBObject> findIfPresentOnDB(String nameDB,String nameField){  
    
        DB database=this.getDB(nameDB);
        Set listCollection= database.getCollectionNames();
        Iterator it=listCollection.iterator();
        List listElem=new ArrayList();
        String collectionName;
        
     //   logger.debug("dentro findLessThanOnDB chiamato su: "+nameDB);
 
    while(it.hasNext()){
        collectionName=(String)it.next();
        listElem.addAll(this.findElement(nameDB, collectionName, nameField, true).toArray());
        
    }
    return listElem;
   }
  
    
public BasicDBList getListAttributeName(String nameDB, String attrName,String collectionName){
      return this.getListField( nameDB, attrName, collectionName,"attributi");
  }
  
private BasicDBList getListField(String nameDB, String nomeField,String collectionName,String id){
  
      BasicDBList listFieldName;
      DBObject oggetto=this.findByIdString(nameDB, collectionName, id);
      String nameField=nomeField.replace(".", "<_>");
      
     // logger.debug("getListFieldName:: "+nameField);
      
      if(oggetto!=null&&oggetto.containsField(nameField)){
        //  logger.debug("aaaaaaaaaaaa"+oggetto.get(nameField));
          listFieldName=(BasicDBList)oggetto.get(nameField);
         }
      else{
            listFieldName=new BasicDBList();
            listFieldName.add(nameField);
        }
      return listFieldName;
  }
  
/**
      * this method is used to merge 2 documents JSON, in this first version, the structure of documents 
      * can be a field which contain the id of this object and some field whose type is to be BasicDBList
      * @param vecchio old document
      * @param nuovo new document
      * @return old document whith the changes
      */
    private BasicDBObject composeDocuments(BasicDBObject vecchio,BasicDBObject nuovo){
         
         
        Set setNew= nuovo.entrySet();
        Iterator itNew=setNew.iterator(),itObj;
        Map.Entry<String,Object> entry;
        String fieldName;
        Object oggetto=null,objToAdd;
        BasicDBList oldList,newList;
      
        while(itNew.hasNext()){
            entry= (Map.Entry<String, Object>) itNew.next();
            fieldName=entry.getKey();
                
                    oggetto=vecchio.get(fieldName);
                    if(oggetto==null){
                      //  logger.debug("campo "+ fieldName+" non presente");
                        vecchio.append(fieldName, entry.getValue());
                        }
                    else{
                        if(oggetto instanceof BasicDBList){
                            //logger.debug(fieldName+ " è un'istanza di BasicDBList");
                            oldList=(BasicDBList)oggetto;
                            newList=((BasicDBList)nuovo.get(fieldName));
                            //logger.debug("old list "+oldList.toString());
                            //logger.debug("new list "+newList.toString());
                            itObj=newList.iterator();
                            while(itObj.hasNext()){
                                objToAdd=itObj.next();
                                if(!oldList.contains(objToAdd)){
                                    //logger.debug("oggetto da aggiungere "+objToAdd);
                                    oldList.add(objToAdd);
                                }
                            }
                           // logger.debug("lista aggiornata "+oldList.toString());
                         vecchio.append(fieldName, oldList);
                        }
                    }
                    
                 
        
        }
         //logger.debug("valore di ritorno "+vecchio.toString());
     return vecchio;
     }
  
  public DBCollection getCollection(DB nameDB, String nameCollection){
    
        nameCollection=nameCollection.replaceAll("-", "__");
    
       return nameDB.getCollection(nameCollection);
        
        }
  
}
