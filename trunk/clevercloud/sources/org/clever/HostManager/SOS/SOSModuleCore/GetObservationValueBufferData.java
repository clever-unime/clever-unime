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
public class GetObservationValueBufferData {

    private String phen_id;
    private String sens_id;
    private String value;
    private String time;
    private String lat;
    private String longit;
    private String mintime;
    private String maxtime;
    private Vector<GetObservationFieldBufferData> field;

    GetObservationValueBufferData() {
        this.phen_id = "";
        this.sens_id = "";
        this.value = "";
        this.time = "";
        this.lat = "";
        this.longit = "";
        this.maxtime = "";
        this.mintime = "";
        this.field = new Vector<GetObservationFieldBufferData>(1);

    }

    Vector<GetObservationFieldBufferData> getfield() {
        return this.field;
    }

    void setfield(Vector<GetObservationFieldBufferData> elem) {
        this.field = elem;
    }

    void setphen_id(String elem) {
        this.phen_id = elem;
    }

    void setsens_id(String elem) {
        this.sens_id = elem;
    }

    void setmintime(String elem) {
        this.mintime = elem;
    }

    void setmaxtime(String elem) {
        this.maxtime = elem;
    }

    void settime(String elem) {
        this.time = elem;
    }

    void setlat(String elem) {
        this.lat = elem;
    }

    void setlong(String elem) {
        this.longit = elem;
    }

    void setvalue(String elem) {
        this.value = elem;
    }

    String getphen_id() {
        return this.phen_id;

    }

    String getmintime() {
        return this.mintime;

    }

    String getmaxtime() {
        return this.maxtime;

    }

    String getsens_id() {
        return this.sens_id;

    }

    String gettime() {
        return this.time;

    }

    String getlat() {
        return this.lat;

    }

    String getlong() {
        return this.longit;

    }

    String getvalue() {
        return this.value;

    }
}
