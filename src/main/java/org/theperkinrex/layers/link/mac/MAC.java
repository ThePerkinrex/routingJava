package org.theperkinrex.layers.link.mac;

import org.theperkinrex.layers.link.LinkAddr;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Scanner;

public class MAC implements LinkAddr {
    public static final MAC DEFAULT = new MAC((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
    public static final MAC BROADCAST = new MAC((byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255);
    private final byte[] address;

    public MAC(byte a, byte b, byte c, byte d, byte e, byte f) {
        this.address = new byte[]{a, b, c, d, e, f};
    }

    public MAC(String mac) {
        Scanner scn = new Scanner(mac);
        scn.useDelimiter("-");
        this.address = new byte[]{scn.nextByte(16), scn.nextByte(16), scn.nextByte(16), scn.nextByte(16),
                scn.nextByte(16), scn.nextByte(16)};
    }

    @Override
    public String toString() {
        return String.format("%02X-%02X-%02X-%02X-%02X-%02X", address[0], address[1], address[2], address[3],
                address[4], address[5]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MAC mac = (MAC) o;

        return Arrays.equals(address, mac.address) || Arrays.equals(mac.address, BROADCAST.address) || Arrays.equals(address, BROADCAST.address);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(address);
    }

    @Override
    public LinkAddrKind kind() {
        return LinkAddrKind.MAC;
    }

    @Override
    public LinkAddr zeroed() {
        return DEFAULT;
    }

    @Override
    public LinkAddr broadcast() {
        return BROADCAST;
    }
}
