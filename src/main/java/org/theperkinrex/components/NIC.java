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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NIC implements Iface<MAC> {
    public record PayloadWithDest(NetPacket payload, MAC dest){}

    private final MAC mac;
    private final BlockingQueue<PayloadWithDest> sendQueue;
    private DuplexChannel<EthernetFrame> conn;

    private final ConcurrentHashMap<Class<? extends NetPacket>, BlockingQueue<PacketAddr<? extends NetPacket, MAC>>> freezer;

    private final Thread sender;

    public NIC(MAC mac) {
        this.mac = mac;
        this.conn = null;
        this.sendQueue = new LinkedBlockingQueue<>();
        this.sender = new Thread(sender());
        this.freezer = new ConcurrentHashMap<>();
    }

    @Override
    public void start() {
        this.sender.start();
    }

    public void connect(DuplexChannel<EthernetFrame> channel) {
        this.conn = channel;
    }

    public EthernetFrame receiveFrame() throws InterruptedException {
        if (this.conn == null)return null;
        EthernetFrame frame = null;
        do {
            frame = this.conn.receive();
//            System.out.println(mac +  " Received frame [" + frame.destination.equals(mac) + "]: " + frame);
        } while (!frame.destination.equals(mac));
//        System.out.println("Is correct");
        return frame;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends NetPacket> PacketAddr<P, MAC> receive(Class<P> packetType) throws InterruptedException {
        EthernetFrame.EtherType etherType = NetPacket.ethertypeForClass(packetType);
//        System.out.println("Trying freezer");
        PacketAddr<P, MAC> packet = (PacketAddr<P, MAC>) Util.mapOrNull(freezer.get(packetType), q -> {
            try {
                return q.poll(0L, TimeUnit.MICROSECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        if (packet != null) return packet;
        EthernetFrame frame;
        do {
            frame = receiveFrame();
            if (!frame.etherType.equals(etherType)) {
//                System.out.println("Putting packet in freezer " + frame);
                freezer.computeIfAbsent(packetType, k -> new LinkedBlockingQueue<>()).add(new PacketAddr<>(frame.payload, frame.destination));
            }
        } while(!frame.etherType.equals(etherType));
        return new PacketAddr<>((P) frame.payload, frame.destination);
    }

    @Override
    public LinkAddr addr() {
        return mac;
    }

    public void send(NetPacket p, MAC dest) {
        sendQueue.add(new PayloadWithDest(p, dest));
    }
    public Runnable sender() {
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

    @Override
    public State state() {
        return this.conn == null ? State.DOWN : State.UP;
    }

    @Override
    public void stop() {
        sender.interrupt();
    }
}
