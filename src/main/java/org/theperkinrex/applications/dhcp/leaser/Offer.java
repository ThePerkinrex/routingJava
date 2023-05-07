package org.theperkinrex.applications.dhcp.leaser;

import org.jetbrains.annotations.NotNull;

public interface Offer<A> {
	@NotNull
	A addr();
}
