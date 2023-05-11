package org.theperkinrex.layers.net.ip.packet.factory;

import org.theperkinrex.layers.transport.TransportSegment;

public interface PacketFactory<A, P> {
    P create(A source, A destination, TransportSegment payload);
}
