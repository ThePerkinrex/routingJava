package org.theperkinrex.components;

import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.link.mac.MAC;
import org.theperkinrex.layers.net.NetPacket;
import org.theperkinrex.util.DuplexChannel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NIC {
    public record PayloadWithDest(NetPacket payload, MAC dest){}

    private final MAC mac;
    private final BlockingQueue<PayloadWithDest> sendQueue;
    private DuplexChannel<EthernetFrame> conn;

    private final Thread sender;

    public NIC(MAC mac) {
        this.mac = mac;
        this.conn = null;
        this.sendQueue = new LinkedBlockingQueue<>();
        this.sender = new Thread(sender());
        this.sender.start();
    }

    public void connect(DuplexChannel<EthernetFrame> channel) {
        this.conn = channel;
    }

    public EthernetFrame receive() throws InterruptedException {
        if (this.conn == null)return null;
        EthernetFrame frame = null;
        do {
            frame = this.conn.receive();
        } while (frame.destination != mac);

        return frame;
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

    public void interrupt() {
        sender.interrupt();
    }
}
