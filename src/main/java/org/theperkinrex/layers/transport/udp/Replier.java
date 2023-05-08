package org.theperkinrex.layers.transport.udp;

import org.theperkinrex.routing.RouteNotFoundException;

@FunctionalInterface
public interface Replier {
    void sendReply(Object o) throws RouteNotFoundException, InterruptedException;
}
