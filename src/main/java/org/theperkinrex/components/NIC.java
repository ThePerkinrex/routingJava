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
    private final BlockingQueue<EthernetFrame> recvQueue;
    private DuplexChannel<EthernetFrame> conn;
    private final Thread sender;
    private final Thread receiver;

    public NIC(MAC mac) {
        this.mac = mac;
        this.conn = null;
        this.sendQueue = new LinkedBlockingQueue<>();
        this.recvQueue = new LinkedBlockingQueue<>();
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
        while(true) {
            EthernetFrame frame = recvQueue.take();
            if(frame.etherType.equals(etherType)) {
                return new PacketAddr<>((P) frame.payload, frame.destination);
            }else{
                recvQueue.add(frame);
            }
        }

    }

    @Override
    public LinkAddr addr() {
        return mac;
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
            while(true) {
                EthernetFrame frame;
                try {
                    frame = receiveFrame();
                } catch (InterruptedException e) {
                    break;
                }
                if (frame == null) {
                    Thread.yield();
                    continue;
                }
                recvQueue.add(frame);
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
