package org.theperkinrex.layers.net.ip.packet.gen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.iface.IfaceNotConfiguredException;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.ip.packet.factory.PacketFactory;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Packet;
import org.theperkinrex.layers.transport.TransportSegment;
import org.theperkinrex.util.Pair;

import java.util.Iterator;

public class TransportPacketGenerator<A extends NetAddr, P> implements PacketGenerator<A, P>{
    private final PacketFactory<A, P> factory;
    private final TransportSegment transportSegment;
    private final A destination;

    public TransportPacketGenerator(PacketFactory<A, P> factory, TransportSegment transportSegment, A destination) {
        this.factory = factory;
        this.transportSegment = transportSegment;
        this.destination = destination;
    }

    @Override
    public @Nullable Iterator<Pair<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>>>> ifacesOnBroadcast() {
        return null;
    }

    @Override
    public @NotNull A getDestination() {
        return destination;
    }

    @Override
    public @NotNull P getPacket(Chassis chassis, Chassis.IfaceId<? extends Iface<? extends LinkAddr>> ifaceId) throws IfaceNotConfiguredException {
        var iface = chassis.getIface((Chassis.IfaceId<Iface<LinkAddr>>) ifaceId);
        var source = iface.conf().getAddr((Class<A>) destination.getClass());
        if (source == null) throw new IfaceNotConfiguredException();
        return factory.create(source, destination, transportSegment);
    }
}
