package org.theperkinrex.net.udp;

import org.theperkinrex.net.ConnectionClosedException;
import org.theperkinrex.net.NetListener;
import org.theperkinrex.net.NetStream;
import org.jetbrains.annotations.NotNull;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.transport.udp.UdpProcess;
import org.theperkinrex.util.Pair;
import org.theperkinrex.util.listener.ListenerMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class UdpListener implements NetListener<Object> {
	private final ConcurrentMap<Pair<NetAddr, Short>, UdpListenerStream> currentStreams;
	private final UdpProcess udpProcess;
	private final NetAddr listenAddr;
	private final short listenPort;
	private boolean closed;

	private final BlockingQueue<UdpListenerStream> connQueue;

	public UdpListener(@NotNull UdpProcess udpProcess, NetAddr listenAddr, short listenPort)
			throws ListenerMap.PortAlreadyInUseException {
		this.listenAddr = listenAddr;
		this.listenPort = listenPort;
		currentStreams = new ConcurrentHashMap<>();
		this.udpProcess = udpProcess;
		connQueue = new LinkedBlockingQueue<>();
		closed = false;
		var listener = listener();
		if (listenAddr != null)
			udpProcess.registerListener(listenAddr, listenPort, listener);
		else udpProcess.registerListener(listenPort, listener());
	}

	public UdpListener(@NotNull UdpProcess udpProcess, short listenPort) throws ListenerMap.PortAlreadyInUseException {
		this(udpProcess, null, listenPort);
	}

	private org.theperkinrex.layers.transport.udp.UdpListener listener() {
		return (NetAddr sourceAddr, short sourcePort, Object payload) -> {
			var key = new Pair<>(sourceAddr, sourcePort);
			if (currentStreams.containsKey(key)) {
				currentStreams.get(key).recvQueue.add(payload);
			} else {
				var stream = new UdpListenerStream(listenPort, sourcePort, sourceAddr, udpProcess);
				stream.recvQueue.add(payload);
				currentStreams.put(key, stream);
				connQueue.add(stream);
			}
		};
	}

	private class UdpListenerStream extends UdpStream {
		public UdpListenerStream(short localPort, short remotePort, NetAddr remoteAddr, UdpProcess udpProcess) {
			super(localPort, remotePort, remoteAddr, udpProcess);
		}

		@Override
		public void close() {
			currentStreams.remove(new Pair<>(remoteAddr, remotePort));
			super.close();
		}
	}

	@Override
	public UdpStream listen() throws InterruptedException, ConnectionClosedException {
		if (closed) throw new ConnectionClosedException();
		return connQueue.take();
	}

	@Override
	public void close() {
		closed = true;
		if (listenAddr != null) udpProcess.unregisterListener(listenAddr, listenPort);
		else udpProcess.unregisterListener(listenPort);

		for (var stream : currentStreams.values()) {
			stream.close();
		}
	}
}
