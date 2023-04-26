package org.theperkinrex.layers.net.ipv4;

import org.theperkinrex.layers.net.NetPacket;
import org.theperkinrex.layers.transport.TransportSegment;

public class IPv4Packet implements NetPacket {
    private final TransportSegment payload;
    public final IPv4Addr destination;
    public final IPv4Addr source;
    private byte timeToLive;

    public IPv4Packet(TransportSegment payload, IPv4Addr destination, IPv4Addr source, byte timeToLive) {
        this.payload = payload;
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
    public TransportSegment encapsulated() {
        return payload;
    }
}
