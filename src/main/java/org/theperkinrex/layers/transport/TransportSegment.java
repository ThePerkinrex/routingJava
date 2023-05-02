package org.theperkinrex.layers.transport;

import org.theperkinrex.layers.net.ip.v4.IPv4Packet;

public interface TransportSegment {
    IPv4Packet.Protocol protocol();
}
