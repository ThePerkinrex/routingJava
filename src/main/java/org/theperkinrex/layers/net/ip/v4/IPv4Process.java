package org.theperkinrex.layers.net.ip.v4;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.ip.IpProcess;
import org.theperkinrex.layers.net.ip.PacketAddr;
import org.theperkinrex.layers.transport.TransportSegment;
import org.theperkinrex.layers.transport.icmp.ICMP;
import org.theperkinrex.layers.transport.icmp.v4.ICMPv4Packet;
import org.theperkinrex.layers.transport.icmp.v4.ICMPv4Payload;
import org.theperkinrex.layers.transport.icmp.v4.payload.EchoReply;
import org.theperkinrex.layers.transport.icmp.v4.payload.EchoRequest;
import org.theperkinrex.layers.transport.icmp.v4.payload.TTLinTransit;
import org.theperkinrex.process.IfaceRegistry;
import org.theperkinrex.process.Process;
import org.theperkinrex.routing.RouteNotFoundException;
import org.theperkinrex.routing.Router;
import org.theperkinrex.util.Pair;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class IPv4Process implements Process, IfaceRegistry, IpProcess<IPv4Addr> {
    private class Receiver implements Runnable {
        private final Chassis.IfaceData<LinkAddr, Iface<LinkAddr>> ifaceData;
        private final Chassis.IfaceId<? extends Iface<? extends LinkAddr>> id;

        public Receiver(Chassis.IfaceData<LinkAddr, Iface<LinkAddr>> ifaceData, Chassis.IfaceId<? extends Iface<? extends LinkAddr>> id) {
            this.ifaceData = ifaceData;
            this.id = id;
        }

        @Override
        public void run() {
            while(true) {
                IPv4Packet packet;
                try {
                    Iface.PacketAddr<IPv4Packet, ?> pa = ifaceData.iface().receive(IPv4Packet.class);
                    if(pa == null) {
                        Thread.yield();
                        continue;
                    }
                    packet = pa.packet();
                }catch (InterruptedException ignored) {
                    break;
                }
                System.out.println(ifaceData.conf().getAddr(IPv4Addr.class) +  " | Received packet: " + packet);
                if (packet.getTimeToLive() != 0 && ifaceData.conf().hasAddr(packet.destination)) {
                    recvQueue.computeIfAbsent(packet.payload.getClass(), k -> new LinkedBlockingQueue<>()).add(packet);
                }else if(packet.getTimeToLive() != 0){
                    packet.decreaseTTL();
                    System.out.println(ifaceData.conf().getAddr(IPv4Addr.class) +  " | Routing packet: " + packet);
                    try {
                        send(packet);
                    } catch (RouteNotFoundException e) {
                        System.err.println("Routing error: " + e);
                        e.printStackTrace();
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }else{
                    try {
                        send(new ICMPv4Packet(new TTLinTransit(packet)), packet.source);
                    } catch (RouteNotFoundException e) {
                        System.err.println("Routing error: " + e);
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            System.out.println("Stopping IPv4 receiver");
        }
    }

    public class Icmp implements Process, ICMP<IPv4Addr> {

        private final ConcurrentMap<TTLinTransit.IPv4Header, BiConsumer<IPv4Addr, IPv4Addr>> ttlListeners;
        private final ConcurrentHashMap<Pair<Integer, Integer>, Consumer<Byte>> echoListeners;

        private final Thread receiver;

        private Icmp() {
            this.ttlListeners = new ConcurrentHashMap<>();
            this.echoListeners = new ConcurrentHashMap<>();
            this.receiver = new Thread(() -> {
                while(true) {
                    PacketAddr<ICMPv4Packet, IPv4Addr> p;
                    try {
                        p = receive(ICMPv4Packet.class);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                    ICMPv4Payload payload = p.payload().payload;
                    if (payload instanceof EchoRequest r) {
                        try {
                            send(new ICMPv4Packet(new EchoReply(r.id, r.seq)), p.source());
                        } catch (RouteNotFoundException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }else if (payload instanceof EchoReply r) {
                        var c = echoListeners.remove(new Pair<>(r.id, r.seq));
                        if(c != null) {
                            c.accept(p.ttl());
                        }
                    }else if (payload instanceof TTLinTransit t) {
                        var c = ttlListeners.remove(t.header);
                        if(c != null) {
                            c.accept(p.source(), t.header.dest());
                        }
                    }
                }
            });
        }

        public byte echo(int id, int seq, IPv4Addr dest) throws InterruptedException, RouteNotFoundException {
            Semaphore s = new Semaphore(0);
            AtomicReference<Byte> res = new AtomicReference<>((byte) 0);
            this.echoListeners.put(new Pair<>(id, seq), b -> {
                res.set(b);
                s.release();
            });
            send(new ICMPv4Packet(new EchoRequest(id, seq)), dest);
            s.acquire();
            return res.get();
        }

        public IPv4Addr limitedTTLSegment(TransportSegment ts, IPv4Addr dest, byte ttl) throws InterruptedException, RouteNotFoundException {
            Semaphore s = new Semaphore(0);
            AtomicReference<IPv4Addr> res = new AtomicReference<>();
            IPv4Addr route = routingTable.getRoute(dest);
            if (route == null) throw new RouteNotFoundException(dest);
            var arp_reply = chassis.arp.get(route);
            if (arp_reply == null) throw new RouteNotFoundException(dest);
            var iface = chassis.getIface((Chassis.IfaceId<Iface<LinkAddr>>) arp_reply.u);
            var packet = new IPv4Packet(ts, dest, iface.conf().getAddr(IPv4Addr.class), ttl);
            ttlListeners.put(TTLinTransit.IPv4Header.fromPacket(packet), (droppedAt, d) -> {
                res.set(droppedAt);
                s.release();
            });
            iface.iface().send(packet, arp_reply.t);
            s.acquire();
            return res.get();
        }

        @Override
        public void start() {
            this.receiver.start();
        }

        @Override
        public void stop() {
            this.receiver.interrupt();
        }
    }
    private final Chassis chassis;
    public final Router<IPv4Addr> routingTable;

    private final ConcurrentMap<Class<? extends TransportSegment>, BlockingQueue<IPv4Packet>> recvQueue;
    private final ConcurrentMap<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Thread> receivers;
    public final Icmp icmp;
    private boolean started;

    public IPv4Process(Chassis chassis, Router<IPv4Addr> routingTable) {
        this.chassis = chassis;
        this.routingTable = routingTable;
        this.recvQueue = new ConcurrentHashMap<>();
        this.receivers = new ConcurrentHashMap<>();
        this.icmp = new Icmp();
    }

    private void send(IPv4Packet packet) throws RouteNotFoundException, InterruptedException {
        IPv4Addr route = routingTable.getRoute(packet.destination);
        if (route == null) throw new RouteNotFoundException(packet.destination);
        if (route.isBroadcast()) {
            for(var iface : chassis.ifaces()) {
                iface.u.iface().send(packet, iface.u.iface().broadcast());
            }
        }else{
            var arp_reply = chassis.arp.get(route);
            if (arp_reply == null) throw new RouteNotFoundException(packet.destination);
            var iface = chassis.getIface((Chassis.IfaceId<Iface<LinkAddr>>) arp_reply.u);
            iface.iface().send(packet, arp_reply.t);
        }
    }

    public void send(TransportSegment payload, IPv4Addr destination) throws RouteNotFoundException, InterruptedException {
        IPv4Addr route = routingTable.getRoute(destination);
        if (route == null) throw new RouteNotFoundException(destination);
        if (route.isBroadcast()) {
            for(var iface : chassis.ifaces()) {
                iface.u.iface().send(new IPv4Packet(payload, destination, iface.u.conf().getAddr(IPv4Addr.class)), iface.u.iface().broadcast());
            }
        }else {
            var arp_reply = chassis.arp.get(route);
            if (arp_reply == null) throw new RouteNotFoundException(destination);
            var iface = chassis.getIface((Chassis.IfaceId<Iface<LinkAddr>>) arp_reply.u);
            iface.iface().send(new IPv4Packet(payload, destination, iface.conf().getAddr(IPv4Addr.class)), arp_reply.t);
        }
    }

    public <S extends TransportSegment> PacketAddr<S, IPv4Addr> receive(Class<S> c) throws InterruptedException {
        IPv4Packet p = recvQueue.computeIfAbsent(c, k -> new LinkedBlockingQueue<>()).take();
        return new PacketAddr<>((S) p.payload, p.destination, p.source, p.getTimeToLive());
    }

    public void registerIface(Chassis.IfaceId<? extends Iface<? extends LinkAddr>> ifaceId, Chassis.IfaceData<LinkAddr, Iface<LinkAddr>> ifaceData) {
        Thread t = new Thread(new Receiver(ifaceData, ifaceId));
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
        icmp.start();
        started = true;
    }

    @Override
    public void stop() {
        started = false;
        icmp.stop();
        for(Thread t : receivers.values()) {
            t.interrupt();
        }
    }
}
