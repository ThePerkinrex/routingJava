package org.theperkinrex.applications.dhcp.leaser;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface Offer<A> {
	@NotNull
	A addr();

	@NotNull
	Duration time();
}
