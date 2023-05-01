package org.theperkinrex.routing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.theperkinrex.components.Chassis;
import org.theperkinrex.iface.Iface;
import org.theperkinrex.layers.link.LinkAddr;
import org.theperkinrex.layers.net.NetAddr;
import org.theperkinrex.layers.net.NetMask;
import org.theperkinrex.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RoutingTable<A extends NetAddr> implements Router<A> {
    private final ConcurrentNavigableMap<Pair<A, NetMask<A>>, Pair<Chassis.IfaceId<Iface<LinkAddr>>, NextHop<A>>> table;

    public RoutingTable() {
        table = new ConcurrentSkipListMap<>((o1, o2) -> o2.u.order() - o1.u.order());
    }

    private void add(A addr, NetMask<A> mask, Chassis.IfaceId<Iface<LinkAddr>> ifaceId, NextHop<A> nextHop) {
        table.put(new Pair<>(addr, mask), new Pair<>(ifaceId, nextHop));
    }

    public <LA extends LinkAddr,I extends Iface<LA>> void add(A addr, NetMask<A> mask, Chassis.IfaceId<I> ifaceId, A nextHop) {
        add(addr, mask, (Chassis.IfaceId<Iface<LinkAddr>>) ifaceId, NextHop.to(nextHop));
    }

    public <LA extends LinkAddr,I extends Iface<LA>> void add(A addr, NetMask<A> mask, Chassis.IfaceId<I> ifaceId) {
        add(addr, mask,(Chassis.IfaceId<Iface<LinkAddr>>)  ifaceId, NextHop.direct());
    }

    public void print() {
        for (Map.Entry<Pair<A, NetMask<A>>, Pair<Chassis.IfaceId<Iface<LinkAddr>>, NextHop<A>>> entry : table.entrySet()) {

            System.out.println(entry.getKey().t.toString() + entry.getKey().u.toString() + " -> " + entry.getValue());
        }
    }

    private class RoutePredicate implements Predicate<Map.Entry<Pair<A, NetMask<A>>, Pair<Chassis.IfaceId<Iface<LinkAddr>>, NextHop<A>>>> {
        private NetMask<A> currentMask;


        @Override
        public boolean test(Map.Entry<Pair<A, NetMask<A>>, Pair<Chassis.IfaceId<Iface<LinkAddr>>, NextHop<A>>> entry) {
            boolean r = currentMask == null || currentMask.order() == entry.getKey().u.order();
            if(currentMask == null) currentMask = entry.getKey().u;
            return r;
        }

        public RoutePredicate() {
            this.currentMask = null;
        }
    }

    public Set<Pair<Chassis.IfaceId<Iface<LinkAddr>>, NextHop<A>>> getRoutes(A addr) {
        return table
                .entrySet()
                .stream()
//                .peek(entry -> System.out.println(addr + " " + entry.getKey() + " " + entry.getKey().u.matchesMasked(addr, entry.getKey().t)))
                .dropWhile(entry -> !entry.getKey().u.matchesMasked(addr, entry.getKey().t))
                .takeWhile(new RoutePredicate())
                .map(Map.Entry::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Nullable
    public Pair<Chassis.IfaceId<Iface<LinkAddr>>, A> getRoute(A addr) {
        Set<Pair<Chassis.IfaceId<Iface<LinkAddr>>, NextHop<A>>> routes = getRoutes(addr);
        if (routes.isEmpty()) return null;
        return routes.iterator().next().map((a,b) -> new Pair<>(a, b.unwrapOr(addr)));
    }

    @Override
    public String toString() {
        return "RoutingTable{" +
                "table=" + table +
                '}';
    }
}
