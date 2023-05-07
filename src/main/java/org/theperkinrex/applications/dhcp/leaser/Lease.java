package org.theperkinrex.applications.dhcp.leaser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

public interface Lease<A> {
	@NotNull
	A addr();
	@Nullable Instant end();
}
