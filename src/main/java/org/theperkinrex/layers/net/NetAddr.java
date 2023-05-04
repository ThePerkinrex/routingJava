package org.theperkinrex.layers.net;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;

public interface NetAddr {
    boolean isBroadcast();
    String name();
    EthernetFrame.EtherType etherType();
}
