package org.theperkinrex.layers.application.dhcp.process;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.conf.IfaceConf;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.iface.IfaceConfigurer;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.transport.udp.UdpProcess;
import org.theperkinrex.process.Process;
import org.theperkinrex.util.Pair;

public class DhcpClientProcess implements Process, IfaceConfigurer {
    private final UdpProcess udpProcess;
    private final Pair<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>>> iface;
    public final Thread configurer;

    public DhcpClientProcess(Chassis chassis, Chassis.IfaceId<Iface<LinkAddr>> ifaceId) {
        this.udpProcess = chassis.processes.get(UdpProcess.class);
        var iface = chassis.getIface(ifaceId);
        this.iface = new Pair<>(ifaceId, iface);
        this.configurer = new Thread(() -> {
            conf().add(IPv4Addr.defaultAddr());
            conf().getAddrUnconfigured(IPv4Addr.class).setConfigurer(this);



            conf().getAddrUnconfigured(IPv4Addr.class).setConfigurer(null);
        });
    }

    private IfaceConf conf() {
        return iface.u.conf();
    }



    @Override
    public void start() {
        configurer.start();
    }

    @Override
    public void stop() {
        configurer.interrupt();
    }


    @Override
    public Pair<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>>> onIface() {
        return iface;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DhcpClientProcess that = (DhcpClientProcess) o;

        if (!udpProcess.equals(that.udpProcess)) return false;
        return iface.equals(that.iface);
    }

    @Override
    public int hashCode() {
        int result = udpProcess.hashCode();
        result = 31 * result + iface.hashCode();
        return result;
    }
}
