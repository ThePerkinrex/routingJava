package org.theperkinrex.layers.transport.icmp.v4.payload;

import org.theperkinrex.layers.transport.icmp.v4.ICMPv4Payload;

public class EchoReply extends ICMPv4Payload {
	public final int id;
	public final int seq;

	public EchoReply(int id, int seq) {
		this.id = id;
		this.seq = seq;
	}

	@Override
	public String toString() {
		return "EchoReply{" + "id=" + id + ", seq=" + seq + '}';
	}
}
