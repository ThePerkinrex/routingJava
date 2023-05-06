package org.theperkinrex.layers.application.dhcp.options;


import org.theperkinrex.layers.application.dhcp.DhcpOption;

public class SubnetMask implements DhcpOption {
	@Override
	public int code() {
		return SUBNET_MASK;
	}
}
