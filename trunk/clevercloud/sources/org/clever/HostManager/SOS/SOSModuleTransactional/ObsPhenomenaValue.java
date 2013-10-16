/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleTransactional;

/**
 *
 * @author user
 */
public class ObsPhenomenaValue {

    String phenomena_id;
    String value;
    String uom;

    void setPhenomena_id(String stringa) {
        this.phenomena_id = stringa;
    }

    void setUom(String stringa) {
        this.uom = stringa;
    }

    void setValue(String stringa) {
        this.value = stringa;
    }

    String getPhenomena_id() {
        return this.phenomena_id;
    }

    String getUom() {
        return this.uom;
    }

    String getValue() {
        return this.value;
    }
}
