package org.theperkinrex.layers.transport.udp;

import org.theperkinrex.layers.net.NetAddr;

@FunctionalInterface
public interface UdpListener {
    void accept(NetAddr sourceAddr, short sourcePort, Object payload);
}
