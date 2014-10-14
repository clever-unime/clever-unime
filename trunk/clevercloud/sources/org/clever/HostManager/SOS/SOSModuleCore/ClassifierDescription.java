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
