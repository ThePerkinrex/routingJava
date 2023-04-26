package org.theperkinrex.layers.net.arp;

import org.theperkinrex.layers.link.ethernet.EthernetPacket;
import org.theperkinrex.layers.net.NetPacket;
import org.theperkinrex.layers.transport.TransportSegment;

public class ArpPacket implements NetPacket {
    public enum Operation {
        REQUEST,
        REPLY
    }

    /**
     * 1 for ethernet
     */
    public final int htype;

    public final EthernetPacket.EtherType ptype;

    public final Operation operation;
    public final Object sha;
    public final Object spa;

    public final Object tha;
    public final Object tpa;

    public ArpPacket(int htype, EthernetPacket.EtherType ptype, Operation operation, Object sha, Object spa, Object tha, Object tpa) {
        this.htype = htype;
        this.ptype = ptype;
        this.operation = operation;
        this.sha = sha;
        this.spa = spa;
        this.tha = tha;
        this.tpa = tpa;
    }

    @Override
    public TransportSegment encapsulated() {
        return null;
    }
}
