package org.theperkinrex.routing;

import org.theperkinrex.layers.net.NetAddr;

public class RouteNotFoundException extends Exception {
    public RouteNotFoundException(NetAddr addr) {
        super("Route for " + addr + " not found");
    }
}
