package org.theperkinrex.routing;

import org.jetbrains.annotations.NotNull;
import org.theperkinrex.layers.net.NetAddr;

import java.util.Objects;

public class NextHop<A extends NetAddr> {
    private final A value;

    private NextHop(A value) {
        this.value = value;
    }

    public A getNextHopOrNull() {
        return this.value;
    }

    public boolean isDirect() {
        return this.value == null;
    }

    public static <A extends NetAddr> NextHop<A> direct() {
        return new NextHop<>(null);
    }

    public static <A extends NetAddr> NextHop<A> to(@NotNull A nextHop) {
        return new NextHop<>(nextHop);
    }

    @NotNull
    public A unwrapOr(A directAddr) {
        if (this.value == null) {
            return directAddr;
        }
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NextHop<?> nextHop = (NextHop<?>) o;

        return Objects.equals(value, nextHop.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        if (this.value != null)
            return this.value.toString();
        return "DIRECT";
    }
}
