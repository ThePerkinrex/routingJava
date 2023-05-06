package org.theperkinrex.layers.application.dhcp;

import org.theperkinrex.layers.application.dhcp.options.DhcpMessageType;
import org.theperkinrex.layers.application.dhcp.options.ParamterRequestList;
import org.theperkinrex.layers.application.dhcp.options.RequestedIpAddress;
import org.theperkinrex.layers.application.dhcp.options.ServerIdentifier;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;

public class DhcpMessage {
	public enum Operation {
		REQUEST, REPLY
	}

	public final Operation operation;
	/**
	 * 1 for ethernet
	 */
	public final int htype;
	public final int xid;
	public final short secs;
	public final boolean broadcastFlag;
	public final IPv4Addr clientAddr;
	public final IPv4Addr yourAddr;
	public final IPv4Addr serverAddr;

	public final LinkAddr clientHaddr;

	public final DhcpOption[] options;

	private DhcpMessage(Operation operation, int htype, int xid, short secs, boolean broadcastFlag,
	                    IPv4Addr clientAddr, IPv4Addr yourAddr, IPv4Addr serverAddr, LinkAddr clientHaddr,
	                    DhcpOption[] options) {
		this.operation = operation;
		this.htype = htype;
		this.xid = xid;
		this.secs = secs;
		this.broadcastFlag = broadcastFlag;
		this.clientAddr = clientAddr;
		this.yourAddr = yourAddr;
		this.serverAddr = serverAddr;
		this.clientHaddr = clientHaddr;
		this.options = options;
	}

	private static int linkAddrAsHType(LinkAddr l) {
		return linkKindAsHType(l.kind());
	}

	private static int linkKindAsHType(LinkAddr.LinkAddrKind kind) {
		if (LinkAddr.LinkAddrKind.MAC.equals(kind)) return 1;
		throw new IllegalArgumentException("Unknown kind");
	}

	public static DhcpMessage dhcpDiscover(int xid, LinkAddr clientHaddr, int[] parameter_request_list) {
		DhcpOption[] options = new DhcpOption[]{DhcpMessageType.DHCPDISCOVER, new ParamterRequestList(
				parameter_request_list)};
		return new DhcpMessage(Operation.REQUEST, linkAddrAsHType(clientHaddr), xid, (short) 0, true, null, null,
		                       null,
		                       clientHaddr, options);
	}

	public static DhcpMessage dhcpDiscover(int xid, LinkAddr clientHaddr, IPv4Addr requestedAddr,
	                                       int[] parameter_request_list) {
		DhcpOption[] options = new DhcpOption[]{DhcpMessageType.DHCPDISCOVER, new RequestedIpAddress(requestedAddr),
		                                        new ParamterRequestList(parameter_request_list)};
		return new DhcpMessage(Operation.REQUEST, linkAddrAsHType(clientHaddr), xid, (short) 0, false, null, null,
		                       null, clientHaddr, options);
	}

	public static DhcpMessage dhcpOffer(int xid, LinkAddr clientHaddr, IPv4Addr offeredAddr, IPv4Addr serverAddr,
	                                    short secs, DhcpOption[] options) {
		DhcpOption[] opt = new DhcpOption[options.length + 2];
		opt[0] = DhcpMessageType.DHCPOFFER;
		opt[1] = new ServerIdentifier(serverAddr);
		System.arraycopy(options, 0, opt, 2, options.length);
		return new DhcpMessage(Operation.REPLY, linkAddrAsHType(clientHaddr), xid, secs, false, null, offeredAddr,
		                       serverAddr, clientHaddr, opt);
	}

	public static DhcpMessage dhcpRequest(int xid, LinkAddr clientHaddr, IPv4Addr requestedAddr, IPv4Addr serverAddr, short secs) {
		DhcpOption[] options = new DhcpOption[]{DhcpMessageType.DHCPREQUEST, new RequestedIpAddress(requestedAddr), new ServerIdentifier(serverAddr)};
		return new DhcpMessage(Operation.REQUEST, linkAddrAsHType(clientHaddr), xid, secs, false, null, null, serverAddr, clientHaddr, options);
	}

	public static DhcpMessage dhcpAck(int xid, LinkAddr clientHaddr, IPv4Addr acknowledgedAddr, IPv4Addr serverAddr, short secs, DhcpOption[] options) {
		DhcpOption[] opt = new DhcpOption[options.length + 2];
		opt[0] = DhcpMessageType.DHCPACK;
		opt[1] = new ServerIdentifier(serverAddr);
		System.arraycopy(options, 0, opt, 2, options.length);
		return new DhcpMessage(Operation.REQUEST, linkAddrAsHType(clientHaddr), xid, secs, false, null, acknowledgedAddr, serverAddr, clientHaddr, opt);
	}

	public static DhcpMessage dhcpDecline(int xid, LinkAddr clientHaddr, IPv4Addr serverAddr, short secs) {
		DhcpOption[] options = new DhcpOption[]{DhcpMessageType.DHCPDECLINE, new ServerIdentifier(serverAddr)};
		return new DhcpMessage(Operation.REQUEST, linkAddrAsHType(clientHaddr), xid, secs, false, null, null, serverAddr, clientHaddr, options);
	}
}
