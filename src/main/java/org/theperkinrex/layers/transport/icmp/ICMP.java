package org.theperkinrex.layers.transport.icmp;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.iface.IfaceNotConfiguredException;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Packet;
import org.theperkinrex.layers.transport.TransportSegment;
import org.theperkinrex.routing.RouteNotFoundException;
import org.theperkinrex.util.Pair;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public interface ICMP<A extends NetAddr> {
	byte echo(int id, int seq, A dest) throws InterruptedException, RouteNotFoundException, IfaceNotConfiguredException;

	A limitedTTLSegment(TransportSegment ts, A dest, byte ttl) throws InterruptedException, RouteNotFoundException, IfaceNotConfiguredException;
}
