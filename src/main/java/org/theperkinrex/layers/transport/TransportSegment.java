package org.theperkinrex.layers.transport;

import org.theperkinrex.layers.net.ipv4.IPv4Packet;

public interface TransportSegment {
    IPv4Packet.Protocol protocol();
}
