package org.theperkinrex.util;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DuplexChannel<I> {
    private final BlockingQueue<I> sender;
    private final BlockingQueue<I> receiver;

    public DuplexChannel(BlockingQueue<I> sender, BlockingQueue<I> receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public void send(I item) {
        this.sender.add(item);
    }

    public I receive() throws InterruptedException {
        return this.receiver.take();
    }

    public I peek() {
        return this.receiver.peek();
    }

    public static class ChannelPair<I> {
        public final DuplexChannel<I> a;
        public final DuplexChannel<I> b;

        private ChannelPair(DuplexChannel<I> a, DuplexChannel<I> b) {
            this.a = a;
            this.b = b;
        }
    }

    public static <I> ChannelPair<I> createPair() {
        BlockingQueue<I> a = new LinkedBlockingQueue<>();
        BlockingQueue<I> b = new LinkedBlockingQueue<>();

        return new ChannelPair<>(new DuplexChannel<>(a, b), new DuplexChannel<>(b, a));
    }
}
