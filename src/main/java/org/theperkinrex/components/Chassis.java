package org.theperkinrex.components;

import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.link.mac.authority.MACAuthority;
import org.theperkinrex.process.Process;
import org.theperkinrex.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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

    private record IfaceData<A extends LinkAddr, I extends Iface<A>>(I iface) {
    }

    private final HashMap<Class<? extends Iface<? extends LinkAddr>>, ArrayList<IfaceData<? extends LinkAddr, ? extends Iface<? extends LinkAddr>>>> interfaces;

    private boolean started;

    @SuppressWarnings("unchecked")
    public <A extends LinkAddr, I extends Iface<A>> IfaceId<I> addIface(I iface) {
        Class<? extends Iface<LinkAddr>> c = (Class<? extends Iface<LinkAddr>>) iface.getClass();
        ArrayList<IfaceData<? extends LinkAddr, ? extends Iface<? extends LinkAddr>>> ifaces = interfaces.computeIfAbsent(c, k -> new ArrayList<>());
        int idx = ifaces.size();
        ifaces.add(new IfaceData<>((Iface<LinkAddr>) iface));
        if (started) iface.start();
        //noinspection rawtypes
        return new IfaceId<>((Class<I>) iface.getClass(), idx);
    }

    @SuppressWarnings("unchecked")
    public Pair<IfaceId<? extends Iface<? extends LinkAddr>>, Iface<LinkAddr>>[] ifaces() {
        LinkedList<Pair<IfaceId<? extends Iface<? extends LinkAddr>>, Iface<LinkAddr>>> res = new LinkedList<>();
        for (Class<? extends Iface<? extends LinkAddr>> c :
                interfaces.keySet()) {
            ArrayList<IfaceData<? extends LinkAddr, ? extends Iface<? extends LinkAddr>>> list = interfaces.get(c);
            for (int i = 0; i < list.size(); i++) {
                IfaceId<? extends Iface<? extends LinkAddr>> id = (IfaceId<? extends Iface<? extends LinkAddr>>) new IfaceId<>(c, i);
                res.add(new Pair<>(id, (Iface<LinkAddr>) list.get(i).iface));
            }
        }
        return res.toArray((Pair<IfaceId<? extends Iface<? extends LinkAddr>>, Iface<LinkAddr>>[]) new Pair[res.size()]);
    }

    public <A extends LinkAddr, I extends Iface<A>> I getIface(IfaceId<I> id) throws IndexOutOfBoundsException {
        if (!interfaces.containsKey(id.type))
            throw new IndexOutOfBoundsException(id + " is not a type of interface in this chassis");
        ArrayList<IfaceData<? extends LinkAddr, ? extends Iface<? extends LinkAddr>>> arr = interfaces.get(id.type);
        if (id.id >= arr.size()) throw new IndexOutOfBoundsException(id + " does not exist on this chassis");
        return (I) arr.get(id.id).iface;
    }

    public Chassis() {
        this.interfaces = new HashMap<>();
        this.started = false;
    }

    @Override
    public void start() {
        this.started = true;
        for (Pair<IfaceId<? extends Iface<? extends LinkAddr>>, Iface<LinkAddr>> p : ifaces()) {
            p.u.start();
        }
    }

    @Override
    public void stop() {
        this.started = false;
        for (Pair<IfaceId<? extends Iface<? extends LinkAddr>>, Iface<LinkAddr>> p : ifaces()) {
            p.u.stop();
        }
    }

    public static Chassis SingleNIC(MACAuthority auth) {
        Chassis res = new Chassis();
        res.addIface(new NIC(auth.next()));
        return res;
    }

    public static Chassis Router3NIC(MACAuthority auth) {
        Chassis res = new Chassis();
        res.addIface(new NIC(auth.next()));
        res.addIface(new NIC(auth.next()));
        res.addIface(new NIC(auth.next()));
        return res;
    }
}
