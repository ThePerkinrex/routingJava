package org.theperkinrex.layers.link.mac;

import java.util.Arrays;

public class MAC {
    private final byte[] address;

    public MAC(byte a, byte b, byte c, byte d, byte e, byte f) {
        this.address = new byte[]{a, b, c,d, e, f};
    }

    @Override
    public String toString() {
        return String.format("%02X-%02X-%02X-%02X-%02X-%02X",
                address[0], address[1] , address[2], address[3], address[4], address[5]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MAC mac = (MAC) o;

        return Arrays.equals(address, mac.address);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(address);
    }
}
