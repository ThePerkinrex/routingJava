package org.theperkinrex.layers.application.dhcp.options;


import org.theperkinrex.layers.application.dhcp.DhcpOption;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;

public class SubnetMask implements DhcpOption {
	public final IPv4Addr.Mask mask;

	public SubnetMask(IPv4Addr.Mask mask) {
		this.mask = mask;
	}

	@Override
	public int code() {
		return SUBNET_MASK;
	}
}
