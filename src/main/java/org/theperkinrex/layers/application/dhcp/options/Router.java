package org.theperkinrex.layers.application.dhcp.options;

import org.theperkinrex.layers.application.dhcp.DhcpOption;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;

public class Router implements DhcpOption {
	public final IPv4Addr router;

	public Router(IPv4Addr router) {
		this.router = router;
	}

	@Override
	public int code() {
		return ROUTER;
	}
}
