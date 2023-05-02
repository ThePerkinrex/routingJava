package org.theperkinrex.components;

import org.theperkinrex.conf.IfaceConf;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.link.mac.authority.MACAuthority;
import org.theperkinrex.layers.net.arp.ArpProcess;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Process;
import org.theperkinrex.process.Process;
import org.theperkinrex.routing.RoutingTable;
import org.theperkinrex.util.Pair;
import org.theperkinrex.util.ProcessManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Chassis implements Process {
    public static class IfaceId<I extends Iface<? extends LinkAddr>> {
        public final Class<I> type;
        public final int id;

        public IfaceId(Class<I> type, int id) {
            this.type = type;
            this.id = id;
        }

        @Override
        public String toString() {
            if (type.equals(NIC.class)) {
                return "eth" + id;
            } else {
                return type.getName() + id;
            }
        }
    }

    public record IfaceData<A extends LinkAddr, I extends Iface<A>>(I iface, IfaceConf conf) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IfaceData<?, ?> ifaceData = (IfaceData<?, ?>) o;

            if (!Objects.equals(iface, ifaceData.iface)) return false;
            return Objects.equals(conf, ifaceData.conf);
        }

        @Override
        public int hashCode() {
            int result = iface != null ? iface.hashCode() : 0;
            result = 31 * result + (conf != null ? conf.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return iface.state() + "\n\tPhysical address: [" + iface.addrKind() + "] " + iface.addr() + "\n\t" + conf();
        }
    }

    private final ConcurrentMap<Class<? extends Iface<? extends LinkAddr>>, ArrayList<IfaceData<? extends LinkAddr, ? extends Iface<? extends LinkAddr>>>> interfaces;
    public final ArpProcess arp;
    public final ProcessManager processes;
    private boolean started;

    @SuppressWarnings("unchecked")
    public <A extends LinkAddr, I extends Iface<A>> IfaceId<I> addIface(I iface) {
        Class<? extends Iface<LinkAddr>> c = (Class<? extends Iface<LinkAddr>>) iface.getClass();
        ArrayList<IfaceData<? extends LinkAddr, ? extends Iface<? extends LinkAddr>>> ifaces = interfaces.computeIfAbsent(c, k -> new ArrayList<>());
        int idx = ifaces.size();
        IfaceData<LinkAddr, Iface<LinkAddr>> data = new IfaceData<>((Iface<LinkAddr>) iface, new IfaceConf());
        ifaces.add(data);
        IfaceId<I> ifaceId = new IfaceId<>((Class<I>) iface.getClass(), idx);
        this.arp.registerIface(ifaceId, data);
        this.processes.registerIface(ifaceId, data);
        if (started) iface.start();
        return ifaceId;
    }

    @SuppressWarnings("unchecked")
    public Pair<IfaceId<? extends Iface<? extends LinkAddr>>, IfaceData<LinkAddr, Iface<LinkAddr>>>[] ifaces() {
        LinkedList<Pair<IfaceId<? extends Iface<? extends LinkAddr>>, IfaceData<LinkAddr, Iface<LinkAddr>>>> res = new LinkedList<>();
        for (Class<? extends Iface<? extends LinkAddr>> c :
                interfaces.keySet()) {
            ArrayList<IfaceData<? extends LinkAddr, ? extends Iface<? extends LinkAddr>>> list = interfaces.get(c);
            for (int i = 0; i < list.size(); i++) {
                IfaceId<? extends Iface<? extends LinkAddr>> id = (IfaceId<? extends Iface<? extends LinkAddr>>) new IfaceId<>(c, i);
                res.add(new Pair<>(id, (IfaceData<LinkAddr, Iface<LinkAddr>>) list.get(i)));
            }
        }
        return res.toArray((Pair<IfaceId<? extends Iface<? extends LinkAddr>>, IfaceData<LinkAddr, Iface<LinkAddr>>>[]) new Pair[res.size()]);
    }

    public <A extends LinkAddr, I extends Iface<A>> IfaceData<A, I> getIface(IfaceId<I> id) throws IndexOutOfBoundsException {
        if (!interfaces.containsKey(id.type))
            throw new IndexOutOfBoundsException(id + " is not a type of interface in this chassis");
        ArrayList<IfaceData<? extends LinkAddr, ? extends Iface<? extends LinkAddr>>> arr = interfaces.get(id.type);
        if (id.id >= arr.size()) throw new IndexOutOfBoundsException(id + " does not exist on this chassis");
        return (IfaceData<A, I>) arr.get(id.id);
    }

    public Chassis() {
        this.interfaces = new ConcurrentHashMap<>();
        this.arp = new ArpProcess(this);
        this.processes = new ProcessManager();
        this.started = false;
    }

    @Override
    public void start() {
        this.started = true;
        this.arp.start();
        this.processes.start();
        for (Pair<IfaceId<? extends Iface<? extends LinkAddr>>, IfaceData<LinkAddr, Iface<LinkAddr>>> p : ifaces()) {
            p.u.iface.start();
        }
    }

    @Override
    public void stop() {
        this.started = false;
        this.arp.stop();
        this.processes.stop();
        for (Pair<IfaceId<? extends Iface<? extends LinkAddr>>, IfaceData<LinkAddr, Iface<LinkAddr>>> p : ifaces()) {
            p.u.iface.stop();
        }
    }

    public static Chassis SingleNIC(MACAuthority auth) {
        Chassis res = new Chassis();
        res.addIface(new NIC(auth.next()));
        return res;
    }

    public static Chassis IPv4Router3NIC(MACAuthority auth, IPv4Addr a, IPv4Addr.Mask amask, IPv4Addr b, IPv4Addr.Mask bmask, IPv4Addr c, IPv4Addr.Mask cmask) {
        Chassis res = new Chassis();
        RoutingTable<IPv4Addr> routing = new RoutingTable<>();
        routing.add(a, amask);
        res.getIface(res.addIface(new NIC(auth.next()))).conf().add(a);
        routing.add(b, bmask);
        res.getIface(res.addIface(new NIC(auth.next()))).conf().add(b);
        routing.add(c, cmask);
        res.getIface(res.addIface(new NIC(auth.next()))).conf().add(c);
        res.processes.add(new IPv4Process(res, routing));
        routing.print();
        return res;
    }
}
