/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleCore;

/**
 *
 * @author user
 */
public class SensorDescription {

    private String sensor_id;
    private String description_type;
    private String crs;
    private int mobile;
    private int status;
    private int fixed;
    private float latitude;
    String lat_uom;
    private float longitude;
    private String long_uom;
    private float altitude;
    private String alt_uom;
    private String Frequency;
    private String FrequencyUom;

    SensorDescription() {
        this.sensor_id = "";
        this.crs = "";
        this.description_type = "";
        this.status = -1;
        this.mobile = -1;
        this.fixed = -1;
        this.altitude = (float) -1.0;
        this.latitude = (float) -1.0;
        this.longitude = (float) -1.0;
        this.alt_uom = "";
        this.lat_uom = "";
        this.long_uom = "";
        this.Frequency = "";
        this.FrequencyUom = "";
    }

    public void setSensor_id(String id) {
        this.sensor_id = id;
    }

    public void setDescription_type(String dt) {
        this.description_type = dt;
    }

    public void setCrs(String cr) {
        this.crs = cr;
    }

    public void setStatus(int st) {
        this.status = st;
    }

    public void setFixed(int st) {
        this.fixed = st;
    }

    public void setMobile(int mb) {
        this.mobile = mb;
    }

    public void setLatitude(float lt) {
        this.latitude = lt;
    }

    public void setLongitude(float lg) {
        this.longitude = lg;
    }

    public void setAltitude(float alt) {
        this.altitude = alt;
    }

    public void setlat_uom(String cr) {
        this.lat_uom = cr;
    }

    public void setlong_uom(String cr) {
        this.long_uom = cr;
    }

    public void setalt_uom(String cr) {
        this.alt_uom = cr;
    }

    public String getSensor_id() {
        return this.sensor_id;
    }

    public String getDescription_type() {
        return this.description_type;
    }

    public String getCrs() {
        return this.crs;
    }

    public int getStatus() {
        return this.status;
    }

    public int getMobile() {
        return this.mobile;
    }

    public int getFixed() {
        return this.fixed;
    }

    public float getLatitude() {
        return this.latitude;
    }

    public float getLongitude() {
        return this.longitude;
    }

    public float getAltitude() {
        return this.altitude;
    }

    public String getalt_uom() {
        return this.alt_uom;
    }

    public String getlat_uom() {
        return this.lat_uom;
    }

    public String getlong_uom() {
        return this.long_uom;
    }

    void setFrequency(String string) {
        this.Frequency = string;
    }

    void setFrequencyUom(String string) {
        this.FrequencyUom = string;
    }

    String getFrequency() {
        return this.Frequency;
    }

    String getFrequencyUom() {
        return this.FrequencyUom;
    }
}
