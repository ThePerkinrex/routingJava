package org.theperkinrex.layers.application.dhcp.options;

import org.theperkinrex.layers.application.dhcp.DhcpOption;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;

public class ServerIdentifier implements DhcpOption {
	public final IPv4Addr serverIdentifier;

	public ServerIdentifier(IPv4Addr serverIdentifier) {
		this.serverIdentifier = serverIdentifier;
	}

	@Override
	public int code() {
		return SERVER_IDENTIFIER;
	}
}
