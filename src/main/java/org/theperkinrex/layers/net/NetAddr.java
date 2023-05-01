package org.theperkinrex.layers.net;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;

public interface NetAddr {
    String name();
    EthernetFrame.EtherType etherType();
}
