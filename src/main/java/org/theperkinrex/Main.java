package org.theperkinrex;

import org.theperkinrex.components.NIC;
import org.theperkinrex.components.SimpleSwitch;
import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.link.mac.MAC;
import org.theperkinrex.layers.link.mac.authority.MACAuthority;
import org.theperkinrex.layers.link.mac.authority.SequentialAuthority;
import org.theperkinrex.layers.net.SimplePacket;
import org.theperkinrex.util.DuplexChannel;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        MACAuthority auth = new SequentialAuthority(0x00_00_69);
        MAC amac = auth.next();
        NIC a = new NIC(amac);
        MAC bmac = auth.next();
        NIC b = new NIC(bmac);
        MAC cmac = auth.next();
        NIC c = new NIC(cmac);
        SimpleSwitch s = new SimpleSwitch(3, Duration.ofSeconds(1));
        DuplexChannel.ChannelPair<EthernetFrame> apair = DuplexChannel.createPair();
        a.connect(apair.a);
        s.connect(0, apair.b);

        DuplexChannel.ChannelPair<EthernetFrame> bpair = DuplexChannel.createPair();
        b.connect(bpair.a);
        s.connect(1, bpair.b);

        DuplexChannel.ChannelPair<EthernetFrame> cpair = DuplexChannel.createPair();
        c.connect(cpair.a);
        s.connect(2, cpair.b);

        a.send(new SimplePacket("Hola"), bmac);
        Thread arecv = new Thread(() -> {
            while(true) {
                try {
                    System.out.println("A: " + a.receive());
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("Stopping receiver");
        });
        arecv.start();
        Thread brecv = new Thread(() -> {
            while(true) {
                try {
                    System.out.println("B: " + b.receive());
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("Stopping receiver");
        });
        brecv.start();
        Thread crecv = new Thread(() -> {
            while(true) {
                try {
                    System.out.println("C: " + c.receive());
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("Stopping receiver");
        });
        crecv.start();
        a.send(new SimplePacket("Adios"), cmac);
        b.send(new SimplePacket("desde b"), amac);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        arecv.interrupt();
        brecv.interrupt();
        crecv.interrupt();
        a.interrupt();
        b.interrupt();
        c.interrupt();
        s.interrupt();
    }
}