package org.theperkinrex.net;

import org.theperkinrex.routing.RouteNotFoundException;

public interface NetStream<P> extends AutoCloseable {
	void send(P payload) throws RouteNotFoundException, ConnectionClosedException, InterruptedException;
	P receive() throws ConnectionResetException, ConnectionClosedException, InterruptedException;
}
