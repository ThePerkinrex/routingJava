package org.theperkinrex.layers.transport.udp;

import org.theperkinrex.layers.net.ip.v4.IPv4Packet;
import org.theperkinrex.layers.transport.TransportSegment;

public class UdpDatagram implements TransportSegment {
	public final short sourcePort;
	public final short destinationPort;
	public final Object payload;

	public UdpDatagram(short sourcePort, short destinationPort, Object payload) {
		this.sourcePort = sourcePort;
		this.destinationPort = destinationPort;
		this.payload = payload;
	}

	@Override
	public IPv4Packet.Protocol protocol() {
		return IPv4Packet.Protocol.UDP;
	}

	@Override
	public String toString() {
		return "UdpDatagram{" + "sourcePort=" + sourcePort + ", destinationPort=" + destinationPort + ", payload=" +
				payload + '}';
	}
}
