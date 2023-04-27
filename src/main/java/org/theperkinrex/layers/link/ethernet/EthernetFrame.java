package org.theperkinrex.layers.link.ethernet;

import org.theperkinrex.layers.link.LinkFrame;
import org.theperkinrex.layers.link.mac.MAC;
import org.theperkinrex.layers.net.NetPacket;
import org.theperkinrex.layers.net.ipv4.IPv4Packet;

public class EthernetFrame implements LinkFrame {
    public final NetPacket payload;
    public final MAC destination;
    public MAC source;
    public final EtherType etherType;
    public final Dot1qTag dot1qTag;

    public EthernetFrame(NetPacket payload, MAC destination, MAC source, EtherType etherType, Dot1qTag dot1qTag) {
        this.payload = payload;
        this.destination = destination;
        this.source = source;
        this.etherType = etherType;
        this.dot1qTag = dot1qTag;
    }

    public EthernetFrame(NetPacket payload, MAC destination, MAC source, EtherType etherType) {
        this(payload, destination, source, etherType, null);
    }

    public EthernetFrame(NetPacket payload, MAC destination, EtherType etherType, Dot1qTag dot1qTag) {
        this(payload, destination, null, etherType, dot1qTag);
    }

    public EthernetFrame(NetPacket payload, MAC destination, EtherType etherType) {
        this(payload, destination, etherType, null);
    }

    public EthernetFrame(IPv4Packet payload, MAC destination, MAC source) {
        this(payload, destination, source, EtherType.IP_V4, null);
    }

    public static class EtherType {
        private final int type;

        private EtherType(int type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EtherType etherType = (EtherType) o;

            return type == etherType.type;
        }

        @Override
        public int hashCode() {
            return type;
        }

        public static final EtherType IP_V4 = new EtherType(0x0800);
        public static final EtherType IP_V6 = new EtherType(0x86DD);
        public static final EtherType ARP = new EtherType(0x0806);
        public static final EtherType SIMPLE = new EtherType(0x0000);

        @Override
        public String toString() {
            if (type == IP_V4.type) return "IP_V4";
            if (type == IP_V6.type) return "IP_V6";
            if (type == ARP.type) return "ARP";
            return "EtherType(" + type + ')';
        }
    }

    public static class Dot1qTag {
        public final byte priority_code_point;
        public final boolean drop_elegible;
        public final int vlan_id;

        public Dot1qTag(byte priority_code_point, boolean drop_elegible, int vlan_id) {
            this.priority_code_point = priority_code_point;
            this.drop_elegible = drop_elegible;
            this.vlan_id = vlan_id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Dot1qTag dot1qTag = (Dot1qTag) o;

            return vlan_id == dot1qTag.vlan_id;
        }

        @Override
        public int hashCode() {
            return vlan_id;
        }

        @Override
        public String toString() {
            return "Dot1qTag{" +
                    "priority_code_point=" + priority_code_point +
                    ", drop_elegible=" + drop_elegible +
                    ", vlan_id=" + vlan_id +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "EthernetFrame{" +
                "payload=" + payload +
                ", destination=" + destination +
                ", source=" + source +
                ", etherType=" + etherType +
                ", dot1qTag=" + dot1qTag +
                '}';
    }
}
