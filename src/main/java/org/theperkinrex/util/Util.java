package org.theperkinrex.util;

import java.util.function.Function;

public class Util {
    public static <T,U> U mapOrNull(T value, Function<T, U> f) {
        return value == null ? null : f.apply(value);
    }
}
