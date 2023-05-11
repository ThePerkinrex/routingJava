package org.theperkinrex.layers.net.ip;

import org.theperkinrex.iface.IfaceConfigurer;
import org.theperkinrex.iface.IfaceNotConfiguredException;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Process;
import org.theperkinrex.layers.transport.TransportSegment;
import org.theperkinrex.routing.RouteNotFoundException;

public interface IpProcess<A extends NetAddr> {
	void send(TransportSegment payload, A destination) throws RouteNotFoundException, InterruptedException, IfaceNotConfiguredException;
	void sendUnconfigured(TransportSegment payload, A destination, IfaceConfigurer ifaceConfigurer) throws RouteNotFoundException, InterruptedException, IfaceNotConfiguredException;
	<S extends TransportSegment> PacketAddr<S, A> receive(Class<S> c) throws InterruptedException;
}
