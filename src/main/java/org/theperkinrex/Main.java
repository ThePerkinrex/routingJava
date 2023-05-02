package org.theperkinrex;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.components.NIC;
import org.theperkinrex.components.SimpleSwitch;
import org.theperkinrex.layers.link.ethernet.EthernetFrame;
import org.theperkinrex.layers.link.mac.authority.MACAuthority;
import org.theperkinrex.layers.link.mac.authority.SequentialAuthority;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Process;
import org.theperkinrex.layers.transport.SimpleSegment;
import org.theperkinrex.routing.RoutingTable;
import org.theperkinrex.util.DuplexChannel;

import java.text.ParseException;
import java.time.Duration;

public class Main {
    private static class Receiver implements Runnable {
        private Chassis chassis;
        private String name;

        public Receiver(Chassis chassis, String name) {
            this.chassis = chassis;
            this.name = name;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println(name + ": " + chassis.processes.get(IPv4Process.class, 0).receive(SimpleSegment.class));
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
        RoutingTable<IPv4Addr> routing_a = new RoutingTable<>();
        routing_a.add(new IPv4Addr("200.0.0.0"), new IPv4Addr.Mask((byte) 24));
        routing_a.add(new IPv4Addr("0.0.0.0"), new IPv4Addr.Mask((byte) 0), new IPv4Addr("200.0.0.1"));
        a.processes.add(new IPv4Process(a, routing_a));
        a.getIface(ID).conf().add(new IPv4Addr("200.0.0.2"));
        Chassis b = Chassis.SingleNIC(auth);
        RoutingTable<IPv4Addr> routing_b = new RoutingTable<>();
        routing_b.add(new IPv4Addr("200.0.0.0"), new IPv4Addr.Mask((byte) 24));
        routing_b.add(new IPv4Addr("0.0.0.0"), new IPv4Addr.Mask((byte) 0), new IPv4Addr("200.0.0.1"));
        b.processes.add(new IPv4Process(b, routing_b));
        b.getIface(ID).conf().add(new IPv4Addr("200.0.0.3"));
        Chassis c = Chassis.SingleNIC(auth);
        RoutingTable<IPv4Addr> routing_c = new RoutingTable<>();
        routing_c.add(new IPv4Addr("200.10.0.0"), new IPv4Addr.Mask((byte) 24));
        routing_c.add(new IPv4Addr("0.0.0.0"), new IPv4Addr.Mask((byte) 0), new IPv4Addr("200.10.0.1"));
        c.processes.add(new IPv4Process(c, routing_c));
        c.getIface(ID).conf().add(new IPv4Addr("200.10.0.2"));
        a.start();
        b.start();
        c.start();

        Chassis router = Chassis.IPv4Router3NIC(auth,
                new IPv4Addr("200.0.0.1"), new IPv4Addr.Mask((byte) 24),
                new IPv4Addr("100.100.100.1"), new IPv4Addr.Mask((byte) 30),
                new IPv4Addr("200.10.0.1"), new IPv4Addr.Mask((byte) 24));
        router.start();
        SimpleSwitch s = new SimpleSwitch(3, Duration.ofSeconds(1));
        DuplexChannel.ChannelPair<EthernetFrame> apair = DuplexChannel.createPair();
        a.getIface(ID).iface().connect(apair.a);
        s.connect(0, apair.b);

        DuplexChannel.ChannelPair<EthernetFrame> bpair = DuplexChannel.createPair();
        b.getIface(ID).iface().connect(bpair.a);
        s.connect(1, bpair.b);

        DuplexChannel.ChannelPair<EthernetFrame> router_switch_pair = DuplexChannel.createPair();
        router.getIface(new Chassis.IfaceId<>(NIC.class, 0)).iface().connect(router_switch_pair.a);
        s.connect(2, router_switch_pair.b);

        DuplexChannel.ChannelPair<EthernetFrame> cpair = DuplexChannel.createPair();
        c.getIface(ID).iface().connect(cpair.a);
        router.getIface(new Chassis.IfaceId<>(NIC.class, 2)).iface().connect(cpair.b);

//        a.getIface(ID).iface().send(new SimplePacket("Hola"), new MAC("00-00-69-00-00-02"));
        try {
            a.processes.get(IPv4Process.class, 0).send(new SimpleSegment("Hola"), new IPv4Addr("200.10.0.2"));
            a.processes.get(IPv4Process.class, 0).send(new SimpleSegment("Adios"), new IPv4Addr("200.0.0.3"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread arecv = new Thread(new Receiver(a, "A"));
        arecv.start();
        Thread brecv = new Thread(new Receiver(b, "B"));
        brecv.start();
        Thread crecv = new Thread(new Receiver(c, "C"));
        crecv.start();
//        a.getIface(ID).iface().send(new SimplePacket("Adios"), new MAC("00-00-69-00-00-03"));
//        b.getIface(ID).iface().send(new SimplePacket("desde b"), new MAC("00-00-69-00-00-01"));
        try {
//            a.arp.get(new IPv4Addr("200.0.0.3")).consume((addr, id) -> System.out.println("A: 200.0.0.3 is " + addr + " on " + id));
//            a.arp.get(new IPv4Addr("200.0.0.2")).consume((addr, id) -> System.out.println("A: 200.0.0.2 is " + addr + " on " + id));
//            c.arp.get(new IPv4Addr("200.0.0.1")).consume((addr, id) -> System.out.println("C: 200.0.0.1 is " + addr + " on " + id));

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
        router.stop();
        s.interrupt();
    }
}