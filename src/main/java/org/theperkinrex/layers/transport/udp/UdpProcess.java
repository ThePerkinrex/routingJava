package org.theperkinrex.layers.transport.udp;

import org.theperkinrex.components.Chassis;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.ip.IpProcess;
import org.theperkinrex.layers.transport.socket.Socket;
import org.theperkinrex.process.Process;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UdpProcess implements Process {
	private final Chassis chassis;
//	private final ConcurrentMap<Socket, >

	public UdpProcess(Chassis chassis) {
		this.chassis = chassis;
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}
}
