package org.theperkinrex.applications.dhcp.leaser;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public record SimpleOffer<A>(@NotNull A addr, @NotNull Duration time) implements Offer<A> {}
