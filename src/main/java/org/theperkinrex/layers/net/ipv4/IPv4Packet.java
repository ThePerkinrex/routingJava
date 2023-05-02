package org.theperkinrex.layers.net.ipv4;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.net.NetPacket;
import org.theperkinrex.layers.transport.TransportSegment;

public class IPv4Packet implements NetPacket {
    public static class Protocol {
        private final byte protocol;

        public Protocol(byte protocol) {
            this.protocol = protocol;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Protocol protocol1 = (Protocol) o;

            return protocol == protocol1.protocol;
        }

        @Override
        public int hashCode() {
            return protocol;
        }

                @Override
        public String toString() {
            if (protocol == ICMP.protocol) return "ICMP";
            if (protocol == TCP.protocol) return "TCP";
            if (protocol == UDP.protocol) return "UDP";
            return "Protocol(" + protocol + ')';
        }

        public static final Protocol ICMP = new Protocol((byte) 0x01);
        public static final Protocol TCP = new Protocol((byte) 0x06);
        public static final Protocol UDP = new Protocol((byte) 0x11);
    }
    public final TransportSegment payload;
    public final Protocol protocol;
    public final IPv4Addr destination;
    public final IPv4Addr source;
    private byte timeToLive;

    public IPv4Packet(TransportSegment payload, IPv4Addr destination, IPv4Addr source, byte timeToLive) {
        this.payload = payload;
        this.protocol = payload.protocol();
        this.destination = destination;
        this.source = source;
        this.timeToLive = timeToLive;
    }

    public IPv4Packet(TransportSegment payload, IPv4Addr destination, IPv4Addr source) {
        this(payload, destination, source, (byte) 255);
    }

    public void decreaseTTL() {
        if (timeToLive != 0)
            this.timeToLive--;
    }

    public byte getTimeToLive() {
        return timeToLive;
    }

    @Override
    public EthernetFrame.EtherType etherType() {
        return EthernetFrame.EtherType.IP_V4;
    }

    @Override
    public String toString() {
        return "IPv4Packet{" +
                "payload=" + payload +
                ", protocol=" + protocol +
                ", destination=" + destination +
                ", source=" + source +
                ", timeToLive=" + timeToLive +
                '}';
    }
}
