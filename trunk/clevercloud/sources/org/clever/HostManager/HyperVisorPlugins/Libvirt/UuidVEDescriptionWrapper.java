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
package org.clever.HostManager.HyperVisorPlugins.Libvirt;
import org.clever.Common.VEInfo.VEDescription;

/**
 *
 * @author francesco
 */
public class UuidVEDescriptionWrapper {
    private String uuid;
    private VEDescription veDescriptor;
/*
    public UuidVEDescriptionWrapper(String uuid, VEDescription veDescriptor) {
        this.uuid = uuid;
        this.veDescriptor = veDescriptor;
    }

  */

    public String getUuid() {
        return uuid;
    }

    public VEDescription getVeDescriptor() {
        return veDescriptor;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setVeDescriptor(VEDescription veDescriptor) {
        this.veDescriptor = veDescriptor;
    }
}
