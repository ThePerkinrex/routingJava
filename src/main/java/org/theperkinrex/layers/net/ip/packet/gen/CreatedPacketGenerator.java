package org.theperkinrex.layers.net.ip.packet.gen;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.ip.IpPacket;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Packet;

public class CreatedPacketGenerator<A extends NetAddr, P extends IpPacket<A>> implements PacketGenerator<A, P> {
    public CreatedPacketGenerator(P packet) {
        this.packet = packet;
    }

    public final P packet;
    @Override
    public A getDestination() {
        return packet.destination();
    }

    @Override
    public P getPacket(Chassis chassis, Chassis.IfaceId<? extends Iface<? extends LinkAddr>> iface) {
        return packet;
    }
}
