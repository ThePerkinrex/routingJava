package org.theperkinrex.layers.net.ip.packet.gen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.iface.IfaceNotConfiguredException;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Packet;
import org.theperkinrex.util.Pair;

import java.util.Iterator;

public interface PacketGenerator<A, P> {
    @Nullable
    Iterator<Pair<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>>>> ifacesOnBroadcast();
    @NotNull
    A getDestination();
    @NotNull
    P getPacket(Chassis chassis, Chassis.IfaceId<? extends Iface<? extends LinkAddr>> iface) throws IfaceNotConfiguredException;
}
