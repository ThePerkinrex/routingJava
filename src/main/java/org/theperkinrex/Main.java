package org.theperkinrex;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.components.NIC;
import org.theperkinrex.components.SimpleSwitch;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.link.mac.MAC;
import org.theperkinrex.layers.link.mac.authority.MACAuthority;
import org.theperkinrex.layers.link.mac.authority.SequentialAuthority;
import org.theperkinrex.layers.net.SimplePacket;
import org.theperkinrex.layers.net.ipv4.IPv4Addr;
import org.theperkinrex.layers.net.ipv4.IPv4Packet;
import org.theperkinrex.routing.RoutingTable;
import org.theperkinrex.util.DuplexChannel;
import org.theperkinrex.util.Pair;

import java.text.ParseException;
import java.time.Duration;

public class Main {
    private static class Receiver<A extends LinkAddr, I extends Iface<A>> implements Runnable {
        private Chassis chassis;
        private Chassis.IfaceId<I> iface;
        private String name;

        public Receiver(Chassis chassis, Chassis.IfaceId<I> iface, String name) {
            this.chassis = chassis;
            this.iface = iface;
            this.name = name;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println(name + ": " + chassis.getIface(iface).iface().receive(SimplePacket.class));
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("Stopping receiver " + name);
        }
    }

    public static void main(String[] args) throws ParseException {
        MACAuthority auth = new SequentialAuthority(0x00_00_69);
        Chassis.IfaceId<NIC> ID = new Chassis.IfaceId<>(NIC.class, 0);
        Chassis a = Chassis.SingleNIC(auth);
        a.getIface(ID).conf().add(new IPv4Addr("200.0.0.1"));
        Chassis b = Chassis.SingleNIC(auth);
        b.getIface(ID).conf().add(new IPv4Addr("200.0.0.2"));
        Chassis c = Chassis.SingleNIC(auth);
        c.getIface(ID).conf().add(new IPv4Addr("200.0.0.3"));
        a.start();
        b.start();
        c.start();

        SimpleSwitch s = new SimpleSwitch(3, Duration.ofSeconds(1));
        DuplexChannel.ChannelPair<EthernetFrame> apair = DuplexChannel.createPair();
        a.getIface(ID).iface().connect(apair.a);
        s.connect(0, apair.b);

        DuplexChannel.ChannelPair<EthernetFrame> bpair = DuplexChannel.createPair();
        b.getIface(ID).iface().connect(bpair.a);
        s.connect(1, bpair.b);

        DuplexChannel.ChannelPair<EthernetFrame> cpair = DuplexChannel.createPair();
        c.getIface(ID).iface().connect(cpair.a);
        s.connect(2, cpair.b);

        a.getIface(ID).iface().send(new SimplePacket("Hola"), new MAC("00-00-69-00-00-02"));
        Thread arecv = new Thread(new Receiver<>(a, ID, "A"));
        arecv.start();
        Thread brecv = new Thread(new Receiver<>(b, ID, "B"));
        brecv.start();
        Thread crecv = new Thread(new Receiver<>(c, ID, "C"));
        crecv.start();
        a.getIface(ID).iface().send(new SimplePacket("Adios"), new MAC("00-00-69-00-00-03"));
        b.getIface(ID).iface().send(new SimplePacket("desde b"), new MAC("00-00-69-00-00-01"));
        try {
            a.arp.get(new IPv4Addr("200.0.0.3")).consume((addr, id) -> System.out.println("A: 200.0.0.3 is " + addr + " on " + id));
            a.arp.get(new IPv4Addr("200.0.0.2")).consume((addr, id) -> System.out.println("A: 200.0.0.2 is " + addr + " on " + id));
            c.arp.get(new IPv4Addr("200.0.0.1")).consume((addr, id) -> System.out.println("C: 200.0.0.1 is " + addr + " on " + id));

            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        arecv.interrupt();
        brecv.interrupt();
        crecv.interrupt();
        a.stop();
        b.stop();
        c.stop();
        s.interrupt();

        RoutingTable<IPv4Addr> r = new RoutingTable<>();
        r.add(new IPv4Addr("200.0.0.0"), new IPv4Addr.Mask((byte) 24), ID);
        r.add(new IPv4Addr(0), new IPv4Addr.Mask((byte) 0), ID, new IPv4Addr("200.0.0.1"));
        System.out.println(r.getRoute(new IPv4Addr("200.0.0.2")));
        System.out.println(r.getRoute(new IPv4Addr("1.1.1.1")));
    }
}