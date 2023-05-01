package org.theperkinrex.conf;

import org.theperkinrex.layers.net.NetAddr;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public class IfaceConf {
    private final ConcurrentHashMap<Class<? extends NetAddr>, NetAddr> addresses;

    public IfaceConf() {
        addresses = new ConcurrentHashMap<>();
    }

    public <A extends NetAddr> A getAddr(Class<A> c) {
        NetAddr a = addresses.get(c);
        if (a!=null) {
            assert a.getClass().equals(c);
            return (A) a;
        }else{
            return null;
        }
    }

    public <A extends NetAddr> boolean hasAddr(A addr) {
        return Objects.equals(getAddr((Class<A>) addr.getClass()), addr);
    }

    public <A extends NetAddr> void add(A addr) {
        addresses.put(addr.getClass(), addr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IfaceConf ifaceConf = (IfaceConf) o;

        return addresses.equals(ifaceConf.addresses);
    }

    @Override
    public int hashCode() {
        return addresses.hashCode();
    }

    @Override
    public String toString() {
        StringJoiner s = new StringJoiner("\n\t");
        for(NetAddr a : addresses.values()) {
            s.add(a.name() + " " + a);
        }
        return s.toString();
    }
}
