package org.theperkinrex.process;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.ipv4.IPv4Process;

public interface IfaceRegistry {
    void registerIface(Chassis.IfaceId<? extends Iface<? extends LinkAddr>> ifaceId, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>> ifaceData);

    void unregisterIface(Chassis.IfaceId<? extends Iface<? extends LinkAddr>> ifaceId);
}
