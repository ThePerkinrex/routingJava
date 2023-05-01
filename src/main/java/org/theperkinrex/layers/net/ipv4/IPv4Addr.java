package org.theperkinrex.layers.net.ipv4;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.net.NetAddr;

import java.text.ParseException;
import java.util.Scanner;

public class IPv4Addr implements NetAddr {
    private final int addr;

    public IPv4Addr(int addr) {
        this.addr = addr;
    }

    public IPv4Addr(byte a, byte b, byte c, byte d) {
        this(((int) a) << 24 + ((int) b) << 16 + ((int) c)  << 8 + ((int) d));
    }

    public IPv4Addr(String ip) {
        Scanner scn = new Scanner(ip);
        scn.useDelimiter("\\.");
        this.addr = ((scn.nextInt() & 0xff) << 24) + ((scn.nextInt() & 0xff) << 16) + ((scn.nextInt() & 0xff)  << 8) + (scn.nextInt() & 0xff);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPv4Addr iPv4Addr = (IPv4Addr) o;

        return addr == iPv4Addr.addr;
    }

    @Override
    public int hashCode() {
        return addr;
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d.%d", (addr >> 24) & 0xff, (addr >> 16) & 0xff, (addr >> 8) & 0xff, addr & 0xff);
    }

    @Override
    public String name() {
        return "inet";
    }

    @Override
    public EthernetFrame.EtherType etherType() {
        return EthernetFrame.EtherType.IP_V4;
    }
}
