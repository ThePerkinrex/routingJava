package org.theperkinrex.routing;

import org.jetbrains.annotations.Nullable;
import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.util.Pair;

import java.util.Set;

public interface Router<A extends NetAddr> {
    Set<NextHop<A>> getRoutes(A addr);

    @Nullable
    A getRoute(A addr);
}
