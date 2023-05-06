package org.theperkinrex.layers.transport.icmp.v4;

import org.theperkinrex.layers.net.ip.v4.IPv4Packet;
import org.theperkinrex.layers.transport.TransportSegment;

public class ICMPv4Packet implements TransportSegment {
	public final ICMPv4Payload payload;

	public ICMPv4Packet(ICMPv4Payload payload) {
		this.payload = payload;
	}

	@Override
	public IPv4Packet.Protocol protocol() {
		return IPv4Packet.Protocol.ICMP;
	}

	@Override
	public String toString() {
		return payload.toString();
	}
}
