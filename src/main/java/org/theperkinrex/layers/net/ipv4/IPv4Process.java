package org.theperkinrex.layers.net.ipv4;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.arp.ArpProcess;
import org.theperkinrex.layers.transport.TransportSegment;
import org.theperkinrex.process.IfaceRegistry;
import org.theperkinrex.process.Process;
import org.theperkinrex.routing.RouteNotFoundException;
import org.theperkinrex.routing.Router;
import org.theperkinrex.util.Pair;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IPv4Process implements Process, IfaceRegistry {
    public record PacketAddr<P extends TransportSegment>(P payload, IPv4Addr onAddrReceived, IPv4Addr source) {}

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
                if (ifaceData.conf().hasAddr(packet.destination)) {
                    recvQueue.add(packet);
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
                }
            }
            System.out.println("Stopping IPv4 receiver");
        }
    }
    private final Chassis chassis;
    public final Router<IPv4Addr> routingTable;

    private final BlockingQueue<IPv4Packet> recvQueue;
    private final ConcurrentHashMap<Chassis.IfaceId<? extends Iface<? extends LinkAddr>>, Thread> receivers;
    private boolean started;

    public IPv4Process(Chassis chassis, Router<IPv4Addr> routingTable) {
        this.chassis = chassis;
        this.routingTable = routingTable;
        this.recvQueue = new LinkedBlockingQueue<>();
        this.receivers = new ConcurrentHashMap<>();
    }

    private void send(IPv4Packet packet) throws RouteNotFoundException, InterruptedException {
        IPv4Addr route = routingTable.getRoute(packet.destination);
        if (route == null) throw new RouteNotFoundException(packet.destination);
        var arp_reply = chassis.arp.get(route);
        if (arp_reply == null) throw new RouteNotFoundException(packet.destination);
        var iface = chassis.getIface((Chassis.IfaceId<Iface<LinkAddr>>) arp_reply.u);
        iface.iface().send(packet, arp_reply.t);
    }

    public void send(TransportSegment payload, IPv4Addr destination) throws RouteNotFoundException, InterruptedException {
        IPv4Addr route = routingTable.getRoute(destination);
        if (route == null) throw new RouteNotFoundException(destination);
        System.out.println("Route to " + destination + " thorough " + route);
        var arp_reply = chassis.arp.get(route);
        if (arp_reply == null) throw new RouteNotFoundException(destination);
        var iface = chassis.getIface((Chassis.IfaceId<Iface<LinkAddr>>) arp_reply.u);
        iface.iface().send(new IPv4Packet(payload, destination, iface.conf().getAddr(IPv4Addr.class)), arp_reply.t);
    }

    public <S extends TransportSegment> PacketAddr<S> receive(Class<S> c) throws InterruptedException {
        while(true) {
            IPv4Packet p = recvQueue.take();
            if (p.payload.getClass().equals(c)) {
                return new PacketAddr<>((S) p.payload, p.destination, p.source);
            }
            recvQueue.add(p);
            Thread.yield();
        }
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
