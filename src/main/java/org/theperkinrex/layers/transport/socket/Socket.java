package org.theperkinrex.layers.transport.socket;

import org.theperkinrex.layers.net.NetAddr;

public record Socket(NetAddr addr, short port) {}
