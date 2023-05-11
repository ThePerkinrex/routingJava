package org.theperkinrex.iface;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.util.Pair;

public interface IfaceConfigurer {
    Pair<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>>> onIface();
}
