package org.theperkinrex.layers.net;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;

public interface NetPacket {
    EthernetFrame.EtherType etherType();
}
