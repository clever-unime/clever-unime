/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clever.HostManager.SOS.SOSModuleTransactional;

/**
 *
 * @author user
 */
public class IdentifierDescription {

    private String identifier_id;
    private String identifier_value;
    private String identifier_description;

    IdentifierDescription() {
        this.identifier_description = "";
        this.identifier_id = "";
        this.identifier_value = "";


    }

    public void setidentifier_id(String str) {
        this.identifier_id = str;
    }

    public void setidentifier_description(String str) {
        this.identifier_description = str;

    }

    public void setidentifier_value(String str) {
        this.identifier_value = str;
    }

    public String getidentifier_id() {
        return this.identifier_id;
    }

    public String getidentifier_description() {
        return this.identifier_description;

    }

    public String getidentifier_value() {
        return this.identifier_value;
    }
}
