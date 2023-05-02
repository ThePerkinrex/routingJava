package org.theperkinrex.layers.net.arp;

import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.NetPacket;

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
    public final LinkAddr sha;
    public final NetAddr spa;

    public final LinkAddr tha;
    public final NetAddr tpa;

    private ArpPacket(int htype, EthernetFrame.EtherType ptype, Operation operation, LinkAddr sha, NetAddr spa, LinkAddr tha, NetAddr tpa) {
        this.htype = htype;
        this.ptype = ptype;
        this.operation = operation;
        this.sha = sha;
        this.spa = spa;
        this.tha = tha;
        this.tpa = tpa;
    }

    public static <L extends LinkAddr, A extends NetAddr> ArpPacket Request(L sha, A spa, A tpa) {
        int htype;
        if (sha.kind().equals(LinkAddr.LinkAddrKind.MAC)) {
            htype = 1;
        }else{
            throw new IllegalArgumentException(sha.kind() + " is not a valid LinkAddr");
        }
        return new ArpPacket(htype, spa.etherType(), Operation.REQUEST, sha, spa, sha.zeroed(), tpa);
    }

    public static <L extends LinkAddr, A extends NetAddr> ArpPacket Reply(L sha, A spa, L tha, A tpa) {
        int htype;
        if (sha.kind().equals(LinkAddr.LinkAddrKind.MAC)) {
            htype = 1;
        }else{
            throw new IllegalArgumentException(sha.kind() + " is not a valid LinkAddr");
        }
        return new ArpPacket(htype, spa.etherType(), Operation.REPLY, sha, spa, tha, tpa);
    }

//    public static ArpPacket RequestEtherIPv4(MAC sha, IPv4Addr spa, MAC tha, IPv4Addr tpa) {
//        return new ArpPacket(1, EthernetFrame.EtherType.IP_V4, Operation.REQUEST, sha, spa, tha, tpa);
//    }
//
//    public static ArpPacket ReplyEtherIPv4(MAC sha, IPv4Addr spa, MAC tha, IPv4Addr tpa) {
//        return new ArpPacket(1, EthernetFrame.EtherType.IP_V4, Operation.REPLY, sha, spa, tha, tpa);
//    }

    @Override
    public EthernetFrame.EtherType etherType() {
        return EthernetFrame.EtherType.ARP;
    }

    @Override
    public String toString() {
        return "ArpPacket{" +
                "htype=" + htype +
                ", ptype=" + ptype +
                ", operation=" + operation +
                ", sha=" + sha +
                ", spa=" + spa +
                ", tha=" + tha +
                ", tpa=" + tpa +
                '}';
    }
}
