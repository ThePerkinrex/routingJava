package org.theperkinrex.layers.application.dhcp.process;

import org.theperkinrex.applications.dhcp.leaser.exceptions.NoLeasableAddress;
import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.application.dhcp.DhcpMessage;
import org.theperkinrex.layers.application.dhcp.DhcpOption;
import org.theperkinrex.layers.application.dhcp.options.DhcpMessageType;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.ip.v4.IPv4Addr;
import org.theperkinrex.layers.transport.udp.UdpProcess;
import org.theperkinrex.process.Process;
import org.theperkinrex.util.listener.ListenerMap;
import org.theperkinrex.applications.dhcp.leaser.Leaser;

import java.util.HashMap;
import java.util.Map;

public class DhcpServerProcess implements Process {
	private final Chassis chassis;
	private final Chassis.IfaceId<Iface<LinkAddr>> iface;
	public final Leaser<IPv4Addr> leaser;
	public final IPv4Addr serverAddr;

	public DhcpServerProcess(Chassis chassis, Chassis.IfaceId<Iface<LinkAddr>> iface, Leaser<IPv4Addr> leaser,
	                         IPv4Addr serverAddr) {
		this.chassis = chassis;
		this.iface = iface;
		this.leaser = leaser;
		this.serverAddr = serverAddr;
	}

	private static Map<Integer, DhcpOption> options(DhcpOption[] options) {
		Map<Integer, DhcpOption> res = new HashMap<>();
		for (DhcpOption opt : options) {
			res.put(opt.code(), opt);
		}
		return res;
	}

	@Override
	public void start() {
		try {
			chassis.processes.get(UdpProcess.class).registerListener((short) 67, (NetAddr sourceAddr, short sourcePort, Object payload) -> {
				if (sourcePort == 68 && payload instanceof DhcpMessage m) {
					var options = options(m.options);
					DhcpMessageType dhcpMessageType = (DhcpMessageType) options.get(DhcpOption.DHCP_MESSAGE_TYPE);
					switch (dhcpMessageType) {
						case DHCPDISCOVER -> {
							try {
								var offer = leaser.offer(m.clientHaddr);
								// TODO add options
								DhcpMessage reply = DhcpMessage.dhcpOffer(m.xid, m.clientHaddr, offer.addr(), serverAddr, m.secs, new DhcpOption[]{});
							} catch (NoLeasableAddress e) {
								System.err.println("No leasable address");
							}
						}
						case DHCPDECLINE -> {
						}
						case DHCPACK -> {

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
