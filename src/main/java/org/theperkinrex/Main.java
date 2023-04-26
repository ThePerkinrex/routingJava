package org.theperkinrex;

import org.theperkinrex.layers.link.mac.authority.MACAuthority;
import org.theperkinrex.layers.link.mac.authority.SequentialAuthority;

public class Main {
    public static void main(String[] args) {
        MACAuthority auth = new SequentialAuthority(0x00_00_69);
        for (int i = 0; i < 300; i++) {
            System.out.println(auth.next().toString());
        }
    }
}