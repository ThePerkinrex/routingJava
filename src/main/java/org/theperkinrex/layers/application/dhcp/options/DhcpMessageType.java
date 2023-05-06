package org.theperkinrex.layers.application.dhcp.options;

import org.theperkinrex.layers.application.dhcp.DhcpMessage;
import org.theperkinrex.layers.application.dhcp.DhcpOption;

public enum DhcpMessageType implements DhcpOption {
	DHCPDISCOVER, DHCPOFFER, DHCPREQUEST, DHCPDECLINE, DHCPACK, DHCPNAK, DHCPRELEASE, DHCPINFORM, DHCPFORCERENEW,
	DHCPLEASEQUERY, DHCPLEASEUNASSIGNED, DHCPLEASEUNKNOWN, DHCPLEASEACTIVE, DHCPBULKLEASEQUERY, DHCPLEASEQUERYDONE,
	DHCPACTIVELEASEQUERY, DHCPLEASEQUERYSTATUS, DHCPTLS;

	@Override
	public int code() {
		return DHCP_MESSAGE_TYPE;
	}
}

