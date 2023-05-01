package org.theperkinrex.layers.net;

import java.util.Comparator;

public interface NetMask<A extends NetAddr> {
    int order();
    boolean matchesMasked(A a, A b);
}
