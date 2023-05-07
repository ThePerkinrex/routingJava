package org.theperkinrex.applications.dhcp.leaser;

import org.jetbrains.annotations.NotNull;

public record SimpleOffer<A>(@NotNull A addr) implements Offer<A> {}
