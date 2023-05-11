package org.theperkinrex.net;

import org.theperkinrex.iface.IfaceNotConfiguredException;
import org.theperkinrex.routing.RouteNotFoundException;

public interface NetStream<P> extends AutoCloseable {
	void send(P payload) throws RouteNotFoundException, ConnectionClosedException, InterruptedException, IfaceNotConfiguredException;
	P receive() throws ConnectionResetException, ConnectionClosedException, InterruptedException;
}
