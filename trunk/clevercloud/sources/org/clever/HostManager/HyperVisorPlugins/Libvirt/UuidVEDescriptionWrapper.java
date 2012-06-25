
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
