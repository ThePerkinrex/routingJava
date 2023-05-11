package org.theperkinrex.iface;

import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetPacket;
import org.theperkinrex.process.Process;

public interface Iface<A extends LinkAddr> extends Process {
    A broadcast();

    void send(NetPacket packet, A addr);

    record PacketAddr<P extends NetPacket, A extends LinkAddr>(P packet, A addr) {}

    <P extends NetPacket> PacketAddr<P, A> receive(Class<P> packetType) throws InterruptedException;

    enum State {
        UP,
        DOWN,
        UNKNOWN
    }

    State state();

    LinkAddr addr();

    default LinkAddr.LinkAddrKind addrKind() {
        return this.addr().kind();
    }
}
