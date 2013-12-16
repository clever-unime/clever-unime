/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleCore;

import java.util.Vector;

/**
 *
 * @author user
 */
public class GetObservationBufferData {
    /*    private String maxTime;
    private String minTime;*/

    private Vector<String> procedure;
    //  private Vector<GetObservationFieldBufferData> field;
    private Vector<GetObservationValueBufferData> values;

    GetObservationBufferData() {
        /* this.maxTime="";
        this.minTime="";*/
        this.procedure = new Vector<String>(1);
        this.values = new Vector<GetObservationValueBufferData>(1);




    }
    /*   String getmintime(){
    return this.minTime;
    
    }
    
    String getmaxtime(){
    return this.maxTime;
    
    }*/

    Vector<String> getprocedure() {
        return this.procedure;

    }

    Vector<GetObservationValueBufferData> getvalues() {
        return this.values;

    }

    /*   void setmintime(String elem){
    this.minTime=elem;
    }
    void setmaxtime(String elem){
    this.maxTime=elem;
    }*/
    void setprocedure(Vector<String> elem) {
        this.procedure = elem;
    }

    void setvalues(Vector<GetObservationValueBufferData> elem) {
        this.values = elem;
    }
}
