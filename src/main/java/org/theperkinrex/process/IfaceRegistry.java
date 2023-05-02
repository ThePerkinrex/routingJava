package org.theperkinrex.process;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;

public interface IfaceRegistry {
    void registerIface(Chassis.IfaceId<? extends Iface<? extends LinkAddr>> ifaceId, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>> ifaceData);

    void unregisterIface(Chassis.IfaceId<? extends Iface<? extends LinkAddr>> ifaceId);
}
