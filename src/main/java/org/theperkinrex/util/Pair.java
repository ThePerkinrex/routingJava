package org.theperkinrex.util;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Pair<T,U> {
    public final T t;
    public final U u;

    public Pair(T t, U u) {
        this.t = t;
        this.u = u;
    }

    public <R> R map(BiFunction<T,U,R> f) {
        return f.apply(t,u);
    }

    public void consume(BiConsumer<T,U> c) {
        c.accept(t, u);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!Objects.equals(t, pair.t)) return false;
        return Objects.equals(u, pair.u);
    }

    @Override
    public int hashCode() {
        int result = t != null ? t.hashCode() : 0;
        result = 31 * result + (u != null ? u.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "(" + t + ", " + u + ")";
    }
}
