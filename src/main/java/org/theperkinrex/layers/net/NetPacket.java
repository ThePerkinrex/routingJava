package org.theperkinrex.layers.net;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.net.arp.ArpPacket;
import org.theperkinrex.layers.net.ipv4.IPv4Packet;

public interface NetPacket {
    EthernetFrame.EtherType etherType();

    static <P extends NetPacket> EthernetFrame.EtherType ethertypeForClass(Class<P> c) {
        if (c.equals(IPv4Packet.class)) return EthernetFrame.EtherType.IP_V4;
        if (c.equals(ArpPacket.class)) return EthernetFrame.EtherType.ARP;
        if (c.equals(SimplePacket.class)) return EthernetFrame.EtherType.SIMPLE;
        throw new IllegalArgumentException("Invalid packet type: " + c.getName());
    }
}
