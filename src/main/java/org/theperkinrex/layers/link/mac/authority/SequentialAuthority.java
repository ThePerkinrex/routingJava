package org.theperkinrex.layers.link.mac.authority;

import org.theperkinrex.layers.link.mac.MAC;

public class SequentialAuthority implements MACAuthority {
    private int current;
    private int oui;

    public SequentialAuthority(int oui) {
        if (oui > 0xff_ff_ff) throw new IllegalArgumentException("OUI is greater than 3 bytes");
        this.oui = oui;
        this.current = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SequentialAuthority that = (SequentialAuthority) o;

        return oui == that.oui;
    }

    @Override
    public int hashCode() {
        return oui;
    }

    @Override
    public MAC next() {
        current++;
        return new MAC((byte) ((oui >> 16) & 0xff), (byte) ((oui >> 8) & 0xff), (byte) (oui & 0xff),
                (byte) ((current >> 16) & 0xff), (byte) ((current >> 8) & 0xff), (byte) (current & 0xff));
    }
}
