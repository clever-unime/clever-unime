/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.Common.Utils;

import java.util.ArrayList;

/**
 *
 * @author Antonio Galletta 2014
 */
public class BigDataParameterContainer {
    private ArrayList nameFields,thresholds;
    private BigDataMethodName method;
    private String dbName, collectionName;
    private TypeOfElement type;
    private Object elemToInsert;

    public Object getElemToInsert() {
        return elemToInsert;
    }

    public void setElemToInsert(Object elemToInsert) {
        this.elemToInsert = elemToInsert;
    }

    public TypeOfElement getType() {
        return type;
    }

    public void setType(TypeOfElement type) {
        this.type = type;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
    
    public BigDataParameterContainer(){
        nameFields=new ArrayList();
        thresholds =new ArrayList();
    }
    
    public void addFields(String nameField){
        nameFields.add(nameField);
    }
    
    public void addThreshold(Object threshold){
        this.thresholds.add(threshold);
    }
    
    public void setMethod(BigDataMethodName method){
        this.method=method;
    }
    
    public ArrayList getFields(){
        return this.nameFields;
    }
    
    public ArrayList getThresholds(){
        return this.thresholds;
    }
    
    public BigDataMethodName getMethod(){
        return this.method;
    }
}
