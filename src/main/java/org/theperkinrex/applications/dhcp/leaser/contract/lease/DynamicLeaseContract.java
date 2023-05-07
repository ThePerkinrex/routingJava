package org.theperkinrex.applications.dhcp.leaser.contract.lease;

import org.theperkinrex.layers.link.LinkAddr;

import java.time.Instant;

public class DynamicLeaseContract extends LeaseContract {
	public final Instant end;

	public DynamicLeaseContract(LinkAddr linkAddr, Instant end) {
		super(linkAddr);
		this.end = end;
	}

	@Override
	public String toString() {
		return "DynamicLeaseContract{" + "end=" + end + ", linkAddr=" + linkAddr + '}';
	}
}
