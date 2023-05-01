package org.theperkinrex.components;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.link.mac.MAC;
import org.theperkinrex.util.DuplexChannel;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleSwitch {
    private record Port(DuplexChannel<EthernetFrame> channel, Thread t) {
    }

    private final Port[] ports;

    private final Duration cacheTime;

    private record Cache(int port, Instant updated) {
    }

    private final ConcurrentHashMap<MAC, Cache> cache;

    private record Message(EthernetFrame frame, int origin) {
    }

    private final BlockingQueue<Message> sendQueue;
    private final Thread sender;

    public SimpleSwitch(int nPorts, Duration cacheTime) {
        this.ports = new Port[nPorts];
        this.cacheTime = cacheTime;
        this.cache = new ConcurrentHashMap<>();
        this.sendQueue = new LinkedBlockingQueue<>();
        this.sender = new Thread(sender());
        this.sender.start();
    }

    private Runnable sender() {
        return () -> {
            try {
                while (true) {
                    Message m = sendQueue.take();
                    Cache c = cache.get(m.frame.destination);
                    if (c != null && Duration.between(c.updated, Instant.now()).compareTo(cacheTime) > 0) {
//                        System.out.println("Forgot " + m.frame.destination);
                        cache.remove(m.frame.destination);
                        c = null;
                    }
                    if (c == null) {
                        for (int i = 0; i < ports.length; i++) {
                            Port port = ports[i];
                            if (i != m.origin && port.channel != null) {
//                                System.out.println("Sending frame received on " + m.origin + " to " + i);
                                port.channel.send(m.frame);
                            }
                        }
                    } else {
//                        System.out.println("Cached destination: " + c.port + " for " + m.frame.destination);
                        Port port = ports[c.port];
                        if (port != null) {
                            port.channel.send(m.frame);
                        } else {
//                            System.out.println("Forgot " + m.frame.destination);
                            cache.remove(m.frame.destination);
                            sendQueue.add(m); // try sending again
                        }
                    }
                }
            } catch (InterruptedException ignored) {

            }
        };
    }

    public void connect(int port, DuplexChannel<EthernetFrame> channel) {
        if (ports[port] != null) throw new RuntimeException("port already connected"); // FIXME
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    EthernetFrame m = channel.receive();
//                    System.out.println("Received on port " + port);
//                    System.out.println("Learnt " + m.source + " is on port " + port);
                    cache.put(m.source, new Cache(port, Instant.now()));
                    sendQueue.add(new Message(m, port));
                }
            } catch (InterruptedException ignored) {
            }
        });
        t.start();
        ports[port] = new Port(channel, t);
    }

    public void interrupt() {
        for (Port port : ports) {
            port.t.interrupt();
        }
        sender.interrupt();
    }
}
