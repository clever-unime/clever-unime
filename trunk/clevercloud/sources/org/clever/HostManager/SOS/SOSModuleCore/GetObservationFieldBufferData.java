/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleCore;

/**
 *
 * @author user
 */
public class GetObservationFieldBufferData {

    private String name;
    private String def;
    private String uom;

    GetObservationFieldBufferData() {
        this.name = "";
        this.def = "";
        this.uom = "";
    }

    void setuom(String elem) {
        this.uom = elem;
    }

    void setname(String elem) {
        this.name = elem;
    }

    void setdef(String elem) {
        this.def = elem;
    }

    String getdef() {
        return this.def;

    }

    String getname() {
        return this.name;

    }

    String getuom() {
        return this.uom;

    }
}