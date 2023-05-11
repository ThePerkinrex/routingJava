package org.theperkinrex.layers.net.ip.packet.gen;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.iface.IfaceNotConfiguredException;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Packet;

public interface PacketGenerator<A, P> {
    A getDestination();
    P getPacket(Chassis chassis, Chassis.IfaceId<? extends Iface<? extends LinkAddr>> iface) throws IfaceNotConfiguredException;
}
