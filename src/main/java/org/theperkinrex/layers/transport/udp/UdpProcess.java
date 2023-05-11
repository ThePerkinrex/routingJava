package org.theperkinrex.layers.transport.udp;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.IfaceNotConfiguredException;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.ip.IpProcess;
import org.theperkinrex.layers.net.ip.PacketAddr;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Process;
import org.theperkinrex.process.Process;
import org.theperkinrex.routing.RouteNotFoundException;
import org.theperkinrex.util.listener.AnyListenerMap;
import org.theperkinrex.util.listener.ListenerMap;
import org.theperkinrex.util.listener.ManyListenerMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UdpProcess implements Process {
    private final Chassis chassis;
    private final ConcurrentMap<Short, ListenerMap<NetAddr, UdpListener>> listeners;
    private Thread ipV4receiver = null;

    public UdpProcess(Chassis chassis) {
        this.chassis = chassis;
        listeners = new ConcurrentHashMap<>();
        var p = chassis.processes.get(IPv4Process.class, 0);
        if (p != null) {
            ipV4receiver = new Thread(new UdpReceiver<>(p));
        }
    }

    public void registerListener(NetAddr addr, short port, UdpListener listener) throws ListenerMap.PortAlreadyInUseException {
        listeners.computeIfAbsent(port, k -> new ManyListenerMap<>()).put(addr, listener);
    }

    public void registerListener(short port, UdpListener listener) throws ListenerMap.PortAlreadyInUseException {
        listeners.computeIfAbsent(port, k -> new AnyListenerMap<>()).put(null, listener);
    }

    public void unregisterListener(NetAddr addr, short port) {
        listeners.computeIfAbsent(port, k -> new ManyListenerMap<>()).remove(addr);
    }

    public void unregisterListener(short port) {
        listeners.computeIfAbsent(port, k -> new ManyListenerMap<>()).remove(null);
    }

    public void send(Object payload, NetAddr dest, short source, short destination) throws RouteNotFoundException, InterruptedException, IfaceNotConfiguredException {
        if (dest instanceof IPv4Addr ip) {
            chassis.processes.get(IPv4Process.class, 0).send(new UdpDatagram(source, destination, payload), ip);
        } else {
            throw new IllegalArgumentException("Unknown address type");
        }
    }

    @Override
    public void start() {
        if (ipV4receiver != null) ipV4receiver.start();
    }

    @Override
    public void stop() {
        if (ipV4receiver != null) ipV4receiver.interrupt();
    }

    private class UdpReceiver<A extends NetAddr> implements Runnable {
        private final IpProcess<A> process;

        public UdpReceiver(IpProcess<A> process) {
            this.process = process;
        }

        @Override
        public void run() {
            while (true) {
                PacketAddr<UdpDatagram, A> datagram;
                try {
                    datagram = process.receive(UdpDatagram.class);
                } catch (InterruptedException e) {
                    break;
                }
                if (listeners.containsKey(datagram.payload().destinationPort)) {
                    var l = listeners.get(datagram.payload().destinationPort);
                    if (l.containsKey(datagram.onAddrReceived())) {
                        try {
                            l.get(datagram.onAddrReceived()).accept(datagram.source(), datagram.payload().sourcePort, datagram.payload().payload, (reply) ->
                                    {
                                        try {
                                            send(reply, datagram.source(), datagram.payload().destinationPort, datagram.payload().sourcePort);
                                        } catch (IfaceNotConfiguredException e) {
                                            e.printStackTrace();
                                        }
                                    }
                            );
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
