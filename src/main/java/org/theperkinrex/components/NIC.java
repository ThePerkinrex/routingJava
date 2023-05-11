package org.theperkinrex.components;

import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.link.mac.MAC;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.NetPacket;
import org.theperkinrex.util.DuplexChannel;
import org.theperkinrex.util.Util;

import java.util.Queue;
import java.util.concurrent.*;

public class NIC implements Iface<MAC> {
    public record PayloadWithDest(NetPacket payload, MAC dest){}

    private final MAC mac;
    private final BlockingQueue<PayloadWithDest> sendQueue;
    private final ConcurrentMap<Class<? extends NetPacket>, BlockingQueue<PacketAddr<? extends NetPacket, MAC>>> recvQueue;
    private DuplexChannel<EthernetFrame> conn;
    private final Thread sender;
    private final Thread receiver;

    private final Semaphore connectSemaphore;

    public NIC(MAC mac) {
        this.mac = mac;
        this.conn = null;
        this.sendQueue = new LinkedBlockingQueue<>();
        this.recvQueue = new ConcurrentHashMap<>();
        this.connectSemaphore = new Semaphore(0);
        this.sender = new Thread(sender());
        this.receiver = new Thread(receiver());
    }

    @Override
    public void start() {
        this.sender.start();
        this.receiver.start();
    }

    public void connect(DuplexChannel<EthernetFrame> channel) {
        this.conn = channel;
        connectSemaphore.release();
    }

    public EthernetFrame receiveFrame() throws InterruptedException {
        connectSemaphore.acquire();
        EthernetFrame frame = null;
        do {
            frame = this.conn.receive();
//            System.out.println(mac +  " Received frame [" + frame.destination.equals(mac) + "]: " + frame);
        } while (!frame.destination.equals(mac));
//        System.out.println("Is correct");
        connectSemaphore.release();
        return frame;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends NetPacket> PacketAddr<P, MAC> receive(Class<P> packetType) throws InterruptedException {
        EthernetFrame.EtherType etherType = NetPacket.ethertypeForClass(packetType);
        var p =  recvQueue.computeIfAbsent(packetType, k -> new LinkedBlockingQueue<>()).take();
        return new PacketAddr<>((P) p.packet(), p.addr());
    }

    @Override
    public LinkAddr addr() {
        return mac;
    }

    @Override
    public MAC broadcast() {
        return MAC.BROADCAST;
    }

    public void send(NetPacket p, MAC dest) {
        sendQueue.add(new PayloadWithDest(p, dest));
    }
    private Runnable sender() {
        return () -> {
                while (true) {
                    PayloadWithDest p = null;
                    try {
                        p = sendQueue.take();
                    } catch (InterruptedException e) {
                        break;
                    }
                    if (this.conn != null) {
                        this.conn.send(new EthernetFrame(p.payload, p.dest, mac, p.payload.etherType()));
                    }
                }
            System.out.println("Sender stopped");
        };
    }

    private Runnable receiver() {
        return () -> {
//            int counter = 100;
            while(true) {
                EthernetFrame frame;
                try {
                    frame = receiveFrame();
                } catch (InterruptedException e) {
                    break;
                }
//                if (frame == null) {
//                    try {
//                        Thread.sleep(counter);
//                        counter *= 2;
//                    } catch (InterruptedException e) {
//                        break;
//                    }
//                    continue;
//                }
//                counter = 100;
                recvQueue
                        .computeIfAbsent(frame.payload.getClass(), k -> new LinkedBlockingQueue<>())
                        .add(new PacketAddr<>(frame.payload, frame.source));
            }
        };
    }

    @Override
    public State state() {
        return this.conn == null ? State.DOWN : State.UP;
    }

    @Override
    public void stop() {
        sender.interrupt();
        receiver.interrupt();
    }
}
