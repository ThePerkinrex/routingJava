package org.theperkinrex.net.udp;

import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.transport.udp.UdpProcess;
import org.theperkinrex.util.listener.ListenerMap;

public class UdpClientStream extends UdpStream {
	private final NetAddr localAddr;
	public UdpClientStream(short localPort, short remotePort, NetAddr localAddr, NetAddr remoteAddr, UdpProcess udpProcess)
			throws ListenerMap.PortAlreadyInUseException {
		super(localPort, remotePort, remoteAddr, udpProcess);
		this.localAddr = localAddr;
		udpProcess.registerListener(localAddr, localPort, (NetAddr sourceAddr, short sourcePort, Object payload) -> {
			if (sourceAddr.equals(remoteAddr) && sourcePort == remotePort) {
				this.recvQueue.add(payload);
			}else{
				System.out.println("Udp datagram from unknown source dropped");
			}
		});
	}

	@Override
	public void close() {
		udpProcess.unregisterListener(localAddr, localPort);
		super.close();
	}
}
