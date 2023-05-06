package org.theperkinrex.net;

public interface NetListener<P> extends AutoCloseable {
	NetStream<P> listen() throws InterruptedException, ConnectionClosedException;
}
