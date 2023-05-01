package org.theperkinrex.layers.link;

public interface LinkAddr {
    enum LinkAddrKind {
        MAC
    }

    LinkAddrKind kind();
    LinkAddr zeroed();
    LinkAddr broadcast();
}
