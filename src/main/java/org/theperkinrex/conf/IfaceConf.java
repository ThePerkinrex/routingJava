package org.theperkinrex.conf;

import org.jetbrains.annotations.NotNull;
import org.theperkinrex.iface.IfaceConfigurer;
import org.theperkinrex.layers.net.NetAddr;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public class IfaceConf {
    public record Address<A extends NetAddr>(@NotNull A address, IfaceConfigurer configurer){
        public <B extends A> Address<B> cast() {
            return new Address<>((B) address(), configurer());
        }
    }

    private final ConcurrentHashMap<Class<? extends NetAddr>, Address<NetAddr>> addresses;


    public IfaceConf() {
        addresses = new ConcurrentHashMap<>();
    }

    public <A extends NetAddr> A getAddr(Class<A> c) {
        var a = addresses.get(c);
        if (a!=null) {
            assert a.address().getClass().equals(c);
            return a.configurer() == null ? ((A) a.address()) : null;
        }else{
            return null;
        }
    }

    public <A extends NetAddr> Address<A> getAddrUnconfigured(Class<A> c) {
        var a = addresses.get(c);
        if (a!=null) {
            assert a.address().getClass().equals(c);
            return a.cast();
        }else{
            return null;
        }
    }

    public <A extends NetAddr> boolean hasAddr(A addr) {
        return Objects.equals(getAddr((Class<A>) addr.getClass()), addr);
    }

    public <A extends NetAddr> void add(A addr) {
        addresses.put(addr.getClass(), new Address<>(addr, null));
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
        for(var a : addresses.values()) {
            s.add(a.address().name() + (a.configurer() == null ? "" : " CONFIGURING") + " " + a);
        }
        return s.toString();
    }
}
