/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clever.HostManager.SOS.SOSModuleCore;

/**
 *
 * @author user
 */
public class PhenomenonDescription {
   private String phenomenon_id;
   private  String phenomenon_description;
   private  String phenomenon_unit;
   private  String phenomenon_valuetype;
   private  String offering_id;
   private  String offering_name;
    
    PhenomenonDescription(){
        this.offering_id="";
        this.offering_name="";
        this.phenomenon_description="";
        this.phenomenon_id="";
        this.phenomenon_unit="";
        this.phenomenon_valuetype="";
    
    
    }
    
     public void setPhenomenon_id(String str){
        this.phenomenon_id= str;
    }
     
     public void setPhenomenon_description(String str){
        this.phenomenon_description=str;
       
    }
     
     public void setPhenomenon_unit(String str){
        this.phenomenon_unit=str;
    }
     
     public void setPhenomenon_valuetype(String str){
        this.phenomenon_valuetype=str;
    }
     
     public void setOffering_id(String str){
        this.offering_id=str;
    }
     
     public void setOffering_name(String str){
        this.offering_name=str;
    }
     
      public String getPhenomenon_id(){
       return this.phenomenon_id;
    }
     
     public String getPhenomenon_description(){
        return this.phenomenon_description;
       
    }
     
     public String getPhenomenon_unit(){
        return this.phenomenon_unit;
    }
     
     public String getPhenomenon_valuetype(){
        return this.phenomenon_valuetype;
    }
     
     public String getOffering_id(){
        return this.offering_id;
    }
     
     public String getOffering_name(){
        return this.offering_name;
    }
    
}
