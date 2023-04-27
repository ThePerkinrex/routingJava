package org.theperkinrex.layers.net;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;

import java.util.Objects;

public class SimplePacket implements NetPacket{
    public Object payload;

    public SimplePacket(Object payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplePacket that = (SimplePacket) o;

        return Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return payload != null ? payload.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SimplePacket{" +
                "payload=" + payload +
                '}';
    }

    @Override
    public EthernetFrame.EtherType etherType() {
        return EthernetFrame.EtherType.SIMPLE;
    }
}
