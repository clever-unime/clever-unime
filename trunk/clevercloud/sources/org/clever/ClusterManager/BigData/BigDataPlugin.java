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

package org.clever.ClusterManager.BigData;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.clever.Common.Plugins.RunnerPlugin;
import org.clever.Common.Utils.TypeOfElement;
import org.clever.Common.Utils.BigDataMethodName;
import org.clever.Common.Utils.BigDataParameterContainer;
import org.clever.Common.Utils.CalculateOnFieldParameterContainer;

/**
 *
 * @author Antonio Galletta 2014
 */
public interface BigDataPlugin extends RunnerPlugin {
    

    public List<DBObject> findOnSensingDB(String nameFiled, Object thresholdMin, Object thresholdMax,BigDataMethodName name);
    public List<DBObject> findOnVMLogDB(String nameFiled, Object thresholdMin, Object thresholdMax,BigDataMethodName name);
    public List<DBObject> findOnHostStateDB(String nameFiled, Object thresholdMin, Object thresholdMax,BigDataMethodName name);
    public void shutdown();
   // public BasicDBList getListFieldName(String nameDB, String nameField,String collectionName);
    public DBObject findByObjId(String nameDB,String nameCollection,ObjectId id);
    public DBObject findByIdString(String nameDB,String nameCollection,String id);
    //public void insertOnDB(String dbName,Object elementToInsert, TypeOfElement type);
    public void insertOnDB(BigDataParameterContainer container);
    public void insertSensing(BigDataParameterContainer container);
    public void insertVMLog(BigDataParameterContainer container);
    public void insertHostState(BigDataParameterContainer container);
   // public BasicDBList getListAttributeName(String nameDB, String attrName,String collectionName);
   // public List<DBObject> findIfPresentOnCollectionToArray(String nameDB, String nameCollection, String nameField);
   // public List<DBObject> findIfNotPresentOnCollectionToArray(String nameDB, String nameCollection, String nameField);
   //public List<DBObject> findOnDB(String nameDB,BasicDBObject query);
    public List<DBObject> find(BigDataParameterContainer struct);
   // public List<DBObject> findOnCollection(String nameDB,String nameCollection,String nameFiled, Object thresholdMin, Object thresholdMax, BigDataMethodName nameMtd);
   //public List<DBObject> findOnDB(String nameDB,String nameFiled, Object thresholdMin, Object thresholdMax, BigDataMethodName nameMtd);
    public ArrayList calculateOnField(CalculateOnFieldParameterContainer container);
}
