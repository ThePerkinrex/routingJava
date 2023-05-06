package org.theperkinrex.layers.application.dhcp.options;

import org.theperkinrex.layers.application.dhcp.DhcpOption;

public class Router implements DhcpOption {
	@Override
	public int code() {
		return ROUTER;
	}
}
