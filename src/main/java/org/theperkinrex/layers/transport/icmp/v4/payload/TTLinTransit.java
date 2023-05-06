package org.theperkinrex.layers.transport.icmp.v4.payload;

import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.net.ip.v4.IPv4Packet;
import org.theperkinrex.layers.transport.icmp.v4.ICMPv4Payload;

public class TTLinTransit extends ICMPv4Payload {
	public record IPv4Header(IPv4Addr source, IPv4Addr dest, IPv4Packet.Protocol protocol) {
		public static IPv4Header fromPacket(IPv4Packet p) {
			return new IPv4Header(p.source, p.destination, p.protocol);
		}
	}
	public final IPv4Header header;

	public TTLinTransit(IPv4Header header) {
		this.header = header;
	}

	public TTLinTransit(IPv4Packet header) {
		this(IPv4Header.fromPacket(header));
	}

	@Override
	public String toString() {
		return "TTLinTransit{" + "header=" + header + '}';
	}
}
