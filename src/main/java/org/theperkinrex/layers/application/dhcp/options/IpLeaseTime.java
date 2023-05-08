package org.theperkinrex.layers.application.dhcp.options;

import org.theperkinrex.layers.application.dhcp.DhcpMessage;
import org.theperkinrex.layers.application.dhcp.DhcpOption;

public class IpLeaseTime implements DhcpOption {
	public final int leaseTime;

	public IpLeaseTime(int leaseTime) {
		this.leaseTime = leaseTime;
	}

	@Override
	public int code() {
		return IP_ADDR_LEASE_TIME;
	}
}
