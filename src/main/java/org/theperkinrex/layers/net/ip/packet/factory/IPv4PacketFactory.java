package org.theperkinrex.layers.net.ip.packet.factory;

import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Packet;
import org.theperkinrex.layers.transport.TransportSegment;

public class IPv4PacketFactory implements PacketFactory<IPv4Addr, IPv4Packet> {
    @Override
    public IPv4Packet create(IPv4Addr source, IPv4Addr destination, TransportSegment payload) {
        return new IPv4Packet(payload, source, destination);
    }
}
