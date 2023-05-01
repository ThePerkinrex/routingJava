package org.theperkinrex.layers.net.ipv4;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.process.Process;
import org.theperkinrex.routing.Router;

public class IPv4Process implements Process {
    private final Chassis chassis;
    public final Router<IPv4Addr> routingTable;

    public IPv4Process(Chassis chassis, Router<IPv4Addr> routingTable) {
        this.chassis = chassis;
        this.routingTable = routingTable;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
