package org.theperkinrex.layers.net.arp;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.arp.ArpPacket;
import org.theperkinrex.process.Process;
import org.theperkinrex.util.Pair;

import java.time.Duration;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class ArpProcess implements Process {
    private class ArpReceiver implements Runnable {
        public ArpReceiver(Chassis.IfaceData<LinkAddr, Iface<LinkAddr>> ifaceData, Chassis.IfaceId<? extends Iface<? extends LinkAddr>> id) {
            this.ifaceData = ifaceData;
            this.id = id;
        }

        private final Chassis.IfaceData<LinkAddr, Iface<LinkAddr>> ifaceData;
        private final Chassis.IfaceId<? extends Iface<? extends LinkAddr>> id;
        @Override
        public void run() {
            while(true) {
                ArpPacket p;
                try {
                    Iface.PacketAddr<ArpPacket, LinkAddr> pa = ifaceData.iface().receive(ArpPacket.class);
                    if (pa == null) {
                        Thread.yield();
                        continue;
                    }
//                    System.out.println("Received packet: " + pa);
                    p = pa.packet();
                }catch (InterruptedException ignored) {
                    break;
                }
                arpTable.put(p.spa, new Pair<>(p.sha, id));
                if (requests.containsKey(p.spa)) {
                    Queue<BiConsumer<LinkAddr, Chassis.IfaceId<? extends Iface<? extends LinkAddr>>>> q = requests.get(p.spa);
                    q.remove().accept(p.sha, id);
                    if (q.isEmpty()) {
                        requests.remove(p.spa);
                    }
                }
                if(p.operation.equals(ArpPacket.Operation.REQUEST) && ifaceData.conf().hasAddr(p.tpa)) {
                    NetAddr self = ifaceData.conf().getAddr(p.tpa.getClass());
                    assert self != null;
                    ArpPacket reply = ArpPacket.Reply(ifaceData.iface().addr(), self, p.sha, p.spa);
                    ifaceData.iface().send(reply, p.sha);
                }
            }
        }
    }
    private final Chassis chassis;
    private final ConcurrentHashMap<NetAddr, Pair<LinkAddr, Chassis.IfaceId<? extends Iface<? extends LinkAddr>>>> arpTable;
    private final ConcurrentHashMap<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Thread> receivers;
    private boolean started;
    private final ConcurrentHashMap<NetAddr, ConcurrentLinkedQueue<BiConsumer<LinkAddr, Chassis.IfaceId<? extends Iface<? extends LinkAddr>>>>> requests;

    public ArpProcess(Chassis chassis) {
        this.chassis = chassis;
        this.arpTable = new ConcurrentHashMap<>();
        this.requests = new ConcurrentHashMap<>();
        this.receivers = new ConcurrentHashMap<>();
        started = false;
    }

    private Pair<LinkAddr, Chassis.IfaceId<? extends Iface<? extends LinkAddr>>> getCached(NetAddr addr) {
        return arpTable.get(addr);
    }

    private void addRequest(NetAddr addr, BiConsumer<LinkAddr, Chassis.IfaceId<? extends Iface<? extends LinkAddr>>> consumer) {
        Pair<LinkAddr, Chassis.IfaceId<? extends Iface<? extends LinkAddr>>> cached = getCached(addr);
        if (cached != null) {
            consumer.accept(cached.t, cached.u);
            return;
        }
        requests.computeIfAbsent(addr, k -> new ConcurrentLinkedQueue<>()).add(consumer);
        for (Pair<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>>> iface : chassis.ifaces()) {
//            System.out.println(addr + " | Trying sending through " + iface.t + " [" + addr.getClass() + "]");
            NetAddr self = iface.u.conf().getAddr(addr.getClass());
            if (self != null) {
//                System.out.println(addr + " | Sending through " + iface.t + " [" + self+ "]");
                ArpPacket packet = ArpPacket.Request(iface.u.iface().addr(), self, addr);
                iface.u.iface().send(packet, iface.u.iface().addr().broadcast());
            }
        }
    }

    public Pair<LinkAddr, Chassis.IfaceId<? extends Iface<? extends LinkAddr>>> get(NetAddr addr, long timeout, TimeUnit timeUnit) throws InterruptedException {
        BlockingQueue<Pair<LinkAddr, Chassis.IfaceId<? extends Iface<? extends LinkAddr>>>> q = new ArrayBlockingQueue<>(1);
        addRequest(addr, (a, b) -> q.add(new Pair<>(a, b)));
        return q.poll(timeout, timeUnit);
    }

    public Pair<LinkAddr, Chassis.IfaceId<? extends Iface<? extends LinkAddr>>> get(NetAddr addr) throws InterruptedException {
        return get(addr, 500, TimeUnit.MILLISECONDS);
    }

    public void registerIface(Chassis.IfaceId<? extends Iface<? extends LinkAddr>> ifaceId, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>> ifaceData) {
        Thread t = new Thread(new ArpReceiver(ifaceData, ifaceId));
        receivers.put(ifaceId, t);
        if(started && !t.isAlive()) t.start();
    }

    public void unregisterIface(Chassis.IfaceId<? extends Iface<? extends LinkAddr>> ifaceId) {
        Thread t = receivers.remove(ifaceId);
        if (t != null) t.interrupt();
    }

    @Override
    public void start() {
        for(Thread t : receivers.values()) {
            t.start();
        }
        started = true;
    }

    @Override
    public void stop() {
        started = false;
        for(Thread t : receivers.values()) {
            t.interrupt();
        }
    }
}
