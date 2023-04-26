package org.theperkinrex.layers.net.ipv4;

public class IPv4Addr {
    private final int addr;

    public IPv4Addr(int addr) {
        this.addr = addr;
    }

    public IPv4Addr(byte a, byte b, byte c, byte d) {
        this(((int) a) << 24 + ((int) b) << 16 + ((int) c)  << 8 + ((int) d));
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
}
