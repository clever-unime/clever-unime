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
package org.clever.HostManager.SOS.SOSModuleTransactional.Readers;

import java.util.Vector;

/**
 *
 * @author user
 */
public class Sensor_Struct {

    Sensor_Info sensor_info;
    Vector<Sensor_Phenomena> sensor_phen;
    Vector<Sensor_Component> sensor_comp;

    public Sensor_Struct() {
        sensor_info = new Sensor_Info();
        sensor_phen = new Vector<Sensor_Phenomena>(1);
        sensor_comp = new Vector<Sensor_Component>(1);

    }

    public Sensor_Info getSensor_info() {
        return this.sensor_info;
    }

    public Vector<Sensor_Phenomena> getSensor_Phenomena() {
        return this.sensor_phen;
    }

    public Vector<Sensor_Component> getSensor_Component() {
        return this.sensor_comp;
    }
}
