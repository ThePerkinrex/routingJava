package org.theperkinrex.layers.net.ip.packet.gen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.ip.IpPacket;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Packet;
import org.theperkinrex.util.Pair;

import java.util.Iterator;

public class CreatedPacketGenerator<A extends NetAddr, P extends IpPacket<A>> implements PacketGenerator<A, P> {
    public CreatedPacketGenerator(P packet) {
        this.packet = packet;
    }

    public final P packet;

    @Override
    @Nullable
    public Iterator<Pair<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>>>> ifacesOnBroadcast() {
        return null;
    }

    @Override
    public @NotNull A getDestination() {
        return packet.destination();
    }

    @Override
    public @NotNull P getPacket(Chassis chassis, Chassis.IfaceId<? extends Iface<? extends LinkAddr>> iface) {
        return packet;
    }
}
