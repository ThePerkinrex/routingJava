package org.theperkinrex.layers.application.dhcp.process;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.conf.IfaceConf;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.iface.IfaceConfigurer;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.transport.udp.UdpProcess;
import org.theperkinrex.process.Process;
import org.theperkinrex.util.Pair;

public class DhcpClientProcess implements Process, IfaceConfigurer {
    private final UdpProcess udpProcess;
    private final IfaceConf ifaceConf;
    private final Pair<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>>> iface;

    public DhcpClientProcess(Chassis chassis, Chassis.IfaceId<Iface<LinkAddr>> ifaceId) {
        this.udpProcess = chassis.processes.get(UdpProcess.class);
        var iface = chassis.getIface(ifaceId);
        this.ifaceConf = iface.conf();
        this.iface = new Pair<>(ifaceId, iface);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Pair<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>>> onIface() {
        return iface;
    }
}
