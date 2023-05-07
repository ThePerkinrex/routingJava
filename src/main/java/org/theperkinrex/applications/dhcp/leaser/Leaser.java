package org.theperkinrex.applications.dhcp.leaser;

import org.jetbrains.annotations.NotNull;
import org.theperkinrex.applications.dhcp.leaser.exceptions.NoLeasableAddress;
import org.theperkinrex.applications.dhcp.leaser.exceptions.NoLeasedAddress;
import org.theperkinrex.applications.dhcp.leaser.exceptions.NoOfferedAddress;
import org.theperkinrex.layers.link.LinkAddr;

public interface Leaser<A> {
	@NotNull
	Offer<A> offer(@NotNull LinkAddr addr) throws NoLeasableAddress, InterruptedException;

	void decline(@NotNull LinkAddr addr) throws NoOfferedAddress, InterruptedException;
	@NotNull
	Lease<A> lease(@NotNull LinkAddr addr) throws NoOfferedAddress, InterruptedException;
	void release(@NotNull LinkAddr addr) throws NoLeasedAddress, InterruptedException;
}
