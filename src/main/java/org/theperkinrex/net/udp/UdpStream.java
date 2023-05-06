package org.theperkinrex.net.udp;

import org.theperkinrex.net.ConnectionClosedException;
import org.theperkinrex.net.NetStream;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.transport.udp.UdpProcess;
import org.theperkinrex.routing.RouteNotFoundException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class UdpStream implements NetStream<Object> {
	public final short localPort;
	public final short remotePort;
	public final NetAddr remoteAddr;
	public final UdpProcess udpProcess;
	protected boolean closed;
	protected final BlockingQueue<Object> recvQueue;

	protected UdpStream(short localPort, short remotePort, NetAddr remoteAddr, UdpProcess udpProcess) {
		this.localPort = localPort;
		this.remotePort = remotePort;
		this.remoteAddr = remoteAddr;
		this.udpProcess = udpProcess;
		this.recvQueue = new LinkedBlockingQueue<>();
		this.closed = false;
	}

	@Override
	public void send(Object payload) throws RouteNotFoundException, InterruptedException, ConnectionClosedException {
		if (closed) throw new ConnectionClosedException();
		udpProcess.send(payload, remoteAddr, localPort, remotePort);
	}

	@Override
	public Object receive() throws ConnectionClosedException, InterruptedException {
		if (closed) throw new ConnectionClosedException();
		return recvQueue.take();
	}

	@Override
	public void close() {
		closed = true;
	}
}
