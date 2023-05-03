package org.theperkinrex.layers.net.ip;

import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.transport.TransportSegment;

public record PacketAddr<S extends TransportSegment, A extends NetAddr>(S payload, A onAddrReceived, A source, byte ttl) {}
