package org.theperkinrex.layers.net.ip.v4;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.NetMask;

import java.util.Scanner;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class IPv4Addr implements NetAddr {
    public static class Mask implements NetMask<IPv4Addr> {
        private final int mask;

        public Mask(IPv4Addr mask) {
            this.mask = mask.addr;
        }

        public Mask(byte bits) {
            if (bits > 32) throw new IllegalArgumentException("too many bits: " + bits);
            int res = 0;
            for (int i = (32-bits); i < 32; i++) {
                res |= 1 << i;
            }
            this.mask = res;
        }

        public IPv4Addr asAddr() {
            return new IPv4Addr(mask);
        }

        @Override
        public boolean matchesMasked(IPv4Addr a, IPv4Addr b) {
            return (a.addr & mask) == (b.addr & mask);
        }

        @Override
        public String toString() {
            return "/" + Integer.bitCount(mask)+ " [" + asAddr() + "]";
        }

        @Override
        public int order() {
            return Integer.bitCount(mask);
        }
    }

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
    public boolean isBroadcast() {
        return addr == 0xff_ff_ff_ff;
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
