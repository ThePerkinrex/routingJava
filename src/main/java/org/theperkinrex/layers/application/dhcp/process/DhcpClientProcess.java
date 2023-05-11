package org.theperkinrex.layers.application.dhcp.process;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.conf.IfaceConf;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.transport.udp.UdpProcess;
import org.theperkinrex.process.Process;

public class DhcpClientProcess implements Process {
    private final UdpProcess udpProcess;
    private final IfaceConf ifaceConf;

    public DhcpClientProcess(UdpProcess udpProcess, IfaceConf ifaceConf) {
        this.udpProcess = udpProcess;
        this.ifaceConf = ifaceConf;
    }

    public DhcpClientProcess(Chassis chassis, Chassis.IfaceId<Iface<LinkAddr>> ifaceId) {
        this.udpProcess = chassis.processes.get(UdpProcess.class);
        this.ifaceConf = chassis.getIface(ifaceId).conf();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
