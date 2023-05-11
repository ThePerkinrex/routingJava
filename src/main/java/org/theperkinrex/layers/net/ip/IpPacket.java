package org.theperkinrex.layers.net.ip;

import org.theperkinrex.layers.net.NetAddr;

public interface IpPacket<A extends NetAddr> {
    A destination();
}
