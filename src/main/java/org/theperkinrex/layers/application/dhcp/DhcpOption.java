package org.theperkinrex.layers.application.dhcp;

public interface DhcpOption {
	int SUBNET_MASK = 1;
	int ROUTER = 3;
	int DOMAIN_NAME_SERVER = 6;
	int IP_TTL = 23;
	int BROADCAST_ADDRESS = 28;
	int ARP_CACHE_TIMEOUT = 25;
	int REQUESTED_IP_ADDR = 50;
	int IP_ADDR_LEASE_TIME = 51;
	int DHCP_MESSAGE_TYPE = 53;
	int SERVER_IDENTIFIER = 53;
	int PARAMETER_REQUEST_LIST = 55;
	int code();
}
