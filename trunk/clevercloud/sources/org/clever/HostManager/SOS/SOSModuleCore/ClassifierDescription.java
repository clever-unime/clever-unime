/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.HostManager.SOS.SOSModuleCore;

/**
 *
 * @author user
 */
public class ClassifierDescription {
    String classifier_id;
    String classifier_value;
    String classifier_description;
    
    ClassifierDescription(){
        this.classifier_description="";
        this.classifier_id="";
        this.classifier_value="";
    
    
    }
    
     public void setClassifier_id(String str){
        this.classifier_id= str;
    }
     
     public void setClassifier_description(String str){
        this.classifier_description=str;
       
    }
     
     public void setClassifier_value(String str){
        this.classifier_value=str;
    }
     
     
    
     
      public String getClassifier_id(){
       return this.classifier_id;
    }
     
     public String getClassifier_description(){
        return this.classifier_description;
       
    }
     
     public String getClassifier_value(){
        return this.classifier_value;
    }
     
    
    
}
