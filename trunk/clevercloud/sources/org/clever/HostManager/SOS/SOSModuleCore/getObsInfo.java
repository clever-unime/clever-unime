/*
 * The MIT License
 *
 * Copyright 2012 Università di Messina.
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
package org.clever.HostManager.SOS.SOSModuleCore;

/**
 *
 * @author user
 */
import java.util.Vector;

/**
 *
 * @author user
 */
public class getObsInfo {
    Vector<String> sensor_id;
    String time_stamp_min;
    String time_stamp_max;
    String offering;
    String geom_type;
    Vector<String> ObsPhenomena;
    Vector<Float> coordinate;
    Vector<String>cord_uom;
    String geom_property;

    getObsInfo(){
        this.sensor_id=new Vector<String>(1);
        this.time_stamp_min="";
        this.time_stamp_max="";
        this.geom_type="";
        this.geom_property="";
        this.offering="";
        this.coordinate=new Vector<Float>(1);
        this.ObsPhenomena=new Vector<String>(1);
        this.cord_uom=new Vector<String>(1);
    
    }
     Vector<String> getSensor_id (){
        return this.sensor_id;
     }
     
     String getTime_stamp_min (){
        return this.time_stamp_min;
     }
     String getTime_stamp_max (){
        return this.time_stamp_max;
     }
     String getOffering (){
        return this.offering;
     }
     String getGeom_property (){
        return this.geom_property;
     }
      String getGeom_type (){
        return this.geom_type;
     }
     Vector<Float> getCoordinate(){
        return this.coordinate;
     }
     Vector<String> getCoord_uom(){
        return this.cord_uom;
     }
     Vector<String> getObsPhenomena(){
        return this.cord_uom;
     }
     void setSensorid(Vector<String> elem){
        this.sensor_id=elem;
     
     }
     void setCoordinate(Vector<Float> elem){
        this.coordinate=elem;
     
     }
     void setCord_uom(Vector<String> elem){
        this.cord_uom=elem;
     
     }
     void setObsPhenomena(Vector<String> elem){
        this.ObsPhenomena=elem;
     
     }
     
     void setGeom_type(String elem){
        this.geom_type=elem;
     }
     void setGeom_property(String elem){
        this.geom_property=elem;
     }
     void setOffering(String elem){
        this.offering=elem;
     }
     void setTime_stamp_max(String elem){
        this.time_stamp_max=elem;
     }
     void setTime_stamp_min(String elem){
        this.time_stamp_min=elem;
     }
    
}