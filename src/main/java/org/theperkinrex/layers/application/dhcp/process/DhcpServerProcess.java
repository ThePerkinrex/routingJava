package org.theperkinrex.layers.application.dhcp.process;

import org.theperkinrex.applications.dhcp.leaser.exceptions.NoLeasableAddress;
import org.theperkinrex.applications.dhcp.leaser.exceptions.NoLeasedAddress;
import org.theperkinrex.applications.dhcp.leaser.exceptions.NoOfferedAddress;
import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.application.dhcp.DhcpMessage;
import org.theperkinrex.layers.application.dhcp.DhcpOption;
import org.theperkinrex.layers.application.dhcp.options.*;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.transport.udp.Replier;
import org.theperkinrex.layers.transport.udp.UdpProcess;
import org.theperkinrex.process.Process;
import org.theperkinrex.routing.RouteNotFoundException;
import org.theperkinrex.util.listener.ListenerMap;
import org.theperkinrex.applications.dhcp.leaser.Leaser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DhcpServerProcess implements Process {
	private final Chassis chassis;
	private final Chassis.IfaceId<Iface<LinkAddr>> iface;
	public final Leaser<IPv4Addr> leaser;
	public final IPv4Addr serverAddr;
	public final IPv4Addr gateway;
	public final IPv4Addr.Mask subnetMask;

	public DhcpServerProcess(Chassis chassis, Chassis.IfaceId<Iface<LinkAddr>> iface, Leaser<IPv4Addr> leaser,
							 IPv4Addr serverAddr, IPv4Addr gateway, IPv4Addr.Mask subnetMask) {
		this.chassis = chassis;
		this.iface = iface;
		this.leaser = leaser;
		this.serverAddr = serverAddr;
		this.gateway = gateway;
		this.subnetMask = subnetMask;
	}

	private static Map<Integer, DhcpOption> options(DhcpOption[] options) {
		Map<Integer, DhcpOption> res = new HashMap<>();
		for (DhcpOption opt : options) {
			res.put(opt.code(), opt);
		}
		return res;
	}

	private DhcpOption[] getParameters(ParamterRequestList prl) {
		List<DhcpOption> res = new LinkedList<>();
		for (int parameter : prl.list) {
			switch (parameter) {
				case DhcpOption.ROUTER -> res.add(new Router(gateway));
				default -> System.err.println("Unknown parameter");
			}
		}
		return res.toArray(new DhcpOption[]{});
	}

	@Override
	public void start() {
		try {
			chassis.processes.get(UdpProcess.class).registerListener((short) 67, (NetAddr sourceAddr, short sourcePort, Object payload, Replier replier) -> {
				if (sourcePort == 68 && payload instanceof DhcpMessage m) {
					var options = options(m.options);
					DhcpMessageType dhcpMessageType = (DhcpMessageType) options.get(DhcpOption.DHCP_MESSAGE_TYPE);
					ServerIdentifier si = (ServerIdentifier) options.get(DhcpOption.SERVER_IDENTIFIER);
					if (si != null && !si.serverIdentifier.equals(serverAddr))  {
						return;
					}
					switch (dhcpMessageType) {
						case DHCPDISCOVER -> {
							try {
								var offer = leaser.offer(m.clientHaddr);
								DhcpMessage reply = DhcpMessage.dhcpOffer(m.xid, m.clientHaddr, offer.addr(), subnetMask,
										(int) offer.time().getSeconds(), serverAddr, m.secs);
								replier.sendReply(reply);
							} catch (NoLeasableAddress e) {
								System.err.println("No leasable address");
							} catch (RouteNotFoundException e) {
								System.err.println("No route");
							}
						}
						case DHCPDECLINE -> {
							try {
								leaser.decline(m.clientHaddr);
							} catch (NoOfferedAddress e) {
								System.err.println("No offered address");
							}
						}
						case DHCPREQUEST -> {
							try {
								var lease = leaser.lease(m.clientHaddr);
								if (!lease.addr().equals(((RequestedIpAddress) options.get(DhcpOption.REQUESTED_IP_ADDR)).addr)) {
									System.err.println("Leased address doesnt match");
									return;
								}
								DhcpMessage reply = DhcpMessage.dhcpAck(m.xid, m.clientHaddr, lease.addr(), serverAddr, m.secs,
										getParameters((ParamterRequestList) options.get(DhcpOption.PARAMETER_REQUEST_LIST)));
								replier.sendReply(reply);
							} catch (NoOfferedAddress e) {
								System.err.println("No offered address");
							} catch (RouteNotFoundException e) {
								System.err.println("No route");
							}
						}
						case DHCPRELEASE -> {
							try {
								leaser.release(m.clientHaddr);
							} catch (NoLeasedAddress e) {
								System.err.println("No leased address");
							}
						}
					}
				}
			});
		} catch (ListenerMap.PortAlreadyInUseException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void stop() {
		chassis.processes.get(UdpProcess.class).unregisterListener((short) 67);

	}
}
