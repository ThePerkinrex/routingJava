package org.theperkinrex.layers.application.dhcp.options;

import org.theperkinrex.layers.application.dhcp.DhcpMessage;
import org.theperkinrex.layers.application.dhcp.DhcpOption;

public class IpTtl implements DhcpOption {
	@Override
	public int code() {
		return IP_TTL;
	}
}
