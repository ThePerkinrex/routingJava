package org.theperkinrex.layers.application.dhcp.options;

import org.theperkinrex.layers.application.dhcp.DhcpMessage;
import org.theperkinrex.layers.application.dhcp.DhcpOption;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;

public class RequestedIpAddress implements DhcpOption {
	public final IPv4Addr addr;

	public RequestedIpAddress(IPv4Addr addr) {
		this.addr = addr;
	}

	@Override
	public int code() {
		return REQUESTED_IP_ADDR;
	}
}
