package org.theperkinrex.process;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.util.Pair;

import java.util.concurrent.ConcurrentHashMap;

public class ArpProcess implements Process {
    private final Chassis chassis;
    private final Thread thread;
    private final ConcurrentHashMap<NetAddr, Pair<LinkAddr, Chassis.IfaceId>> arpTable;

    public ArpProcess(Chassis chassis) {
        this.chassis = chassis;
        this.arpTable = new ConcurrentHashMap<>();
        this.thread = new Thread(() -> {

        });
    }

    @Override
    public void start() {
        thread.start();
    }

    @Override
    public void stop() {
        thread.interrupt();
    }
}
