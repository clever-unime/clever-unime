/*
 * Copyright [2014] [Università di Messina]
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
