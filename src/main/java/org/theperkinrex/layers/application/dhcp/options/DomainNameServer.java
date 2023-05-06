package org.theperkinrex.layers.application.dhcp.options;

import org.theperkinrex.layers.application.dhcp.DhcpMessage;
import org.theperkinrex.layers.application.dhcp.DhcpOption;

public class DomainNameServer implements DhcpOption {
	@Override
	public int code() {
		return DOMAIN_NAME_SERVER;
	}
}
