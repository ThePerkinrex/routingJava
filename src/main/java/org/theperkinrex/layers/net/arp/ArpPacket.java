package org.theperkinrex.layers.net.arp;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.link.mac.MAC;
import org.theperkinrex.layers.net.NetPacket;
import org.theperkinrex.layers.net.ipv4.IPv4Addr;

public class ArpPacket implements NetPacket {
    public enum Operation {
        REQUEST,
        REPLY
    }

    /**
     * 1 for ethernet
     */
    public final int htype;

    public final EthernetFrame.EtherType ptype;

    public final Operation operation;
    public final Object sha;
    public final Object spa;

    public final Object tha;
    public final Object tpa;

    private ArpPacket(int htype, EthernetFrame.EtherType ptype, Operation operation, Object sha, Object spa, Object tha, Object tpa) {
        this.htype = htype;
        this.ptype = ptype;
        this.operation = operation;
        this.sha = sha;
        this.spa = spa;
        this.tha = tha;
        this.tpa = tpa;
    }

    public static ArpPacket RequestEtherIPv4(MAC sha, IPv4Addr spa, MAC tha, IPv4Addr tpa) {
        return new ArpPacket(1, EthernetFrame.EtherType.IP_V4, Operation.REQUEST, sha, spa, tha, tpa);
    }

    public static ArpPacket ReplyEtherIPv4(MAC sha, IPv4Addr spa, MAC tha, IPv4Addr tpa) {
        return new ArpPacket(1, EthernetFrame.EtherType.IP_V4, Operation.REPLY, sha, spa, tha, tpa);
    }

    @Override
    public EthernetFrame.EtherType etherType() {
        return EthernetFrame.EtherType.ARP;
    }
}
