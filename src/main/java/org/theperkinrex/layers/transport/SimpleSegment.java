package org.theperkinrex.layers.transport;

import org.theperkinrex.layers.net.ip.v4.IPv4Packet;

import java.util.Objects;

public class SimpleSegment implements TransportSegment {
    public Object payload;

    public SimpleSegment(Object payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleSegment that = (SimpleSegment) o;

        return Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return payload != null ? payload.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SimpleSegment{" +
                "payload=" + payload +
                '}';
    }

    @Override
    public IPv4Packet.Protocol protocol() {
        return new IPv4Packet.Protocol((byte) 0xff);
    }
}
